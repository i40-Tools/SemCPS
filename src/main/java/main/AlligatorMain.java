package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import org.codehaus.groovy.control.CompilationFailedException;

import Test.ModelRepair;
import datalogApproach.DeductiveDB;
import groovy.lang.Script;
import groovy.util.ResourceException;
import integration.Integration;
import integration.XSDValidator;
import util.ConfigManager;

/**
 * Main class of the Alligator project
 * 
 * @author Irlan
 * @author Omar
 * 
 */
public class AlligatorMain {

	private Integration integration;
	private Files2Facts standardFiles = new Files2Facts();
	private GoldStandard goldStandard = new GoldStandard();

	public static void main(String[] args) throws Throwable {

		AlligatorMain main = new AlligatorMain();
		main.readConvertStandardFiles();	
		main.generatePSLDataModel();
		main.executePSLAproach();
		// main.executeDatalogApproach();
		// main.integrate();
		// main.executePSLAproach();
		// main.integrate();

	}

	/**
	 * Models similar.txt in to GoldStandard format.
	 * @throws Exception 
	 */
	public void modelSimilar() throws Exception {
		goldStandard.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
		goldStandard.convertSimilar();
	}

	/**
	 * Method that read standard files and convert then to RDF
	 * 
	 * @throws Exception
	 *             TODO create more specific exceptions
	 */
	public void readConvertStandardFiles() throws Exception {
		standardFiles.readFiles(ConfigManager.getFilePath(), ".aml", ".opcua", ".xml");
		standardFiles.convert2RDF();
		standardFiles = new Files2Facts();
		standardFiles.improveRDFOutputFormat();
		goldStandard.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
		//standardFiles.readFiles(ConfigManager.getOntoURIPath(), ".ttl", ".rdf", ".owl");
	}

	/**
	 * Generate the PSL datamodel out of the existing standard documents
	 * 
	 * @throws Exception
	 */
	public void generatePSLDataModel() throws Exception {
		ConfigManager.createDataPath();// creates folders if not there
		goldStandard.addGoldStandard();
		standardFiles.generatePSLPredicates(ConfigManager.getFilePath());
	}

	/**
	 * General method to execute the PSL-based approach
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
		//		 script.evaluate(new File("src/main/java/pslApproach/EasyLP.groovy"));
		script.evaluate(new File("src/main/java/pslApproach/DocumentAlignment.groovy"));
	}



	/**
	 * General method to execute the Datalog-based approach
	 * @throws Throwable
	 * TODO create more specific exceptions
	 */
	public void executeDatalogApproach() throws Throwable {
		standardFiles.prologFilePath();
		standardFiles.generateExtensionalDB(ConfigManager.getFilePath());
		DeductiveDB deductiveDB = new DeductiveDB();
		// formats the output.txt in java objects
		deductiveDB.readWorkingDirectory();
		deductiveDB.executeKB();
		// formats the output.txt in java objects
		deductiveDB.readOutput();
		deductiveDB.consultKB();
	}

	/**
	 * Method used to integrate the documents taking the results from the
	 * inference
	 * 
	 * @throws Throwable
	 *             TODO create more specific exceptions
	 */
	public void integrate() throws Throwable {
		integration = new Integration();
		integration.integrate();
		// check for validity
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
}
