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
		MAILFROMSTATE, RCPT_TOSTATE,REQ_DATA,SEND_DATA
	};

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		currentState = ProtocolState.MAILFROMSTATE;

		// BufferedReader fileBuffer=new BufferedReader(new
		// FileReader(args[0]));
		BufferedReader fileBuffer = new BufferedReader(new FileReader(
				"forward/TONYbO@CS.txt"));

		// buffer for reading input
		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader(System.in));

		String nextFileLine = null;
		String nextResponseLine = null;

		do {
			
			nextFileLine = fileBuffer.readLine();
		
			processFileInput(nextFileLine);
			
			printNextCommand();
			
		if (!(currentState == ProtocolState.SEND_DATA)) {
			
			nextResponseLine = responseBuffer.readLine();

			processServerResponse(nextResponseLine);
		}
			
		} while (nextFileLine != null || nextResponseLine != null);

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
			
			printQueue.add(MAIL_FROM+line.substring(line.indexOf(' ')));
			
			currentState=ProtocolState.RCPT_TOSTATE;
			
		}else if(currentState==ProtocolState.RCPT_TOSTATE && line.startsWith("To: ")){
			
			printQueue.add(RCPT_TO+line.substring(line.indexOf(' ')));
			
		}else if(currentState==ProtocolState.RCPT_TOSTATE){
			
			currentState=ProtocolState.REQ_DATA;
			
			System.err.println(DATA);
			
			printQueue.add(line);
			
		}else if(currentState==ProtocolState.REQ_DATA){
			
			printQueue.add(line);
			currentState=ProtocolState.SEND_DATA;
			
		}else if(currentState==ProtocolState.SEND_DATA && line.startsWith("From: ")){
			
			currentState=ProtocolState.MAILFROMSTATE;
			
			printQueue.add(".");
			
			printQueue.add(MAIL_FROM+line.substring(line.indexOf(' ')));
			
		}else if(currentState==ProtocolState.SEND_DATA){
			
			printQueue.add(line);
		}

	}

	private static void processServerResponse(String response) {
		if(response==null){
			return;
		}
		printQueue.add(response);
	}

}
