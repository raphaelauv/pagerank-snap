import java.util.Arrays;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;

public class Tp1 {


	/*
	public static void main(String[] args) {

		float[][] multi = new float[][] { { 0, 0, 0.1f, 0 }, { 0.2f, 0.3f, 0, 0.4f }, { 0, 0.5f, 0.6f, 0.7f },
				{ 0, 0, 0, 0 }, };

		CSRMatrix CSRm = new CSRMatrix(4, 7, multi);
		CSRm.toString();

		
		VectorMAAIN v = new VectorMAAIN(new float[] { 1f, 2f, 3f, 4f });
		
		
		VectorMAAIN vectY = CSRm.multiTransPosed(v, null);
		
		System.out.println(Arrays.toString(vectY.values));
		
		vectY = CSRm.multiDirect(v, null);
		System.out.println(Arrays.toString(vectY.values));
	}
	*/
	
}

class VectorMAAIN {
	float[] values;

	public VectorMAAIN(float[] x) {
		this.values = x;
	}

	public VectorMAAIN(int n) {
		values = new float[n];
	}


	public boolean diffInfEps(VectorMAAIN vec, float eps) {
		if (vec.values.length != this.values.length) {
			System.out.println("vector should have the same size");
			return false;
		}
		
		float mv = 0;
	    
		for (int i = 0; i < this.values.length; i++) {
	        mv += Math.abs(vec.values[i] - this.values[i]);
	    }
		
	    //System.out.println("iter : "+mv);

		return mv>eps;
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < values.length; i++) {
			s += values[i]+" ";
			if (i != values.length - 1)
				s += ",";
		}
		return s;
	}

}

class CSRMatrix {

	int n; // size
	int m; // not null values

	float[] C;
	int[] L;
	int[] I;

	public CSRMatrix(int n , int m , GraphStruct graph) {		
		this.n = n;
		this.m = m;
		C = new float[m];
		L = new int[n + 1];
		I = new int[m];

		int cmpC = 0;
		int cmpI = 0;

		int nbNotNullLine = 0;
		
		Node actualNode;

		
		for (int i = 0; i < graph.allNodes.length; i++) {
			actualNode= graph.allNodes[i];
			if (i > 0) {
				L[i] = L[i - 1] + nbNotNullLine;
			}
			nbNotNullLine = 0;
			
			float valueRankNode= (float) (1.0/actualNode.neighbour.size());
			
			for (int j = 0; j < actualNode.neighbour.size(); j++) {

					nbNotNullLine++;
					C[cmpC] = valueRankNode;
					I[cmpI] = actualNode.neighbour.get(j);
					cmpC++;
					cmpI++;
			}
		}
		L[L.length - 1] = L[L.length - 2] + nbNotNullLine;

		
	}
	
	public CSRMatrix(int n,int m,File sortedFile) throws IOException {
		
		BufferedReader bf = new BufferedReader(new FileReader(sortedFile));
		this.n = n;
		this.m = m;
		C = new float[m];
		L = new int[n + 1];
		I = new int[m];

		int cmpC = 0;
		int cmpI = 0;

		int nbNotNullLine = 0;

		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;
		int actualId;
		int actualIdNeighbour;
		
		int lastNumberArc = -1;
		int lastAssociatedNumberArc = -1;

		boolean firstNode = true;
		
		
		float valueRankNode =1;
		
		int cmpCLast=0;
		
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

			} catch (NumberFormatException e) {
				System.out.println("ERREUR ligne " + nbLine + " format Invalide");
				bf.close();
				throw new IOException();
			}

			if (firstNode) {
				// System.out.println("new node");
				firstNode = false;
				lastNumberArc = actualId;
				lastAssociatedNumberArc = actualIdNeighbour;
				
				nbNotNullLine++;
				//C[cmpC] = valueRankNode;
				I[cmpI] = actualIdNeighbour;
				cmpC++;
				cmpI++;
				
			}

			if (lastNumberArc != actualId) {
				
				// System.out.println("new node");
				
				float rankValue = (float) (1.0/ (cmpC -cmpCLast));
				//System.out.println("rankValue de  "+lastNumberArc+" -> "+rankValue);
				for(int i=cmpCLast;i<cmpC;i++) {
					C[i]=rankValue;
				}
				cmpCLast=cmpC;
				
				lastNumberArc = actualId;
				lastAssociatedNumberArc = -1;
				
				L[actualId] = L[actualId - 1] + nbNotNullLine;
				nbNotNullLine = 0;
			}

