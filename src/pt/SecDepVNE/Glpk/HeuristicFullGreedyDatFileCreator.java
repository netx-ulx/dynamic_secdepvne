package pt.SecDepVNE.Glpk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import pt.SecDepVNE.Common.Pair;
import pt.SecDepVNE.Common.Utils;
import pt.SecDepVNE.Substrate.SubstrateNetwork;
import pt.SecDepVNE.Virtual.VirtualNetwork;

/**
 * Handles the creation of a structured file to be the input of the HeuristicSecDep formulation
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class HeuristicFullGreedyDatFileCreator{

	/**
	 * Constructs the input file for SecDep
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 * @param mappedNodes 
	 */
	public void createDatFile(String file, SubstrateNetwork subNet, VirtualNetwork virNet, ArrayList<Pair<String>> mappedNodes) {

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			putNetInfo(bufferedWriter, subNet, virNet, mappedNodes);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts structured substrate network info into the files
	 * @param bufferedWriter Writer of the file
	 * @param subNet Substrate network
	 * @param virNet 
	 * @param linkHeuristicInfo 
	 * @throws IOException
	 */
	public void putNetInfo(BufferedWriter bufferedWriter, SubstrateNetwork subNet, VirtualNetwork virNet, ArrayList<Pair<String>> mappedNodes) throws IOException {

		int sNodes = subNet.getNumOfNodes();
		int sEdges = subNet.getNumOfEdges();
		int vEdges = virNet.getNumOfEdges();
		ArrayList<String> fs = new ArrayList<String>();
		ArrayList<String> fe = new ArrayList<String>();

		bufferedWriter.write("data;\n\n"); // Initialize
		bufferedWriter.write("# Nodes: "+sNodes+", Edges: "+sEdges+"\n\n");

		// ---------- Set Substrate Nodes ----------
		bufferedWriter.write("set N := ");

		for(int i = 0; i < sNodes; i++){
			bufferedWriter.write(i+" ");
		}

		bufferedWriter.write(";\n\n");


		bufferedWriter.write("set F := ");

		for(int i = 0; i < vEdges; i++){

			bufferedWriter.write("f"+i+" ");
		}


		bufferedWriter.write(";\n\n");

		// ---------- Param b ----------

		bufferedWriter.write("param b :\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){

			bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

			for(int j = 0; j < sNodes; j++){
				if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
					bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
				else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
					bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
				else
					bufferedWriter.write("0.0000 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");
		
		fs = Utils.populateFullGreedyFS(mappedNodes, virNet, subNet);
		bufferedWriter.write("param fs :=\n");

		for(int i = 0; i < vEdges; i++){

			bufferedWriter.write("f"+i+"      "+fs.get(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Set Substrate Destination Link ----------
		fe = Utils.populateFullGreedyFE(mappedNodes, virNet, subNet);
		bufferedWriter.write("param fe :=\n");

		for(int i = 0; i < vEdges; i++){

			bufferedWriter.write("f"+i+"      "+fe.get(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Set Virtual bandwidth ----------
		bufferedWriter.write("param fd :=\n");

		for(int i = 0; i < vEdges; i++){

			bufferedWriter.write("f"+i+"      "+virNet.getEdgeBw(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		bufferedWriter.write("end;\n");
	}
}

