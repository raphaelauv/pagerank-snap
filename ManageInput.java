import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;


class Node {
	int id;
	ArrayList<Integer> neighbour;

	public Node(int idNode) {
		this.id = idNode;
		neighbour = new ArrayList<Integer>();
	}

	public void addNeighbour(int idNeighbour) {
		this.neighbour.add(idNeighbour);
	}
}

class GraphStruct {
	Node[] allNodes;

	private GraphStruct(InfoFile info) {
		allNodes = new Node[info.nbNode];
	}

	public void addEdge(int idNode, int idNeighbour) {
		if (allNodes[idNode] == null) {
			allNodes[idNode] = new Node(idNode);
		}

		allNodes[idNode].addNeighbour(idNeighbour);
	}

	public static GraphStruct parseAndFillGraph(File file, InfoFile info) throws IOException {
		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;
		int actualId;
		int actualIdNeighbour;

		GraphStruct graphStr = new GraphStruct(info);

		BufferedReader bf = new BufferedReader(new FileReader(file));

		while ((line = bf.readLine()) != null) {
			nbLine++;
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}

			arrayOfLine = line.split("\\s");
			if (arrayOfLine.length != 2) {
				System.out.println("ERREUR ligne " + nbLine + " format Invalide");
				bf.close();
				return null;
			}

			 System.out.println(arrayOfLine[0] +" "+ arrayOfLine[1]);
			try {
				actualId = Integer.parseInt(arrayOfLine[0]);
				actualIdNeighbour = Integer.parseInt(arrayOfLine[1]);

				graphStr.addEdge(actualId, actualIdNeighbour);

				/*
				 * graphStr.edges[cmpEdge]=actualId;
				 * graphStr.edges[cmpEdge+1]=actualIdNeighbour; cmpEdge+=2;
				 */

				// graph.addEdge(actualId, actualIdNeighbour);
			} catch (NumberFormatException e) {
				System.out.println("ERREUR ligne " + nbLine + " format Invalide");
				bf.close();
				return null;
			}

		}
		bf.close();
		return graphStr;

	}

}

class InfoFile {
	int biggerNumberSeen;
	int nbNode;
	Long nbLine;
	boolean isSort;
	int nbEdges;

	public InfoFile(int biggerNumberSeen, int nbNode, Long nbLine, boolean isSort, int nbEdges) {
		this.biggerNumberSeen = biggerNumberSeen;
		this.nbNode = nbNode;
		this.nbLine = nbLine;
		this.isSort = isSort;
		this.nbEdges = nbEdges;
	}

	@Override
	public String toString() {
		return "nb Edges : "+nbEdges+" nb nodes : " + nbNode + "\nis sorted ? " + isSort + "\nbigger number find : " + biggerNumberSeen;
	}
}

public final class ManageInput {

	private static void printMemory(String msg) {
		System.out.println(msg + " | Mémoire allouée : "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "octets");
	}

	public static void printMemoryStart() {
		printMemory("FIN LECTURE FICHIER + CREATION GRAPH");
	}

	public static void printMemoryEND() {
		printMemory("FIN PARCOUR");
	}

	public static void SortFile(File file) throws IOException {

		BufferedReader bf = new BufferedReader(new FileReader(file));

		bf.close();

	}
	
    public static void writePageRank(File out,VectorMAAIN v) throws IOException{
        BufferedWriter bf = new BufferedWriter(new FileWriter(out));
        bf.write(v.values.length+"");
        bf.newLine();
        for(int i=0;i<v.values.length;i++){
            bf.write(v.values[i]+"");
            bf.newLine();
        }
        bf.close();

    }
    
    public static VectorMAAIN readPageRankFile(File f) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(f));
        int size = Integer.parseInt(br.readLine());
        float tab[] = new float[size];

        for(int i=0;i<size;i++){
            tab[i] = Float.parseFloat(br.readLine());
        }
        br.close();
        return new VectorMAAIN(tab);
    }
    
	public static InfoFile getInfoFile(File file) throws IOException {

		BufferedReader bf = new BufferedReader(new FileReader(file));

		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;
		int actualId;
		int actualIdNeighbour;
		boolean isSort = true;

		int biggerNumberSeen = -1;

		int lastNumberArc = -1;
		int lastAssociatedNumberArc = -1;

		boolean firstNode = true;
		boolean firstAssociatedNode = true;

		int nbNode = 0;

		int nbEdges = 0;

		while ((line = bf.readLine()) != null) {
			nbLine++;
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			arrayOfLine = line.split("\\s");
			//System.out.println(line);
			try {
				actualId = Integer.parseInt(arrayOfLine[0]);
				actualIdNeighbour = Integer.parseInt(arrayOfLine[1]);
				nbEdges++;
				
				
				if (actualId > biggerNumberSeen) {
					biggerNumberSeen = actualId;
				}
				if (actualIdNeighbour > biggerNumberSeen) {
					biggerNumberSeen = actualIdNeighbour;
				}
				
				
			} catch (NumberFormatException e) {
				System.out.println("ERREUR ligne " + nbLine + " format Invalide");
				bf.close();
				return null;
			}

			if (firstNode) {
				nbNode++;
				// System.out.println("new node");
				firstNode = false;
				lastNumberArc = actualId;
				if (firstAssociatedNode) {
					firstAssociatedNode = false;
					lastAssociatedNumberArc = actualIdNeighbour;
				}
			}

		

			if (lastNumberArc != actualId) {
				if (actualId < lastNumberArc) {
					isSort = false;
				}
				lastNumberArc = actualId;
				// System.out.println("new node");
				nbNode++;
				lastAssociatedNumberArc = -1;

			}

			if (lastAssociatedNumberArc != actualIdNeighbour) {
				if (actualIdNeighbour < lastAssociatedNumberArc) {
					isSort = false;
				}
				lastAssociatedNumberArc = actualIdNeighbour;
			}
		}
		bf.close();

		return new InfoFile(biggerNumberSeen, nbNode, nbLine, isSort, nbEdges);
	}

}
