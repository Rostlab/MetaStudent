import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class GOSSIPSTarter
{

	
	public static List<String> indivdResults = new ArrayList<String>();
	public static List<String[]> fastaEntries = new ArrayList<String[]>();
	public static String NL = System.getProperty("line.separator");
	
	
	
	
	public static void main(String[] args)
	{
		System.out.println(args.length);
		if(args.length != 6){
			System.out.println("Usage: targets(fasta-Format) blastResults outputfile threshold k iters");
			System.exit(1);
		}
		String targets = args[0]; //target proteins, fasta file
		String blastResults = args[1]; //target proteins, fasta file
		String outputfile = args[2]; //outputfolder

		Constants.THRESHOLD =  Double.valueOf(args[3]);
		Constants.K =  Integer.valueOf(args[4]);
		Constants.NUM_BLAST_ITERATIONS = Integer.valueOf(args[5]);
		
		System.out.println("Using " + targets + " as target proteins");
		System.out.println("Using " + blastResults + " as blast output");
		System.out.println("Writing results to " + outputfile);
		
		readBlastResult(blastResults);
		readFastaFile(targets);
		if(indivdResults.size() != fastaEntries.size())
		{
			System.out.println("Error: number of fastas and blasts dont match: " + fastaEntries.size() + " " + indivdResults.size());
			System.exit(1);
		}
		
		Selector sel = new EValueCount(Constants.THRESHOLD);
		start(targets, outputfile, sel);


	}

	public static void readBlastResult(String path)
	{
		
		StringBuilder text = new StringBuilder();
	    
	    Scanner scanner = null;
		try 
		{
			scanner = new Scanner(new FileInputStream(path));
			while (scanner.hasNextLine())
			{
				String currLine = scanner.nextLine() + NL;
				if(currLine.startsWith("BLASTP") && text.toString().replaceAll("\\s","").length() > 0)
				{
//					System.out.println(text.toString());
//					System.out.println("===== + ======");
//					System.exit(1);
					indivdResults.add(text.toString());
					text = new StringBuilder();
				}
				text.append(currLine);
			}
			indivdResults.add(text.toString());
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	    finally
	    {
	    	scanner.close();
	    }
	}
	
	
	public static void readFastaFile(String filePath)
	{	
		try
		{
			BufferedReader multiFastaReader = new BufferedReader(new FileReader(new File(filePath)));
			String line = "";
			String sequence = "";
			String id = "";
			while ((line = multiFastaReader.readLine()) != null)
			{
				if (line.startsWith(">"))
				{
					// id wird inklusive ">" uebergeben
					if (!sequence.equals(""))
					{	
						fastaEntries.add(new String[] {id, sequence});
						id = "";
						sequence = "";
						// Ergebnis bekommen, Confidence berechnen und in ein File schreiben (Val-File und CAFA-File)
					}
					id = line;
				}
				else sequence += line;
			}
			fastaEntries.add(new String[] {id, sequence});
			multiFastaReader.close();
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
	
	public static void start(String target, String out, Selector sel)
	{
		Selector selector = sel;	
		Predictor predictor = new Predictor(selector, out);
		
		for(int i = 0; i<fastaEntries.size(); i++)
		{
			predictor.predict(i);
		}
//		try 
//		{
//			predictor.writeCAFATail();
//		}
//		catch (IOException e) 
//		{
//			e.printStackTrace();
//		}
	}
}
