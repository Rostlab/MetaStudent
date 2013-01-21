import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BlastFileParser {

	private static boolean moreThanOneRound = false;
	private static ArrayList<ArrayList<TabulatorFormat>> allOverResult = new ArrayList<ArrayList<TabulatorFormat>>();
	private static ArrayList<String> allTargets = new ArrayList<String>();
	private static ArrayList<Boolean> allMoreThanOneRound = new ArrayList<Boolean>();
	private static String outputFile;
	private static String name;
	private static String goTerms;

	public static void main(String[] args) {
		try {
			readFiles(args[1]);
			outputFile = args[2];
			writeAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//reads the file and gives result as blocks to parse
	public static void readFiles(String dir) throws IOException {
		String line;
		ArrayList<String> result_per_blast;
		File file = new File(dir);
		result_per_blast = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null) {
			if(Pattern.matches("BLASTP.*",line)) {
				if(!result_per_blast.isEmpty()) {
					parse(result_per_blast);
				}
				result_per_blast.clear();
				result_per_blast.add(line);
			} else {
				result_per_blast.add(line);
			}
		}
		if(!result_per_blast.isEmpty()) {
			parse(result_per_blast);
		}
		in.close();
	}

	public static void parse(ArrayList<String> result_per_blast) {
		Pattern pattern;
		Matcher matcher;
		String line;
		String target = "";
		int round = 0;
		ArrayList<TabulatorFormat> result = new ArrayList<TabulatorFormat>();

		for(int i = 0; i < result_per_blast.size();i++) {
			line = result_per_blast.get(i);
			result.add(new TabulatorFormat());
			if(Pattern.matches("Results\\sfrom\\sround\\s\\d+",line)) {
				moreThanOneRound = true;
				result.add(new TabulatorFormat());
				round++;
				result.get(round).addRound(round);
			}
			if(Pattern.matches("Query=\\s(\\S+).*",line)) {
				pattern = Pattern.compile("Query=\\s(\\S+).*");
				matcher = pattern.matcher(line);
				matcher.find();
				target = matcher.group(1);
			}
			if(Pattern.matches(">\\S*GO\\S+\\s*",line)) {
				QueryResult queryResult = new QueryResult();
				if(Pattern.matches(">GO.*", line)) {
					pattern = Pattern.compile(">(GO\\S+)\\s*");
					matcher = pattern.matcher(line);
					matcher.find();
					goTerms = matcher.group(1);
					while(!Pattern.matches("\\s*Length\\s=\\s\\d+.*", line) && i < result_per_blast.size()-1) {
						if(Pattern.matches("\\s*GO\\S*\\s*", line)) {
							pattern = Pattern.compile("\\s*(GO\\S+)\\s*");
							matcher = pattern.matcher(line);
							matcher.find();
							goTerms += matcher.group(1);
						}
						i++;
						line = result_per_blast.get(i);
					}
					queryResult.setName(goTerms);
				} else if (Pattern.matches(">\\S+\\|\\S+.*",line)) {
					pattern = Pattern.compile(">(\\S+)\\|(\\S+)\\s*");
					matcher = pattern.matcher(line);
					matcher.find();
					name = matcher.group(1);
					goTerms = matcher.group(2);
					while(!Pattern.matches("\\s*Length\\s=\\s\\d+.*", line) && i < result_per_blast.size()-1) {
						if(Pattern.matches("\\s*GO\\S*\\s*", line)) {
							pattern = Pattern.compile("\\s*(GO\\S+)\\s*");
							matcher = pattern.matcher(line);
							matcher.find();
							goTerms += matcher.group(1);
						}
						i++;
						line = result_per_blast.get(i);
					}
					queryResult.setName(name);
				}
				queryResult.addGoTerms(goTerms);
				pattern = Pattern.compile("\\s+Length\\s*=\\s*(\\d+)\\s*");
				matcher = pattern.matcher(line);
				matcher.find();
				queryResult.addLength("" + matcher.group(1));
				while(!Pattern.matches("\\s*Score\\s=\\s*\\S+\\sbits\\s\\(\\d+\\),\\sExpect\\s=\\s\\S+.*", line) && i < result_per_blast.size()-1) {
					i++;
					line = result_per_blast.get(i);
				}
				pattern = Pattern.compile("\\s*Score\\s=\\s*(\\S+)\\sbits\\s\\((\\d+)\\),\\sExpect\\s=\\s(\\S+),.*");
				matcher = pattern.matcher(line);
				matcher.find();
				queryResult.addScore(Double.parseDouble(matcher.group(1)));
				queryResult.addNumber(Integer.parseInt(matcher.group(2)));
				queryResult.addEValue(matcher.group(3));

				while(!Pattern.matches("\\s*Identities\\s=\\s\\S+\\s\\(\\d+%\\),\\sPositives\\s=\\s\\d+/\\d+\\s\\(\\d+%\\).*", line) && i < result_per_blast.size()-1) {
					i++;
					line = result_per_blast.get(i);
				}
				if(Pattern.matches("\\s*Identities\\s=\\s\\S+\\s\\(\\d+%\\),\\sPositives\\s=\\s\\d+/\\d+\\s\\(\\d+%\\),\\s+Gaps\\s+=\\s+\\S+\\s+\\(\\d+%\\).*",line)) {
					pattern = Pattern.compile("\\s*Identities\\s=\\s(\\S+)\\s\\((\\d+)%\\),\\sPositives\\s=\\s(\\d+)/\\d+\\s\\((\\d+)%\\),\\s+Gaps\\s+=\\s+(\\S+)\\s\\((\\S+)%\\)");
					matcher = pattern.matcher(line);
					matcher.find();
					queryResult.addIdentities(matcher.group(1));
					queryResult.addIdenititesPercent(Integer.parseInt(matcher.group(2)));
					queryResult.addPositives(matcher.group(3));
					queryResult.addPositivesPercent(Integer.parseInt(matcher.group(4)));
					queryResult.addGaps(matcher.group(5));
					queryResult.addGapsPercent(Integer.parseInt(matcher.group(6)));
				} else {
					pattern = Pattern.compile("\\s*Identities\\s=\\s(\\S+)\\s\\((\\d+)%\\),\\sPositives\\s=\\s(\\d+)/\\d+\\s\\((\\d+)%\\)");
					matcher = pattern.matcher(line);
					matcher.find();
					queryResult.addIdentities(matcher.group(1));
					queryResult.addIdenititesPercent(Integer.parseInt(matcher.group(2)));
					queryResult.addPositives(matcher.group(3));
					queryResult.addPositivesPercent(Integer.parseInt(matcher.group(4)));
				}				
				result.get(round).addQuery(queryResult);
			}
		}
		allOverResult.add(result);
		allTargets.add(target);
		allMoreThanOneRound.add(moreThanOneRound);
		moreThanOneRound = false;
	}

	private static void writeAll() throws IOException {
		int startInt;
		ArrayList<String> tempStringArray;
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		for(int i = 0;i < allOverResult.size();i++) {
			if(i > 0) writer.newLine();
			writer.write(">" + allTargets.get(i));
			System.out.println(">" + allTargets.get(i));
			startInt = 0;
			if(allMoreThanOneRound.get(i)) startInt = 1;
			for(int a = startInt;a < allOverResult.get(i).size();a++) {
				tempStringArray =  allOverResult.get(i).get(a).getOutput();
				for(int b = 0;b < tempStringArray.size();b++) {
					writer.newLine();
					writer.write(tempStringArray.get(b));
					System.out.println(tempStringArray.get(b));
				}
			}
			writer.newLine();
			writer.write("//");
		}
		writer.close();
	}

}

