package pt.SecDepVNE.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.PriorityQueue;

import pt.SecDepVNE.Common.Pair;
import pt.SecDepVNE.Common.Utils;
import pt.SecDepVNE.Common.VirtualNetworkEvent;
import pt.SecDepVNE.Glpk.DVineDatFileCreator;
import pt.SecDepVNE.Glpk.HeuristicDVineDatFileCreator;
import pt.SecDepVNE.Glpk.HeuristicFullGreedyDatFileCreator;
import pt.SecDepVNE.Glpk.HeuristicSecDepDatFileCreator;
import pt.SecDepVNE.Glpk.OutputFileReader;
import pt.SecDepVNE.Glpk.SecDepDatFileCreator;
import pt.SecDepVNE.Heuristic.LinkHeuristicInfo;
import pt.SecDepVNE.Heuristic.NodeHeuristicInfo;
import pt.SecDepVNE.Heuristic.SecLoc;
import pt.SecDepVNE.Substrate.SubstrateManager;
import pt.SecDepVNE.Substrate.SubstrateNetwork;
import pt.SecDepVNE.Substrate.UpdateMode;
import pt.SecDepVNE.Virtual.VirtualNetwork;

/**
 * 
 * @author Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias
 *         da Universidade de Lisboa
 *
 */
public class Worker implements Runnable {

	private static final int PERIOD = 100;

	private SubstrateNetwork subNet;
	private ArrayList<VirtualNetwork> virNets;
	private String modFile;
	private String modFile2;
	private String graphstype;

	// Heuristics info
	ArrayList<Double> securityValuesSubstrate = new ArrayList<Double>();
	ArrayList<Double> locationValuesSubstrate = new ArrayList<Double>();
	ArrayList<Pair<String>> mappedWorkingNodes = new ArrayList<Pair<String>>();
	ArrayList<Pair<String>> mappedWorkingEdges = new ArrayList<Pair<String>>();
	ArrayList<Pair<String>> mappedBackupNodes = new ArrayList<Pair<String>>();
	ArrayList<Pair<String>> mappedBackupEdges = new ArrayList<Pair<String>>();

	HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap;
	HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> virtualSecLocNodeHeuristicInfoArrayMap;
	ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray;

	// ---------------

	// New stuff - link corrections (Max - June 3rd)
	ArrayList<Double> securityValuesVirtual = new ArrayList<Double>();
	ArrayList<Double> locationValuesVirtual = new ArrayList<Double>();
	ArrayList<Pair<String>> mappedWorkingEdgesAux = new ArrayList<Pair<String>>();
	ArrayList<Pair<String>> mappedBackupEdgesAux = new ArrayList<Pair<String>>();

	// ---------------

	private PriorityQueue<Event> queue;

	// Joao Paulino, 48490
	private String nodeMap;
	private String linkMap;
	private boolean reconfig = false;
	private double totalAccepted = 0.0;
	private int numReconfig = 0;
	private int failedRec = 0;
	private int curTime = 0;
	private boolean considersSecCost;

	// Counters for the number of failures
	private int linkFail = 0;
	private int nodeFail = 0;

	private String reconfigType = "";
	private boolean success = false;
	private boolean checkReq = false;
	private String folder = "";
	
	private ArrayList<Event> rejectedEvents = new ArrayList<>();
	private ArrayList<String> finalString = new ArrayList<>();
	
	private int link1 = 0, link2 = 0, link3 = 0, link4 = 0, totalLinks = 0;
	
	// -----------------

	public Worker(String graphstype, SubstrateNetwork subNet, ArrayList<VirtualNetwork> virNets,
			PriorityQueue<Event> queue, String modFile) {
		this.subNet = subNet;
		this.virNets = virNets;
		this.modFile = modFile;
		this.graphstype = graphstype;
		this.queue = queue;
	}

	public Worker(String graphstype, SubstrateNetwork subNet, ArrayList<VirtualNetwork> virNets,
			PriorityQueue<Event> queue, String modFile1, String modFile2) {
		this.subNet = subNet;
		this.virNets = virNets;
		this.modFile = modFile1;
		this.modFile2 = modFile2;
		this.graphstype = graphstype;
		this.queue = queue;
	}

	// -----------------
	
	// Joao Paulino, 48490
	public Worker(String graphstype, SubstrateNetwork subNet, ArrayList<VirtualNetwork> virNets,
			PriorityQueue<Event> queue, String modFile, String nodeMap, String linkMap, boolean reconfig,
			boolean considersSecCost, String reconfigType) {
		this.subNet = subNet;
		this.virNets = virNets;
		this.modFile = modFile;
		this.graphstype = graphstype;
		this.queue = queue;
		this.nodeMap = nodeMap;
		this.linkMap = linkMap;
		this.reconfig = reconfig;
		this.considersSecCost = considersSecCost;
		this.reconfigType = reconfigType;
	}

	// Full Greedy performs requirement checking
	public Worker(String graphstype, SubstrateNetwork subNet, ArrayList<VirtualNetwork> virNets,
			PriorityQueue<Event> queue, String modFile, String nodeMap, String linkMap, boolean reconfig,
			boolean considersSecCost, String reconfigType, boolean checkReq) {
		this.subNet = subNet;
		this.virNets = virNets;
		this.modFile = modFile;
		this.graphstype = graphstype;
		this.queue = queue;
		this.nodeMap = nodeMap;
		this.linkMap = linkMap;
		this.reconfig = reconfig;
		this.considersSecCost = considersSecCost;
		this.reconfigType = reconfigType;
		this.checkReq = checkReq;
	}
	
	// Joao Paulino, 48490 (For D-ViNE, which uses 2 different modFiles)
	public Worker(String graphstype, SubstrateNetwork subNet, ArrayList<VirtualNetwork> virNets,
			PriorityQueue<Event> queue, String modFile, String modFile2, String nodeMap, String linkMap,
			boolean reconfig, boolean considersSecCost, String reconfigType) {
		this.subNet = subNet;
		this.virNets = virNets;
		this.modFile = modFile;
		this.modFile2 = modFile2;
		this.graphstype = graphstype;
		this.queue = queue;
		this.nodeMap = nodeMap;
		this.linkMap = linkMap;
		this.reconfig = reconfig;
		this.considersSecCost = considersSecCost;
		this.reconfigType = reconfigType;
	}
	
	// -----------------
	
	@Override
	public void run() {
		System.out.println(graphstype + " started at " + Calendar.getInstance().getTime());
		System.out.println("Substrate Nodes = " + subNet.getNumOfNodes() + "; Number of requests = " + virNets.size());
		System.out.println("-----------------------------------//-----------------------------");
		// subNet.printToFile("./statistics/Dynamic/tests/" + graphstype +
		// "_preCPU.txt");

		ArrayList<OutputFileReader> fileReaders = new ArrayList<>();
		SubstrateManager subMngr = new SubstrateManager(subNet);
		if (reconfig)
			folder = "Dynamic";

		if (modFile == null) {
			ArrayList<Event> events = new ArrayList<>();

			while (!queue.isEmpty()) {
				events.add(queue.poll());
				fileReaders.add(new OutputFileReader());
			}

//			reconfiguration(fileReaders, virNets, events);
		} else {
			if (modFile.contains("SecDep_MIP")) {
				secDepMapping(fileReaders, subMngr);
			}
			if (modFile.contains("DViNE_MIP")) {
				dVineMapping(fileReaders, subMngr);
			}
			if (modFile.contains("UTILITY_HEURISTIC")) {
				utilityHeuristicMapping(fileReaders, subMngr);
			}
			if (modFile.contains("FULL_RANDOM_HEURISTIC")) {
				fullRandomHeuristicMapping(fileReaders, subMngr);
			}
			if (modFile.contains("PARTIAL_RANDOM_HEURISTIC")) {
				partialRandomHeuristicMapping(fileReaders, subMngr);
			}
			// Max's proposed D-ViNE.
			if (modFile.contains("DVINE_HEURISTIC")) {
				dVineHeuristicMapping(fileReaders, subMngr);
			}
			if (modFile.contains("FULLGREEDY_MCF_HEURISTIC")) {
				fullGreedyMapping(fileReaders, subMngr);
			}
		}

		subNet.printToFile("./statistics/Dynamic/tests/" + graphstype + "_postCPU.txt");
		System.out.println(graphstype + " finished at " + Calendar.getInstance().getTime());
	}

	// -------------------- SecDep should also be tested as the optimal solution (for comparison) -------------------------

