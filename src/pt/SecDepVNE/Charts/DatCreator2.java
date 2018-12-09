package pt.SecDepVNE.Charts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DatCreator2 {
	
	public static void main(String[] args) throws IOException {
		
		int nodes = Integer.parseInt(args[0]); //50 nodes
		String secDep = args[1]; //sec0_dep0 sec0_dep20 sec20_dep0 sec20_dep20
		int index = Integer.parseInt(args[2]); //experience index
		String reconfig = args[3]; //noRec duration size recent
		
		int NUM_REPS = 5; //Number of repetitions for each experience
		int NUM_HEU = (nodes == 50) ? 7 : 6; //Number of heuristics for node mapping
		
		ArrayList<Double> acceptanceRate = new ArrayList<>();
		ArrayList<Double> nodeStress = new ArrayList<>();
		ArrayList<Double> linkStress = new ArrayList<>();
		
		ArrayList<Double> acceptanceRateStd = new ArrayList<>();
		ArrayList<Double> nodeStressStd = new ArrayList<>();
		ArrayList<Double> linkStressStd = new ArrayList<>();
		
		for(int i = index; i < NUM_HEU + index; i++){
			double totalAR = 0.0, totalNode = 0.0, totalLink = 0.0;
			ArrayList<Double> aR = new ArrayList<>();
			ArrayList<Double> nS = new ArrayList<>();
			ArrayList<Double> lS = new ArrayList<>();
			
			for(int j = 1; j < NUM_REPS + 1; j++){
				FileReader fReader = new FileReader("./final_results/"+secDep+"/"+reconfig+"/"+nodes+"sub_exp"+j+"/DynamicExp"+i+".dat");
				BufferedReader reader = new BufferedReader(fReader);
				
				String line = "", line2 = "";
				while(line != null){
					line2 = reader.readLine();
					if(line2 != null){
						line = line2;
					} else {
						break;
					}
				}
				
				String[] parts = line.split(" +");
				
				totalAR += Double.parseDouble(parts[5]);
				totalNode += Double.parseDouble(parts[3]);
				totalLink += Double.parseDouble(parts[4]);
				
				aR.add(Double.parseDouble(parts[5]));
				nS.add(Double.parseDouble(parts[3]));
				lS.add(Double.parseDouble(parts[4]));
				
				reader.close();
			}
			
			acceptanceRate.add(totalAR/NUM_REPS);
			nodeStress.add(totalNode/NUM_REPS);
			linkStress.add(totalLink/NUM_REPS);
			
			double v1 = 0.0, v2 = 0.0, v3 = 0.0;
			for(int k = 0; k < aR.size(); k++){
				v1 += (aR.get(k) - (totalAR/NUM_REPS)) * (aR.get(k) - (totalAR/NUM_REPS));
				v2 += (nS.get(k) - (totalNode/NUM_REPS)) * (nS.get(k) - (totalNode/NUM_REPS));
				v3 += (lS.get(k) - (totalLink/NUM_REPS)) * (lS.get(k) - (totalLink/NUM_REPS));
			}
			
			acceptanceRateStd.add(Math.sqrt(v1/NUM_REPS));
			nodeStressStd.add(Math.sqrt(v2/NUM_REPS));
			linkStressStd.add(Math.sqrt(v3/NUM_REPS));
		}
		
		DecimalFormat df = new DecimalFormat("#.#####");
		
		FileWriter fWriter1 = new FileWriter("./finalResults/"+secDep+"_"+reconfig+"_"+nodes+"sub_AcceptanceRate.dat");
		BufferedWriter writer1 = new BufferedWriter(fWriter1);
		
		//Heuristic Medium StdDev
		writer1.write("PARTIAL_RANDOM    " + df.format(acceptanceRate.get(0)) + "    " + df.format(acceptanceRateStd.get(0)) + "\n");
		writer1.write("FULL_RANDOM       " + df.format(acceptanceRate.get(1)) + "    " + df.format(acceptanceRateStd.get(1)) + "\n");
		writer1.write("CPU_VALUE         " + df.format(acceptanceRate.get(2)) + "    " + df.format(acceptanceRateStd.get(2)) + "\n");
		writer1.write("BW_VALUE          " + df.format(acceptanceRate.get(3)) + "    " + df.format(acceptanceRateStd.get(3)) + "\n");
		writer1.write("UTILITY           " + df.format(acceptanceRate.get(4)) + "    " + df.format(acceptanceRateStd.get(4)) + "\n");
		writer1.write("FULL_GREEDY       " + df.format(acceptanceRate.get(5)) + "    " + df.format(acceptanceRateStd.get(5)) + "\n");
		writer1.write("D-VINE            " + df.format(acceptanceRate.get(6)) + "    " + df.format(acceptanceRateStd.get(6)) + "\n");
		
		writer1.close();
		
		FileWriter fWriter2 = new FileWriter("./finalResults/"+secDep+"_"+reconfig+"_"+nodes+"sub_NodeStress.dat");
		BufferedWriter writer2 = new BufferedWriter(fWriter2);
		
		//Heuristic Medium StdDev
		writer2.write("PARTIAL_RANDOM    " + df.format(nodeStress.get(0)) + "    " + df.format(nodeStressStd.get(0)) + "\n");
		writer2.write("FULL_RANDOM       " + df.format(nodeStress.get(1)) + "    " + df.format(nodeStressStd.get(1)) + "\n");
		writer2.write("CPU_VALUE         " + df.format(nodeStress.get(2)) + "    " + df.format(nodeStressStd.get(2)) + "\n");
		writer2.write("BW_VALUE          " + df.format(nodeStress.get(3)) + "    " + df.format(nodeStressStd.get(3)) + "\n");
		writer2.write("UTILITY           " + df.format(nodeStress.get(4)) + "    " + df.format(nodeStressStd.get(4)) + "\n");
		writer2.write("FULL_GREEDY       " + df.format(nodeStress.get(5)) + "    " + df.format(nodeStressStd.get(5)) + "\n");
		writer2.write("D-VINE            " + df.format(nodeStress.get(6)) + "    " + df.format(nodeStressStd.get(6)) + "\n");
		
		writer2.close();
		
		FileWriter fWriter3 = new FileWriter("./finalResults/"+secDep+"_"+reconfig+"_"+nodes+"sub_LinkStress.dat");
		BufferedWriter writer3 = new BufferedWriter(fWriter3);
		
		//Heuristic Medium StdDev
		writer3.write("PARTIAL_RANDOM    " + df.format(linkStress.get(0)) + "    " + df.format(linkStressStd.get(0)) + "\n");
		writer3.write("FULL_RANDOM       " + df.format(linkStress.get(1)) + "    " + df.format(linkStressStd.get(1)) + "\n");
		writer3.write("CPU_VALUE         " + df.format(linkStress.get(2)) + "    " + df.format(linkStressStd.get(2)) + "\n");
		writer3.write("BW_VALUE          " + df.format(linkStress.get(3)) + "    " + df.format(linkStressStd.get(3)) + "\n");
		writer3.write("UTILITY           " + df.format(linkStress.get(4)) + "    " + df.format(linkStressStd.get(4)) + "\n");
		writer3.write("FULL_GREEDY       " + df.format(linkStress.get(5)) + "    " + df.format(linkStressStd.get(5)) + "\n");
		writer3.write("D-VINE            " + df.format(linkStress.get(6)) + "    " + df.format(linkStressStd.get(6)) + "\n");
		
		writer3.close();
	}

}
