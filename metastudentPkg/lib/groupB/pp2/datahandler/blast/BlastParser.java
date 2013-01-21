package pp2.datahandler.blast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pp2.components.BlastHit;
import pp2.tools.Tools;

public class BlastParser {
	
	private ArrayList<ArrayList<BlastHit>> round_results = new ArrayList<ArrayList<BlastHit>>();  // each ArrayList element holds a list of the results for each round
	
	public BlastParser(String file){
		parseFile(file);
	}
	
	public ArrayList<ArrayList<BlastHit>> getData(){
		return round_results;
	}
	
	public void parseFile(String file) {
		BufferedReader bl_data = Tools.openFile(file);
		String line = "";
		try {
			boolean new_entry = false;
			StringBuffer entry = new StringBuffer("");
			Pattern which_round = Pattern.compile("Results\\s+from\\s+round\\s+(\\d+)");
			int round = 0;
			
			while((line = bl_data.readLine()) != null){
				Matcher ma = which_round.matcher(line);
				if (ma.matches()){
					if (entry.length() > 0){
						round_results.get(round).add(new BlastHit(entry.toString(), round+1));
						entry = new StringBuffer("");
						new_entry = false;
					}
					round = Integer.valueOf(ma.group(1))-1;
					ArrayList<BlastHit> bl = new  ArrayList<BlastHit>();
					round_results.add(bl);
				}
				
				if(line.startsWith(">")) {
					if (entry.length()<1){ //reached first hit 
						new_entry = true;
						entry = new StringBuffer("");
						entry.append(line+"\n");
					}
					else{ // reached hit 2..n
						new_entry = true;
						round_results.get(round).add(new BlastHit(entry.toString(), round+1));
						entry = new StringBuffer("");
						entry.append(line+"\n");
					}
				} 
				else if (new_entry){ // all lines between the entries are added
					entry.append(line+"\n");
				}
			} // while
			round_results.get(round).add(new BlastHit(entry.toString(), round+1)); // add last hit
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args){
		if(args.length > 0) {
			String blast_output_file  = "";
			int round = -1;
			for(int k=0; k<args.length; k+=2) {
				if(args[k].equals("-i")) {
					blast_output_file = args[k+1];
				} 
				else if(args[k].equals("-r")) {
					try{
						round = Integer.valueOf(args[k+1])-1;
					}
					catch(NumberFormatException e){
						System.out.println("ERROR: Parameter r has to be an integer!");
						printUsage();
						System.exit(0);
					}
				} 
				else {
					printUsage();
					System.exit(0);
				}
			}
			BlastParser bp  = new BlastParser(blast_output_file);
			if (round < 0){
				for (int i = 0; i< bp.getData().size(); i++){
					System.out.println("Round: "+(i+1));
					for (int j = 0; j < bp.getData().get(i).size();j++){
						System.out.println(bp.getData().get(i).get(j).toString());
					}
				}
			}
			else{
				try{
					for (int j = 0; j < bp.getData().get(round).size();j++){
							System.out.println(bp.getData().get(round).get(j).toString());
					}
				}
				catch(IndexOutOfBoundsException e){
					System.out.println("ERROR: No results for round "+round+" available!");
					System.exit(0);
				}
			}
		} 
		else {
			printUsage();
		}
	}
	
	public static void printUsage(){
		System.out.println("Usage: java -jar blastparser.jar -i [Blast-output-file] (-r [results from which round? (int 1..n)])\n no -r shows all results");
	}

}
