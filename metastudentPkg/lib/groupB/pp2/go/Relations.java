package pp2.go;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * example constructor:
 * Relations test = new Relations("./../data/deductiveClosureGO.txt","./../data/goterm.txt",true);
 * noCC stands for no Cellular Component (for CAFA, true, since CC is ignored)		
 * @author Mark Heron
 *
 */
public class Relations {
	
	HashMap<String, String[]> ahnen;
	HashMap<String, String> type;
	boolean noCC;
	
	public Relations(String closure, String terms, boolean noCC) {

		BufferedReader input;

		this.noCC = noCC;
		
		// create type
		type = new HashMap<String, String>();
		input = pp2.tools.Tools.openFile(terms);

		try {
			String line;
			while((line = input.readLine()) != null) {
				
				String[] split = line.split("\t");
				if(split.length > 4) {
					
					String gokey = split[3];
					String typevalue = split[2];
					
					type.put(gokey, typevalue);
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// create ancestors/parents
		ahnen = new HashMap<String, String[]>();
		HashMap<String, HashSet<String>> ahnen_tmp = new HashMap<String, HashSet<String>>();
		
		input = pp2.tools.Tools.openFile(closure);

		try {
			String line;
			while((line = input.readLine()) != null) {
				
				if(line.startsWith("GO:")) {
					
					String[] split = line.split("\t");
					String key = split[0];
					String value = split[2];
					if(getType(key).length() > 0 && getType(value).length() > 0 ) {
						if(isTypeofInterst(key) && isTypeofInterst(value)) {
							if(ahnen_tmp.containsKey(key)) {
								ahnen_tmp.get(key).add(value);
							} else {
								HashSet<String> ll = new HashSet<String>();
								ll.add(value);
								ahnen_tmp.put(key, ll);
							}
						}
					}
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String key : ahnen_tmp.keySet()) {
			String[] values = new String[0];
			values = ahnen_tmp.get(key).toArray(values);
			ahnen.put(key, values);
		}
		
	}
	
	
	
	public String[] getAncestors(String goterm) {
		if(ahnen.containsKey(goterm)) {
			return ahnen.get(goterm);
		} else {
			return new String[0];
		}
	}
	
	
	
	public String getType(String goterm) {
		if (type.containsKey(goterm)) {
			return type.get(goterm);
		} else {
			return "";
		}
	}
	
	public boolean isTypeofInterst(String goterm) {
		if(!type.containsKey(goterm)) { // only happens for GO:0048220
			return true;				// no idea why it is missing in goterm.txt
		}
		String tmp = type.get(goterm);
		if(tmp.equals("biological_process") || tmp.equals("molecular_function") || tmp.equals("cellular_component")) {
			return true;
		} else if (!noCC && tmp.equals("cellular_component")){
			return true;
		} else {
			return false;
		}
	}
	
	
	
	public String[][] getExclusivePaths(String[] goterms) {
		
		String[][] results = new String[goterms.length][];
		HashSet<String> allreadydone = new HashSet<String>();
		
		for (int i = 0; i < goterms.length; i++) {
			String goterm = goterms[i];
			String[] anc = getAncestors(goterm);
			HashSet<String> exc = new HashSet<String>();
			
			for (String a : anc) {
				if( ! allreadydone.contains(a)) {
					exc.add(a);
					allreadydone.add(a);
				}
			}
			String[] tmp = new String[0];
			results[i] = (String[]) exc.toArray(tmp);
		}
		return results;
	}
	
	
	
	public static void main(String[] args) {
		
		Relations test = new Relations("C:\\Dokumente und Einstellungen\\murgs\\Eigene Dateien\\Studium\\9. Semester\\PP2\\ProteinPrediction2\\data\\deductiveClosureGO.txt"
				,"C:\\Dokumente und Einstellungen\\murgs\\Eigene Dateien\\Studium\\9. Semester\\PP2\\ProteinPrediction2\\data\\goterm.txt",true);
		String[] tmp = {"GO:999","GO:0005737","GO:0019134","GO:0000287","GO:0003977","GO:0000902","GO:0007047","GO:0009103","GO:0009252","GO:0008360","GO:0046872","GO:0008152"};
		DAG dag = new DAG(test, tmp);
		System.out.println(Arrays.toString(dag.getNodes()));
		System.out.println(Arrays.toString(dag.getLeaves()));
		System.out.println(Arrays.toString(dag.getAncestors("GO:0005737")));
		System.out.println(Arrays.deepToString(test.getExclusivePaths(tmp)));
	}
}
