package smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import parsing.SimpleParser;

public class SMTP1 {

	
	public static void main(String[] args) {
		//variable for line of input
		String nextLine="";
		
		//create parser object
		SimpleParser myParser=new SimpleParser();
		//buffer for reading input
		BufferedReader myBuffer=new BufferedReader(new InputStreamReader(System.in));
		
		/*do-while that tries to get next line of file
		 * and if successful passes line to the parse
		 * method else it catches the exception.
		 * It halts when readLine reads a null
		 */
		do{
			try {	
				nextLine=myBuffer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			myParser.parseInput(nextLine,"DATA");
		}while(nextLine!=null);
	}
}