			if (lastAssociatedNumberArc != actualIdNeighbour) {
				lastAssociatedNumberArc = actualIdNeighbour;
				
				nbNotNullLine++;
				//C[cmpC] = valueRankNode;
				I[cmpI] = actualIdNeighbour;
				cmpC++;
				cmpI++;
			}
		}
		
		float rankValue = (float) (1.0/ (cmpC -cmpCLast));
		//System.out.println("rankValue de  "+lastNumberArc+" -> "+rankValue);
		for(int i=cmpCLast;i<cmpC;i++) {
			C[i]=rankValue;
		}
		
		bf.close();

		L[L.length - 1] = L[L.length - 2] + nbNotNullLine;
	}

	
	public CSRMatrix(int n, int m, float[][] denseMatrix) {
		this.n = n;
		this.m = m;
		C = new float[m];
		L = new int[n + 1];
		I = new int[m];

		int cmpC = 0;
		int cmpI = 0;

		int nbNotNullLine = 0;

		for (int i = 0; i < denseMatrix.length; i++) {

			if (i > 0) {
				L[i] = L[i - 1] + nbNotNullLine;
			}
			nbNotNullLine = 0;
			for (int j = 0; j < denseMatrix.length; j++) {

				if (denseMatrix[i][j] != 0) {
					nbNotNullLine++;
					C[cmpC] = denseMatrix[i][j];
					I[cmpI] = j;
					cmpC++;
					cmpI++;
				}
			}
		}
		L[L.length - 1] = L[L.length - 2] + nbNotNullLine;

	}

	
	public void show() {
		toString();
	}
	
	@Override
	public String toString() {
		System.out.println(Arrays.toString(C));
		System.out.println(Arrays.toString(L));
		System.out.println(Arrays.toString(I));
		System.out.println("---------------------------");
		return "";
	}

	public VectorMAAIN multiDirect(VectorMAAIN vec, VectorMAAIN resultY) {

		if (resultY == null) {
			resultY = new VectorMAAIN(n);
		}
		
		for (int i = 0; i < n; i++) {
			resultY.values[i] = 0;
			for (int k = L[i]; k < L[i + 1]; k++) {
				resultY.values[i] += C[k] * vec.values[I[k]];
			}
		}

		return resultY;
	}

	public VectorMAAIN multiTransPosed(VectorMAAIN vec,  VectorMAAIN resultY) {

		
		Arrays.fill(resultY.values, 0);
		
		
		for (int i = 0; i < n; i++) {
			for (int j = L[i]; j < L[i + 1]; j++) {
				
				resultY.values[I[j]] += C[j] * vec.values[i];
				
			}
		}
		return resultY;

	}
	
	

	public VectorMAAIN multiTransPosed(VectorMAAIN vec,  VectorMAAIN resultY,float zap) {

		resultY = multiTransPosed(vec,resultY);
		
		float valueZapByN = zap / (float) n;
		float oneUnderZap = (float) (1.0 - zap);
		for (int i = 0; i < n; i++) {
			resultY.values[i] = valueZapByN + (oneUnderZap * resultY.values[i]);
	    }
		
		return resultY;

	}


	public VectorMAAIN getPageRank(float eps, float d) {
		
		float val = (float) (1.0 / n);
		VectorMAAIN good = new VectorMAAIN(n);
		for(int i =0;i< good.values.length;i++) {
			good.values[i]=val;
		}
	
		
		VectorMAAIN newGood = multiTransPosed(good,new VectorMAAIN(n), d);
		
		PageRank.modeVerbose(good.toString());
		PageRank.modeVerbose(newGood.toString());
		
		boolean reverse=true;
		
		while(newGood.diffInfEps(good, eps)) {
			
			//System.out.println("ITER");
			if(reverse) {
				multiTransPosed(newGood,good, d);
				reverse=false;
				
				PageRank.modeVerbose(newGood.toString());
				PageRank.modeVerbose(good.toString());
			
			}else {
				multiTransPosed(good,newGood, d);
				reverse=true;
	
				PageRank.modeVerbose(good.toString());
				PageRank.modeVerbose(newGood.toString());	
				
			}
			
			
			
			//System.out.println("iter");
		}
		return newGood;
	}
}
