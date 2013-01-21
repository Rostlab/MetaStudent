package pp2.prediction.knn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import pp2.go.DAG;
import pp2.go.Relations;
import pp2.tools.Tools;

/**
 * represents one function prediction target which is given in the BLAST results file
 * @author Thomas Hopf
 *
 */
public class PredictionTarget {

	public static final int NUM_ONTOLOGIES = 3;
	public static final int BIOLOGICAL_PROCESS = 0;
	public static final int MOLECULAR_FUNCTION = 1;
	public static final int CELLULAR_COMPONENT = 2;
	
	public static final double MAX_EVALUE = 1000;
	
	private static String newline = System.getProperty("line.separator");
	private static String tab = "\t";
	
	private String targetID;
	private Vector<BlastHit> blastHits = new Vector<BlastHit>();
	
	/*
	 *  getters and setters
	 */
	public void setTargetID(String targetID) {
		this.targetID = targetID;
	}
	
	public String getTargetID()
	{
		return targetID;
	}
	
	public void addBlastHit(String hitLine, boolean deleteDuplicates)
	{			
		if(deleteDuplicates)
		{
			String newID = hitLine.split("\t")[0];
			for(int i=0; i<blastHits.size(); i++)
			{
				if(blastHits.get(i).getHitID().equals(newID))
					blastHits.remove(i);
			}
		}
		
		blastHits.add(new BlastHit(hitLine));
	}
	
	public Vector<BlastHit> getBlastHits()
	{
		return blastHits;
	}
	
	/*
	 * methods
	 */
	
	private int ontologyToIndex(String ontology)
	{
		if(ontology.equals("molecular_function"))
			return MOLECULAR_FUNCTION;
		else {
			if(ontology.equals("biological_process"))
				return PredictionTarget.BIOLOGICAL_PROCESS;
			else
				return PredictionTarget.CELLULAR_COMPONENT;
		}
	}
	
//	private double limitRange(double value)
//	{
//		return Math.max(1E-308, Math.min(value, 1));
//	}
	
	private double getExponent(double eValue)
	{
		if(eValue <= 0)
			return MAX_EVALUE;
		else
			return -Math.log(eValue);
	}
	
	private String indexToOntology(int index)
	{
		switch(index)
		{
			case BIOLOGICAL_PROCESS: return "B";
			case MOLECULAR_FUNCTION: return "M";
			case CELLULAR_COMPONENT: return "C";
			default: return "-";
		}
	}
	