	private void secDepMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		try {
			if (folder.equals(""))
				folder = "SecDep";

			FileWriter fileWriter = new FileWriter(
					"./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(
					"EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime\n");

			SecDepDatFileCreator secDepDatCreator = new SecDepDatFileCreator();

			int i = 0;

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();

				fileReaders.add(new OutputFileReader());

				if (curEvent.getEventType() == EventType.ARRIVE) {
					secDepDatCreator.createDatFile(
							"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + ".dat", subNet,
							virNets.get(i));
					boolean res = Utils.runGLPSOL(
							"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + ".dat", modFile,
							"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
					if (res) {
						fileReaders.get(i).collectAllInfo(virNets.get(i), subNet.getNumOfNodes(),
								"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
						if (fileReaders.get(i).wasAccepted()) {
							subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
									fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
									fileReaders.get(i).getNodesUsed(), UpdateMode.DECREMENT);
							queue.add(new Event(EventType.DEPART,
									virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));
						}
					} else {
						fileReaders.get(i).setWasAccepted(res);
					}
				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
							fileReaders.get(i).getNodesUsed(), UpdateMode.INCREMENT);
				}

				double totalRev = virNets.get(i).getRevenue();
				double totalCost = subNet.getSecDepCost(virNets.get(i), fileReaders.get(i).getwNodesUsed(),
						fileReaders.get(i).getwMappedNodes(), fileReaders.get(i).getbNodesUsed(),
						fileReaders.get(i).getbMappedNodes(), fileReaders.get(i).getwEdgesUsed(),
						fileReaders.get(i).getwMappedEdges(), fileReaders.get(i).getbEdgesUsed(),
						fileReaders.get(i).getbMappedEdges());
				double avgNU = subNet.getAverageNodeStress();
				double avgLU = subNet.getAverageLinkStress();

				writer.write(i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART"));
				writer.write("  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void dVineMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		try {
			if (folder.equals(""))
				folder = modFile.contains("ModifiedDViNE_MIP.mod") ? "ModifiedDViNE" : "DViNE";

			FileWriter fileWriter = new FileWriter(
					"./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(
					"EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime\n");

			DVineDatFileCreator dVineDatCreator = new DVineDatFileCreator();
			ArrayList<ArrayList<String>> tmp = new ArrayList<>();
			ArrayList<ArrayList<Pair<String>>> tmp2 = new ArrayList<>();

			int i = 0;

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();

				fileReaders.add(new OutputFileReader());
				tmp.add(new ArrayList<String>());
				tmp2.add(new ArrayList<Pair<String>>());

				if (curEvent.getEventType() == EventType.ARRIVE) {
					dVineDatCreator.createDatFile(
							"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + ".dat", subNet,
							virNets.get(i));
					boolean res = Utils.runGLPSOL(
							"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + ".dat", modFile,
							"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
					if (res) {
						fileReaders.get(i).collectAllInfo(virNets.get(i), subNet.getNumOfNodes(),
								"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
						if (fileReaders.get(i).wasAccepted()) {
							for (String s : fileReaders.get(i).getNodesUsed()) {
								tmp.get(i).add(Utils.convertToAlphabet(s));
							}
							for (Pair<String> s : fileReaders.get(i).getEdgesUsed()) {
								tmp2.get(i).add(new Pair<String>(Utils.convertToAlphabet(s.getLeft()),
										Utils.convertToAlphabet(s.getRight())));
							}
							subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
									tmp2.get(i), fileReaders.get(i).getMappedNodes(), tmp.get(i), UpdateMode.DECREMENT);
							queue.add(new Event(EventType.DEPART,
									virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));
						}
					} else
						fileReaders.get(i).setWasAccepted(res);
				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(), tmp2.get(i),
							fileReaders.get(i).getMappedNodes(), tmp.get(i), UpdateMode.INCREMENT);
				}

				double totalRev = virNets.get(i).getRevenue();
				double totalCost = subNet.getDvineCost(virNets.get(i), tmp2.get(i),
						fileReaders.get(i).getMappedEdges());
				double avgNU = subNet.getAverageNodeStress();
				double avgLU = subNet.getAverageLinkStress();

				writer.write(i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART"));
				writer.write("  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// -------------------- Used Algorithms ------------------------------------------

	private void fullRandomHeuristicMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		long startTimeTotal = System.currentTimeMillis();
		long startTime = 0, endTime = 0, endTimeTotal = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;
		long startBNodeMappingTime = 0;
		long startBLinkMappingTime = 0;
		long endBNodeMappingTime = 0;
		long endBLinkMappingTime = 0;

		try {
			if (folder.equals("")) folder = "Heuristic";

			FileWriter fileWriter = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t"
					+ "AvgLinkUtilization \t MIPExecTime \t nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout\n");

			ArrayList<OutputFileReader> filePartialReaders = new ArrayList<>();
			ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray = new ArrayList<LinkHeuristicInfo>();
			HeuristicSecDepDatFileCreator heuristicSecDepDatCreator = new HeuristicSecDepDatFileCreator();

			int counter = 0, i = 0;
			ArrayList<VirtualNetwork> acceptedVNs = new ArrayList<>();
			ArrayList<Event> acceptedEvents = new ArrayList<>();

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();
				counter++;

				curTime = curEvent.getTime();

				if (curEvent.getEventType() == EventType.ARRIVE) {
					fileReaders.add(new OutputFileReader());

					startTime = System.currentTimeMillis();

					SubstrateNetwork subNetAux = new SubstrateNetwork();
					Utils.populateSubstrateInfo(subNetAux, subNet);
					SubstrateManager subMngrAux = new SubstrateManager(subNetAux);

					startWNodeMappingTime = System.currentTimeMillis();
					mappedWorkingNodes = Utils.mappingFullRandomVirtualNodes(subNetAux, virNets.get(i), null, false);
					endWNodeMappingTime = System.currentTimeMillis();
					fileReaders.get(i).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

					String finalResult = "";

					if (mappedWorkingNodes != null) {
						for (int j = 0; j < virNets.get(i).getEdges().size(); j++) {
							virtualLinkHeuristicInfoArray.add(new LinkHeuristicInfo(j, virNets.get(i).getEdgeBw(j),
									virNets.get(i).getEdgeLatency(j)));
						}
						Collections.sort(virtualLinkHeuristicInfoArray);

						for (int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++) {
							filePartialReaders.add(new OutputFileReader());

							heuristicSecDepDatCreator.createDatFile(
									"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + k
											+ ".dat",
									subNetAux, virNets.get(i), mappedWorkingNodes,
									virtualLinkHeuristicInfoArray.get(k));

							startWLinkMappingTime = System.currentTimeMillis();
							String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
									+ graphstype + "/random_req" + i + "_" + k + ".dat", modFile);

							if (partialResult.equals("timeout")) {
								// Link mapping timeout
								fileReaders.get(i).setTimeout(true);
								fileReaders.get(i).setWasAccepted(false);
								linkFail++;
								break;
							} else {
								if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
										|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
										|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

									// Working links failed.
									fileReaders.get(i).setWasAccepted(false);
									finalResult += partialResult;
									linkFail++;
									break;
								} else {
									filePartialReaders.get(k).collectAllHeuristicWorkingInfoPartial(virNets.get(i),
											subNetAux.getNumOfNodes(), partialResult, subNetAux, mappedWorkingNodes);
									endWLinkMappingTime = System.currentTimeMillis();

									Utils.copyMappedEdges(mappedWorkingEdgesAux,
											filePartialReaders.get(k).getEdgesUsed());

									subMngrAux.updateSubstrateNetwork(virNets.get(i),
											filePartialReaders.get(k).getMappedEdges(),
											filePartialReaders.get(k).getEdgesUsed(),
											filePartialReaders.get(k).getBwEdgesUsed(), UpdateMode.DECREMENT);
									finalResult += partialResult;
								}
							}
						}
						fileReaders.get(i).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);
					} else {
						// Working nodes failed.
						fileReaders.get(i).setWasAccepted(false);
						nodeFail++;
					}

					if (fileReaders.get(i).wasAccepted() && virNets.get(i).getWantBackup()) {
						filePartialReaders.clear();

						startBNodeMappingTime = System.currentTimeMillis();
						mappedBackupNodes = Utils.mappingFullRandomVirtualNodes(subNetAux, virNets.get(i), mappedWorkingNodes, true);
						endBNodeMappingTime = System.currentTimeMillis();
						fileReaders.get(i).setNodeBMappingTime(endBNodeMappingTime - startBNodeMappingTime);

						if (mappedBackupNodes != null) {

							for (int p = 0; p < virtualLinkHeuristicInfoArray.size(); p++) {
								filePartialReaders.add(new OutputFileReader());

								heuristicSecDepDatCreator.createDatFile(
										"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + p
												+ ".dat",
										subNetAux, virNets.get(i), mappedBackupNodes,
										virtualLinkHeuristicInfoArray.get(p));

								startBLinkMappingTime = System.currentTimeMillis();
								String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
										+ graphstype + "/random_req" + i + "_" + p + ".dat", modFile);

								if (partialResult.equals("timeout")) {
									// Backup links timeout.
									fileReaders.get(i).setTimeout(true);
									fileReaders.get(i).setWasAccepted(false);
									linkFail++;
									break;
								} else {
									if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
											|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
											|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

										// Backup links failed.
										fileReaders.get(i).setWasAccepted(false);
										finalResult += partialResult;
										linkFail++;
										break;
									} else {
										filePartialReaders.get(p).collectAllHeuristicBackupInfoPartial(virNets.get(i),
												subNetAux.getNumOfNodes(), partialResult, subNetAux, mappedBackupNodes);
										endBLinkMappingTime = System.currentTimeMillis();
										Utils.copyMappedEdges(mappedBackupEdgesAux,
												filePartialReaders.get(p).getEdgesUsed());

										subMngrAux.updateSubstrateNetwork(virNets.get(i),
												filePartialReaders.get(p).getMappedEdges(),
												filePartialReaders.get(p).getEdgesUsed(),
												filePartialReaders.get(p).getBwEdgesUsed(), UpdateMode.DECREMENT);

										finalResult += partialResult;
									}
									Utils.copyMappedEdges(mappedBackupEdges, mappedBackupEdgesAux);

									if (mappedBackupEdgesAux != null) {
										mappedBackupEdgesAux.clear();
									}
								}
							}
							fileReaders.get(i).setLinkBMappingTime(endBLinkMappingTime - startBLinkMappingTime);

							Utils.writeFile(finalResult,
									"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
							endTime = System.currentTimeMillis();

							fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i), subNet.getNumOfNodes(),
									"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt",
									subNet, mappedWorkingNodes, mappedBackupNodes);

							fileReaders.get(i)
									.setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
											+ ((endTime - startTime) / 1000)));

							if (fileReaders.get(i).wasAccepted()) {
								subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
										fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
										fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
										UpdateMode.DECREMENT);

