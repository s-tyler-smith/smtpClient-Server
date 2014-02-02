package smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import parsing.ParsedObject;
import parsing.SimpleParser;

public class SMTP1 {
	
	private enum ProtocalState {
		MAILFROMSTATE(0),
		RCPT_TOSTATE(1),
		DATAWAITINGSTATE(2),
		DATARECIEVINGSTATE(3);
		
		private int order;
		
		private ProtocalState(int initOrder){
			this.order=initOrder;
		}
		public int getOrder(){
			return order;
		}
	};
	private static final String MAILFROMCMD="MAIL FROM";
	private static final String RCPTTOCMD="RCPT TO";
	private static final String DATACMD="DATA";
	
	public static void main(String[] args) {
		//variable for line of input
		String nextLine="";
		
		//create parser object
		SimpleParser myParser=new SimpleParser();
		
		//create protocal enum for state changes
		//begins in MAILFROM state
		ProtocalState state=ProtocalState.MAILFROMSTATE;
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
			ParsedObject inputObj=myParser.parseInput(nextLine,MAILFROMCMD);
			if(inputObj.getSuccessfulParse()){
				
			}else{
				
			}
		}while(nextLine!=null);
	}
	
	private void checkProtocalState(){
		
	}
}
