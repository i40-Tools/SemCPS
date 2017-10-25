package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import util.ConfigManager;

/**
 * 
 * @author Irlan This class reports the results of the process
 */

public class Report {

	/**
	 * This function provides the root of the heterogeneity examples to
	 * reproduce result of the published paper.
	 * 
	 * @param root
	 * @throws Exception
	 */
	static void getReport(String root) throws Exception {
		int k = 2;
		while (k <= 2) {
			int i = 1;
			while (i <= 3) {
				long startTime = System.currentTimeMillis();
				if (k == 1) {
					System.out.println(root + "M1/M1.1//Testbeds-" + i);
					ConfigManager.filePath = root + "M1/M1.1//Testbeds-" + i + "/Generated/";
					SemCPSMain main2 = new SemCPSMain();
					main2.readConvertStandardFiles();
					main2.generatePSLDataModel();
					main2.executePSLAproach();

				} else {

					System.out.println(root + "M" + k + "/Testbeds-" + i);
					ConfigManager.filePath = root + "M" + k + "/Testbeds-" + i + "/Generated/";
					SemCPSMain main = new SemCPSMain();
					main.readConvertStandardFiles();
					main.generatePSLDataModel();
					main.executePSLAproach();
				}
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				
				
				FileWriter fw = new FileWriter(ConfigManager.getFilePath()+"PSL/test/Precision/F1NoTraining.txt",true);
				fw.write("Time:" + totalTime);//appends the string to the file
			    fw.close();
				
				i++;
			}
			k++;
		}
	}

	/**
	 * This function gets the size of the seeds in kb.
	 * 
	 * @param root
	 * @throws IOException
	 */
	static void getSize(String root) throws IOException {
		int k = 1;
		while (k <= 7) {
			int j = 1;

			String filesize = "";
			while (j <= 10) {
				if (k == 1) {
					File f = new File(
							root + "M" + k + "/M1.1/Testbeds-" + j + "/Generated/seed.aml");
					filesize += new DecimalFormat("#.#").format(((double) f.length() / 1024))
							+ "\n";
				} else {
					File f = new File(root + "M" + k + "/Testbeds-" + j + "/Generated/seed.aml");
					filesize += new DecimalFormat("#.#").format(((double) f.length() / 1024))
							+ "\n";
				}
				j++;
			}
			System.out.print(filesize);
			k++;
		}
	}

	/**
	 * This function reads the computed result from a file and outputs the
	 * Precision, Recall and Fmeasure.
	 * 
	 * @param root
	 * @throws IOException
	 */
	static void getResults() throws IOException {
		int k = 2;
		while (k <= 7) {
			int j = 1;
			String line;
			String precision = "";
			String recall = "";
			String fmeasure = "";
			String time = "";


			while (j <= 10) {
				BufferedReader br = new BufferedReader(new FileReader(
						new File(ConfigManager.getExperimentFolder() + "M" + k + "/Testbeds-"+j
								+ "/Generated/PSL/test/Precision/F1NoTraining.txt")));

				while ((line = br.readLine()) != null) {
					if (line.contains("Precision :")) {
						precision += line.replace("Precision :", "") + "\n";
					}
					if (line.contains("Recall:")) {
						recall += line.replace("Recall:", "") + "\n";
					}
					if (line.contains("Fmeasure:")) {
						fmeasure += line.replace("Fmeasure:", "") + "\n";
					}
					
					if (line.contains("Time:")) {
						time += line.replace("Time:", "") + "\n";
					}
				}

				j++;
			}

//			System.out.print(precision);
//			System.out.print(recall);
//			System.out.print(fmeasure);
//			System.out.print(time);

			k++;
		}
	}

	/**
	 * This function outputs the information for number of heterogeneities in a
	 * testbed.
	 * 
	 * @param root
	 * @throws IOException
	 */
	static void getResults2(String root) throws IOException {
		int k = 1;
		while (k <= 1) {
			int j = 1;
			String line;
			String multi = "";
			System.out.println("M" + k);

			while (j <= 10) {
				BufferedReader br = new BufferedReader(new FileReader(new File(root
						+ "M1/M1.1/Testbeds-" + j + "/Generated/PSL//test/Precision/multi.txt")));

				while ((line = br.readLine()) != null) {
					multi += line;
					System.out.print(line + ",");

				}
				System.out.print("\n");

				j++;
			}
			k++;
		}
	}

}
