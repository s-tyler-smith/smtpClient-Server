package client_side;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class SMTP2 {

	private static ProtocolState currentState;
	private static final String MAIL_FROM="MAIL FROM:";
	private static final String RCPT_TO="RCPT TO:";
	private static final String DATA="DATA";
	
	private static Queue<String> printQueue=new LinkedList<String>();
	

	// enum for maintaining state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE,REQ_DATA,SEND_DATA,END_DATA
	};

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		currentState = ProtocolState.MAILFROMSTATE;

		// BufferedReader fileBuffer=new BufferedReader(new
		// FileReader(args[0]));
		BufferedReader fileBuffer = new BufferedReader(new FileReader(
				"forward/<JEROME@CS"));

		// buffer for reading input
		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader(System.in));

		String nextFileLine = null;
		String nextResponseLine = null;

		do {
			
			
			if((currentState==ProtocolState.REQ_DATA)){
				
				processFileInput(DATA);
						
			}else if(currentState==ProtocolState.END_DATA){
				
				processFileInput(printQueue.poll());
				
			}else{
				nextFileLine = fileBuffer.readLine();
				processFileInput(nextFileLine);
			}
		
			//printNextCommand();
			
		if (!(currentState == ProtocolState.SEND_DATA)) {
			
			nextResponseLine = responseBuffer.readLine();

			processServerResponse(nextResponseLine);
		}
			
		} while (nextFileLine != null && nextResponseLine != null);

		fileBuffer.close();
		responseBuffer.close();

	}
	private static void printNextCommand(){
		if(printQueue.size()!=0){
			System.err.println(printQueue.poll());
			if(printQueue.size()!=0){
				System.err.println(printQueue.poll());
			}
		}
	}

	private static void processFileInput(String line) {
		if(line==null)return;
		//System.out.println(line);
		if(currentState==ProtocolState.MAILFROMSTATE && line.startsWith("From: ")){
			
			System.err.println(MAIL_FROM+line.substring(line.indexOf(' ')));
				
		}else if(currentState==ProtocolState.RCPT_TOSTATE && line.startsWith("To: ")){
			
			System.err.println(RCPT_TO+line.substring(line.indexOf(' ')));
			
		}else if(currentState==ProtocolState.REQ_DATA){
			
			System.err.println(line);
			
		}else if(currentState==ProtocolState.SEND_DATA && (line.startsWith("From: ")||(line==null))){
			
			currentState=ProtocolState.END_DATA;
			
			System.err.println(".");
			
			printQueue.add(MAIL_FROM+line.substring(line.indexOf(' ')));
		
		}else if(currentState==ProtocolState.SEND_DATA){
			
			System.err.println(line);
				
		}else if(currentState==ProtocolState.END_DATA){
			
			System.err.println(line);
			currentState=ProtocolState.MAILFROMSTATE;
		}

	}

	private static void processServerResponse(String response) {
		if(response==null){
			return;
		}
		if(currentState==ProtocolState.MAILFROMSTATE){
			System.err.println(response);
			currentState=ProtocolState.RCPT_TOSTATE;
		}else if(currentState==ProtocolState.RCPT_TOSTATE){
			System.err.println(response);
			currentState=ProtocolState.REQ_DATA;
		}else if(currentState==ProtocolState.REQ_DATA){
			System.err.println(response);
			currentState=ProtocolState.SEND_DATA;
		}else if(currentState==ProtocolState.END_DATA){
			System.err.println(response);
		}
		
	}

}
