package pp2.go;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import pp2.prediction.knn.DAGNode;

public class DAG {

	String[] nodes = new String[0];
	HashMap<String,String[]> ancestors;
	String[] leaves = new String[0];
	HashMap<String,DAGNode> DAGNodes;
	
	/**
	 * 
	 * @author Mark Heron
	 *
	 */
	public DAG(Relations rel, String[] goterms) {
		
		//filter goterms
		String[] goterms_f = new String[0];
		HashSet<String> tmp = new HashSet<String>();
		tmp.addAll(Arrays.asList(goterms));
		goterms_f = tmp.toArray(goterms_f);
		tmp = new HashSet<String>();
		for (String goterm : goterms_f) {
			if(rel.getType(goterm).length() > 0 && rel.isTypeofInterst(goterm)) {
				tmp.add(goterm);
			}
		}
		goterms_f = new String[0];
		goterms_f = tmp.toArray(goterms_f);
		
		
		// make nodes
		HashSet<String> nodes_tmp = new HashSet<String>();
		nodes_tmp.addAll(Arrays.asList(goterms_f));
		for (String goterm : goterms_f) {
			nodes_tmp.addAll(Arrays.asList(rel.getAncestors(goterm)));
		}
		nodes = nodes_tmp.toArray(nodes);
		
		// make ancestors
		ancestors = new HashMap<String, String[]>();
		for (String goterm : nodes) {
			ancestors.put(goterm, rel.getAncestors(goterm));
		}
		
		// make leaves
		HashSet<String> leaves_tmp = new HashSet<String>();
		for (String goterm : goterms_f) {
			boolean leaf = true;
			for (String goterm2 : goterms_f) {
				if(goterm != goterm2) {
					for (String ancestor : rel.getAncestors(goterm2)) {
						if(goterm.equals(ancestor)) {
							leaf = false;
						}
					}
				}
			}
			if(leaf) {
				leaves_tmp.add(goterm);
			}
		}
		leaves = leaves_tmp.toArray(leaves);
		
		// make initial scores
		DAGNodes = new HashMap<String, DAGNode>();
		for (String node : nodes) {
			DAGNodes.put(node, new DAGNode());
			
			// also add scores for the ancestors
			for (String anc : ancestors.get(node)) {
				DAGNodes.put(anc, new DAGNode());
			}
		}
		
	}
	
	
	public String[] getNodes() {
		return nodes;
	}
	
	public String[] getAncestors(String goterm) {
		return ancestors.get(goterm);
	}
	
	public String[] getLeaves() {
		return leaves;
	}
	
	public DAGNode getDAGNode(String goterm) {
		return DAGNodes.get(goterm);
	}
	
	public HashMap<String, DAGNode> getDAGNodes()
	{
		return DAGNodes;
	}
}
