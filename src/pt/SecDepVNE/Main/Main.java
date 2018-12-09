package pt.SecDepVNE.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.SecDepVNE.Common.ResourceGenerator;
import pt.SecDepVNE.Substrate.SubstrateCreator;
import pt.SecDepVNE.Substrate.SubstrateNetwork;
import pt.SecDepVNE.Virtual.RequestsCreator;
import pt.SecDepVNE.Virtual.VirtualNetwork;

public class Main {

	private static String UTILITY_HEURISTICMODFILE = "./glpk/modFiles/UTILITY_HEURISTIC.mod";
	private static String PARTIAL_RANDOM_HEURISTICMODFILE = "./glpk/modFiles/PARTIAL_RANDOM_HEURISTIC.mod";
	private static String FULL_GREEDY_HEURISTICMODFILE = "./glpk/modFiles/FULLGREEDY_MCF_HEURISTIC.mod";
	private static String FULL_RANDOM_HEURISTICMODFILE = "./glpk/modFiles/FULL_RANDOM_HEURISTIC.mod";
	private static String DVINE_HEURISTICMODFILE = "./glpk/modFiles/DVINE_HEURISTIC.mod";
	private static String DVINE_MCF_HEURISTICMODFILE = "./glpk/modFiles/DVINE_MCF_HEURISTIC.mod";

//	private static int POISSON_MEAN = 50; //2 pedidos por 100 unidades de tempo - 1000 pedidos
	private static int POISSON_MEAN_25 = 25; //4 pedidos por 100 unidades de tempo - 2000 pedidos
	private static int POISSON_MEAN_12 = 12; //8 pedidos por 100 unidades de tempo - 4000 pedidos
	private static int TOTAL_TIME = 50000;
	
