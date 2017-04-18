
package pslApproach
import java.text.DecimalFormat

import edu.umd.cs.psl.application.inference.MPEInference
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE
import edu.umd.cs.psl.config.*
import edu.umd.cs.psl.database.DataStore
import edu.umd.cs.psl.database.Database
import edu.umd.cs.psl.database.Partition
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import edu.umd.cs.psl.evaluation.statistics.DiscretePredictionComparator
import edu.umd.cs.psl.evaluation.statistics.DiscretePredictionStatistics
import edu.umd.cs.psl.groovy.*
import edu.umd.cs.psl.model.argument.ArgumentType
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.argument.type.*
import edu.umd.cs.psl.model.atom.GroundAtom
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.predicate.Predicate
import edu.umd.cs.psl.model.predicate.type.*
import edu.umd.cs.psl.ui.functions.textsimilarity.*
import edu.umd.cs.psl.ui.loading.InserterUtils
import edu.umd.cs.psl.util.database.Queries


public class DocumentAligment
{

	private ConfigManager cm
	private ConfigBundle config
	private Database testDB
	private Database trainDB
	private Database truthDB
	private PSLModel model
	private DataStore data
	private Partition testObservations
	private Partition testPredictions
	private Partition targetsPartition
	private Partition truthPartition
	def dir
	def testDir
	def trainDir

	public static void main(String[] args)
	{
		DocumentAligment docAlign = new DocumentAligment()
		docAlign.execute()
	}

	public void run()
	{
		config()
		definePredicates()
		defineFunctions()
		defineRules()
		setUpData()
		populateSimilar(testDB)
		runInference()

		evalResults(targetsPartition, truthPartition)
	}

	public void execute()
	{
		DocumentAligment documentAligment = new DocumentAligment()
		documentAligment.run()
	}

