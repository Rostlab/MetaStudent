package pp2.prediction;

import java.io.*;
import pp2.datahandler.blast.*;

public class BestHit {

	private int maxIterations = 8;
	private double eValue = 20;
	private double hValue = 0.001;
	
	public void run(String trainingSet, String testSet, Writer output) throws IOException, InterruptedException{
				
					String[] parameters = new String[] {
							"-h",
							String.valueOf(hValue),
							"-e",
							String.valueOf(eValue),
							"-j",
							String.valueOf(maxIterations),
							"-m",
							"8",
					};

					System.err.println("Starting BLAST with parameters h="+hValue+", e="+eValue+", j="+maxIterations+"...");
					
					// initialize BLAST performer with BLAST database
					BlastPerformer bp = new BlastPerformer(new File(trainingSet).getAbsolutePath());
				
					// run BLAST and get ouput in a stream
					Process blast = bp.startBLAST(new File(testSet), parameters);
					
					// parse BLAST output from stream
					System.err.println("Parsing BLAST output...");
					TabularBlastParser.parseBlast(blast, output);
	}


	public void sethValue(double hValue) {
		this.hValue = hValue;
	}

	public void setMaxIter(int maxIter) {
		this.maxIterations = maxIter;
	}

	public void seteValue(double eValue) {
		this.eValue = eValue;
	}

	public static void main(String[] args) throws Exception {
		String usage = 	"Usage: BestHit -t trainingSet -p predictSet " +
						"[-h value] " +
						"[-e value] " +
						"[-j maxIterations]";

		try {
			if(args.length == 0) {
				System.out.println(usage);
			} else {
				BestHit bhbw = new BestHit();;

				String trainingSet = null;
				String predictSet = null;
				
				for(int i=0; i<args.length; i++) {
					
					if(args[i].equals("-h")) {
						double h = Double.parseDouble(args[i+1]);	
						bhbw.sethValue(h);
						i++;
					} else if(args[i].equals("-e")) {
						double e = Double.parseDouble(args[i+1]);
						bhbw.seteValue(e);
						i++;
					} else if(args[i].equals("-j")) {
						int j = Integer.parseInt(args[i+1]);
						bhbw.setMaxIter(j);
						i++;
					} else if(args[i].equals("-t")) {
						trainingSet = args[i+1];
						i++;
					} else if(args[i].equals("-p")) {
						predictSet = args[i+1];
						i++;
					} else {
						System.out.println(usage);
					}
				}
				
				if(trainingSet == null) throw new Exception("Must specify training set!");
				if(predictSet == null) throw new Exception("Must specify prediction set!");
				
				bhbw.run(trainingSet, predictSet, new OutputStreamWriter(System.out));

			}
		} catch (NumberFormatException e) {
			System.out.println(usage);
		}
	}
}
