import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;


public abstract class Selector
{

	ArrayList<Double> blastScores;
	ArrayList<Double> eValues;
	ArrayList<Double> identity;
	ArrayList<Double> positives;
	ArrayList<Double> gaps;
	ArrayList<Integer> length;
	ArrayList<String> gos;
	
	GoTree tree;
	
	Hashtable<String, Double> probabilistic_estimate;
	
	private double predictBoth = 0.1;
	double treshold;
		
	public abstract ArrayList<String> select();
	
	
	
	public void initialize()
	{
		probabilistic_estimate = new Hashtable<String, Double>();
		eValues = new ArrayList<Double>();
		blastScores = new ArrayList<Double>();
		identity = new ArrayList<Double>();
		positives = new ArrayList<Double>();
		gaps = new ArrayList<Double>();
		length = new ArrayList<Integer>();
		gos = new ArrayList<String>();

		tree = new GoTree(Constants.GOPath); /////////TODO file for go -> pfad einfuegen
		
	}
	
	private ArrayList<String> removeParents(ArrayList<String> list){
		ArrayList<String> temp = new ArrayList<String>();
		for(String a : list){
			for (String b : list){
				if(a.equals(b))continue;
				if(tree.isAparentOfB(a, b)) temp.add(a);
			}
		}
		list.removeAll(temp);
		return list;
	}
	
	
	public ArrayList<String> cleanList(ArrayList<String> list){

		ArrayList<String>biol = new ArrayList<String>();
		ArrayList<String>cell = new ArrayList<String>();
		ArrayList<String>mol = new ArrayList<String>();
		
		for(String s : list){
			if(tree.getCategory(s).equals(GoTree.biolproc)) biol.add(s);
//			if(tree.getCategory(s).equals(GoTree.cellcomp)) cell.add(s);
			if(tree.getCategory(s).equals(GoTree.molfunction)) mol.add(s);			
		}
		
		if(biol.size() > 1){
			biol = removeParents(biol);
			if(biol.size() > 1){
				biol = cleanSublists(biol);
			}
		}
		if(mol.size() > 1){
			mol =  removeParents(mol);
			if(mol.size() > 1){
				mol = cleanSublists(mol);
			}
		}
		if(cell.size() > 1){
			cell =  removeParents(cell);
			if(cell.size() > 1){
				cell = cleanSublists(cell);
			}
		}
		
		//join lists
		cell.addAll(mol);
		biol.addAll(cell);
		
		return biol;
	}

	//teste ob wir beide nehmen oder nur einen oder den vorgaenger
	private ArrayList<String> cleanSublists(ArrayList<String> list){
		
		
		// geaendert, da man die Listen so nicht modifizieren kann --> ConcurrentModificationException
		
//		
//		for(String a : list){
//			for(String b : list){
//				if(a.equals(b)) continue;
//				double similarity = compareClosureOf(tree.getTree(a), tree.getTree(b));
//				if(similarity > 1 - predictBoth){ // get ancestor
//					//TODO testen ob Liste waehrend der laufzeit veraendert werden kann.
//					list.remove(a);
//					list.remove(b);
//					list.add(tree.getLowestCommonAncestor(a, b));
//				}
//				if(similarity < 1-predictBoth && similarity > predictBoth){
//					int a_ = tree.getLengthToRoot(a);
//					int b_ = tree.getLengthToRoot(b);
//					if(a_ >= b_) list.remove(b);
//					else list.remove(a);
//				}
//			}
//		}
//		return list;
		
		
		ArrayList<String> delFromList = new ArrayList<String>();
		ArrayList<String> addToList= new ArrayList<String>();

		
		
		for(String a : list){
			for(String b : list){
				if(a.equals(b)) continue;
				double similarity = compareClosureOf(tree.getTree(a), tree.getTree(b));
				if(similarity > 1 - predictBoth){ // get ancestor
					delFromList.add(a);
					delFromList.add(b);
					String ancestor = tree.getLowestCommonAncestor(a, b);
					double score = Math.max(this.probabilistic_estimate.get(a), this.probabilistic_estimate.get(b));
					this.probabilistic_estimate.put(ancestor, score);
					addToList.add(ancestor);
				}
				if(similarity < 1-predictBoth && similarity > predictBoth){
					int a_ = tree.getLengthToRoot(a);
					int b_ = tree.getLengthToRoot(b);
					if(a_ >= b_) delFromList.add(b);
					else delFromList.add(a);
				}
			}
		}
		
		
		list.removeAll(delFromList);
		list.addAll(addToList);
		
		while(list.contains(null))
			list.remove(null);
		
		// max 3 elemente pro kategorie
		while(list.size() > 4){
			removeAdditionalElement(list);
		}
		return list;
		
	}
	
	
	private ArrayList<String> removeAdditionalElement(ArrayList<String> list){
		
		int mindepth = 100000;
		int index = -1;
		for(int i = 0; i < list.size(); i++) {
				int a_ = tree.getLengthToRoot(list.get(i));
				if(a_< mindepth) {
					index = i;
					mindepth = a_;
				}
			}
		list.remove(index);
		return list;
	}
	
	
	private double compareClosureOf(ArrayList<String> a, ArrayList<String> b)
	{
		int equal = 0;
		for(String s : a){
			if(b.contains(s)) equal++;
		}
		return (2.0 * equal) / (double)(a.size() + b.size());
	}
	
	
}
