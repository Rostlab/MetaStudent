package pp2.prediction.knn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pp2.tools.Tools;

/**
 * Determines distribution of confidence score prefactor
 * among all targets to make the score comparable on a
 * global level
 * @author Stefanie Kaufmann
 *
 */
public class DetermineDistribution {

	/**
	 * Determines distribution of confidence score prefactor
	 * among all targets to make the score comparable on a
	 * global level
	 * @param blastresults File containing the tabular blast
	 * results
	 * @param outputFile Name of the output file
	 * @param k Parameter k, describes the number of best hits
	 * that will be incorporated into the calculation
	 * @param eThresh Parameter e-Value threshold, describes
	 * maximal e-Value that should be used, higher values
	 * are discarded
	 */
	public void determineDistribution(String blastresults, String outputFile,
			int k, double eThresh) {
		try {
			//eThresh = logValue(eThresh);
			BufferedReader blast = Tools.openFile(blastresults);
			BufferedWriter output = Tools.writeToFile(outputFile);
			String line;
//			List<Double> values = new ArrayList<Double>();
			List<Double> factors = new ArrayList<Double>();
			Map<String,Double> swissprotToFactors = new HashMap<String,Double>();
			int counter = 0;
			String template = "";

			int numTargets = 0;
			int x = 0;
			
			while((line = blast.readLine()) != null) {
				counter++;
				if(line.startsWith("//")) {
					List<Double> values = new ArrayList<Double>(swissprotToFactors.values());
					//take only best k hits and remove bad e-Values
					Collections.sort(values);
					if(k > 0 && k < values.size()) {
						values = values.subList(0, k);
					}
					if(eThresh > 0) {
						
						Iterator<Double> iterator = values.iterator();
						while(iterator.hasNext())
						{
							Double v = iterator.next();
							if(v >= eThresh) {
								iterator.remove();
							}
						}
						// Exception, Erklärung: http://tech.puredanger.com/2009/02/02/java-concurrency-bugs-concurrentmodificationexception/
						/*for(Double v : values) {
							if(v >= eThresh) {
								values.remove(v);
							}
						}*/
					}
					
					int c = 0;
					for(Double v : values) {
						if(v == 0)
							c++;
					}
					if(c == values.size())
						x++;
					
					//calculate prefactor based on average and standard deviation
					double avg = calculateAvg(values);
					double stddev = calculateStdDev(values, avg);
					factors.add(prefactor(stddev, avg));
				//new target
				} else if(line.startsWith(">")) {
					swissprotToFactors = new HashMap<String, Double>();
					numTargets++;
				} else if(!line.isEmpty() && line.split("\t").length > 1){
					//parse e-Value for current hit and save it
					String[] content = line.split("\t");
					template = content[1];
					String e = content[2].toUpperCase();
					if(e.startsWith("E")) {
						e = 1.0+e;
					}
					double eValue = Double.parseDouble(e);
					swissprotToFactors.put(template, eValue);
				}
			}
			System.out.println("   Done parsing...");
			blast.close();
			//sort prefactors
			Collections.sort(factors);
			System.out.println("   Done sorting...");
			//divide set of prefactors into 100 parts
			int divisionSize = (int)(factors.size()/100.0);
			
			System.out.println("   Division size: " + divisionSize);
			
			for(int i=1; i<101; i++) {
				output.write(factors.get(i*divisionSize)+"\n");
			}
			output.flush();
			output.close();
			
			System.out.println("   Number of targets: " + numTargets);
			System.out.println("   Targets with all eVals == 0: " + x);
			System.out.println("   Max number problematic quantiles: " + (x / (double)divisionSize));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates average for a list of e-Values
	 * @param eValues List of e-Values
	 * @return Average of those e-Values
	 */
	public double calculateAvg(List<Double> eValues) {
		double sum = 0;
		for(double e : eValues) {
			double log = logValue(e);
			sum += log;
		}
		return sum/(double)eValues.size();
	}
	
	/**
	 * Calculates standard deviation for a list of e-Values
	 * @param eValues List of e-Values
	 * @param avg Average of those e-Values
	 * @return Standard deviation of e-Values
	 */
	public double calculateStdDev(List<Double> eValues, double avg) {
		double sum = 0;
		for(double e : eValues) {
			double log = logValue(e);
			sum += (log-avg)*(log-avg);
		}
		//if there is only one e-Value (=1 hit), standard deviation is 0
		if(eValues.size() == 1) {
			return 0;
		}
		return Math.sqrt(sum/(double)(eValues.size()-1));
	}
	
	/**
	 * Calculates the negative logarithm of the e-Value
	 * @param eValue The e-Value
	 * @return Negative logarithm of the e-Value
	 */
	public double logValue(double eValue) {
		if(eValue == 0) {
			return 1000;
		}
		
		if(-Math.log(eValue)>1000)
			System.out.println(-Math.log(eValue));
		
		return -Math.log(eValue);
	}
	
	/**
	 * Calculates the prefactor for the confidence score
	 * based on standard deviation and average
	 * @param stddev Standard deviation of e-Values of all
	 * hits for current target
	 * @param avg Average of e-Values of all hits for current
	 * target
	 * @return Prefactor for global comparison of confidence
	 * score
	 */
	public double prefactor(double stddev, double avg) {
		return avg+2*stddev;
	}
	
	public static void printUsage() {
		System.err.println("   Usage: DetermineDistribution -i blastfile" +
				"-o outputfile (-k k | -e e-value-threshold)");
		System.exit(1);
	}
	
	public static void main(String[] args) {
		String blastFile = "";
		String outputFile = "";
		int k = 0;
		double e = 0;
		for(int i=0; i<args.length; i+=2) {
			if(args[i].equals("-i")) {
				blastFile = args[i+1];
			} else if(args[i].equals("-o")) {
				outputFile = args[i+1];
			} else if(args[i].equals("-k")) {
				k = Integer.parseInt(args[i+1]);
			} else if(args[i].equals("-e")) {
				e = Double.parseDouble(args[i+1]);
			} else {
				printUsage();
			}
		}
		if(!blastFile.isEmpty() && !outputFile.isEmpty() 
				&& (k > 0 || e > 0)) {
			new DetermineDistribution().determineDistribution(blastFile, outputFile, k, e);
		} else {
			printUsage();
		}
		
	}
}
