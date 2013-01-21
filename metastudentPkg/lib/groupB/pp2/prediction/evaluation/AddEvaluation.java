package pp2.prediction.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import pp2.tools.Tools;

public class AddEvaluation {

	private int predictCol;
	private int actualCol;
	private int precisionCol;
	private int recallCol;


	public AddEvaluation(String inputFile, int predictCol, int actualCol) {
		this.predictCol = predictCol;
		this.actualCol = actualCol;
		addEvaluationToFile(new File(inputFile), inputFile+".eval");
	}

	public AddEvaluation(String directory, FilenameFilter filter,
			int predictCol, int actualCol) {
		this.predictCol = predictCol;
		this.actualCol = actualCol;
		for(File file : new File(directory).listFiles(filter)) {
			addEvaluationToFile(file, file.getAbsolutePath()+".eval");
		}
	}


	public void addEvaluationToFile(File inputFile, String outputFile) {
		try {
			BufferedReader input = Tools.openFile(inputFile);
			BufferedWriter output = Tools.writeToFile(outputFile);

			String line;
			boolean firstLine = true;

			while((line = input.readLine()) != null) {
				if(firstLine) {
					//					output.write("#")
					firstLine = false;
				}
				String[] content = line.split("\t");
				String[] predList = content[predictCol].split(",");
				String[] actList = content[actualCol].split(",");
				precisionCol = content.length+1;
				recallCol = content.length+2;
				GOListComparator glc = new GOListComparator(predList, actList);
				output.write(line+"\t"+glc.precision()+"\t"+glc.recall()+"\n");
			}
			output.flush();
			output.close();
			input.close();
			inputFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPrecisionCol() {
		return precisionCol;
	}

	public int getRecallCol() {
		return recallCol;
	}

	public static void main(String[] args) {
		String usage = "AddEvaluation -i inputFile";
		if(args.length == 0) {
			System.out.println(usage);
		} else {
			String file = "";
			for(int i=0; i<args.length; i+=2) {
				if(args[i].equals("-i")) {
					file = args[i+1];
				}
			}
			new AddEvaluation(file, 1, 0);
		}
	}
}
