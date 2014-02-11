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

	private static final String FPATH = "From: <[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";

	private static final String TPATH = "To: <[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";

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

		if ((args[0] == null)) {

			endProgram(ProtocolState.ERROR);
		}

		// start in mail from state
		currentState = ProtocolState.MAILFROMSTATE;

		try {
			// take in a single file as input
			 fileBuffer=new BufferedReader(new
			 FileReader(args[0]));
			//fileBuffer = new BufferedReader(new FileReader(
				//	"forward/TONYbO@CS.txt"));

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
					System.err.println(nextResponseLine);

					processServerResponse(nextResponseLine);
				}

			} while (nextFileLine != null && nextResponseLine != null);

			// end the program
			endProgram(ProtocolState.END_DATA);

		} catch (Exception e) {
			endProgram(ProtocolState.ERROR);
		}

	}

	private static void processFileInput(String line) {

		switch (currentState) {

		case MAILFROMSTATE:
			if (line.matches(FPATH)) {
				// generate command
				System.out.println(MAIL_FROM
						+ line.substring(line.indexOf(' ')));
			}
			break;
		case RCPT_TOSTATE:
			if (line.matches(TPATH)) {
				// generate command
				System.out.println(RCPT_TO + line.substring(line.indexOf(' ')));
			}
			break;

		case REQ_DATA:
			// create DATA command
			System.out.println(DATA);

			break;

		case SEND_DATA:

			if (line == null) {

				currentState = ProtocolState.END_DATA;

				System.out.println(".");

			} else if (line.matches(FPATH)) {

				currentState = ProtocolState.END_DATA;

				System.out.println(".");

				printQueue.add(MAIL_FROM + line.substring(line.indexOf(' ')));

			} else {

				System.out.println(line);
			}
			break;

		case END_DATA:

			System.out.println(line);

			currentState = ProtocolState.MAILFROMSTATE;

			break;

		default:

			break;
		}
	}

	private static void processServerResponse(String response) {
		if (response == null) {
			return;
		}

		switch (currentState) {

		case MAILFROMSTATE:

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

			if (response.matches(DATA_SEND_STATEMENT)) {

				currentState = ProtocolState.SEND_DATA;

			} else {

				endProgram(ProtocolState.ERROR);
			}
			break;

		case END_DATA:

			if (!response.matches(OK_STATEMENT)) {

				endProgram(ProtocolState.ERROR);

			}

			break;

		default:

			break;
		}
	}

	private static void endProgram(ProtocolState state) {

		if (state == ProtocolState.ERROR) {

			System.out.println(QUIT);

			try {

				fileBuffer.close();

			} catch (Exception e) {

				e.printStackTrace();
			}

			System.exit(1);

		} else {

			System.out.println(QUIT);

			try {

				fileBuffer.close();

			} catch (Exception e) {

				e.printStackTrace();
			}

			System.exit(0);
		}
	}
}