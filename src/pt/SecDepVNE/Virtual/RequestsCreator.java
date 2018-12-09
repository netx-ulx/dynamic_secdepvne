package pt.SecDepVNE.Virtual;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
 * Handles the creation of virtual network requests
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class RequestsCreator {

	private ResourceGenerator resGen;
	private Random rand;

	public RequestsCreator() {
		this.resGen = new ResourceGenerator();
		this.rand = new Random();
	}

	/**
	 * Generate a specs file with random networks info and returns nRequests requests (virtual networks)
	 * @param file Name of the file
	 * @param nRequests Number of requests
	 * @param scale Grid scale
	 * @param maxSecAllowed Max cloud security level
	 */
	public void generateRandomVirtualNetwork(String file, int nSubNodes) {
		
		int nNodes;
		
		switch (nSubNodes) {
		case 50:
			nNodes = rand.nextInt(4) + 2; // 2 to 5
			break;
		case 100:
			nNodes = rand.nextInt(9) + 2; // 2 to 10
			break;
		case 200:
			nNodes = rand.nextInt(19) + 2; // 2 to 20
			break;
		default:
			nNodes = rand.nextInt(9) + 2; // 2 to 10
			break;
		}
		
		// 0.1 to 0.3
		double factor = (rand.nextDouble() * 0.2) + 0.1;
		double beta = 0.3;
		
		try {

			FileWriter fileWriter = new FileWriter("./gt-itm/graphs/input_specs/"+file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("geo 1");
			bufferedWriter.newLine();
//			bufferedWriter.write(nNodes + " 10 3 "+factor); 				//Full random generator
			bufferedWriter.write(nNodes + " 10 2 "+factor+" "+beta);		//Waxman generator
			bufferedWriter.newLine();

			bufferedWriter.close();
		}
		catch(IOException ex) {
			System.out.println("Error writing to file '"+ file +"'");
			System.out.println("Exiting...");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Create virtual networks with security and dependability requirements
	 * @param altFile 
	 * @param nClouds Number of clouds
	 * @return A virtual network with security and dependability requirements 
	 */
	public VirtualNetwork createVirtualNetwork(String altFile, int nClouds, int percentOfNoSecNodes, int percentOfMediumSecNodes) {

		String line = null;
		String[] parts = null;
		
		VirtualNetwork virtualNet = new VirtualNetwork(resGen.generateLifeTime());
		
		int nNodes = 0;
		
		try {
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
				virtualNet.addNode(parts[1]);
			}

			ArrayList<String> nodes = virtualNet.getNodes();
			nNodes = nodes.size();

			for(String node: nodes){
				virtualNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(20)));
				if (percentOfNoSecNodes == 33 && percentOfMediumSecNodes == 33){
					virtualNet.addNodeSec(resGen.generateWeightedSecurityNewSecParam(3));
				}else{
					virtualNet.addNodeSec(resGen.generateWeightedSecurityNewSecParam(percentOfNoSecNodes, percentOfMediumSecNodes));
				}
			}
			
            bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				virtualNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}

			for(Pair<String> edge: virtualNet.getEdges()){
				virtualNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(20)));
				virtualNet.addEdgeSec(resGen.generateWeightedSecurityNewSecParam(3));
				virtualNet.addEdgeLatency(10000);
			}
			
			for(int x = 0; x < nNodes; x++){
				virtualNet.addCloudSecurity(resGen.generateWeightedSecurityNewSecParam(3));
				
				if(virtualNet.getCloudSecurity(x) == 1.0 || virtualNet.getCloudSecurity(x) == 1.2){
					virtualNet.addBackupLocalization(resGen.generateSecurity(2) + 1);
				}else if(virtualNet.getCloudSecurity(x) == 5.0){
					virtualNet.addBackupLocalization(2);
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
		
		return virtualNet;
	}

	/**
	 * Create virtual networks without security and dependability requirements
	 * @param altFile 
	 * @param nClouds Number of clouds
	 * @return A virtual network without security and dependability requirements 
	 */
	public VirtualNetwork createVirtualNetworkNoReqs(String altFile, int nClouds) {

		String line = null;
		String[] parts = null;
		
		VirtualNetwork virtualNet = new VirtualNetwork(resGen.generateLifeTime());
		
		int nNodes = 0;
		
		try {
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
				virtualNet.addNode(parts[1]);
			}

			ArrayList<String> nodes = virtualNet.getNodes();
			nNodes = nodes.size();

			for(String node: nodes){
				virtualNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(20)));
				virtualNet.addNodeSec(1.0);
			}
			
            bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				virtualNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}

			for(Pair<String> edge: virtualNet.getEdges()){
				virtualNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(20)));
				virtualNet.addEdgeSec(1.0);
				virtualNet.addEdgeLatency(10000);
			}
			
			virtualNet.setWantBackup(false);
			
			for(int x = 0; x < nNodes; x++){
				virtualNet.addBackupLocalization(0);
				virtualNet.addCloudSecurity(1.0);
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
		
		return virtualNet;
	}
	
	public VirtualNetwork createVirtualNetworkDep(String altFile, int nClouds) {

		String line = null;
		String[] parts = null;
		
		VirtualNetwork virtualNet = new VirtualNetwork(resGen.generateLifeTime());
		
		int nNodes = 0;
		
		try {
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
				virtualNet.addNode(parts[1]);
			}

			ArrayList<String> nodes = virtualNet.getNodes();
			nNodes = nodes.size();

			for(String node: nodes){
				virtualNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(20)));
				virtualNet.addNodeSec(1.0);
			}
			
            bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				virtualNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}

			for(Pair<String> edge: virtualNet.getEdges()){
				virtualNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(20)));
				virtualNet.addEdgeSec(1.0);
				virtualNet.addEdgeLatency(10000);
			}
			
			virtualNet.setWantBackup(false);
			
			for(int x = 0; x < nNodes; x++){
				virtualNet.addCloudSecurity(resGen.generateWeightedSecurityNewSecParam(3));
				
				if(virtualNet.getCloudSecurity(x) == 1.0 || virtualNet.getCloudSecurity(x) == 1.2){
					int value = resGen.generateSecurity(2)+1;
					virtualNet.addBackupLocalization(value);
				} else {	//if(virtualNet.getCloudSecurity(x) == 5.0)
					virtualNet.addBackupLocalization(2);
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
		
		return virtualNet;
	}

	/**
	 * Generate a virtual network from a file pre created
	 * @param staticFile The file with the info of a virtual network
	 * @return A virtual network
	 */
	public VirtualNetwork generateVirtualNetwork(String staticFile) {
		VirtualNetwork virNet = new VirtualNetwork(0);
		
		int nNodes, nEdges, nClouds;
		String[] parts;
		
		try {
			FileReader fileReader = new FileReader(staticFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			nNodes = Integer.parseInt(bufferedReader.readLine());
			nEdges = Integer.parseInt(bufferedReader.readLine());
			
			bufferedReader.readLine(); //NODES
			
			for(int i = 0; i < nNodes; i++)
				virNet.addNode(bufferedReader.readLine());
			
			bufferedReader.readLine(); //NODESCPU
			
			for(int i = 0; i < nNodes; i++)
				virNet.addNodeCPU(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //NODESSEC
			
			for(int i = 0; i < nNodes; i++)
				virNet.addNodeSec(Integer.parseInt(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGES

			for(int i = 0; i < nEdges; i++){
				parts = bufferedReader.readLine().split(" ");
				virNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}
			
			bufferedReader.readLine(); //EDGESBW
			
			for(int i = 0; i < nEdges; i++)
				virNet.addEdgeBw(Double.parseDouble(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //EDGESSEC

			for(int i = 0; i < nEdges; i++)
				virNet.addEdgeSec(Integer.parseInt(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //NCLOUDS
			nClouds = Integer.parseInt(bufferedReader.readLine());

			bufferedReader.readLine(); //CLOUDSSEC

			for(int i = 0; i < nClouds; i++)
				virNet.addCloudSecurity(Integer.parseInt(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //BACKUPLOCALIZATION
			
			for(int i = 0; i < nNodes; i++)
				virNet.addBackupLocalization(Integer.parseInt(bufferedReader.readLine()));
			
			bufferedReader.readLine(); //DURATION
			virNet.setDuration(Integer.parseInt(bufferedReader.readLine()));
						
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return virNet;
	}

	public VirtualNetwork createVirtualNetworkNewPar(String altFile, int nClouds, int percentOfNoSecNodes, int percentOfMediumSecNodes) {

		String line = null;
		String[] parts = null;
		
		VirtualNetwork virtualNet = new VirtualNetwork(resGen.generateLifeTime());
		
		int nNodes = 0;
		
		try {
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
				virtualNet.addNode(parts[1]);
			}

			ArrayList<String> nodes = virtualNet.getNodes();
			nNodes = nodes.size();

			for(String node: nodes){
				virtualNet.addNodeCPU(Utils.roundDecimals(resGen.generateCPU(20)));
				virtualNet.addNodeSec(resGen.generateWeightedSecurityNewSecParam(percentOfNoSecNodes, percentOfMediumSecNodes));
			}
			
            bufferedReader.readLine();

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
				parts = line.split(" ");
				virtualNet.addEdge(new Pair<String>(parts[0], parts[1]));
			}

			for(Pair<String> edge: virtualNet.getEdges()){
				virtualNet.addEdgeBw(Utils.roundDecimals(resGen.generateBandwidth(20)));
				virtualNet.addEdgeSec(resGen.generateWeightedSecurityNewSecParam(percentOfNoSecNodes, percentOfMediumSecNodes));
				virtualNet.addEdgeLatency(10000);
			}
			
			for(int x = 0; x < nNodes; x++){
				virtualNet.addCloudSecurity(resGen.generateWeightedSecurityNewSecParam(percentOfNoSecNodes, percentOfMediumSecNodes));
				
				if(virtualNet.getCloudSecurity(x) == 1.0 || virtualNet.getCloudSecurity(x) == 1.2)
					virtualNet.addBackupLocalization(resGen.generateSecurity(2) + 1);
				else if(virtualNet.getCloudSecurity(x) == 5.0)
					virtualNet.addBackupLocalization(1);
				
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
		return virtualNet;
	}
	
}
