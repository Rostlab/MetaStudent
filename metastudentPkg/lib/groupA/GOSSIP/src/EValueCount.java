import java.util.ArrayList;
import java.util.Hashtable;


public class EValueCount extends Selector
{

	public EValueCount()
	{
		treshold = 0.6;
	}
	public EValueCount(double treshold)
	{
		this.treshold = treshold;
		
		
	}
	
	
	public ArrayList<String> select()
	{
		int i = 0;
		Hashtable<String, Integer> counts = new Hashtable<String, Integer>();
		Hashtable<String, Double> eValues = new Hashtable<String, Double>();
		
		for (String s : this.gos)
		{
			s = s.replaceAll(">", "");
			String[] goss = s.split(",");
			for (String ss : goss)
			{
				//System.out.println(ss);
				
				// falls es diese GO-Nummer schon gibt, wird der count um 1 erhoeht
				// E-Values werden addiert fuer Durchschnittsberechnung
				if (counts.containsKey(ss))
				{
					counts.put(ss, counts.get(ss) + 1);
					eValues.put(ss, eValues.get(ss) + this.eValues.get(i));					
				}
				else{
					counts.put(ss, 1);
					eValues.put(ss, this.eValues.get(i));
				}

			}
			i++;
		}
		//System.out.println(counts.size());
		ArrayList<String> temp = new ArrayList<String>(counts.size());
		for (String s : counts.keySet())
		{
			//System.out.println(s);
			//double negEValue = -(double)eValues.get(s);
			double invertedEValue = 1 - (double)eValues.get(s);
			double goPercentage = counts.get(s)/this.gos.size(); 
			
			// teilen durch 2 weil der  1-E-Value max 1 ist und die Percentage ebenfalls.. mit /2 wird der Score auf 1 normiert
			double score = (invertedEValue + goPercentage) / 2;
			if (score >= treshold) {
				this.probabilistic_estimate.put(s, score);
				temp.add(s);
			}
		}
		//System.out.println(temp.size());
		return this.cleanList(temp);
	}

}
