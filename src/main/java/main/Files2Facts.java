
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import uni.bonn.krextor.Krextor;
import util.ConfigManager;
import util.StringUtil;

/**
 * Reads the RDF files and convert them to Datalog facts or to PSL facts
 * 
 * @author Irlan 28.06.2016
 */
public class Files2Facts {
	private RDFNode object;
	private RDFNode predicate;
	private RDFNode subject;

	private ArrayList<File> files;
	private Resource refSemanticSubject;
	private Resource refSemanticValue;
	private Resource IDSubject;
	private String IDValue;
	private Set<String> forInternalElements;
	private PrintWriter fromDocumentwriter;
	private PrintWriter attributeWriter;
	private PrintWriter hasRefSemanticwriter;
	private PrintWriter hasIDwriter;
	private PrintWriter internalElementwriter;
	private Model model;
	private LinkedHashSet<String> forAttribute;
	private LinkedHashSet<String> forID;
	private LinkedHashSet<String> forRefSemantic;
	private LinkedHashSet<String> forDocument;

	/**
	 * Converts the file to turtle format based on Krextor
	 * 
	 * @param input
	 * @param output
	 */
	public void convert2RDF() {
		int i = 0;
		for (File file : files) {
			if (file.getName().endsWith(".aml")) {
				Krextor krextor = new Krextor();
				krextor.convertRdf(file.getAbsolutePath(), "aml", "turtle",
						ConfigManager.getFilePath() + "plfile" + i + ".ttl");
			} else {
				Krextor krextor = new Krextor();
				krextor.convertRdf(file.getAbsolutePath(), "opcua", "turtle",
						ConfigManager.getFilePath() + "plfile" + i + ".ttl");
			}
			i++;
		}
	}

	/**
	 * Read the RDF files of a given path
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public ArrayList<File> readFiles(String path, String type, String type2, String type3) throws Exception {
		files = new ArrayList<File>();
		File originalFilesFolder = new File(path);
		if (originalFilesFolder.isDirectory()) {
			for (File amlFile : originalFilesFolder.listFiles()) {
				if (amlFile.isFile() && (amlFile.getName().endsWith(type) || amlFile.getName().endsWith(type2)
						|| amlFile.getName().endsWith(type3))) {
					if (amlFile.getName().endsWith(".aml")) {
						String name = amlFile.getName().replace(".aml", "");
						if (name.endsWith("0") || name.endsWith("1")) {
							files.add(amlFile);
						}
					}

					else if (amlFile.getName().endsWith(".opcua")) {
						String name = amlFile.getName().replace(".opcua", "");
						if (name.endsWith("0") || name.endsWith("1")) {
							files.add(amlFile);
						}
					}

					else if (amlFile.getName().endsWith(".xml")) {
						String name = amlFile.getName().replace(".xml", "");
						files.add(amlFile);
					}

					else {
						files.add(amlFile);
					}
				}
			}
		} else {
			System.out.println("Error in the directory that you provided");
			System.exit(0);
		}
		return files;
	}

	/**
	 * Adds a better turtle format for the obtained RDF files
	 * @throws IOException
	 */
	public void improveRDFOutputFormat() throws IOException{
		for (File file : files) {
			if (file.getName().endsWith(".ttl")) {
				org.apache.jena.rdf.model.Model model = RDFDataMgr.loadModel(file.getAbsolutePath()) ;
				File replaceFile = new File(file.getParent() + "/" + file.getName() );
				FileOutputStream out = new FileOutputStream(replaceFile, false);
				RDFDataMgr.write(out, model, RDFFormat.TURTLE_BLOCKS) ;
				out.close();
			}
		}
	}


