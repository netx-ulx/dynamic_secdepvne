package pt.SecDepVNE.Charts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import pt.SecDepVNE.Main.EventType;

/**
 * Class responsible for the creation of the .dat files of all experiments
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class DatCreator {

	public static void main(String[] args) {

		int nNodes = Integer.parseInt(args[0]); // Number of substrate nodes
		
		String input1 = "./statistics/Heuristic/experience1/output_s" + nNodes + ".txt";
		String input2 = "./statistics/Dynamic/experience2/output_s" + nNodes + ".txt";
		String input3 = "./statistics/Dynamic/experience3/output_s" + nNodes + ".txt";
		String input4 = "./statistics/Dynamic/experience4/output_s" + nNodes + ".txt";
		String input5 = "./statistics/Heuristic/experience5/output_s" + nNodes + ".txt";
		String input6 = "./statistics/Dynamic/experience6/output_s" + nNodes + ".txt";
		String input7 = "./statistics/Dynamic/experience7/output_s" + nNodes + ".txt";
		String input8 = "./statistics/Dynamic/experience8/output_s" + nNodes + ".txt";
		String input9 = "./statistics/Heuristic/experience9/output_s" + nNodes + ".txt";
		String input10 = "./statistics/Dynamic/experience10/output_s" + nNodes + ".txt";
		String input11 = "./statistics/Dynamic/experience11/output_s" + nNodes + ".txt";
		String input12 = "./statistics/Dynamic/experience12/output_s" + nNodes + ".txt";
		String input13 = "./statistics/Heuristic/experience13/output_s" + nNodes + ".txt";
		String input14 = "./statistics/Dynamic/experience14/output_s" + nNodes + ".txt";
		String input15 = "./statistics/Dynamic/experience15/output_s" + nNodes + ".txt";
		String input16 = "./statistics/Dynamic/experience16/output_s" + nNodes + ".txt";
		String input17 = "./statistics/Heuristic/experience17/output_s" + nNodes + ".txt";
		String input18 = "./statistics/Dynamic/experience18/output_s" + nNodes + ".txt";
		String input19 = "./statistics/Dynamic/experience19/output_s" + nNodes + ".txt";
		String input20 = "./statistics/Dynamic/experience20/output_s" + nNodes + ".txt";
		String input21 = "./statistics/Heuristic/experience21/output_s" + nNodes + ".txt";
		String input22 = "./statistics/Dynamic/experience22/output_s" + nNodes + ".txt";
		String input23 = "./statistics/Dynamic/experience23/output_s" + nNodes + ".txt";
		String input24 = "./statistics/Dynamic/experience24/output_s" + nNodes + ".txt";
//		String input25 = "./statistics/Heuristic/experience25/output_s" + nNodes + ".txt";
//		String input26 = "./statistics/Heuristic/experience26/output_s" + nNodes + ".txt";
//		String input27 = "./statistics/Heuristic/experience27/output_s" + nNodes + ".txt";
//		String input28 = "./statistics/Heuristic/experience28/output_s" + nNodes + ".txt";
//		String input29 = "./statistics/Heuristic/experience29/output_s" + nNodes + ".txt";
//		String input30 = "./statistics/Heuristic/experience30/output_s" + nNodes + ".txt";
//		String input31 = "./statistics/Heuristic/experience31/output_s" + nNodes + ".txt";
//		String input32 = "./statistics/Heuristic/experience32/output_s" + nNodes + ".txt";
//		String input33 = "./statistics/Heuristic/experience33/output_s" + nNodes + ".txt";
//		String input34 = "./statistics/Heuristic/experience34/output_s" + nNodes + ".txt";
//		String input35 = "./statistics/Heuristic/experience35/output_s" + nNodes + ".txt";
//		String input36 = "./statistics/Heuristic/experience36/output_s" + nNodes + ".txt";
//		String input37 = "./statistics/Heuristic/experience37/output_s" + nNodes + ".txt";
//		String input38 = "./statistics/Heuristic/experience38/output_s" + nNodes + ".txt";
//		String input39 = "./statistics/Heuristic/experience39/output_s" + nNodes + ".txt";
//		String input40 = "./statistics/Heuristic/experience40/output_s" + nNodes + ".txt";
//		String input41 = "./statistics/Heuristic/experience41/output_s" + nNodes + ".txt";
//		String input42 = "./statistics/Heuristic/experience42/output_s" + nNodes + ".txt";
//		String input43 = "./statistics/Heuristic/experience43/output_s" + nNodes + ".txt";
//		String input44 = "./statistics/Heuristic/experience44/output_s" + nNodes + ".txt";
//		String input45 = "./statistics/Heuristic/experience45/output_s" + nNodes + ".txt";
//		String input46 = "./statistics/Heuristic/experience46/output_s" + nNodes + ".txt";
//		String input47 = "./statistics/Heuristic/experience47/output_s" + nNodes + ".txt";
//		String input48 = "./statistics/Heuristic/experience48/output_s" + nNodes + ".txt";
//		String input49 = "./statistics/Heuristic/experience49/output_s" + nNodes + ".txt";
//		String input50 = "./statistics/Heuristic/experience50/output_s" + nNodes + ".txt";
//		String input51 = "./statistics/Heuristic/experience51/output_s" + nNodes + ".txt";
//		String input52 = "./statistics/Heuristic/experience52/output_s" + nNodes + ".txt";
//		String input53 = "./statistics/Heuristic/experience53/output_s" + nNodes + ".txt";
//		String input54 = "./statistics/Heuristic/experience54/output_s" + nNodes + ".txt";
//		String input55 = "./statistics/Heuristic/experience55/output_s" + nNodes + ".txt";
//		String input56 = "./statistics/Heuristic/experience56/output_s" + nNodes + ".txt";
//		String input57 = "./statistics/Heuristic/experience57/output_s" + nNodes + ".txt";
//		String input58 = "./statistics/Heuristic/experience58/output_s" + nNodes + ".txt";
//		String input59 = "./statistics/Heuristic/experience59/output_s" + nNodes + ".txt";
//		String input60 = "./statistics/Heuristic/experience60/output_s" + nNodes + ".txt";
//		String input61 = "./statistics/Heuristic/experience61/output_s" + nNodes + ".txt";
//		String input62 = "./statistics/Heuristic/experience62/output_s" + nNodes + ".txt";
//		String input63 = "./statistics/Heuristic/experience63/output_s" + nNodes + ".txt";
//		String input64 = "./statistics/Heuristic/experience64/output_s" + nNodes + ".txt";
//		String input65 = "./statistics/Heuristic/experience65/output_s" + nNodes + ".txt";
//		String input66 = "./statistics/Heuristic/experience66/output_s" + nNodes + ".txt";
//		String input67 = "./statistics/Heuristic/experience67/output_s" + nNodes + ".txt";
//		String input68 = "./statistics/Heuristic/experience68/output_s" + nNodes + ".txt";
//		String input69 = "./statistics/Heuristic/experience69/output_s" + nNodes + ".txt";
//		String input70 = "./statistics/Heuristic/experience70/output_s" + nNodes + ".txt";
//		String input71 = "./statistics/Heuristic/experience71/output_s" + nNodes + ".txt";
//		String input72 = "./statistics/Heuristic/experience72/output_s" + nNodes + ".txt";
//		String input73 = "./statistics/Dynamic/experience73/output_s" + nNodes + ".txt";
//		String input74 = "./statistics/Dynamic/experience74/output_s" + nNodes + ".txt";
//		String input75 = "./statistics/Dynamic/experience75/output_s" + nNodes + ".txt";
//		String input76 = "./statistics/Heuristic/experience76/output_s" + nNodes + ".txt";
//		String input77 = "./statistics/Heuristic/experience77/output_s" + nNodes + ".txt";
//		String input78 = "./statistics/Heuristic/experience78/output_s" + nNodes + ".txt";
//		String input79 = "./statistics/Dynamic/experience79/output_s" + nNodes + ".txt";
//		String input80 = "./statistics/Dynamic/experience80/output_s" + nNodes + ".txt";
//		String input81 = "./statistics/Dynamic/experience81/output_s" + nNodes + ".txt";
//		String input82 = "./statistics/Dynamic/experience82/output_s" + nNodes + ".txt";
//		String input83 = "./statistics/Dynamic/experience83/output_s" + nNodes + ".txt";
//		String input84 = "./statistics/Dynamic/experience84/output_s" + nNodes + ".txt";
//		String input85 = "./statistics/Dynamic/experience85/output_s" + nNodes + ".txt";
//		String input86 = "./statistics/Dynamic/experience86/output_s" + nNodes + ".txt";
//		String input87 = "./statistics/Dynamic/experience87/output_s" + nNodes + ".txt";
//		String input88 = "./statistics/Dynamic/experience88/output_s" + nNodes + ".txt";
//		String input89 = "./statistics/Dynamic/experience89/output_s" + nNodes + ".txt";
//		String input90 = "./statistics/Dynamic/experience90/output_s" + nNodes + ".txt";
		
		//--------------------------------------------------
		
		String output1 = "./plots/DynamicExp1.dat";
		String output2 = "./plots/DynamicExp2.dat";
		String output3 = "./plots/DynamicExp3.dat";
		String output4 = "./plots/DynamicExp4.dat";
		String output5 = "./plots/DynamicExp5.dat";
		String output6 = "./plots/DynamicExp6.dat";
		String output7 = "./plots/DynamicExp7.dat";
		String output8 = "./plots/DynamicExp8.dat";
		String output9 = "./plots/DynamicExp9.dat";
		String output10 = "./plots/DynamicExp10.dat";
		String output11 = "./plots/DynamicExp11.dat";
		String output12 = "./plots/DynamicExp12.dat";
		String output13 = "./plots/DynamicExp13.dat";
		String output14 = "./plots/DynamicExp14.dat";
		String output15 = "./plots/DynamicExp15.dat";
		String output16 = "./plots/DynamicExp16.dat";
		String output17 = "./plots/DynamicExp17.dat";
		String output18 = "./plots/DynamicExp18.dat";
		String output19 = "./plots/DynamicExp19.dat";
		String output20 = "./plots/DynamicExp20.dat";
		String output21 = "./plots/DynamicExp21.dat";
		String output22 = "./plots/DynamicExp22.dat";
		String output23 = "./plots/DynamicExp23.dat";
		String output24 = "./plots/DynamicExp24.dat";
//		String output25 = "./plots/DynamicExp25.dat";
//		String output26 = "./plots/DynamicExp26.dat";
//		String output27 = "./plots/DynamicExp27.dat";
//		String output28 = "./plots/DynamicExp28.dat";
//		String output29 = "./plots/DynamicExp29.dat";
//		String output30 = "./plots/DynamicExp30.dat";
//		String output31 = "./plots/DynamicExp31.dat";
//		String output32 = "./plots/DynamicExp32.dat";
//		String output33 = "./plots/DynamicExp33.dat";
//		String output34 = "./plots/DynamicExp34.dat";
//		String output35 = "./plots/DynamicExp35.dat";
//		String output36 = "./plots/DynamicExp36.dat";
//		String output37 = "./plots/DynamicExp37.dat";
//		String output38 = "./plots/DynamicExp38.dat";
//		String output39 = "./plots/DynamicExp39.dat";
//		String output40 = "./plots/DynamicExp40.dat";
//		String output41 = "./plots/DynamicExp41.dat";
//		String output42 = "./plots/DynamicExp42.dat";
//		String output43 = "./plots/DynamicExp43.dat";
//		String output44 = "./plots/DynamicExp44.dat";
//		String output45 = "./plots/DynamicExp45.dat";
//		String output46 = "./plots/DynamicExp46.dat";
//		String output47 = "./plots/DynamicExp47.dat";
//		String output48 = "./plots/DynamicExp48.dat";
//		String output49 = "./plots/DynamicExp49.dat";
//		String output50 = "./plots/DynamicExp50.dat";
//		String output51 = "./plots/DynamicExp51.dat";
//		String output52 = "./plots/DynamicExp52.dat";
//		String output53 = "./plots/DynamicExp53.dat";
//		String output54 = "./plots/DynamicExp54.dat";
//		String output55 = "./plots/DynamicExp55.dat";
//		String output56 = "./plots/DynamicExp56.dat";
//		String output57 = "./plots/DynamicExp57.dat";
//		String output58 = "./plots/DynamicExp58.dat";
//		String output59 = "./plots/DynamicExp59.dat";
//		String output60 = "./plots/DynamicExp60.dat";
//		String output61 = "./plots/DynamicExp61.dat";
//		String output62 = "./plots/DynamicExp62.dat";
//		String output63 = "./plots/DynamicExp63.dat";
//		String output64 = "./plots/DynamicExp64.dat";
//		String output65 = "./plots/DynamicExp65.dat";
//		String output66 = "./plots/DynamicExp66.dat";
//		String output67 = "./plots/DynamicExp67.dat";
//		String output68 = "./plots/DynamicExp68.dat";
//		String output69 = "./plots/DynamicExp69.dat";
//		String output70 = "./plots/DynamicExp70.dat";
//		String output71 = "./plots/DynamicExp71.dat";
//		String output72 = "./plots/DynamicExp72.dat";
		
		makeAll(input1, output1);
		makeAll(input2, output2);
		makeAll(input3, output3);
		makeAll(input4, output4);
		makeAll(input5, output5);
		makeAll(input6, output6);
		makeAll(input7, output7);
		makeAll(input8, output8);
		makeAll(input9, output9);
		makeAll(input10, output10);
		makeAll(input11, output11);
		makeAll(input12, output12);
		makeAll(input13, output13);
		makeAll(input14, output14);
		makeAll(input15, output15);
		makeAll(input16, output16);
		makeAll(input17, output17);
		makeAll(input18, output18);
		makeAll(input19, output19);
		makeAll(input20, output20);
		makeAll(input21, output21);
		makeAll(input22, output22);
		makeAll(input23, output23);
		makeAll(input24, output24);
//		makeAll(input25, output25);
//		makeAll(input26, output26);
//		makeAll(input27, output27);
//		makeAll(input28, output28);
//		makeAll(input29, output29);
//		makeAll(input30, output30);
//		makeAll(input31, output31);
//		makeAll(input32, output32);
//		makeAll(input33, output33);
//		makeAll(input34, output34);
//		makeAll(input35, output35);
//		makeAll(input36, output36);
//		makeAll(input37, output37);
//		makeAll(input38, output38);
//		makeAll(input39, output39);
//		makeAll(input40, output40);
//		makeAll(input41, output41);
//		makeAll(input42, output42);
//		makeAll(input43, output43);
//		makeAll(input44, output44);
//		makeAll(input45, output45);
//		makeAll(input46, output46);
//		makeAll(input47, output47);
//		makeAll(input48, output48);
//		makeAll(input49, output49);
//		makeAll(input50, output50);
//		makeAll(input51, output51);
//		makeAll(input52, output52);
//		makeAll(input53, output53);
//		makeAll(input54, output54);
//		makeAll(input55, output55);
//		makeAll(input56, output56);
//		makeAll(input57, output57);
//		makeAll(input58, output58);
//		makeAll(input59, output59);
//		makeAll(input60, output60);
//		makeAll(input61, output61);
//		makeAll(input62, output62);
//		makeAll(input63, output63);
//		makeAll(input64, output64);
//		makeAll(input65, output65);
//		makeAll(input66, output66);
//		makeAll(input67, output67);
//		makeAll(input68, output68);
//		makeAll(input69, output69);
//		makeAll(input70, output70);
//		makeAll(input71, output71);
//		makeAll(input72, output72);
		
	}

	/**
	 * Creates a .dat file to generate the plots after 
	 * @param inputFile Read a file with all the info of an experiment
	 * @param outputFile Write a file with the info structured to plot 
	 */
	public static void makeAll(String inputFile, String outputFile) {
		
		// We need plots for: acceptance rate, revenue, cost, node stress, link stress, node fail, link fail
		// node execution time, and link execution time.
		
		// Index (0), Time (1), Duration (2), EventType (3), Accepted (4), Revenue (5), Cost (6), AvgNodeStress (7),
		// AvgLinkStress (8), MipExecTime (9), wNodeMapTime (10), wLinkMapTime (11), bNodeMapTime (12), bLinkMapTime (13), Timeout (14)
		
		String[] parts;
		String line;
		
		int time = 0;
		EventType event; // Arrival or depart
		boolean accepted; // Accepted or not
		double rev = 0, cost = 0, avgNU = 0, avgLU = 0; // revenue, cost, nodeUtilization, linkUtilization
		int acCount = 0, total_ac = 0; // Total accepted
		int index = 0;

		double tRev = 0, tCost = 0, tAvgNode = 0, tAvgLink = 0;
		
		long nodeWMappingTime = 0;
		long linkWMappingTime = 0;
		long nodeBMappingTime = 0;
		long linkBMappingTime = 0;
		double tNodeWMappingTime = 0, tLinkWMappingTime = 0, tNodeBMappingTime = 0, tLinkBMappingTime = 0;  

		try {
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader reader = new BufferedReader(fileReader);

			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			
			reader.readLine(); //Read first line
			
			while( (line = reader.readLine()) != null){
				
				if(line.contains("Acceptance Rate"))
					break;
				
				parts = line.split(" +");
			
				index = Integer.parseInt(parts[0]);
				time = Integer.parseInt(parts[1]);
				event = parts[3].contains("ARRIVE") ? EventType.ARRIVE : EventType.DEPART;
				accepted = parts[4].contains("true") ? true: false;
				rev = Double.parseDouble(parts[5]);
				cost = Double.parseDouble(parts[6]);
				avgNU = Double.parseDouble(parts[7]);
				avgLU = Double.parseDouble(parts[8]);
				nodeWMappingTime = Long.parseLong(parts[10]);
				linkWMappingTime = Long.parseLong(parts[11]);
				nodeBMappingTime = Long.parseLong(parts[12]);
				linkBMappingTime = Long.parseLong(parts[13]);
				
				if(event == EventType.ARRIVE){
					if(accepted){
						
						acCount += 1;
						total_ac++;
						
						tRev += rev;
						tCost += cost;
						tAvgNode += avgNU;
						tAvgLink += avgLU;
						
						tNodeWMappingTime += nodeWMappingTime;
						tLinkWMappingTime += linkWMappingTime;
						tNodeBMappingTime += nodeBMappingTime;
						tLinkBMappingTime += linkBMappingTime;  
						
						
						// Write the info
						writer.write(time+" "+String.format(Locale.ENGLISH,"%10.4f",tRev/(double)time)+" "+String.format(Locale.ENGLISH,"%10.4f",tCost/(double)acCount)+
								" "+String.format(Locale.ENGLISH,"%10.4f",tAvgNode/(double)acCount)+" "+String.format(Locale.ENGLISH,"%10.4f",tAvgLink/(double)acCount)+
								" "+String.format(Locale.ENGLISH,"%10.4f",(double)total_ac/(double)(index+1))+" "+String.format(Locale.ENGLISH,"%10.4f",tRev/(double)acCount)+
								" "+String.format(Locale.ENGLISH,"%10.4f",(double)tNodeWMappingTime/(double)acCount)+" "+String.format(Locale.ENGLISH,"%10.4f",tLinkWMappingTime/(double)acCount)+
								" "+String.format(Locale.ENGLISH,"%10.4f",(double)tNodeBMappingTime/(double)acCount)+" "+String.format(Locale.ENGLISH,"%10.4f",tLinkBMappingTime/(double)acCount)+"\n");
					}
				}
			}
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
