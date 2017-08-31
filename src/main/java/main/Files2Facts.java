
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import industryStandard.AML;
import industryStandard.IndustryStandards;
import uni.bonn.krextor.Krextor;
import util.ConfigManager;

/**
 * Reads the converted RDF files and convert them to PSL facts
 * 
 * @author Irlan 28.06.2016
 */
public class Files2Facts extends IndustryStandards {

	protected ArrayList<File> files;

	private LinkedHashSet<String> subjectsToWrite;

	public Files2Facts() {
		IndustryStandards industryStandard = new IndustryStandards();
		files = new ArrayList<File>();
	}

	/**
	 * Converts files from XML to RDF-based format(turtle) based on Krextor
	 * 
	 * @param input
	 * @param output
	 */
	public void convert2RDF() {
		int i = 0;
		Krextor krextor = new Krextor();

		for (File file : files) {
			if (file.getName().endsWith(".aml")) {
				if (file.getName().equals("seed.aml")) {
					krextor.convertRdf(file.getAbsolutePath(), "aml", "turtle",
							ConfigManager.getFilePath() + "seed" + ".ttl");

				} else {
					krextor.convertRdf(file.getAbsolutePath(), "aml", "turtle",
							ConfigManager.getFilePath() + "plfile" + i + ".ttl");
				}
			} else {
				krextor.convertRdf(file.getAbsolutePath(), "opcua", "turtle",
						ConfigManager.getFilePath() + "plfile" + i + ".ttl");
			}
			i++;
		}
	}

	/**
	 * Read RDF files of a given path
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public ArrayList<File> readFiles(String path, String type, String type2, String type3)
			throws Exception {

		File originalFilesFolder = new File(path);
		if (originalFilesFolder.isDirectory()) {
			for (File amlFile : originalFilesFolder.listFiles()) {
				if (amlFile.isFile()
						&& (amlFile.getName().endsWith(type) || amlFile.getName().endsWith(type2)
								|| amlFile.getName().endsWith(type3))) {
					if (amlFile.getName().endsWith(".aml")) {
						String name = amlFile.getName().replace(".aml", "");
						if (name.endsWith("0") || name.endsWith("1") || name.equals("seed")) {
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
	 * Adds a better turtle format for the obtained RDF files
	 * 
	 * @throws IOException
	 */
	public void improveRDFOutputFormat() throws IOException {
		for (File file : files) {
			if (file.getName().endsWith(".ttl")) {
				org.apache.jena.rdf.model.Model model = RDFDataMgr
						.loadModel(file.getAbsolutePath());
				File replaceFile = new File(file.getParent() + "/" + file.getName());
				FileOutputStream out = new FileOutputStream(replaceFile, false);
				RDFDataMgr.write(out, model, RDFFormat.TURTLE_BLOCKS);
				out.close();
			}
		}
	}

	/**
	 * Reads the RDF files and extract their contents for creating PSL
	 * predicates.
	 * 
	 * @param file
	 * @param number
	 * @param standard
	 * @return
	 * @throws Exception
	 */
	public String createPSLPredicate(File file, int number, String standard, AML aml)
			throws Exception {

		InputStream inputStream = FileManager.get().open(file.getAbsolutePath());
		Model model = ModelFactory.createDefaultModel();
		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		subjectsToWrite = new LinkedHashSet<String>();
		switch (standard) {

		case "aml":
			aml.setModel(model);
			aml.setNumber(number);
			aml.addsDataforAML(); // process required data for AML

			writeData(aml);
		}
		return "";
	}

	/**
	 * Reads RDF files with Standards data and create PSL files with this
	 * content The name of the files match with the list without repetition of
	 * RDF predicates
	 * 
	 * @param collection
	 * @param documentwriter
	 * @throws FileNotFoundException
	 */
	private void writeData(AML aml) throws FileNotFoundException {
		try {
			Set<String> predicates = aml.generic.keySet(); // gets predicates to
															// name the data
															// files

			for (String i : predicates) {
				// name files as predicates
				PrintWriter documentwriter = new PrintWriter(
						ConfigManager.getFilePath() + "PSL/test/" + i + ".txt");
				Collection<String> values = aml.generic.get(i);// for every
																// predicate get
																// its value
				for (String val : values) {

					// remove annotation to make it a literal value
					if (val.contains("aml:remove")) {
						val = val.replace("aml:remove", "");
					}

					if (val.contains(":ConnectionPoint")) {
						val = val.replace(":ConnectionPoint", "");
					}

					documentwriter.println(val);
				}

				documentwriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates PSL predicates
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void generatePSLPredicates(String path) throws Exception {
		int i = 1;
		AML aml = new AML();
		for (File file : files) {
			// pass in the writers
			if (!file.getName().equals("seed.ttl")) {
				createPSLPredicate(file, i++, ConfigManager.getStandard(), aml);
			}
		}
	}

}
