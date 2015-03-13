import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

// evtl. sollte der predictor noch den pfad zum CAFA-file bekommen, damit die predict-methode direkt in das file schreiben kann
public class Predictor
{
	String dbPath;
	Selector selector;
	String valFile;
	
	//int blastIterations = Constants.NUM_BLAST_ITERATIONS;
	
	//k nearest!!
	int k = Constants.K;
	
	public Predictor(String dbPath, Selector selector)
	{
		this.selector = selector;
	}
	public Predictor(Selector selector, String valFile)
	{
		this.selector = selector;
		this.valFile = valFile;
	}
	
	public void predict(int i)
	{
		try
		{
			blast(i);
			ArrayList<String> results = selector.select();
			//System.out.println(results.size());
			
			//writeValFile(id, results);
			writeCAFAFile(GOSSIPSTarter.fastaEntries.get(i)[0], results, selector.probabilistic_estimate);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	
//	public void predict(String id, String sequence, String blastResult)
//	{
//		try
//		{
//			blast(blastResult);
//			ArrayList<String> results = selector.select();
//			//System.out.println(results.size());
//			
//			//writeValFile(id, results);
//			writeCAFAFile(id, results, selector.probabilistic_estimate);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}		
//	}
	public void writeCAFAHead() throws IOException{
		BufferedWriter buff = new BufferedWriter(new FileWriter(valFile + ".cafa"));
		buff.append("AUTHOR: 3 Engel fuer Cafa\n");
		buff.append("MODEL: 1\n");
		buff.append("KEYWORDS:     \n");
		buff.flush();
		buff.close();
		
	}
	public void writeCAFATail() throws IOException{
		BufferedWriter buff = new BufferedWriter(new FileWriter(valFile + ".cafa", true));
		buff.append("END");
		buff.flush();
		buff.close();
	}
	private void writeCAFAFile(String id, ArrayList<String> predicted, Hashtable<String, Double> table)throws IOException{
		BufferedWriter buff = new BufferedWriter(new FileWriter(valFile + ".cafa", true));
		for(String s : predicted){
			double score = table.get(s);
			buff.append(id + "\t" + s + "\t" + score + "\n");
		}
		
		buff.flush();
		buff.close();
		
	}
	
//	private void writeValFile(String id, ArrayList<String> predicted) throws IOException{
//		BufferedWriter buff = new BufferedWriter(new FileWriter(valFile + ".val",true));
//		//String buff = "";
//		System.out.println(predicted.size() + " GOs found for " + id);
//		if(predicted.size() == 0){
//			buff.append(id + "\t" + "#\n");
//		}
//		else if(predicted.size() == 1){
//			buff.append(id + "\t" + predicted.get(0) + "\n");				
//		}
//		else{
//			buff.append(id + "\t" + predicted.get(0) +",");	
//			for(int i = 1; i < predicted.size() - 1; i++){
//				//buff += id + "\t" + gos.get(i).replace(">", "") + "\n";
//				buff.append(predicted.get(i) + ",");		
//			}
//			buff.append(predicted.get(predicted.size() - 1) + "\n");			
//		}
//		
//		buff.flush();
//		buff.close();
//	}

	
	private void blast(int index) throws IOException{
		
		String blastResult = GOSSIPSTarter.indivdResults.get(index);
		
		// blast aufrufen
		selector.initialize();
		
		boolean read = false;
		boolean go = false;
		
		String[] lines = blastResult.split(GOSSIPSTarter.NL);
		
		String actGo = "";
		for(int i = 0; i < lines.length; i++)
		{	
			if(   (Constants.NUM_BLAST_ITERATIONS==1 && lines[i].startsWith("Searching")) || (Constants.NUM_BLAST_ITERATIONS>1 && lines[i].startsWith("Results from round "))   )
			{	
				selector.gos.clear();
				selector.length.clear();
				selector.blastScores.clear();
				selector.eValues.clear();
				selector.identity.clear();
				selector.positives.clear();
				selector.gaps.clear();
				
				//System.out.println("READING");
				read = true;
			}
			if(read){
				if(lines[i].startsWith(">") && lines[i].contains("|")  ){
					lines[i] = lines[i].split("\\|")[1];
					//System.out.println("HITT");
					go = true;
					actGo = "";
				}
				if(go){
					if(!lines[i].contains("Length")){
						actGo += lines[i];
					}
					else{
						go = false;
						actGo =  actGo.replaceAll("\\s+", "");
						selector.gos.add(actGo);
						selector.length.add(Integer.parseInt(lines[i].split(" = ")[1]));
						i++;
						//System.out.println(line);
						i++;
						//System.out.println(line);
						lines[i] = lines[i].trim();
						String[] kram = lines[i].split("\\s+");
						selector.blastScores.add(Double.parseDouble(kram[2]));
						if(kram[7].startsWith("e")) 
							kram[7] = "0.0";
						selector.eValues.add(Double.parseDouble(kram[7].replace(",", "")));
						i++;
						//System.out.println(line);
						lines[i] = lines[i].replaceAll("[%(),]", "");
						lines[i] = lines[i].trim();
						kram = lines[i].split("\\s+");
						selector.identity.add(Double.parseDouble(kram[3]));
						selector.positives.add(Double.parseDouble(kram[7]));
						if (lines[i].contains("Gaps")) selector.gaps.add(Double.parseDouble(kram[kram.length - 1]));
						else selector.gaps.add(0.0);
					}
				}
			}
			
			if(selector.gos.size() == k) break;
		}
		//if(selector.gos.size() == 0) System.exit(1);
		System.out.println(selector.gos.size() + " sequences found");
		
	}
}

