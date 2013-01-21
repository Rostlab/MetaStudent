import java.util.ArrayList;
import java.util.Hashtable;


public class EValue extends Selector
{
	public EValue()
	{
		treshold = 0.6;
	}
	public EValue(double treshold){
		this.treshold = treshold;
		
		
	}
	
	public ArrayList<String> select()
	{
		int i = 0;
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		Hashtable<String, Double> hash2 = new Hashtable<String, Double>();
		for (String s : this.gos)
		{
			s = s.replaceAll(">", "");
			String[] goss = s.split(",");
			for (String ss : goss)
			{
				// falls es diese GO-Nummer schon gibt, wird der count um 1 erhoeht
				// E-Values werden addiert fuer Durchschnittsberechnung
				if (hash.containsKey(ss))
				{
					hash.put(ss, hash.get(ss) + 1);
					hash2.put(ss, hash2.get(ss) + this.eValues.get(i));					
				}
				else{
					hash.put(ss, 1);
					hash2.put(ss, this.eValues.get(i));
				}

			}
			i++;
		}
		ArrayList<String> temp = new ArrayList<String>(hash.size());
		for (String s : hash.keySet())
		{
			double score = (double)hash2.get(s)/hash.get(s); 
			if (score <= treshold){
				temp.add(s);
				this.probabilistic_estimate.put(s, score);
			}
		}
		return this.cleanList(temp);
	}

}
