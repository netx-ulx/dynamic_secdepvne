package pt.SecDepVNE.Substrate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import pt.SecDepVNE.Common.Pair;
import pt.SecDepVNE.Common.ResourceGenerator;
import pt.SecDepVNE.Common.Utils;

/**
 * Handles the creation of substrate networks
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class SubstrateCreator {

	private ResourceGenerator resGen;
	private Random rand;

	public SubstrateCreator() {
		this.resGen = new ResourceGenerator();
		this.rand = new Random();
	}

	/**
	 * Generate a specs file with random network info and returns a substrate network
	 * @param file File name
	 * @param nNodes Number of nodes in the network
	 * @param scale Grid scale
	 * @param nClouds Number of clouds
	 * @return A SubstrateNetwork
	 */
	public SubstrateNetwork generateRandomSubstrateNetwork(String file, int nNodes, int nClouds) {
		// double factor = rand.nextDouble() * (0.3 - 0.1) + 0.1;
		// Configured with each pair of substrate nodes randomly connected with probability 50% of probability (The same as D-Vine and FullGreed). There is a bug in the D-Vine paper because it says that the prob is 0.5
		double alpha = 0.5;
		double beta = 0.5;
		
		try {
			FileWriter fileWriter = new FileWriter("./gt-itm/graphs/input_specs/"+file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("geo 1");
			bufferedWriter.newLine();
			
//			Waxman random graph
			bufferedWriter.write(nNodes + " 10 2 "+alpha+" "+beta);
			
//			Random substrate topology
//			bufferedWriter.write(nNodes + " 25 3 "+alpha);
			bufferedWriter.newLine();

			bufferedWriter.close();
		}
		catch(IOException ex) {
			System.out.println("Error writing to file '"+ file +"'");
			System.out.println("Exiting...");
			ex.printStackTrace();
			System.exit(0);
		}
		try {
			Thread.sleep(10);
			Utils.generateAltFiles();
			Thread.sleep(10);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return generateSubstrateNetwork("./gt-itm/graphs/alt_files/random/"+file+".alt", nClouds);
	}

	/**
	 * Generates a substrate network with random attributes
	 * @param altFile The input file with the network topology
	 * @param nClouds Number of clouds
	 * @return Substrate network
	 */
	public SubstrateNetwork generateSubstrateNetwork(String altFile, int nClouds) {
		SubstrateNetwork subNet = new SubstrateNetwork(nClouds);

		String line = null;
		String[] parts = null;

		try {
			File file = new File(altFile);
			file.createNewFile();
			FileReader fileReader = new FileReader(altFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// Eat the first line from <fileName>
			bufferedReader.readLine();

			// Eat number of nodes and edges
			bufferedReader.readLine();

			bufferedReader.readLine();
			bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				subNet.addNode(Utils.convertToAlphabet(parts[1]));
			}

			ArrayList<String> nodes = subNet.getNodes();

			for(String node: nodes){
				subNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(100)));
				subNet.addNodeSec(resGen.randomWithProbability());
			}

			bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				subNet.addEdge(new Pair<String>(Utils.convertToAlphabet(parts[0]), Utils.convertToAlphabet(parts[1])));
			}
			
			if(nClouds == 3){
				subNet.addCloudSecurity(1);
				subNet.addCloudSecurity(1.2);
				subNet.addCloudSecurity(5.0);
			}else
				for(int i = 0; i < nClouds; i++)
					subNet.addCloudSecurity(resGen.generateWeightedSecurity(3));
			
			subNet.fillDoesItBelong();
			
			for(Pair<String> edge: subNet.getEdges()){
				subNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(100)));
				subNet.addEdgeSec(resGen.randomWithProbability());
				subNet.addEdgeWeight(1);
				int a = subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getLeft()));
				int b = subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getRight()));
				if(subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getLeft())) ==
						subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getRight()))){
					subNet.addEdgeLatency(1);
				}else{
						subNet.addEdgeLatency(20);
				}
			}

			bufferedReader.close();

		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + altFile + "'");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + altFile + "'");                  
			ex.printStackTrace();
			System.exit(0);
		}

		return subNet;
	}
	
	public SubstrateNetwork generateSubstrateNoSec(String altFile, int nClouds){
		SubstrateNetwork subNet = new SubstrateNetwork(nClouds);

		String line = null;
		String[] parts = null;

		try {
			File file = new File(altFile);
			file.createNewFile();
			FileReader fileReader = new FileReader(altFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// Eat the first line from <fileName>
			bufferedReader.readLine();

			// Eat number of nodes and edges
			bufferedReader.readLine();

			bufferedReader.readLine();
			bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				subNet.addNode(Utils.convertToAlphabet(parts[1]));
			}

			ArrayList<String> nodes = subNet.getNodes();

			for(String node: nodes){
				subNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(100)));
				subNet.addNodeSec(1);
			}

			bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				subNet.addEdge(new Pair<String>(Utils.convertToAlphabet(parts[0]), Utils.convertToAlphabet(parts[1])));
			}
			
			if(nClouds == 3){
				subNet.addCloudSecurity(1);
				subNet.addCloudSecurity(1.1);
				subNet.addCloudSecurity(1.2);
			}else
				for(int i = 0; i < nClouds; i++)
					subNet.addCloudSecurity(resGen.generateWeightedSecurity(3));
			
			subNet.fillDoesItBelong();
			
			for(Pair<String> edge: subNet.getEdges()){
				subNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(100)));
				subNet.addEdgeSec(resGen.randomWithProbability());
				subNet.addEdgeWeight(1);
				int a = subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getLeft()));
				int b = subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getRight()));
				if(subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getLeft())) == subNet.getNodesCloudIndex(Utils.convertFromAlphabet(edge.getRight()))){
					subNet.addEdgeLatency(1);
				}else{
						subNet.addEdgeLatency(20);
				}
			}

			bufferedReader.close();

		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + altFile + "'");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + altFile + "'");                  
			ex.printStackTrace();
			System.exit(0);
		}

		return subNet;
	}
	
	/**
	 * Generates a substrate network from a file
	 * @param staticFile File with the info of a substrate network
	 * @return Substrate network
	 */
	public SubstrateNetwork generateSubstrateNetwork(String staticFile) {
		SubstrateNetwork subNet = new SubstrateNetwork();
		
		int nNodes, nEdges, nClouds;
		String[] parts;
		
		try {
			FileReader fileReader = new FileReader(staticFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			nNodes = Integer.parseInt(bufferedReader.readLine());
			nEdges = Integer.parseInt(bufferedReader.readLine());
			
			bufferedReader.readLine(); //NODES
			
			for(int i = 0; i < nNodes; i++)
				subNet.addNode(bufferedReader.readLine());
			
			bufferedReader.readLine(); //NODESCPU
			
			for(int i = 0; i < nNodes; i++)
				subNet.addNodeCPU(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //NODESSEC
			
			for(int i = 0; i < nNodes; i++)
				subNet.addNodeSec(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGES

			for(int i = 0; i < nEdges; i++){
				parts = bufferedReader.readLine().split(" ");
				subNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}
			
			bufferedReader.readLine(); //EDGESBW
			
			for(int i = 0; i < nEdges; i++)
				subNet.addEdgeBw(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGESLATENCY
			
			for(int i = 0; i < nEdges; i++)
				subNet.addEdgeLatency(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGESSEC

			for(int i = 0; i < nEdges; i++)
				subNet.addEdgeSec(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGESWEIGHT

			for(int i = 0; i < nEdges; i++)
				subNet.addEdgeWeight(Integer.parseInt(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //NCLOUDS
			nClouds = Integer.parseInt(bufferedReader.readLine());
			subNet.addNClouds(nClouds);
			
			bufferedReader.readLine(); //CLOUDSSEC

			for(int i = 0; i < nClouds; i++){
				subNet.addCloudSecurity(Double.parseDouble(bufferedReader.readLine()));
			}
			
			bufferedReader.readLine(); //DOESITBELONG
			
			int[][] doesItBelong = new int[nClouds][nNodes];
			
			for(int i = 0; i < nClouds; i++){
				for(int j = 0; j < nNodes; j++){
					doesItBelong[i][j] = Integer.parseInt(bufferedReader.readLine());
				}
			}
			
			subNet.setDoesItBelong(doesItBelong);
			
			subNet.cloudSecSup();
						
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return subNet;
		
	}

	public SubstrateNetwork generateSubNet(String file, int nNodes, int nClouds, int type){
		//Configured with each pair of substrate nodes randomly connected with probability 0.05% of probalitity (The same as D-Vine and FullGreed).
		//There is a bug in the D-Vine paper because it says that the prob is 0.5
		
		double alpha;
		double beta;
		
		try {
			FileWriter fileWriter = new FileWriter("./gt-itm/graphs/input_specs/"+file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			// 1 stands for the number of graphs desired
			bufferedWriter.write("geo 1");
			bufferedWriter.newLine();
			
			switch (type) {
			case 0:
				// Waxman parameters
				// input: numNodes + scale + method (Waxman RG2) + alpha + beta
				alpha = (rand.nextDouble() * 0.2) + 0.4;
				beta = (rand.nextDouble() * 0.2) + 0.1;
				
				bufferedWriter.write(nNodes + " 25 2 "+alpha+" "+beta);
				break;
			case 1:
				// Random substrate topology (factor = probability of connection between nodes)
				// input: numNodes + scale + method (pure random) + alpha (probability)
				alpha = (rand.nextDouble() * 0.2) + 0.1; // 0.1 to 0.3
				
				bufferedWriter.write(nNodes + " 25 3 "+alpha);
				break;
			default:
				break;
			}
			bufferedWriter.newLine();

			bufferedWriter.close();
		}
		catch(IOException ex) {
			System.out.println("Error writing to file '"+ file +"'");
			System.out.println("Exiting...");
			ex.printStackTrace();
			System.exit(0);
		}
		try {
			Thread.sleep(10);
//			Utils.generateAltFiles();
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return generateSubstrateNetwork("./gt-itm/graphs/alt_files/random/"+file+".alt", nClouds);
	}
	
}
