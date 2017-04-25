
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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

import industryStandard.IndustryStandards;
import uni.bonn.krextor.Krextor;
import util.ConfigManager;
import util.StringUtil;

/**
 * Reads the RDF files and convert them to Datalog or to PSL facts
 * 
 * @author Irlan 28.06.2016
 */
public class Files2Facts extends IndustryStandards {
	private RDFNode object;
	private RDFNode predicate;
	private RDFNode subject;

	private ArrayList<File> files;

	private LinkedHashSet<String> subjectsToWrite;

	private Model model;

	int number = 0;
	private PrintWriter documentwriter;

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
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public ArrayList<File> readFiles(String path, String type, String type2, String type3)
			throws Exception {
		files = new ArrayList<File>();
		File originalFilesFolder = new File(path);
		if (originalFilesFolder.isDirectory()) {
			for (File amlFile : originalFilesFolder.listFiles()) {
				if (amlFile.isFile()
						&& (amlFile.getName().endsWith(type) || amlFile.getName().endsWith(type2)
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
	 * 
	 * @throws IOException
	 */
	public void improveRDFOutputFormat() throws IOException {
		for (File file : files) {
			if (file.getName().endsWith(".ttl")) {
				org.apache.jena.rdf.model.Model model = RDFDataMgr
						.loadModel(file.getAbsolutePath());
				File replaceFile = new File(file.getParent() + "/" + file.getName());
				FileOutputStream out = new FileOutputStream(replaceFile, false);
				RDFDataMgr.write(out, model, RDFFormat.TURTLE_BLOCKS);
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

			buf.append("clause1(")
					.append(StringUtil.lowerCaseFirstChar(predicate.asNode().getLocalName()))
					.append("(")
					.append(StringUtil.lowerCaseFirstChar(subject.asNode().getLocalName()) + number)
					.append(",");
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
	 * Reads the RDF files and extract the contents for creating PSL predicates.
	 * 
	 * @param file
	 * @param number
	 * @param standard
	 * @return
	 * @throws Exception
	 */
	public String createPSLPredicate(File file, int number, String standard) throws Exception {
		
		this.number = number;
		InputStream inputStream = FileManager.get().open(file.getAbsolutePath());
		model = ModelFactory.createDefaultModel();
		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		StmtIterator iterator = model.listStatements();
		subjectsToWrite = new LinkedHashSet<String>();
		
		switch (standard) {

		case "aml":

			while (iterator.hasNext()) {
				
				Statement stmt = iterator.nextStatement();
				subject = stmt.getSubject();
				predicate = stmt.getPredicate();
				object = stmt.getObject();

				if (predicate.asNode().getLocalName().toString().equals("type")) {
					subjectsToWrite.add(object.asNode().getLocalName());
				}
				// all subjects are added according to ontology e.g aml
				addSubjectURI(subject, "", number, "hasDocument");
				addsDataforAML(number); // process required data for AML
			}

			// case "opcua":

			writeData();
		}
		return "";
	}

	/**
	 * Writes data to files
	 * 
	 * @param collection
	 * @param documentwriter
	 * @throws FileNotFoundException
	 */
	private void writeData() throws FileNotFoundException {

		Set<String> keys = generic.keySet();// gets all predicates

		for (String i : keys) {
			// same name of files as predicates
			documentwriter = new PrintWriter(
					ConfigManager.getFilePath() + "PSL/test/" + i + ".txt");

			Collection<String> values = generic.get(i);// for every predicate
														// get its value
			for (String val : values) {
				// remove annotation to make it a literal value
				if (val.contains("aml:remove")) {
					val = val.replace("aml:remove", "");
				}
				if (val.contains("opcua:remove")) {
					val = val.replace("opcua:remove", "");
				}
				documentwriter.println(val);
			}
			documentwriter.close();

		}
	}

	/**
	 * Gets the class of object.
	 * 
	 * @param name
	 * @return
	 */
	String getType(RDFNode name) {
		String type = null;
		StmtIterator stmts = model.listStatements(name.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			if (stmte.getPredicate().asNode().getLocalName().toString().equals("type")) {
				type = stmte.getObject().asNode().getLocalName();
			}
		}
		return type;
	}

	/**
	 * Automation ML part for data population
	 * 
	 * @throws FileNotFoundException
	 */
	private void addsDataforAML(int number) throws FileNotFoundException {
		// RefSemantic part starts here

		if (predicate.asNode().getLocalName().equals("hasAttribute")) {

			// gets all classes which hasAttribute relation
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));
		}

		if (predicate.asNode().getLocalName().equals("hasRefSemantic")) {

			// adds for attribute
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));

			// adds for refsemantic.txt
			addRefSemantic();
		}

		/***
		 * Eclass part starts here. TODO refactoring and functions only for test
		 * purpose now
		 */
		if (object.isLiteral()) {
			if (object.asLiteral().getLexicalForm().equals("eClassClassificationClass")
					|| object.asLiteral().getLexicalForm().equals("eClassVersion")
					|| object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
				if (predicate.asNode().getLocalName().equals("hasAttributeName")) {
					// adds for attribute.txt
					addSubjectURI(subject, ":" + object.asLiteral().getLexicalForm(), number,
							"hasAttribute");
				}

				StmtIterator stmts = model.listStatements(subject.asResource(), null,
						(RDFNode) null);
				while (stmts.hasNext()) {
					Statement stmte = stmts.nextStatement();

					if (stmte.getPredicate().asNode().getLocalName().equals("hasAttributeValue")) {

						if (object.asLiteral().getLexicalForm()
								.equals("eClassClassificationClass")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEClassClassificationClass");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassVersion")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassVersion");
						}
						if (object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
							addSubjectURI(subject,
									":remove" + stmte.getObject().asLiteral().getLexicalForm(),
									number, "hasEclassIRDI");
						}

					}
				}

			}

		}

		// RefSemantic part starts here
		if (predicate.asNode().getLocalName().equals("identifier")) {

			// id is for internal Element goes in InternalElement file
			if (subject.asNode().getLocalName().contains("InternalElement")) {
				addSubjectURI(subject, ":" + predicate.asNode().getLocalName(), number,
						"hasInternalElement");
			} else {
				// id is for attribute goes in hasAttribute
				addSubjectURI(subject, ":" + predicate.asNode().getLocalName(), number,
						"hasAttribute");
			}
			// gets the literal ID value and add it to hasID
			addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
					"hasID");
		}
	}

	/**
	 * Add refsemantic in the list
	 * 
	 * @throws FileNotFoundException
	 */
	private void addRefSemantic() throws FileNotFoundException {
		StmtIterator stmts = model.listStatements(object.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {

			Statement stmte = stmts.nextStatement();
			// remove because its a literal so we can identify. we dont need
			// aml:
			// opcua: tags
			if (stmte.getObject().isLiteral()) {
				addSubjectURI(object, ":remove" + stmte.getObject().asLiteral().getLexicalForm(),
						number, "hasRefSemantic");
			}
		}
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
		for (File file : files) {
			// pass in the writers
			createPSLPredicate(file, i++, ConfigManager.getStandard());
		}

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

		prologWriter = new PrintWriter(
				new File(System.getProperty("user.dir") + "/resources/files/output.txt"));
		prologWriter.println("'" + ConfigManager.getFilePath() + "output.txt" + "'.");
		prologWriter.close();

	}

}