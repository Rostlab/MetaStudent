import java.util.ArrayList;


public class TabulatorFormat {

	private int round;
	private ArrayList<QueryResult> queryResultList = new ArrayList<QueryResult>();
	private ArrayList<String> outputList = new ArrayList<String>();
	
	public TabulatorFormat() {
	}
	
	public ArrayList<String> getOutput() {
		for(int i = 0;i < queryResultList.size();i++) {
			outputList.add("#" + queryResultList.get(i).getName()+
					"\t"+queryResultList.get(i).getGoValuesFromString()+
					"\t"+queryResultList.get(i).getEvalue()+
					"\t"+(int)queryResultList.get(i).getScore()+
					"\t"+queryResultList.get(i).getLength()+
					"\t"+queryResultList.get(i).getIdentities()+
					"\t"+queryResultList.get(i).getGaps()+
					"\t"+round);
		}
		return outputList;
	}
	
	public void addRound(int a) {
		round = a;
	}

	public String getRound() {
		return "" + round;
	}

	public void addQuery(QueryResult queryResult) {
		queryResultList.add(queryResult);
	}

}


