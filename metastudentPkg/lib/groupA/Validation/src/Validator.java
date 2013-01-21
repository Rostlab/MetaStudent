import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * TODO
 * 		implement all methods
 * 		include GO Parser
 *
 * @author Yannick, Ariane
 *
 */
public class Validator {

	private String valFile;
	private String parentFile = "GO_Table.txt";
	private Hashtable<String, String[]> parentTable;
	
	private double rec;
	private double prec;
	
	/**
	 * Constructor
	 * @param validationFile
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Validator(String validationFile) throws FileNotFoundException, IOException{
		valFile = validationFile;
		parentTable  = createParentTable();
		rec = 0;
		prec = 0;
	}
	
	/**
	 * this method does the validation job
	 * including	*reading the validationFile
	 * 				*comparing GO Terms
	 * 				*calculate Performance of results
	 */
	public void validate() throws FileNotFoundException, IOException{
		
		BufferedReader valFileReader = new BufferedReader(new FileReader(valFile));
		String line; 
		int counter = 0;
		while((line = valFileReader.readLine()) != null){
			String[] tempLine = line.substring(1).split("\t");
			String[] ids = tempLine[0].split(",");
			String[] GOs = tempLine[1].split(",");
			
			counter++;
			
			Vector<String> idsWithHull = getHull(ids); 
			Vector<String> GOsWithHull = getHull(GOs); 
			
			calcPosNegs(idsWithHull, GOsWithHull);

		}
		calcPerformanceII(counter);
	}
	
	/**
	 * returns the Hull for a specific term
	 * @param terms
	 * @return
	 */

	private Vector<String> getHull(String[] terms){
		
		Vector<String> termsWithHull = new Vector<String>();
		
		for(String term : terms){
			String[] termParents = parentTable.get(term);
			termsWithHull.add(term);
			
			if(termParents != null){
				for(String par:termParents){
					if(!par.equals("GO:0003674") && !par.equals("GO:0008150") && !par.equals("GO:0005575") && !termsWithHull.contains(par)){
						termsWithHull.add(par);
					}
				}
			}

		}
		return termsWithHull;
	}
	
	/**
	 * saves the GO_Parent file into a Hastable
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Hashtable<String, String[]> createParentTable() throws FileNotFoundException, IOException{
		
		Hashtable<String, String[]> returnTable = new Hashtable<String, String[]>();

		BufferedReader parentFileReader = new BufferedReader(new FileReader(parentFile));
		
		String inline;
		while((inline = parentFileReader.readLine()) != null){
			String[] tempinline = inline.split("\t");
			String GO = tempinline[0];
			String[] parents = tempinline[1].split(",");
			
			returnTable.put(GO, parents);
		}
		return returnTable;
	}
	
	/**
	 * calculates the TP, FP, FN and the first Part for the Performance
	 * @param idsWithHull
	 * @param GOsWithHull
	 */
	private void calcPosNegs(Vector<String> idsWithHull, Vector<String> GOsWithHull){
		
		int TP = 0;
		int FP = 0;
		int FN = 0;
		
		Vector<String> toRemove = new Vector<String>();
		
		for(String a : idsWithHull){
			if (GOsWithHull.contains(a)){
				TP++;
				toRemove.add(a);
			}
		}
		
		for(String tr : toRemove){
			idsWithHull.remove(tr);
			GOsWithHull.remove(tr);
		}
		
		FP = GOsWithHull.size();
		FN = idsWithHull.size();
		
		calcPerformanceI(TP, FP, FN);

	}
	
	/**
	 * calculates the first Step for Recall and Precision
	 * @param TP
	 * @param FP
	 * @param FN
	 */
	private void calcPerformanceI(int TP, int FP, int FN){
		double recall = (TP / (TP + FN));
		double precision = (TP / (TP + FP));
		
		prec += precision;
		rec += recall;
	}
	
	/**
	 * calculates the performance of Recall and Precision
	 * @param counter
	 */
	private void calcPerformanceII(int counter){

		double precision = prec / (double)counter;
		double recall = rec / (double)counter;
		
		System.out.println("Precision: " + precision + "\t" + "Recall: " + recall);
	}

}