	public void config()
	{
		cm = ConfigManager.getManager()
		config = cm.getBundle("ontology-alignment")

		def defaultPath = System.getProperty("java.io.tmpdir")
		String dbpath = config.getString("dbpath", defaultPath + File.separator + "ontology-alignment")
		// EQ
		// DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)
		data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)
		model = new PSLModel(this, data)
	}

	public void definePredicates()
	{
		model.add predicate: "name"        , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "fromDocument", types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "Attribute"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "InternalElements"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasRefSemantic"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasID"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasInternalLink"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassVersion"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassClassificationClass"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassIRDI"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "similar"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "similarType"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
		model.add predicate: "eval", types: [ArgumentType.String, ArgumentType.String]

		model.add predicate: "RoleClass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
		model.add predicate: "Interfaceclass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
		model.add predicate: "SystemUnitclass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
	}


	public void defineFunctions()
	{
		model.add function: "similarValue"  , implementation: new LevenshteinSimilarity()
	}

	public void defineRules()
	{

		// Two AML Attributes are the same if its RefSemantic are the same
		model.add rule : (Attribute(A,X) & Attribute(B,Y) & hasRefSemantic(X,Z)
		& hasRefSemantic(Y,W) & similarValue(Z,W) & fromDocument(A,O1) & fromDocument(B,O2) &
		(O1-O2)) >> similar(A,B) , weight : 10

		// Two AMl Attributes are the same if they share the same ID
		model.add rule : (Attribute(A,X) & Attribute(B,Y) & hasID(A,Z) & hasID(B,W)
		& similarValue(Z,W) &
		fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 5

		// Two AMl InternalElements are the same if they share the same ID
		model.add rule : (InternalElements(A,X) & InternalElements(B,Y) & hasID(A,Z) & hasID(B,W)
		& similarValue(Z,W) &fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) ,
		weight : 5


		// Two Roles Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( RoleClass(A1,B1) & RoleClass(A2,B2) & Attribute(B1,X) & Attribute(B2,Y)
		& hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& RoleClass(A1,C1) & RoleClass(A2,D2) & Attribute(C1,Q) & Attribute(D2,T) &
		hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)& RoleClass(A1,E1) &
		RoleClass(A2,F2) &Attribute(E1,D) & Attribute(F2,K) & hasEClassVersion(E1,O) &
		hasEClassVersion(F2,L) & similarValue(O,L)& fromDocument(A1,O1) & fromDocument(A2,O2) &
		(O1-O2)) >> similar(A1,A2) , weight : 12


		// Two Interface Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( Interfaceclass(A1,B1) & Interfaceclass(A2,B2) & Attribute(B1,X)
		& Attribute(B2,Y)  & hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& Interfaceclass(A1,C1) & Interfaceclass(A2,D2) & Attribute(C1,Q) & Attribute(D2,T)
		& hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)
		& Interfaceclass(A1,E1) & Interfaceclass(A2,F2) &Attribute(E1,D) & Attribute(F2,K)
		& hasEClassVersion(E1,O) & hasEClassVersion(F2,L) & similarValue(O,L)
		& fromDocument(A1,O1) & fromDocument(A2,O2) & (O1-O2)) >> similar(A1,A2) , weight : 100


		// Two SystemUnit Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( SystemUnitclass(A1,B1) & SystemUnitclass(A2,B2) & Attribute(B1,X)
		& Attribute(B2,Y)  & hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& SystemUnitclass(A1,C1) & SystemUnitclass(A2,D2) & Attribute(C1,Q) & Attribute(D2,T)
		& hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)&
		SystemUnitclass(A1,E1) & SystemUnitclass(A2,F2) &Attribute(E1,D) & Attribute(F2,K) &
		hasEClassVersion(E1,O) & hasEClassVersion(F2,L) & similarValue(O,L)
		& fromDocument(A1,O1) & fromDocument(A2,O2) & (O1-O2)) >> similar(A1,A2) , weight : 12


		//Two InternalElements are the same if its InternalLink is the same
		//		model.add rule : (InternalElements(A,X) & InternalElements(B,Y)  & hasInternalLink(X,Z) & hasInternalLink(Y,W) &
		//		similarValue(Z,W) & fromDocument(A,O1) & fromDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 12;

		// constraints
		model.add PredicateConstraint.PartialFunctional , on : similar
		model.add PredicateConstraint.PartialInverseFunctional , on : similar
		model.add PredicateConstraint.Symmetric , on : similar

		// prior
		model.add rule : ~similar(A,B), weight: 1



	}


	public void setUpData()
	{
		GroundTerm classID = data.getUniqueID("class")

		/* Loads data */
		dir = 'data' + java.io.File.separator + 'ontology' + java.io.File.separator

		/////////////////////////// test setup //////////////////////////////////

		testDir = dir + 'test' + java.io.File.separator
		trainDir = dir + 'train' + java.io.File.separator
		Partition trainObservations = new Partition(0)
		Partition trainPredictions = new Partition(1)
		Partition truth = new Partition(2)
		for (Predicate p : [fromDocument, name, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, roleClass, InternalElements, SystemUnitclass, Interfaceclass])
		{
			def insert = data.getInserter(p, trainObservations)

			InserterUtils.loadDelimitedData(insert, trainDir + p.getName().toLowerCase() + ".txt")

		}


		def insert = data.getInserter(similar, truth)
		InserterUtils.loadDelimitedDataTruth(insert, trainDir+"similar.txt")

		trainDB = data.getDatabase(trainPredictions, [name, fromDocument, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, roleClass, hasEClassIRDI, InternalElements, SystemUnitclass, Interfaceclass

		] as Set, trainObservations)
		populateSimilar(trainDB)
		truthDB = data.getDatabase(truth, [similar] as Set)


		println "LEARNING WEIGHTS..."
		MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(model, trainDB, truthDB, config)
		weightLearning.learn()
		weightLearning.close()
		println "LEARNING WEIGHTS DONE"



		Partition testObservations = new Partition(3)
		Partition testPredictions = new Partition(4)
		for (Predicate p : [fromDocument, name, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, roleClass, InternalElements, SystemUnitclass, Interfaceclass])
		{
			insert = data.getInserter(p, testObservations)

			InserterUtils.loadDelimitedData(insert, testDir + p.getName().toLowerCase() + ".txt")

		}


		testDB = data.getDatabase(testPredictions, [name, fromDocument, Attribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, roleClass, hasEClassIRDI, InternalElements, SystemUnitclass, Interfaceclass

		] as Set, testObservations)

		populateSimilar(testDB)
	}

	public void runInference(){
		/////////////////////////// test inference //////////////////////////////////
		println "INFERRING..."

		MPEInference inference = new MPEInference(model, testDB, config)
		inference.mpeInference()
		inference.close()

		println "INFERENCE DONE"
		def file1 = new File('data/ontology/test/similar.txt')
		file1.write('')
		def file2 = new File('data/ontology/test/similarwithConfidence.txt')
		file2.write('')
		DecimalFormat formatter = new DecimalFormat("#.##")
		for (GroundAtom atom : Queries.getAllAtoms(testDB, similar)){
			println atom.toString() + ": " + formatter.format(atom.getValue())
			// only writes if its equal to 1 or u can set the threshold
			if(formatter.format(atom.getValue())>"0.3"){
				println 'matches threshold writing to similar.txt'
				// converting to format for evaluation
				String result=atom.toString().replaceAll("SIMILAR","")
				result=result.replaceAll("[()]","")
				String[] text=result.split(",")
				result=text[0]+"\t"+text[1]
				String result2=text[0]+"\t"+text[1]+" "+atom.getValue()

				file1.append(result+'\n')
				file2.append(result2+'\n')

			}
		}
	}


	/**
	 * Evaluates the results of inference versus expected truth values
	 */
	private void evalResults(Partition targetsPartition, Partition truthPartition) {
		targetsPartition = new Partition(5)
		truthPartition = new Partition(6)

		def insert = data.getInserter(eval, targetsPartition)
		InserterUtils.loadDelimitedData(insert, testDir + "GoldStandard.txt")

		insert = data.getInserter(eval, truthPartition)
		InserterUtils.loadDelimitedData(insert, testDir + "similar.txt")

		Database resultsDB = data.getDatabase(targetsPartition, [eval] as Set)
		Database truthDB = data.getDatabase(truthPartition, [eval] as Set)
		DiscretePredictionComparator dpc = new DiscretePredictionComparator(resultsDB)
		dpc.setBaseline(truthDB)
		DiscretePredictionStatistics stats = dpc.compare(eval)


		System.out.println("Precision (Positive):"+stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Recall: (Positive)"+stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Precision:(Negative)"+stats.getPrecision(DiscretePredictionStatistics.BinaryClass.NEGATIVE))
		System.out.println("Recall:(Negative)"+stats.getRecall(DiscretePredictionStatistics.BinaryClass.NEGATIVE))



		resultsDB.close()
		truthDB.close()
	}


	/**
	 * Populates all the similar atoms between the concepts of two Documents using
	 * the fromDocument predicate.
	 *
	 * @param db  The database to populate. It should contain the fromDocument atoms
	 */
	void populateSimilar(Database db) {
		/* Collects the ontology concepts */
		Set<GroundAtom> concepts = Queries.getAllAtoms(db, fromDocument)
		Set<GroundTerm> o1 = new HashSet<GroundTerm>()
		Set<GroundTerm> o2 = new HashSet<GroundTerm>()
		for (GroundAtom atom : concepts) {
			if (atom.getArguments()[1].toString().equals("aml1"))
				o1.add(atom.getArguments()[0])
			else
				o2.add(atom.getArguments()[0])
		}

		/* Populates manually (as opposed to using DatabasePopulator) */
		for (GroundTerm o1Concept : o1) {
			for (GroundTerm o2Concept : o2) {
				((RandomVariableAtom) db.getAtom(similar, o1Concept, o2Concept)).commitToDB()
				((RandomVariableAtom) db.getAtom(similar, o2Concept, o1Concept)).commitToDB()
			}
		}
	}

}