import java.util.ArrayList;
import java.util.Hashtable;


public class SimpleCount extends Selector
{
	public SimpleCount()
	{
		treshold = 0.6;
	}
	public SimpleCount(double treshold){
		this.treshold = treshold;
	}

	public ArrayList<String> select()
	{
		
		
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		for (String s : this.gos)
		{
			s = s.replaceAll(">", "");
			String[] goss = s.split(",");
			for (String ss : goss)
			{
				// falls es diese GO-Nummer schon gibt, wird der count um 1 erhoeht
				if (hash.containsKey(ss))
				{
					hash.put(ss, hash.get(ss)+1);
				}
				else hash.put(ss, 1);
			}
		}
		ArrayList<String> temp = new ArrayList<String>(hash.size());
		for (String s : hash.keySet())
		{
			double score = (double)hash.get(s)/this.gos.size(); 
			if (score >= treshold) {
				temp.add(s);
				this.probabilistic_estimate.put(s, score);
			}
		}
		
		return this.cleanList(temp);
	}

}