	/**
	 * Reads the turtle format RDF files and extract the contents for data log
	 * conversion.
	 * 
	 * @param file
	 * @param number
	 * @return
	 * @throws Exception
	 */
	public String factsFromFiles(File file, int number) throws Exception {

		StringBuilder buf = new StringBuilder();
		InputStream inputStream = FileManager.get().open(file.getAbsolutePath());

		Model model = null;
		model = ModelFactory.createDefaultModel();

		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		StmtIterator iterator = model.listStatements();

		while (iterator.hasNext()) {
			Statement stmt = iterator.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();

			buf.append("clause1(").append(StringUtil.lowerCaseFirstChar(predicate.asNode().getLocalName())).append("(")
			.append(StringUtil.lowerCaseFirstChar(subject.asNode().getLocalName()) + number).append(",");
			if (object.isURIResource()) {
				object = model.getResource(object.as(Resource.class).getURI());
				String objectStr = object.asNode().getLocalName();
				if (predicate.asNode().getLocalName().toString().equals("type")) {
					buf.append(StringUtil.lowerCaseFirstChar(objectStr));
				} else {
					buf.append(StringUtil.lowerCaseFirstChar(objectStr) + number);
				}

			} else {
				if (object.isLiteral()) {
					buf.append("'" + object.asLiteral().getLexicalForm() + "'");

				} else {
					buf.append(object);

				}
			}

			buf.append("),true).");
			buf.append(System.getProperty("line.separator"));

		}

		return buf.toString();
	}

	/**
	 * add subjects with appropriate ontology
	 * @param subject
	 * @param subjects
	 * @TODO don't understand the comment
	 */
	void addSubjectURI(RDFNode subject, Set<String> subjects, String predicate) {
		if (subject.asNode().getNameSpace().contains("aml")) {
			subjects.add("aml:" + subject.asNode().getLocalName() + "\t" + "aml" + predicate);
		}

		else if (subject.asNode().getNameSpace().contains("opcua")) {
			subjects.add("opcua:" + subject.asNode().getLocalName() + "\t" + "opcua" + predicate);
		}
	}

	/**
	 * Searches parent subject and adds it to list based on parentnodeID for OPC UA.
	 * @param allSubjects
	 * @param model
	 * @param parentNode
	 * @param forID
	 */
	void addParentSubject(ArrayList<Resource> allSubjects, Model model, String parentNode, Set<String> forID,
			String value, boolean flag) {

		for (int i = 0; i < allSubjects.size(); i++) {
			StmtIterator stmts = model.listStatements(allSubjects.get(i), null, (RDFNode) null);
			while (stmts.hasNext()) {

				Statement stmte = stmts.nextStatement();

				if (stmte.getPredicate().asNode().getLocalName().equals("hasNodeId")) {
					if (stmte.getObject().asLiteral().getLexicalForm().equals(parentNode)) {
						if (stmte.getSubject().asNode().getLocalName().contains("UAObject") && flag == true) {
							addSubjectURI(allSubjects.get(i), forInternalElements, value);

						} else {
							addSubjectURI(allSubjects.get(i), forID, value);
						}

					}
				}

			}
		}

	}

	/**
	 * Reads the RDF files and extract the contents for creating PSL predicates.
	 * @param file
	 * @param number
	 * @return
	 * @throws Exception
	 */
	public String createPSLPredicate(File file, int number, PrintWriter fromDocumentwriter, PrintWriter attributewriter,
			PrintWriter hasRefSemanticwriter, PrintWriter hasIDwriter, PrintWriter internalElementwriter)
					throws Exception {

		InputStream inputStream = FileManager.get().open(file.getAbsolutePath());
		model = ModelFactory.createDefaultModel();
		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		StmtIterator iterator = model.listStatements();
		StmtIterator subjectIterator = model.listStatements();

		// init data structures
		initDataStructs();

		// gets all subjects for opcua
		ArrayList<Resource> allSubjects = getAllSubjects(subjectIterator);

		while (iterator.hasNext()) {

			Statement stmt = iterator.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();
			System.out.println(subject.asNode().getLocalName() + " subject");

			// all subjects are added accordign to ontology e.g aml: opcua:
			// forDocument.txt
			addSubjectURI(subject, forDocument, "");

			System.out.println(predicate.asNode().getLocalName() + " pred");

			addsDataforAML(); // process required data for AML
			addsDataforOPCUA(allSubjects); // process required data for opcua
		}

		/**
		 * Write data to files
		 */
		writeData(forDocument, fromDocumentwriter);
		writeData(forAttribute, attributewriter);
		writeData(forInternalElements, internalElementwriter);
		writeData(forRefSemantic, hasRefSemanticwriter);
		writeData(forID, hasIDwriter);

		return "";
	}

