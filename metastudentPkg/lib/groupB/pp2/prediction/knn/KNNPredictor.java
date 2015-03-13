package pp2.prediction.knn;


/**
 * command-line wrapper to apply kNN predictions to a file containing several targets
 * @author Thomas Hopf
 *
 */
public class KNNPredictor {

	
	public static void main(String[] args)
	{		
		// example run: java KNNPredictor -i examples/blast_results_all.blast -o examples/prediction_out1 -k 11 -c data/deductiveClosureGO.txt -t data/goterm.txt

		String usage = 	"java KNNPredictor -i <blast input> -o <output path> -d <distribution> [-k <#neighbors> |-e <eValue threshold>] [-nofilter] [-cc] " +
						"-c <GO closure file> -t <GO term file>";
		
		if(args.length < 8)
		{
			System.out.println(usage);
			System.exit(-1);
		}
		
		String hitFile = null; //"examples/blast_results_example_subset2";
		String outFile = null; // "examples/prediction_out1";
		String distributionFile = null;
		boolean eliminateDuplicateHits = true;
		boolean useEValueThreshold = false;
		double eValueTreshold = 1E-10;
		int k=1;
		
		String closureFile = null;
		String termFile = null;
		boolean noCellularComponent = false;
		
		//String hitFile = "examples/blast_results_all.blast";
		//String outFile = "examples/prediction_out1";
		//boolean eliminateDuplicateHits = true;
		//boolean useEValueThreshold = false;
		//double eValueTreshold=1E-10;
		//int k=2;
		//"data/deductiveClosureGO.txt"
		//"data/goterm.txt"

		for(int i=0; i<args.length; i++) {
			
			if(args[i].equals("-i"))
				hitFile = args[++i];
			else if(args[i].equals("-o"))
				outFile = args[++i];
			else if(args[i].equals("-c"))
				closureFile = args[++i];
			else if(args[i].equals("-t"))
				termFile = args[++i];
			else if(args[i].equals("-o"))
				outFile = args[++i];
			else if(args[i].equals("-d"))
				distributionFile = args[++i];
			else if(args[i].equals("-k"))
			{
				k = Integer.parseInt(args[++i]);
				useEValueThreshold = false;
			}
			else if(args[i].equals("-e"))
			{
				eValueTreshold = Double.parseDouble(args[++i]);
				useEValueThreshold = true;
			}
			else if(args[i].equals("-nofilter"))
				eliminateDuplicateHits = false;
//			else if(args[i].equals("-cc"))
//				noCellularComponent = false;
			else
				System.out.println(usage);
		}
	
		if(hitFile == null || outFile == null || closureFile == null || termFile == null)
		{
			System.out.println("invalid file parameters!");
			System.out.println(usage);
			System.exit(-1);
		}

		System.out.println("   input file: " + hitFile);
		System.out.println("   out path: " + outFile);
		System.out.println("   use E-value: " + useEValueThreshold);
		System.out.println("   k: " + k);
		System.out.println("   E-Value threshold: " + eValueTreshold);
		System.out.println("   eliminate duplicate hits: " + eliminateDuplicateHits);
		System.out.println("   closure file: " + closureFile);
		System.out.println("   term file: " + termFile);
		System.out.println("   distribution file: " + distributionFile);
		System.out.println("   no cellular component: " + noCellularComponent);
		System.out.println();

		KNNKernel knn = new KNNKernel(closureFile, termFile, noCellularComponent);
		knn.predictTargetList(hitFile, outFile, eliminateDuplicateHits,useEValueThreshold, eValueTreshold, k, distributionFile);
		
		//-i examples/blast_results_all.blast -o examples/prediction_out1 -k 11 -c data/deductiveClosureGO.txt -t data/goterm.tx
		
		/*
		 * TODO:
		 * X doppelte Hits filtern (aus den verschiedenen PSI-Blast Iterationen)
		 * X BestHit als 1-NN implementieren!
		 * X normalisierung per ontologie
		 * 
		 * - warum korrelieren unweighted und weighted so stark?
		 * - exception handling für individuelle targets implementieren
		 * X scores runden
		 */
	}
}
