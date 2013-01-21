package pp2.prediction.knn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import pp2.go.Relations;
import pp2.tools.Tools;

/**
 * performs all operations necessary to predict a list of targets
 * 
 * @author Thomas Hopf
 *
 */
public class KNNKernel {
	
	// holds all relations in the GO ontology tree
	private Relations relations;
	
	public KNNKernel(String closureFile, String termFile, boolean noCellularComponent)
	{
		relations = new Relations(closureFile, termFile, noCellularComponent);
	}
	
	/**
	 * fetches the next target with all BLAST hits from the reader
	 * @param reader
	 * @return
	 */
	private PredictionTarget getNextTarget(BufferedReader reader, boolean eliminateDuplicateHits)
	{
		PredictionTarget nextTarget = new PredictionTarget();
		
		try {
			String line = null;
			while((line = reader.readLine()) != null)
			{
				// return current target when we hit the "end-of-target"-line "//"
				if(line.equals("//"))
					return nextTarget;
				
				// get ID of current target
				if(line.startsWith(">"))
					nextTarget.setTargetID(line.substring(1));
				
				// if line starts with "#", this is one of the BLAST hits in the database
				if(line.startsWith("#"))
					nextTarget.addBlastHit(line.substring(1), eliminateDuplicateHits);
			}
				
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * carries out the prediction of all targets in file containing multiple targets
	 * @param targetFile
	 */
	public void predictTargetList(String targetFile, String outFile, boolean eliminateDuplicateHits, boolean useEValueThreshold, double eValueTreshold, int k, String distributionFile)
	{
		try {
			BufferedReader in = Tools.openFile(targetFile);
			BufferedWriter naiveWriter = Tools.writeToFile(outFile + ".naive");
			BufferedWriter leaveWriter = Tools.writeToFile(outFile + ".leaves");

			// read the distribution of global quality scores
			double[] distribution = null;
			if(distributionFile != null)
			{
				distribution = new double[100];
				int i = 0;
				BufferedReader distReader = Tools.openFile(distributionFile);
				
				String line = null;
				while((line = distReader.readLine()) != null)
				{
					distribution[i] = Double.parseDouble(line);
					i++;
				}
				if(i != 100)
					System.err.println("warning: distribution file does not contain exactly 100 quantile lines.");
				
				distReader.close();
			}
			
			// the current target for which to perform a prediction
			PredictionTarget target = null;
			
			int numPredicted = 0;
			// iterate over all targets
			while((target = getNextTarget(in, eliminateDuplicateHits)) != null)
			{
				// skip double // error in blast output file
				if(target.getTargetID() == null)
					continue;
				
				//System.err.println(">" + target.getTargetID());
				try {
					target.predict(relations, useEValueThreshold, eValueTreshold, k, naiveWriter, leaveWriter, distribution);
				} catch(Exception e)
				{
					System.err.println("an error occured while predicting target " + target.getTargetID());
					e.printStackTrace();
				}
				
				numPredicted++;
			}
			
			System.out.println("   predicted " + numPredicted + " sequences.");
			in.close();
			naiveWriter.close();
			leaveWriter.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	

}
