package pt.SecDepVNE.Substrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import pt.SecDepVNE.Common.Pair;
import pt.SecDepVNE.Common.Utils;
import pt.SecDepVNE.Common.KShortestPath.model.abstracts.BaseVertex;
import pt.SecDepVNE.Virtual.VirtualNetwork;

/**
 * Manager of substrate resources
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class SubstrateManager {

	private SubstrateNetwork subNet;

	public SubstrateManager(SubstrateNetwork subNet) {
		this.subNet = subNet;
	}

	/**
	 * Updates substrate resources as virtual networks arrive to the system
	 * or depart from it
	 * @param virNet Virtual Network
	 * @param mappedEdges Virtual edges embedded
	 * @param edgesUsed Substrate edges used
	 * @param mappedNodes Virtual nodes embedded
	 * @param nodesUsed Substrate nodes used
	 * @param mode Arrival or departure
	 */
	//Alterer este aqui
	public void updateSubstrateNetwork(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges, ArrayList<Pair<String>> edgesUsed, 
			ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, UpdateMode mode) {

		updateSubstrateEdges(virNet, mappedEdges, edgesUsed, mode);
		updateSubstrateNodes(virNet, mappedNodes, nodesUsed, mode);

	}

	public void updateSubstrateNetworkDK(VirtualNetwork virNet,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup,
			ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, UpdateMode mode) {

		updateSubstrateEdgesDK(virNet, indexVirtualLinkToPahtsForVirtualLinksWorking, 
				indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking, indexVirtualLinkToPahtsForVirtualLinksBackup, 
				indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup, mode);
		
		updateSubstrateNodesDK(virNet, mappedNodes, nodesUsed, indexVirtualLinkToPahtsForVirtualLinksWorking, 
				indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking, indexVirtualLinkToPahtsForVirtualLinksBackup, 
				indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup, mode);
	}

	//Used in case of MCF
	public void updateSubstrateNetwork(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges, ArrayList<Pair<String>> edgesUsed, 
			ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, ArrayList<Double> bwEdgesUsed, UpdateMode mode) {

		updateSubstrateEdges(virNet, mappedEdges, edgesUsed, bwEdgesUsed, mode);
		updateSubstrateNodes(virNet, mappedNodes, nodesUsed, mode); 

	}

	public void updateSubstrateNetwork(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgesUsed, UpdateMode mode){

		updateSubstrateEdges(virNet, mappedEdges, edgesUsed, mode);

	}
	
	//Used in case of Shortest-path
	public void updateSubstrateNetworkSP(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgesUsed, UpdateMode mode){

		updateSubstrateEdgesSP(virNet, mappedEdges, edgesUsed, mode);

	}

	public void updateSubstrateNetwork(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgesUsed, ArrayList<Double> bwEdgesUsed, UpdateMode mode){

		updateSubstrateEdges(virNet, mappedEdges, edgesUsed, bwEdgesUsed, mode);

	}


	public void updateSubstrateNetwork(VirtualNetwork virNet, 
			HashMap<Integer, List<BaseVertex>> listOfKPathsComplyLatency,
			HashMap<Integer, Double> bandwidthInPahtForVirtualLinks, UpdateMode mode) {

		updateSubstrateEdges(virNet, listOfKPathsComplyLatency, bandwidthInPahtForVirtualLinks, mode);

	}
	
	public void updateSubstrateNetworkDK(VirtualNetwork virNet, 
			HashMap<Integer, List<String>> listOfKPathsComplyLatency,
			HashMap<Integer, Double> bandwidthInPahtForVirtualLinks, UpdateMode mode) {

		updateSubstrateEdgesDK(virNet, listOfKPathsComplyLatency, bandwidthInPahtForVirtualLinks, mode);

	}


	// Update the substrate egdes residual capacity
	private void updateSubstrateEdges(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges,
			ArrayList<Pair<String>> edgesUsed, UpdateMode mode) {

		Pair<String> tmpEdge, tmpEdge2;
		double bw = 0;
		ArrayList<Pair<String>> vEdges = virNet.getEdges();

		for(int i = 0; i < edgesUsed.size(); i++){

			tmpEdge = mappedEdges.get(i);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(vEdges.contains(tmpEdge))
				bw = virNet.getEdgeBw(vEdges.indexOf(tmpEdge));
			else if(vEdges.contains(tmpEdge2))
				bw = virNet.getEdgeBw(vEdges.indexOf(tmpEdge2));

			bw = mode == UpdateMode.DECREMENT ? -bw : bw;

			tmpEdge = edgesUsed.get(i);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge), bw);
			else if(subNet.getEdges().contains(tmpEdge2))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge2), bw);
		}
	}
	
	private void updateSubstrateEdgesSP(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges,
			ArrayList<Pair<String>> edgesUsed, UpdateMode mode) {

		Pair<String> tmpEdge, tmpEdge2;
		double bw = 0;
		ArrayList<Pair<String>> vEdges = virNet.getEdges();
		
		tmpEdge = mappedEdges.get(0);
		tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

		if(vEdges.contains(tmpEdge))
			bw = virNet.getEdgeBw(vEdges.indexOf(tmpEdge));
		else if(vEdges.contains(tmpEdge2))
			bw = virNet.getEdgeBw(vEdges.indexOf(tmpEdge2));

		bw = mode == UpdateMode.DECREMENT ? -bw : bw;

		for(int i = 0; i < edgesUsed.size(); i++){

			tmpEdge = edgesUsed.get(i);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge), bw);
			else if(subNet.getEdges().contains(tmpEdge2))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge2), bw);
		}
	}

	// Update the substrate egdes residual capacity MCF
	private void updateSubstrateEdges(VirtualNetwork virNet, ArrayList<Pair<String>> mappedEdges,
			ArrayList<Pair<String>> edgesUsed, ArrayList<Double> bwEdgesUsed, UpdateMode mode) {

		Pair<String> tmpEdge, tmpEdge2;
		double bw = 0;

		for(int i = 0; i < edgesUsed.size(); i++){
			bw = bwEdgesUsed.get(i);

			bw = mode == UpdateMode.DECREMENT ? -bw : bw;

			tmpEdge = edgesUsed.get(i);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge), bw);
			else if(subNet.getEdges().contains(tmpEdge2))
				subNet.updateEdgeBw(subNet.getEdges().indexOf(tmpEdge2), bw);
		}
	}

	// Update the substrate egdes residual capacity K-ShortestPath for ONE virtual edge
	private void updateSubstrateEdges(VirtualNetwork virNet, 
			HashMap<Integer, List<BaseVertex>> listOfKPathsComplyLatency,
			HashMap<Integer, Double> bandwidthInPahtForVirtualLinks, UpdateMode mode) {

		ArrayList<Pair<String>> edgesInAPath = null;
		List<BaseVertex> path;
		int key = -1; 
		Iterator<Entry<Integer, List<BaseVertex>>> it = listOfKPathsComplyLatency.entrySet().iterator();
		double bw = 0;

		while(it.hasNext()){
			HashMap.Entry pair = (HashMap.Entry)it.next();
			key = (int) pair.getKey();

			path = listOfKPathsComplyLatency.get(key);
			Utils.copyEdgesUsedK(edgesInAPath, path, subNet);

			for(int i = 0; i < edgesInAPath.size(); i++){

				bw = bandwidthInPahtForVirtualLinks.get(key);
				bw = mode == UpdateMode.DECREMENT ? -bw : bw;
				subNet.updateEdgeBw(subNet.getEdges().indexOf(edgesInAPath.get(i)), bw);
			}
		}
	}
	
	// Update the substrate egdes residual capacity K-ShortestPath for ONE virtual edge
	private void updateSubstrateEdgesDK(VirtualNetwork virNet, 
			HashMap<Integer, List<String>> listOfKPathsComplyLatency,
			HashMap<Integer, Double> bandwidthInPahtForVirtualLinks, UpdateMode mode) {

		ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
		List<String> path;
		int key = -1; 
		Iterator<Entry<Integer, List<String>>> it = listOfKPathsComplyLatency.entrySet().iterator();
		double bw = 0;

		while(it.hasNext()){
			HashMap.Entry pair = (HashMap.Entry)it.next();
			key = (int) pair.getKey();

			path = listOfKPathsComplyLatency.get(key);
			Utils.copyEdgesUsedDK(edgesInAPath, path, subNet);

			for(int i = 0; i < edgesInAPath.size(); i++){

				bw = bandwidthInPahtForVirtualLinks.get(key);
				bw = mode == UpdateMode.DECREMENT ? -bw : bw;
				subNet.updateEdgeBw(subNet.getEdges().indexOf(edgesInAPath.get(i)), bw);
			}
			edgesInAPath.clear();
		}
	}

	// Update the substrate egdes residual capacity K-ShortestPath for ALL edges
	private void updateSubstrateEdgesDK(VirtualNetwork virNet,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup,
			UpdateMode mode) {

		HashMap<Integer, List<String>> listOfKPathsComplyLatency = null;
		HashMap<Integer, Double> bandwidthInPahtForVirtualLinks = null;

		//Working edges
		if((indexVirtualLinkToPahtsForVirtualLinksWorking != null) && (indexVirtualLinkToPahtsForVirtualLinksWorking.size() >= 0)){
			
			Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it = indexVirtualLinkToPahtsForVirtualLinksWorking.entrySet().iterator();

			while(it.hasNext()){
				HashMap.Entry pair = (HashMap.Entry)it.next();
				int key = (int) pair.getKey();

				listOfKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksWorking.get(key);
				bandwidthInPahtForVirtualLinks = indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking.get(key);

				ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
				List<String> path;
				int key2 = -1; 
				Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
				double bw = 0;

				while(it2.hasNext()){
					HashMap.Entry pair2 = (HashMap.Entry)it2.next();
					key2 = (int) pair2.getKey();

					path = listOfKPathsComplyLatency.get(key2);
					Utils.copyEdgesUsed(edgesInAPath, path, subNet);

					for(int i = 0; i < edgesInAPath.size(); i++){

						bw = bandwidthInPahtForVirtualLinks.get(key2);
						bw = mode == UpdateMode.DECREMENT ? -bw : bw;
						subNet.updateEdgeBw(subNet.getEdges().indexOf(edgesInAPath.get(i)), bw);
					}
					edgesInAPath.clear();
				}
			}
		}


		//Backup edges
		if((indexVirtualLinkToPahtsForVirtualLinksBackup != null) && (indexVirtualLinkToPahtsForVirtualLinksBackup.size() >= 0)){
			
			Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it3 = indexVirtualLinkToPahtsForVirtualLinksBackup.entrySet().iterator();

			while(it3.hasNext()){
				HashMap.Entry pair = (HashMap.Entry)it3.next();
				int key = (int) pair.getKey();

				listOfKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksBackup.get(key);
				bandwidthInPahtForVirtualLinks = indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup.get(key);

				ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
				List<String> path;
				int key2 = -1; 
				Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
				double bw = 0;

				while(it2.hasNext()){
					HashMap.Entry pair2 = (HashMap.Entry)it2.next();
					key2 = (int) pair2.getKey();

					path = listOfKPathsComplyLatency.get(key2);
					Utils.copyEdgesUsed(edgesInAPath, path, subNet);

					for(int i = 0; i < edgesInAPath.size(); i++){

						bw = bandwidthInPahtForVirtualLinks.get(key2);
						bw = mode == UpdateMode.DECREMENT ? -bw : bw;
						subNet.updateEdgeBw(subNet.getEdges().indexOf(edgesInAPath.get(i)), bw);
					}
					edgesInAPath.clear();
				}
			}
		}
	}

	// Update the substrate nodes residual capacity
	private void updateSubstrateNodes(VirtualNetwork virNet, ArrayList<String> mappedNodes,
			ArrayList<String> nodesUsed, UpdateMode mode) {

		String tmpNode;
		double cpu = 0;
		ArrayList<String> vNodes = virNet.getNodes();

		for(int i = 0; i < nodesUsed.size(); i++){

			if(nodesUsed.size() > mappedNodes.size()){
				System.out.println("Erro!");
			}

			tmpNode = mappedNodes.get(i);

			if(vNodes.contains(tmpNode))
				cpu = virNet.getNodeCPU(vNodes.indexOf(tmpNode));

			cpu = mode == UpdateMode.DECREMENT ? -cpu : cpu;

			subNet.updateNodeCPU(subNet.getNodes().indexOf(nodesUsed.get(i)), cpu);

		}
	}
	
	// Update the substrate nodes residual capacity
	private void updateSubstrateNodesDK(VirtualNetwork virNet, ArrayList<String> mappedNodes,
			ArrayList<String> nodesUsed, HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup, UpdateMode mode) {

		String tmpNode;
		double cpu = 0;
		ArrayList<String> vNodes = virNet.getNodes();

		for(int i = 0; i < nodesUsed.size(); i++){

			if(nodesUsed.size() > mappedNodes.size()){
				System.out.println("Erro!");
			}

			tmpNode = mappedNodes.get(i);

			if(vNodes.contains(tmpNode))
				cpu = virNet.getNodeCPU(vNodes.indexOf(tmpNode));

			cpu = mode == UpdateMode.DECREMENT ? -cpu : cpu;

			subNet.updateNodeCPU(subNet.getNodes().indexOf(nodesUsed.get(i)), cpu);

		}
	}
}