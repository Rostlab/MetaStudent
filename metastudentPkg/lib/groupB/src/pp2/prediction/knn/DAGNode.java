package pp2.prediction.knn;

import java.util.HashSet;

/**
 * stores all information associated with one node of the GO DAG
 * @author Thomas Hopf
 *
 */
public class DAGNode {

	private HashSet<BlastHit> supportingHits;
	
	private double unweightedScore;
	
	private double eValueScore;
	private double eValueSquaredScore;
	
	private double eValueLogScore;
	private double eValueLogSquaredScore;
	
	private double bitScoreScore;
	private double bitScoreSquaredScore;
	
	private double leafScoreEValue;
	private double leafScoreEValueLog;
	private double leafScoreUnweighted;
	
	public double getLeafScoreUnweighted() {
		return leafScoreUnweighted;
	}

	public void setLeafScoreUnweighted(double leafScoreUnweighted) {
		this.leafScoreUnweighted = leafScoreUnweighted;
	}

	public double getLeafScoreEValueLog() {
		return leafScoreEValueLog;
	}

	public void setLeafScoreEValueLog(double leafScoreEValueLog) {
		this.leafScoreEValueLog = leafScoreEValueLog;
	}

	public double getLeafScoreEValue() {
		return leafScoreEValue;
	}

	public void setLeafScoreEValue(double leafScore) {
		this.leafScoreEValue = leafScore;
	}

	public DAGNode()
	{
		this.supportingHits = new HashSet<BlastHit>();
	}
	
	public void addHit(BlastHit hit)
	{
		supportingHits.add(hit);
	}

	public HashSet<BlastHit> getSupportingHits()
	{
		return supportingHits;
	}
	
	public double geteValueScore() {
		return eValueScore;
	}

	public void seteValueScore(double eValueScore) {
		this.eValueScore = eValueScore;
	}

	public double geteValueSquaredScore() {
		return eValueSquaredScore;
	}

	public void seteValueSquaredScore(double eValueSquaredScore) {
		this.eValueSquaredScore = eValueSquaredScore;
	}

	public double geteValueLogScore() {
		return eValueLogScore;
	}

	public void seteValueLogScore(double eValueLogScore) {
		this.eValueLogScore = eValueLogScore;
	}

	public double geteValueLogSquaredScore() {
		return eValueLogSquaredScore;
	}

	public void seteValueLogSquaredScore(double eValueLogSquaredScore) {
		this.eValueLogSquaredScore = eValueLogSquaredScore;
	}

	public double getBitScoreScore() {
		return bitScoreScore;
	}

	public void setBitScoreScore(double bitScoreScore) {
		this.bitScoreScore = bitScoreScore;
	}

	public double getBitScoreSquaredScore() {
		return bitScoreSquaredScore;
	}

	public void setBitScoreSquaredScore(double bitScoreSquaredScore) {
		this.bitScoreSquaredScore = bitScoreSquaredScore;
	}
	
	public void setUnweightedSCore(double unweightedScore)
	{
		this.unweightedScore = unweightedScore;
	}
	
	public double getUnweightedScore()
	{
		return unweightedScore;
	}
	
	
}
