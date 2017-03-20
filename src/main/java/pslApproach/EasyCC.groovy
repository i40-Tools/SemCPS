package pslApproach;

import java.nio.file.Paths;

import org.linqs.psl.application.inference.MPEInference;
import org.linqs.psl.config.ConfigBundle;
import org.linqs.psl.config.ConfigManager;
import org.linqs.psl.database.DataStore;
import org.linqs.psl.database.Database;
import org.linqs.psl.database.Partition;
import org.linqs.psl.database.Queries;
import org.linqs.psl.database.ReadOnlyDatabase
import org.linqs.psl.database.loading.Inserter;
import org.linqs.psl.database.rdbms.RDBMSDataStore;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import org.linqs.psl.groovy.PSLModel;
import org.linqs.psl.model.atom.Atom;
import org.linqs.psl.model.function.ExternalFunction
import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.term.Constant
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.utils.dataloading.InserterUtils;
import org.linqs.psl.utils.evaluation.printing.AtomPrintStream;
import org.linqs.psl.utils.evaluation.printing.DefaultAtomPrintStream;
import org.linqs.psl.utils.evaluation.statistics.DiscretePredictionComparator;
import org.linqs.psl.utils.evaluation.statistics.DiscretePredictionStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.time.TimeCategory;

/**
 * A simple Collective Classification example that mirrors the example for the
 * PSL command line tool.
 * This PSL program uses social relationships to determine where people live.
 * It optionally uses a functional constraint that specified that a person
 * can only live in one location.
 *
 * @author Jay Pujara <jay@cs.umd.edu>
 */
public class EasyCC {
	private static final String PARTITION_OBSERVATIONS = "observations";
	private static final String PARTITION_TARGETS = "targets";
	private static final String PARTITION_TRUTH = "truth";

	private Logger log;
	private DataStore ds;
	private PSLConfig config;
	private PSLModel model;

	/**
	 * Class containing options for configuring the PSL program
	 */
	private class PSLConfig {
		public ConfigBundle cb;

		public String experimentName;
		public String dbPath;
		public String dataPath;
		public String outputPath;

		public boolean sqPotentials = true;
		public Map weightMap = [
			"Knows":10,
			"Prior":2
		];
		public boolean useFunctionalConstraint = false;
		public boolean useFunctionalRule = false;

		public PSLConfig(ConfigBundle cb){
			this.cb = cb;

			this.experimentName = cb.getString('experiment.name', 'default');
			this.dbPath = cb.getString('experiment.dbpath', '/tmp');
			this.dataPath = cb.getString('experiment.data.path', 'data');
			this.outputPath = cb.getString('experiment.output.outputdir', Paths.get('output', this.experimentName).toString());

			this.weightMap["Knows"] = cb.getInteger('model.weights.knows', weightMap["Knows"]);
			this.weightMap["Prior"] = cb.getInteger('model.weights.prior', weightMap["Prior"]);
			this.useFunctionalConstraint = cb.getBoolean('model.constraints.functional', false);
			this.useFunctionalRule = cb.getBoolean('model.rules.functional', false);
		}
	}

