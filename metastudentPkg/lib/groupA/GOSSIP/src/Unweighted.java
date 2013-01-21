import java.util.ArrayList;
import java.util.Hashtable;

public class Unweighted extends Selector
{

	@Override
	public ArrayList<String> select()
	{
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		for (String s : this.gos)
		{
			s = s.replaceAll(">", "");
			String[] goss = s.split(",");
			for (String ss : goss)
			{
				hash.put(ss, 0);
			}
		}
		ArrayList<String> temp = new ArrayList<String>(hash.size());
		for (String s : hash.keySet())
		{
			temp.add(s);
		}
		return this.cleanList(temp);
	}

}
