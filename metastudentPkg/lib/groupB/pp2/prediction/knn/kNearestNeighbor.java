package pp2.prediction.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 * @author Thomas Hopf
 *
 */
public class kNearestNeighbor {

	/*
	 * constants for different node scoring methods
	 */
	public static final int EVAL_UNWEIGHTED = 1;	// this is unweighted kNN
	public static final int EVAL_LOG_SCORING = 2;
	public static final int EVAL_LOG_LOG_SCORING = 3;
	//public static final int SEQID_SCORING = 4;

	/**
	 *  limits the range of E-values such that log values are meaningful (i.e., -log(value) is defined and non-negative)
	 */
	private double limitRange(double value)
	{
		return Math.max(1E-308, Math.min(value, 1));
	}
	
	/**
	 * 
	 * @param blastHits file like "go_test_output". must already contain fully added GO paths!
	 * @param scoringMethod determines type of sequence weighting, specified by one of the constants EVAL_*
	 * @param scoringLambda constant by which EVAL_LOG scores are multiplied in the end
	 * @param useEValueThreshold determines whether conventional kNN or a fixed E-Value threshold is used to include BLAST hits
	 * @param eValueThreshold if useEvalueThreshold == true, then all hits with an E-Value below this parameter are included
	 * @param k if conventional kNN is used, this determines the fixed number of neighbors to use, if there are enough
	 * @return
	 */
	public ArrayList<GoNode> predictFunction(BlastResultList blastHits, int scoringMethod, boolean useEValueThreshold, float eValueThreshold, int k)
	{
		// all GO term nodes
		HashMap<String, GoNode> nodes = new HashMap<String, GoNode>();
		
		// reliability values of each BLAST hit (e.g. E-Value per hit)
		LinkedList<Double> normalizationValues = new LinkedList<Double>();
		int i = 0;
		
		/*
		 * iterate over all hits in the BLAST file
		 */
		for(BlastHit h: blastHits.getHits())
		{
			// only consider first k hits if doing conventional kNN
			if(!useEValueThreshold && i>=k)
				break;
			
			// if doing "E-Value threshold"-kNN, skip current hit (for safety: not assuming the blast hit list is sorted by E-value)
			if(useEValueThreshold && h.getEValue() > eValueThreshold)
				continue;
			
			// iterate over all go terms of the current hit
			for(String goTerm: h.getGoTerms())
			{
				// check if we already had that GO term, otherwise create a new GO term node.
				// in both cases add the index and the reliability score (e.g. E-value) of the blast hit that supports the node
				if(nodes.containsKey(goTerm)){
					nodes.get(goTerm).addHit(i, h.getEValue());
				} else {
					nodes.put(goTerm, new GoNode(goTerm, i, h.getEValue()));
				}
			}
			
			// add current hit to scoring function normalization factor list
			normalizationValues.add(h.getEValue());
			
			i++;
		}
		
		/*
		 * calculate normalization factor
		 */
		double normalizationFactor = 0;
		for(double value: normalizationValues)
		{
			value = limitRange(value);
			
			switch(scoringMethod) {
				case EVAL_UNWEIGHTED:
					normalizationFactor += 1;
					break;
				case EVAL_LOG_SCORING:
					normalizationFactor += -Math.log(value);
					break;
				case EVAL_LOG_LOG_SCORING:
					normalizationFactor += Math.log((-Math.log(value)));
					break;
				default: break;
			}
		}

		// slight hack for safety: ensure normalization factor is nonzero
		if(normalizationFactor <= 0.01)
			normalizationFactor = 1;
		
		//System.out.println("normalization factor: " + normalizationFactor);
		
		
		/*
		 * iterate over all nodes to calculate their individual score
		 */
		for(GoNode node: nodes.values())
		{
			double score = 0;
			
			for(double value: node.getReliabilities())
			{
				value = limitRange(value);

				switch(scoringMethod) {
					case EVAL_UNWEIGHTED:
						score += 1;
						break;
					case EVAL_LOG_SCORING:
						score += -Math.log(value);
						break;
					case EVAL_LOG_LOG_SCORING:
						score += Math.log((-Math.log(value)));
						break;
					default: break;
				}	
			}
			node.setScore(score / normalizationFactor);
			
			//System.out.println(node.getGoTerm() + ":" + node.getReliabilities().size() + "->" + node.getScore());
		}
		
		ArrayList<GoNode> resultNodeList = new ArrayList<GoNode>(nodes.values());
		Collections.sort(resultNodeList);
		return resultNodeList;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// nimmt an, dass go_test_output aufsteigend nach E-values sortiert ist, und bereits die vollen GO-pfade enthält 
		BlastResultList brl = new BlastResultList("examples/go_test_output");
		
		kNearestNeighbor knn = new kNearestNeighbor();
		
		System.out.println("#GOterm\t\tnum seq\tscore");
		for(GoNode g: knn.predictFunction(brl, kNearestNeighbor.EVAL_LOG_LOG_SCORING, true, 1, 9))
			System.out.println(g.getGoTerm() + "\t" + g.getReliabilities().size() + "\t" + g.getScore());

		// TODO: die result-liste kann noch nodes mit score 0 enthalten!
		// TODO: kommandozeilenparameter, output file schreiben etc.
		
	}

}
