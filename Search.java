import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Search {

	VectorMAAIN pageRank;
	Collecteur collector;
	HashMap<Integer, ArrayList<String>> idAndAssociatedWords; //not sorted
	
	
	int defaultSizeForRandomPageRank = 943275;

	public Search(File pageRankFile, File dictFile, ArrayList<File> listForbid) throws IOException {

		this.collector = new Collecteur(dictFile, listForbid);

		try {
			this.pageRank = ManageInput.readPageRankFile(pageRankFile);
		} catch (IOException e) {
			System.err.println("ERROR WITH FILE PAGERANK , a RANDOM HAVE BEEN CREATE");
			
			this.pageRank = new VectorMAAIN(defaultSizeForRandomPageRank);

			for (int i = 0; i < defaultSizeForRandomPageRank; i++) {
				this.pageRank.values[i] = new Random().nextFloat();
			}
		}

	}
	
	//temp structs for reuse inside printByPageRank
	HashMap<Integer, Float> idAndPageRank;
	ArrayList<Entry<Integer, Float>> idsSortedByPageRankValue;


	public void printByPageRank(ArrayList<Entry<Integer, ArrayList<String>>> idsWithSameNumberOfOccurence) {

		if(this.idAndPageRank==null) {
			this.idAndPageRank = new HashMap<>(idsWithSameNumberOfOccurence.size());	
		}else {
			this.idAndPageRank.clear();
		}

		for (Entry<Integer, ArrayList<String>> entry : idsWithSameNumberOfOccurence) {
			int id = entry.getKey();

			try {
				idAndPageRank.put(id, pageRank.values[id]);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("PAGERANK FILE IS TO SMALL OR INVALIDE : " + id);
			}

		}

		if(this.idsSortedByPageRankValue==null) {
			this.idsSortedByPageRankValue = new ArrayList<>(idAndPageRank.size());	
		}else {
			this.idsSortedByPageRankValue.clear();
		} 

		idAndPageRank
			.entrySet()
			.stream()
			.sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
			.forEachOrdered(e -> idsSortedByPageRankValue.add(e));

		for (Entry<Integer, Float> entry : idsSortedByPageRankValue) {
			System.out.print("ID : " + entry.getKey());
			System.out.print(" PageRank : " + entry.getValue());
			ArrayList<String> matchedWords = idAndAssociatedWords.get(entry.getKey());
			System.out.println(" Match " + matchedWords.size() + " WORDS : " + matchedWords);
		}

	}
	
	
	public void printResearch(ArrayList<Entry<Integer, ArrayList<String>>> idAndWordsSorted) {
		
		ArrayList<Entry<Integer, ArrayList<String>>> idsWithSameNumberOfOccurence = new ArrayList<>();
		int i = 0;
		int lastSize = 0;

		for (Entry<Integer, ArrayList<String>> val : idAndWordsSorted) {

			if (i == 0) {
				i++;
				idsWithSameNumberOfOccurence.add(val);
				lastSize = val.getValue().size();

			} else {
				if (val.getValue().size() < lastSize) {

					this.printByPageRank(idsWithSameNumberOfOccurence);
					idsWithSameNumberOfOccurence.clear();

					i = 0;
				} else {
					idsWithSameNumberOfOccurence.add(val);
				}
			}
		}

		if (!idsWithSameNumberOfOccurence.isEmpty()) {
			this.printByPageRank(idsWithSameNumberOfOccurence);
		}
	}
	
	public ArrayList<Entry<Integer, ArrayList<String>>> getSortedAssociationIdsAndWords(ArrayList<String> correctSearchWordList){
	
		this.idAndAssociatedWords = new HashMap<>();
		for (String word : correctSearchWordList) {
			for (Integer id : this.collector.getDic().get(word)) {
				if (idAndAssociatedWords.containsKey(id)) {
					idAndAssociatedWords.get(id).add(word);
				} else {
					ArrayList<String> listStr = new ArrayList<>();
					listStr.add(word);
					idAndAssociatedWords.put(id, listStr);
				}
			}
		}
		// System.out.println(idAndWords); 
		
		ArrayList<Entry<Integer, ArrayList<String>>> idAndWordsSorted = new ArrayList<>(idAndAssociatedWords.size());
		this.idAndAssociatedWords
			.entrySet()
			.stream()
			.sorted(Map.Entry.<Integer, ArrayList<String>>comparingByValue((list1, list2) -> list1.size() - list2.size()).reversed())
			.forEachOrdered(e -> idAndWordsSorted.add(e));
		
		//System.out.println(idAndWordsSorted);
		
		return idAndWordsSorted;
	}
	
	public ArrayList<String> analyseInputWords(String[] args){
		ArrayList<String> correctSearchWordList = new ArrayList<>(args.length - 2);
		for (int i = 3; i < args.length; i++) {
			if (!this.collector.isForbidden(args[i])) { // no forbiden
				if (!correctSearchWordList.contains(args[i])) { // no redundant
					if (this.isWordInsideCollector(args[i])) { // only words in the dict
						correctSearchWordList.add(args[i]);
					} else {
						System.err.println(args[i] + " is not referended");
					}
				} else {
					System.err.println(args[i] + " is redundant");
				}
			} else {
				System.err.println(args[i] + " is forbideen");
			}
		}

		System.err.flush();
		System.out.println("\nWORDS :" + correctSearchWordList);
		
		return correctSearchWordList;
	}
	
	public boolean isWordInsideCollector(String word) {
		return this.collector.getDic().containsKey(word);
	}
	
	public static void main(String[] args) {

		if (args.length < 4) {
			System.err.println("arguments missing: java Search [page_Rank] [word_product] [forbiddent_words] [words]");
			return;
		}

		Search sch;
		try {

			File pageRankFile = new File(args[0]);
			File dictFile = new File(args[1]);

			ArrayList<File> listForbid = new ArrayList<>();
			if (!args[2].equals("nothing")) {
				listForbid.add(new File(args[2]));
			}

			sch = new Search(pageRankFile, dictFile, listForbid);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		ArrayList<String> correctSearchWordList = sch.analyseInputWords(args);
		if (correctSearchWordList.isEmpty()) {
			System.err.println("all your words are forbiden");
			return;
		}

		ArrayList<Entry<Integer, ArrayList<String>>> idAndWordsSorted = sch.getSortedAssociationIdsAndWords(correctSearchWordList);

		sch.printResearch(idAndWordsSorted);
		
	}
}