	/**
	 * Writes data to files
	 * @param docName
	 * @param documentwriter
	 */
	private void writeData(Set<String> docName, PrintWriter documentwriter) {
		for (String i : docName) {
			// remove annotation to make it a literal value
			if (i.contains("aml:remove")) {
				i = i.replace("aml:remove", "");
			}
			if (i.contains("opcua:remove")) {
				i = i.replace("opcua:remove", "");
			}
			documentwriter.println(i);
		}
	}

	/**
	 * OPCUA data is complex and requires matching with parent node
	 * @param allSubjects
	 */
	private void addsDataforOPCUA(ArrayList<Resource> allSubjects) {
		if (object.isLiteral()) {
			System.out.println(object.asLiteral().getLexicalForm() + "value");

			// RefSemantic part starts here for opcua
			if (object.asLiteral().getLexicalForm().equals("RefSemantic")) {
				// get subject

				String parentNode = getParentNode("RefSemantic");// gets
				// ParentNode

				// adds opcua refsemantic value here
				StmtIterator stmts = model.listStatements(refSemanticValue.asResource(), 
									 null, (RDFNode) null);
				Statement stmte = stmts.nextStatement();

				if(stmte.getObject().isLiteral()){
					// adds to list to write on file
					addSubjectURI(refSemanticSubject, forRefSemantic,
							":remove" + stmte.getObject().asLiteral().getLexicalForm());
				}

				// loop all subjects and match parentnode it to identify
				// which attribute is it and so that we could orignal
				// attribute whose refsemantic it is

				addParentSubject(allSubjects, model, parentNode, forAttribute, 
						         ":" + subject.asNode().getLocalName(),true);
			}

			// gets value for id
			if (object.asLiteral().getLexicalForm().equals("ID")) {
				addOPCUAid(allSubjects);
			}

		} else {
			System.out.println(object + " object");
		}

	}

	/**
	 * Adds OPC UA ids
	 * @param allSubjects
	 */
	private void addOPCUAid(ArrayList<Resource> allSubjects) {
		String parentNode = getParentNode("ID");

		// adds opcua ID value here using its object
		StmtIterator stmts = model.listStatements(IDSubject.asResource(), null, 
				             (RDFNode) null);
		Statement stmte = stmts.nextStatement();
		if(stmte.getObject().isLiteral()){
			// gets Value
			IDValue = stmte.getObject().asLiteral().getLexicalForm();
			IDValue = IDValue.replace("{", "");
			IDValue = IDValue.replace("}", "");
		}

		// loop all subjects and match parentnode it to identify
		// which attribute is it

		addParentSubject(allSubjects, model, parentNode, forAttribute, ":" + IDSubject.asNode().getLocalName(), true);

		addSubjectURI(IDSubject, forID, ":remove" + IDValue);

	}

	/**
	 * Just returns parents node for opcua
	 * 
	 * @param type
	 * @return
	 */
	private String getParentNode(String type) {
		// TODO Auto-generated method stub

		String parentNode = null;
		StmtIterator stmts = model.listStatements(subject.asResource(), null, (RDFNode) null);

		// goes through its statements and gets parent node id
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			if (stmte.getPredicate().asNode().getLocalName().equals("parentNodeId")) {
				parentNode = stmte.getObject().asLiteral().getLexicalForm();
			}

			// get opcua refsemantic subject and its value object
			if (stmte.getPredicate().asNode().getLocalName().equals("hasValue")) {
				if (type == "ID") {
					IDSubject = stmte.getObject().asResource();
				} else if (type == "RefSemantic") {

					refSemanticValue = stmte.getObject().asResource();
					refSemanticSubject = stmte.getSubject();

				}
			}

		}

