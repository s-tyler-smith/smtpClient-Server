package client_side;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class SMTP2 {

	private static ProtocolState currentState;
	
	private static final String MAIL_FROM = "MAIL FROM:";
	private static final String RCPT_TO = "RCPT TO:";
	private static final String DATA = "DATA";

	private static Queue<String> printQueue = new LinkedList<String>();

	// enum for maintaining state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, REQ_DATA, SEND_DATA, END_DATA, ERROR
	};

	public static void main(String[] args) throws IOException {

		currentState = ProtocolState.MAILFROMSTATE;

		// BufferedReader fileBuffer=new BufferedReader(new
		// FileReader(args[0]));
		BufferedReader fileBuffer = new BufferedReader(new FileReader(
				"forward/TONYbO@CS.txt"));

		// buffer for reading input
		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader(System.in));

		String nextFileLine=null,nextResponseLine = null;

		do {
			if (currentState == ProtocolState.ERROR) {
				System.err.println("QUIT");
				break;
			}

			if ((currentState == ProtocolState.REQ_DATA)) {

				processFileInput(DATA);

			} else if (currentState == ProtocolState.END_DATA) {

				if (printQueue.size() > 0) {
					
					processFileInput(printQueue.poll());
				}

			} else {
				
				nextFileLine = fileBuffer.readLine();
				
				processFileInput(nextFileLine);
			}

			// printNextCommand();

			if (!(currentState == ProtocolState.SEND_DATA)) {

				nextResponseLine = responseBuffer.readLine();

				processServerResponse(nextResponseLine);
			}

		} while (nextFileLine != null && nextResponseLine != null);

		fileBuffer.close();
		
		responseBuffer.close();
	}

	private static void processFileInput(String line) {

		if (currentState == ProtocolState.MAILFROMSTATE
				&& line.startsWith("From: ")) {

			System.out.println(MAIL_FROM + line.substring(line.indexOf(' ')));

		} else if (currentState == ProtocolState.RCPT_TOSTATE
				&& line.startsWith("To: ")) {

			System.out.println(RCPT_TO + line.substring(line.indexOf(' ')));

		} else if (currentState == ProtocolState.REQ_DATA) {

			System.out.println(line);

		} else if (currentState == ProtocolState.SEND_DATA && line == null) {
			System.out.println(".");
			currentState = ProtocolState.END_DATA;

		} else if (currentState == ProtocolState.SEND_DATA
				&& line.startsWith("From: ")) {

			currentState = ProtocolState.END_DATA;

			System.out.println(".");
			
			printQueue.add(MAIL_FROM + line.substring(line.indexOf(' ')));

		} else if (currentState == ProtocolState.SEND_DATA) {

			System.out.println(line);

		} else if (currentState == ProtocolState.END_DATA) {

			System.out.println(line);
			
			currentState = ProtocolState.MAILFROMSTATE;
		}
	}

	private static void processServerResponse(String response) {
		if (response == null) {
			return;
		}
		
		if (currentState == ProtocolState.MAILFROMSTATE) {
			if (response.startsWith("250")) {
				
				System.err.println(response);
				
				currentState = ProtocolState.RCPT_TOSTATE;
				
			} else {
				
				currentState = ProtocolState.ERROR;
				
			}
		} else if (currentState == ProtocolState.RCPT_TOSTATE) {
			if (response.startsWith("250")) {
				
				System.err.println(response);
				
				currentState = ProtocolState.REQ_DATA;
			} else {
				
				currentState = ProtocolState.ERROR;
			}
		} else if (currentState == ProtocolState.REQ_DATA) {
			if (response.startsWith("354")) {
				
				System.err.println(response);
				
				currentState = ProtocolState.SEND_DATA;
			} else {
				
				currentState = ProtocolState.ERROR;
			}

		} else if (currentState == ProtocolState.END_DATA) {
			if (!response.startsWith("250")) {
				currentState = ProtocolState.ERROR;
			}
		}
	}
}
