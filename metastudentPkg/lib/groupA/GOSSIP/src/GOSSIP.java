import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class GOSSIP 
{
	/**
	 * @param path to BLAST database
	 * @param path to fasta file
	 * @param path to output folder
	 */
	public static void main(String[] args) 
	{
		// Falls waerend der laufzeit etwas passiert...
		try{
			String inputfile = args[0]; //database
			String targets = args[1]; //target proteins, fasta file
			String outputfile = args[2]; //outputfolder
			System.out.println("Using " + inputfile + " as Database");
			System.out.println("Using " + targets + " as target proteins");
			System.out.println("Writing results to " + outputfile);
			
			double testTreshold = 0.9;
			while(testTreshold > 0.1){
				Selector sel = new SimpleCount(testTreshold);
				start(inputfile, targets, outputfile + "/SIMPLECOUNT_" + testTreshold, sel);
				testTreshold = testTreshold - 0.1;
			}
			
			
			
			
			testTreshold = 0.0;
			while(testTreshold < 0.1){
				Selector sel = new EValue(testTreshold);
				start(inputfile, targets, outputfile + "/EVALUE_" + testTreshold, sel);
				testTreshold = testTreshold + 0.01;
			}
			
			
			testTreshold = 0.0;
			while(testTreshold < 1){
				Selector sel = new EValueCount(testTreshold);
				start(inputfile, targets, outputfile + "/EVALUECOUNT_" + testTreshold, sel);
				testTreshold = testTreshold + 0.05;			
			}
			
		}catch(Exception e){
			BufferedWriter error;
			try
			{
				error = new BufferedWriter(new FileWriter("/mnt/project/interres/CAFAEval/methods/groupA/results/error.log"));
				StackTraceElement[] stack = e.getStackTrace();
				for(StackTraceElement s : stack){
					error.write(s.toString() + "\n");
				}
				error.write(e.toString());
				error.flush();
				error.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}		
	}
	
	public static void start(String in, String target, String out, Selector sel){
		Selector selector = sel;
		
		Predictor predictor = new Predictor(in, selector, out);
		try
		{
			BufferedReader multiFastaReader = new BufferedReader(new FileReader(new File(target)));
			String line = "";
			String sequence = "";
			String id = "";
			
			predictor.writeCAFAHead();
			
			while ((line = multiFastaReader.readLine()) != null)
			{
				if (line.startsWith(">"))
				{
					// id wird inklusive ">" uebergeben
					if (!sequence.equals(""))
					{	
						predictor.predict(id, sequence);
						id = "";
						sequence = "";
						// Ergebnis bekommen, Confidence berechnen und in ein File schreiben (Val-File und CAFA-File)
					}
					id = line;
				}
				else sequence += line;
			}
			multiFastaReader.close();
			predictor.predict(id, sequence);
			predictor.writeCAFATail();
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
}
