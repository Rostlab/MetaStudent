
public class QueryResult {

	private String goTerm;
	private String length;
	private double score;
	private String eValue;
	private int numberIdontKnow;
	private String identities;
	private String positives;
	private int identitiesPercent;
	private int positivesPercent;
	private String gaps = "0";
	private int gabsPercent;
	private String name;
	
	public QueryResult() {	
	}

	public void addGoTerms(String term) {
		goTerm = term;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public String getName() {
		return name;
	}
	
	public String getGoTerm() {
		return goTerm;
	}
	
	public void addLength(String l) {
		length = l;
	}
	
	public String getLength() {
		return length;
	}
	
	public void addScore(double s) {
		score = s;
	}
	
	public double getScore() {
		return score;
	}
	
	public void addNumber(int bla) {
		numberIdontKnow = bla;
	}
	
	public int getNumber() {
		return numberIdontKnow;
	}
	
	public void addEValue(String e) {
		eValue = e;
	}
	
	public String getEvalue() {
		return eValue;
	}
	
	public void addIdentities(String i) {
		identities = i;
	}
	
	public String getIdentities() {
		return identities;
	}
	
	public void addIdenititesPercent(int i) {
		identitiesPercent = i;
	}
	
	public String getIdentitiesPercent() {
		return "" + identitiesPercent + "%";
	}
	
	public int getIdentitiesPercentAsInt() {
		return identitiesPercent;
	}
	
	public void addPositivesPercent(int p) {
		positivesPercent = p;
	}
	
	public String getPositivesPercent() {
		return "" + positivesPercent + "%";
	}
	
	public int getPositivesPercentAsInt() {
		return positivesPercent;
	}
	
	public void addPositives(String p) {
		positives = p;
	}
	
	public String getPositives() {
		return positives + "/" + length;
	}
	
	public void addGaps(String g) {
		gaps = g;
	}
	
	public String getGaps() {
		return gaps;
	}
	
	public void addGapsPercent(int g) {
		gabsPercent = g;
	}
	
	public String getGabsPercent() {
		return "" + gabsPercent + "%";
	}
	
	public int getGabsPercentAsInt() {
		return gabsPercent;
	}
	
	public String getGoValuesFromString() {
		return goTerm.replace("GO:", "");
	}
	
}
