package pt.SecDepVNE.Glpk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import pt.SecDepVNE.Common.Pair;
import pt.SecDepVNE.Common.Utils;
import pt.SecDepVNE.Heuristic.NodeHeuristicInfo;
import pt.SecDepVNE.Substrate.SubstrateNetwork;
import pt.SecDepVNE.Virtual.VirtualNetwork;

/**
 * Handles the interpretation of the output files that result from
 * the execution of the formulations
 * @authors Luis Ferrolho, fc41914, Max Alaluna fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class OutputFileReader {

	private double backupResources;
	private String executionTime = "0";
	private boolean wasAccepted;

	private ArrayList<Pair<String>> wEdgesUsed;
	private ArrayList<Pair<String>> wMappedEdges;

	private ArrayList<Pair<String>> bEdgesUsed;
	private ArrayList<Pair<String>> bMappedEdges;

	private ArrayList<String> wNodesUsed;
	private ArrayList<String> wMappedNodes;

	private ArrayList<String> bNodesUsed;
	private ArrayList<String> bMappedNodes;

	////////////////////////////////////////////////////////////

	private ArrayList<Pair<String>> edgesUsed;
	private ArrayList<Pair<String>> mappedEdges;

	private ArrayList<String> nodesUsed;
	private ArrayList<String> mappedNodes;

	private ArrayList<Double> bwEdgesUsed;
	
	private long nodeWMappingTime = 0;
	private long linkWMappingTime = 0;
	private long nodeBMappingTime = 0;
	private long linkBMappingTime = 0;
	private boolean timeout = false;
	

	public OutputFileReader() {
		this.wEdgesUsed = new ArrayList<>();
		this.wNodesUsed = new ArrayList<>();

		this.wMappedEdges = new ArrayList<>();
		this.wMappedNodes = new ArrayList<>();

		this.bNodesUsed = new ArrayList<>();
		this.bMappedNodes = new ArrayList<>();

		this.bEdgesUsed = new ArrayList<>();
		this.bMappedEdges = new ArrayList<>();

		this.wasAccepted = true;

		////////////////////////////////////////////////////////

		edgesUsed = new ArrayList<>();
		mappedEdges = new ArrayList<>();

		nodesUsed = new ArrayList<>();
		mappedNodes = new ArrayList<>();
		bwEdgesUsed = new ArrayList<>();
	}

	/**
	 * Interprets the results of the execution of the formulations
	 * @param virNet Virtual network that tried the embedding
	 * @param numOfNodes Number of substrate nodes in the substrate network
	 * @param outputFile File that has to be interpreted
	 */
	public void collectAllInfo(VirtualNetwork virNet, int numOfNodes, String outputFile) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used"))
					executionTime = parts[2];
				else if(parts[1].contains("r[") && !parts[2].equalsIgnoreCase("0.000000"))
					backupResources += Double.parseDouble(parts[2]);
				else if(parts[1].contains("gama[") && !parts[2].equalsIgnoreCase("0.000000"))
					backupResources += Double.parseDouble(parts[2]);

				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000")) || 
						(parts[1].contains("fb[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					if(outputFile.contains("SecDep")){ //SECDEP

						if(parts[1].contains("fw[")){
							tmp = new Pair<>(parts2[1],parts2[2]);
							wMappedEdges.add(tmp);

							tmp = new Pair<>(parts2[3],parts2[4]);
							wEdgesUsed.add(tmp);
						}else if(parts[1].contains("fb[")){
							tmp = new Pair<>(parts2[1],parts2[2]);
							bMappedEdges.add(tmp);

							tmp = new Pair<>(parts2[3],parts2[4]);
							bEdgesUsed.add(tmp);
						}

						tmp = new Pair<>(parts2[1],parts2[2]);
						mappedEdges.add(tmp);

						tmp = new Pair<>(parts2[3],parts2[4]);
						edgesUsed.add(tmp);

					}else{ //DVINE
						parts = parts2[1].split("f");

						tmp = virNet.getEdge(Integer.parseInt(parts[1]));

						String n1 = String.valueOf(Integer.parseInt(tmp.getLeft()) + numOfNodes);
						String n2 = String.valueOf(Integer.parseInt(tmp.getRight()) + numOfNodes);

						if(!parts2[2].equals(n1) && !parts2[2].equals(n2) && !parts2[3].equals(n1) && !parts2[3].equals(n2)){
							wMappedEdges.add(tmp);
							mappedEdges.add(tmp);

							tmp = new Pair<String>(parts2[2],parts2[3]);
							wEdgesUsed.add(tmp);
							edgesUsed.add(tmp);
						}

					}

				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0")) || 
						(parts[1].contains("thetab[") && !parts[2].equalsIgnoreCase("0"))){ //CPU

					if(outputFile.contains("SecDep")){ //SECDEP

						if(parts[1].contains("thetaw[")){
							wMappedNodes.add(parts2[1]);
							wNodesUsed.add(parts2[2]);
						}else if(parts[1].contains("thetab[")){
							bMappedNodes.add(parts2[1]);
							bNodesUsed.add(parts2[2]);
						}

						mappedNodes.add(parts2[1]);
						nodesUsed.add(parts2[2]);

					}else{ //DVINE

						String opLeft = String.valueOf(Math.abs(numOfNodes - Integer.parseInt(parts2[1])));
						String opRight = String.valueOf(Math.abs(numOfNodes - Integer.parseInt(parts2[2])));

						if(virNet.getNodes().contains(opLeft) && !virNet.getNodes().contains(opRight) && Integer.parseInt(parts2[1]) >= numOfNodes){
							wMappedNodes.add(opLeft);
							wNodesUsed.add(parts2[2]);
							mappedNodes.add(opLeft);
							nodesUsed.add(parts2[2]);
						}
					}
				}
			}
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void collectAllHeuristicInfo(VirtualNetwork virNet, int numOfNodes, String outputFile, 
			SubstrateNetwork subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);
					
					if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3]));

					} else if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2]));
					}
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){
				
				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
			if(mappedBackupNodesHeu != null){

				for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
					
					bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
					mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicInfoMCF(VirtualNetwork virNet, int numOfNodes, String outputFile, 
			SubstrateNetwork subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);
					
					if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3]));

					} else if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2]));
					}
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){
				
				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
			if(mappedBackupNodesHeu != null){

				for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
					
					bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
					mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicInfoSP(VirtualNetwork virNet, int numOfNodes, SubstrateNetwork subNet, 
			ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu, 
			ArrayList<Pair<String>> finalSubstrateWorkingAndBackupEdges, ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges){

		cleanAllInfo();

		for (int i = 0; i < finalSubstrateWorkingAndBackupEdges.size(); i++){

			wEdgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			edgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));

			wMappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
			mappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
		}

		for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

			wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
		}
		
		if(mappedBackupNodesHeu != null){

			for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
				bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
			}
		}
	}
	
	public void collectAllHeuristicInfoDK(VirtualNetwork virNet, int numOfNodes, SubstrateNetwork subNet, 
			ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu, 
			ArrayList<Pair<String>> finalSubstrateWorkingAndBackupEdges, ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges){

		cleanAllInfo();

		for (int i = 0; i < finalSubstrateWorkingAndBackupEdges.size(); i++){

			wEdgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			edgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));

			wMappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
			mappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
		}

		for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

			wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
		}
		
		if(mappedBackupNodesHeu != null){

			for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
				bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
			}
		}
	}

	public void collectDVineHeuristicInfo(VirtualNetwork virNet, int numOfNodes, String outputFile, 
			SubstrateNetwork subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;
		
		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					tmp = virNet.getEdge(Integer.parseInt(parts[1]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(parts2[2],parts2[3]);
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void collectFullGreedyHeuristicInfo(VirtualNetwork virNet, int numOfNodes, String outputFile, 
			SubstrateNetwork subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					tmp = virNet.getEdge(Integer.parseInt(parts[1]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(parts2[2],parts2[3]);
					
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicWorkingInfoPartial(VirtualNetwork virNet, int numOfNodes, String outputFile, SubstrateNetwork subNet,
			ArrayList<Pair<String>> mappedNodesHeu) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;
		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(outputFile));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");
					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(subNet.getNode(Integer.parseInt(parts2[2])),subNet.getNode(Integer.parseInt(parts2[3])));

					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			executionTime = partialExecutionTime +"";

			bufferedReader.close();

			for(int i = 0; i < mappedNodesHeu.size(); i++){

				wMappedNodes.add(mappedNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedNodesHeu.get(i).getRight());
				nodesUsed.add(mappedNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicWorkingInfoPartial(ArrayList<Pair<String>> mappedWorkingNodes, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgeUsed, long partialExecutionTime) {

		cleanAllInfo();

		executionTime += partialExecutionTime;

		for(int i = 0; i < edgeUsed.size(); i++){
			wEdgesUsed.add(new Pair<String>(edgeUsed.get(i).getLeft(), edgeUsed.get(i).getRight()));
			wMappedEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));

		}

		if(mappedWorkingNodes != null){
			for(int i = 0; i < mappedWorkingNodes.size(); i++){

				wMappedNodes.add(mappedWorkingNodes.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodes.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodes.get(i).getRight());
				nodesUsed.add(mappedWorkingNodes.get(i).getLeft());
			}
		}
	}

	public void collectAllHeuristicBackupInfoPartial(VirtualNetwork virNet, int numOfNodes, String outputFile, SubstrateNetwork subNet, ArrayList<Pair<String>> mappedNodesHeu) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;

		Pair<String> tmp = null;


		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(outputFile));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					executionTime += parts[2];
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];

					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					bMappedEdges.add(tmp);
					mappedEdges.add(tmp);
					
					tmp = new Pair<String>(subNet.getNode(Integer.parseInt(parts2[2])),subNet.getNode(Integer.parseInt(parts2[3])));
					
					bEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			bufferedReader.close();

			for(int i = 0; i < mappedNodesHeu.size(); i++){
				
				bMappedNodes.add(mappedNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedNodesHeu.get(i).getRight());
				nodesUsed.add(mappedNodesHeu.get(i).getLeft());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicBackupInfoPartial(ArrayList<Pair<String>> mappedBackupNodes, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgeUsed, long partialExecutionTime) {
		cleanAllInfo();

		executionTime += partialExecutionTime;

		for(int i = 0; i < edgeUsed.size(); i++){
			bEdgesUsed.add(new Pair<String>(edgeUsed.get(i).getLeft(), edgeUsed.get(i).getRight()));
			bMappedEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));
		}

		if(mappedBackupNodes != null){

			for(int i = 0; i < mappedBackupNodes.size(); i++){

				bMappedNodes.add(mappedBackupNodes.get(i).getRight());
				bNodesUsed.add(mappedBackupNodes.get(i).getLeft());
				mappedNodes.add(mappedBackupNodes.get(i).getRight());
				nodesUsed.add(mappedBackupNodes.get(i).getLeft());
			}
		}


	}

	public HashMap<Integer, ArrayList<NodeHeuristicInfo>> populateDVineHeuristicWorkingInfo(
			VirtualNetwork virtualNetwork, int numOfNodes,
			String partialResult, SubstrateNetwork subNet) {

		cleanAllInfo();

		HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();
		ArrayList<NodeHeuristicInfo> aux;

		String line = null;
		String flow = "";
		String x = "";
		String[] parts = null, parts2 = null;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(partialResult));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted){
				parts = line.split(" +");
				
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION")){
					return null;
				}

				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){
					executionTime = parts[2];
				}

				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW
					
					flow = parts[2];
					parts = parts2[1].split("f");
					
					// 2 = substrate, 3 = virtual
					if((subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])) &&
							!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[3])))){
						if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[3]))== null){
							aux = new ArrayList<NodeHeuristicInfo>();
							aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[2]), Integer.parseInt(parts[1]), 
									-1.0, Double.parseDouble(flow)));
							
							virtualNodeIndexArraySubstrateNodeInfoMap.put(Integer.parseInt(parts2[3]), aux);
						} else {
							aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[3]));
							aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[2]), Integer.parseInt(parts[1]),
									-1.0, Double.parseDouble(flow)));
						}
					} else {
						// 2 = virtual, 3 = substrate
						if((!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])) && 
								subNet.getNodes().contains(Utils.convertToAlphabet(parts2[3])))){
							if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]))== null){
								aux = new ArrayList<NodeHeuristicInfo>();
								aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[3]), Integer.parseInt(parts[1]),
										-1.0, Double.parseDouble(flow)));
								virtualNodeIndexArraySubstrateNodeInfoMap.put(Integer.parseInt(parts2[2]), aux);
							}else{
								aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]));
								aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[3]), Integer.parseInt(parts[1]), 
										-1.0, Double.parseDouble(flow)));
							}
						}
					}
				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0.000000"))){
					x = parts[2];
					if((subNet.getNodes().contains(Utils.convertToAlphabet(parts2[1])) && 
							!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])))){
						
						if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]))!= null){
							aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]));
							for(int i = 0; i < aux.size() ; i++)
								if(aux.get(i).getIndexNode() == Integer.parseInt(parts2[1]))
									aux.get(i).setX(Double.parseDouble(x));
								
						}else{
							if((!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[1])) &&
									subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])))){

								if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[1]))!= null){
									aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[1]));
									for(int i = 0; i < aux.size() ; i++)
										if(aux.get(i).getIndexNode() == Integer.parseInt(parts2[2]))
											aux.get(i).setX(Double.parseDouble(x));
									
								}
							}
						}
					}
				}
			}
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return virtualNodeIndexArraySubstrateNodeInfoMap;
	}

	public String populateDVineHeuristicWorkingInfoTest(
			VirtualNetwork virtualNetwork, int numOfNodes,
			String partialResult, SubstrateNetwork subNet) {

		cleanAllInfo();

		String line = null;
		String linesf = "";
		String linesx = "";
		String[] parts = null, parts2 = null;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(partialResult));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION")){
					return null;
				}

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					executionTime = parts[2];
				}

				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					linesf += line+"\n";
					parts = parts2[1].split("f");

				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //CPU

					linesx += line+"\n";
				}
			}

			bufferedReader.close();

			return linesf+"\n"+linesx;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Empty all attributes
	 */
	public void cleanAllInfo() {
		wasAccepted = true;
		wEdgesUsed.clear();
		wMappedEdges.clear();
		bEdgesUsed.clear();
		bMappedEdges.clear();
		wNodesUsed.clear();
		wMappedNodes.clear();
		bNodesUsed.clear();
		bMappedNodes.clear();
		backupResources = 0;
		mappedEdges.clear();
		edgesUsed.clear();
		mappedNodes.clear();
		nodesUsed.clear();
		bwEdgesUsed.clear();
	}

	public double getBackupResourcesQuantity() {
		return backupResources;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String time) {
		this.executionTime = time;
	}

	public boolean wasAccepted() {
		return wasAccepted;
	}

	public void setWasAccepted(boolean wasAccepted) {
		this.wasAccepted = wasAccepted;
	}

	public ArrayList<Pair<String>> getwEdgesUsed() {
		return wEdgesUsed;
	}

	public ArrayList<Pair<String>> getwMappedEdges() {
		return wMappedEdges;
	}

	public ArrayList<Pair<String>> getEdgesUsed() {
		return edgesUsed;
	}

	public ArrayList<Pair<String>> getMappedEdges() {
		return mappedEdges;
	}

	public ArrayList<Pair<String>> getbEdgesUsed() {
		return bEdgesUsed;
	}

	public ArrayList<Pair<String>> getbMappedEdges() {
		return bMappedEdges;
	}

	public ArrayList<String> getwNodesUsed() {
		return wNodesUsed;
	}

	public ArrayList<String> getwMappedNodes() {
		return wMappedNodes;
	}

	public ArrayList<Double> getBwEdgesUsed() {
		return bwEdgesUsed;
	}

	public void setwMappedNodes(ArrayList<String> wMappedNodes) {
		this.wMappedNodes = wMappedNodes;
	}

	public ArrayList<String> getNodesUsed() {
		return nodesUsed;
	}

	public ArrayList<String> getMappedNodes() {
		return mappedNodes;
	}

	public ArrayList<String> getbNodesUsed() {
		return bNodesUsed;
	}

	public ArrayList<String> getbMappedNodes() {
		return bMappedNodes;
	}

	public void setEdgesUsed(ArrayList<Pair<String>> edgesUsed) {

		for(int i = 0; i < edgesUsed.size(); i++)
			this.edgesUsed.add(new Pair<String>(edgesUsed.get(i).getLeft(), 
					edgesUsed.get(i).getRight()));
	}

	public void setMappedEdges(ArrayList<Pair<String>> mappedEdges) {

		for(int i = 0; i < edgesUsed.size(); i++)
			this.mappedEdges.add(new Pair<String>(mappedEdges.get(i).getLeft(), 
					mappedEdges.get(i).getRight()));
	}

	public long getNodeWMappingTime() {
		return nodeWMappingTime;
	}

	public void setNodeWMappingTime(long nodeMappingTime) {
		this.nodeWMappingTime = nodeMappingTime;
	}

	public long getLinkWMappingTime() {
		return linkWMappingTime;
	}

	public void setLinkWMappingTime(long linkMappingTime) {
		if(linkMappingTime >= 0)
			this.linkWMappingTime = linkMappingTime;
		else
			this.linkWMappingTime = 0;
	}

	public long getNodeBMappingTime() {
		return nodeBMappingTime;
	}

	public void setNodeBMappingTime(long nodeBMappingTime) {
		this.nodeBMappingTime = nodeBMappingTime;
	}

	public long getLinkBMappingTime() {
		return linkBMappingTime;
	}

	public void setLinkBMappingTime(long linkBMappingTime) {
		this.linkBMappingTime = linkBMappingTime;
	}

	public boolean isTimeout() {
		return timeout;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}
}
