import java.io.IOException;


public class Parser{
	
	public static void main(String[] args){
		
		ParseGONew pgo = new ParseGONew("deductiveClosureGO.txt");
		try {
			pgo.ParseDeductiveClosure();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
