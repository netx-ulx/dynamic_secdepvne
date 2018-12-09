package pt.SecDepVNE.Revenue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class Revenue {

	public static void main(String[] args) {

		try {
			readDvineRevenue();
			readSecDepRevenue();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void readDvineRevenue() throws IOException {
		String[] parts;
		String line;

		Double revenue = 0.0;

		String[] nodesID = null, linksID = null;

		FileWriter fileWriter = new FileWriter("/root/malaluna/secdep_18nov16/revenues/dvine.txt");
		BufferedWriter writer = new BufferedWriter(fileWriter);

		for(int i = 0; i < 1000; i++){

			if(wasAccepted("/root/malaluna/secdep_18nov16/glpk/outputFiles/DViNE/random_exp1/random_req"+i+".txt")) {

				FileReader fileReader = new FileReader("/root/malaluna/secdep_18nov16/glpk/datFiles/DViNE/random_exp1/random_req"+i+".dat");
				BufferedReader reader = new BufferedReader(fileReader);

				for(int j = 0; j < 5; j++)
					reader.readLine();


				line = reader.readLine();
				parts = line.split(" ");

				nodesID = new String[parts.length - 4];

				for(int j = 0; j < nodesID.length; j++)
					nodesID[j] = parts[j+3];


				line = reader.readLine();
				parts = line.split(" ");


				linksID = new String[parts.length - 4];

				for(int j = 0; j < linksID.length; j++)
					linksID[j] = parts[j+3];


				for(int j = 0; j < 27; j++)
					reader.readLine();

				for(int j = 0; j < nodesID.length; j++){
					line = reader.readLine();
					parts = line.split(" ");
					nodesID[j] = parts[1];
				}

				while( (line = reader.readLine()) != null) {

					if(line.contains("param fd := ")){
						for(int j = 0; j < linksID.length; j++){
							line = reader.readLine();
							parts = line.split(" ");
							linksID[j] = parts[1];
						}

					}
				}

				reader.close();

				writer.write("------------ Req "+i+" --------------\n");
				writer.write("nNodes: "+nodesID.length+"\n");
				for(int j = 0; j < nodesID.length; j++){
					writer.write(nodesID[j]+"\n");
					revenue += Double.parseDouble(nodesID[j]);
				}
				writer.write("nLinks: "+linksID.length+"\n");
				for(int j = 0; j < linksID.length; j++){
					writer.write(linksID[j]+"\n");
					revenue += Double.parseDouble(linksID[j]);
				}

			}

		}

		writer.write("dvine total revenue: "+revenue);

		writer.close();

	}

	public static void readSecDepRevenue() throws IOException {
		String[] parts;
		String line;

		Double revenue = 0.0, weightedRevenue = 0.0, tmp = 0.0;

		String[] nodesID = null, linksID = null, nodesSec = null, linksSec = null, cloudSec = null;

		boolean exit = false;


		FileWriter fileWriter = new FileWriter("/root/malaluna/secdep_18nov16/revenues/mixsecdep_5.txt");
		BufferedWriter writer = new BufferedWriter(fileWriter);

		for(int i = 0; i < 1000; i++){

			if(wasAccepted("/root/malaluna/secdep_18nov16/glpk/outputFiles/SecDep/random_exp7/random_req"+i+".txt")) {

				FileReader fileReader = new FileReader("/root/malaluna/secdep_18nov16/glpk/datFiles/SecDep/random_exp7/random_req"+i+".dat");
				BufferedReader reader = new BufferedReader(fileReader);

				while( (line = reader.readLine()) != null && !exit) {

					if(line.contains("set VNodes := ")){

						parts = line.split(" ");

						nodesID = new String[parts.length - 4];
						nodesSec = new String[parts.length - 4];
						cloudSec = new String[parts.length - 4];

						for(int j = 0; j < nodesID.length; j++) {
							nodesID[j] = parts[j+3];
						}

						exit = true;

					}
				}

				line = reader.readLine();

				parts = line.split(" ");

				linksID = new String[parts.length - 4];
				linksSec = new String[parts.length - 4];

				for(int j = 0; j < linksID.length; j++) {
					linksID[j] = parts[j+3];
				}

				reader.readLine();
				reader.readLine();

				for(int j = 0; j < nodesID.length; j++){
					line = reader.readLine();

					parts = line.split(" ");

					nodesID[j] = parts[1];
				}

				reader.readLine();
				reader.readLine();
				reader.readLine();

				for(int j = 0; j < nodesSec.length; j++){
					line = reader.readLine();

					parts = line.split(" ");

					nodesSec[j] = parts[1];
				}

				reader.readLine();
				reader.readLine();
				reader.readLine();

				for(int j = 0; j < linksID.length; j++){
					line = reader.readLine();

					parts = line.split(" ");

					linksID[j] = parts[2];
				}

				reader.readLine();
				reader.readLine();
				reader.readLine();

				for(int j = 0; j < linksSec.length; j++){
					line = reader.readLine();

					parts = line.split(" ");

					linksSec[j] = parts[2];
				}
				
				reader.readLine();
				reader.readLine();
				reader.readLine();

				for(int j = 0; j < cloudSec.length; j++){
					line = reader.readLine();

					parts = line.split(" ");

					cloudSec[j] = parts[1];
				}

				reader.close();

				writer.write("------------ Req "+i+" --------------\n");
				writer.write("nNodes: "+nodesID.length+"\n");
				for(int j = 0; j < nodesID.length; j++){
					writer.write(nodesID[j]+" "+nodesSec[j]+" "+cloudSec[j]+"\n");
					revenue += Double.parseDouble(nodesID[j]) * Double.parseDouble(nodesSec[j]) * Double.parseDouble(cloudSec[j]);

					if(Double.parseDouble(nodesSec[j]) == 1.0)
						tmp = Double.parseDouble(nodesID[j]);
					else if(Double.parseDouble(nodesSec[j]) == 1.1)
						tmp = Double.parseDouble(nodesID[j]) * 1.4;
					else if(Double.parseDouble(nodesSec[j]) == 1.2)
						tmp = Double.parseDouble(nodesID[j]) * 1.6;
					
					if(Double.parseDouble(cloudSec[j]) == 1.0)
						tmp *= 1.0;
					else if(Double.parseDouble(cloudSec[j]) == 1.1)
						tmp *= 1.4;
					else if(Double.parseDouble(cloudSec[j]) == 1.2)
						tmp *= 1.6;
					
					weightedRevenue += tmp;
					
					tmp = 0.0;

				}

				writer.write("nLinks: "+linksID.length+"\n");
				for(int j = 0; j < linksID.length; j++){
					writer.write(linksID[j]+" "+linksSec[j]+"\n");
					revenue += Double.parseDouble(linksID[j]) * Double.parseDouble(linksSec[j]);

					if(Double.parseDouble(linksSec[j]) == 1.0)
						weightedRevenue += Double.parseDouble(linksID[j]);
					if(Double.parseDouble(linksSec[j]) == 1.1)		
						weightedRevenue += Double.parseDouble(linksID[j]) * 1.4;
					if(Double.parseDouble(linksSec[j]) == 1.2)
						weightedRevenue += Double.parseDouble(linksID[j]) * 1.6;

				}
			}

			exit = false;

		}

		writer.write("secdep total revenue: "+revenue+"\n");
		writer.write("secdep total weighted revenue: "+weightedRevenue+"\n");

		writer.close();

	}

	public static boolean wasAccepted(String outputFile) throws IOException {
		String line = null;
		boolean wasAccepted = true;

		FileReader fileReader = new FileReader(outputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {

			// Types of request denial
			if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
					line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
					line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION") ||
					line.contains("Time used: 540"))
				wasAccepted = false;
		}

		bufferedReader.close();

		return wasAccepted;
	}

}
