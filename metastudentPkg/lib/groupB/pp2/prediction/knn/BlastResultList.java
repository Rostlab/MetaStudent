package pp2.prediction.knn;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import pp2.tools.Tools;

/**
 * stores a list of BLAST results and allows to rerank hits by E-Value etc., sequence identity etc.
 * @author Thomas Hopf
 *
 */
public class BlastResultList {

	private LinkedList<BlastHit> hits;
	
	/**
	 * stores all hits from blastOutputFile into a list of blast hits
	 * @param blastOutputFile
	 */
	public BlastResultList(String blastOutputFile)
	{
		hits = new LinkedList<BlastHit>();
		
		try {
			BufferedReader in = Tools.openFile(blastOutputFile);
			
			String line;
			while((line = in.readLine()) != null)
			{
				String goTerms = line.split("\t")[0];
				String eValue = line.split("\t")[1];
				
				// in some cases, the eValue looks like "e-133" which gives a parsing error, so fix that
				if(eValue.startsWith("e"))
					eValue = "1" + eValue;
					
				hits.add(new BlastHit(goTerms, Double.parseDouble(eValue)));
			}
			in.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LinkedList<BlastHit> getHits()
	{
		return hits;
	}

	
}
