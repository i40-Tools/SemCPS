
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

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
 * Reads the RDF files and convert them to Datalog facts
 * 
 * @author Irlan 28.06.2016
 */
public class Files2Facts {
	private RDFNode object;
	private RDFNode predicate;
	private RDFNode subject;
	private ArrayList<File> files;

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

		// parses in turtle format
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