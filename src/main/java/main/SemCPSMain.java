package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import org.codehaus.groovy.control.CompilationFailedException;

import Test.ModelRepair;
import groovy.lang.Script;
import groovy.util.ResourceException;
import integration.Integration;
import integration.XSDValidator;
import util.ConfigManager;

/**
 * Main class of the SemCPS project
 * 
 * @author Irlan
 * @author Omar
 * 
 */

public class SemCPSMain {

	private Files2Facts standardFiles = new Files2Facts();
	private Similar similar = new Similar();

	public static void main(String[] args) throws Throwable {

		// Report.getReport(ConfigManager.getExperimentFolder());
		// Report.getResults();
		// System.exit(0);
		SemCPSMain main = new SemCPSMain();
		main.readConvertStandardFiles();
		main.generatePSLDataModel();
		main.executePSLAproach();
		// main.integrate();

		// main.executePSLAproach();
		// main.integrate();
	}

	/**
	 * This function Models similar.txt in to GoldStandard format. This modeling
	 * is required for computation of Precision and Recall.
	 * 
	 * @throws Exception
	 */
	public void modelSimilar() throws Exception {
		similar.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
		similar.convertSimilar();
	}

	/**
	 * This function read standard files and convert then to RDF
	 * 
	 * @throws Exception
	 */
	public void readConvertStandardFiles() throws Exception {
		standardFiles.readFiles(ConfigManager.getFilePath(), ".aml", ".opcua", ".xml");
		standardFiles.convert2RDF();
		standardFiles = new Files2Facts();
		standardFiles.improveRDFOutputFormat();
		similar.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
	}

	/**
	 * This function generate the PSL datamodel out of the existing standard
	 * documents.
	 * 
	 * @throws Exception
	 */
	public void generatePSLDataModel() throws Exception {
		ConfigManager.createDataPath();// creates folders if not there
		similar.generatePSLPredicates(ConfigManager.getFilePath());
	}

	/**
	 * This function integrates two AML files based on PSL rules.
	 * 
	 * @throws Throwable
	 */

	public void integrate() throws Throwable {
		Integration integrate = new Integration();
		integrate.integrate();
		// checks valdity and repairs the aml file.
		File file = new File(ConfigManager.getFilePath() + "integration/integration.aml");
		if (file.exists()) {
			if (!new XSDValidator(ConfigManager.getFilePath() + "integration/integration.aml")
					.schemaValidate()) {
				System.out.println("Repairing Structure");
				ModelRepair
						.testRoundTrip(ConfigManager.getFilePath() + "integration/integration.aml");
				System.out.println("Schema Validated");

			}
		}

	}

	/**
	 * This function is a general method to execute the PSL-based approach.
	 * 
	 * @throws CompilationFailedException
	 * @throws IOException
	 * @throws ScriptException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws groovy.util.ScriptException
	 * @throws ResourceException
	 */
	public void executePSLAproach() throws CompilationFailedException, IOException, ScriptException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstantiationException, ResourceException,
			groovy.util.ScriptException {
		// Needed to run the PSL rules part
		Script script = new Script() {
			@Override
			public Object run() {
				return null;
			}
		};
		try {
			script.evaluate(new File("src/main/java/pslApproach/KGAlignment.groovy"));
		} catch (Exception e) {
		}
	}

}
