package pp2.prediction.knn;


/**
 * Stores a BLAST hit with associated information such as E-value etc.
 * @author Thomas Hopf
 *
 */
public class BlastHit implements Comparable<BlastHit>{

	// FASTA identifier of the hit
	private String hitID;
	
	// list of all associated go terms
	private String[] goTerms;
	
	// all measures of the hit
	private double eValue;
	private double bitScore;
	private int length;
	
	private int sequenceIdentity;
	private int sequenceIdentityLength;
	private int gaps;
	private int gapsLength;
	
	// PSI-Blast iteration in which the hit was found
	private int iteration;
	
	// holds which ontologies are supported by this hit (important for calculating normalization values)
	private boolean[] supports;
//	private boolean supportsMolecularFunction = false;
//	private boolean supportsBiologicalProcess = false;
//	private boolean supportsCellularComponent = false;
	
	public BlastHit(String hitLine)
	{
		try {
			String[] split = hitLine.split("\t");
			//System.out.println(Arrays.toString(split));
			
			// extract all information from the hitLine
			hitID = split[0];
			goTerms = split[1].split(",");
			
			// if eValue has format e-117, we need to add a "1" as prefix to avoid a NumberFormatException
			eValue = Double.parseDouble(split[2].startsWith("e") ? ("1"+split[2]): split[2]);
			
			bitScore = Double.parseDouble(split[3]);
			length = Integer.parseInt(split[4]);
			sequenceIdentity = Integer.parseInt(split[5].split("/")[0]);
			sequenceIdentityLength = Integer.parseInt(split[5].split("/")[1]);
			if(split[6].equals("0"))
			{
				gaps = 0;
				gapsLength = length;
			}
			else
			{
				gaps = Integer.parseInt(split[6].split("/")[0]);
				gapsLength = Integer.parseInt(split[6].split("/")[1]);
			}
			iteration = Integer.parseInt(split[7]);
			
			supports = new boolean[PredictionTarget.NUM_ONTOLOGIES];
		} catch(Exception e) {
			System.err.println("an error occurred while parsing a blast hit line:");
			System.err.println(hitLine);
			e.printStackTrace();
			//System.err.println(e);
		}
		//System.out.println(hitID + "->" + goTerms + ", " + eValue);
	}
	
	// deprecated
	public BlastHit(String goTerms, double eValue)
	{
		this.goTerms = goTerms.split(",");
		this.eValue = eValue;
	}
	
	public double getEValue()
	{
		return eValue;
	}
	
	public String[] getGoTerms()
	{
		return goTerms;
	}
	
	public double getBitScore()
	{
		return bitScore;
	}
	
	public String getHitID()
	{
		return hitID;
	}
	
	public boolean supports(int ontologyType) {
		return supports[ontologyType];
	}

	public void activateOntologySupport(String ontology)
	{
		if(ontology.equals("molecular_function"))
			supports[PredictionTarget.MOLECULAR_FUNCTION] = true;
		else {
			if(ontology.equals("biological_process"))
				supports[PredictionTarget.BIOLOGICAL_PROCESS] = true;
			else
				supports[PredictionTarget.CELLULAR_COMPONENT] = true;
		}
	}
	
	@Override
	public int compareTo(BlastHit o) {
				
		if(this.eValue < o.eValue)
			return -1;
		else {
			if (this.eValue > o.eValue)
				return 1;
			else
				return 0;
		}

	}

}
