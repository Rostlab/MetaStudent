import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;


public class GoTree
{
	//TODO go nummern eintragen
	public final static String biolproc = "bio_proc";
	public final static String molfunction = "mol_func";
	public final static String cellcomp = "cel_comp";
	
	Hashtable<String, ArrayList<String>> goTree;
	Hashtable<String, Integer> golength;
	Hashtable<String, String> goRoot;
	
	public GoTree(String file){
		goTree = new Hashtable<String, ArrayList<String>>();
		golength =  new Hashtable<String, Integer>();
		goRoot = new Hashtable<String, String>();
		
		try
		{
			read(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String getLowestCommonAncestor(String a, String b){
		
		ArrayList<String> a_ = goTree.get(a);
		ArrayList<String> b_ = goTree.get(b);
		
		String ancestor = "";
		int depth = 0;
		
		for(String s : a_){
			if(b_.contains(s)){
				if(s.equals("GO:0008150")) continue; // biol_proc
				if(s.equals("GO:0003674")) continue; // mol_func
				if(s.equals("GO:0005575")) continue; // cell_comp
				
				if(golength.get(s) > depth) {
					ancestor = s;
					depth = golength.get(s);
				}
			}
		}
		return ancestor;
	}
	
	public int getLengthToRoot(String goNo){
		if(!golength.keySet().contains(goNo))
		{
			return 0;
		}
		else
		{
			return golength.get(goNo);
		}
	}
	
	public String getCategory(String goNo){
		
		String root = goRoot.get(goNo);
		try{
			if(root.equals("null")){}			
		}catch(NullPointerException e){
			System.err.println("\n" + goNo + " not in Database.");
			System.err.println("ignoring: " + goNo);	
			return "unidentified";
		}
		return root;
	}
	public ArrayList<String> getTree(String goNo)
	{
		
		ArrayList<String> tree = goTree.get(goNo);
		try{
			if(tree.equals("null")){}			
		}catch(NullPointerException e){
			System.err.println("\n" + goNo + " not in Database.");
			System.err.println("ignoring: " + goNo);	
			return new ArrayList<String>();
		}
		return tree;
	}
	
	public boolean isAparentOfB(String a, String b){
		ArrayList<String> bTree = goTree.get(b);
		if (bTree.contains(a)) return true;
		else return false;
	}

	private void read(String file) throws IOException{
		BufferedReader buff = new BufferedReader(new FileReader(file));
		
		String line = "";
		while((line = buff.readLine()) != null){
			String[] temp = line.split("\t");
			golength.put(temp[0], Integer.parseInt(temp[2]));
			goRoot.put(temp[0], temp[3]);
			goTree.put(temp[0], splitGoNumbers(temp[1]));
		}
		
		
	}
	private ArrayList<String> splitGoNumbers(String gos){
		ArrayList<String> t = new ArrayList<String>();
		String[] temp = gos.split(",");
		for(String s : temp){
			t.add(s);
		}
		return t;
	}
}
