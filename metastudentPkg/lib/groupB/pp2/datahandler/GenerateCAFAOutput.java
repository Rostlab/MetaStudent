package pp2.datahandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pp2.go.Relations;
import pp2.tools.Tools;

public class GenerateCAFAOutput {
	
	public static final String teamName = "rostlab";
	public static final String[] keywords = new String[] {
		"sequence alignments"
	};
	public static final int confCol = 4;
	public static final int ontCol = 2;

	/**
	 * Converts output file with predictions and corresponding
	 * scores to CAFA output file format
	 * @param inputFile Prediction file
	 * @param outputFile Name of the output file
	 * @param goClosure Name of the file containing go ontology
	 * @param goTermFile Name of the file containing namespace/ontology
	 * @param teamName Name of the team
	 * @param keywords List of keywords (have to be CAFA keywords!)
	 * @param model Number of model (1-3)
	 * @param pr Precision of method
	 * @param rc Recall of method
	 * @param confCol Column containing the confidence score
	 * @param n Parameter n = Number of Predictions per target
	 */
	public static void parseToCAFAOutput(String inputFile, String outputFile,
			String goClosure, String goTermFile,
			String teamName, String[] keywords, int model, double pr, double rc,
			int confCol, int n) {
		try {
			BufferedReader input = Tools.openFile(inputFile);
			BufferedWriter output = Tools.writeToFile(outputFile);
			output.write("AUTHOR\t"+teamName+"\n"
					+"MODEL\t"+model+"\n"
					+"KEYWORDS\t"+listKeywords(keywords)+"\n");
			if(pr > -1 && rc > -1) {
				output.write("ACCURACY\tPR="+String.format("%3.2f",pr).replace(",", ".")+
						"; RC="+String.format("%3.2f",rc).replace(",", ".")+"\n");
			}
			String line;
			String target = "";
			Map<Double,LinkedList<String>> scoreToHitM = new HashMap<Double, LinkedList<String>>();	
			//Map<Double,String> scoreToHitB = new HashMap<Double, String>();
			//Map<Double,String> scoreToHitC = new HashMap<Double, String>();	
			Relations rel = null;
			if(!goClosure.isEmpty() && !goTermFile.isEmpty()) {
				rel = new Relations(goClosure, goTermFile, true);
			}
			
			while((line = input.readLine()) != null) {
				if(line.startsWith("//")) {
					output.write(filterScores(scoreToHitM, n, target, rel));
					//output.write(filterScores(scoreToHitB, n, target));
					//output.write(filterScores(scoreToHitC, n, target));
				} else if(line.startsWith(">")) {
					target = line.substring(1);
					scoreToHitM = new HashMap<Double, LinkedList<String>>();
				} else {
					String[] content = line.split("\t");
					double score = Double.parseDouble(content[confCol].replace(",","."));
					String go = content[0];
					
					if(scoreToHitM.containsKey(score))
						scoreToHitM.get(score).add(go+"\t"+formatScore(score));
					else
					{
						LinkedList<String> l = new LinkedList<String>();
						l.add(go+"\t"+formatScore(score));
						scoreToHitM.put(score, l);
					}
						//scoreToHitM.put(score, go+"\t"+String.format("%1.2f", score).replace(",", "."));
				}
			}
			output.write("END");
			input.close();
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Truncated version of converter from prediction file with
	 * scores to CAFA output file format
	 * @param inputFile Prediction file
	 * @param outputFile Output file name
	 * @param model Number of model (1-3)
	 * @param pr Precision of method
	 * @param rc Recall of method
	 * @param n Parameter n = Number of Predictions per target
	 */
	public static void parseToCAFAOutput(String inputFile, String outputFile,
			int model, double pr, double rc, int k) {
		parseToCAFAOutput(inputFile, outputFile, "", "", teamName, keywords,
				model, pr, rc, confCol, k);
	}
	
	public static String formatScore(double score) {
		return String.format("%1.2f", score).replace(",", ".");
	}
	
	public static String listKeywords(String[] keywords) {
		StringBuffer result = new StringBuffer("");
		for(int i=0; i<keywords.length-1; i++) {
			result.append(keywords[i]+", ");
		}
		return result.append(keywords[keywords.length-1]+".").toString();
	}
	
	public static void printUsage() {
		System.err.println("   Usage: GenerateCAFAOutput -i scorefile -o outputfile" +
				" -m model -pr precision -rc recall [-n number_of_predictions/target] " +
				"[-t teamname] [-c column_index_of_score] [-k keywords (komma separated)] " +
				"[-gc deductive_closure_go -gn namespace_go_file]");
		System.exit(1);
	}
	
	private static String filterScores(Map<Double,LinkedList<String>> scoreToHit,
			int n, String target, Relations rel) {
		/*if(n == 0) {
			n = scoreToHit.size();
		}*/
		//TreeSet<Double> scores = new TreeSet<Double>(scoreToHit.keySet());
		StringBuffer output = new StringBuffer("");
		int i = 0;
		
		// sort scores in descending order
		List<Double> sortedScores = new ArrayList<Double>();
		sortedScores.addAll(scoreToHit.keySet());
		Collections.sort(sortedScores);
		Collections.reverse(sortedScores);
		Set<String> parentNodes = new HashSet<String>();
		
		for(Double score: sortedScores)
		{
			for(String pred: scoreToHit.get(score))
			{
				if(n > 0 && i >= n)
					break;
				
				output.append(target+"\t"+pred+"\n");
				i++;
				if(rel != null) {
					String go = pred.split("\t")[0];
					String[] parents = rel.getAncestors(go);
					for(String p : parents) {
						if(!parentNodes.contains(p)) {
							output.append(target+"\t"+p+"\t"+formatScore(score)+"\n");
							parentNodes.add(p);
						}
					}
				}
				
				
			}
			
			if(n > 0 && i >= n)
				break;
		}
		
		/*for(int i=0; i<n; i++) {
			if(scores.size() > 0) {
				double s = scores.last();
				output.append(target+"\t"+scoreToHit.get(s)+"\n");
				scores.remove(s);
			}
		}*/
		return output.toString();
	}
	
	public static void main(String[] args) {
		String inputfile = "";
		String outputfile = "";
		String goClosure = "";
		String goTermFile = "";
		int model = 0;
		double pr = 0;
		double rc = 0;
		int n = 0;
		String teamName = "";
		String[] keywords = null;
		int confCol = -1;
		
		for(int i=0; i<args.length; i+=2) {
			if(args[i].equals("-i")) {
				inputfile = args[i+1];
			} else if(args[i].equals("-o")) {
				outputfile = args[i+1];
			} else if(args[i].equals("-m")) {
				model = Integer.parseInt(args[i+1]);
			} else if(args[i].equals("-pr")) {
				try {
					pr = Double.parseDouble(args[i+1]);
				} catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			} else if(args[i].equals("-rc")) {
				try {
					rc = Double.parseDouble(args[i+1]);
				} catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			} else if(args[i].equals("-n")) {
				n = Integer.parseInt(args[i+1]);
			} else if(args[i].equals("-t")) {
				teamName = args[i+1];
			} else if(args[i].equals("-k")) {
				keywords = args[i+1].split(",");
			} else if(args[i].equals("-c")) {
				confCol = Integer.parseInt(args[i+1]);
			} else if(args[i].equals("-gc")) {
				goClosure = args[i+1];
			} else if(args[i].equals("-gn")) {
				goTermFile = args[i+1];
			} else {
				printUsage();
			}
		}
		if(teamName.isEmpty()) {
			teamName = GenerateCAFAOutput.teamName;
		}
		if(keywords == null) {
			keywords = GenerateCAFAOutput.keywords;
		}
		if(confCol == -1) {
			confCol = GenerateCAFAOutput.confCol;
		}
		if(!inputfile.isEmpty() && !outputfile.isEmpty()
				&& model > 0)  {
			parseToCAFAOutput(inputfile, outputfile, goClosure, goTermFile,
					teamName, keywords, model, pr, rc, confCol, n);
		} else {
			printUsage();
		}
		
		
	}
}
