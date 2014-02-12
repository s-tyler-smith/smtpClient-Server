package client_side;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class SMTP2 {

	// Strings I use for generating/comparing commands
	private static final String MAIL_FROM = "MAIL FROM:";
	private static final String RCPT_TO = "RCPT TO:";
	private static final String DATA = "DATA";
	private static final String QUIT="QUIT";

	//strings to check the lines in the file for commands
	private static final String FPATH = "From: <[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";
	private static final String TPATH = "To: <[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";

	//expected response messages,I expect any amount of whitespace followed
	//by, 250 or 354 respectfully, followed by a non-digit followed by anything
	private static final String OK_STATEMENT = "( |\t)*250[^\\d].*";
	private static final String DATA_SEND_STATEMENT = "( |\t)*354[^\\d].*";

	// queue for storing commands to print-I only use it when a file has more
	// than one mail from command
	private static Queue<String> printQueue = new LinkedList<String>();

	// readers for file reading and input response reading
	private static BufferedReader fileBuffer, responseBuffer;

	// enum for maintaining protocol state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, REQ_DATA, SEND_DATA, END_DATA, ERROR
	};

	// variable for representing currentState
	private static ProtocolState currentState;

	public static void main(String[] args) {

		//check if no argument passed in
		if ((args == null) || args.length<=0) {

			endProgram(ProtocolState.ERROR);
		}

		try {
			// start in mail from state
			currentState = ProtocolState.MAILFROMSTATE;
			
			// take in a single file as input
			 fileBuffer=new BufferedReader(new
			 FileReader(args[0]));
			 
			// buffer for reading input
			responseBuffer = new BufferedReader(
					new InputStreamReader(System.in));

			// strings for holding readline method calls
			String nextFileLine = null, nextResponseLine = null;

			do {
				/*
				 * First check the state before determining what to read from
				 * the file
				 */
				if ((currentState == ProtocolState.REQ_DATA)) {

					// just send the "Data" command and don't read
					// another line from the file
					processFileInput(DATA);

				} else if (currentState == ProtocolState.END_DATA) {

					// if in the end sate check if another mail from command
					// was saved/if so print it out and continue
					if (printQueue.size() > 0) {

						processFileInput(printQueue.poll());
					}

				} else {

					// normal case where we simply read a line of the file
					// and process it
					nextFileLine = fileBuffer.readLine();

					processFileInput(nextFileLine);
				}

				/*
				 * after reading a line from the file we want to ask wait for a
				 * response from the "server" as long as we are not in send_data
				 * state meaning we are printing out the data of the message
				 */
				if (!(currentState == ProtocolState.SEND_DATA)) {

					nextResponseLine = responseBuffer.readLine();
					
					//output server response on err
					System.err.println(nextResponseLine);
					
					processServerResponse(nextResponseLine);
				}

			} while (nextFileLine != null && nextResponseLine != null);

			// end the program successfully
			endProgram(ProtocolState.END_DATA);

		} catch (Exception e) {
			
			//file wasn't found or an error creating
			//the buffers has occurred
			endProgram(ProtocolState.ERROR);
		}
	}

	private static void processFileInput(String line) {

		//Process file data depending 
		//on the current state
		switch (currentState) {

		case MAILFROMSTATE:
			
			if (line.matches(FPATH)) {
				// generate  MAIL FROM: command
				System.out.println(MAIL_FROM
						+ line.substring(line.indexOf(' ')));
			}
			break;
			
		case RCPT_TOSTATE:
			if (line.matches(TPATH)) {
				// generate RCPT TO: command
				System.out.println(RCPT_TO + line.substring(line.indexOf(' ')));
			}
			break;

		case REQ_DATA:
			
			// create DATA command
			System.out.println(DATA);

			break;
		//case where we are supposed to be printing the message body
		//we have to check for end of file or next mail from message
		case SEND_DATA:

			//end of file 
			if (line == null) {

				currentState = ProtocolState.END_DATA;

				System.out.println(".");
			//another mail from command is detected
			} else if (line.matches(FPATH)) {

				currentState = ProtocolState.END_DATA;

				System.out.println(".");
				//save that command and continue
				//I will print it out the next while loop iteration
				printQueue.add(MAIL_FROM + line.substring(line.indexOf(' ')));

			} else {
				//normal case where are simply printing 
				//data in the message body
				System.out.println(line);
			}
			break;

		case END_DATA:
			//done sending data
			System.out.println(line);
			
			//reset to mail from state
			currentState = ProtocolState.MAILFROMSTATE;

			break;

		default:
			break;
		}
	}

	private static void processServerResponse(String response) {
		//if null just stop
		if (response == null) {
			return;
		}

		switch (currentState) {

		case MAILFROMSTATE:
			//if response was good change state
			if (response.matches(OK_STATEMENT)) {

				currentState = ProtocolState.RCPT_TOSTATE;

			} else {

				endProgram(ProtocolState.ERROR);

			}
			break;

		case RCPT_TOSTATE:

			if (response.matches(OK_STATEMENT)) {

				currentState = ProtocolState.REQ_DATA;

			} else {

				endProgram(ProtocolState.ERROR);
			}
			break;

		case REQ_DATA:
			//transition to sending data state where the 
			//message body gets printed out and we no longer
			//need to get a server response
			if (response.matches(DATA_SEND_STATEMENT)) {

				currentState = ProtocolState.SEND_DATA;

			} else {

				endProgram(ProtocolState.ERROR);
			}
			break;

		case END_DATA:
			//if ending is anything but a 250 ok
			//quit
			if (!response.matches(OK_STATEMENT)) {

				endProgram(ProtocolState.ERROR);

			}

			break;

		default:

			break;
		}
	}

	private static void endProgram(ProtocolState state) {
		//an error has occurred somewhere in the program
		if (state == ProtocolState.ERROR) {
			
			System.out.println(QUIT);

			try {
				//close the file read buffer
				fileBuffer.close();

			} catch (Exception e) {
			}

			System.exit(1);
		//exit with no error
		} else {

			System.out.println(QUIT);

			try {

				fileBuffer.close();

			} catch (Exception e) {
			}

			System.exit(0);
		}
	}
}