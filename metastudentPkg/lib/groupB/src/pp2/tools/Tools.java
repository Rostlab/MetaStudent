package pp2.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class Tools {

	private static DecimalFormat df = new DecimalFormat("0.00");
	
	public static String round(double value)
	{
		if(value >= 0.01)
			return df.format(value);
		else 
			return "0.01";
	}	
	
	public static BufferedReader openFile(String inputFile) {
		return openFile(new File(inputFile));
	}
	
	public static BufferedReader openFile(File inputFile) {
		BufferedReader buff = null;
		try {
			buff = new BufferedReader(new FileReader(inputFile));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return buff;
	}
	
	public static BufferedWriter writeToFile(String outputFile) {
		try {
			return new BufferedWriter(new FileWriter(new File(outputFile)));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
