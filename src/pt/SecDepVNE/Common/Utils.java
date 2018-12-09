package pt.SecDepVNE.Common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.xml.soap.Node;

import pt.SecDepVNE.Common.KShortestPath.model.abstracts.BaseVertex;
import pt.SecDepVNE.Common.ShortestPath.Vertex;
import pt.SecDepVNE.Glpk.DVineDatFileCreator;
import pt.SecDepVNE.Glpk.OutputFileReader;
import pt.SecDepVNE.Heuristic.NodeHeuristicInfo;
import pt.SecDepVNE.Heuristic.SecLoc;
import pt.SecDepVNE.Substrate.SubstrateNetwork;
import pt.SecDepVNE.Virtual.VirtualNetwork;

/**
 * 
 * Functions that are used by the simulator in order to perform or facilitate some actions.
 * 
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 */
public class Utils {

	static Random random = new Random();

	//Uncomment if there is no file at ../gt-itm/graphs/alt_files/random
	public static void generateAltFiles() {
		try {
			Process p = Runtime.getRuntime().exec("./gt-itm/Runall.sh");
			p.waitFor();
		}catch (InterruptedException e) {
			e.printStackTrace();    
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Run the formulation over an input file
	 * @param datFile The input file
	 * @param modFile The formulation
	 * @param outputFile The output file with the results
	 * @return True if it finished before timeout, false otherwise
	 */
	public static boolean runGLPSOL(String datFile, String modFile, String outputFile) {		

		int TIMEOUT = 1800;
		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			builder.redirectOutput(new File(outputFile));

			Process p = builder.start();

			// Establish a timer to not allow the mip to run more than TIMEOUT
			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * TIMEOUT; 
			long finish = now + timeoutInMillis; 

			while(isAlive(p)){
				Thread.sleep(10);
				if (System.currentTimeMillis() > finish){
					System.out.println("!!! Timeout while solving this request !!!");
					p.destroy();
					return false;
				}
			}

		}catch (InterruptedException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public static String runGLPSOLHeuristic(String datFile, String modFile){		

		int TIMEOUT = 300;
		String result = "";

		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			Process p = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			// Establish a timer to not allow the mip to run more than TIMEOUT
			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * TIMEOUT; 
			long finish = now + timeoutInMillis; 
			
			while(isAlive(p)){
				Thread.sleep(10);
				if (System.currentTimeMillis() > finish){
					p.destroy();
					return "timeout";
				}
			}
			
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.getProperty("line.separator"));
			}

			result = stringBuilder.toString();

		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	//Stops the mip execution if there is a timeout
	private static boolean isAlive(Process p) {  
		try{  
			p.exitValue();  
			return false;  
		}catch (IllegalThreadStateException e) {  
			return true;  
		}  
	}
	
	//Random methods
	public static String convertToAlphabet(String number) {
		int n = Integer.parseInt(number);
		int mod = 0, tmp = n;

		String res = "";

		if(tmp == 0)
			return "A";

		while (tmp != 0) {
			mod = tmp % 26;
			res = ((char) (65 + mod)) + res;
			tmp /= 26;
		}

		return res;
	}

	public static int convertFromAlphabet(String word) {
		int result = 0, power = 0, mantissa = 0;

		for (int i = word.length() - 1; i >= 0; i--) {
			mantissa = (int)word.charAt(i) - 65;
			result += mantissa * Math.pow(26, power++);
		}

		return result;
	}

	public static double roundDecimals(double d) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		DecimalFormat twoDForm = new DecimalFormat("##.####", otherSymbols);

		return Double.valueOf(twoDForm.format(d));
	}

	/**
	 * 
	 * Copies the information of subNet to subNetAux, maintaining the state.
	 * This is used so that the auxiliary network can be changed without compromising the real one.
	 * 
	 * @param subNetAux
	 * @param subNet
	 */
	public static void populateSubstrateInfo(SubstrateNetwork subNetAux, SubstrateNetwork subNet){
		
		for (int i = 0; i < subNet.getNodes().size(); i++)
			if(!subNetAux.getNodes().contains(subNet.getNode(i)))
				subNetAux.getNodes().add(subNet.getNode(i));
		
		for (int i = 0; i < subNet.getEdges().size(); i++)
			if(!subNetAux.getEdges().contains(subNet.getEdge(i)))
				subNetAux.getEdges().add(subNet.getEdge(i));
		
		for (int i = 0; i < subNet.getEdgesBw().size(); i++)
			subNetAux.getEdgesBw().add(subNet.getEdgeBw(i));
		
		for (int i = 0; i < subNet.getEdgesLatency().size(); i++)
			subNetAux.getEdgesLatency().add(subNet.getEdgeLatency(i));
		
		for (int i = 0; i < subNet.getEdgesSec().size(); i++)
			subNetAux.getEdgesSec().add(subNet.getEdgeSec(i));

		for (int i = 0; i < subNet.getNodesSec().size(); i++)
			subNetAux.getNodesSec().add(subNet.getNodesSec().get(i));

		for (String key: subNet.getCloudSecSup().keySet())
			subNetAux.getCloudSecSup().put(key, subNet.getCloudSecSup().get(key));

		for (int i = 0; i < subNet.getNodesCPU().size(); i++)
			subNetAux.getNodesCPU().add(subNet.getNodesCPU().get(i));

		subNetAux.setNClouds(subNet.getNClouds());

		int[][] doesItBelong = new int[subNet.getNClouds()][subNet.getNumOfNodes()];
		for(int i = 0; i < subNet.getNClouds(); i++)
			for(int j = 0; j < subNet.getNumOfNodes(); j++)
				doesItBelong[i][j] = subNet.getDoesItBelong()[i][j];
		subNetAux.setDoesItBelong(doesItBelong);
		
		subNetAux.setTotalNodesCPU(subNet.getTotalNodesCPU());
		subNetAux.setTotalEdgesBw(subNet.getTotalEdgesBw());
	}

	/**
	 * 
	 * Method that writes String info into File file.
	 * 
	 * @param info
	 * @param file
	 */
	public static void writeFile(String info, String file){

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(info);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyMappedEdges(ArrayList<Pair<String>> mappedWorkingEdges, ArrayList<Pair<String>> mappedEdges) {
		for(int i = 0; i < mappedEdges.size(); i++){
			mappedWorkingEdges.add(mappedEdges.get(i));
		}
	}
	
	public static void copyEdgesUsed(ArrayList<Pair<String>> finalWorkingAndBackupEdges, LinkedList<Vertex> path, SubstrateNetwork subNet,
			ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges, ArrayList<Pair<String>> mappedEdges) {

		Vertex left = null;
		Vertex right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<Vertex> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left.getName(), right.getName());
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			if(finalVirtualWorkingAndBackupEdges != null && mappedEdges != null){
				finalVirtualWorkingAndBackupEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));
			}

