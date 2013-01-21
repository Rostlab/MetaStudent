package pp2.prediction.knn;

import java.util.LinkedList;

/**
 * for each GO term, stores by which sequences it is supported  
 * @author Thomas Hopf
 *
 */
public class GoNode implements Comparable<GoNode> {
	
	private String goTerm;
	private LinkedList<Double> reliabilities;
	private LinkedList<Integer> hitIndex;
	private double score;
	
	public GoNode(String goTerm, int index, double reliability) 
	{
		this.goTerm = goTerm;
		hitIndex = new LinkedList<Integer>();
		reliabilities = new LinkedList<Double>();
		
		hitIndex.add(index);
		reliabilities.add(reliability);
	}
	
	public void addHit(int index, double reliability) 
	{
		hitIndex.add(index);
		reliabilities.add(reliability);
	}
	
	public LinkedList<Integer> getHitIndex()
	{
		return hitIndex;
	}
	
	public LinkedList<Double> getReliabilities()
	{
		return reliabilities;
	}
	
	public void setScore(double score)
	{
		this.score = score;
	}
	
	public double getScore()
	{
		return score;
	}
	
	public String getGoTerm()
	{
		return goTerm;
	}

	@Override
	public int compareTo(GoNode o) {
		
		// highest scores are "smallest" so they end up as the first list elements
		
		if(this.score < o.score)
			return 1;
		else {
			if (this.score > o.score)
				return -1;
			else
				return 0;
		}

	}

	
}
