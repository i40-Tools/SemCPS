
import java.nio.file.Paths

import org.linqs.psl.config.ConfigBundle
import org.linqs.psl.config.ConfigManager
import org.linqs.psl.database.DataStore
import org.linqs.psl.database.Database
import org.linqs.psl.database.Partition
import org.linqs.psl.database.rdbms.RDBMSDataStore
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import org.linqs.psl.groovy.PSLModel
import org.linqs.psl.model.term.ConstantType
import org.linqs.psl.utils.dataloading.InserterUtils
import org.linqs.psl.utils.evaluation.statistics.ContinuousPredictionComparator
import org.linqs.psl.utils.evaluation.statistics.DiscretePredictionComparator
import org.linqs.psl.utils.evaluation.statistics.DiscretePredictionStatistics
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A simple EasyLP example.
 * In this example, we try to determine if two people know each other.
 * The model uses two features: where the people lived and what they like.
 * The model also has options to include symmetry and transitivity rules.
 *
 * @author Jay Pujara <jay@cs.umd.edu>
 */

public class EasyLP
{
	private static final String PARTITION_OBSERVATIONS = "observations"
	private static final String PARTITION_TARGETS = "targets"
	private static final String PARTITION_TRUTH = "truth"

	private Logger log

	/**
	 * Class for config variables
	 */
	private class PSLConfig
	{
		public ConfigBundle cb

		public String experimentName
		public String dbPath
		public String dataPath
		public String outputPath


		public PSLConfig(ConfigBundle cb)
		{
			this.cb = cb

			this.experimentName = cb.getString('experiment.name', 'default')
			this.dbPath = cb.getString('experiment.dbpath', '/tmp')
			this.dataPath = cb.getString('experiment.data.path', util.ConfigManager.getFilePath()+"PSL/test/")
			this.outputPath = cb.getString('experiment.output.outputdir', Paths.get('data', this.experimentName).toString())
		}
	}

	private void evalResults2()
	{

		DataStore ds
		PSLConfig config
		PSLModel model
		ConfigBundle cb = ConfigManager.getManager().getBundle("easylp")

		log = LoggerFactory.getLogger(this.class)
		config = new PSLConfig(cb)
		ds = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, Paths.get(config.dbPath, 'easylp').
			toString(), true), cb)
		model = new PSLModel(this, ds)


		model.add predicate: "eval2", types: [ConstantType.UniqueID, ConstantType.UniqueID]


		Partition truthPartition = ds.getPartition("truthss")
		
		Partition targetsPartition = ds.getPartition("t")

		def	inserter = ds.getInserter(eval2, targetsPartition)
		InserterUtils.loadDelimitedDataTruth(inserter, Paths.get
			(config.dataPath, "similar.txt").toString())

		inserter = ds.getInserter(eval2, truthPartition)
		InserterUtils.loadDelimitedDataTruth(inserter, Paths.get
			(config.dataPath, "GoldStandard.txt").toString())


		Database resultsDB = ds.getDatabase(targetsPartition, [eval2] as Set)
		Database truthDB = ds.getDatabase(truthPartition, [eval2] as Set)
		DiscretePredictionComparator dpc = new DiscretePredictionComparator(resultsDB)
		ContinuousPredictionComparator cpc = new ContinuousPredictionComparator(resultsDB)
		dpc.setBaseline(truthDB)
		//	 dpc.setThreshold(0.99);
		cpc.setBaseline(truthDB)
		DiscretePredictionStatistics stats = dpc.compare(eval2)
		double mse = cpc.compare(eval2)
		log.info("MSE: {}", mse)
		log.info("Accuracy {}, Error {}",stats.getAccuracy(), stats.getError())
		System.out.println("Accuracy:"+stats.getAccuracy())
		System.out.println("Error:"+stats.getError()+mse)
		System.out.println("Fmeasure:"+stats.getF1
			(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("True negative:"+stats.tn)
		System.out.println("True Positive:"+stats.tp)
		System.out.println("False Positive:"+stats.fp)
		System.out.println("False Negative:"+stats.fn)

		System.out.println("Precision (Positive):"+stats.getPrecision
			(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Recall: (Positive)"+stats.getRecall
			(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Precision:(Negative)"+stats.getPrecision
			(DiscretePredictionStatistics.BinaryClass.NEGATIVE))
		System.out.println("Recall:(Negative)"+stats.getRecall
			(DiscretePredictionStatistics.BinaryClass.NEGATIVE))

		resultsDB.close()
		truthDB.close()
	}

	public static void main(String[] args)
	{
		EasyLP docAlign = new EasyLP()
		docAlign.evalResults2()
	}
}
