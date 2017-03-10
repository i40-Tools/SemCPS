package datalogApproach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import org.jpl7.Query;
import org.jpl7.Term;
import org.w3c.dom.Document;

import integration.XmlParser;
import util.ConfigManager;

/**
 * 
 * @author Irlan
 *
 */
public class DeductiveDB {

	private String extractedAttr;
	private String originalText;
	public static ArrayList<String> baseClass;
	public static ArrayList<String> attrName;

	/**
	 * Setting working directory
	 */
	public void readWorkingDirectory() {
		String path = System.getProperty("user.dir");
		File myUri = new File(path);
		path = myUri.toURI().toString().replace("file:/", "");
		Query.hasSolution("working_directory(_," + "'" + path + "')");
	}

	/**
	 * Executing the AML Datalog rules over Prolog. Executing the
	 * writePredicates method which generates the output.txt containing the
	 * conflicting elements.
	 */
	public void executeKB() {
		// Queries evalAMl.pl
		String evalAML = "consult('resources/files/evalAML.pl')";
		System.out.println(evalAML + " " + (Query.hasSolution(evalAML) ? "succeeded" : "failed"));

		// Queries eval
		System.out.println("eval" + " " + (Query.hasSolution("eval") ? "succeeded" : "failed"));

		// Queries writePredicates.
		String writeFiles = "writePredicates";
		Query.hasSolution(writeFiles);
	}

	/**
	 * Querying the knowledge base.
	 * 
	 * @throws Throwable
	 */
	public void consultKB() throws Throwable {
		String attributes[] = null;
		if (!extractedAttr.equals("")) {
			attributes = extractedAttr.split(",");
		}
		attrName = new ArrayList<String>();

		// loops through all atributes
		int j = 0;

		if (attributes != null) {
			while (j < attributes.length) {

				// performs query to get the attribute name
				Map<String, Term>[] results = Query.allSolutions("hasAttributeName" + "(" + attributes[j] + ",Y)");
				for (int i = 0; i < results.length; i++) {
					// stores in array
					attrName.add(results[i].get("Y").toString());

					// updates output.txt
					originalText = originalText.replaceAll(attributes[j], results[i].get("Y").toString());

				}
				j++;
			}
		} else {
			System.out.println(
					"None of the prolog rules returned true for current data set.File integration completed without any specific rules ");
		}
		// writes the attributes names in the output.txt
		PrintWriter prologWriter = new PrintWriter(new File(ConfigManager.getFilePath() + "/output.txt"));
		prologWriter.println(originalText);
		Document doc;
		File file = new File(ConfigManager.getFilePath() + "seed.aml");
		File file2 = new File(ConfigManager.getFilePath() + "seed.opcua");

		if (file.exists()) {

			doc = XmlParser.initInput(ConfigManager.getFilePath() + "seed.aml");
			prologWriter.println("Number of Elements =" + XmlParser.getAllNodes(doc).size());
			prologWriter.close();

		} else if (file2.exists()) {
			doc = XmlParser.initInput(ConfigManager.getFilePath() + "seed.opcua");
			prologWriter.println("Number of Elements =" + XmlParser.getAllNodes(doc).size());
			prologWriter.close();

		}

		if (attributes != null) {
			addBaseClass(attributes);
		}
	}

	/**
	 * Reads the output.txt for mapping the attributes to names or values so
	 * that integration can be performed. Mapping is important to identify the
	 * attributes in AML files. This extracts the attributes from Datalog format
	 * to java string objects so that query can be made on them.
	 * 
	 * @param extractedAttr
	 * @param originalText
	 * @throws IOException
	 * @throws Exception
	 */
	public void readOutput() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(ConfigManager.getFilePath() + "/output.txt"));
		StringBuilder sb = new StringBuilder();
		StringBuilder orignal = new StringBuilder();
		String line = br.readLine();

		while (line != null) {
			orignal.append(line);
			orignal.append(System.lineSeparator());

			int a = line.indexOf('(');
			int b = line.indexOf(')');
			if (a + 1 >= 0 && b >= 0) {
				line = line.substring(a + 1, b);
			}
			sb.append(line + ",");
			line = br.readLine();
		}
		extractedAttr = sb.toString();
		originalText = orignal.toString();
		br.close();
	}

	/**
	 * (Work in progress) This function adds a new output.txt for integration
	 * which mentions the attribute names and the classes it belongs to. This is
	 * required for identification of attributes if there are multiple
	 * attributes with same name. This helps in integration process.
	 * 
	 * @param attributes
	 * @throws FileNotFoundException
	 */

	void addBaseClass(String attributes[]) throws FileNotFoundException {
		int j = 0;
		baseClass = new ArrayList<String>();

		while (j < attributes.length) {

			if (Query.hasSolution("hasAttribute(Y," + attributes[j] + ")")) {
				Map<String, Term>[] results2 = Query.allSolutions("hasAttribute(Y," + attributes[j] + ")");

				for (int i = 0; i < results2.length; i++) {
					Map<String, Term>[] results3 = Query
							.allSolutions("hasAttributeName(" + results2[i].get("Y").toString() + ",Y)");
					for (int k = 0; k < results3.length; k++) {
						baseClass.add(results3[k].get("Y").toString());
					}
				}
			} else {
				baseClass.add(attributes[j]);
			}

			j++;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < baseClass.size(); i++) {
			if (attrName.size() > i) {
				if (!sb.toString().contains(baseClass.get(i) + "," + attrName.get(i))) {
					sb.append(baseClass.get(i) + "," + attrName.get(i));
					sb.append(System.lineSeparator());
				}
			}
		}
		PrintWriter prologWriter = new PrintWriter(new File(ConfigManager.getFilePath() + "/output2.txt"));
		prologWriter.println(sb.toString());
		prologWriter.close();

	}

}
