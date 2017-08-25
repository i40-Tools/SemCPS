package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.ModelFactory;

import util.ConfigManager;

/**
 * Converts computed results from object to values by getting there rdf reference.
 * @author Omar Rana
 *
 */

public class Similar extends Files2Facts {

	public Similar() {

	}


	/**
	 * Adds a better turtle format for the obtained RDF files
	 * 
	 * @throws IOException
	 */
	public void convertSimilar() throws IOException {
		ArrayList<String> aml1List = new ArrayList<String>();
		ArrayList<String> aml2List = new ArrayList<String>();
		ArrayList<String> aml1negList = new ArrayList<String>();
		ArrayList<String> aml2negList = new ArrayList<String>();
		ArrayList<String> aml1Values = new ArrayList<String>();
		ArrayList<String> aml2Values = new ArrayList<String>();
		ArrayList<String> aml1negValues = new ArrayList<String>();
		ArrayList<String> aml2negValues = new ArrayList<String>();
		ArrayList<String> duplicateCheck = new ArrayList<String>();

		try (BufferedReader br = new BufferedReader(
				new FileReader(new File(ConfigManager.getFilePath() + "PSL/test/similar.txt")))) {
			String line;
			while ((line = br.readLine()) != null) {
					String values[] = line.split(",");
					if (line.contains("truth:1")) {
						aml1List.add(values[0].replaceAll("aml1:", ""));
						aml2List.add(values[1].replaceAll("aml2:", ""));

					} else {
						aml1negList.add(values[0].replaceAll("aml1:", ""));
						aml2negList.add(values[1].replaceAll("aml2:", ""));
					}
			}
		}

		PrintWriter similar = new PrintWriter(ConfigManager.getFilePath() + "PSL/test/similar.txt");

		for (File file : files) {
			InputStream inputStream = com.hp.hpl.jena.util.FileManager.get().open(file.getAbsolutePath());
			model = ModelFactory.createDefaultModel();

			model.read(new InputStreamReader(inputStream), null, "TURTLE");

			if (file.getName().equals("plfile0.ttl")) {

				addAmlValues(aml1List, aml1Values, "aml1:", "hasAttributeName");
				addAmlValues(aml1List, aml1Values, "aml1:", "refBaseClassPath");
				addAmlValues(aml1List, aml1Values, "aml1:", "identifier");
				addAmlValues(aml1List, aml1Values, "aml1:", "hasCorrespondingAttributePath");

				addAmlValues(aml1negList, aml1negValues, "aml1:", "hasAttributeName");
				addAmlValues(aml1negList, aml1negValues, "aml1:", "refBaseClassPath");
				addAmlValues(aml1negList, aml1negValues, "aml1:", "identifier");
				addAmlValues(aml1negList, aml1negValues, "aml1:", "hasCorrespondingAttributePath");

			}

			if (file.getName().equals("plfile1.ttl")) {
				addAmlValues(aml2List, aml2Values, "aml2:", "hasAttributeName");
				addAmlValues(aml2List, aml2Values, "aml2:", "refBaseClassPath");
				addAmlValues(aml2List, aml2Values, "aml2:", "identifier");
				addAmlValues(aml2List, aml2Values, "aml2:", "hasCorrespondingAttributePath");

				addAmlValues(aml2negList, aml2negValues, "aml2:", "hasAttributeName");
				addAmlValues(aml2negList, aml2negValues, "aml2:", "refBaseClassPath");
				addAmlValues(aml2negList, aml2negValues, "aml2:", "identifier");
				addAmlValues(aml2negList, aml2negValues, "aml2:", "hasCorrespondingAttributePath");
			}
		}

		
		try {
			for (int j = 0; j < aml1Values.size(); j++) {
				if (!aml1Values.get(j).equals("aml1:eClassIRDI")
						&& !aml1Values.get(j).equals("aml1:eClassClassificationClass")
						&& !aml1Values.get(j).equals("aml1:eClassVersion")) {

					if (!duplicateCheck
							.contains(aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1")) {
						duplicateCheck
								.add(aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1");

						similar.println(aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1");
					}
				}
			}

			for (int j = 0; j < aml1negValues.size(); j++) {
				if (!aml1negValues.get(j).equals("aml1:eClassIRDI")
						|| !aml1negValues.get(j).equals("aml1:eClassClassificationClass")
						|| !aml1negValues.get(j).equals("aml1:eClassVersion")) {

					if (!duplicateCheck.contains(
							aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0")
							&& !duplicateCheck.contains(aml1negValues.get(j) + "\t"
									+ aml2negValues.get(j) + "\t" + "1")) {
						duplicateCheck.add(
								aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0");

						similar.println(
								aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0");
					}
				}
			}
		} catch (Exception e) {

		}		
		similar.close();
	}
}