			left = right;
		}
	}

	public static void copyEdgesUsed(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<String> path, SubstrateNetwork subNet) {

		String left = "";
		String right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<String> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left, right);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}
	

	public static void copyEdgesUsedK(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<BaseVertex> path, SubstrateNetwork subNet) {

		BaseVertex left = null;
		BaseVertex right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<BaseVertex> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(Utils.convertToAlphabet(""+left.get_id()), Utils.convertToAlphabet(""+right.get_id()));
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}

	public static void copyEdgesUsedDK(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<String> path, SubstrateNetwork subNet) {

		String left = null;
		String right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<String> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left, right);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}

	
	public static ArrayList<String> populateFS(ArrayList<Pair<String>> mappedNodes,	VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fs = new ArrayList<String>();

		for(int i = 0; i < virNet.getEdges().size(); i++){

			fs.add(Utils.getSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getLeft(), subNet));
		}

		return fs;
	}

	public static ArrayList<String> populateFE(ArrayList<Pair<String>> mappedNodes, VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fe = new ArrayList<String>();

		for(int i = 0; i < virNet.getEdges().size(); i++){

			fe.add(Utils.getSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getRight(), subNet));
		}

		return fe;
	}

	private static String getSubstrateNodeIndex(ArrayList<Pair<String>> mappedNodes, String virtualNode, SubstrateNetwork subNet) {

		for(int i = 0; i < mappedNodes.size(); i++){
			
			if(virtualNode.equals(mappedNodes.get(i).getRight())){
				
				return mappedNodes.get(i).getLeft();
			}
			
		}
		return null;
	}
	
	//Other mapping algorithms (D-ViNE Heuristic)

	public static ArrayList<Pair<String>> mappingVirtualWorkingNodes(HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap, SubstrateNetwork subNet){

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		
		int virtualNodeIndex, substrateNodeIndex = -1;
		double pz = -1;

		NodeHeuristicInfo nodeHeuristicInfoAux;
		HashMap<Integer, Double> auxPZ = new HashMap<Integer, Double>();
		ArrayList<NodeHeuristicInfo> aux;
		
		if(virtualNodeIndexArraySubstrateNodeInfoMap == null || virtualNodeIndexArraySubstrateNodeInfoMap.size() <=0)
			return null;

		for (Integer key : virtualNodeIndexArraySubstrateNodeInfoMap.keySet()){

			virtualNodeIndex = key - subNet.getNumOfNodes();
			aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(key);

			for (int i = 0; i < aux.size(); i++){

				nodeHeuristicInfoAux = aux.get(i);

				if(auxPZ.get(nodeHeuristicInfoAux.getIndexNode())== null){
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				} else {
					double oldPZ = auxPZ.get(nodeHeuristicInfoAux.getIndexNode());
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), oldPZ + nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				}
			}
			
			for (Integer nodeNumber : auxPZ.keySet()){
				if((pz < (auxPZ.get(nodeNumber))) && !mappedNodes.contains(nodeNumber)){
					pz = auxPZ.get(nodeNumber);
					substrateNodeIndex = nodeNumber;
				}
			}
			if(substrateNodeIndex == -1)
				return null;

			mappingVirtualNodes.add(new Pair<String>(""+substrateNodeIndex, ""+virtualNodeIndex));
			mappedNodes.add(substrateNodeIndex);
			
			substrateNodeIndex = -1;
			virtualNodeIndex = -1;
			pz=-1;
			auxPZ.clear();
		}
		return mappingVirtualNodes;
	}
	
	public static ArrayList<Pair<String>> mappingVirtualWorkingNodesSP(HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap, SubstrateNetwork subNet){

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		
		int virtualNodeIndex, substrateNodeIndex = -1;
		double pz = -1;
		double mediumDistance = 1000;

		NodeHeuristicInfo nodeHeuristicInfoAux;
		HashMap<Integer, Double> auxPZ = new HashMap<Integer, Double>();
		ArrayList<NodeHeuristicInfo> aux;
		
		if(virtualNodeIndexArraySubstrateNodeInfoMap == null || virtualNodeIndexArraySubstrateNodeInfoMap.size() <=0)
			return null;

		for (Integer key : virtualNodeIndexArraySubstrateNodeInfoMap.keySet()){

			virtualNodeIndex = key - subNet.getNumOfNodes();
			aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(key);

			for (int i = 0; i < aux.size(); i++){

				nodeHeuristicInfoAux = aux.get(i);

				if(auxPZ.get(nodeHeuristicInfoAux.getIndexNode())== null){
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				} else {
					double oldPZ = auxPZ.get(nodeHeuristicInfoAux.getIndexNode());
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), oldPZ + nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				}
			}
			
			for (Integer nodeNumber : auxPZ.keySet()){
				if((pz < (auxPZ.get(nodeNumber))) && !mappedNodes.contains(nodeNumber)){
					pz = auxPZ.get(nodeNumber);
					substrateNodeIndex = nodeNumber;
				} else if(pz == (auxPZ.get(nodeNumber)) && !mappedNodes.contains(nodeNumber)){
					double tmpMedium = 0;
					int counter = 0;
					for(int node: mappedNodes){
						if(node != -1){
							counter++;
							tmpMedium += subNet.getNodeDistance(nodeNumber, node);
						}
					}
					if(((tmpMedium/counter) < mediumDistance) || counter == 0){
						pz = auxPZ.get(nodeNumber);
						substrateNodeIndex = nodeNumber;
					}
				}
			}
			if(substrateNodeIndex == -1)
				return null;

			mappingVirtualNodes.add(new Pair<String>(""+substrateNodeIndex, ""+virtualNodeIndex));
			mappedNodes.add(substrateNodeIndex);
			
			substrateNodeIndex = -1;
			virtualNodeIndex = -1;
			pz=-1;
			auxPZ.clear();
		}
		return mappingVirtualNodes;
	}

	//D-ViNE algorithm
	
	public static ArrayList<String> populateDVineFS(ArrayList<Pair<String>> mappedNodes,
			VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fs = new ArrayList<String>();

		for(int i = 0; i < virNet.getEdges().size(); i++){

			fs.add(Utils.getDVineSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getLeft(), subNet));
		}

		return fs;
	}

	public static ArrayList<String> populateDVineFE(ArrayList<Pair<String>> mappedNodes,
			VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fe = new ArrayList<String>();

		for(int i = 0; i < virNet.getEdges().size(); i++){

			fe.add(Utils.getDVineSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getRight(), subNet));
		}

		return fe;
	}
	
	private static String getDVineSubstrateNodeIndex(ArrayList<Pair<String>> mappedNodes,
			String virtualNode, SubstrateNetwork subNet) {

		for(int i = 0; i < mappedNodes.size(); i++){

			if(virtualNode.equals(mappedNodes.get(i).getRight())){

				return mappedNodes.get(i).getLeft();
			}
		}
		return null;
	}
	
	public static ArrayList<Pair<String>> mappingDVineVirtualWorkingNodes(
			SubstrateNetwork subNet, VirtualNetwork virtualNetwork, OutputFileReader fileReaders) {

		DVineDatFileCreator dVineDatCreator = new DVineDatFileCreator();
		HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();
		ArrayList <Pair<String>> mappedWorkingNodes = new ArrayList <Pair<String>>();

		dVineDatCreator.createDatFile("/home/secdep_18nov16_bkp1/testesHeuristicas/testeDVINE_HEU.dat", subNet, virtualNetwork);

		String partialResult = Utils.runGLPSOLHeuristic("/home/secdep_18nov16_bkp1/testesHeuristicas/testeDVINE_HEU.dat", 
				"/home/secdep_18nov16_bkp1/testesHeuristicas/DVINE_HEURISTIC2.mod");

		if(partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
				partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
				partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")){

			fileReaders.setWasAccepted(false);
			return null;

		} else{
			virtualNodeIndexArraySubstrateNodeInfoMap = fileReaders.populateDVineHeuristicWorkingInfo(virtualNetwork, subNet.getNumOfNodes(), partialResult, subNet);

			mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(virtualNodeIndexArraySubstrateNodeInfoMap, subNet);
		}
		return mappedWorkingNodes;
	}
	
	//Utility algorithm
	
	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateSubstrateUtility(SubstrateNetwork subNet, 
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate, String nodeMap){

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		double lambda = 1;
		double kn = 1;
		
		int numberOfLinksAux;
		double sumEdgesAux;
		double sumTotalEdgesAux;
		double nodeUtilityAux = 0;
		
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();

		securityValuesSubstrate.clear();
		locationValuesSubstrate.clear();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			sumEdgesAux = 0;
			sumTotalEdgesAux = 0;
			numberOfLinksAux = 0;

			if(securityValuesSubstrate != null){
				if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i))))
					securityValuesSubstrate.add(subNet.getNodeSec(i));
			}
			Collections.sort(securityValuesSubstrate);
			
			if(locationValuesSubstrate != null){
				if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i))))){
					locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));
				}
			}
			Collections.sort(locationValuesSubstrate);

			for(int j = 0; j < subNet.getNumOfEdges(); j++){
				if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
					
					double edgeBW = subNet.getEdgeBw(j);
					double totalEdgeBW = subNet.getTotalEdgeBw(j);
					sumEdgesAux += edgeBW;
					sumTotalEdgesAux += totalEdgeBW;
					numberOfLinksAux ++;
				}
			}

			// First is the utility function, second uses the same utility as full greedy