	public void predict(Relations relations, boolean useEValueThreshold, double eValueTreshold, int k, BufferedWriter naiveWriter, BufferedWriter leafWriter, double[] distribution) throws Exception
	{
//		System.out.println("predicting " + targetID);

//		System.out.print(".");
		// if we don't have any BLAST hits for the current target, predicting anything is pointless...
		if(blastHits.size() == 0)
			return;
		
		// sort result list in ascending order by e-Value
		Collections.sort(blastHits);
		
		// create a vector containing only the relevant Blast hits, i.e. those with an e-Value
		// below the threshold or the first k nearest neighbors
		Vector<BlastHit> blastHitsTmp = new Vector<BlastHit>(blastHits.size());
		
		int j=0;

		while(j < blastHits.size() && blastHits.get(j).getEValue() <= eValueTreshold && j < k)
		{
			blastHitsTmp.add(blastHits.get(j));
			j++;
		}

		
		// calculate the global quality score of this hit (i.e. the quantile of mean(E-Values) + 2*stddev(E-values)
		double quantile = 1;
		
		if(distribution != null)
		{
			// first, calculate mean
			double mean = 0;
			
			for(BlastHit b: blastHitsTmp)
				mean += getExponent(b.getEValue());
			
			mean /= blastHitsTmp.size();	
			//System.out.println("mean " + mean);
			
			// second, calculate standard deviation
			double stdDev = 0;
			
			for(BlastHit b: blastHitsTmp)
				stdDev += (getExponent(b.getEValue()) - mean) * (getExponent(b.getEValue()) - mean);
			
			// unbiased estimator: normalize by N-1
			stdDev = Math.sqrt(stdDev / (blastHitsTmp.size() - 1));
			//System.out.println("sd " + stdDev);
			
			// calculate quality score as mean+2*stddev
			double qualityScore = mean + 2*stdDev;
			//System.out.println("score " + qualityScore);
			
			// find which quantile the quality score corresponds to. This is the global quality score of the target
			int i = 0;
			while(i<100 && qualityScore >= distribution[i])
				i++;
			
			quantile = (i + 1)/(double)100;
			//System.out.println("quantile :" + quantile);
		}
		
		
//		System.out.println("before: " + blastHits.size() + " => after: " + blastHitsTmp.size());
		
//		for(BlastHit b: blastHits)
//		{
//			System.out.println(b.getHitID() + ": " + b.getEValue());
//		}
		
		/*
		 * step 1: get a list of all unique GO terms of hits for this prediction and construct the DAG
		 *
		 * TODO: this is a bit redundant since DAG.java also converts the String[] parameter to a HashSet 
		 */
		HashSet<String> allGoTerms = new HashSet<String>();
		int totalNumGoTerms = 0;
		
		for(BlastHit b : blastHitsTmp)
			for(String s : b.getGoTerms())
			{
				allGoTerms.add("GO:" + s);
				totalNumGoTerms++;
			}	
		
		DAG dag = new DAG(relations, (String [])allGoTerms.toArray(new String[0]));
		HashSet<BlastHit> normalizationHits = new HashSet<BlastHit>();
		
		// check whether there are any valid nodes or if we lost everything due to filtering cellular component annotations.
		// if we did, it makes no sense to make a prediction
		if(dag.getLeaves().length <= 0) 
		{
			System.out.println("no DAG nodes for " + targetID + " after CC filtering, skip");
			return;
		}
		
		/*
		 * step 2: add annotations to the DAG by iterating over all evidence (= BLAST hits)
		 */
		
		// once again, iterate over all BLAST hits
		for(BlastHit b : blastHitsTmp)
		{			
			// iterate over all GO terms of the current hit
			for(String s: b.getGoTerms())
			{
				//b.activateOntologySupport(relations.getType("GO:"+s));
				
				// attach to corresponding nodes in the DAG
				DAGNode n = null;
				if((n = dag.getDAGNode("GO:" + s)) != null)
				{
					n.addHit(b);
					// update which GO ontologies are supported by the hit, do only those that weren't filtered before
					
					String r = relations.getType("GO:"+s);
					if(r == null)	// TODO: remove me
						System.err.println("error! invalid: GO:"+s + " for target " + this.targetID);
					
					b.activateOntologySupport(r);
					
					// also add evidence to all "ancestors" in the DAG
					for(String ancestor: dag.getAncestors("GO:"+s))
						dag.getDAGNode(ancestor).addHit(b); // here no additional "GO:" is necessary because the getAncestors() method already returns correct IDs				
				}
			}
			
		}
		
		/*
		 * step 3: calculate normalization values
		 */
		double[] eValueSum = new double[NUM_ONTOLOGIES];
		double[] eValueSumSquared = new double[NUM_ONTOLOGIES];
		
		double[] eValueSumLog = new double[NUM_ONTOLOGIES];
		double[] eValueSumLogSquared = new double[NUM_ONTOLOGIES];
		
		double[] bitScoreSum = new double[NUM_ONTOLOGIES];
		double[] bitScoreSumSquared = new double[NUM_ONTOLOGIES];
		
		int[] unweightedSum = new int[NUM_ONTOLOGIES];
		
		for(BlastHit b: blastHitsTmp)
		{
			//double exponent = -Math.log(limitRange(b.getEValue()));
			double exponent = getExponent(b.getEValue());
			//System.out.println(exponent); 
			double exponentLog = Math.log(exponent);
			
			for(int i=0; i<NUM_ONTOLOGIES; i++)
			{
				if(b.supports(i))
				{
					unweightedSum[i]++;
					
					eValueSum[i] += exponent;
					eValueSumSquared[i] += exponent*exponent;			
					
					eValueSumLog[i] += exponentLog;
					eValueSumLogSquared[i] += exponentLog*exponentLog;
					
					bitScoreSum[i] += b.getBitScore();
					bitScoreSumSquared[i] += b.getBitScore();
				}
			}
		}
		
		/*
		 * step 4: score all nodes in the DAG
		 */
		naiveWriter.write(">" + targetID + newline);
		for(String s: dag.getDAGNodes().keySet())
		{
			DAGNode d = dag.getDAGNodes().get(s);
			double eValueNom = 0, eValueLogNom = 0, bitScoreNom = 0;
			int unweightedNom = 0;
			
			for(BlastHit b: d.getSupportingHits())
			{
				unweightedNom++;
				
				//double exponent = -Math.log(limitRange(b.getEValue()));
				double exponent = getExponent(b.getEValue());
				
				eValueNom += exponent;
				eValueLogNom += Math.log(exponent);
				bitScoreNom += b.getBitScore();				
			}
			
			int index = ontologyToIndex(relations.getType(s));
		//	System.out.println(index + " -> " + eValueSum[index]);
		//	System.out.print(index);
			
			d.setUnweightedSCore((double)unweightedNom / (unweightedSum[index] > 0 ? unweightedSum[index] : 1));
			
			d.seteValueScore(eValueNom / (eValueSum[index] > 1 ? eValueSum[index] : 1));
		//	d.seteValueSquaredScore(eValueNom / (eValueSumSquared[index] > 1 ? eValueSumSquared[index] : 1));
			
			d.seteValueLogScore(eValueLogNom / (eValueSumLog[index] > 1 ? eValueSumLog[index] : 1));
		//	d.seteValueLogSquaredScore(eValueLogNom / (eValueSumLogSquared[index] > 1 ? eValueSumLogSquared[index] : 1));
			
			d.setBitScoreScore(bitScoreNom / (bitScoreSum[index] > 1 ? bitScoreSum[index] : 1));
		//	d.setBitScoreSquaredScore(bitScoreNom / (bitScoreSumSquared[index] > 1 ? bitScoreSumSquared[index] : 1));
			
			
			naiveWriter.write(s + tab + indexToOntology(index) + tab + Tools.round(d.getUnweightedScore()) + tab + Tools.round(d.geteValueScore()) + tab + Tools.round(d.geteValueLogScore()));	
			if(distribution != null)
				naiveWriter.write(tab + Tools.round(d.getUnweightedScore() * quantile) + tab + Tools.round(d.geteValueScore() * quantile) + tab + Tools.round(d.geteValueLogScore() * quantile));
		
			naiveWriter.write(newline);

			//			System.out.println(s + ": " + d.geteValueScore() + "\t" + "\t" + d.getUnweightedScore() + "\t" + relations.getType(s) + "\t" + eValueSum[index]);
			// ontologies
		}
		naiveWriter.write("//" + newline); // + Character.LINE_SEPARATOR);
		
		// TODO : 1 - score?
		// TODO: unweighted kNN
		// TODO: bitscore macht keinen sinn da direkt abhängig von E-value...http://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
		
		/*
		 * step 5: score leaves and output
		 */
//		System.out.println("biological_process: " + unweightedSum[BIOLOGICAL_PROCESS] + ", " + eValueSum[BIOLOGICAL_PROCESS]);
//		System.out.println("molecular_function: " + unweightedSum[MOLECULAR_FUNCTION] + ", " + eValueSum[MOLECULAR_FUNCTION]);
//		System.out.println("cellular_component: " + unweightedSum[CELLULAR_COMPONENT] + ", " + eValueSum[CELLULAR_COMPONENT]);
		
		leafWriter.write(">" + targetID + newline);
		for(String l: dag.getLeaves())
		{
			DAGNode currentNode = dag.getDAGNodes().get(l);
			
			double leafScoreEValue = currentNode.geteValueScore();
			double leafScoreEValueLog = currentNode.geteValueLogScore();
			double leafScoreUnweighted = currentNode.getUnweightedScore();
			
			for(String m: dag.getAncestors(l))
			{
				leafScoreEValue += dag.getDAGNodes().get(m).geteValueScore();
				leafScoreEValueLog += dag.getDAGNodes().get(m).geteValueLogScore();
				leafScoreUnweighted += dag.getDAGNodes().get(m).getUnweightedScore();
			}
			
			int N = dag.getAncestors(l).length + 1;
			currentNode.setLeafScoreEValue(leafScoreEValue / N);
			currentNode.setLeafScoreEValueLog(leafScoreEValueLog / N);
			currentNode.setLeafScoreUnweighted(leafScoreUnweighted / N);
		
//			System.out.println("->" + l + " " + dag.getDAGNodes().get(l).getSupportingHits().size() + " " + dag.getDAGNodes().get(l).geteValueScore() + " "  + dag.getDAGNodes().get(l).getLeafScoreEValue() + " " + relations.getType(l));
			leafWriter.write(l + tab + indexToOntology(ontologyToIndex(relations.getType(l))) + tab + Tools.round(currentNode.getLeafScoreUnweighted()) + tab + Tools.round(currentNode.getLeafScoreEValue()) + tab + Tools.round(currentNode.getLeafScoreEValueLog()));
			if(distribution != null)
				leafWriter.write(tab + Tools.round(currentNode.getLeafScoreUnweighted() * quantile) + tab + Tools.round(currentNode.getLeafScoreEValue() * quantile) + tab + Tools.round(currentNode.getLeafScoreEValueLog() * quantile));
		
			leafWriter.write(newline);
		}
		leafWriter.write("//" + newline);
//			if(relations.getType(l).equals("biological_process"))
//				; //continue;
//			
//			System.out.println("->" + l + " " + dag.getDAGNodes().get(l).getSupportingHits().size() + " " + dag.getDAGNodes().get(l).geteValueScore() + " " + relations.getType(l));
//			System.out.print("\t");
//			for(String m: dag.getAncestors(l))
//			{
//				//System.out.print(" | " + m + "="+dag.getDAGNodes().get(m).geteValueScore());
//				System.out.print(" | " + m + "="+dag.getDAGNodes().get(m).getSupportingHits().size());
//			}
//			System.out.println();
//		}
		
	}
	
	
}
