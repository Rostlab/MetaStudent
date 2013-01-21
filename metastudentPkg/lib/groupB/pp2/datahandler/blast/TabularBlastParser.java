package pp2.datahandler.blast;

import java.io.*;

public class TabularBlastParser {

	public static final int eValPos = 10;
	public static final int hspPos = 11; //TODO

			
	public static void parseBlast(Process blastProcess, Writer output) throws IOException, InterruptedException {
	
		
		BufferedReader blast = new BufferedReader(new InputStreamReader(blastProcess.getInputStream()));
		
		String line;
		String prevQuery = "";
		String bestHit = "";
		double bestE = Double.MAX_VALUE;
		
		while((line = blast.readLine()) != null) {
			
			if(!line.startsWith("#")) {
				
				String[] content = line.split("\t");
				if(content.length > Math.max(hspPos, eValPos)) {
					
					String query = content[0];
					String hit   = content[1];
					
					double eValue = Double.parseDouble(content[eValPos]);
					double hsp = Double.parseDouble(content[hspPos]);
					
					if(!query.equals(prevQuery)) {
						
						output.write(bestHit);
						bestHit = query+"\t"+hit+"\t"+eValue+"\t"+hsp+"\n";
						bestE = eValue;
						
					} else {
						
						if(eValue < bestE) {
							bestE = eValue;
							bestHit = query+"\t"+hit+"\t"+eValue+"\t"+hsp+"\n";
						} else if(eValue == bestE) {
							String[] old = bestHit.split("\t");
							bestHit = query+"\t"+old[1]+","+hit+"\t"+eValue+"\t"+hsp+"\n";
						}
						
					}
					
					prevQuery = query;
				}
			}
		}
		
		int blastExitCode = blastProcess.waitFor();

		if(blastExitCode != 0) System.err.println("BLAST Exit code was " + blastExitCode + "!");
		
		output.flush();
		output.close();
		blast.close();
		
	}

}
