package pp2.datahandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pp2.components.GO;
import pp2.components.Protein;
import pp2.tools.Tools;

public class SwissProtParser {

	private List<Protein> proteins;
	private File database;
	
	public SwissProtParser(String db) {
		this.database = new File(db);
	}
	
	public SwissProtParser(File db) {
		this.database = db;
	}
	
	public void loadProteins(String outputFile) {
		loadProteins(outputFile, false);
	}
	
	public void loadProteins(String outputFile, boolean save) {
		proteins = new ArrayList<Protein>();
		boolean output = true;
		if(outputFile.isEmpty()) {
			output = false;
		}
		try {
			BufferedReader db = Tools.openFile(database);
			BufferedWriter of = null;
			if(output) {
				of = Tools.writeToFile(outputFile);
			}
			
			String line;
			boolean newEntry = true;
			StringBuffer entry = new StringBuffer("");
			
			while((line = db.readLine()) != null) {
				if(newEntry) {
					entry = new StringBuffer("");
					entry.append(line+"\n");
					newEntry = false;
				} else {
					entry.append(line+"\n");
					if(line.endsWith("//")) {
						newEntry  = true;
						Protein p = new Protein(entry);
						if(save) {
							proteins.add(p);
						}
						if(output && p.getAnnotations().length > 0) {
							of.write(p.toString()+"\n");
						}
					}
				}
			}
			
			db.close();
			if(output) {
				of.flush();
				of.close();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find database file!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void goStatistics() {
		int totalNumber = 0;
		int goNumber    = 0;
		int goSequences = 0;
		Set<String> goIDs = new HashSet<String>();
		
		for(Protein p : proteins) {
			totalNumber++;
			if(p.getAnnotations().length > 0) {
				goSequences++;
				goNumber += p.getAnnotations().length;
				for(GO go : p.getAnnotations()) {
					goIDs.add(go.getId());
				}
			}
		}
		
		System.out.println("There are a total of "+goSequences+" GO-annotated sequences.");
		System.out.println("The average number of GO numbers per se" +
				"quence is\n"+String.format("%.2f", (double)goNumber/(double)goSequences)+
				"when considering only GO-annotated sequences, and\n"+
				String.format("%.2f", (double)goNumber/(double)totalNumber)+" for all" +
						"sequences.");
		System.out.println("There are "+goIDs.size()+" distinct GO numbers.");
	}
	
	public void goStatistics(String fastaFile) {
		int goNumber      = 0;
		int goSequences   = 0;
		Set<String> goIDs = new HashSet<String>();
		
		try {
			BufferedReader fasta = Tools.openFile(fastaFile);
			String line;
			
			while((line = fasta.readLine()) != null) {
				if(line.startsWith(">")) {
					goSequences++;
					goNumber += line.split(",").length;
					for(String go : line.substring(1).split(",")) {
						goIDs.add(go);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file "+fastaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("There are a total of "+goSequences+" GO-annotated sequences.");
		System.out.println("The average number of GO numbers per se" +
				"quence is\n"+String.format("%.2f", (double)goNumber/(double)goSequences)+
				" when considering only GO-annotated sequences.");
		System.out.println("There are "+goIDs.size()+" distinct GO numbers.");
	}
	
	public void loadProteins() {
		loadProteins("");
	}
	
	public static void printUsage() {
		System.out.println("Usage: java SwissProtParser -i [Swissprot-Database-File] -o [OutputDir]");
	}
	
	public static void main(String[] args) {
		if(args.length > 0) {
			String database  = "";
			String outputDir = "";
			for(int i=0; i<args.length; i+=2) {
				if(args[i].equals("-i")) {
					database = args[i+1];
				} else if(args[i].equals("-o")) {
					outputDir = args[i+1];
				} else {
					printUsage();
					System.exit(0);
				}
				SwissProtParser spp = new SwissProtParser(database);
				spp.loadProteins(outputDir+"go-annotations.fa");
				spp.goStatistics(outputDir+"go-annotations.fa");
			}
		} else {
			printUsage();
		}
	}
}
