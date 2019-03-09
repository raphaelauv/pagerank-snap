import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Collecteur {
	private LinkedHashMap<String, ArrayList<Integer>> dict;
	private Collection<String> forbiddenWords;

	public Collecteur(File dictFile, ArrayList<File> forbidenWordsFILES) throws IOException {
		this.forbiddenWords = createHashSet(forbidenWordsFILES);
		this.dict =readDictionnaryFile(dictFile);
	}
	
	public Collecteur(File amazonFile,ArrayList<File> authorisezWordsFILES, ArrayList<File> forbidenWordsFILES) throws IOException {
		this.forbiddenWords = createHashSet(forbidenWordsFILES);
		initDict(authorisezWordsFILES);
		fillDict(amazonFile);
	}
	
	/*
	 * create a file with the sorted Graph of the amazon meta file
	 */
	public static void getGraph(String amazonFile) throws NumberFormatException, IOException {
		
		LinkedHashMap<Integer, ArrayList<String>> graph = new LinkedHashMap<>();
		
		LinkedHashMap<String, Integer> IdAndASIN = new LinkedHashMap<>();
		
		BufferedReader bf = new BufferedReader(new FileReader(amazonFile));
		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;

		int currentId = 0;
		
		String currentASIN="";
		long nbEdges=0;
		
		while ((line = bf.readLine()) != null) {
			nbLine++;
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			line = line.replaceAll("^\\s+", "");

			if (line.startsWith("Id")) {
				arrayOfLine = line.split(" +");
				currentId = Integer.parseInt(arrayOfLine[1]);
			}else if (line.startsWith("ASIN:")) {
				
				arrayOfLine = line.split(": ");
				currentASIN = arrayOfLine[1];
			}else if (line.startsWith("similar:")) {
				
				IdAndASIN.put(currentASIN,currentId);
				
				
				String title = line.substring(9);
				
				arrayOfLine=title.split(" ");
				
				int nbNeigbours=Integer.parseInt(arrayOfLine[0]);
				nbEdges+=nbNeigbours;
				
				for(int i=1; i<arrayOfLine.length; i++) {
					
					String str=arrayOfLine[i];
					if(str.length()==0) {
						continue;
					}
					
					String neigbhour = str;
					
					if(graph.containsKey(currentId)) {
						ArrayList<String> neigbhours = graph.get(currentId);
						if(!neigbhours.contains(neigbhour)) {
							neigbhours.add(neigbhour);
						}
					}else {
						
						ArrayList<String> neigbhours = new ArrayList<>(nbNeigbours);
						neigbhours.add(neigbhour);
						graph.put( currentId, neigbhours);
					}
				}
				
			} else {
				continue;
			}
		}
		bf.close();
		
		
		
		LinkedHashMap<String, Integer> idsSorted = new LinkedHashMap<>(IdAndASIN.size());	
		IdAndASIN.entrySet().stream().sorted(Map.Entry
				.<String, Integer>comparingByValue())
				.forEachOrdered(e -> idsSorted.put(e.getKey(), e.getValue()));
	
	
		Path file2 = Paths.get("./" + amazonFile + ".graph");
		OutputStream out = new BufferedOutputStream(Files.newOutputStream(file2, CREATE, TRUNCATE_EXISTING));
		byte[] jumpLine = System.lineSeparator().getBytes();
		byte[] space = " ".getBytes();
		
		out.write(("# NB NODES : "+graph.size()).getBytes());
		out.write(("# NB EDGES : "+nbEdges).getBytes());
		out.write(jumpLine);
		
		for(Entry<String,Integer> key:idsSorted.entrySet()) {
			
			//System.out.println(key.getKey()+" -> "+key.getValue());
				
			ArrayList<String> neigbours = graph.get(key.getValue()); 
			
			if(neigbours==null) {
				System.err.println("for "+key.getValue()+" No NEIGBOUR\n");
				continue;
			}
			
			ArrayList<Integer> neigboursId= new ArrayList<>(neigbours.size());
		
			for(String neighbour : neigbours ) {
				
				Integer valuNeigh = IdAndASIN.get(neighbour);
				
				if(valuNeigh!=null) {
					neigboursId.add(valuNeigh);	
				}else {
					System.err.println("NO ID for "+neighbour+"\n");
				}
				
				
			}
			Collections.sort(neigboursId);

			for(Integer neighbour : neigboursId ) {
				//System.out.println(key.getValue()+" "+neighbour);
				
				out.write(String.valueOf(key.getValue()).getBytes());
				out.write(space);
				out.write(String.valueOf(neighbour).getBytes());
				out.write(jumpLine);
			}
			
		}
		
		out.close();
		
	}

	private void initDict(ArrayList<File> authorisezWordsFILES) throws IOException {
	
		this.dict = new LinkedHashMap<String, ArrayList<Integer>>();

		for (int i = 0; i < authorisezWordsFILES.size(); i++) {
			BufferedReader bf = new BufferedReader(new FileReader(authorisezWordsFILES.get(i)));
			String line;
			while ((line = bf.readLine()) != null) {
				if(!isForbidden(line)) {
					dict.put(line, new ArrayList<Integer>());	
				}
			}
			bf.close();
		}
	}

	
	private static HashSet<String> createHashSet(ArrayList<File> listOfFiles) throws IOException {
		HashSet<String> set = new HashSet<String>();
		
		if(listOfFiles==null) {
			return set;
		}
		
		for (int i = 0; i < listOfFiles.size(); i++) {
			BufferedReader bf = new BufferedReader(new FileReader(listOfFiles.get(i)));
			String line;
			while ((line = bf.readLine()) != null) {
				set.add(line);
			}
			bf.close();
		}
		return set;
	}

	
    private void fillDict(File amazonFile) throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader(amazonFile));
		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;

		int currentId = 0;
		while ((line = bf.readLine()) != null) {
			nbLine++;
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			line = line.replaceAll("^\\s+", "");

			if (line.startsWith("Id")) {
				arrayOfLine = line.split(" +");
				currentId = Integer.parseInt(arrayOfLine[1]);
			} else if (line.startsWith("title:")) {
				String title = line.substring(7);
				analizeTitle(title, currentId);
			} else if (line.startsWith("categories:")) {
				arrayOfLine = line.split(" +");
				int nbCat = Integer.parseInt(arrayOfLine[1]);
				for (int i = 0; i < nbCat; i++) {
					if ((line = bf.readLine()) != null) {
						analizeCategories(line, currentId);
					}
				}
			} else {
				continue;
			}
		}
		bf.close();
		
	}
	
	public void analizeTitle(String title, int id) {
		String removed = title.replaceAll("[,;.?:]", " ");
		removed = removed.toLowerCase();
		String[] words = removed.split(" +");
		
		ArrayList<Integer> list;
		
		for (int i = 0; i < words.length; i++) {

			list = dict.get(words[i]);

			if (list != null) {
				if (list.size() > 0) {
					if (list.get(list.size() - 1) != id)
						list.add(id);
				} else {
					list.add(id);
				}
			}
		}
	}

	public void analizeCategories(String categorie, int id) {
		String[] cats = categorie.split("\\|");
		for (int i = 2; i < cats.length; i++) {
			String clean = cats[i].replaceAll("\\[.*\\]", "");
			String[] words = clean.split("[ &,;]");
			
			ArrayList<Integer> list;
			
			for (int j = 0; j < words.length; j++) {
				list = dict.get(words[j].toLowerCase());
				if (list !=null) {
					if (list.size() > 0) {
						if (list.get(list.size() - 1) != id)
							list.add(id);
					} else
						list.add(id);

				}
			}
		}
	}

	public boolean isForbidden(String s) {
		return forbiddenWords.contains(s);
	}

	public int distanceWord(char[] a, char[] b) {
		int[][] dist = new int[a.length + 1][b.length + 1];

		for (int i = 0; i <= a.length; i++) {
			dist[i][0] = i;
		}
		for (int i = 0; i <= b.length; i++) {
			dist[0][i] = i;
		}
		for (int i = 1; i <= a.length; i++) {
			for (int j = 1; j <= b.length; j++) {
				int coutSubst = 1;
				if (a[i - 1] == b[j - 1])
					coutSubst = 0;
				dist[i][j] = Math.min(dist[i - 1][j] + 1, Math.min(dist[i][j - 1] + 1, dist[i - 1][j - 1] + coutSubst));
			}
		}
		return dist[a.length][b.length];
	}

	public LinkedHashMap<String, ArrayList<Integer>> getDic() {
		return dict;
	}

	public void printDic() {
		for (Entry<String, ArrayList<Integer>> actual : dict.entrySet()) {
			ArrayList<Integer> prod = actual.getValue();
			if (prod.size() > 0) {
				System.out.print(actual.getKey() + " : ");
				for (int i = 0; i < prod.size(); i++) {
					System.out.print(prod.get(i) + " ");

				}
			}
		}
	}
	
	
	  
    /*
     * create A dictionnary drom a file ( created with the collector write function)
     */
	private LinkedHashMap<String, ArrayList<Integer>> readDictionnaryFile(File dictFile) throws IOException{
		LinkedHashMap<String, ArrayList<Integer>> dict=new LinkedHashMap<>();
		BufferedReader bf = new BufferedReader(new FileReader(dictFile));
		String line = "";
		Long nbLine = 0l;
		String[] arrayOfLine;
		String actualCatego;
		int indexSeparation=0;
	
		while ((line = bf.readLine()) != null) {
			nbLine++;
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			
			indexSeparation = line.indexOf(":");
			actualCatego = line.substring(0,indexSeparation-1);
			arrayOfLine = line.substring(indexSeparation+2).split(" "); //+2 for the : and the space folowing

			ArrayList<Integer> listId = new ArrayList<>(arrayOfLine.length+1);
			for(String tmp : arrayOfLine) {
				listId.add(Integer.valueOf(tmp));
			}
			
			dict.put(actualCatego, listId);
		
			
		}
		bf.close();
		
		return dict;
	}

	public void writeDict(String fileName) throws IOException {

		Path file2 = Paths.get("./" + fileName + ".dict");

		OutputStream out = new BufferedOutputStream(Files.newOutputStream(file2, CREATE, TRUNCATE_EXISTING));

		byte[] jumpLine = System.lineSeparator().getBytes();
		byte[] separation = " : ".getBytes();
		byte[] space = " ".getBytes();

		for (Entry<String, ArrayList<Integer>> actual : dict.entrySet()) {
			ArrayList<Integer> prod = actual.getValue();
			if (prod.size() > 0) {
				out.write(actual.getKey().getBytes());
				out.write(separation);
				for (int i = 0; i < prod.size(); i++) {
			
					out.write(String.valueOf(prod.get(i)).getBytes());		
					out.write(space);
					
				}
				out.write(jumpLine);
			}
		}
		out.close();

	}

	
	public static void manageFiles(ArrayList<File> authorisezWordsFILES,ArrayList<File> forbidenWordsFILES,String[] args) {
		
		boolean forb = false;
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-fw")) {
				forb = true;
			} else if (forb)
				forbidenWordsFILES.add(new File(args[i]));
			else {
				authorisezWordsFILES.add(new File(args[i]));
			}
		}
	
		if(!authorisezWordsFILES.isEmpty()) {
			System.out.println("authorisezWordsFILES : "+authorisezWordsFILES);	
		}
		if(!forbidenWordsFILES.isEmpty()) {
			System.out.println("forbidenWordsFILES "+forbidenWordsFILES);
		}
	}
	
	public static void main(String[] args) {

		if(args.length>1 && args[0].equals("createGraph")) {
			try {
				getGraph(args[1]);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		if (args.length < 2) {
			System.err.println(
					"java Collecteur [dataAmazonFile] [dictionnaryS of authorisez Files] [-fw] [dictionnaryS of forbidden words]");
			return;
		}
		
		ArrayList<File> authorisezWordsFILES = new ArrayList<File>();
		ArrayList<File> forbidenWordsFILES = new ArrayList<File>();

		try {
			File amazonFile = new File(args[0]);
			manageFiles(authorisezWordsFILES,forbidenWordsFILES,args);
			Collecteur collector = new Collecteur(amazonFile,authorisezWordsFILES,forbidenWordsFILES);

			//System.out.println("write collecteur");

			collector.writeDict(args[0]);

		} catch (IOException e) {
			System.err.println("ERREUR OCCURED");
			e.printStackTrace();
		}
	}
}