								queue.add(new Event(EventType.DEPART,
										virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

								acceptedVNs.add(virNets.get(i));
								acceptedEvents.add(curEvent);
								totalAccepted++;
							}
						} else {
							// Backup nodes failed.
							fileReaders.get(i).setWasAccepted(false);
							nodeFail++;
						}
					} else {
						if (fileReaders.get(i).wasAccepted()) {
							// Only working nodes and links were mapped.
							Utils.writeFile(finalResult,
									"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
							endTime = System.currentTimeMillis();

							fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i), subNet.getNumOfNodes(),
									"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt",
									subNet, mappedWorkingNodes, mappedBackupNodes);
							fileReaders.get(i)
									.setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
											+ ((endTime - startTime) / 1000)));

							if (fileReaders.get(i).wasAccepted()) {
								subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
										fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
										fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
										UpdateMode.DECREMENT);
								queue.add(new Event(EventType.DEPART,
										virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

								acceptedVNs.add(virNets.get(i));
								acceptedEvents.add(curEvent);
								totalAccepted++;
							}
						}
					}

					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();
					if (substrateSecLocNodeHeuristicInfoArrayMap != null)
						substrateSecLocNodeHeuristicInfoArrayMap.clear();
					if (virtualSecLocNodeHeuristicInfoArray != null)
						virtualSecLocNodeHeuristicInfoArray.clear();
					if (virtualLinkHeuristicInfoArray != null)
						virtualLinkHeuristicInfoArray.clear();
					if (filePartialReaders != null)
						filePartialReaders.clear();

				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
							fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
							UpdateMode.INCREMENT);

					acceptedEvents.remove(acceptedVNs.indexOf(virNets.get(i)));
					acceptedVNs.remove(virNets.get(i));
				}

				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;

				totalRev = virNets.get(i).getRevenue();
				totalCost = subNet.getSecDepCostHeuristic(virNets.get(i), fileReaders.get(i).getwNodesUsed(),
						fileReaders.get(i).getwMappedNodes(), fileReaders.get(i).getbNodesUsed(),
						fileReaders.get(i).getbMappedNodes(), fileReaders.get(i).getwEdgesUsed(),
						fileReaders.get(i).getwMappedEdges(), fileReaders.get(i).getBwEdgesUsed());

				avgNU = subNet.getAverageNodeStress();
				avgLU = subNet.getAverageLinkStress();

				writer.write(i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART"));

				writer.write("  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "  " + fileReaders.get(i).getNodeWMappingTime() + "  "
						+ fileReaders.get(i).getLinkWMappingTime() + "  " + fileReaders.get(i).getNodeBMappingTime()
						+ "  " + fileReaders.get(i).getLinkBMappingTime() + "  " + fileReaders.get(i).isTimeout()
						+ "\n");

				if (counter == PERIOD && reconfig) {
					reconfiguration(fileReaders, acceptedVNs, acceptedEvents);
					numReconfig++;
					counter = 0;
				}
			}
			writer.close();

			// Acceptance Rate / Node Fails / Link Fails
			FileWriter expData = new FileWriter("./utils/experienceData/" + graphstype + ".txt");
			expData.write("Acceptance Rate = " + totalAccepted / virNets.size() + "\n");
			expData.write("Number of Failures in Node Mapping = " + nodeFail + "\n");
			expData.write("Number of Failures in Link Mapping = " + linkFail + "\n");
			expData.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		endTimeTotal = System.currentTimeMillis();
		System.out.println("");
		System.out.println(
				"--------------------------------------------||-------------------------------------------------");
		System.out.println("Experiment " + graphstype + " (" + modFile + ") tooks " + (endTimeTotal - startTimeTotal)
				+ " milliseconds");
		
		removeDatFiles();
	}

	private void partialRandomHeuristicMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		long totalStartTime = System.currentTimeMillis();
		long startTime = 0, endTime = 0, endTimeTotal = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;
		long startBNodeMappingTime = 0;
		long startBLinkMappingTime = 0;
		long endBNodeMappingTime = 0;
		long endBLinkMappingTime = 0;

		try {
			if (folder.equals(""))
				folder = "Heuristic";

			FileWriter fileWriter = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
					+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout\n");

			ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray = new ArrayList<LinkHeuristicInfo>();
			HeuristicSecDepDatFileCreator heuristicSecDepDatCreator = new HeuristicSecDepDatFileCreator();
			ArrayList<OutputFileReader> filePartialReaders = new ArrayList<>();

			ArrayList<VirtualNetwork> acceptedVNs = new ArrayList<>();
			ArrayList<Event> acceptedEvents = new ArrayList<>();
			int counter = 0, i = 0;

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();
				counter++;

				curTime = curEvent.getTime();

				if (curEvent.getEventType() == EventType.ARRIVE) {
					fileReaders.add(new OutputFileReader());

					startTime = System.currentTimeMillis();
					String finalResult = "";

					SubstrateNetwork subNetAux = new SubstrateNetwork();
					Utils.populateSubstrateInfo(subNetAux, subNet);
					SubstrateManager subMngrAux = new SubstrateManager(subNetAux);

					startWNodeMappingTime = System.currentTimeMillis();
					mappedWorkingNodes = Utils.mappingPartialRandomVirtualNodes(subNetAux, virNets.get(i), false);
					endWNodeMappingTime = System.currentTimeMillis();
					fileReaders.get(i).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);
					
					if (mappedWorkingNodes != null) {
						for (int j = 0; j < virNets.get(i).getNumOfEdges(); j++) {
							virtualLinkHeuristicInfoArray.add(new LinkHeuristicInfo(j, virNets.get(i).getEdgeBw(j),
									virNets.get(i).getEdgeLatency(j)));
						}
						Collections.sort(virtualLinkHeuristicInfoArray); // Descending
						
						for (int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++) {
							filePartialReaders.add(new OutputFileReader());
							heuristicSecDepDatCreator.createDatFile(
									"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + k
											+ ".dat",
									subNetAux, virNets.get(i), mappedWorkingNodes,
									virtualLinkHeuristicInfoArray.get(k));

							startWLinkMappingTime = System.currentTimeMillis();
							String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
									+ graphstype + "/random_req" + i + "_" + k + ".dat", modFile);

							if (partialResult.equals("timeout")) {
								// Link mapping timeout
								fileReaders.get(i).setTimeout(true);
								fileReaders.get(i).setWasAccepted(false);
								linkFail++;
								break;
							} else {
								if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
										|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
										|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

									// Link mapping failed
									fileReaders.get(i).setWasAccepted(false);
									finalResult += partialResult;
									linkFail++;
									break;
								} else {
									filePartialReaders.get(k).collectAllHeuristicWorkingInfoPartial(virNets.get(i),
											subNetAux.getNumOfNodes(), partialResult, subNetAux, mappedWorkingNodes);
									endWLinkMappingTime = System.currentTimeMillis();

									Utils.copyMappedEdges(mappedWorkingEdgesAux,
											filePartialReaders.get(k).getEdgesUsed());

									subMngrAux.updateSubstrateNetwork(virNets.get(i),
											filePartialReaders.get(k).getMappedEdges(),
											filePartialReaders.get(k).getEdgesUsed(),
											filePartialReaders.get(k).getBwEdgesUsed(), UpdateMode.DECREMENT);

									finalResult += partialResult;
								}

								Utils.copyMappedEdges(mappedWorkingEdges, mappedWorkingEdgesAux);
								if (mappedWorkingEdgesAux != null) {
									mappedWorkingEdgesAux.clear();
								}
							}
						}
						fileReaders.get(i).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);
						
						// Working links mapped successfully
						if (fileReaders.get(i).wasAccepted() && virNets.get(i).getWantBackup()
								&& (mappedWorkingNodes != null)) {

							filePartialReaders.clear();
							Utils.disableWorkingNodesAndLinks(subNetAux, mappedWorkingNodes, mappedWorkingEdges);

							startBNodeMappingTime = System.currentTimeMillis();
							mappedBackupNodes = Utils.mappingPartialRandomVirtualNodes(subNetAux, virNets.get(i), true);
							endBNodeMappingTime = System.currentTimeMillis();
							fileReaders.get(i).setNodeBMappingTime(endBNodeMappingTime - startBNodeMappingTime);

							// Backup nodes mapped successfully
							if (mappedBackupNodes != null) {
								for (int p = 0; p < virtualLinkHeuristicInfoArray.size(); p++) {
									filePartialReaders.add(new OutputFileReader());
									heuristicSecDepDatCreator.createDatFile(
											"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + p
													+ "_backup.dat",
											subNetAux, virNets.get(i), mappedBackupNodes,
											virtualLinkHeuristicInfoArray.get(p));

									startBLinkMappingTime = System.currentTimeMillis();
									String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
											+ graphstype + "/random_req" + i + "_" + p + "_backup.dat", modFile);

									if (partialResult.equals("timeout")) {
										// Link mapping timeout
										fileReaders.get(i).setTimeout(true);
										fileReaders.get(i).setWasAccepted(false);
										linkFail++;
										break;
									} else {
										if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
												|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
												|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

											// Link mapping failed.
											fileReaders.get(i).setWasAccepted(false);
											finalResult += partialResult;
											linkFail++;
											break;
										} else {
											filePartialReaders.get(p).collectAllHeuristicBackupInfoPartial(
													virNets.get(i), subNetAux.getNumOfNodes(), partialResult, subNetAux,
													mappedBackupNodes);
											endBLinkMappingTime = System.currentTimeMillis();

											Utils.copyMappedEdges(mappedBackupEdgesAux,
													filePartialReaders.get(p).getEdgesUsed());

											subMngrAux.updateSubstrateNetwork(virNets.get(i),
													filePartialReaders.get(p).getMappedEdges(),
													filePartialReaders.get(p).getEdgesUsed(),
													filePartialReaders.get(p).getBwEdgesUsed(), UpdateMode.DECREMENT);

											finalResult += partialResult;
										}

										Utils.copyMappedEdges(mappedBackupEdges, mappedBackupEdgesAux);
										if (mappedBackupEdgesAux != null) {
											mappedBackupEdgesAux.clear();
										}
									}
								}
								fileReaders.get(i).setLinkBMappingTime(endBLinkMappingTime - startBLinkMappingTime);

								if (mappedBackupNodes != null && fileReaders.get(i).wasAccepted()) {

									Utils.writeFile(finalResult, "./glpk/outputFiles/" + folder + "/" + graphstype
											+ "/random_req" + i + ".txt");
									endTime = System.currentTimeMillis();

									fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i),
											subNet.getNumOfNodes(), "./glpk/outputFiles/" + folder + "/" + graphstype
													+ "/random_req" + i + ".txt",
											subNet, mappedWorkingNodes, mappedBackupNodes);
									fileReaders.get(i).setExecutionTime(
											"" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
													+ ((endTime - startTime) / 1000)));

									// Backup links mapped successfully
									if (fileReaders.get(i).wasAccepted()) {
										subMngr.updateSubstrateNetwork(virNets.get(i),
												fileReaders.get(i).getMappedEdges(), fileReaders.get(i).getEdgesUsed(),
												fileReaders.get(i).getMappedNodes(), fileReaders.get(i).getNodesUsed(),
												fileReaders.get(i).getBwEdgesUsed(), UpdateMode.DECREMENT);

										queue.add(new Event(EventType.DEPART,
												virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

										acceptedVNs.add(virNets.get(i));
										acceptedEvents.add(curEvent);
										totalAccepted++;
									}
								}
							} else {
								// Backup node mapping failed
								fileReaders.get(i).setWasAccepted(false);
								nodeFail++;
							}
						} else {
							if ((mappedWorkingNodes != null) && fileReaders.get(i).wasAccepted()) {

								Utils.writeFile(finalResult,
										"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
								endTime = System.currentTimeMillis();

								fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i), subNet.getNumOfNodes(),
										"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt",
										subNet, mappedWorkingNodes, mappedBackupNodes);
								fileReaders.get(i)
										.setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
												+ ((endTime - startTime) / 1000)));
								if (fileReaders.get(i).wasAccepted()) {
									subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
											fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
											fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
											UpdateMode.DECREMENT);

									queue.add(new Event(EventType.DEPART,
											virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

									acceptedVNs.add(virNets.get(i));
									acceptedEvents.add(curEvent);
									totalAccepted++;
								}
							}
						}
					} else {
						// Node mapping failed
						fileReaders.get(i).setWasAccepted(false);
						nodeFail++;
					}

					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();
					if (virtualLinkHeuristicInfoArray != null)
						virtualLinkHeuristicInfoArray.clear();

				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
							fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
							UpdateMode.INCREMENT);

					acceptedEvents.remove(acceptedVNs.indexOf(virNets.get(i)));
					acceptedVNs.remove(virNets.get(i));
				}

				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;

				totalRev = virNets.get(i).getRevenue();

				totalCost = subNet.getSecDepCostHeuristic(virNets.get(i), fileReaders.get(i).getwNodesUsed(),
						fileReaders.get(i).getwMappedNodes(), fileReaders.get(i).getbNodesUsed(),
						fileReaders.get(i).getbMappedNodes(), fileReaders.get(i).getwEdgesUsed(),
						fileReaders.get(i).getwMappedEdges(), fileReaders.get(i).getBwEdgesUsed());

				avgNU = subNet.getAverageNodeStress();
				avgLU = subNet.getAverageLinkStress();

				writer.write(i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART"));

				writer.write("  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "  " + fileReaders.get(i).getNodeWMappingTime() + "  "
						+ fileReaders.get(i).getLinkWMappingTime() + "  " + fileReaders.get(i).getNodeBMappingTime()
						+ "  " + fileReaders.get(i).getLinkBMappingTime() + "  " + fileReaders.get(i).isTimeout()
						+ "\n");

				if (counter == PERIOD && reconfig) {
					reconfiguration(fileReaders, acceptedVNs, acceptedEvents);
					numReconfig++;
					counter = 0;
				}
			}
			writer.close();

			// Acceptance Rate / Node Fails / Link Fails
			FileWriter expData = new FileWriter("./utils/experienceData/" + graphstype + ".txt");
			expData.write("Acceptance Rate = " + totalAccepted / virNets.size() + "\n");
			expData.write("Number of Failures in Node Mapping = " + nodeFail + "\n");
			expData.write("Number of Failures in Link Mapping = " + linkFail + "\n");
			expData.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		endTimeTotal = System.currentTimeMillis();
		System.out.println("");
		System.out.println(
				"--------------------------------------------||-------------------------------------------------");
		System.out.println("Experiment " + graphstype + " (" + modFile + ") tooks " + (endTimeTotal - totalStartTime)
				+ " milliseconds");
		
		removeDatFiles();
	}

	private void utilityHeuristicMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		long startTimeTotal = System.currentTimeMillis();
		long startTime = 0, endTime = 0, endTimeTotal = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;
		long startBNodeMappingTime = 0;
		long startBLinkMappingTime = 0;
		long endBNodeMappingTime = 0;
		long endBLinkMappingTime = 0;

		try {
			if (folder.equals("")) folder = "Heuristic";

			FileWriter fileWriter = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
					+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout \t MediumPathLength\n");
			
			ArrayList<OutputFileReader> filePartialReaders = new ArrayList<>();
			ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray = new ArrayList<LinkHeuristicInfo>();

			HeuristicSecDepDatFileCreator heuristicSecDepDatCreator = new HeuristicSecDepDatFileCreator();

			ArrayList<VirtualNetwork> acceptedVNs = new ArrayList<>();
			ArrayList<Event> acceptedEvents = new ArrayList<>();
			int counter = 0, i = 0;

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();
				counter++;

				curTime = curEvent.getTime();

				if (curEvent.getEventType() == EventType.ARRIVE) {
					
					fileReaders.add(new OutputFileReader());

					startTime = System.currentTimeMillis();

					SubstrateNetwork subNetAux = new SubstrateNetwork();
					Utils.populateSubstrateInfo(subNetAux, subNet);
					SubstrateManager subMngrAuxHeuristic = new SubstrateManager(subNetAux);

					startWNodeMappingTime = System.currentTimeMillis();
					
					//if not reversed, then it is in descending order
					virtualSecLocNodeHeuristicInfoArray = Utils.calculateVirtualUtility(virNets.get(i),
							securityValuesVirtual, locationValuesVirtual, nodeMap);
					substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateSubstrateUtility(subNetAux,
								securityValuesSubstrate, locationValuesSubstrate, nodeMap);
					
					mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(substrateSecLocNodeHeuristicInfoArrayMap,
							virtualSecLocNodeHeuristicInfoArray, subNetAux, virNets.get(i), securityValuesSubstrate,
							locationValuesSubstrate, false);

					endWNodeMappingTime = System.currentTimeMillis();
					fileReaders.get(i).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

					String finalResult = "";
					if (mappedWorkingNodes != null) {

						for (int j = 0; j < virNets.get(i).getNumOfEdges(); j++)
							virtualLinkHeuristicInfoArray.add(new LinkHeuristicInfo(j, virNets.get(i).getEdgeBw(j), virNets.get(i).getEdgeLatency(j)));

						Collections.sort(virtualLinkHeuristicInfoArray);
						
						for (int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++) {
							filePartialReaders.add(new OutputFileReader());

							heuristicSecDepDatCreator.createDatFile("./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + k + ".dat",
									subNetAux, virNets.get(i), mappedWorkingNodes, virtualLinkHeuristicInfoArray.get(k));

							startWLinkMappingTime = System.currentTimeMillis();
							String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
									+ graphstype + "/random_req" + i + "_" + k + ".dat", modFile);

							if (partialResult.equals("timeout")) {
								fileReaders.get(i).setTimeout(true);
								fileReaders.get(i).setWasAccepted(false);
								
								linkFail++;
								break;
							} else {
								if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
										|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
										|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {
									
									fileReaders.get(i).setWasAccepted(false);
									finalResult += partialResult;

									// Link mapping failed.
									linkFail++;
									break;
								} else {
									filePartialReaders.get(k).collectAllHeuristicWorkingInfoPartial(virNets.get(i),
											subNetAux.getNumOfNodes(), partialResult, subNetAux, mappedWorkingNodes);
									endWLinkMappingTime = System.currentTimeMillis();

									Utils.copyMappedEdges(mappedWorkingEdgesAux,
											filePartialReaders.get(k).getEdgesUsed());

									subMngrAuxHeuristic.updateSubstrateNetwork(virNets.get(i),
											filePartialReaders.get(k).getMappedEdges(),
											filePartialReaders.get(k).getEdgesUsed(),
											filePartialReaders.get(k).getBwEdgesUsed(), UpdateMode.DECREMENT);
									finalResult += partialResult;
								}

								Utils.copyMappedEdges(mappedWorkingEdges, mappedWorkingEdgesAux);
								
								if (mappedWorkingEdgesAux != null) {
									switch (mappedWorkingEdgesAux.size()) {
									case 1: link1++;
										break;
									case 2: link2++;
										break;
									case 3: link3++;
										break;
									default: link4++;
										break;
									}
									totalLinks++;
									
									mappedWorkingEdgesAux.clear();
								}
							}
						}
						fileReaders.get(i).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);
						
						if(fileReaders.get(i).wasAccepted() && virNets.get(i).getWantBackup() && (mappedWorkingNodes != null)){
							
							filePartialReaders.clear();
							Utils.disableWorkingNodesAndLinks(subNetAux, mappedWorkingNodes, mappedWorkingEdges);

							startBNodeMappingTime = System.currentTimeMillis();
							
							substrateSecLocNodeHeuristicInfoArrayMap.clear();
							substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateSubstrateUtility(subNetAux,
									securityValuesSubstrate, locationValuesSubstrate, nodeMap);

							mappedBackupNodes = Utils.mappingVirtualWorkingNodes(substrateSecLocNodeHeuristicInfoArrayMap,
									virtualSecLocNodeHeuristicInfoArray, subNetAux, virNets.get(i), securityValuesSubstrate,
									locationValuesSubstrate, true);

							endBNodeMappingTime = System.currentTimeMillis();
							fileReaders.get(i).setNodeBMappingTime(endBNodeMappingTime - startBNodeMappingTime);

							if (mappedBackupNodes != null) {

								for (int p = 0; p < virtualLinkHeuristicInfoArray.size(); p++) {
									filePartialReaders.add(new OutputFileReader());
									heuristicSecDepDatCreator.createDatFile(
											"./glpk/datFiles/" + folder + "/" + graphstype + "/random_req" + i + "_" + p
													+ "_backup.dat",
											subNetAux, virNets.get(i), mappedBackupNodes,
											virtualLinkHeuristicInfoArray.get(p));

									startBLinkMappingTime = System.currentTimeMillis();
									String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/"
											+ graphstype + "/random_req" + i + "_" + p + "_backup.dat", modFile);

									if (partialResult.equals("timeout")) {
										fileReaders.get(i).setTimeout(true);
										fileReaders.get(i).setWasAccepted(false);

										// Backup link timeout.
										linkFail++;
										break;
									} else {
										if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
												|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
												|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

											fileReaders.get(i).setWasAccepted(false);
											finalResult += partialResult;

											// Backup link mapping failed.
											linkFail++;
											break;
										} else {
											filePartialReaders.get(p).collectAllHeuristicBackupInfoPartial(virNets.get(i),
													subNetAux.getNumOfNodes(), partialResult, subNetAux, mappedBackupNodes);
											endBLinkMappingTime = System.currentTimeMillis();

											Utils.copyMappedEdges(mappedBackupEdgesAux,
													filePartialReaders.get(p).getEdgesUsed());

											subMngrAuxHeuristic.updateSubstrateNetwork(virNets.get(i),
													filePartialReaders.get(p).getMappedEdges(),
													filePartialReaders.get(p).getEdgesUsed(),
													filePartialReaders.get(p).getBwEdgesUsed(), UpdateMode.DECREMENT);

											finalResult += partialResult;
										}

										Utils.copyMappedEdges(mappedBackupEdges, mappedBackupEdgesAux);
										if (mappedBackupEdgesAux != null) {
											mappedBackupEdgesAux.clear();
										}
									}
								}
								fileReaders.get(i).setLinkBMappingTime(endBLinkMappingTime - startBLinkMappingTime);

								if(mappedBackupNodes != null && fileReaders.get(i).wasAccepted()){
									Utils.writeFile(finalResult, "./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
									endTime = System.currentTimeMillis();

									fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i), subNet.getNumOfNodes(),
											"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt",
											subNet, mappedWorkingNodes, mappedBackupNodes);
									fileReaders.get(i).setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
													+ ((endTime - startTime) / 1000)));

									if (fileReaders.get(i).wasAccepted()) {
										subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
												fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
												fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
												UpdateMode.DECREMENT);

										queue.add(new Event(EventType.DEPART,
												virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

										acceptedVNs.add(virNets.get(i));
										acceptedEvents.add(curEvent);
										totalAccepted++;
									}
								}
							} else {
								fileReaders.get(i).setWasAccepted(false);

								// Backup node failed.
								nodeFail++;
							}
						} else {
							if((mappedWorkingNodes != null) && fileReaders.get(i).wasAccepted()){
								Utils.writeFile(finalResult, "./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt");
								endTime = System.currentTimeMillis();

								fileReaders.get(i).collectAllHeuristicInfoMCF(virNets.get(i), subNet.getNumOfNodes(),
										"./glpk/outputFiles/" + folder + "/" + graphstype + "/random_req" + i + ".txt",
										subNet, mappedWorkingNodes, mappedBackupNodes);
								fileReaders.get(i)
										.setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
												+ ((endTime - startTime) / 1000)));

								if (fileReaders.get(i).wasAccepted()) {
									subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
											fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
											fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
											UpdateMode.DECREMENT);

									queue.add(new Event(EventType.DEPART,
											virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

									acceptedVNs.add(virNets.get(i));
									acceptedEvents.add(curEvent);
									totalAccepted++;
								}
							}
						}
					} else {
						fileReaders.get(i).setWasAccepted(false);
						nodeFail++;
					}

					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();
					if (substrateSecLocNodeHeuristicInfoArrayMap != null)
						substrateSecLocNodeHeuristicInfoArrayMap.clear();
					if (virtualSecLocNodeHeuristicInfoArray != null)
						virtualSecLocNodeHeuristicInfoArray.clear();
					if (virtualLinkHeuristicInfoArray != null)
						virtualLinkHeuristicInfoArray.clear();
					
				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getEdgesUsed(), fileReaders.get(i).getMappedNodes(),
							fileReaders.get(i).getNodesUsed(), fileReaders.get(i).getBwEdgesUsed(),
							UpdateMode.INCREMENT);

					acceptedEvents.remove(acceptedVNs.indexOf(virNets.get(i)));
					acceptedVNs.remove(virNets.get(i));
				}

				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;
				String s = "";

				totalRev = virNets.get(i).getRevenue();

				totalCost = subNet.getSecDepCostHeuristic(virNets.get(i), fileReaders.get(i).getwNodesUsed(),
						fileReaders.get(i).getwMappedNodes(), fileReaders.get(i).getbNodesUsed(),
						fileReaders.get(i).getbMappedNodes(), fileReaders.get(i).getwEdgesUsed(),
						fileReaders.get(i).getwMappedEdges(), fileReaders.get(i).getBwEdgesUsed());

				avgNU = subNet.getAverageNodeStress();
				avgLU = subNet.getAverageLinkStress();
				
				s += i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART");
				
				s += "  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "  " + fileReaders.get(i).getNodeWMappingTime() + "  "
						+ fileReaders.get(i).getLinkWMappingTime() + "  " + fileReaders.get(i).getNodeBMappingTime()
						+ "  " + fileReaders.get(i).getLinkBMappingTime() + "  " + fileReaders.get(i).isTimeout()
						+ (fileReaders.get(i).getEdgesUsed().size()/virNets.get(i).getNumOfEdges()) + "\n";

				writer.write(s);
				finalString.add(s);

				if(!fileReaders.get(i).wasAccepted())
					rejectedEvents.add(curEvent);
				
				if (counter == PERIOD && reconfig) {
					reconfiguration(fileReaders, acceptedVNs, acceptedEvents);
					numReconfig++;
					counter = 0;
				}
			}
			writer.close();
			
			//File including reconfiguration
			FileWriter fileWriterRec = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + "_Rec.txt");
			BufferedWriter writerRec = new BufferedWriter(fileWriterRec);
			writerRec.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
					+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout \t MediumPathLength\n");
			
			for(int k = 0; k < finalString.size(); k++){
				writerRec.write(finalString.get(k));
			}
			
			writerRec.close();

			double totalPathLength = 0;
			
			for(int f = 0; f < fileReaders.size(); f++){
				if(fileReaders.get(f).wasAccepted()){
					totalPathLength += (fileReaders.get(f).getEdgesUsed().size()/virNets.get(f).getNumOfEdges());
				}
			}
			
			// Acceptance Rate / Node Fails / Link Fails
			// Medium Path Length only concerns working edges
			FileWriter expData = new FileWriter("./utils/experienceData/" + graphstype + ".txt");
			expData.write("Acceptance Rate = " + totalAccepted / virNets.size() + "\n");
			expData.write("Number of Failures in Node Mapping = " + nodeFail + "\n");
			expData.write("Number of Failures in Link Mapping = " + linkFail + "\n");
			expData.write("Number of Reconfigurations = " + numReconfig + "\n");
			expData.write("Number of Failed Reconfigurations = " + failedRec + "\n");
			expData.write("Number of Links with size 1: " + link1 + "\n");
			expData.write("Number of Links with size 2: " + link2 + "\n");
			expData.write("Number of Links with size 3: " + link3 + "\n");
			expData.write("Number of Links with size 4+: " + link4 + "\n");
			expData.write("Total Number of Mapped Links: " + totalLinks + "\n");
			expData.write("Medium Path Length = " + totalPathLength/totalAccepted);
			expData.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		endTimeTotal = System.currentTimeMillis();
		System.out.println("");
		System.out.println("--------------------------------------------||-------------------------------------------------");
		System.out.println("Experiment " + graphstype + " (" + modFile + ") tooks " + (endTimeTotal - startTimeTotal)
				+ " milliseconds.");
		
		removeDatFiles();
	}
	
	private void fullGreedyMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		long startTimeTotal = System.currentTimeMillis();
		long startTime = 0, endTime = 0, endTimeTotal = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;

		try {
			if (folder.equals(""))
				folder = "Heuristic";

			FileWriter fileWriter = new FileWriter(
					"./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(
					"EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
							+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout \t MediumPathLength\n");

			HeuristicFullGreedyDatFileCreator heuristicFullGreedyDatCreator = new HeuristicFullGreedyDatFileCreator();

			ArrayList<ArrayList<String>> usedNodesPerRequest = new ArrayList<>();
			ArrayList<ArrayList<Pair<String>>> usedEdgesPerRequest = new ArrayList<>();

			ArrayList<VirtualNetwork> acceptedVNs = new ArrayList<>();
			ArrayList<Event> acceptedEvents = new ArrayList<>();
			int counter = 0, i = 0;

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();
				counter++;

				curTime = curEvent.getTime();

				if (curEvent.getEventType() == EventType.ARRIVE) {
					fileReaders.add(new OutputFileReader());

					usedNodesPerRequest.add(new ArrayList<String>());
					usedEdgesPerRequest.add(new ArrayList<Pair<String>>());

					if(!virNets.get(i).getWantBackup()){
						startTime = System.currentTimeMillis();
						startWNodeMappingTime = System.currentTimeMillis();
						
						ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic = Utils.calculateGreedy(subNet);
						ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic = Utils.calculateGreedyDesc(virNets.get(i));
						
						mappedWorkingNodes = Utils.mappingFullGreedyVirtualNodes(subNet, virNets.get(i), substrateNodeGreedyHeuristic, virtualNodeGreedyHeuristic, checkReq);
						
						endWNodeMappingTime = System.currentTimeMillis();
						fileReaders.get(i).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

						if (mappedWorkingNodes != null) {
							heuristicFullGreedyDatCreator.createDatFile(
									"./glpk/datFiles/" + folder + "/" + graphstype + "/randomMCF_req" + i + ".dat", subNet,
									virNets.get(i), mappedWorkingNodes);

							startWLinkMappingTime = System.currentTimeMillis();

							String partialResult = Utils.runGLPSOLHeuristic(
									"./glpk/datFiles/" + folder + "/" + graphstype + "/randomMCF_req" + i + ".dat",
									modFile);

							if (partialResult.equals("timeout")) {
								// Link timeout.
								fileReaders.get(i).setTimeout(true);
								fileReaders.get(i).setWasAccepted(false);
								linkFail++;
							} else {
								if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
										|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
										|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

									// Link mapping failed.
									fileReaders.get(i).setWasAccepted(false);
									linkFail++;
								} else {
									Utils.writeFile(partialResult, "./glpk/outputFiles/" + folder + "/" + graphstype
											+ "/randomMCF_req" + i + ".txt");

									fileReaders.get(i).collectFullGreedyHeuristicInfo(
											virNets.get(i), subNet.getNumOfNodes(), "./glpk/outputFiles/" + folder + "/"
													+ graphstype + "/randomMCF_req" + i + ".txt",
											subNet, mappedWorkingNodes);
									endWLinkMappingTime = System.currentTimeMillis();
									
									fileReaders.get(i).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);

									if (fileReaders.get(i).wasAccepted()) {

										for (String s : fileReaders.get(i).getNodesUsed()) {
											usedNodesPerRequest.get(i).add(Utils.convertToAlphabet(s));
										}

										for (Pair<String> s : fileReaders.get(i).getEdgesUsed()) {
											usedEdgesPerRequest.get(i).add(new Pair<String>(Utils.convertToAlphabet(s.getLeft()),
															Utils.convertToAlphabet(s.getRight())));
										}

										subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
												usedEdgesPerRequest.get(i), fileReaders.get(i).getMappedNodes(),
												usedNodesPerRequest.get(i), fileReaders.get(i).getBwEdgesUsed(),
												UpdateMode.DECREMENT);

										queue.add(new Event(EventType.DEPART,
												virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

										acceptedVNs.add(virNets.get(i));
										acceptedEvents.add(curEvent);
										totalAccepted++;
									}
									endTime = System.currentTimeMillis();
									fileReaders.get(i)
											.setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
													+ ((endTime - startTime) / 1000)));
								}
							}
						} else {
							// Node mapping failed.
							fileReaders.get(i).setWasAccepted(false);
							nodeFail++;
						}
					} else {
						fileReaders.get(i).setWasAccepted(false);
						nodeFail++;
					}

					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();

				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							usedEdgesPerRequest.get(i), fileReaders.get(i).getMappedNodes(), usedNodesPerRequest.get(i),
							fileReaders.get(i).getBwEdgesUsed(), UpdateMode.INCREMENT);

					acceptedEvents.remove(acceptedVNs.indexOf(virNets.get(i)));
					acceptedVNs.remove(virNets.get(i));
				}
				
				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;
				String s = "";

				if (!considersSecCost) {
					totalRev = virNets.get(i).getNoSecRevenue();
					totalCost = subNet.getDvineAndFullGreedyHeuNoSecCost(virNets.get(i),
							fileReaders.get(i).getMappedNodes(), usedNodesPerRequest.get(i), usedEdgesPerRequest.get(i),
							fileReaders.get(i).getMappedEdges(), fileReaders.get(i).getBwEdgesUsed());
				} else {
					totalRev = virNets.get(i).getDvineAndFullGreedyHeuRevenue();
					totalCost = subNet.getDvineAndFullGreedyHeuCost(virNets.get(i), fileReaders.get(i).getMappedNodes(),
							usedNodesPerRequest.get(i), usedEdgesPerRequest.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getBwEdgesUsed());
				}

				avgNU = subNet.getAverageNodeStress();
				avgLU = subNet.getAverageLinkStress();
				
				s += i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART");
				
				s += "  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "  " + fileReaders.get(i).getNodeWMappingTime() + "  "
						+ fileReaders.get(i).getLinkWMappingTime() + "  " + fileReaders.get(i).getNodeBMappingTime()
						+ "  " + fileReaders.get(i).getLinkBMappingTime() + "  " + fileReaders.get(i).isTimeout()
						+ (fileReaders.get(i).getEdgesUsed().size()/virNets.get(i).getNumOfEdges()) + "\n";

				writer.write(s);
				finalString.add(s);

				if (counter == PERIOD && reconfig) {
					fullGreedyReconfiguration(fileReaders, acceptedVNs, acceptedEvents, usedNodesPerRequest,
							usedEdgesPerRequest);
					numReconfig++;
					counter = 0;
				}
			}
			writer.close();
			
			//File including reconfiguration
			FileWriter fileWriterRec = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + "_Rec.txt");
			BufferedWriter writerRec = new BufferedWriter(fileWriterRec);
			writerRec.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
					+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout \t MediumPathLength\n");
			
			for(int k = 0; k < finalString.size(); k++){
				writerRec.write(finalString.get(k));
			}
			
			writerRec.close();

			double totalPathLength = 0;
			
			for(int f = 0; f < fileReaders.size(); f++){
				if(fileReaders.get(f).wasAccepted()){
					totalPathLength += (fileReaders.get(f).getEdgesUsed().size()/virNets.get(f).getNumOfEdges());
				}
			}
			
			// Acceptance Rate / Node Fails / Link Fails
			// Medium Path Length only concerns working edges
			FileWriter expData = new FileWriter("./utils/experienceData/" + graphstype + ".txt");
			expData.write("Acceptance Rate = " + totalAccepted / virNets.size() + "\n");
			expData.write("Number of Failures in Node Mapping = " + nodeFail + "\n");
			expData.write("Number of Failures in Link Mapping = " + linkFail + "\n");
			expData.write("Number of Reconfigurations = " + numReconfig + "\n");
			expData.write("Number of Failed Reconfigurations = " + failedRec + "\n");
			expData.write("Medium Path Length = " + totalPathLength/totalAccepted);
			expData.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		endTimeTotal = System.currentTimeMillis();
		System.out.println("");
		System.out.println(
				"--------------------------------------------||-------------------------------------------------");
		System.out.println("Experiment " + graphstype + " (" + modFile + ") tooks " + (endTimeTotal - startTimeTotal)
				+ " milliseconds.");
		
		removeDatFiles();
	}

	private void dVineHeuristicMapping(ArrayList<OutputFileReader> fileReaders, SubstrateManager subMngr) {
		long startTimeTotal = System.currentTimeMillis();
		long startTime = 0, endTime = 0, endTimeTotal = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;

		try {
			if (folder.equals(""))
				folder = "Heuristic";

			FileWriter fileWriter = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + ".txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			
			writer.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t "
					+ "AvgLinkUtilization \t MIPExecTime \t nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout"
					+ " \t MediumPathLength\n");
			
			DVineDatFileCreator dVineDatCreator = new DVineDatFileCreator();
			HeuristicDVineDatFileCreator heuristicDVineDatCreator = new HeuristicDVineDatFileCreator();

			ArrayList<ArrayList<String>> usedNodes = new ArrayList<>();
			ArrayList<ArrayList<Pair<String>>> usedEdges = new ArrayList<>();

			int counter = 0, i = 0;
			ArrayList<VirtualNetwork> acceptedVNs = new ArrayList<>();
			ArrayList<Event> acceptedEvents = new ArrayList<>();

			while (!queue.isEmpty()) {
				Event curEvent = queue.poll();
				i = curEvent.getIndex();
				counter++;

				curTime = curEvent.getTime();

				usedNodes.add(new ArrayList<String>());
				usedEdges.add(new ArrayList<Pair<String>>());

				if (curEvent.getEventType() == EventType.ARRIVE) {
					
					fileReaders.add(new OutputFileReader());
					
					if(!virNets.get(i).getWantBackup()){
						startTime = System.currentTimeMillis();
						dVineDatCreator.createDatFile("./glpk/datFiles/" + folder + "/" + graphstype + "/randomLP_req" + i + ".dat", subNet,
								virNets.get(i));

						startWNodeMappingTime = System.currentTimeMillis();
						// Creating the clusters and calculate pz values for each substrate node.
						String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/" + graphstype + "/randomLP_req" + i + ".dat", modFile);

						if (partialResult.equals("timeout")) {
							// Timeout
							fileReaders.get(i).setTimeout(true);
							fileReaders.get(i).setWasAccepted(false);
							nodeFail++;
						} else {
							Utils.writeFile(partialResult, "./glpk/outputFiles/" + folder + "/" + graphstype + "/randomLP_req" + i + ".txt");

							if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
									|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
									|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

								// pz values failed
								fileReaders.get(i).setWasAccepted(false);
								nodeFail++;
							} else {
								HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();

								virtualNodeIndexArraySubstrateNodeInfoMap = fileReaders.get(i).populateDVineHeuristicWorkingInfo(virNets.get(i), 
										subNet.getNumOfNodes(), partialResult, subNet);

								mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(virtualNodeIndexArraySubstrateNodeInfoMap, subNet);
								endWNodeMappingTime = System.currentTimeMillis();

								fileReaders.get(i).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

								if (mappedWorkingNodes != null) {
									heuristicDVineDatCreator.createDatFile("./glpk/datFiles/" + folder + "/" + graphstype + "/randomMCF_req" + i + ".dat",
											subNet, virNets.get(i), mappedWorkingNodes);

									startWLinkMappingTime = System.currentTimeMillis();
									String finalResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/" + folder + "/" + graphstype +
											"/randomMCF_req" + i + ".dat", modFile2);

									if (finalResult.equals("timeout")) {
										// Link mapping timeout
										fileReaders.get(i).setTimeout(true);
										fileReaders.get(i).setWasAccepted(false);
										linkFail++;
									} else {
										Utils.writeFile(finalResult, "./glpk/outputFiles/" + folder + "/" + graphstype
												+ "/randomMCF_req" + i + ".txt");

										if (finalResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
												|| finalResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
												|| finalResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

											// Link mapping failed
											fileReaders.get(i).setWasAccepted(false);
											linkFail++;
										} else {
											fileReaders.get(i).collectDVineHeuristicInfo(virNets.get(i), subNet.getNumOfNodes(),
													"./glpk/outputFiles/" + folder + "/" + graphstype + "/randomMCF_req" + i + ".txt",
													subNet, mappedWorkingNodes);
											endWLinkMappingTime = System.currentTimeMillis();

											fileReaders.get(i).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);

											if (fileReaders.get(i).wasAccepted()) {
												for (String s : fileReaders.get(i).getNodesUsed())
													usedNodes.get(i).add(Utils.convertToAlphabet(s));

												for (Pair<String> s : fileReaders.get(i).getEdgesUsed())
													usedEdges.get(i).add(new Pair<String>(Utils.convertToAlphabet(s.getLeft()),
															Utils.convertToAlphabet(s.getRight())));

												subMngr.updateSubstrateNetwork(virNets.get(i),
														fileReaders.get(i).getMappedEdges(), usedEdges.get(i),
														fileReaders.get(i).getMappedNodes(), usedNodes.get(i),
														fileReaders.get(i).getBwEdgesUsed(), UpdateMode.DECREMENT);

												queue.add(new Event(EventType.DEPART,
														virNets.get(i).getArrival() + virNets.get(i).getDuration(), i));

												acceptedVNs.add(virNets.get(i));
												acceptedEvents.add(curEvent);
												totalAccepted++;
											}
											endTime = System.currentTimeMillis();
											fileReaders.get(i).setExecutionTime("" + (Double.valueOf(fileReaders.get(i).getExecutionTime())
													+ ((endTime - startTime) / 1000)));
										}
									}
								} else {
									// Node Mapping failed
									fileReaders.get(i).setWasAccepted(false);
									nodeFail++;
								}
							}
						}
					} else {
						// If a request requires backup D-Vine cannot satisfy it
						fileReaders.get(i).setWasAccepted(false);
					}

					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();

				} else if (curEvent.getEventType() == EventType.DEPART) {
					subMngr.updateSubstrateNetwork(virNets.get(i), fileReaders.get(i).getMappedEdges(),
							usedEdges.get(i), fileReaders.get(i).getMappedNodes(), usedNodes.get(i),
							fileReaders.get(i).getBwEdgesUsed(), UpdateMode.INCREMENT);

					acceptedEvents.remove(acceptedVNs.indexOf(virNets.get(i)));
					acceptedVNs.remove(virNets.get(i));
				}

				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;
				String s = "";

				if (!considersSecCost) {
					totalRev = virNets.get(i).getNoSecRevenue();
					totalCost = subNet.getDvineAndFullGreedyHeuNoSecCost(virNets.get(i),
							fileReaders.get(i).getMappedNodes(), usedNodes.get(i), usedEdges.get(i),
							fileReaders.get(i).getMappedEdges(), fileReaders.get(i).getBwEdgesUsed());
				} else {
					totalRev = virNets.get(i).getDvineAndFullGreedyHeuRevenue();
					totalCost = subNet.getDvineAndFullGreedyHeuCost(virNets.get(i), fileReaders.get(i).getMappedNodes(),
							usedNodes.get(i), usedEdges.get(i), fileReaders.get(i).getMappedEdges(),
							fileReaders.get(i).getBwEdgesUsed());
				}

				avgNU = subNet.getAverageNodeStress();
				avgLU = subNet.getAverageLinkStress();
				
				s += i + "  " + curEvent.getTime() + "  " + virNets.get(i).getDuration() + "  "
						+ (curEvent.getEventType() == EventType.ARRIVE ? "ARRIVE" : "DEPART");
				
				s += "  " + fileReaders.get(i).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + fileReaders.get(i).getExecutionTime()
						+ "  " + fileReaders.get(i).getNodeWMappingTime() + "  "
						+ fileReaders.get(i).getLinkWMappingTime() + "  " + fileReaders.get(i).getNodeBMappingTime()
						+ "  " + fileReaders.get(i).getLinkBMappingTime() + "  " + fileReaders.get(i).isTimeout()
						+ (fileReaders.get(i).getEdgesUsed().size()/virNets.get(i).getNumOfEdges()) + "\n";

				writer.write(s);
				finalString.add(s);

				if (counter == PERIOD && reconfig) {
					dVineReconfiguration(fileReaders, acceptedVNs, acceptedEvents, usedNodes, usedEdges);
					numReconfig++;
					counter = 0;
				}
			}
			writer.close();

			//File including reconfiguration
			FileWriter fileWriterRec = new FileWriter("./statistics/" + folder + "/" + graphstype + "/output_s" + subNet.getNumOfNodes() + "_Rec.txt");
			BufferedWriter writerRec = new BufferedWriter(fileWriterRec);
			writerRec.write("EventIndex \t Time \t Duration \t EventType \t Accepted \t TotalRev \t TotalCost \t AvgNodeUtilization \t AvgLinkUtilization \t MIPExecTime \t "
					+ "nodeWMappingTime \t linkWMappingTime \t nodeBMappingTime \t linkBMappingTime \t Timeout \t MediumPathLength\n");
			
			for(int k = 0; k < finalString.size(); k++){
				writerRec.write(finalString.get(k));
			}
			
			writerRec.close();

			double totalPathLength = 0;
			
			for(int f = 0; f < fileReaders.size(); f++){
				if(fileReaders.get(f).wasAccepted()){
					totalPathLength += (fileReaders.get(f).getEdgesUsed().size()/virNets.get(f).getNumOfEdges());
				}
			}
			
			// Acceptance Rate / Node Fails / Link Fails
			// Medium Path Length only concerns working edges
			FileWriter expData = new FileWriter("./utils/experienceData/" + graphstype + ".txt");
			expData.write("Acceptance Rate = " + totalAccepted / virNets.size() + "\n");
			expData.write("Number of Failures in Node Mapping = " + nodeFail + "\n");
			expData.write("Number of Failures in Link Mapping = " + linkFail + "\n");
			expData.write("Number of Reconfigurations = " + numReconfig + "\n");
			expData.write("Number of Failed Reconfigurations = " + failedRec + "\n");
			expData.write("Medium Path Length = " + totalPathLength/totalAccepted);
			expData.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		endTimeTotal = System.currentTimeMillis();
		System.out.println("");
		System.out.println("--------------------------------------------||-------------------------------------------------");
		System.out.println("Experiment " + graphstype + " (" + modFile + ") tooks " + (endTimeTotal - startTimeTotal) + " milliseconds");
		
		removeDatFiles();
	}
	
	// -------------------- Reconfiguration Algorithm ------------------------------------------

	/**
	 * Joao Paulino, 48490
	 * 
	 * @param virNets
	 *            - This array contains the currently mapped virtual networks in
	 *            the substrate network
	 * @param writer 
	 * @throws IOException 
	 */
	private void reconfiguration(ArrayList<OutputFileReader> fileReaders, ArrayList<VirtualNetwork> virNets,
			ArrayList<Event> events) throws IOException {

		String output = "";
		
		boolean rejected = false;

		Comparator<VirtualNetworkEvent> comp = null;

		switch (reconfigType) {
		case "OPTIMAL": // Knowing how much time the requests will stay in the network, we can use those values to map them according to how much time to live they have left.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return (vn2.getVirNet().getDuration() - (curTime - vn2.getEvent().getTime()))
							- (vn1.getVirNet().getDuration() - (curTime - vn1.getEvent().getTime()));
				}
			};
			break;
		case "RECENT": // The last ones to arrive are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn2.getVirNet().getArrival() - vn1.getVirNet().getArrival();
				}
			};
			break;
		case "SIZE": // Requests with less nodes are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
				}
			};
			break;
		case "UTILITY": // Requests with lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {	
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) { 
					return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork()); 
				}
			};
			break;
		case "SIZE_UTILITY": // Requests with less nodes and lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int	compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					int sComp = vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
					
					if (sComp != 0) 
						return sComp;
					else 
						return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork());
				}
			};
			break;
		case "PATH":
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getArrival() - vn2.getVirNet().getArrival();
				}
			};
			break;
		default:
			break;
		}
		
		PriorityQueue<VirtualNetworkEvent> vne = new PriorityQueue<>(comp);
		for (int q = 0; q < virNets.size(); q++) {
			vne.add(new VirtualNetworkEvent(virNets.get(q), events.get(q)));
		}

		// Any problem just replace tmpSub here with subNet and delete the following two lines
		SubstrateNetwork tmpSub = new SubstrateNetwork(subNet);
		tmpSub.resetSubstrateNetwork();
		SubstrateManager subMan = new SubstrateManager(tmpSub);
		
		// Event indexes are used to manipulate the correct file readers
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<OutputFileReader> tmpFileReaders = new ArrayList<OutputFileReader>();

		long startTime = System.currentTimeMillis();
		long endTime = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;
		long startBNodeMappingTime = 0;
		long startBLinkMappingTime = 0;
		long endBNodeMappingTime = 0;
		long endBLinkMappingTime = 0;

		VirtualNetworkEvent virtEvent;
		VirtualNetwork vNet;
		int i;

		ArrayList<OutputFileReader> filePartialReaders = new ArrayList<OutputFileReader>();
		ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray = new ArrayList<LinkHeuristicInfo>();
		HeuristicSecDepDatFileCreator heuristicSecDepDatCreator = new HeuristicSecDepDatFileCreator();
		
		ArrayList<String> tmpRec = new ArrayList<>();
		ArrayList<VirtualNetworkEvent> rejectedEvents = new ArrayList<>();
		
		for (int j = 0; j < virNets.size(); j++) {
			
			if(!vne.isEmpty()){
				virtEvent = vne.peek();
			} else {
				virtEvent = rejectedEvents.get(0);
			}
			
			vNet = virtEvent.getVirNet();
			i = virtEvent.getEvent().getIndex();

			indexes.add(i);
			tmpFileReaders.add(new OutputFileReader());
			String finalResult = "";

			SubstrateNetwork auxSub = new SubstrateNetwork();
			Utils.populateSubstrateInfo(auxSub, tmpSub);
			SubstrateManager auxMan = new SubstrateManager(auxSub);

			startWNodeMappingTime = System.currentTimeMillis();

			switch (nodeMap) {
			case "PARTIAL_RANDOM":
				mappedWorkingNodes = Utils.mappingPartialRandomVirtualNodes(auxSub, vNet, false);
				break;
			case "FULL_RANDOM":
				mappedWorkingNodes = Utils.mappingFullRandomVirtualNodes(auxSub, vNet, null, false);
				break;
			// Utility and other variations
			default:
				
				substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateSubstrateUtility(auxSub,
						securityValuesSubstrate, locationValuesSubstrate, nodeMap);
				virtualSecLocNodeHeuristicInfoArray = Utils.calculateVirtualUtility(vNet, securityValuesVirtual,
						locationValuesVirtual, nodeMap);
				
				if(reconfigType.equals("PATH")){
					mappedWorkingNodes = Utils.mappingVirtualWorkingNodesSP(substrateSecLocNodeHeuristicInfoArrayMap,
							virtualSecLocNodeHeuristicInfoArray, auxSub, vNet, securityValuesSubstrate,
							locationValuesSubstrate, false);
				} else {
					mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(substrateSecLocNodeHeuristicInfoArrayMap,
							virtualSecLocNodeHeuristicInfoArray, auxSub, vNet, securityValuesSubstrate,
							locationValuesSubstrate, false);
				}
				
				break;
			}

			endWNodeMappingTime = System.currentTimeMillis();
			tmpFileReaders.get(j).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

			// Working nodes were mapped successfully
			if (mappedWorkingNodes != null) {
				switch (linkMap) {
				case "MCF":
					startWLinkMappingTime = System.currentTimeMillis();

					for (int k = 0; k < vNet.getNumOfEdges(); k++) {
						virtualLinkHeuristicInfoArray
								.add(new LinkHeuristicInfo(k, vNet.getEdgeBw(k), vNet.getEdgeLatency(k)));
					}
					Collections.sort(virtualLinkHeuristicInfoArray);

					for (int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++) {
						filePartialReaders.add(new OutputFileReader());

						heuristicSecDepDatCreator.createDatFile(
								"./glpk/datFiles/Dynamic/" + graphstype + "/random_req" + i + "_" + k + ".dat", auxSub,
								vNet, mappedWorkingNodes, virtualLinkHeuristicInfoArray.get(k));

						String partialResult = Utils.runGLPSOLHeuristic(
								"./glpk/datFiles/Dynamic/" + graphstype + "/random_req" + i + "_" + k + ".dat",
								modFile);

						if (partialResult.equals("timeout")) {
							tmpFileReaders.get(j).setTimeout(true);
							tmpFileReaders.get(j).setWasAccepted(false);
							break;
						} else {
							if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
									|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
									|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {
								
								tmpFileReaders.get(j).setWasAccepted(false);
								finalResult += partialResult;
								break;
							} else {
								filePartialReaders.get(k).collectAllHeuristicWorkingInfoPartial(vNet,
										auxSub.getNumOfNodes(), partialResult, auxSub, mappedWorkingNodes);
								Utils.copyMappedEdges(mappedWorkingEdgesAux, filePartialReaders.get(k).getEdgesUsed());

								auxMan.updateSubstrateNetwork(vNet, filePartialReaders.get(k).getMappedEdges(),
										filePartialReaders.get(k).getEdgesUsed(),
										filePartialReaders.get(k).getBwEdgesUsed(), UpdateMode.DECREMENT);
								finalResult += partialResult;
							}
							Utils.copyMappedEdges(mappedWorkingEdges, mappedWorkingEdgesAux);
							
							if (mappedWorkingEdgesAux != null) {
								switch (mappedWorkingEdgesAux.size()) {
								case 1: link1++;
									break;
								case 2: link2++;
									break;
								case 3: link3++;
									break;
								default: link4++;
									break;
								}
								totalLinks++;
								
								mappedWorkingEdgesAux.clear();
							}
						}
					}
					endWLinkMappingTime = System.currentTimeMillis();
					tmpFileReaders.get(j).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);

					// Working links mapped successfully and wants backup
					if (tmpFileReaders.get(j).wasAccepted() && vNet.getWantBackup()) {

						filePartialReaders.clear();
						Utils.disableWorkingNodesAndLinks(auxSub, mappedWorkingNodes, mappedWorkingEdges);

						startBNodeMappingTime = System.currentTimeMillis();

						switch (nodeMap) {
						case "PARTIAL_RANDOM":
							mappedBackupNodes = Utils.mappingPartialRandomVirtualNodes(auxSub, vNet, true);
							break;
						case "FULL_RANDOM":
							mappedBackupNodes = Utils.mappingFullRandomVirtualNodes(auxSub, vNet, mappedWorkingNodes,
									true);
						default:
							substrateSecLocNodeHeuristicInfoArrayMap.clear();
							substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateSubstrateUtility(auxSub,
									securityValuesSubstrate, locationValuesSubstrate, nodeMap);

							mappedBackupNodes = Utils.mappingVirtualWorkingNodes(
									substrateSecLocNodeHeuristicInfoArrayMap, virtualSecLocNodeHeuristicInfoArray,
									auxSub, vNet, securityValuesSubstrate, locationValuesSubstrate, true);
							break;
						}
						endBNodeMappingTime = System.currentTimeMillis();
						tmpFileReaders.get(j).setNodeBMappingTime(endBNodeMappingTime - startBNodeMappingTime);

						// Backup nodes were mapped successfully
						if (mappedBackupNodes != null) {
							startBLinkMappingTime = System.currentTimeMillis();

							for (int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++) {
								filePartialReaders.add(new OutputFileReader());

								heuristicSecDepDatCreator.createDatFile(
										"./glpk/datFiles/Dynamic/" + graphstype + "/random_req" + i + "_" + k
												+ "_backup.dat",
										auxSub, vNet, mappedBackupNodes, virtualLinkHeuristicInfoArray.get(k));

								String partialResult = Utils.runGLPSOLHeuristic("./glpk/datFiles/Dynamic/" + graphstype
										+ "/random_req" + i + "_" + k + "_backup.dat", modFile);

								if (partialResult.equals("timeout")) {

									tmpFileReaders.get(j).setTimeout(true);
									tmpFileReaders.get(j).setWasAccepted(false);
									break;
								} else {
									if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
											|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
											|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

										tmpFileReaders.get(j).setWasAccepted(false);
										finalResult += partialResult;
										break;
									} else {
										filePartialReaders.get(k).collectAllHeuristicBackupInfoPartial(vNet,
												auxSub.getNumOfNodes(), partialResult, auxSub, mappedBackupNodes);
										Utils.copyMappedEdges(mappedBackupEdgesAux,
												filePartialReaders.get(k).getEdgesUsed());

										auxMan.updateSubstrateNetwork(vNet, filePartialReaders.get(k).getMappedEdges(),
												filePartialReaders.get(k).getEdgesUsed(),
												filePartialReaders.get(k).getBwEdgesUsed(), UpdateMode.DECREMENT);
										finalResult += partialResult;
									}
									Utils.copyMappedEdges(mappedBackupEdges, mappedBackupEdgesAux);
									if (mappedBackupEdgesAux != null) {
										mappedBackupEdgesAux.clear();
									}
								}
							}
							endBLinkMappingTime = System.currentTimeMillis();
							tmpFileReaders.get(j).setLinkBMappingTime(endBLinkMappingTime - startBLinkMappingTime);

							// Backup links were mapped successfully
							if (tmpFileReaders.get(j).wasAccepted()) {
								Utils.writeFile(finalResult,
										"./glpk/outputFiles/Dynamic/" + graphstype + "/random_req" + i + ".txt");

								endTime = System.currentTimeMillis();
								tmpFileReaders.get(j).collectAllHeuristicInfoMCF(vNet, tmpSub.getNumOfNodes(),
										"./glpk/outputFiles/Dynamic/" + graphstype + "/random_req" + i + ".txt", tmpSub,
										mappedWorkingNodes, mappedBackupNodes);
								tmpFileReaders.get(j)
										.setExecutionTime("" + (Double.valueOf(tmpFileReaders.get(j).getExecutionTime())
												+ ((endTime - startTime) / 1000)));

								subMan.updateSubstrateNetwork(vNet, tmpFileReaders.get(j).getMappedEdges(),
										tmpFileReaders.get(j).getEdgesUsed(), tmpFileReaders.get(j).getMappedNodes(),
										tmpFileReaders.get(j).getNodesUsed(), tmpFileReaders.get(j).getBwEdgesUsed(),
										UpdateMode.DECREMENT);
							}
						} else {
							tmpFileReaders.get(j).setWasAccepted(false);
						}
					} else {
						if (tmpFileReaders.get(j).wasAccepted()) {
							Utils.writeFile(finalResult,
									"./glpk/outputFiles/Dynamic/" + graphstype + "/random_req" + i + ".txt");

							endTime = System.currentTimeMillis();

							tmpFileReaders.get(j).collectAllHeuristicInfoMCF(vNet, tmpSub.getNumOfNodes(),
									"./glpk/outputFiles/Dynamic/" + graphstype + "/random_req" + i + ".txt", tmpSub,
									mappedWorkingNodes, mappedBackupNodes);
							tmpFileReaders.get(j)
									.setExecutionTime("" + (Double.valueOf(tmpFileReaders.get(j).getExecutionTime())
											+ ((endTime - startTime) / 1000)));

							subMan.updateSubstrateNetwork(vNet, tmpFileReaders.get(j).getMappedEdges(),
									tmpFileReaders.get(j).getEdgesUsed(), tmpFileReaders.get(j).getMappedNodes(),
									tmpFileReaders.get(j).getNodesUsed(), tmpFileReaders.get(j).getBwEdgesUsed(),
									UpdateMode.DECREMENT);
						}
					}
					if (mappedWorkingNodes != null)
						mappedWorkingNodes.clear();
					if (mappedWorkingEdges != null)
						mappedWorkingEdges.clear();
					if (mappedBackupNodes != null)
						mappedBackupNodes.clear();
					if (mappedBackupEdges != null)
						mappedBackupEdges.clear();
					if (substrateSecLocNodeHeuristicInfoArrayMap != null)
						substrateSecLocNodeHeuristicInfoArrayMap.clear();
					if (virtualSecLocNodeHeuristicInfoArray != null)
						virtualSecLocNodeHeuristicInfoArray.clear();
					if (virtualLinkHeuristicInfoArray != null)
						virtualLinkHeuristicInfoArray.clear();
					filePartialReaders.clear();
					break;
				default:
					break;
				}
			} else {
				tmpFileReaders.get(j).setWasAccepted(false);
			}

			if (!tmpFileReaders.get(j).wasAccepted()) {
				if(!vne.isEmpty()){
					vne.poll();
					j--;
					indexes.remove(indexes.size()-1);
					tmpFileReaders.remove(tmpFileReaders.size()-1);
					
					rejectedEvents.add(virtEvent);
				} else {
					rejected = true;
					break;
				}
			} else {
				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;

				totalRev = vNet.getRevenue();

				totalCost = tmpSub.getSecDepCostHeuristic(vNet, tmpFileReaders.get(j).getwNodesUsed(),
						tmpFileReaders.get(j).getwMappedNodes(), tmpFileReaders.get(j).getbNodesUsed(),
						tmpFileReaders.get(j).getbMappedNodes(), tmpFileReaders.get(j).getwEdgesUsed(),
						tmpFileReaders.get(j).getwMappedEdges(), tmpFileReaders.get(j).getBwEdgesUsed());

				avgNU = tmpSub.getAverageNodeStress();
				avgLU = tmpSub.getAverageLinkStress();

				output += i + "  " + curTime + "  " + vNet.getDuration() + "  "
						+ "RECONFIG";

				output += "  " + tmpFileReaders.get(j).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + tmpFileReaders.get(j).getExecutionTime()
						+ "  " + tmpFileReaders.get(j).getNodeWMappingTime() + "  "
						+ tmpFileReaders.get(j).getLinkWMappingTime() + "  " + tmpFileReaders.get(j).getNodeBMappingTime()
						+ "  " + tmpFileReaders.get(j).getLinkBMappingTime() + "  " + tmpFileReaders.get(j).isTimeout()
						+ (tmpFileReaders.get(j).getEdgesUsed().size()/vNet.getNumOfEdges()) + "\n";
				
				tmpRec.add(output);
				
				if(!vne.isEmpty())
					vne.poll();
				else
					rejectedEvents.remove(0);
			}
		}
		
		if (!rejected) {
			subNet.setCPUBW(tmpSub);
			
			for (int k = 0; k < tmpFileReaders.size(); k++){
				fileReaders.set(indexes.get(k), tmpFileReaders.get(k));
			}
			
			success = true;
			
			for(String line: tmpRec){
				for(int s = 0; s < finalString.size(); s++){
					String[] tmp = finalString.get(s).split("\\s+");
					if(tmp[0].equals(line.split("\\s+")[0]) && (tmp[3].equals("ARRIVE") || tmp[3].equals("RECONFIG"))){
						finalString.remove(s);
						break;
					}
				}
				finalString.add(line);
			}
			
		} else {
			failedRec++;
			System.out.println("FAILED REC");
		}
	}

	private void fullGreedyReconfiguration(ArrayList<OutputFileReader> fileReaders, ArrayList<VirtualNetwork> virNets,
			ArrayList<Event> events, ArrayList<ArrayList<String>> usedNodesPerRequest,
			ArrayList<ArrayList<Pair<String>>> usedEdgesPerRequest) {

		boolean rejected = false;
		String output = "";

		Comparator<VirtualNetworkEvent> comp = null;

		switch (reconfigType) {
		case "OPTIMAL": // Knowing how much time the requests will stay in the network,
			// we can use those values to map them according to how much time to live they have left.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return (vn2.getVirNet().getDuration() - (curTime - vn2.getEvent().getTime()))
							- (vn1.getVirNet().getDuration() - (curTime - vn1.getEvent().getTime()));
				}
			};
			break;
		case "RECENT": // The last ones to arrive are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn2.getVirNet().getArrival() - vn1.getVirNet().getArrival();
				}
			};
			break;
		case "SIZE": // Requests with less nodes are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
				}
			};
			break;
		case "UTILITY": // Requests with lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {	
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) { 
					return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork()); 
				}
			};
			break;
		case "SIZE_UTILITY": // Requests with less nodes and lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int	compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					int sComp = vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
					
					if (sComp != 0) 
						return sComp;
					else 
						return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork());
				}
			};
			break;
		case "PATH":
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getArrival() - vn2.getVirNet().getArrival();
				}
			};
			break;
		default:
			break;
		}

		PriorityQueue<VirtualNetworkEvent> vne = new PriorityQueue<>(comp);
		for (int q = 0; q < virNets.size(); q++) {
			vne.add(new VirtualNetworkEvent(virNets.get(q), events.get(q)));
		}

		// Any problem just replace tmpSub here with subNet and delete the
		// following two lines
		SubstrateNetwork tmpSub = new SubstrateNetwork(subNet);
		tmpSub.resetSubstrateNetwork();
		SubstrateManager subMan = new SubstrateManager(tmpSub);

		// Event indexes are used to manipulate the correct file readers
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<OutputFileReader> tmpFileReaders = new ArrayList<OutputFileReader>();

		ArrayList<ArrayList<String>> tmpUsedNodes = new ArrayList<>();
		ArrayList<ArrayList<Pair<String>>> tmpUsedEdges = new ArrayList<>();

		long startTime = System.currentTimeMillis();
		long endTime = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;

		VirtualNetworkEvent virtEvent;
		VirtualNetwork vNet;
		int i;

		HeuristicFullGreedyDatFileCreator heuristicFullGreedyDatCreator = new HeuristicFullGreedyDatFileCreator();

		ArrayList<String> tmpRec = new ArrayList<>();
		ArrayList<VirtualNetworkEvent> rejectedEvents = new ArrayList<>();
		
		System.out.println("---------------------------------- RECONFIG -----------------------------------------");
		System.out.println("Num Virtual Networks: " + virNets.size());
		
		for (int j = 0; j < virNets.size(); j++) {
			
			if(!vne.isEmpty()){
				virtEvent = vne.peek();
			} else {
				virtEvent = rejectedEvents.get(0);
			}
			
			vNet = virtEvent.getVirNet();
			i = virtEvent.getEvent().getIndex();

			indexes.add(i);
			tmpFileReaders.add(new OutputFileReader());
			tmpUsedNodes.add(new ArrayList<String>());
			tmpUsedEdges.add(new ArrayList<Pair<String>>());

			startWNodeMappingTime = System.currentTimeMillis();

			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic = Utils.calculateGreedy(tmpSub);
			ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic = Utils.calculateGreedyDesc(vNet);

			if(reconfigType.equals("PATH")){
				mappedWorkingNodes = Utils.mappingFullGreedyVirtualNodesSP(tmpSub, vNet, substrateNodeGreedyHeuristic,
						virtualNodeGreedyHeuristic, checkReq);
			} else {
				mappedWorkingNodes = Utils.mappingFullGreedyVirtualNodes(tmpSub, vNet, substrateNodeGreedyHeuristic,
						virtualNodeGreedyHeuristic, checkReq);
			}

			endWNodeMappingTime = System.currentTimeMillis();
			tmpFileReaders.get(j).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

			if (mappedWorkingNodes != null) {
				switch (linkMap) {
				case "MCF":
					heuristicFullGreedyDatCreator.createDatFile(
							"./glpk/datFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".dat", tmpSub, vNet,
							mappedWorkingNodes);

					startWLinkMappingTime = System.currentTimeMillis();

					String partialResult = Utils.runGLPSOLHeuristic(
							"./glpk/datFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".dat", modFile);

					if (partialResult.equals("timeout")) {
						tmpFileReaders.get(j).setTimeout(true);
						tmpFileReaders.get(j).setWasAccepted(false);
					} else {
						if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
								|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
								|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

							tmpFileReaders.get(j).setWasAccepted(false);
						} else {
							Utils.writeFile(partialResult,
									"./glpk/outputFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".txt");

							tmpFileReaders.get(j).collectFullGreedyHeuristicInfo(vNet, tmpSub.getNumOfNodes(),
									"./glpk/outputFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".txt", tmpSub,
									mappedWorkingNodes);

							endWLinkMappingTime = System.currentTimeMillis();
							tmpFileReaders.get(j).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);

							if (tmpFileReaders.get(j).wasAccepted()) {

								for (String s : tmpFileReaders.get(j).getNodesUsed()) {
									tmpUsedNodes.get(j).add(Utils.convertToAlphabet(s));
								}

								for (Pair<String> s : tmpFileReaders.get(j).getEdgesUsed()) {
									tmpUsedEdges.get(j).add(new Pair<String>(Utils.convertToAlphabet(s.getLeft()),
											Utils.convertToAlphabet(s.getRight())));
								}

								subMan.updateSubstrateNetwork(vNet, tmpFileReaders.get(j).getMappedEdges(),
										tmpUsedEdges.get(j), tmpFileReaders.get(j).getMappedNodes(),
										tmpUsedNodes.get(j), tmpFileReaders.get(j).getBwEdgesUsed(),
										UpdateMode.DECREMENT);
							}
							endTime = System.currentTimeMillis();
							tmpFileReaders.get(j)
									.setExecutionTime("" + (Double.valueOf(tmpFileReaders.get(j).getExecutionTime())
											+ ((endTime - startTime) / 1000)));
						}
					}
					break;
				default:
					break;
				}
			} else {
				tmpFileReaders.get(j).setWasAccepted(false);
			}

			if (mappedWorkingNodes != null)
				mappedWorkingNodes.clear();
			if (mappedWorkingEdges != null)
				mappedWorkingEdges.clear();
			if (mappedBackupNodes != null)
				mappedBackupNodes.clear();
			if (mappedBackupEdges != null)
				mappedBackupEdges.clear();

			
			if (!tmpFileReaders.get(j).wasAccepted()) {
				if(!vne.isEmpty()){
					vne.poll();
					j--;
					indexes.remove(indexes.size()-1);
					tmpFileReaders.remove(tmpFileReaders.size()-1);
					tmpUsedEdges.remove(tmpUsedEdges.size()-1);
					tmpUsedNodes.remove(tmpUsedNodes.size()-1);
					
					rejectedEvents.add(virtEvent);
				} else {
					rejected = true;
					break;
				}
			} else {
				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;

				if (!considersSecCost) {
					totalRev = vNet.getNoSecRevenue();
					totalCost = tmpSub.getDvineAndFullGreedyHeuNoSecCost(vNet,
							tmpFileReaders.get(j).getMappedNodes(), tmpUsedNodes.get(j), tmpUsedEdges.get(j),
							tmpFileReaders.get(j).getMappedEdges(), tmpFileReaders.get(j).getBwEdgesUsed());
				} else {
					totalRev = vNet.getDvineAndFullGreedyHeuRevenue();
					totalCost = tmpSub.getDvineAndFullGreedyHeuCost(vNet, tmpFileReaders.get(j).getMappedNodes(),
							tmpUsedNodes.get(j), tmpUsedEdges.get(j), tmpFileReaders.get(j).getMappedEdges(),
							tmpFileReaders.get(j).getBwEdgesUsed());
				}

				avgNU = tmpSub.getAverageNodeStress();
				avgLU = tmpSub.getAverageLinkStress();

				output += i + "  " + curTime + "  " + vNet.getDuration() + "  "
						+ "RECONFIG";

				output += "  " + tmpFileReaders.get(j).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + tmpFileReaders.get(j).getExecutionTime()
						+ "  " + tmpFileReaders.get(j).getNodeWMappingTime() + "  "
						+ tmpFileReaders.get(j).getLinkWMappingTime() + "  " + tmpFileReaders.get(j).getNodeBMappingTime()
						+ "  " + tmpFileReaders.get(j).getLinkBMappingTime() + "  " + tmpFileReaders.get(j).isTimeout()
						+ (tmpFileReaders.get(j).getEdgesUsed().size()/vNet.getNumOfEdges()) + "\n";
				
				tmpRec.add(output);
				
				if(!vne.isEmpty())
					vne.poll();
				else
					rejectedEvents.remove(0);
			}
		}

		if (!rejected) {
			subNet.setCPUBW(tmpSub);
			
			for (int k = 0; k < tmpFileReaders.size(); k++){
				fileReaders.set(indexes.get(k), tmpFileReaders.get(k));
				usedNodesPerRequest.set(indexes.get(k), tmpUsedNodes.get(k));
				usedEdgesPerRequest.set(indexes.get(k), tmpUsedEdges.get(k));
			}
			success = true;
			
			for(String line: tmpRec){
				for(int s = 0; s < finalString.size(); s++){
					String[] tmp = finalString.get(s).split("\\s+");
					if(tmp[0].equals(line.split("\\s+")[0]) && (tmp[3].equals("ARRIVE") || tmp[3].equals("RECONFIG"))){
						finalString.remove(s);
						break;
					}
				}
				finalString.add(line);
			}
			
		} else {
			failedRec++;
			System.out.println("FAILED REC");
		}
		
		System.out.println("---------------------------------- END RECONFIG -----------------------------------------");
	}

	private void dVineReconfiguration(ArrayList<OutputFileReader> fileReaders, ArrayList<VirtualNetwork> virNets,
			ArrayList<Event> events, ArrayList<ArrayList<String>> usedNodes,
			ArrayList<ArrayList<Pair<String>>> usedEdges) {

		String output = "";
		
		boolean rejected = false;

		Comparator<VirtualNetworkEvent> comp = null;

		switch (reconfigType) {
		case "OPTIMAL": // Knowing how much time the requests will stay in the network,
			// we can use those values to map them according to how much time to live they have left.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return (vn2.getVirNet().getDuration() - (curTime - vn2.getEvent().getTime()))
							- (vn1.getVirNet().getDuration() - (curTime - vn1.getEvent().getTime()));
				}
			};
			break;
		case "RECENT": // The last ones to arrive are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn2.getVirNet().getArrival() - vn1.getVirNet().getArrival();
				}
			};
			break;
		case "SIZE": // Requests with less nodes are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
				}
			};
			break;
		case "UTILITY": // Requests with lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {	
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) { 
					return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork()); 
				}
			};
			break;
		case "SIZE_UTILITY": // Requests with less nodes and lower utility of the network are the first to be mapped.
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int	compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					int sComp = vn1.getVirNet().getNumOfNodes() - vn2.getVirNet().getNumOfNodes();
					
					if (sComp != 0) 
						return sComp;
					else 
						return Double.compare(vn1.getVirNet().getUtilityNetwork(), vn2.getVirNet().getUtilityNetwork());
				}
			};
			break;
		case "PATH":
			comp = new Comparator<VirtualNetworkEvent>() {
				@Override
				public int compare(VirtualNetworkEvent vn1, VirtualNetworkEvent vn2) {
					return vn1.getVirNet().getArrival() - vn2.getVirNet().getArrival();
				}
			};
			break;
		default:
			break;
		}

		PriorityQueue<VirtualNetworkEvent> vne = new PriorityQueue<>(comp);
		for (int q = 0; q < virNets.size(); q++) {
			vne.add(new VirtualNetworkEvent(virNets.get(q), events.get(q)));
		}

		// tmpSub is equal to the original subNet...
		SubstrateNetwork tmpSub = new SubstrateNetwork(subNet);
		tmpSub.resetSubstrateNetwork();
		SubstrateManager subMan = new SubstrateManager(tmpSub);

		// Event indexes are used to manipulate the correct file readers
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<OutputFileReader> tmpFileReaders = new ArrayList<OutputFileReader>();
		ArrayList<ArrayList<String>> tmpUsedNodes = new ArrayList<>();
		ArrayList<ArrayList<Pair<String>>> tmpUsedEdges = new ArrayList<>();

		long startTime = System.currentTimeMillis();
		long endTime = 0;

		long startWNodeMappingTime = 0;
		long startWLinkMappingTime = 0;
		long endWNodeMappingTime = 0;
		long endWLinkMappingTime = 0;

		VirtualNetworkEvent virtEvent;
		VirtualNetwork vNet;
		int i;

		DVineDatFileCreator dVineDatCreator = new DVineDatFileCreator();
		HeuristicDVineDatFileCreator heuristicDVineDatCreator = new HeuristicDVineDatFileCreator();

		ArrayList<String> tmpRec = new ArrayList<>();
		ArrayList<VirtualNetworkEvent> rejectedEvents = new ArrayList<>();
		
		System.out.println("---------------------------------- RECONFIG -----------------------------------------");
		System.out.println("Num Virtual Networks: " + virNets.size());
		
		for (int j = 0; j < virNets.size(); j++) {
			
			if(!vne.isEmpty()){
				virtEvent = vne.peek();
			} else {
				virtEvent = rejectedEvents.get(0);
			}
			
			vNet = virtEvent.getVirNet();
			i = virtEvent.getEvent().getIndex();

			indexes.add(i);
			tmpFileReaders.add(new OutputFileReader());
			tmpUsedNodes.add(new ArrayList<String>());
			tmpUsedEdges.add(new ArrayList<Pair<String>>());

			startTime = System.currentTimeMillis();
			dVineDatCreator.createDatFile("./glpk/datFiles/Dynamic/" + graphstype + "/randomLP_req" + i + ".dat",
					tmpSub, vNet);

			startWNodeMappingTime = System.currentTimeMillis();
			String partialResult = Utils.runGLPSOLHeuristic(
					"./glpk/datFiles/Dynamic/" + graphstype + "/randomLP_req" + i + ".dat", modFile);

			if (partialResult.equals("timeout")) {
				tmpFileReaders.get(j).setTimeout(true);
				tmpFileReaders.get(j).setWasAccepted(false);
			} else {
				Utils.writeFile(partialResult,
						"./glpk/outputFiles/Dynamic/" + graphstype + "/randomLP_req" + i + ".txt");

				if (partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
						|| partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
						|| partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

					tmpFileReaders.get(j).setWasAccepted(false);
				} else {
					HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();
					virtualNodeIndexArraySubstrateNodeInfoMap = tmpFileReaders.get(j)
							.populateDVineHeuristicWorkingInfo(vNet, tmpSub.getNumOfNodes(), partialResult, tmpSub);
					
					if(reconfigType.equals("PATH")){
						mappedWorkingNodes = Utils.mappingVirtualWorkingNodesSP(virtualNodeIndexArraySubstrateNodeInfoMap, tmpSub);
					} else {
						mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(virtualNodeIndexArraySubstrateNodeInfoMap, tmpSub);
					}
					
					endWNodeMappingTime = System.currentTimeMillis();
					tmpFileReaders.get(j).setNodeWMappingTime(endWNodeMappingTime - startWNodeMappingTime);

					if (mappedWorkingNodes != null) {
						heuristicDVineDatCreator.createDatFile(
								"./glpk/datFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".dat", tmpSub, vNet,
								mappedWorkingNodes);

						startWLinkMappingTime = System.currentTimeMillis();
						String finalResult = Utils.runGLPSOLHeuristic(
								"./glpk/datFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".dat", modFile2);

						if (finalResult.equals("timeout")) {
							tmpFileReaders.get(j).setTimeout(true);
							tmpFileReaders.get(j).setWasAccepted(false);
						} else {
							Utils.writeFile(finalResult,
									"./glpk/outputFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".txt");

							if (finalResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION")
									|| finalResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION")
									|| finalResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")) {

								tmpFileReaders.get(j).setWasAccepted(false);
							} else {
								tmpFileReaders.get(j).collectDVineHeuristicInfo(vNet, tmpSub.getNumOfNodes(),
										"./glpk/outputFiles/Dynamic/" + graphstype + "/randomMCF_req" + i + ".txt",
										tmpSub, mappedWorkingNodes);

								endWLinkMappingTime = System.currentTimeMillis();
								tmpFileReaders.get(j).setLinkWMappingTime(endWLinkMappingTime - startWLinkMappingTime);

								if (tmpFileReaders.get(j).wasAccepted()) {
									for (String s : tmpFileReaders.get(j).getNodesUsed()) {
										tmpUsedNodes.get(j).add(Utils.convertToAlphabet(s));
									}
									for (Pair<String> s : tmpFileReaders.get(j).getEdgesUsed()) {
										tmpUsedEdges.get(j).add(new Pair<String>(Utils.convertToAlphabet(s.getLeft()),
												Utils.convertToAlphabet(s.getRight())));
									}
									subMan.updateSubstrateNetwork(vNet, tmpFileReaders.get(j).getMappedEdges(),
											tmpUsedEdges.get(j), tmpFileReaders.get(j).getMappedNodes(),
											tmpUsedNodes.get(j), tmpFileReaders.get(j).getBwEdgesUsed(),
											UpdateMode.DECREMENT);
								}
								endTime = System.currentTimeMillis();
								tmpFileReaders.get(j)
										.setExecutionTime("" + (Double.valueOf(tmpFileReaders.get(j).getExecutionTime())
												+ ((endTime - startTime) / 1000)));
							}
						}
					} else {
						tmpFileReaders.get(j).setWasAccepted(false);
					}
				}
			}

			if (mappedWorkingNodes != null)
				mappedWorkingNodes.clear();
			if (mappedWorkingEdges != null)
				mappedWorkingEdges.clear();
			if (mappedBackupNodes != null)
				mappedBackupNodes.clear();
			if (mappedBackupEdges != null)
				mappedBackupEdges.clear();
			
			if (!tmpFileReaders.get(j).wasAccepted()) {
				if(!vne.isEmpty()){
					vne.poll();
					j--;
					indexes.remove(indexes.size()-1);
					tmpFileReaders.remove(tmpFileReaders.size()-1);
					tmpUsedEdges.remove(tmpUsedEdges.size()-1);
					tmpUsedNodes.remove(tmpUsedNodes.size()-1);
					
					rejectedEvents.add(virtEvent);
				} else {
					rejected = true;
					break;
				}
			} else {
				double totalRev = 0, totalCost = 0, avgNU = 0, avgLU = 0;

				if (!considersSecCost) {
					totalRev = vNet.getNoSecRevenue();
					totalCost = tmpSub.getDvineAndFullGreedyHeuNoSecCost(vNet,
							tmpFileReaders.get(j).getMappedNodes(), tmpUsedNodes.get(j), tmpUsedEdges.get(j),
							tmpFileReaders.get(j).getMappedEdges(), tmpFileReaders.get(j).getBwEdgesUsed());
				} else {
					totalRev = vNet.getDvineAndFullGreedyHeuRevenue();
					totalCost = tmpSub.getDvineAndFullGreedyHeuCost(vNet, tmpFileReaders.get(j).getMappedNodes(),
							tmpUsedNodes.get(j), tmpUsedEdges.get(j), tmpFileReaders.get(j).getMappedEdges(),
							tmpFileReaders.get(j).getBwEdgesUsed());
				}

				avgNU = tmpSub.getAverageNodeStress();
				avgLU = tmpSub.getAverageLinkStress();

				output += i + "  " + curTime + "  " + vNet.getDuration() + "  "
						+ "RECONFIG";

				output += "  " + tmpFileReaders.get(j).wasAccepted() + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalRev) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", totalCost) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgNU) + "  "
						+ String.format(Locale.ENGLISH, "%10.4f", avgLU) + "  " + tmpFileReaders.get(j).getExecutionTime()
						+ "  " + tmpFileReaders.get(j).getNodeWMappingTime() + "  "
						+ tmpFileReaders.get(j).getLinkWMappingTime() + "  " + tmpFileReaders.get(j).getNodeBMappingTime()
						+ "  " + tmpFileReaders.get(j).getLinkBMappingTime() + "  " + tmpFileReaders.get(j).isTimeout()
						+ (tmpFileReaders.get(j).getEdgesUsed().size()/vNet.getNumOfEdges()) + "\n";
				
				tmpRec.add(output);
				
				if(!vne.isEmpty())
					vne.poll();
				else
					rejectedEvents.remove(0);
			}
		}

		if (!rejected) {
			subNet.setCPUBW(tmpSub);
			
			for (int k = 0; k < tmpFileReaders.size(); k++){
				fileReaders.set(indexes.get(k), tmpFileReaders.get(k));
				usedNodes.set(indexes.get(k), tmpUsedNodes.get(k));
				usedEdges.set(indexes.get(k), tmpUsedEdges.get(k));
			}
			success = true;
			
			for(String line: tmpRec){
				for(int s = 0; s < finalString.size(); s++){
					String[] tmp = finalString.get(s).split("\\s+");
					if(tmp[0].equals(line.split("\\s+")[0]) && (tmp[3].equals("ARRIVE") || tmp[3].equals("RECONFIG"))){
						finalString.remove(s);
						break;
					}
				}
				finalString.add(line);
			}
			
		} else {
			failedRec++;
			System.out.println("FAILED REC");
		}
		
		System.out.println("---------------------------------- END RECONFIG -----------------------------------------");
	}

	public void checkSubNet() {

		System.out.println("Number of nodes = " + subNet.getNumOfNodes());
		System.out.println("Number of links = " + subNet.getNumOfEdges());
		System.out.println("Number of clouds = " + subNet.getNClouds());

		System.out.println("-----------------------------||---------------------------------");

		int count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.0)
				count++;
		}
		System.out.println("Number of nodes with sec0 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.1)
				count++;
		}
		System.out.println("Number of nodes with sec1 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.2)
				count++;
		}
		System.out.println("Number of nodes with sec2 = " + count);

		System.out.println("-----------------------------||---------------------------------");

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.0) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.0) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec0 connected to nodes with sec0 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.0) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.1) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec1 connected to nodes with sec0 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.0) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.2) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec2 connected to nodes with sec0 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.1) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.0) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec0 connected to nodes with sec1 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.1) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.1) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec1 connected to nodes with sec1 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.1) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.2) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec2 connected to nodes with sec1 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.2) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.0) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec0 connected to nodes with sec2 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.2) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.1) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec1 connected to nodes with sec2 = " + count);

		count = 0;
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			if (subNet.getNodeSec(i) == 1.2) {
				for (int j = 0; j < subNet.getNumOfEdges(); j++) {
					if (subNet.getEdge(j).contains(subNet.getNode(i)) && subNet.getEdgeSec(j) == 1.2) {
						count++;
					}
				}
			}
		}
		System.out.println("Number of edges with sec2 connected to nodes with sec2 = " + count);

		System.out.println("-----------------------------||---------------------------------");
	}

	public void removeDatFiles(){
			System.out.println("Removing datFiles from ./glpk/datFiles/"+folder+"/"+graphstype+"/*");
			
			File f = new File("./glpk/datFiles/"+folder+"/"+graphstype+"/");
			for(File file: f.listFiles()) 
			    if (!file.isDirectory()) 
			        file.delete();
			
	}

}
