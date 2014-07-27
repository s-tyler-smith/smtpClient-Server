import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Client {

	// Strings I use for generating/comparing commands
	private static final String MAIL_FROM = "MAIL FROM:";
	private static final String RCPT_TO = "RCPT TO:";
	private static final String DATA = "DATA";
	private static final String QUIT = "QUIT";
	private static final String TO = "To:";
	private static final String FROM = "From:";

	// strings to check the lines in the file for commands
	private static final String FPATH = "From:(\\s*)<[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";
	private static final String TPATH = "To:(\\s*)<[^<>()\\.,\\\\:@;\\s\\[\\]\"]+@[a-zA-Z][a-zA-Z\\d]+(\\.{1}[a-zA-Z][a-zA-Z\\d]+)*>";

	//string for socket programming
	private static final String newLine = "\n";
	private static final String ARGSERROR = "wrong number of arguments";
	private static final String HELO = "HELO ";
	private static final String TWOFIFTY = "250";
	private static final String TWOTWENTY = "220 ";
	private static final String FILENAME = "./outgoing";
	private static final String NOTANUMBER = " is not a number";
	private static final String WRONGMAIL = "Mail From malformed in outgoing file";
	private static final String WRONGRCPT = "Rcpt to malformed in outgoing file";
	private static final String ERROK = "Error Receiving OK";
	private static final String RCPTOOO = "RCPT TO Command Out of order in outgoing file";
	private static final String PROBCONN = "Problem Connecting Client";

	// expected response messages,I expect any amount of whitespace followed
	// by, 250 or 354 respectfully, followed by a non-digit followed by anything
	private static final String OK_STATEMENT = "( |\t)*250[^\\d].*";
	private static final String DATA_SEND_STATEMENT = "( |\t)*354[^\\d].*";

	// queue for storing commands to print-I only use it when a file has more
	// than one mail from command
	private static Queue<String> printQueue = new LinkedList<String>();

	// readers for file reading and input response reading
	private static BufferedReader fileBuffer, responseBuffer, inFromServer;

	// enum for maintaining protocol state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, REQ_DATA, SEND_DATA, END_DATA, ERROR, CONNECTING, ARGSERROR, CONNERROR
	};

	private static DataOutputStream outToServer;
	private static Socket clientSocket;
	private static String hostName;
	private static int serverPort;
	private static String savedData;

	// variable for representing currentState
	private static ProtocolState currentState;

	public static void main(String[] args) throws IOException {

		// check if no argument passed in
		if ((args == null) || args.length < 2) {

			endProgram(ProtocolState.ARGSERROR);

		} else {

			hostName = args[0];

			try {
				serverPort = Integer.parseInt(args[1]);

				try {
					// start in mail from state
					currentState = ProtocolState.CONNECTING;

					clientSocket = new Socket(hostName, serverPort);
					hostName = hostName.substring(hostName.indexOf('.') + 1);

					if (clientSocket.isConnected()) {
						clientSocket.setKeepAlive(true);
						outToServer = new DataOutputStream(
								clientSocket.getOutputStream());
						inFromServer = new BufferedReader(
								new InputStreamReader(
										clientSocket.getInputStream()));

						String welcomeMessage = inFromServer.readLine();

						if (!welcomeMessage.startsWith(TWOTWENTY)) {
							endProgram(ProtocolState.CONNERROR);
						}

						outToServer.writeBytes(HELO + hostName + newLine);
						String respondHeloMessage = inFromServer.readLine();

						if (!respondHeloMessage.startsWith(TWOFIFTY)) {
							endProgram(ProtocolState.CONNERROR);
						}

						// take in a single file as input
						fileBuffer = new BufferedReader(
								new FileReader(FILENAME));


						currentState = ProtocolState.MAILFROMSTATE;
						// strings for holding readline method calls
						String nextFileLine = null, nextResponseLine = null;

						do {

							/*
							 * First check the state before determining what to
							 * read from the file
							 */
							if ((currentState == ProtocolState.REQ_DATA)) {

								nextFileLine = fileBuffer.readLine();
								if (nextFileLine.startsWith(TO)) {
									currentState = ProtocolState.RCPT_TOSTATE;
									processFileInput(nextFileLine);
								} else {
									// will save data if not another recipient
									// exits
									// one "bug" is that if at any point a rcpt
									// to is
									// malformed it will be treated as data
									savedData = nextFileLine;

									processFileInput(DATA);

								}

							} else if (currentState == ProtocolState.SEND_DATA
									&& printQueue.size() > 0) {

								currentState = ProtocolState.MAILFROMSTATE;

								processFileInput(printQueue.poll());
							} else {

								// normal case where we simply read a line of
								// the file
								// and process it
								nextFileLine = fileBuffer.readLine();

								processFileInput(nextFileLine);
							}

							/*
							 * after reading a line from the file we want to ask
							 * wait for a response from the "server" as long as
							 * we are not in send_data state meaning we are
							 * printing out the data of the message
							 */
							if (!(currentState == ProtocolState.SEND_DATA)) {

								nextResponseLine = inFromServer.readLine();

								processServerResponse(nextResponseLine);
								// recover lost data from multlple rcpt to hack
								if (currentState == ProtocolState.SEND_DATA) {
									processFileInput(savedData);
								}
							}

						} while (nextFileLine != null
								&& nextResponseLine != null);

						// end the program successfully
						endProgram(ProtocolState.END_DATA);
					}

				} catch (Exception e) {

					// file wasn't found or an error creating
					// the buffers has occurred
					endProgram(ProtocolState.ERROR);
				}
			} catch (Exception e) {
				System.out.println(args[1] + NOTANUMBER);
			}

		}

	}

	private static void processFileInput(String line) throws IOException {

		// Process file data depending
		// on the current state
		switch (currentState) {

		case MAILFROMSTATE:

			if (!line.matches(FPATH)) {
				System.out.println(WRONGMAIL);
				endProgram(ProtocolState.ERROR);
			}
			// generate MAIL FROM: command
			outToServer.writeBytes(MAIL_FROM
					+ line.substring(line.indexOf('<')) + newLine);
			break;

		case RCPT_TOSTATE:
			if (!line.matches(TPATH)) {
				System.out.println(WRONGRCPT);
				endProgram(ProtocolState.ERROR);
			}
			// generate RCPT TO: command
			outToServer.writeBytes(RCPT_TO + line.substring(line.indexOf('<'))
					+ newLine);
			break;

		case REQ_DATA:

			// create DATA command
			outToServer.writeBytes(DATA + newLine);

			break;
		// case where we are supposed to be printing the message body
		// we have to check for end of file or next mail from message
		case SEND_DATA:

			// end of file
			if (line == null) {

				currentState = ProtocolState.END_DATA;

				outToServer.writeBytes("." + newLine);

				// another mail from command is detected
			} else if (line.startsWith(FROM)) {

				// currentState = ProtocolState.END_DATA;

				outToServer.writeBytes("." + newLine);

				String response = inFromServer.readLine();

				if (response.matches(OK_STATEMENT)) {

					printQueue.add(line);

				} else {
					System.out.println(ERROK);
					endProgram(ProtocolState.ERROR);
				}

			} else if (line.startsWith(TO)) {
				System.out.println(RCPTOOO);
				endProgram(ProtocolState.ERROR);
			} else {
				// normal case where are simply printing
				// data in the message body
				outToServer.writeBytes(line + newLine);
			}
			break;

		case END_DATA:
			// done sending data
			outToServer.writeBytes(line + newLine);

			// reset to mail from state
			currentState = ProtocolState.MAILFROMSTATE;

			break;

		default:
			break;
		}
	}

	private static void processServerResponse(String response)
			throws IOException {
		// if null just stop
		if (response == null) {
			return;
		}

		switch (currentState) {

		case MAILFROMSTATE:
			// if response was good change state
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
			// transition to sending data state where the
			// message body gets printed out and we no longer
			// need to get a server response
			if (response.matches(DATA_SEND_STATEMENT)) {

				currentState = ProtocolState.SEND_DATA;

			} else {

				endProgram(ProtocolState.ERROR);
			}
			break;

		case END_DATA:
			// if ending is anything but a 250 ok
			// quit
			if (!response.matches(OK_STATEMENT)) {

				endProgram(ProtocolState.ERROR);

			}

			break;

		default:

			break;
		}
	}

	private static void endProgram(ProtocolState state) throws IOException {
		// an error has occurred somewhere in the program
		if (state == ProtocolState.ERROR) {

			outToServer.writeBytes(QUIT + newLine);

			try {
				// close the file read buffer
				fileBuffer.close();
				String response;
				if ((response = inFromServer.readLine()) != null) {
					clientSocket.close();
				} else {
					clientSocket.close();
				}

			} catch (Exception e) {
			}
			System.exit(1);
			// exit with no error
		} else if (state == ProtocolState.ARGSERROR) {
			System.out.println(ARGSERROR);
			System.exit(1);
		} else if (state == ProtocolState.CONNERROR) {
			System.out.println(PROBCONN);
			clientSocket.close();
		}

		else {

			outToServer.writeBytes(QUIT + newLine);
			String quitResponse;
			if ((quitResponse = inFromServer.readLine()) != null) {
				try {

					fileBuffer.close();
					clientSocket.close();

				} catch (Exception e) {
				}
			} else {
				try {

					fileBuffer.close();
					clientSocket.close();

				} catch (Exception e) {
				}
			}

			System.exit(0);
		}
	}
}
