import java.io.File;
import java.io.IOException;

public class PageRank {

	public static boolean verbose = false;

	public static void modeVerbose(String s) {
		if (verbose) {
			System.out.println(s);
		}
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("to run : java PageRank [epsilon] [d] [fileName]");
			return;
		} else {
			try {
				verbose=false;
				File file = new File(args[2]);
				InfoFile info = ManageInput.getInfoFile(file);

				if (!info.isSort) {
					System.err.println("file not sorted");
					return;
				}
				//System.out.println(info);

				CSRMatrix cmat;

				// GRAPH VERSION
				/*
				GraphStruct graph = GraphStruct.parseAndFillGraph(file, info);
				cmat = new CSRMatrix(info.nbNode, info.nbEdges, graph);
				cmat.show();
				*/
				
				// DIRECT FILE VERSION
				cmat = new CSRMatrix(info.nbNode, info.nbEdges, file);
				//cmat.show();

				float eps = Float.parseFloat(args[0]);
				float d = Float.parseFloat(args[1]);

				VectorMAAIN pageRank = cmat.getPageRank(eps, d);
				System.out.printf("(eps = %.4f , d = %.2f)\n",eps,d);
				System.out.println(pageRank);
				ManageInput.writePageRank(new File("pageRank.out"), pageRank);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
