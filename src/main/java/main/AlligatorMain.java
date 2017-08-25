package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

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
	private Similar similar = new Similar();

	public static void main(String[] args) throws Throwable {

		getReport(ConfigManager.getExperimentFolder());
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
	 * Provide root of the heterogeneity examples to reproduce the result of the
	 * published paper.
	 * @param root
	 * @throws Exception
	 */
	static void getReport(String root) throws Exception {
		int k = 5;
		while (k <=5) {
			int i =6;
			while (i <= 10) {
				if (k == 1) {
					System.out.println(root + "M1/M1.1//Testbeds-" + i);
					ConfigManager.filePath = root + "M1/M1.1//Testbeds-" + i + "/Generated/";
					AlligatorMain main2 = new AlligatorMain();
					main2.readConvertStandardFiles();
					main2.generatePSLDataModel();
					main2.executePSLAproach();
				}

				else {
					System.out.println(root + "M" + k + "/Testbeds-" + i);

					ConfigManager.filePath = root + "M" + k + "/Testbeds-" + i + "/Generated/";

					AlligatorMain main = new AlligatorMain();
					main.readConvertStandardFiles();
					main.generatePSLDataModel();
					main.executePSLAproach();
				}
				i++;
			}
			k++;
		}
	}

	/**
	 * Reads the result.txt and output results.
	 * @param root
	 * @throws IOException
	 */
	static void getresults(String root) throws IOException {
		int k = 1;
		while (k <= 1) {
			int j = 1;
			String line;
			String precision = "";
			String recall = "";
			String fmeasure = "";

			while (j <= 10) {
				BufferedReader br = new BufferedReader(
						new FileReader(new File(ConfigManager.getExperimentFolder() + "M" + k + 
								"/Testbeds-" + "/Generated/PSL//test/Precision/F1NoTraining.txt")));

				while ((line = br.readLine()) != null) {
					if (line.contains("Precision :")) {
						precision += line.replace("Precision :", "")+"\n";
					}
					if (line.contains("Recall:")) {
						recall += line.replace("Recall:", "")+"\n";
					}
					if (line.contains("Fmeasure:")) {
						fmeasure += line.replace("Fmeasure:", "")+"\n";
					}

				}

				j++;
			}
			System.out.print(precision);
			System.out.print(recall);
			System.out.print(fmeasure );
			k++;
		}
	}


	static void getsize(String root) throws IOException {
		int k = 1;
		while (k <= 7) {
			int j = 1;

			String filesize="";
			while (j <= 10) {
				if(k==1){
					File f=new File(root+"M"+k+"/M1.1/Testbeds-" + j
							+ "/Generated/seed.aml");
					filesize+= new DecimalFormat("#.#").format(((double)f.length()/ 1024))+"\n"; 
				}

				else{
					File f=new File(root+"M"+k+"/Testbeds-" + j
							+ "/Generated/seed.aml");
					filesize+= new DecimalFormat("#.#").format(((double)f.length()/ 1024))+"\n"; 

				}
				j++;
			}
			System.out.print(filesize);
			k++;
		}
	}


	static void getresults2(String root) throws IOException {
		int k = 1;
		while (k <= 1) {
			int j = 1;
			String line;
			String precision = "";
			String recall = "Recall";
			String fmeasure = "F-Measure";
			System.out.println("M" + k);

			while (j <= 10) {
				BufferedReader br = new BufferedReader(
						new FileReader(new File(root+"M1/M1.1/Testbeds-" + j
								+ "/Generated/PSL//test/Precision/multi.txt")));

				while ((line = br.readLine()) != null) {
					precision += line;
					System.out.print(line+",");

				}
				System.out.print("\n");

				j++;
			}
			k++;
		}
	}


	/**
	 * Models similar.txt in to GoldStandard format.
	 * @throws Exception 
	 */
	public void modelSimilar() throws Exception {
		similar.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
		similar.convertSimilar();
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
		similar.readFiles(ConfigManager.getFilePath(), ".ttl", ".rdf", ".owl");
	}

	/**
	 * Generate the PSL datamodel out of the existing standard documents
	 * 
	 * @throws Exception
	 */
	public void generatePSLDataModel() throws Exception {
		ConfigManager.createDataPath();// creates folders if not there
		similar.generatePSLPredicates(ConfigManager.getFilePath());
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
		try {
			script.evaluate(new File("src/main/java/pslApproach/KGAlignment.groovy"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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