	public EasyCC(ConfigBundle cb) {
		log = LoggerFactory.getLogger(this.class);
		config = new PSLConfig(cb);
		ds = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, Paths.get(config.dbPath, 'easycc').toString(), true), cb);
		model = new PSLModel(this, ds);
	}

	/**
	 * Defines the logical predicates used by this program
	 */
	private void definePredicates() {
		model.add predicate: "Lives", types: [
			ConstantType.String,
			ConstantType.String
		];
		model.add predicate: "Knows", types: [
			ConstantType.String,
			ConstantType.String
		];



		model.add function: "similarValue"  ,implementation:new MyStringSimilarity();
	}

	/**
	 * Defines the rules used to infer unknown variables in the PSL program
	 */
	private void defineRules() {
		log.info("Defining model rules");

		model.add(
				rule:( Knows(P1,P2) & Lives(P1,L)) >> Lives(P2,L),
				squared: config.sqPotentials,
				weight : config.weightMap["Knows"]

				);




		log.debug("model: {}", model);
	}




	/**
	 * Loads the evidence, inference targets, and evaluation data into the DataStore
	 */
	private void loadData(Partition obsPartition, Partition targetsPartition, Partition truthPartition) {
		log.info("Loading data into database");

		Inserter inserter = ds.getInserter(Knows, obsPartition);
		InserterUtils.loadDelimitedData(inserter, Paths.get(config.dataPath, "knows_obs.txt").toString());

		inserter = ds.getInserter(Lives, obsPartition);
		InserterUtils.loadDelimitedData(inserter, Paths.get(config.dataPath, "lives_obs.txt").toString());

		inserter = ds.getInserter(Lives, targetsPartition);
		InserterUtils.loadDelimitedData(inserter, Paths.get(config.dataPath, "lives_targets.txt").toString());

		inserter = ds.getInserter(Lives, truthPartition);
		InserterUtils.loadDelimitedData(inserter, Paths.get(config.dataPath, "lives_truth.txt").toString());
	}

	/**
	 * Performs inference using the defined model and evidence, storing results in DataStore
	 */
	private void runInference(Partition obsPartition, Partition targetsPartition) {
		log.info("Starting inference");

		Date infStart = new Date();
		HashSet closed = new HashSet<StandardPredicate>([Knows]);
		Database inferDB = ds.getDatabase(targetsPartition, closed, obsPartition);
		MPEInference mpe = new MPEInference(model, inferDB, config.cb);
		mpe.mpeInference();

		inferDB.close();
		mpe.close();

		log.info("Finished inference in {}", TimeCategory.minus(new Date(), infStart));
	}

	/**
	 * Writes the inference outputs to a file
	 */
	private void writeOutput(Partition targetsPartition) {
		Database resultsDB = ds.getDatabase(targetsPartition);
		PrintStream ps = new PrintStream(new File(Paths.get(config.outputPath, "lives_infer.txt").toString()));
		AtomPrintStream aps = new DefaultAtomPrintStream(ps);
		Set atomSet = Queries.getAllAtoms(resultsDB, Lives);
		for (Atom a : atomSet) {
			aps.printAtom(a);
		}

		aps.close();
		ps.close();
		resultsDB.close();
	}

	/**
	 * Evaluates the results of inference versus expected truth values
	 */
	private void evalResults(Partition targetsPartition, Partition truthPartition) {
		Database resultsDB = ds.getDatabase(targetsPartition, [Lives] as Set);
		Database truthDB = ds.getDatabase(truthPartition, [Lives] as Set);
		DiscretePredictionComparator dpc = new DiscretePredictionComparator(resultsDB);
		dpc.setBaseline(truthDB);
		DiscretePredictionStatistics stats = dpc.compare(Lives);
		log.info(
				"Stats: precision {}, recall {}",
				stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE),
				stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE));

		resultsDB.close();
		truthDB.close();
	}


	/**
	 * Runs the PSL program using configure options - defines a model, loads data,
	 * performs inferences, writes output to files, evaluates results
	 */
	public void run() {
		log.info("Running experiment {}", config.experimentName);

		Partition obsPartition = ds.getPartition(PARTITION_OBSERVATIONS);
		Partition targetsPartition = ds.getPartition(PARTITION_TARGETS);
		Partition truthPartition = ds.getPartition(PARTITION_TRUTH);

		definePredicates();
		defineRules();
		loadData(obsPartition, targetsPartition, truthPartition);
		runInference(obsPartition, targetsPartition);
		writeOutput(targetsPartition);
		evalResults(targetsPartition, truthPartition);

		ds.close();
	}

	public EasyCC(){
	}

	public void execute(){
		ConfigBundle cb = ConfigManager.getManager().getBundle("easycc");
		EasyCC cc = new EasyCC(cb);
		cc.run();
	}

	/**
	 * Runs the PSL program from the command line with specified arguments
	 * @param args - Arguments for program options
	 */
	public static void main(){
		ConfigBundle cb = ConfigManager.getManager().getBundle("easycc");
		EasyCC cc = new EasyCC(cb);
		cc.run();
	}

	public class MyStringSimilarity implements ExternalFunction {
		@Override
		public int getArity() {
			return 2;
		}

		@Override
		public ConstantType[] getArgumentTypes() {
			return [
				ConstantType.String,
				ConstantType.String
			].toArray();
		}

		@Override
		public double getValue(ReadOnlyDatabase db, Constant... args) {
			return args[0].toString().equals(args[1].toString()) ? 1.0 : 0.0;
		}
	}
}




