import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ariane
 *
 */
public class ParseGONew {
	
	private String file;
	private ArrayList<String> allParents = new ArrayList<String>();
	private ArrayList<String> goTerms = new ArrayList<String>();
	private ArrayList<String> rootTerms = new ArrayList<String>();
	private ArrayList<Integer> maxPath = new ArrayList<Integer>();
	
	
	/**
	 * Constructor
	 * @param GOFile
	 */
	public ParseGONew(String GOFile){
		file = GOFile;
	}
	
	/**
	 * The Parser for the deductiveClosure textFile
	 * @throws IOException
	 */
	public void ParseDeductiveClosure() throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String line;
		ArrayList<LinkedList<String>> directParents = new ArrayList<LinkedList<String>>();
		
		String allPar = "";
		while((line = reader.readLine())!= null){
			
			String[] templine = line.split("\t");
			
			String go = templine[0];
			String parent = templine[2];
			String link = templine[3];
			String relation = templine[1];
			
			if(relation.equals("OBO_REL:is_a") || relation.equals("part_of")){
			
				if (!goTerms.contains(go)){
					allParents.add(allPar);
					allPar = "";
				
					goTerms.add(go);
				
					if(parent.equals("GO:0003674")){
						rootTerms.add("mol_func");
					}
					if(parent.equals("GO:0008150")){
						rootTerms.add("bio_proc");
					}
					if(parent.equals("GO:0005575")){
						rootTerms.add("cel_comp");
					}
					
					LinkedList<String> firstParent = new LinkedList<String>();
					firstParent.add(parent);
					
					if(link.equals("asserted")){
						directParents.add(firstParent);
						allPar+= (parent + ",");
					}
					if(link.equals("implied")){
						allPar+= (parent + ",");
					}
				}
				
				else{ 	
					int index = goTerms.indexOf(go);
					
					if(parent.equals("GO:0003674")){
						rootTerms.add("mol_func");
					}
					if(parent.equals("GO:0008150")){
						rootTerms.add("bio_proc");
					}
					if(parent.equals("GO:0005575")){
						rootTerms.add("cel_comp");
					}
	
					if(link.equals("asserted")){
						LinkedList<String> inDirParent = directParents.get(index);
							inDirParent.add(parent);
							directParents.remove(index);
							directParents.add(index, inDirParent);
					}
					
					allPar+= (parent + ",");
				}
			}
			
		}
		allParents.add(allPar);
		allParents.remove(0);
		getAllParents(directParents);
		
	}
	
	/**
	 * calculates the longestPath to the root for each GOTerm
	 * and starts the creation of the outfile
	 * @param parents
	 */
	private void getAllParents(ArrayList<LinkedList<String>> parents){
		
		String output = "";
		for(int i = 0; i < goTerms.size(); i++){
			System.out.println(goTerms.get(i));
			int longestPath = getLongestPathToRoot(parents, i);
			maxPath.add(longestPath);			
		}
		
		createOutputString();		
	}
	
	/**
	 * creates the outputString for further work
	 */
	private void createOutputString(){
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("GO_Parents.txt"));
			String out = "";
			for(int i = 0; i< goTerms.size(); i++){
				out = (goTerms.get(i) + "\t" + allParents.get(i) + "\t" + maxPath.get(i) + "\t" + rootTerms.get(i) + "\n");
				writer.write(out);
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		String out = "";
//		for(int i = 0; i< goTerms.size(); i++){
//			out += (goTerms.get(i) + "\t" + allParents.get(i) + "\t" + maxPath.get(i) + "\t" + rootTerms.get(i) + "\n");
//		}
//		System.out.println(out);
	}
	
	/**
	 * returns for a specific GOTerm the number of steps to the root
	 * @param parents
	 * @param posGO
	 * @return
	 */
	private int getLongestPathToRoot(ArrayList<LinkedList<String>> parents, int posGO){
		int longestPath = 0;
		
		LinkedList<String> diPar = parents.get(posGO);
		HashMap<String, Integer> par = new HashMap<String, Integer>();
		
		for(int i = 0; i < diPar.size(); i++){
			par.put(diPar.get(i), 1);
		}
		
		int counter = 2;
		for(int j = 0; j < diPar.size(); j++){
			
			int index = goTerms.indexOf(diPar.get(j));
			if(index != -1){
				LinkedList<String> help = parents.get(index);
				for(int k = 0; k < help.size(); k++){
					String pa = help.get(k);
					
					if(par.containsKey(pa)){
						int val = par.get(pa) + 1;
						par.remove(pa);
						par.put(pa, val);
					}
					else{
						diPar.add(pa);
						par.put(pa, counter);
					}
				}
				counter++;
			}
		}
		
		Collection<Integer> col = par.values();
		Object[] values = col.toArray();

		longestPath = (Integer) values[0];
		return longestPath;
	}
	
}
