package pp2.prediction.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pp2.tools.RMethods;
import pp2.tools.Tools;

public class EvaluationPlots {

	public EvaluationPlots(String directory, FilenameFilter filter, String rPath, int predictCol, int actualCol) {
		AddEvaluation ae = new AddEvaluation(directory, filter, predictCol, actualCol);
		FilenameFilter evalFil = new FilenameFilter() {
			
			@Override
			public boolean accept(File file, String name) {
				if(name.endsWith(".eval")) {
					return true;
				}
				return false;
			}
		};
		try {
			String name = directory+File.separator+"evaluation-summary";
			BufferedWriter summary = Tools.writeToFile(name);
			summary.write("#Mean_Recall\tMedian_Recall\tMean_Precision\tMedian_Precision\n");
			RMethods rm = new RMethods(rPath);
			rm.startRScript(name+".R");
			rm.addLineToScript("meanRecall <- c()");
			rm.addLineToScript("medianRecall <- c()");
			rm.addLineToScript("meanPrecision <- c()");
			rm.addLineToScript("medianPrecision <- c()");
			rm.addLineToScript("labels <- c()");
			
			for(File file : new File(directory).listFiles(evalFil)) {
				Pattern cv = Pattern.compile("cv=(\\d+)");
				Matcher ma = cv.matcher(file.getName());
				int cvRound = 1;
				if(ma.find()) {
					cvRound = Integer.parseInt(ma.group(1));
				}
				double meanRecall = 0;
				double medianRecall = 0;
				double meanPrecision = 0;
				double medianPrecision = 0;
				
				int preCol = ae.getPrecisionCol();
				int recCol = ae.getRecallCol();
				
				rm.addLineToScript("x <- read.table(\""+file.getAbsolutePath()+"\")");
				rm.addLineToScript("mR <- mean(x[["+recCol+"]])");
				rm.addLineToScript("meR <- median(x[["+recCol+"]])");
				rm.addLineToScript("mP <- mean(x[["+preCol+"]])");
				rm.addLineToScript("meP <- median(x[["+preCol+"]])");
				rm.addLineToScript("meanRecall <- c(meanRecall,mR)");
				rm.addLineToScript("medianRecall <- c(medianRecall,meR)");
				rm.addLineToScript("meanPrecision <- c(meanPrecision,mP)");
				rm.addLineToScript("medianPrecision <- c(medianPrecision,meP)");
			}
			
			rm.addLineToScript("pdf(\""+name+".pdf\")");
			//TODO: plot
			summary.flush();
			summary.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
