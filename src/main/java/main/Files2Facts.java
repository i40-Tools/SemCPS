
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
	public RDFNode object;
	public RDFNode predicate;
	public RDFNode subject;

	protected ArrayList<File> files;

	private LinkedHashSet<String> subjectsToWrite;

	public Model model;

	int number = 0;
	private PrintWriter documentwriter;
	public Files2Facts(){
		files = new ArrayList<File>();
	}

	/**
	 * Converts the file to turtle format based on Krextor
	 * 
	 * @param input
	 * @param output
	 */
	public void convert2RDF() {
		int i = 0;
		Krextor krextor = new Krextor();

		for (File file : files) {			
			if (file.getName().endsWith(".aml")) {
				if (file.getName().equals("seed.aml")) {
					krextor.convertRdf(file.getAbsolutePath(), "aml", "turtle",
							ConfigManager.getFilePath() + "seed" + ".ttl");

				} else {
					krextor.convertRdf(file.getAbsolutePath(), "aml", "turtle",
							ConfigManager.getFilePath() + "plfile" + i + ".ttl");
				}
			} else {
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

		File originalFilesFolder = new File(path);
		if (originalFilesFolder.isDirectory()) {
			for (File amlFile : originalFilesFolder.listFiles()) {
				if (amlFile.isFile()
						&& (amlFile.getName().endsWith(type) || amlFile.getName().endsWith(type2)
								|| amlFile.getName().endsWith(type3))) {
					if (amlFile.getName().endsWith(".aml")) {
						String name = amlFile.getName().replace(".aml", "");
						if (name.endsWith("0") || name.endsWith("1") || name.equals("seed")) {
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
	 * Adds aml Values
	 * @param amlList
	 * @param amlValue
	 * @param aml
	 * @return
	 */
	ArrayList<String> addAmlValues(ArrayList<?> amlList,ArrayList<String> amlValue,String aml,
			String predicate){	
		for(int i = 0;i < amlList.size();i++){	
			StmtIterator iterator = model.listStatements();
			while (iterator.hasNext()) {
				Statement stmt = iterator.nextStatement();
				subject = stmt.getSubject();
				
				if(subject.asResource().getLocalName().equals(amlList.get(i))){
					String value = getValue(subject,predicate);					
					if(value != null){
						amlValue.add(aml + value);
						break;
					}
				}
			}
		}
		return amlValue;
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

				if(number == 3){

				}
				// all subjects are added according to ontology e.g aml
				else{
					addSubjectURI(subject, "", number, "hasDocument");
				}

				addsDataforAML(number); // process required data for AML
			}

			// case "opcua":

			writeData();
		}
		return "";
	}

	/**
	 * Reads RDF files with Standards data and create PSL files with this content
	 * The name of the files match with the list without repetition of RDF predicates 
	 * 
	 * @param collection
	 * @param documentwriter
	 * @throws FileNotFoundException
	 */
	private void writeData() throws FileNotFoundException {

		Set<String> predicates = generic.keySet(); // gets predicates to name the data files

		for (String i : predicates) {
			// name files as predicates
			documentwriter = new PrintWriter(ConfigManager.getFilePath() + "PSL/test/" + i + ".txt");

			Collection<String> values = generic.get(i);// for every predicate get its value
			for (String val : values) {
				// remove annotation to make it a literal value
				if (val.contains("aml:remove")) {
					val = val.replace("aml:remove", "");
				}
				if (val.contains("opcua:remove")) {
					val = val.replace("opcua:remove", "");
				}
				if (val.contains(":ConnectionPoint")) {
					val = val.replace(":ConnectionPoint", "");
				}


				documentwriter.println(val);
			}
			documentwriter.close();
		}
	}

	boolean checkEclass(RDFNode object){
		if (object.asLiteral().getLexicalForm().equals("eClassClassificationClass")
				|| object.asLiteral().getLexicalForm().equals("eClassVersion")
				|| object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
			return true;
		}
		return false;
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
	 * get predicate Value
	 * @param name
	 * @return
	 */
	String getValue(RDFNode name, String predicate) {
		String type = null;
		StmtIterator stmts = model.listStatements(name.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();

			if (stmte.getPredicate().asNode().getLocalName().toString().equals(predicate)) {
				type = stmte.getObject().asLiteral().getLexicalForm();
			}
		}
		return type;
	}



	public void addGenericObject(String firstPredicate,String secondPredicate ) throws FileNotFoundException{

		if (predicate.asNode().getLocalName().equals(firstPredicate)) {

			// adds for attribute
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					"has" + getType(subject));

			// adds for refsemantic.txt
			addGenericValue(predicate.asNode().getLocalName(),secondPredicate);
		}

	}

	/**
	 * Automation ML part for data population
	 * 
	 * @throws FileNotFoundException
	 */
	private void addsDataforAML(int number) throws FileNotFoundException {
		// RefSemantic part starts here

		if (predicate.asNode().getLocalName().equals("hasAttributeName")) {
			if (!checkEclass(object)) {
				if (!getType(subject).equals("Attribute")) {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							"has" + getType(subject) + predicate.asNode().getLocalName()
							.replace("has", ""));
				} else {
					addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
							      predicate.asNode().getLocalName());
				}
			}
		}

		// Adds domain and range
		if (predicate.asNode().getLocalName().equals("domain")|| 
				predicate.asNode().getLocalName().equals("range")) {
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 1, "has" + 
					predicate.asNode().getLocalName());
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), 2, "has" + 
					predicate.asNode().getLocalName());
			addSubjectURI(subject, "", 1, "hasDocument");
			addSubjectURI(subject, "", 2, "hasDocument");
			addSubjectURI(object, "", 1, "hasDocument");
			addSubjectURI(object, "", 2, "hasDocument");			
		}


		if (predicate.asNode().getLocalName().equals("type")) {
			if (number != 3)
				addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
						      "has" + predicate.asNode().getLocalName());
		}

		if (predicate.asNode().getLocalName().equals("hasAttributeValue")) {
			if (!checkEclass(object)) {
				addSubjectURI(subject, ":remove" +object.asLiteral().getLexicalForm(), number,
						      predicate.asNode().getLocalName());
			}
		}

		addGenericObject("hasExternalReference","refBaseClassPath" );
		addGenericObject("hasInternalLink","hasRefPartnerSideB" );
		addGenericObject("hasInternalLink","hasRefPartnerSideA" );

		if (predicate.asNode().getLocalName().equals("hasAttribute")) {
			// gets all classes which hasAttribute relation
			addSubjectURI(subject, ":" + object.asNode().getLocalName(), number,
					      "has" + getType(subject));
		}

		addGenericObject("hasRefSemantic","hasCorrespondingAttributePath");

		/***
		 * Eclass part starts here. TODO refactoring and functions only for test
		 * purpose now
		 */
		if (object.isLiteral()) {
			if (checkEclass(object)) {
				StmtIterator stmts = model.listStatements(subject.asResource(), null,(RDFNode) null);
				while (stmts.hasNext()) {
					Statement stmte = stmts.nextStatement();

					if (stmte.getPredicate().asNode().getLocalName().equals("hasAttributeValue")) {

						if (object.asLiteral().getLexicalForm()
								  .equals("eClassClassificationClass")) {
							addSubjectURI(subject,":remove" + 
								          stmte.getObject().asLiteral().getLexicalForm(),
									      number, "hasEClassClassificationClass");
						}

						if (object.asLiteral().getLexicalForm().equals("eClassVersion")) {
							addSubjectURI(subject, ":remove" + 
						                  stmte.getObject().asLiteral().getLexicalForm(),
									      number, "hasEclassVersion");
						}
						
						if (object.asLiteral().getLexicalForm().equals("eClassIRDI")) {
							addSubjectURI(subject, ":remove" + 
						                  stmte.getObject().asLiteral().getLexicalForm(),
									      number, "hasEclassIRDI");
						}

					}
				}
			}
		}

		// RefSemantic part starts here
		if (predicate.asNode().getLocalName().equals("identifier")) {
			// gets the literal ID value and add it to hasID
			addSubjectURI(subject, ":remove" + object.asLiteral().getLexicalForm(), number,
					      "has" + getType(subject) + "ID");
		}
	}

	/**
	 * Add refsemantic in the list
	 * 
	 * @throws FileNotFoundException
	 */
	private void addGenericValue(String type,String predicate) throws FileNotFoundException {
		StmtIterator stmts = model.listStatements(object.asResource(), null, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmte = stmts.nextStatement();
			// remove because its a literal so we can identify. we dont need aml: opcua: tags
			if(stmte.getPredicate().getLocalName().equals(predicate)){
				if (stmte.getObject().isLiteral()) {
					addSubjectURI(object, ":remove" + stmte.getObject().asLiteral().getLexicalForm(),
								  number, type);
				}
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
			if(!file.getName().equals("seed.ttl")){
				createPSLPredicate(file, i++, ConfigManager.getStandard());
			}
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