		return parentNode;
	}

	/**
	 * Automation ML part for data population
	 */

	private void addsDataforAML() {
		// TODO Auto-generated method stub

		if (predicate.asNode().getLocalName().equals("hasRefSemantic")) {
			// adds for attribute.txt
			addSubjectURI(subject, forAttribute, ":" + object.asNode().getLocalName());
			// adds for refsemantic.txt
			addRefSemantic();

		}

		// adds id for aml
		if (predicate.asNode().getLocalName().equals("identifier")) {

			// id is for internal Element goes in InternalElement file
			if (subject.asNode().getLocalName().contains("InternalElement")) {
				addSubjectURI(subject, forInternalElements, ":" + predicate.asNode().getLocalName());
			} else {
				// id is for attribute goes in forAttribute
				addSubjectURI(subject, forAttribute, ":" + predicate.asNode().getLocalName());
			}
			// gets the literal ID value and add it to forID
			addSubjectURI(subject, forID, ":remove" + object.asLiteral().getLexicalForm());
		}
	}

	/**
	 * Add refsemantic in the list
	 */
	private void addRefSemantic() {
		StmtIterator stmts = model.listStatements(object.asResource(), null, (RDFNode) null);
		Statement stmte = stmts.nextStatement();
		// remove because its a literal so we can identify. we dont need aml:
		// opcua: tags
		if(object.isLiteral()){
			addSubjectURI(object, forRefSemantic, ":remove" + stmte.getObject().asLiteral().getLexicalForm());
		}
	}

	/**
	 * Get all subjects for opcua
	 * @param subjectIterator
	 * @return
	 */
	private ArrayList<Resource> getAllSubjects(StmtIterator subjectIterator) {
		ArrayList<Resource> allSubjects = new ArrayList<Resource>();
		while (subjectIterator.hasNext()) {
			Statement stmt = subjectIterator.nextStatement();
			subject = stmt.getSubject();
			if (subject.asNode().getNameSpace().contains("aml")) {
				break;
			}
			allSubjects.add(subject.asResource());
		}
		return allSubjects;
	}

	/**
	 * Initialises data structures
	 */
	private void initDataStructs() {
		forDocument = new LinkedHashSet<>();
		forAttribute = new LinkedHashSet<>();
		forID = new LinkedHashSet<>();
		forRefSemantic = new LinkedHashSet<>();
		forInternalElements = new LinkedHashSet<>();
	}

	/**
	 * Generate all the files of a given folder
	 * 
	 * @throws Exception
	 */
	public void generateExtensionalDB(String path) throws Exception {
		int i = 1;
		StringBuilder buf = new StringBuilder();
		for (File file : files) {
			buf.append(factsFromFiles(file, i++));
		}
		PrintWriter prologWriter = new PrintWriter(new File(path + "edb.pl"));
		prologWriter.println(buf);
		prologWriter.close();
	}

	/**
	 * Generate PSL predicates
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void generatePSLPredicates(String path) throws Exception {
		int i = 1;
		initFileWriters();
		for (File file : files) {
			// pass in the writers
			createPSLPredicate(file, i++, fromDocumentwriter, attributeWriter, hasRefSemanticwriter, hasIDwriter,
					internalElementwriter);
		}

		closeFileWriters();

	}

	/**
	 * Closes writers
	 */

	private void closeFileWriters() {
		// TODO Auto-generated method stub
		fromDocumentwriter.close();
		attributeWriter.close();
		hasRefSemanticwriter.close();
		hasIDwriter.close();
		internalElementwriter.close();

	}

	/**
	 * Initialises writers
	 * 
	 * @throws FileNotFoundException
	 */

	private void initFileWriters() throws FileNotFoundException {
		fromDocumentwriter = new PrintWriter("data/ontology/test/fromDocument.txt");
		attributeWriter = new PrintWriter("data/ontology/test/Attribute.txt");
		hasRefSemanticwriter = new PrintWriter("data/ontology/test/hasRefsemantic.txt");
		hasIDwriter = new PrintWriter("data/ontology/test/hasID.txt");
		internalElementwriter = new PrintWriter("data/ontology/test/InternalElements.txt");
	}

	/**
	 * Creates temporary files which holds the path for edb.pl and output.txt
	 * These files are necessary for evalAML.pl so that the path is
	 * automatically set from config.ttl
	 * 
	 * @throws FileNotFoundException
	 */
	public void prologFilePath() throws FileNotFoundException {
		PrintWriter prologWriter = new PrintWriter(
				new File(System.getProperty("user.dir") + "/resources/files/edb.txt"));
		prologWriter.println("'" + ConfigManager.getFilePath() + "edb.pl" + "'.");
		prologWriter.close();

		prologWriter = new PrintWriter(new File(System.getProperty("user.dir") + "/resources/files/output.txt"));
		prologWriter.println("'" + ConfigManager.getFilePath() + "output.txt" + "'.");
		prologWriter.close();

	}

}