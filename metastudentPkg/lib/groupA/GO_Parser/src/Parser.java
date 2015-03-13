import java.io.IOException;


public class Parser{
	
	public static void main(String[] args){
		
		ParseGONew pgo = new ParseGONew("/mnt/project/metastudent/tobias/metaNew/transitiveClosure2014.txt");
		try {
			pgo.ParseDeductiveClosure();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