	public static void main(String[] args) {
		
		if(args[0].equals("reconfig")){
			reconfigurationSimulation(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		} else if(args[0].equals("embedding")){
			embeddingSimulation(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		} else {
			executeExperienceType1(50, 2000, 3);
		}
		
		/*
		int option = 2;
		
		switch (option) {
		case 1:
			embeddingSimulation(2000, 3);
			break;
		case 2:
			reconfigurationSimulation(4000, 3);
			break;
		case 3:
			executeExperienceType1(50, 2000, 3);
			break;
		}
		*/
	}

	private static void executeExperienceType1(int nSNodes, int nRequests, int nClouds) {
		SubstrateCreator subCreator = new SubstrateCreator();
		RequestsCreator reqCreator = new RequestsCreator();
		ResourceGenerator resGen = new ResourceGenerator();
		
		/*
		 * To generate a new substrate network, do:
		 * 
		 * Run the program;
		 * Execute Runall.sh;
		 * Delete the save file for the substrate network;
		 * Run the program again.
		 * 
		 */
		
		SubstrateNetwork subNet;
		File f = new File("./gt-itm/graphs/alt_files/saves/random_sub_s" + nSNodes + "_noSec");
		if (!f.exists()) {
			subNet = subCreator.generateRandomSubstrateNetwork("random_sub_s" + nSNodes, nSNodes, nClouds);
			subNet.printToFile("./gt-itm/graphs/alt_files/saves/random_sub_s" + nSNodes);
		} else {
			System.out.println("Using the existent substrate network!");
			subNet = subCreator.generateSubstrateNetwork("./gt-itm/graphs/alt_files/saves/random_sub_s" + nSNodes);
		}
		
		//Node variation is more important that link variation
		SubstrateNetwork subNet50 = subCreator.generateSubstrateNetwork("./gt-itm/graphs/alt_files/saves/random_sub_s50");
		
		ArrayList<VirtualNetwork> virtualNets0 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets1 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets2 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets3 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets4 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets5 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets6 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets7 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets8 = new ArrayList<>();
		
		Comparator<Event> comp = new Comparator<Event>() {
			@Override
			public int compare(Event e1, Event e2) {
				return e1.getTime() - e2.getTime();
			}
		};
		PriorityQueue<Event> queue1 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue2 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue3 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue4 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue5 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue6 = new PriorityQueue<Event>(1000, comp);
		PriorityQueue<Event> queue7 = new PriorityQueue<Event>(1000, comp);

		int countk = 0, k = 0, p = 0, start = 0;
		int arrivalTime;
		
		for (int i = 0; i < nRequests; i++) {
			reqCreator.generateRandomVirtualNetwork("random_req_50_" + i, subNet50.getNumOfNodes());
		}

		System.exit(0);
		
		for (int i = 0; i < nRequests; i++) {
			//Virtual Networks for 50 node substrate
			VirtualNetwork virNet_50_0_0 = reqCreator.createVirtualNetworkNoReqs("./gt-itm/graphs/alt_files/random/random_req_50_" + i + ".alt", subNet50.getNClouds());
			VirtualNetwork virNet_50_5_5 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_50_"+i+".alt", subNet50.getNClouds(), 5, 5);
			VirtualNetwork virNet_50_10_10 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_50_"+i+".alt", subNet50.getNClouds(), 10, 10);
			
			if (countk == k) {
				k = 0;
				while (k == 0) {
					k = resGen.generateArrivalTime(POISSON_MEAN_25);
				}
				countk = 0;
				start = (p * TOTAL_TIME * POISSON_MEAN_25) / nRequests;
				p++;
			}
			arrivalTime = start + ((countk + 1) * TOTAL_TIME * POISSON_MEAN_25) / (nRequests * (k + 1));
			countk++;

			virNet_50_0_0.setArrival(arrivalTime);
			virNet_50_5_5.setArrival(arrivalTime);
			virNet_50_10_10.setArrival(arrivalTime);

			virNet_50_0_0.setNodesCPU(virNet_50_10_10.getNodesCPU());
			virNet_50_0_0.setEdgesBw(virNet_50_10_10.getEdgesBw());
			virNet_50_5_5.setNodesCPU(virNet_50_10_10.getNodesCPU());
			virNet_50_5_5.setEdgesBw(virNet_50_10_10.getEdgesBw());

			//Virtual Networks without security
			virtualNets0.add(virNet_50_0_0);
			
			//Virtual Networks with 5% security
			virtualNets3.add(virNet_50_5_5);
			
			//Virtual Networks with 10% security
			virtualNets6.add(virNet_50_10_10);

			queue1.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue2.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue3.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue4.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue5.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue6.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue7.add(new Event(EventType.ARRIVE, arrivalTime, i));
		}

		Random rand = new Random();
		
		for(VirtualNetwork vNet: virtualNets3){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
		}
		
		//Joao Paulino, reconfiguration base test environment
		//---------------------------------------- || ------------------------------------------------------
		
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		SubstrateNetwork subNet5 = new SubstrateNetwork(subNet50);
		ArrayList<VirtualNetwork> virNets5 = new ArrayList<>(virtualNets0);
		Worker worker5 = new Worker("experience5", subNet5, virNets5, queue5, UTILITY_HEURISTICMODFILE,
				"UTILITY", "MCF", false, false, "");
		service.execute(worker5);
		
		service.shutdown();
	}
	
	private static void embeddingSimulation(int nSNodes, int nRequests, int nClouds){
		SubstrateCreator subCreator = new SubstrateCreator();
		RequestsCreator reqCreator = new RequestsCreator();
		ResourceGenerator resGen = new ResourceGenerator();
		
		SubstrateNetwork subNet;
		File f = new File("./gt-itm/graphs/alt_files/saves/sub" + nSNodes);
		if (!f.exists()) {
			subNet = subCreator.generateRandomSubstrateNetwork("sub" + nSNodes, nSNodes, nClouds);
			subNet.printToFile("./gt-itm/graphs/alt_files/saves/sub" + nSNodes);
		} else {
			System.out.println("Using the existent substrate network!");
			subNet = subCreator.generateSubstrateNetwork("./gt-itm/graphs/alt_files/saves/sub" + nSNodes);
		}
		
		ArrayList<VirtualNetwork> virtualNets1 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets2 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets3 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets4 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets5 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets6 = new ArrayList<>();
		
		Comparator<Event> comp = new Comparator<Event>() {
			@Override
			public int compare(Event e1, Event e2) {
				return e1.getTime() - e2.getTime();
			}
		};
		
		PriorityQueue<Event> queue1 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue2 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue3 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue4 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue5 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue6 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue7 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue8 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue9 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue10 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue11 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue12 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue13 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue14 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue15 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue16 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue17 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue18 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue19 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue20 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue21 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue22 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue23 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue24 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue25 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue26 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue27 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue28 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue29 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue30 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue31 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue32 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue33 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue34 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue35 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue36 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue37 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue38 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue39 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue40 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue41 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue42 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue43 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue44 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue45 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue46 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue47 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue48 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue49 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue50 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue51 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue52 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue53 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue54 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue55 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue56 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue57 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue58 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue59 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue60 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue61 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue62 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue63 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue64 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue65 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue66 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue67 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue68 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue69 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue70 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue71 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue72 = new PriorityQueue<Event>(2000, comp);

		int countk = 0, k = 0, p = 0, start = 0;
		int arrivalTime;
		
		for (int i = 0; i < nRequests; i++) {
			VirtualNetwork virNet_0_0 = reqCreator.createVirtualNetworkNoReqs("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_" + i + ".alt", subNet.getNClouds());
			VirtualNetwork virNet_0_0_dep = reqCreator.createVirtualNetworkDep("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_" + i + ".alt", subNet.getNClouds());
			VirtualNetwork virNet_90_10 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_"+i+".alt", subNet.getNClouds(), 90, 5);
			VirtualNetwork virNet_80_20 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_"+i+".alt", subNet.getNClouds(), 80, 10);
			
			if (countk == k) {
				k = 0;
				while (k == 0) {
					k = resGen.generateArrivalTime(POISSON_MEAN_25);
				}
				countk = 0;
				start = (p * TOTAL_TIME * POISSON_MEAN_25) / nRequests;
				p++;
			}
			arrivalTime = start + ((countk + 1) * TOTAL_TIME * POISSON_MEAN_25) / (nRequests * (k + 1));
			countk++;
			
			virNet_0_0.setArrival(arrivalTime);		// 0% asks for security
			virNet_0_0_dep.setArrival(arrivalTime);
			virNet_90_10.setArrival(arrivalTime);	// 10% security
			virNet_80_20.setArrival(arrivalTime);	// 20% security
			
			virNet_0_0.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_0_0.setEdgesBw(virNet_80_20.getEdgesBw());
			
			virNet_0_0_dep.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_0_0_dep.setEdgesBw(virNet_80_20.getEdgesBw());
			
			virNet_90_10.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_90_10.setEdgesBw(virNet_80_20.getEdgesBw());
						
			virtualNets1.add(new VirtualNetwork(virNet_0_0));		//sec0dep0
			virtualNets2.add(new VirtualNetwork(virNet_0_0_dep));	//sec0dep10
			virtualNets3.add(new VirtualNetwork(virNet_0_0_dep));	//sec0dep20
			virtualNets4.add(new VirtualNetwork(virNet_90_10));		//sec10dep0
			virtualNets5.add(new VirtualNetwork(virNet_80_20));		//sec20dep0
			virtualNets6.add(new VirtualNetwork(virNet_80_20));		//sec20dep20

			queue1.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue2.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue3.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue4.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue5.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue6.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue7.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue8.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue9.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue10.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue11.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue12.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue13.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue14.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue15.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue16.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue17.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue18.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue19.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue20.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue21.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue22.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue23.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue24.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue25.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue26.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue27.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue28.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue29.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue30.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue31.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue32.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue33.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue34.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue35.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue36.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue37.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue38.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue39.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue40.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue41.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue42.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue43.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue44.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue45.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue46.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue47.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue48.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue49.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue50.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue51.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue52.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue53.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue54.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue55.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue56.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue57.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue58.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue59.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue60.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue61.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue62.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue63.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue64.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue65.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue66.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue67.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue68.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue69.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue70.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue71.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue72.add(new Event(EventType.ARRIVE, arrivalTime, i));
		}

		//------------------------------
		
		Random rand = new Random();
		
		//0% of requests ask for dep
		
		for(VirtualNetwork vNet: virtualNets1){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}
		
		for(VirtualNetwork vNet: virtualNets4){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}
		
		for(VirtualNetwork vNet: virtualNets5){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}

		//10% of requests ask for dep
		
		for (VirtualNetwork vnet : virtualNets2) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 9)
				vnet.setWantBackup(true);
			else {
				int nodes = vnet.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet.setBackupLocalization(locs);
				vnet.setWantBackup(false);
			}
		}
		
		//20% of requests ask for dep
		for (VirtualNetwork vnet2 : virtualNets3) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 19){
				vnet2.setWantBackup(true);
			}else {
				int nodes = vnet2.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet2.setBackupLocalization(locs);
				vnet2.setWantBackup(false);
			}
		}
		
		for (VirtualNetwork vnet : virtualNets6) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 19)
				vnet.setWantBackup(true);
			else {
				int nodes = vnet.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet.setBackupLocalization(locs);
				vnet.setWantBackup(false);
			}
		}
		
		//---------------------------------------- || ------------------------------------------------------
		
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		//Substrate Networks
		SubstrateNetwork subNet1 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet2 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet3 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet4 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet5 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet6 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet7 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet8 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet9 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet10 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet11 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet12 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet13 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet14 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet15 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet16 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet17 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet18 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet19 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet20 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet21 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet22 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet23 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet24 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet25 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet26 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet27 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet28 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet29 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet30 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet31 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet32 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet33 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet34 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet35 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet36 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet37 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet38 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet39 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet40 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet41 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet42 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet43 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet44 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet45 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet46 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet47 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet48 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet49 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet50 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet51 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet52 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet53 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet54 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet55 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet56 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet57 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet58 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet59 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet60 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet61 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet62 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet63 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet64 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet65 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet66 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet67 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet68 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet69 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet70 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet71 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet72 = new SubstrateNetwork(subNet);
		
		//Virtual Networks
		ArrayList<VirtualNetwork> virNets1 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets2 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets3 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets4 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets5 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets6 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets7 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets8 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets9 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets10 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets11 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets12 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets13 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets14 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets15 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets16 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets17 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets18 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets19 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets20 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets21 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets22 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets23 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets24 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets25 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets26 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets27 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets28 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets29 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets30 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets31 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets32 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets33 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets34 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets35 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets36 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets37 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets38 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets39 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets40 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets41 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets42 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets43 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets44 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets45 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets46 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets47 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets48 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets49 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets50 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets51 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets52 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets53 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets54 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets55 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets56 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets57 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets58 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets59 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets60 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets61 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets62 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets63 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets64 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets65 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets66 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets67 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets68 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets69 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets70 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets71 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets72 = new ArrayList<>(virtualNets6);
		
		//Sec0Dep0
		Worker worker1 = new Worker("experience1",subNet1,virNets1,queue1,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker2 = new Worker("experience2",subNet2,virNets2,queue2,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker3 = new Worker("experience3",subNet3,virNets3,queue3,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker4 = new Worker("experience4",subNet4,virNets4,queue4,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker5 = new Worker("experience5",subNet5,virNets5,queue5,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker6 = new Worker("experience6",subNet6,virNets6,queue6,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker7 = new Worker("experience7",subNet7,virNets7,queue7,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker8 = new Worker("experience8",subNet8,virNets8,queue8,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker9 = new Worker("experience9",subNet9,virNets9,queue9,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker10 = new Worker("experience10",subNet10,virNets10,queue10,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker11 = new Worker("experience11",subNet11,virNets11,queue11,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker12 = new Worker("experience12",subNet12,virNets12,queue12,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Sec0Dep10
		Worker worker13 = new Worker("experience13",subNet13,virNets13,queue13,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker14 = new Worker("experience14",subNet14,virNets14,queue14,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker15 = new Worker("experience15",subNet15,virNets15,queue15,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker16 = new Worker("experience16",subNet16,virNets16,queue16,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker17 = new Worker("experience17",subNet17,virNets17,queue17,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker18 = new Worker("experience18",subNet18,virNets18,queue18,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker19 = new Worker("experience19",subNet19,virNets19,queue19,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker20 = new Worker("experience20",subNet20,virNets20,queue20,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker21 = new Worker("experience21",subNet21,virNets21,queue21,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker22 = new Worker("experience22",subNet22,virNets22,queue22,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker23 = new Worker("experience23",subNet23,virNets23,queue23,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker24 = new Worker("experience24",subNet24,virNets24,queue24,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Sec0Dep20
		Worker worker25 = new Worker("experience25",subNet25,virNets25,queue25,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker26 = new Worker("experience26",subNet26,virNets26,queue26,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker27 = new Worker("experience27",subNet27,virNets27,queue27,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker28 = new Worker("experience28",subNet28,virNets28,queue28,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker29 = new Worker("experience29",subNet29,virNets29,queue29,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker30 = new Worker("experience30",subNet30,virNets30,queue30,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker31 = new Worker("experience31",subNet31,virNets31,queue31,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker32 = new Worker("experience32",subNet32,virNets32,queue32,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker33 = new Worker("experience33",subNet33,virNets33,queue33,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker34 = new Worker("experience34",subNet34,virNets34,queue34,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker35 = new Worker("experience35",subNet35,virNets35,queue35,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker36 = new Worker("experience36",subNet36,virNets36,queue36,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Sec10Dep0
		Worker worker37 = new Worker("experience37",subNet37,virNets37,queue37,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker38 = new Worker("experience38",subNet38,virNets38,queue38,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker39 = new Worker("experience39",subNet39,virNets39,queue39,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker40 = new Worker("experience40",subNet40,virNets40,queue40,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker41 = new Worker("experience41",subNet41,virNets41,queue41,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker42 = new Worker("experience42",subNet42,virNets42,queue42,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker43 = new Worker("experience43",subNet43,virNets43,queue43,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker44 = new Worker("experience44",subNet44,virNets44,queue44,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker45 = new Worker("experience45",subNet45,virNets45,queue45,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker46 = new Worker("experience46",subNet46,virNets46,queue46,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker47 = new Worker("experience47",subNet47,virNets47,queue47,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker48 = new Worker("experience48",subNet48,virNets48,queue48,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Sec20Dep0
		Worker worker49 = new Worker("experience49",subNet49,virNets49,queue49,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker50 = new Worker("experience50",subNet50,virNets50,queue50,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker51 = new Worker("experience51",subNet51,virNets51,queue51,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker52 = new Worker("experience52",subNet52,virNets52,queue52,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker53 = new Worker("experience53",subNet53,virNets53,queue53,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker54 = new Worker("experience54",subNet54,virNets54,queue54,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker55 = new Worker("experience55",subNet55,virNets55,queue55,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker56 = new Worker("experience56",subNet56,virNets56,queue56,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker57 = new Worker("experience57",subNet57,virNets57,queue57,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker58 = new Worker("experience58",subNet58,virNets58,queue58,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker59 = new Worker("experience59",subNet59,virNets59,queue59,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker60 = new Worker("experience60",subNet60,virNets60,queue60,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Sec20Dep20
		Worker worker61 = new Worker("experience61",subNet61,virNets61,queue61,FULL_RANDOM_HEURISTICMODFILE,"FULL_RANDOM","MCF",false,true,"");
		Worker worker62 = new Worker("experience62",subNet62,virNets62,queue62,PARTIAL_RANDOM_HEURISTICMODFILE,"PARTIAL_RANDOM","MCF",false,true,"");
		Worker worker63 = new Worker("experience63",subNet63,virNets63,queue63,UTILITY_HEURISTICMODFILE,"CPU_PERCENT","MCF",false,true,"");
		Worker worker64 = new Worker("experience64",subNet64,virNets64,queue64,UTILITY_HEURISTICMODFILE,"BANDWIDTH_PERCENT","MCF",false,true,"");
		Worker worker65 = new Worker("experience65",subNet65,virNets65,queue65,UTILITY_HEURISTICMODFILE,"NUMLINKS","MCF",false,true,"");
		Worker worker66 = new Worker("experience66",subNet66,virNets66,queue66,UTILITY_HEURISTICMODFILE,"SECCLOUD","MCF",false,true,"");
		Worker worker67 = new Worker("experience67",subNet67,virNets67,queue67,UTILITY_HEURISTICMODFILE,"BASE","MCF",false,true,"");
		Worker worker68 = new Worker("experience68",subNet68,virNets68,queue68,UTILITY_HEURISTICMODFILE,"BASENUMLINKS","MCF",false,true,"");
		Worker worker69 = new Worker("experience69",subNet69,virNets69,queue69,UTILITY_HEURISTICMODFILE,"BASESECCLOUD","MCF",false,true,"");
		Worker worker70 = new Worker("experience70",subNet70,virNets70,queue70,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		Worker worker71 = new Worker("experience71",subNet71,virNets71,queue71,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
		Worker worker72 = new Worker("experience72",subNet72,virNets72,queue72,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		//Execution
		
		service.execute(worker1);
		service.execute(worker2);
		service.execute(worker3);
		service.execute(worker4);
		service.execute(worker5);
		service.execute(worker6);
		service.execute(worker7);
		service.execute(worker8);
		service.execute(worker9);
		service.execute(worker10);
		service.execute(worker11);
		service.execute(worker12);
		service.execute(worker13);
		service.execute(worker14);
		service.execute(worker15);
		service.execute(worker16);
		service.execute(worker17);
		service.execute(worker18);
		service.execute(worker19);
		service.execute(worker20);
		service.execute(worker21);
		service.execute(worker22);
		service.execute(worker23);
		service.execute(worker24);
		service.execute(worker25);
		service.execute(worker26);
		service.execute(worker27);
		service.execute(worker28);
		service.execute(worker29);
		service.execute(worker30);
		service.execute(worker31);
		service.execute(worker32);
		service.execute(worker33);
		service.execute(worker34);
		service.execute(worker35);
		service.execute(worker36);
		service.execute(worker37);
		service.execute(worker38);
		service.execute(worker39);
		service.execute(worker40);
		service.execute(worker41);
		service.execute(worker42);
		service.execute(worker43);
		service.execute(worker44);
		service.execute(worker45);
		service.execute(worker46);
		service.execute(worker47);
		service.execute(worker48);
		service.execute(worker49);
		service.execute(worker50);
		service.execute(worker51);
		service.execute(worker52);
		service.execute(worker53);
		service.execute(worker54);
		service.execute(worker55);
		service.execute(worker56);
		service.execute(worker57);
		service.execute(worker58);
		service.execute(worker59);
		service.execute(worker60);
		service.execute(worker61);
		service.execute(worker62);
		service.execute(worker63);
		service.execute(worker64);
		service.execute(worker65);
		service.execute(worker66);
		service.execute(worker67);
		service.execute(worker68);
		service.execute(worker69);
		service.execute(worker70);
		service.execute(worker71);
		service.execute(worker72);
		
		service.shutdown();
	}
	
	private static void reconfigurationSimulation(int nSNodes, int nRequests, int nClouds){
		SubstrateCreator subCreator = new SubstrateCreator();
		RequestsCreator reqCreator = new RequestsCreator();
		ResourceGenerator resGen = new ResourceGenerator();

		SubstrateNetwork subNet;
		File f = new File("./gt-itm/graphs/alt_files/saves/sub" + nSNodes);
		if (!f.exists()) {
			subNet = subCreator.generateRandomSubstrateNetwork("sub" + nSNodes, 50, nClouds);
			subNet.printToFile("./gt-itm/graphs/alt_files/saves/subsub" + nSNodes);
		} else {
			System.out.println("Using the existent substrate network!");
			subNet = subCreator.generateSubstrateNetwork("./gt-itm/graphs/alt_files/saves/subsub" + nSNodes);
		}
		
		ArrayList<VirtualNetwork> virtualNets1 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets2 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets3 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets4 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets5 = new ArrayList<>();
		ArrayList<VirtualNetwork> virtualNets6 = new ArrayList<>();
		
		Comparator<Event> comp = new Comparator<Event>() {
			@Override
			public int compare(Event e1, Event e2) {
				return e1.getTime() - e2.getTime();
			}
		};
		
		PriorityQueue<Event> queue1 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue2 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue3 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue4 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue5 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue6 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue7 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue8 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue9 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue10 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue11 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue12 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue13 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue14 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue15 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue16 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue17 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue18 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue19 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue20 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue21 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue22 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue23 = new PriorityQueue<Event>(2000, comp);
		PriorityQueue<Event> queue24 = new PriorityQueue<Event>(2000, comp);

		int countk = 0, k = 0, p = 0, start = 0;
		int arrivalTime;

		for (int i = 0; i < nRequests; i++) {
			reqCreator.generateRandomVirtualNetwork("random_req_" + nSNodes + "_" + i, subNet.getNumOfNodes());
		}
		
		for (int i = 0; i < nRequests; i++) {
			VirtualNetwork virNet_0_0 = reqCreator.createVirtualNetworkNoReqs("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_" + i + ".alt", subNet.getNClouds());
			VirtualNetwork virNet_0_0_dep = reqCreator.createVirtualNetworkDep("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_" + i + ".alt", subNet.getNClouds());
			VirtualNetwork virNet_90_10 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_"+i+".alt", subNet.getNClouds(), 90, 5);
			VirtualNetwork virNet_80_20 = reqCreator.createVirtualNetwork("./gt-itm/graphs/alt_files/random/random_req_"+nSNodes+"_"+i+".alt", subNet.getNClouds(), 80, 10);
			
			if (countk == k) {
				k = 0;
				while (k == 0) {
					k = resGen.generateArrivalTime(POISSON_MEAN_12);
				}
				countk = 0;
				start = (p * TOTAL_TIME * POISSON_MEAN_12) / nRequests;
				p++;
			}
			arrivalTime = start + ((countk + 1) * TOTAL_TIME * POISSON_MEAN_12) / (nRequests * (k + 1));
			countk++;
			
			virNet_0_0.setArrival(arrivalTime);		// 0% asks for security
			virNet_0_0_dep.setArrival(arrivalTime);
			virNet_90_10.setArrival(arrivalTime);	// 10% security
			virNet_80_20.setArrival(arrivalTime);	// 20% security
			
			virNet_0_0.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_0_0.setEdgesBw(virNet_80_20.getEdgesBw());
			
			virNet_0_0_dep.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_0_0_dep.setEdgesBw(virNet_80_20.getEdgesBw());
			
			virNet_90_10.setNodesCPU(virNet_80_20.getNodesCPU());
			virNet_90_10.setEdgesBw(virNet_80_20.getEdgesBw());
						
			virtualNets1.add(new VirtualNetwork(virNet_0_0));		//sec0dep0
			virtualNets2.add(new VirtualNetwork(virNet_0_0_dep));	//sec0dep10
			virtualNets3.add(new VirtualNetwork(virNet_0_0_dep));	//sec0dep20
			virtualNets4.add(new VirtualNetwork(virNet_90_10));		//sec10dep0
			virtualNets5.add(new VirtualNetwork(virNet_80_20));		//sec20dep0
			virtualNets6.add(new VirtualNetwork(virNet_80_20));		//sec20dep20

			queue1.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue2.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue3.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue4.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue5.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue6.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue7.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue8.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue9.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue10.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue11.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue12.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue13.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue14.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue15.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue16.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue17.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue18.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue19.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue20.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue21.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue22.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue23.add(new Event(EventType.ARRIVE, arrivalTime, i));
			queue24.add(new Event(EventType.ARRIVE, arrivalTime, i));
		}

		//------------------------------
		
		Random rand = new Random();
		
		//0% of requests ask for dep
		
		for(VirtualNetwork vNet: virtualNets1){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}
		
		for(VirtualNetwork vNet: virtualNets4){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}
		
		for(VirtualNetwork vNet: virtualNets5){
			int nodes = vNet.getNumOfNodes();
			ArrayList<Integer> locs = new ArrayList<>();
			for(int i = 0; i < nodes; i++)
				locs.add(0);
			vNet.setBackupLocalization(locs);
			vNet.setWantBackup(false);
		}

		//10% of requests ask for dep
		
		for (VirtualNetwork vnet : virtualNets2) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 9)
				vnet.setWantBackup(true);
			else {
				int nodes = vnet.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet.setBackupLocalization(locs);
				vnet.setWantBackup(false);
			}
		}
		
		//20% of requests ask for dep
		for (VirtualNetwork vnet2 : virtualNets3) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 19){
				vnet2.setWantBackup(true);
			}else {
				int nodes = vnet2.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet2.setBackupLocalization(locs);
				vnet2.setWantBackup(false);
			}
		}
		
		for (VirtualNetwork vnet : virtualNets6) {
			int res = rand.nextInt(100);
			if (res >= 0 && res <= 19)
				vnet.setWantBackup(true);
			else {
				int nodes = vnet.getNumOfNodes();
				ArrayList<Integer> locs = new ArrayList<Integer>();
				for (int i = 0; i < nodes; i++)
					locs.add(0);
				vnet.setBackupLocalization(locs);
				vnet.setWantBackup(false);
			}
		}
		
		//---------------------------------------- || ------------------------------------------------------
		
//		ExecutorService service = Executors.newFixedThreadPool(35);
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		//Substrate Networks
		SubstrateNetwork subNet1 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet2 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet3 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet4 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet5 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet6 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet7 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet8 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet9 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet10 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet11 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet12 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet13 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet14 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet15 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet16 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet17 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet18 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet19 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet20 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet21 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet22 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet23 = new SubstrateNetwork(subNet);
		SubstrateNetwork subNet24 = new SubstrateNetwork(subNet);
		
		//Virtual Networks
		ArrayList<VirtualNetwork> virNets1 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets2 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets3 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets4 = new ArrayList<>(virtualNets1);
		ArrayList<VirtualNetwork> virNets5 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets6 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets7 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets8 = new ArrayList<>(virtualNets2);
		ArrayList<VirtualNetwork> virNets9 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets10 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets11 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets12 = new ArrayList<>(virtualNets3);
		ArrayList<VirtualNetwork> virNets13 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets14 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets15 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets16 = new ArrayList<>(virtualNets4);
		ArrayList<VirtualNetwork> virNets17 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets18 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets19 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets20 = new ArrayList<>(virtualNets5);
		ArrayList<VirtualNetwork> virNets21 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets22 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets23 = new ArrayList<>(virtualNets6);
		ArrayList<VirtualNetwork> virNets24 = new ArrayList<>(virtualNets6);
		
		// sec0dep0 - noRec
		Worker worker1 = new Worker("experience1",subNet1,virNets1,queue1,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
//		Worker worker2 = new Worker("experience2",subNet2,virNets2,queue2,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",false,true,"",true);
//		Worker worker3 = new Worker("experience3",subNet3,virNets3,queue3,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",false,true,"");
		
		// sec0dep0 - duration
		Worker worker2 = new Worker("experience2",subNet2,virNets2,queue2,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
//		Worker worker5 = new Worker("experience5",subNet5,virNets5,queue5,FULL_GREEDY_HEURISTICMODFILE,"FULL_GREEDY","MCF",true,true,"OPTIMAL",true);
//		Worker worker6 = new Worker("experience6",subNet6,virNets6,queue6,DVINE_HEURISTICMODFILE,DVINE_MCF_HEURISTICMODFILE,"DVINE","MCF",true,true,"OPTIMAL");
		
		// sec0dep0 - size
		Worker worker3 = new Worker("experience3",subNet3,virNets3,queue3,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec0dep0 - recent
		Worker worker4 = new Worker("experience4",subNet4,virNets4,queue4,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		// sec0dep10 - noRec
		Worker worker5 = new Worker("experience5",subNet5,virNets5,queue5,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		
		// sec0dep10 - duration
		Worker worker6 = new Worker("experience6",subNet6,virNets6,queue6,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
		
		// sec0dep10 - size
		Worker worker7 = new Worker("experience7",subNet7,virNets7,queue7,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec0dep10 - recent
		Worker worker8 = new Worker("experience8",subNet8,virNets8,queue8,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		// sec0dep20 - noRec
		Worker worker9 = new Worker("experience9",subNet9,virNets9,queue9,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		
		// sec0dep20 - duration
		Worker worker10 = new Worker("experience10",subNet10,virNets10,queue10,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
		
		// sec0dep20 - size
		Worker worker11 = new Worker("experience11",subNet11,virNets11,queue11,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec0dep20 - recent
		Worker worker12 = new Worker("experience12",subNet12,virNets12,queue12,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		// sec10dep0 - noRec
		Worker worker13 = new Worker("experience13",subNet13,virNets13,queue13,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		
		// sec10dep0 - duration
		Worker worker14 = new Worker("experience14",subNet14,virNets14,queue14,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
		
		// sec10dep0 - size
		Worker worker15 = new Worker("experience15",subNet15,virNets15,queue15,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec10dep0 - recent
		Worker worker16 = new Worker("experience16",subNet16,virNets16,queue16,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		// sec20dep0 - noRec
		Worker worker17 = new Worker("experience17",subNet17,virNets17,queue17,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		
		// sec20dep0 - duration
		Worker worker18 = new Worker("experience18",subNet18,virNets18,queue18,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
		
		// sec20dep0 - size
		Worker worker19 = new Worker("experience19",subNet19,virNets19,queue19,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec20dep0 - recent
		Worker worker20 = new Worker("experience20",subNet20,virNets20,queue20,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		// sec20dep20 - noRec
		Worker worker21 = new Worker("experience21",subNet21,virNets21,queue21,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",false,true,"");
		
		// sec20dep20 - duration
		Worker worker22 = new Worker("experience22",subNet22,virNets22,queue22,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"OPTIMAL");
		
		// sec20dep20 - size
		Worker worker23 = new Worker("experience23",subNet23,virNets23,queue23,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"SIZE");
		
		// sec20dep20 - recent
		Worker worker24 = new Worker("experience24",subNet24,virNets24,queue24,UTILITY_HEURISTICMODFILE,"UTILITY","MCF",true,true,"RECENT");
		
		
		service.execute(worker1);
		service.execute(worker2);
		service.execute(worker3);
		service.execute(worker4);
		service.execute(worker5);
		service.execute(worker6);
		service.execute(worker7);
		service.execute(worker8);
		service.execute(worker9);
		service.execute(worker10);
		service.execute(worker11);
		service.execute(worker12);
		service.execute(worker13);
		service.execute(worker14);
		service.execute(worker15);
		service.execute(worker16);
		service.execute(worker17);
		service.execute(worker18);
		service.execute(worker19);
		service.execute(worker20);
		service.execute(worker21);
		service.execute(worker22);
		service.execute(worker23);
		service.execute(worker24);
		
		service.shutdown();
	}
	
}