//			nodeUtilityAux = lambda*(subNet.getNodeCPU(i)*sumEdgesAux)*kn*(Math.log10(numberOfLinksAux) + 1)*subNet.getNodeSec(i)*(1/(100.0*numberOfLinksAux+100.0));
			
			double nodeCPU = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
			
			switch (nodeMap) {
			case "CPU_PERCENT":
				nodeUtilityAux = nodeCPU;
				break;
			case "BANDWIDTH_PERCENT":
				nodeUtilityAux = sumEdgesAux/sumTotalEdgesAux;
				break;
			case "NUMLINKS":
				nodeUtilityAux = (Math.log10(numberOfLinksAux)+1);
				break;
			case "SECCLOUD":
				nodeUtilityAux = (1/subNet.getNodeSec(i))*(1/subNet.getCloudSecSup().get(subNet.getNode(i)));
				break;
			case "BASE":
				nodeUtilityAux = nodeCPU*(sumEdgesAux/sumTotalEdgesAux);
				break;
			case "BASENUMLINKS":
				nodeUtilityAux = nodeCPU*(sumEdgesAux/sumTotalEdgesAux)*(Math.log10(numberOfLinksAux)+1);
				break;
			case "BASESECCLOUD":
				nodeUtilityAux = nodeCPU*(sumEdgesAux/sumTotalEdgesAux)*(1/subNet.getNodeSec(i))*(1/subNet.getCloudSecSup().get(subNet.getNode(i)));
				break;
			case "UTILITY":
				//System.out.println("SubnodeUtilityAux = " + nodeCPU + "*" + (sumEdgesAux/sumTotalEdgesAux) + "*(Math.log10(" + numberOfLinksAux + ")+1)*" + "(1/" + subNet.getNodeSec(i) + ")*" +
					//	"(1/" + subNet.getCloudSecSup().get(subNet.getNode(i)));
				nodeUtilityAux = nodeCPU*(sumEdgesAux/sumTotalEdgesAux)*(Math.log10(numberOfLinksAux)+1)*(1/subNet.getNodeSec(i))*(1/subNet.getCloudSecSup().get(subNet.getNode(i)));
				break;
			default:
				break;
			}
			
			nodeUtility.add(nodeUtilityAux);
			
			SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
			int cloud = -1;
			if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null) {
				ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();
				for (int k = 0; k < subNet.getNClouds(); k++){
					if (subNet.getDoesItBelong(k, i) == 1)
						cloud = k;
				}
				aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
				secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
				//System.out.println("Node Sec -> " + subNet.getNodeSec(i));
				//System.out.println("Cloud Sec -> " + subNet.getCloudSecSup().get(subNet.getNode(i)));
			} else {
				for (int k = 0; k < subNet.getNClouds(); k++){
					if (subNet.getDoesItBelong(k, i) == 1)
						cloud = k;
				}
				secLocNodeHeuristicInfoArrayMap.get(secLocAux).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
			}
		}
		
		//Descending order of utility values
		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()){
			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
			//Collections.reverse(secLocNodeHeuristicInfoArrayMap.get(key));
		}
		
		return secLocNodeHeuristicInfoArrayMap;
	}
	
	public static ArrayList<NodeHeuristicInfo> calculateVirtualUtility(VirtualNetwork virtualNetwork, ArrayList<Double> securityValuesVirtual,
			ArrayList<Double> locationValuesVirtual, String nodeMap) {

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		
		double sumEdgesAux;
		int numberOfLinksAux;
		double nodeUtilityAux = 0.0;
		
		ArrayList<NodeHeuristicInfo> secLocNodeHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();

		securityValuesVirtual.clear();
		locationValuesVirtual.clear();

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
			sumEdgesAux = 0;
			numberOfLinksAux = 0;
			
			if(securityValuesVirtual != null){
				if(!(securityValuesVirtual.contains(virtualNetwork.getNodeSec(i))))
					securityValuesVirtual.add(virtualNetwork.getNodeSec(i));
			}
			Collections.sort(securityValuesVirtual);

			if(locationValuesVirtual != null){
				if(!(locationValuesVirtual.contains(virtualNetwork.getCloudsSecurity().get(i))))
					locationValuesVirtual.add(virtualNetwork.getCloudsSecurity().get(i));
			}
			Collections.sort(locationValuesVirtual);

			for(int j = 0; j < virtualNetwork.getNumOfEdges(); j++){
				if ((virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getLeft()))||
						(virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getRight()))){
					
					sumEdgesAux += virtualNetwork.getEdgeBw(j);
					numberOfLinksAux ++;
				}
			}
			
			double nodeCPU = 1;
			sumEdgesAux = 1;
			
			switch (nodeMap) {
			case "CPU_PERCENT":
				nodeUtilityAux = nodeCPU;
				break;
			case "BANDWIDTH_PERCENT":
				nodeUtilityAux = sumEdgesAux;
				break;
			case "NUMLINKS":
				nodeUtilityAux = (Math.log10(numberOfLinksAux)+1);
				break;
			case "SECCLOUD":
				nodeUtilityAux = (1/virtualNetwork.getNodeSec(i))*(1/virtualNetwork.getCloudSecurity(i));
				break;
			case "BASE":
				nodeUtilityAux = nodeCPU*sumEdgesAux;
				break;
			case "BASENUMLINKS":
				nodeUtilityAux = nodeCPU*sumEdgesAux*(Math.log10(numberOfLinksAux)+1);
				break;
			case "BASESECCLOUD":
				nodeUtilityAux = nodeCPU*sumEdgesAux*(1/virtualNetwork.getNodeSec(i))*(1/virtualNetwork.getCloudSecurity(i));
				break;
			case "UTILITY":
				nodeUtilityAux = nodeCPU*sumEdgesAux*(Math.log10(numberOfLinksAux)+1)*(1/virtualNetwork.getNodeSec(i))*(1/virtualNetwork.getCloudSecurity(i));
				break;
			default:
				break;
			}
			
			nodeUtility.add(nodeUtilityAux);
			
			secLocNodeHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeUtilityAux, virtualNetwork.getBackupLocalization(i)));
		}

		Collections.sort(secLocNodeHeuristicInfoArray);
		Collections.reverse(secLocNodeHeuristicInfoArray); // ascending order

		return secLocNodeHeuristicInfoArray;
	}
	
	/**
	 * 
	 * Maps working nodes according to utility values.
	 * 
	 * @param substrateSecLocNodeHeuristicInfoArrayMap
	 * @param virtualSecLocNodeHeuristicInfoArray
	 * @param subNet
	 * @param virtualNetwork
	 * @param securityValuesSubstrate
	 * @param locationValuesSubstrate
	 * @return
	 */
	public static ArrayList<Pair<String>> mappingVirtualWorkingNodes(HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray, SubstrateNetwork subNet, VirtualNetwork virtualNetwork,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate, boolean backup) {
		
		double secVirtualNodeAux, locationVirtualNodeAux;
		int virtualIndexAux, subNodeIndex = -1;
		
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux = new ArrayList<NodeHeuristicInfo>();
		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		
		ArrayList<Integer> validNodes = new ArrayList<Integer>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		
		mappedNodes.clear();
		
		for(int s = 0; s < virtualNetwork.getNumOfNodes(); s++){
			mappedNodes.add(-1);
		}
		
		found:
			for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){
				boolean found = false;
				
				virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
				secVirtualNodeAux = virtualNetwork.getNodeSec(virtualIndexAux);
				locationVirtualNodeAux = virtualNetwork.getCloudSecurity(virtualIndexAux);
				
				validNodes.clear();
				validNodes = Utils.nodesMeetRequirements(subNet, virtualNetwork, virtualIndexAux, backup);
				
				for (int i = securityValuesSubstrate.indexOf(secVirtualNodeAux); i < securityValuesSubstrate.size(); i++){
					for (int j = locationValuesSubstrate.indexOf(locationVirtualNodeAux); j < locationValuesSubstrate.size(); j++){
						
						SecLoc secloc = new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j));
						
						//Get the substrate nodes that have the given node and cloud security levels
						substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get(secloc);
						
						if(substrateSecLocNodeHeuristicInfoArrayAux != null && !validNodes.isEmpty()){
							
							for (NodeHeuristicInfo subInfo : substrateSecLocNodeHeuristicInfoArrayAux){
								subNodeIndex = subInfo.getIndexNode();
								
								if(validNodes.contains(subNodeIndex) && !mappedNodes.contains(subNodeIndex)){
									mappingVirtualNodes.add(new Pair<String>(subNet.getNode(subNodeIndex), virtualNetwork.getNode(virtualIndexAux)));
									substrateSecLocNodeHeuristicInfoArrayAux.remove(subInfo);
									found = true;
									
									if(!backup){
										for(int k = 0; k < subNet.getDoesItBelong().length; k++){
											if(subNet.getDoesItBelong(k, subNodeIndex) == 1)
												virtualNetwork.setCloudNode(virtualIndexAux, k);
										}
									}
									mappedNodes.set(virtualIndexAux, subNodeIndex);
									
									continue found;
								}
							}
						} else if(validNodes.isEmpty()){
							return null;
						}
					}
					if((i == securityValuesSubstrate.size()-1) && !found){
						return null;
					}
				}
			}
		
		if(mappedNodes.contains(-1)){
			return null;
		}
		
		return mappingVirtualNodes;
	}
	
	public static ArrayList<Pair<String>> mappingVirtualWorkingNodesSP(HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray, SubstrateNetwork subNet, VirtualNetwork virtualNetwork,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate, boolean backup) {
		
		double secVirtualNodeAux, locationVirtualNodeAux;
		int virtualIndexAux, subNodeIndex = -1;
		
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux;
		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		
		ArrayList<Integer> validNodes = new ArrayList<>();
		ArrayList<Integer> mappedNodes = new ArrayList<>();
		ArrayList<NodeHeuristicInfo> usedInfo = new ArrayList<>();
		
		for(int s = 0; s < virtualNetwork.getNumOfNodes(); s++){
			mappedNodes.add(-1);
		}
		
		for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){
			int tmpNode = -1;
			NodeHeuristicInfo tmpInfo = null;
			double mediumDistance = 1000;
			
			virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
			secVirtualNodeAux = virtualNetwork.getNodeSec(virtualIndexAux);
			locationVirtualNodeAux = virtualNetwork.getCloudSecurity(virtualIndexAux);
			
			validNodes.clear();
			validNodes = Utils.nodesMeetRequirements(subNet, virtualNetwork, virtualIndexAux, backup);
			
			for (int i = securityValuesSubstrate.indexOf(secVirtualNodeAux); i < securityValuesSubstrate.size(); i++){
				for (int j = locationValuesSubstrate.indexOf(locationVirtualNodeAux); j < locationValuesSubstrate.size(); j++){
					//Get the substrate nodes that have the given node and cloud security levels
					SecLoc secloc = new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j));
					substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get(secloc);
					
					if(substrateSecLocNodeHeuristicInfoArrayAux != null && !validNodes.isEmpty()){
						for (NodeHeuristicInfo subInfo : substrateSecLocNodeHeuristicInfoArrayAux){
							if(!usedInfo.contains(subInfo)){
								subNodeIndex = subInfo.getIndexNode();
								if(validNodes.contains(subNodeIndex) && !mappedNodes.contains(subNodeIndex)){
									double tmpMedium = 0;
									int counter = 0;
									for(int node: mappedNodes){
										if(node != -1){
											counter++;
											tmpMedium += subNet.getNodeDistance(subNodeIndex, node);
										}
									}
									if(((tmpMedium/counter) < mediumDistance) || counter == 0){
										tmpNode = subNodeIndex;
										tmpInfo = subInfo;
									}
								}
							}
						}
					} else if(validNodes.isEmpty())
						return null;
				}
				if((i == securityValuesSubstrate.size()-1) && tmpNode == -1)
					return null;
			}
			
			mappingVirtualNodes.add(new Pair<String>(subNet.getNode(tmpNode), virtualNetwork.getNode(virtualIndexAux)));
			usedInfo.add(tmpInfo);
			
			if(!backup){
				for(int k = 0; k < subNet.getDoesItBelong().length; k++){
					if(subNet.getDoesItBelong(k, tmpNode) == 1)
						virtualNetwork.setCloudNode(virtualIndexAux, k);
				}
			}
			mappedNodes.set(virtualIndexAux, tmpNode);
		}
		
		if(mappedNodes.contains(-1))
			return null;
		
		return mappingVirtualNodes;
	}
	
	//Full Greedy Algorithm
	
	public static ArrayList<String> populateFullGreedyFS(ArrayList<Pair<String>> mappedNodes,
			VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fs = new ArrayList<String>();

		for(int i = 0; i < virNet.getNumOfEdges(); i++)
			fs.add(Utils.getFullGreedySubstrateNodeIndex(mappedNodes, virNet.getEdge(i).getLeft(), subNet));

		return fs;
	}

	public static ArrayList<String> populateFullGreedyFE(ArrayList<Pair<String>> mappedNodes,
			VirtualNetwork virNet, SubstrateNetwork subNet) {

		ArrayList<String> fe = new ArrayList<String>();

		for(int i = 0; i < virNet.getNumOfEdges(); i++)
			fe.add(Utils.getFullGreedySubstrateNodeIndex(mappedNodes, virNet.getEdge(i).getRight(), subNet));

		return fe;
	}
	
	private static String getFullGreedySubstrateNodeIndex(ArrayList<Pair<String>> mappedNodes,
			String virtualNode, SubstrateNetwork subNet) {

		for(int i = 0; i < mappedNodes.size(); i++){

			if(virtualNode.equals(mappedNodes.get(i).getRight())){

				return mappedNodes.get(i).getLeft();
			}
		}
		return null;
	}
	
	/**
	 * Calculates utility values for substrate nodes (CPU and sum of adjacent bandwidth)
	 * 
	 * @param subNet
	 * @return
	 */
	public static ArrayList<NodeHeuristicInfo> calculateGreedy(SubstrateNetwork subNet){
		
		double sumEdgesAux;
		double nodeGreedyAux = 0;
		
		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		ArrayList<NodeHeuristicInfo> nodeGreedyHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();
		
		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			sumEdgesAux = 0;
			for(int j = 0; j < subNet.getNumOfEdges(); j++){
				if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft())) || (subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
					sumEdgesAux += subNet.getEdgeBw(j);
				}
			}
			
			nodeGreedyAux = subNet.getNodeCPU(i)*sumEdgesAux;
			
			nodeUtility.add(nodeGreedyAux);
			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedyAux, -1, -1));
		}
		
		//Sorts utility values in descending order
		Collections.sort(nodeGreedyHeuristicInfoArray);
		
		return nodeGreedyHeuristicInfoArray;
	}
	
	/**
	 * Calculates utility value for virtual links (CPU value)
	 * 
	 * @param virtualNetwork
	 * @return
	 */
	public static ArrayList<NodeHeuristicInfo> calculateGreedyAsc(VirtualNetwork virtualNetwork) {
		
		double nodeGreedyAsc = 0.0;
		ArrayList<NodeHeuristicInfo> nodeGreedyHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();
		
		//Virtual node utility is only calculated using CPU value
		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
			nodeGreedyAsc = virtualNetwork.getNodeCPU(i);
			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedyAsc, virtualNetwork.getBackupLocalization(i)));
		}
		
		//Sorts from lowest to highest (when both methods are called)
		Collections.sort(nodeGreedyHeuristicInfoArray);
		Collections.reverse(nodeGreedyHeuristicInfoArray);
		
		return nodeGreedyHeuristicInfoArray;
	}
	
	public static ArrayList<NodeHeuristicInfo> calculateGreedyDesc(VirtualNetwork virtualNetwork) {

		double nodeGreedy = 0;
		double sumEdgesAux = 0;
		ArrayList<NodeHeuristicInfo> nodeGreedyHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
			sumEdgesAux = 0;
			for(int j = 0; j < virtualNetwork.getNumOfEdges(); j++){
				if ((virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getLeft())) || (virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getRight()))){
					sumEdgesAux += virtualNetwork.getEdgeBw(j);
				}
			}
			
			nodeGreedy = virtualNetwork.getNodeCPU(i)*sumEdgesAux;
			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedy, virtualNetwork.getBackupLocalization(i)));
		}

		Collections.sort(nodeGreedyHeuristicInfoArray);

		return nodeGreedyHeuristicInfoArray;
	}
	
	/**
	 * 
	 * Node mapping algorithm according to utility values (using full greedy approach)
	 * 
	 * @param subNet
	 * @param virtualNetwork
	 * @param substrateNodeGreedyHeuristic
	 * @param virtualNodeGreedyHeuristic
	 * @return
	 */
	public static ArrayList<Pair<String>> mappingFullGreedyVirtualNodes(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, 
			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic, ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic, boolean checkReq){
		
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> mappingFullGreedyVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> meetReq = new ArrayList<Integer>();
		
		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes()){
			System.out.println("too many virtual nodes");
			return null;
		}

		for(int i = 0; i < virtualNodeGreedyHeuristic.size(); i++){
			
			meetReq = Utils.nodesMeetRequirements(subNet, virtualNetwork, virtualNodeGreedyHeuristic.get(i).getIndexNode(), false);
			int nodeIndex = -1;
			
			if(meetReq.size() <= 0){
				return null;
			} else {
				for(int j = 0; j < substrateNodeGreedyHeuristic.size(); j++){
					if(meetReq.contains(substrateNodeGreedyHeuristic.get(j).getIndexNode()) && !mappedNodes.contains(substrateNodeGreedyHeuristic.get(j).getIndexNode())){
						nodeIndex = substrateNodeGreedyHeuristic.get(j).getIndexNode();
						mappingFullGreedyVirtualNodes.add(new Pair<String>(""+nodeIndex, ""+virtualNodeGreedyHeuristic.get(i).getIndexNode()));
						mappedNodes.add(nodeIndex);
					}
					if(nodeIndex != -1)
						break;
						
				}
				if(nodeIndex == -1){
					return null;
				}
			}
			nodeIndex = -1;
		}
		return mappingFullGreedyVirtualNodes;
	}
	
	public static ArrayList<Pair<String>> mappingFullGreedyVirtualNodesSP(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, 
			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic, ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic, boolean checkReq){
		
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> mappingFullGreedyVirtualNodes = new ArrayList<Pair<String>>();
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes())
			return null;

		for(int i = 0; i < virtualNodeGreedyHeuristic.size(); i++){

			indexIndexNodesMeetRequirements = Utils.nodesMeetCPURequirements(subNet, virtualNetwork, 
					virtualNodeGreedyHeuristic.get(i).getIndexNode());

			if(indexIndexNodesMeetRequirements.size() <= 0)
				return null;

			int tmpSub = -1;
			double mediumDistance = 1000;
			
			for(int j = 0; j < substrateNodeGreedyHeuristic.size(); j++){
				
				int subNodeIndex = substrateNodeGreedyHeuristic.get(j).getIndexNode();
				
				if(indexIndexNodesMeetRequirements.containsKey(subNodeIndex) &&
						!mappedNodes.contains(subNodeIndex)){
					
					double tmpMedium = 0;
					int counter = 0;
					for(int node: mappedNodes){
						if(node != -1){
							counter++;
							tmpMedium += subNet.getNodeDistance(subNodeIndex, node);
						}
					}
					if(((tmpMedium/counter) < mediumDistance) || counter == 0){
						tmpSub = subNodeIndex;
					}
				}
				
				if(j == substrateNodeGreedyHeuristic.size()-1 && tmpSub == -1)
					return null;
			}
			
			// Checking security requirements, after choosing the node.
			if(checkReq){
				if(!verifyNodeRequirements(virtualNetwork, virtualNodeGreedyHeuristic.get(i).getIndexNode(), subNet, tmpSub)){
					return null;
				}
			}
			
			mappingFullGreedyVirtualNodes.add(new Pair<String>(""+tmpSub, ""+virtualNodeGreedyHeuristic.get(i).getIndexNode()));
			mappedNodes.add(tmpSub);
			
		}
		return mappingFullGreedyVirtualNodes;
	}
	
	//Previous algorithm
	public static ArrayList<Pair<String>> mappingFullGreedyVirtualNodes2(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, 
			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic, ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic){
		
		ArrayList<Pair<String>> mappingFullGreedyVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> validNodes = new ArrayList<>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		
		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes())
			return null;
		
		for(int i = 0; i < virtualNodeGreedyHeuristic.size(); i++){
			
			validNodes = Utils.nodesMeetRequirements(subNet, virtualNetwork, virtualNodeGreedyHeuristic.get(i).getIndexNode(), false);
			
			if(validNodes.isEmpty())
				return null;
			
			for(int j = 0; j < substrateNodeGreedyHeuristic.size(); j++){
				if(validNodes.contains(substrateNodeGreedyHeuristic.get(j).getIndexNode()) &&
						!mappedNodes.contains(substrateNodeGreedyHeuristic.get(j).getIndexNode())){
					
					mappingFullGreedyVirtualNodes.add(new Pair<String>(""+substrateNodeGreedyHeuristic.get(j).getIndexNode(),
							""+virtualNodeGreedyHeuristic.get(i).getIndexNode()));
					
					mappedNodes.add(substrateNodeGreedyHeuristic.get(j).getIndexNode());
					break;
				}
				if(j == substrateNodeGreedyHeuristic.size() - 1)
					return null;
			}
		}
		
		return mappingFullGreedyVirtualNodes;
	}
	
	//Used algorithms
	
	/**
	 * 
	 * Full Random algorithm for node mapping (insert a small explanation).
	 * 
	 * @param subNet
	 * @param virtualNetwork
	 * @param mappedWorkingNodes
	 * @return
	 */
	public static ArrayList<Pair<String>> mappingFullRandomVirtualNodes(SubstrateNetwork subNet, VirtualNetwork virtualNetwork,
			ArrayList<Pair<String>> mappedWorkingNodes, boolean backup){

		int nodeIndexAux = -1, count = 0;
		
		ArrayList<Pair<String>> mappingRandomVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Integer> validNodes = new ArrayList<Integer>();
		
		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes())
			return null;
		
		if(mappedWorkingNodes != null)
			for(int i = 0; i < mappedWorkingNodes.size(); i++)
				mappedNodes.add(subNet.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()));
		
		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
			validNodes = Utils.nodesMeetRequirements(subNet, virtualNetwork, i, backup);
			
			while (nodeIndexAux == -1 && count < 1000) {
				nodeIndexAux = random.nextInt(subNet.getNumOfNodes());
				if (mappedNodes.contains(nodeIndexAux)){
					nodeIndexAux  = -1;
				}
				count++;
			}
			
			if(!validNodes.contains(nodeIndexAux))
				return null;
			
			mappedNodes.add(nodeIndexAux);
			mappingRandomVirtualNodes.add(new Pair<String>(subNet.getNode(nodeIndexAux),virtualNetwork.getNode(i)));
			
			//Saves information about in which cloud the virtual node was mapped
			if(!backup){
				for(int j = 0; j < subNet.getDoesItBelong().length; j++){
					if(subNet.getDoesItBelong(j, nodeIndexAux) == 1)
						virtualNetwork.setCloudNode(i, j);
				}
			}
			
			count = 0;
			nodeIndexAux = -1;
		}		
		return mappingRandomVirtualNodes;
	}
	
	/**
	 * 
	 * Partial Random algorithm for node mapping (insert a small explanation).
	 * 
	 * @param subNet
	 * @param virtualNetwork
	 * @return
	 */
	public static ArrayList<Pair<String>> mappingPartialRandomVirtualNodes(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, boolean backup){
		int subNodeIndex = -1, count = 0;
		
		ArrayList<Pair<String>> mappingPartialRandomVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Integer> validNodes = new ArrayList<Integer>();
		ArrayList<Integer> validCPUNodes = new ArrayList<Integer>();
		
		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes()){
			System.out.println("Virtual Network has too many nodes.");
			return null;
		}
		
		for (int virtualNodeIndex = 0; virtualNodeIndex < virtualNetwork.getNumOfNodes(); virtualNodeIndex++){
			
			validNodes = Utils.nodesMeetSecRequirements(subNet, virtualNetwork, virtualNodeIndex, backup);
			validCPUNodes = Utils.nodesMeetRequirements(subNet, virtualNetwork, virtualNodeIndex, backup);
			
			if(!validNodes.isEmpty()){
				subNodeIndex = validNodes.get(random.nextInt(validNodes.size()));
				
				if (mappedNodes.contains(subNodeIndex) && !validCPUNodes.contains(subNodeIndex)){
					subNodeIndex = -1;
				}
			}
			
			if(subNodeIndex == -1){
				return null;
			}
			
			mappedNodes.add(subNodeIndex);
			mappingPartialRandomVirtualNodes.add(new Pair<String>(subNet.getNode(subNodeIndex), virtualNetwork.getNode(virtualNodeIndex)));
			
			//Saves information about in which cloud the virtual node was mapped
			if(!backup){
				for(int i = 0; i < subNet.getDoesItBelong().length; i++){
					if(subNet.getDoesItBelong(i, subNodeIndex) == 1)
						virtualNetwork.setCloudNode(virtualNodeIndex, i);
				}
			}
			
			count = 0;
			subNodeIndex = -1;
		}
		
		return mappingPartialRandomVirtualNodes;
	}
	
	/**
	 * 
	 * Algorithm for node mapping with three possibilities for the utility function:
	 * 
	 * <p> CPU Value - Choose the node that fulfills the requirements and contains the highest available CPU value. </p>
	 * <p> CPU Percent - Choose the node that fulfills the requirements and contains the highest available CPU percentage. </p>
	 * <p> Bandwidth - Choose the node that fulfills the requirements and contains the highest available adjacent bandwidth value. </p>
	 * 
	 * @param subNet
	 * @param virNet
	 * @param utility
	 * @return
	 */
	public static ArrayList<Pair<String>> virtualNodeMapping(SubstrateNetwork subNet, VirtualNetwork virNet, String utility, boolean backup) {

		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> nodeMappingResult = new ArrayList<Pair<String>>();
		ArrayList<Integer> validNodes = new ArrayList<>();

		if (virNet.getNumOfNodes() > subNet.getNumOfNodes()){
			System.out.println("Virtual Network is too big!");
			return null;
		}
		
		switch (utility) {
		case "CPU_VALUE":
			for (int virtualNodeIndex = 0; virtualNodeIndex < virNet.getNumOfNodes(); virtualNodeIndex++) {
				int highestCpuIndex = -1;
				double highestCpuValue = 0.0;
				validNodes = Utils.nodesMeetRequirements(subNet, virNet, virtualNodeIndex, backup);
				
				for (int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++) {
					if (subNet.getNodeCPU(subNodeIndex) > highestCpuValue && validNodes.contains(subNodeIndex)
							&& !mappedNodes.contains(subNodeIndex)) {
						
							highestCpuIndex = subNodeIndex;
							highestCpuValue = subNet.getNodeCPU(subNodeIndex);
					}
				}
				
				if (highestCpuIndex == -1)
					return null;

				nodeMappingResult.add(new Pair<String>(subNet.getNode(highestCpuIndex), virNet.getNode(virtualNodeIndex)));
				mappedNodes.add(highestCpuIndex);
				
				if(!backup){
					for(int i = 0; i < subNet.getDoesItBelong().length; i++){
						if(subNet.getDoesItBelong(i, highestCpuIndex) == 1)
							virNet.setCloudNode(virtualNodeIndex, i);
					}
				}
			}
			break;
		case "CPU_PERCENT":
			for (int virtualNodeIndex = 0; virtualNodeIndex < virNet.getNumOfNodes(); virtualNodeIndex++) {
				int highestCpuPercentIndex = -1;
				double highestCpuPercent = 0.0;
				double auxPercent = 0.0;
				validNodes = Utils.nodesMeetRequirements(subNet, virNet, virtualNodeIndex, backup);
				
				for (int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++) {
					if (validNodes.contains(subNodeIndex) && !mappedNodes.contains(subNodeIndex)) {
						auxPercent = (subNet.getNodeCPU(subNodeIndex) / subNet.getTotalNodeCPU(subNodeIndex));
						if (auxPercent > highestCpuPercent) {
							highestCpuPercent = auxPercent;
							highestCpuPercentIndex = subNodeIndex;
						}
					}
				}

				if (highestCpuPercentIndex == -1)
					return null;

				nodeMappingResult.add(new Pair<String>(subNet.getNode(highestCpuPercentIndex), virNet.getNode(virtualNodeIndex)));
				mappedNodes.add(highestCpuPercentIndex);
				
				if(!backup){
					for(int i = 0; i < subNet.getDoesItBelong().length; i++){
						if(subNet.getDoesItBelong(i, highestCpuPercentIndex) == 1)
							virNet.setCloudNode(virtualNodeIndex, i);
					}
				}
			}
			break;
		case "BANDWIDTH":
			for (int virtualNodeIndex = 0; virtualNodeIndex < virNet.getNumOfNodes(); virtualNodeIndex++) {
				double totalAdjacentBandwidth = 0.0;
				int highestBwIndex = -1;
				validNodes = Utils.nodesMeetRequirements(subNet, virNet, virtualNodeIndex, backup);

				for (int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++) {
					if (validNodes.contains(subNodeIndex) && !mappedNodes.contains(subNodeIndex)) {
						double auxTotalBandwidth = 0.0;
						for (int subEdgeIndex = 0; subEdgeIndex < subNet.getNumOfEdges(); subEdgeIndex++)
							if((subNet.getNode(subNodeIndex).equals(subNet.getEdge(subEdgeIndex).getLeft())) || 
									(subNet.getNode(subNodeIndex).equals(subNet.getEdge(subEdgeIndex).getRight())))
								auxTotalBandwidth += subNet.getEdgeBw(subEdgeIndex);
						
						if (auxTotalBandwidth > totalAdjacentBandwidth) {
							totalAdjacentBandwidth = auxTotalBandwidth;
							highestBwIndex = subNodeIndex;
						}
					}
				}

				if (highestBwIndex == -1)
					return null;

				nodeMappingResult.add(new Pair<String>(subNet.getNode(highestBwIndex), virNet.getNode(virtualNodeIndex)));
				mappedNodes.add(highestBwIndex);
				
				if(!backup){
					for(int i = 0; i < subNet.getDoesItBelong().length; i++){
						if(subNet.getDoesItBelong(i, highestBwIndex) == 1)
							virNet.setCloudNode(virtualNodeIndex, i);
					}
				}
			}
			break;
		case "BANDWIDTH_PERCENT":
			for(int virtualNodeIndex = 0; virtualNodeIndex < virNet.getNumOfNodes(); virtualNodeIndex++){
				double highestBwPercent = 0.0;
				int highestBwIndex = -1;
				
				validNodes = Utils.nodesMeetRequirements(subNet, virNet, virtualNodeIndex, backup);
				
				for(int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++){
					if(validNodes.contains(subNodeIndex) && !mappedNodes.contains(subNodeIndex)){
						double totalBw = 0.0, resBw = 0.0, bwPercent = 0.0;
						for(int subEdgeIndex = 0; subEdgeIndex < subNet.getNumOfEdges(); subEdgeIndex++){
							if(subNet.getEdge(subEdgeIndex).contains(Utils.convertToAlphabet(""+subNodeIndex))){
								totalBw += subNet.getTotalEdgeBw(subEdgeIndex);
								resBw += subNet.getEdgeBw(subEdgeIndex);
							}
						}
						bwPercent = resBw / totalBw;
						if(bwPercent > highestBwPercent){
							highestBwPercent = bwPercent;
							highestBwIndex = subNodeIndex;
						}
					}
				}
				
				if(highestBwIndex == -1)
					return null;
				
				nodeMappingResult.add(new Pair<String>(subNet.getNode(highestBwIndex), virNet.getNode(virtualNodeIndex)));
				mappedNodes.add(highestBwIndex);
				
				if(!backup){
					for(int i = 0; i < subNet.getDoesItBelong().length; i++){
						if(subNet.getDoesItBelong(i, highestBwIndex) == 1)
							virNet.setCloudNode(virtualNodeIndex, i);
					}
				}
			}
			break;
		default:
			break;
		}
		
		return nodeMappingResult;
	}
	
	//Other useful methods
	
	/**
	 * 
	 * Disables working nodes and links, so that backups aren't placed in the same substrate resources.
	 * 
	 * @param subNetAux
	 * @param mappedWorkingNodes
	 * @param mappedWorkingEdges
	 */
	public static void disableWorkingNodesAndLinks(SubstrateNetwork subNetAux, ArrayList<Pair<String>> mappedWorkingNodes, ArrayList<Pair<String>> mappedWorkingEdges) {

		for(int i = 0; i < mappedWorkingNodes.size(); i++)
			subNetAux.getNodesCPU().set(subNetAux.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()), 0.0);
		
		Pair<String> tmp;

		for(int i = 0; i < mappedWorkingEdges.size(); i++){
			if(subNetAux.getEdges().contains(mappedWorkingEdges.get(i))){
				subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(mappedWorkingEdges.get(i)), 0.0);
			} else{
				tmp = new Pair<String>(mappedWorkingEdges.get(i).getRight(), mappedWorkingEdges.get(i).getLeft());
				
				if(subNetAux.getEdges().contains(tmp))
					subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(tmp), 0.0);
			}
		}
	}

	/**
	 * 
	 * Verifies substrate node's security level, cloud security level, and available CPU value.
	 * 
	 * @param virtualNetwork
	 * @param virtualNodeIndex
	 * @param subNet
	 * @param subNodeIndex
	 * @return
	 */
	public static boolean verifyNodeRequirements(VirtualNetwork virtualNetwork, int virtualNodeIndex, SubstrateNetwork subNet, int subNodeIndex) {

		if(subNodeIndex != -1)
			if(subNet.getNode(subNodeIndex) != null)
				if(subNet.getCloudSecSup().get(subNet.getNode(subNodeIndex)) != null)
					if(subNet.getCloudSecSup().get(subNet.getNode(subNodeIndex)) >= virtualNetwork.getCloudSecurity(virtualNodeIndex))
						if(subNet.getNodeSec(subNodeIndex) >= virtualNetwork.getNodeSec(virtualNodeIndex))
							if (subNet.getNodeCPU(subNodeIndex) >= virtualNetwork.getNodeCPU(virtualNodeIndex))
								return true;

		return false;
	}
	
	public static boolean verifyNodeSecRequirements(VirtualNetwork virtualNetwork, int virtualNodeIndex, SubstrateNetwork subNet, int subNodeIndex) {

		if(subNodeIndex != -1)
			if(subNet.getNode(subNodeIndex) != null)
				if(subNet.getCloudSecSup().get(subNet.getNode(subNodeIndex)) != null)
					if(subNet.getCloudSecSup().get(subNet.getNode(subNodeIndex)) >= virtualNetwork.getCloudSecurity(virtualNodeIndex))
						if(subNet.getNodeSec(subNodeIndex) >= virtualNetwork.getNodeSec(virtualNodeIndex))
							return true;

		return false;
	}
	
	public static boolean meetsBWReq(VirtualNetwork virtualNetwork, int virtualNodeIndex, SubstrateNetwork subNet, int subNodeIndex){
		double sumSubEdgesAux = 0;
		int numberOfLinksAux = 0;
		
		for(int j = 0; j < subNet.getNumOfEdges(); j++){
			if ((subNet.getNode(subNodeIndex).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(subNodeIndex).equals(subNet.getEdge(j).getRight()))){
			
				double edgeBW = subNet.getEdgeBw(j);
				sumSubEdgesAux += edgeBW;
				numberOfLinksAux ++;
			}
		}
		
		double sumVirtEdgesAux = 0;
		double virtNumberOfLinksAux = 0;
		
		for(int j = 0; j < virtualNetwork.getNumOfEdges(); j++){
			if ((virtualNetwork.getNode(virtualNodeIndex).equals(virtualNetwork.getEdge(j).getLeft()))||
					(virtualNetwork.getNode(virtualNodeIndex).equals(virtualNetwork.getEdge(j).getRight()))){
				
				sumVirtEdgesAux += virtualNetwork.getEdgeBw(j);
				virtNumberOfLinksAux ++;
			}
		}
		
		if((numberOfLinksAux >= virtNumberOfLinksAux) && sumSubEdgesAux >= sumVirtEdgesAux){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * Returns an array with the indexes of the nodes that meet the desired requirements.
	 * 
	 * @param subNet
	 * @param virtualNetwork
	 * @param virtualNodeIndex
	 * 
	 * @return ArrayList containing the indexes of substrate nodes that comply with the requirements.
	 */
	public static ArrayList<Integer> nodesMeetRequirements(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, int virtualNodeIndex, boolean backup){
		ArrayList<Integer> validNodes = new ArrayList<Integer>();
		
		int cloud = -1;
		if(backup){
			cloud = virtualNetwork.getCloudForNode(virtualNodeIndex);
		}
		
		for (int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++){
			if(verifyNodeRequirements(virtualNetwork, virtualNodeIndex, subNet, subNodeIndex) && meetsBWReq(virtualNetwork, virtualNodeIndex, subNet, subNodeIndex)){
				if(backup){
					//same cloud
					if((virtualNetwork.getBackupLocalization(virtualNodeIndex) == 2) && (subNet.getDoesItBelong(cloud, subNodeIndex) == 1)){
						validNodes.add(subNodeIndex);
					}
					//diff cloud
					else if((virtualNetwork.getBackupLocalization(virtualNodeIndex) == 1) && (subNet.getDoesItBelong(cloud, subNodeIndex) == 0)){
						validNodes.add(subNodeIndex);
					}
				} else{
					validNodes.add(subNodeIndex);
				}
			}
		}
		
		return validNodes;
	}

	public static ArrayList<Integer> nodesMeetSecRequirements(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, int virtualNodeIndex, boolean backup){
		ArrayList<Integer> validNodes = new ArrayList<Integer>();
		
		int failedNode = 0;
		int failedLink = 0;
		
		int cloud = -1;
		if(backup){
			cloud = virtualNetwork.getCloudForNode(virtualNodeIndex);
		}
		
		for (int subNodeIndex = 0; subNodeIndex < subNet.getNumOfNodes(); subNodeIndex++){
			if(verifyNodeSecRequirements(virtualNetwork, virtualNodeIndex, subNet, subNodeIndex)){
				if(backup){
					if((virtualNetwork.getBackupLocalization(virtualNodeIndex) == 2) && (subNet.getDoesItBelong(cloud, subNodeIndex) == 1)){
						validNodes.add(subNodeIndex);
					} else if((virtualNetwork.getBackupLocalization(virtualNodeIndex) == 1) && (subNet.getDoesItBelong(cloud, subNodeIndex) == 0)){
						validNodes.add(subNodeIndex);
					}
				} else{
					validNodes.add(subNodeIndex);
				}
			} else {
				if(!verifyNodeRequirements(virtualNetwork, virtualNodeIndex, subNet, subNodeIndex))
					failedNode++;
				else if(!meetsBWReq(virtualNetwork, virtualNodeIndex, subNet, subNodeIndex))
					failedLink++;
			}
		}
		
		return validNodes;
	}
	
	/**
	 * 
	 * Returns an hashmap with the indexes of the substrate nodes that meet CPU requirements.
	 * 
	 * @param subNet
	 * @param virtualNetwork
	 * @param indexVirtualNetwork
	 * @return
	 */
	public static HashMap<Integer,Integer> nodesMeetCPURequirements(SubstrateNetwork subNet, VirtualNetwork virtualNetwork, int indexVirtualNetwork){
		int indexMap = 0;
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		for (int i = 0; i < subNet.getNodes().size(); i++){
			if((subNet.getNodeCPU(i) >= virtualNetwork.getNodeCPU(indexVirtualNetwork))){
				indexIndexNodesMeetRequirements.put(indexMap, i);
				indexMap++;
			}
		}
		return indexIndexNodesMeetRequirements;
	}
	
}
