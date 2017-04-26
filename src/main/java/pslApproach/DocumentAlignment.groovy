
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

/**
 * @author Omar Rana
 * @author Irlan Grangel
 * Computes the alignment of two entities based on Probabilistic Soft Logic(PSL)
 *
 */
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
		config = cm.getBundle("document-alignment")
		def defaultPath = System.getProperty("java.io.tmpdir")
		String dbpath = config.getString("dbpath", defaultPath + File.separator + "document-alignment")
		data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)
		model = new PSLModel(this, data)
	}

	/**
	 * Defines the name and the arguments of predicates that are used in the rules
	 */
	public void definePredicates()
	{

		model.add predicate: "hasDocument", types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasAttribute"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasInternalElement"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasRefSemantic"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasID"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasInternalLink"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassVersion"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassClassificationClass"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "hasEClassIRDI"     , types: [ArgumentType.UniqueID, ArgumentType.String]

		model.add predicate: "similar"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "similarType"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "eval", types: [ArgumentType.String, ArgumentType.String]

		model.add predicate: "hasRoleClass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasInterfaceClass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]

		model.add predicate: "hasSystemUnitClass"     , types: [ArgumentType.UniqueID, ArgumentType.UniqueID]
	}

	public void defineFunctions()
	{
		model.add function: "similarValue"  , implementation: new DiceSimilarity()
	}

	public void defineRules()
	{

		// Two AML hasAttributes are the same if its RefSemantic are the same
		model.add rule : (hasAttribute(A,X) & hasAttribute(B,Y) & hasRefSemantic(X,Z)
		& hasRefSemantic(Y,W) & similarValue(Z,W) & hasDocument(A,O1) & hasDocument(B,O2) &
		(O1-O2)) >> similar(A,B) , weight : 8

		// Two AMl hasAttributes are the same if they share the same ID
		model.add rule : (hasAttribute(A,X) & hasAttribute(B,Y) & hasID(A,Z) & hasID(B,W)
		& similarValue(Z,W) & hasDocument(A,O1) & hasDocument(B,O2) & (O1-O2)) >> similar(A,B) ,
		weight : 1

		// Two AMl hasInternalElement are the same if they share the same ID
		model.add rule : (hasInternalElement(A,X) & hasInternalElement(B,Y) & hasID(A,Z) & hasID(B,W)
		& similarValue(Z,W) &hasDocument(A,O1) & hasDocument(B,O2) & (O1-O2)) >> similar(A,B) ,
		weight : 4

		// Two Roles Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( hasRoleClass(A1,B1) & hasRoleClass(A2,B2) & hasAttribute(B1,X) & hasAttribute(B2,Y)
		& hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& hasRoleClass(A1,C1) & hasRoleClass(A2,D2) & hasAttribute(C1,Q) & hasAttribute(D2,T) &
		hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)& hasRoleClass(A1,E1) &
		hasRoleClass(A2,F2) &hasAttribute(E1,D) & hasAttribute(F2,K) & hasEClassVersion(E1,O) &
		hasEClassVersion(F2,L) & similarValue(O,L)& hasDocument(A1,O1) & hasDocument(A2,O2) &
		(O1-O2)) >> similar(A1,A2) , weight : 1

		// Two Interface Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( hasInterfaceClass(A1,B1) & hasInterfaceClass(A2,B2) & hasAttribute(B1,X)
		& hasAttribute(B2,Y)  & hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& hasInterfaceClass(A1,C1) & hasInterfaceClass(A2,D2) & hasAttribute(C1,Q) & hasAttribute(D2,T)
		& hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)
		& hasInterfaceClass(A1,E1) & hasInterfaceClass(A2,F2) &hasAttribute(E1,D) & hasAttribute(F2,K)
		& hasEClassVersion(E1,O) & hasEClassVersion(F2,L) & similarValue(O,L)
		& hasDocument(A1,O1) & hasDocument(A2,O2) & (O1-O2)) >> similar(A1,A2) , weight : 8

		// Two SystemUnit Class are same if its eclass,IRDI and classification class are the same
		model.add rule :( hasSystemUnitClass(A1,B1) & hasSystemUnitClass(A2,B2) & hasAttribute(B1,X)
		& hasAttribute(B2,Y)  & hasEClassIRDI(B1,Z) & hasEClassIRDI(B2,W) & similarValue(Z,W)
		& hasSystemUnitClass(A1,C1) & hasSystemUnitClass(A2,D2) & hasAttribute(C1,Q) & hasAttribute(D2,T)
		& hasEClassVersion(C1,M) & hasEClassVersion(D2,N) & similarValue(M,N)&
		hasSystemUnitClass(A1,E1) & hasSystemUnitClass(A2,F2) &hasAttribute(E1,D) & hasAttribute(F2,K) &
		hasEClassVersion(E1,O) & hasEClassVersion(F2,L) & similarValue(O,L)
		& hasDocument(A1,O1) & hasDocument(A2,O2) & (O1-O2)) >> similar(A1,A2) , weight : 8


		//Two hasInternalElement are the same if its InternalLink is the same
		//		model.add rule : (hasInternalElement(A,X) & hasInternalElement(B,Y)  & hasInternalLink(X,Z) & hasInternalLink(Y,W) &
		//		similarValue(Z,W) & hasDocument(A,O1) & hasDocument(B,O2) & (O1-O2)) >> similar(A,B) , weight : 12;

		// constraints
		model.add PredicateConstraint.PartialFunctional , on : similar
		model.add PredicateConstraint.PartialInverseFunctional , on : similar
		model.add PredicateConstraint.Symmetric , on : similar

		// prior
		model.add rule : ~similar(A,B), weight: 1
	}

	/**
	 *
	 */
	public void setUpData()
	{
		GroundTerm classID = data.getUniqueID("class")

		/* Loads data */
		dir = 'data' + java.io.File.separator + 'document' + java.io.File.separator

		/////////////////////////// train setup //////////////////////////////////

		testDir = util.ConfigManager.getFilePath() + "PSL/test/"
		trainDir = util.ConfigManager.getFilePath() + "PSL/train/"

		Partition trainObservations = new Partition(0)
		Partition trainPredictions = new Partition(1)
		Partition truth = new Partition(2)

		if(util.ConfigManager.getExecutionMethod() == "true"){

			createFiles(trainDir + "similar.txt")

			for (Predicate p : [hasDocument, hasAttribute, hasRefSemantic, 
				hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, 
				hasEClassIRDI, hasRoleClass, hasInternalElement, hasSystemUnitClass, 
				hasInterfaceClass]){
					
				createFiles(trainDir + p.getName().toLowerCase() + ".txt")
				def insert = data.getInserter(p, trainObservations)
				InserterUtils.loadDelimitedData(insert, trainDir + p.getName().toLowerCase()  +  ".txt")
			}

			def insert  =  data.getInserter(similar, truth)
			InserterUtils.loadDelimitedDataTruth(insert, trainDir + "similar.txt")

			trainDB = data.getDatabase(trainPredictions, [hasDocument, hasAttribute, hasRefSemantic, 
			hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, 
			hasRoleClass, hasInternalElement, hasSystemUnitClass, hasInterfaceClass]
			as Set, trainObservations)
			
			populateSimilar(trainDB)
			truthDB = data.getDatabase(truth, [similar] as Set)

			println "before training" + model

			println "LEARNING WEIGHTS..."
			MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(model, trainDB, truthDB, config)
			weightLearning.learn()
			weightLearning.close()
			println "LEARNING WEIGHTS DONE"
			println "after training" + model

		}

		/////////////////////////// test setup //////////////////////////////////

		Partition testObservations = new Partition(3)
		Partition testPredictions = new Partition(4)

		for (Predicate p : [hasDocument, hasAttribute, hasRefSemantic, hasID, hasInternalLink, 
		hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, hasRoleClass, 
		hasInternalElement, hasSystemUnitClass, hasInterfaceClass])
		{
			createFiles(testDir + p.getName().toLowerCase() + ".txt")			
		}
		createFiles(testDir + "similar.txt")
		createFiles(testDir + "GoldStandard.txt")
		createFiles(testDir + "similarwithConfidence.txt")

		for (Predicate p : [hasDocument, hasAttribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasEClassIRDI, hasRoleClass, hasInternalElement, hasSystemUnitClass, hasInterfaceClass])
		{
			def insert  =  data.getInserter(p, testObservations)

			InserterUtils.loadDelimitedData(insert, testDir  +  p.getName().toLowerCase()  +  ".txt")
		}

		testDB  =  data.getDatabase(testPredictions, [hasDocument, hasAttribute, hasRefSemantic, hasID, hasInternalLink, hasEClassVersion, hasEClassClassificationClass, hasRoleClass, hasEClassIRDI, hasInternalElement, hasSystemUnitClass, hasInterfaceClass
		] as Set, testObservations)

		populateSimilar(testDB)
	}

	public void createFiles(String testDir){

		def dataFile  =  new File(testDir)
		if(!dataFile.exists()){
			dataFile.createNewFile()
		}
	}

	public void runInference(){
		/////////////////////////// test inference //////////////////////////////////
		println "INFERRING..."

		MPEInference inference  =  new MPEInference(model, testDB, config)
		inference.mpeInference()
		inference.close()

		println "INFERENCE DONE"
		def matchResult  =  new File(testDir  +  'similar.txt')
		matchResult.write('')
		def resultConfidence  =  new File(testDir  +  'similarwithConfidence.txt')
		resultConfidence.write('')
		DecimalFormat formatter  =  new DecimalFormat("#.##")
		for (GroundAtom atom : Queries.getAllAtoms(testDB, similar)){
			println atom.toString()  +  ": "  +  formatter.format(atom.getValue())
			// only writes if its equal to 1 or u can set the threshold
			if(formatter.format(atom.getValue())>"0.3"){
				println 'matches threshold writing to similar.txt'
				// converting to format for evaluation
				String result  =  atom.toString().replaceAll("SIMILAR","")
				result  =  result.replaceAll("[()]","")
				String[] text = result.split(",")
				result = text[0] + "\t" + text[1]
				String result2 = text[0] + "\t" + text[1] + " " + atom.getValue()

				matchResult.append(result + '\n')
				resultConfidence.append(result2 + '\n')
			}
		}
	}


	/**
	 * Evaluates the results of inference versus expected truth values
	 */
	private void evalResults(Partition targetsPartition, Partition truthPartition) {
		targetsPartition  =  new Partition(5)
		truthPartition  =  new Partition(6)

		def insert  =  data.getInserter(eval, targetsPartition)
		InserterUtils.loadDelimitedData(insert, testDir  +  "GoldStandard.txt")

		insert  =  data.getInserter(eval, truthPartition)
		InserterUtils.loadDelimitedData(insert, testDir  +  "similar.txt")

		Database resultsDB  =  data.getDatabase(targetsPartition, [eval] as Set)
		Database truthDB  =  data.getDatabase(truthPartition, [eval] as Set)
		DiscretePredictionComparator dpc  =  new DiscretePredictionComparator(resultsDB)
		dpc.setBaseline(truthDB)
		DiscretePredictionStatistics stats  =  dpc.compare(eval)

		System.out.println("Accuracy:" + stats.getAccuracy())
		System.out.println("Error:" + stats.getError())
		System.out.println("Fmeasure:" + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("True negative:" + stats.tn)
		System.out.println("True Positive:" + stats.tp)
		System.out.println("False Positive:" + stats.tp)
		System.out.println("False Negative:" + stats.fp)

		System.out.println("Precision (Positive):" + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Recall: (Positive)" + stats.getRecall(DiscretePredictionStatistics.BinaryClass.POSITIVE))
		System.out.println("Precision:(Negative)" + stats.getPrecision(DiscretePredictionStatistics.BinaryClass.NEGATIVE))
		System.out.println("Recall:(Negative)" + stats.getRecall(DiscretePredictionStatistics.BinaryClass.NEGATIVE))

		// Saving Precision and Recall results to file
		def resultsFile
		
		if(util.ConfigManager.getExecutionMethod()  ==  "true"){
			resultsFile  =  new File(testDir + "Precision/" + "PrecisionRecallWithTraining.txt")
		}
		else{
			resultsFile  =  new File(testDir + "Precision/" + "PrecisionRecallWithoutTraining.txt")
		}

		resultsFile.createNewFile()
		resultsFile.write("")
		resultsFile.append("Accuracy:" + stats.getAccuracy() + '\n')
		resultsFile.append("Error:" + stats.getError() + '\n')
		resultsFile.append("Fmeasure:" + stats.getF1(DiscretePredictionStatistics.BinaryClass.POSITIVE)
				 +  '\n')
		resultsFile.append("True negative:" + stats.tn + '\n')
		resultsFile.append("True Positive:" + stats.tp + '\n')
		resultsFile.append("False Positive:" + stats.fp + '\n')
		resultsFile.append("False Negative:" + stats.fn + '\n')
		resultsFile.append("Precision (Positive):" + stats.getPrecision(DiscretePredictionStatistics.
				BinaryClass.POSITIVE) +  '\n')
		resultsFile.append("Recall: (Positive)" + stats.getRecall(DiscretePredictionStatistics.
				BinaryClass.POSITIVE) + '\n')
		resultsFile.append("Precision:(Negative)" + stats.getPrecision(DiscretePredictionStatistics.
				BinaryClass.NEGATIVE) + '\n')
		resultsFile.append("Recall:(Negative)" + stats.getRecall(DiscretePredictionStatistics.
				BinaryClass.NEGATIVE) + '\n')

		resultsDB.close()
		truthDB.close()
	}


	/**
	 * Populates all the similar atoms between the concepts of two Documents using
	 * the hasDocument predicate.
	 *
	 * @param db  The database to populate. It should contain the hasDocument atoms
	 */
	void populateSimilar(Database db) {
		/* Collects the ontology concepts */
		Set<GroundAtom> concepts  =  Queries.getAllAtoms(db, hasDocument)
		Set<GroundTerm> o1  =  new HashSet<GroundTerm>()
		Set<GroundTerm> o2  =  new HashSet<GroundTerm>()
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