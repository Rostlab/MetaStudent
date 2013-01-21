package pp2.prediction.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GOListComparator {

	private String[] predictedList;
	private String[] actualList;
	
	public GOListComparator(String[] predictedList, String[] actualList) {
		this.predictedList = predictedList;
		this.actualList = actualList;
	}
	
	private int cutSize() {
		int cut = 0;
		List<String> temp = Arrays.asList(actualList);
		List<String> checked = new ArrayList<String>();
		for(String pred : predictedList) {
			if(temp.contains(pred) && !checked.contains(pred)) {
				checked.add(pred);
				cut++;
			}
		}
		return cut;
	}
	
	public double precision() {
		double cut = cutSize();
		return cut/(double)predictedList.length;
	}
	
	public double recall() {
		return (double)cutSize()/(double)actualList.length;
	}
}
