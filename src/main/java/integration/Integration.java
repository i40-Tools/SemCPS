package integration;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import main.Files2Facts;
import util.ConfigManager;

/**
 * 
 * Integrates Two AML files based on PSL Rules.
 */
public class Integration {

	private XMLParser xml;
	public static int count = 0;
	ArrayList<File> file;

	/**
	 * This method integrates two AML files.
	 * 
	 * @throws Throwable
	 */

	public Integration() {

		xml = new XMLParser();

	}

	public void integrate() throws Throwable {

		Files2Facts filesAMLInRDF = new Files2Facts();

		// gets heterogeneity files in array.
		file = filesAMLInRDF.readFiles(ConfigManager.getFilePath(), ".aml", ".opcua", ".xml");

		String contents = FileUtils.readFileToString(new File(file.get(1).getPath()), "UTF-8");

		new File(ConfigManager.getFilePath() + "integration/").mkdir();

		// One of the AML file will have its contents copied as it is.
		PrintWriter outputWriter;
		if (file.get(1).getName().endsWith(".aml")) {
			outputWriter = new PrintWriter(
					new File(ConfigManager.getFilePath() + "integration/integration.aml"));
		} else {
			outputWriter = new PrintWriter(
					new File(ConfigManager.getFilePath() + "integration/integration.opcua"));
		}
		outputWriter.println(contents);
		outputWriter.close();

		// initializing documents.

		Document seed = xml.initInput(file.get(0).getPath());
		Document integration;
		if (file.get(0).getName().endsWith(".aml") && file.get(1).getName().endsWith(".aml")) {
			integration = xml
					.initInput(ConfigManager.getFilePath() + "integration/integration.aml");
		} else {
			integration = xml
					.initInput(ConfigManager.getFilePath() + "integration/integration.opcua");

		}

		processNodesArributes(seed, integration);
		processNodesValues(seed, integration);

	}

	/**
	 * Algorithm for integrating data for nodes with attributes
	 * 
	 * compareConflicts(skips compared one),compareNonConflicts,addNonConflicts
	 * 
	 * @param seed
	 * @param integration
	 * @throws Throwable
	 */
	public void processNodesArributes(Document seed, Document integration) throws Throwable {

		xml.getAllNodes(seed, integration);

		// looping through the seedNode which will be compared to matching
		// elements in output.txt
		for (int i = 0; i < xml.getSeedNodes().size(); i++) {

			// not in the conflicting Element of output.txt
			if (xml.compareConflicts(i, seed, integration) == 0) {

				// we run our noConflicting comparision algorithm
				if (xml.compareNonConflicts(i, seed, integration) != 1) {
					// if its identified its not in integration.aml
					// We need to add non match elements to the integration
					// file.

					xml.addNonConflicts(i, seed, integration);

				}

			}
		}
		// update the integration.aml file
		xml.finalizeIntegration(integration, file.get(0).getName());

	}

	/**
	 * Algorithm for data integration for nodes with values
	 * 
	 * compareConflicts(skips compared one),compareNonConflictsNodes with
	 * value,addNonConflicts
	 * 
	 * @param seed
	 * @param integration
	 * @throws Throwable
	 */
	void processNodesValues(Document seed, Document integration) throws Throwable {

		// update for node values, array's updated.
		xml.setNodeValues(seed, integration);

		// looping through the seedNode which will be compared to matching
		// elements in output.txt
		for (int i = 0; i < xml.getSeedNodes().size(); i++) {

			// not in the conflicting Element of output.txt
			if (xml.compareConflicts(i, seed, integration) == 0) {

				// we run our noConflicting comparision algorithm
				if (xml.compareNonConflictsValues(i, seed, integration) != 1) {

					// if its identified its not in integration.aml
					// We need to add only non matched elements to the
					// integration file.

					xml.addNonConflictsValues(i, seed, integration);

				}

			}
		}

		xml.finalizeIntegration(integration, file.get(0).getName());

	}

}