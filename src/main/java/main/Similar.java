package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.ModelFactory;

import util.ConfigManager;

/**
 * Converts similar.txt results in to RDf predicate values.
 * 
 * @author Omar Rana
 *
 */

public class Similar extends Files2Facts {

	private ArrayList<String> duplicateCheck;

	public Similar() {

	}

	/**
	 * Adds a better turtle format for the obtained RDF files
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 */
	public void convertSimilar() throws FileNotFoundException {
		ArrayList<String> aml1List = new ArrayList<String>();
		ArrayList<String> aml2List = new ArrayList<String>();
		ArrayList<String> aml1negList = new ArrayList<String>();
		ArrayList<String> aml2negList = new ArrayList<String>();
		ArrayList<String> aml1Values = new ArrayList<String>();
		ArrayList<String> aml2Values = new ArrayList<String>();
		ArrayList<String> aml1negValues = new ArrayList<String>();
		ArrayList<String> aml2negValues = new ArrayList<String>();
		duplicateCheck = new ArrayList<String>();

		try {
			try (BufferedReader br = new BufferedReader(new FileReader(
					new File(ConfigManager.getFilePath() + "PSL/test/similar.txt")))) {
				String line;
				while ((line = br.readLine()) != null) {
					String values[] = line.split(",");
					if (values.length > 1)
						if (line.contains("truth:1")) {
							aml1List.add(values[0].replaceAll("aml1:", ""));
							aml2List.add(values[1].replaceAll("aml2:", ""));

						} else {
							aml1negList.add(values[0].replaceAll("aml1:", ""));
							aml2negList.add(values[1].replaceAll("aml2:", ""));
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		PrintWriter similar = new PrintWriter(ConfigManager.getFilePath() + "PSL/test/similar.txt");

		for (File file : files) {
			InputStream inputStream = com.hp.hpl.jena.util.FileManager.get()
					.open(file.getAbsolutePath());
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

		String results = "";
		for (int j = 0; j < aml1Values.size(); j++) {
			if (!aml1Values.get(j).equals("aml1:eClassIRDI")
					&& !aml1Values.get(j).equals("aml1:eClassClassificationClass")
					&& !aml1Values.get(j).equals("aml1:eClassVersion")) {

				if (!duplicateCheck
						.contains(aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1")) {
					duplicateCheck.add(aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1");

					results += aml1Values.get(j) + "\t" + aml2Values.get(j) + "\t" + "1" + "\n";

				}
			}
		}

		for (int j = 0; j < aml1negValues.size(); j++) {
			if (!aml1negValues.get(j).equals("aml1:eClassIRDI")
					|| !aml1negValues.get(j).equals("aml1:eClassClassificationClass")
					|| !aml1negValues.get(j).equals("aml1:eClassVersion")) {

				if (!duplicateCheck
						.contains(aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0")
						&& !duplicateCheck.contains(
								aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "1")) {
					duplicateCheck
							.add(aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0");

					results += aml1negValues.get(j) + "\t" + aml2negValues.get(j) + "\t" + "0"
							+ "\n";
				}
			}
		}

		similar.println(results);

		similar.close();
		convertDocument();
	}

	public void convertDocument() {
		try {
			ArrayList<String> aml1negList = new ArrayList<String>();
			ArrayList<String> aml2negList = new ArrayList<String>();
			HashMap<String, String> aml1negValues = new HashMap<String, String>();
			HashMap<String, String> aml2negValues = new HashMap<String, String>();
			HashMap<String, String> aml1negpred = new HashMap<String, String>();
			HashMap<String, String> aml2negpred = new HashMap<String, String>();

			ArrayList<String> otherValues = new ArrayList<String>();
			ArrayList<String> otherValues2 = new ArrayList<String>();

			try (BufferedReader br = new BufferedReader(new FileReader(
					new File(ConfigManager.getFilePath() + "PSL/test/hasDocument.txt")))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.contains("aml1")) {
						line = line.replaceAll("\t" + "aml1", "");
						aml1negList.add(line.replaceAll("aml1:", ""));
					}

					else {
						line = line.replaceAll("\t" + "aml2", "");
						aml2negList.add(line.replaceAll("aml2:", ""));
					}

				}

			}

			try (BufferedReader br = new BufferedReader(new FileReader(
					new File(ConfigManager.getFilePath() + "PSL/test/hastype.txt")))) {
				String line;
				while ((line = br.readLine()) != null) {
					String values[] = line.split("\t");
					if (values.length > 1)

						if (line.contains("aml1")) {
							otherValues.add(values[1].replaceAll("aml1:", ""));
						}

						else {
							otherValues2.add(values[1].replaceAll("aml2:", ""));
						}

				}

			}

			PrintWriter similar = new PrintWriter(new FileOutputStream(
					new File(ConfigManager.getFilePath() + "PSL/test/similar.txt"), true));

			for (File file : files) {
				InputStream inputStream = com.hp.hpl.jena.util.FileManager.get()
						.open(file.getAbsolutePath());
				model = ModelFactory.createDefaultModel();

				model.read(new InputStreamReader(inputStream), null, "TURTLE");

				if (file.getName().equals("plfile0.ttl")) {

					addAmlNegValues(aml1negList, aml1negValues, "aml1:", "hasAttributeName",
							otherValues, aml1negpred);
					addAmlNegValues(aml1negList, aml1negValues, "aml1:", "refBaseClassPath",
							otherValues, aml1negpred);
					addAmlNegValues(aml1negList, aml1negValues, "aml1:", "identifier", otherValues,
							aml1negpred);
				}

				if (file.getName().equals("plfile1.ttl")) {
					addAmlNegValues(aml2negList, aml2negValues, "aml2:", "hasAttributeName",
							otherValues2, aml2negpred);
					addAmlNegValues(aml2negList, aml2negValues, "aml2:", "refBaseClassPath",
							otherValues2, aml2negpred);
					addAmlNegValues(aml2negList, aml2negValues, "aml2:", "identifier", otherValues2,
							aml2negpred);
				}
			}


			try {
				for (String key : aml1negValues.keySet()) {
					for (String negkey : aml2negValues.keySet()) {

						String type1 = aml1negValues.get(key);
						String type2 = aml2negValues.get(negkey);
						String pred1 = aml1negpred.get(key);
						String pred2 = aml2negpred.get(negkey);

						if (!duplicateCheck.contains(key + "\t" + negkey + "\t" + "1"))

						{
							if (type1.equals(type2) && pred1.equals(pred2)) {
								similar.println(key + "\t" + negkey + "\t" + "0");

							}
						}
					}

				}
			} catch (Exception e) {

			}
			similar.close();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
