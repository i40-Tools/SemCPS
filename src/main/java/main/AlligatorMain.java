package main;

import integration.Integration;
import integration.XSDValidator;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

import Test.ModelRepair;
import util.ConfigManager;
import groovy.lang.Script;

/**
 * 
 * Main class of the Alligator project
 * 
 * @author Irlan
 * @author Omar
 * 
 */
public class AlligatorMain {

	private Integration integration;
	private Files2Facts standardFiles = new Files2Facts();

	public static void main(String[] args) throws Throwable {
		try {
			AlligatorMain main = new AlligatorMain();
			main.readConvertStandardFiles();
			
			//main.executeDatalogApproach();
			main.generatePSLDataModel();
			//main.executePSLAproach();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that read standard files and convert then to RDF
	 * @throws Exception 
	 * TODO create more specific exceptions
	 */
	public void readConvertStandardFiles() throws Exception{
		
		standardFiles.readFiles(ConfigManager.getFilePath(), ".aml",
				".opcua", ".xml");
		standardFiles.convert2RDF();
		standardFiles.readFiles(ConfigManager.getFilePath(), ".ttl",
				".rdf", ".owl");
	}
	
	/**
	 * Generate the PSL datamodel out of the existing standard documents
	 * @throws Exception 
	 */
	public void generatePSLDataModel() throws Exception{
		standardFiles.generatePSLPredicates(ConfigManager.getFilePath());
	}

	/**
	 * General method to execute the PSL-based approach
	 * @throws CompilationFailedException
	 * @throws IOException
	 */
	public void executePSLAproach() throws CompilationFailedException, IOException{
		// Needed to run the PSL rules part
		Script script = new Script() {
			@Override
			public Object run() {
				return null;
			}
		};
		script.evaluate(new File("src/main/java/matching/OntologyAlignment.groovy"));
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
	 * Method used to integrate the documents taking the results from the inference
	 * @throws Throwable
	 * TODO create more specific exceptions
	 */
	public void integrate() throws Throwable{
		integration = new Integration();
		integration.integrate();
		// check for validity
		File file = new File(ConfigManager.getFilePath() +
				"integration/integration.aml");
		if (file.exists()) {
			if (!new XSDValidator(ConfigManager.getFilePath() +
					"integration/integration.aml").schemaValidate()) {
				System.out.println("Repairing Structure");
				ModelRepair.testRoundTrip(ConfigManager.getFilePath() +
						"integration/integration.aml");
				System.out.println("Schema Validated");
			}
		}
	}
}
