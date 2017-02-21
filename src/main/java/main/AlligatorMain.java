package main;

import java.io.File;

import groovy.lang.Binding;
import groovy.lang.Script;

/**
 * 
 * Main class of the Alligator project
 * 
 * @author Irlan
 * @author Omar
 */
public class AlligatorMain {

	public static void main(String[] args) throws Throwable {

		// PSL rules part
		Binding binding = new Binding();
		binding.setVariable("foo", new Integer(2));
		Script script = new Script() {

			@Override
			public Object run() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		script.evaluate(new File("src/main/java/edu/umd/cs/example/OntologyAlignment.groovy"));

		// Automation ML Rules part

		Files2Facts filesAMLInRDF = new Files2Facts();
		try {

			// filesAMLInRDF.prologFilePath();
			// filesAMLInRDF.readFiles(ConfigManager.getFilePath(), ".aml",
			// ".opcua", ".xml");
			// filesAMLInRDF.convertRdf();
			// filesAMLInRDF.readFiles(ConfigManager.getFilePath(), ".ttl",
			// ".rdf", ".owl");
			// filesAMLInRDF.generateExtensionalDB(ConfigManager.getFilePath());
			//
			// DeductiveDB deductiveDB = new DeductiveDB();
			// // formats the output.txt in java objects
			// deductiveDB.readWorkingDirectory();
			//
			// deductiveDB.executeKB();
			// // formats the output.txt in java objects
			// deductiveDB.readOutput();
			// deductiveDB.consultKB();
			//
			// // integrating files
			// Integration integ = new Integration();
			// integ.integrate();
			//
			// // chec valdty
			// File file = new File(ConfigManager.getFilePath() +
			// "integration/integration.aml");
			// if (file.exists()) {
			// if (!new XSDValidator(ConfigManager.getFilePath() +
			// "integration/integration.aml").schemaValidate()) {
			// System.out.println("Repairing Structure");
			// ModelRepair.testRoundTrip(ConfigManager.getFilePath() +
			// "integration/integration.aml");
			// System.out.println("Schema Validated");
			//
			// }
			// }

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}
