import java.io.FileNotFoundException;
import java.io.IOException;



public class GOSSIPValidation {

	public static void main(String[] args){
		String valFile = "";

		try{
			valFile = args[0];
			try{
				Validator val = new Validator(valFile);
				val.validate();
			}
			catch(FileNotFoundException e1){
				System.err.println(e1.toString());
			}
			catch(IOException e2){
				System.err.println(e2.toString());
			}
		}
		catch(IndexOutOfBoundsException e){
			System.err.println("Please provide a val-file (for example: SIMPLECOUNT_0.8.val)");
	}
		
		
	}
}
