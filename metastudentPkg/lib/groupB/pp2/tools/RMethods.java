package pp2.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for the writing of R scripts and running these from the code
 * @author Stefanie Kaufmann
 *
 */
public class RMethods {
	
	private static String installationPath;
	private File script;
	private BufferedWriter buff;
	private File outputFile = new File("temp.out");

	public RMethods(String rPath) {
		installationPath = rPath;
	}
	
	/**
	 * Start R with the previously written script
	 */
	public void runR() {
		try {
			Runtime rt   = java.lang.Runtime.getRuntime();
			String separator = System.getProperty("file.separator");
			//kickstart R with script file
			Process proc = rt.exec(installationPath+separator+"R CMD BATCH "+script.getAbsolutePath()+" "+outputFile.getAbsolutePath());
			
			int exitVal  = proc.waitFor();
			System.out.println(exitVal);
			//remove temporary files
			script.deleteOnExit();
			outputFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Start to write the R script
	 * @param filename The name of the script file
	 */
	public void startRScript(String filename) {
		try {
			script = new File(filename);
			if(script.exists()) {
				script.delete();
				script = new File(filename);
			}
			buff   = new BufferedWriter(new FileWriter(script));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * End the writing of the R script
	 */
	public void endRScript() {
		try {
			buff.flush();
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write one line into the R script
	 * @param line The line that will be written into the script
	 */
	public void addLineToScript(String line) {
		try {
			buff.write(line+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Define the installation path of R
	 * @param path The path of the bin directory where R is installed
	 */
	public void setPath(String path) {
		installationPath = path;
	}
	

}
