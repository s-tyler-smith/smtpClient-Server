package smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SMTP1 {

	private static int recipients = 0;
	private static ArrayList<String> emailInfo;

	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, DATA
	};

	private enum PrintResults {
		OK, BEGINDATA, WRONGORDER, CMDNOTRECOGNIZED, BADFORM
	}

	public static void main(String[] args) {
		// variable for line of input
		String nextLine = "";

		// create protocol enum for state changes
		// begins in MAILFROM state
		ProtocolState state = ProtocolState.MAILFROMSTATE;

		// create
		emailInfo = new ArrayList<String>();

		// buffer for reading input
		BufferedReader myBuffer = new BufferedReader(new InputStreamReader(
				System.in));

		/*
		 * do-while that tries to get next line of file and if successful passes
		 * line to the parse method else it catches the exception. It halts when
		 * readLine reads a null
		 */
		do {
			try {
				nextLine = myBuffer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			state = parseInput(nextLine, state);
			// System.out.println(state.toString());
		} while (nextLine != null);
	}

	private static ProtocolState parseInput(String input,
			ProtocolState currentState) {

		// if first line of file is null
		if (input == null) {
			return currentState;
		}

		// print input first
		System.out.println(input);

		if (currentState == ProtocolState.DATA) {

			if (input.equals(".")) {
				displayResults(PrintResults.OK);
				// write array to file
				writeToFile(emailInfo);

				// reset mail objects for new message
				resetEmail();

				// change state back to MAILFROMSTATE
				return ProtocolState.MAILFROMSTATE;
			} else {
				// add to array
				emailInfo.add(input);
				return currentState;
			}

		} else if (currentState == ProtocolState.MAILFROMSTATE) {

			return checkMailFrom(input.trim(), currentState);

		} else if (currentState == ProtocolState.RCPT_TOSTATE) {

			return checkRcptTo(input.trim(), currentState);

		} else {

			return currentState;
		}
	}

	private static void resetEmail() {
		// empty arraylist with email information
		emailInfo.clear();
		// set recipients back zero
		recipients = 0;
	}

	private static ProtocolState checkRcptTo(String input,
			ProtocolState currentState) {
		if (input.equals("DATA")) {
			if (recipients > 0) {
				displayResults(PrintResults.BEGINDATA);
				return ProtocolState.DATA;
			} else {
				displayResults(PrintResults.WRONGORDER);
				return currentState;
			}
		}

		// split on first colon
		String[] splitInput = input.split(":", 2);

		/*
		 * when split on ':' there should only be two parts the 'mail from' and
		 * the 'path'
		 */
		if (splitInput.length != 2) {
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			return currentState;
		}

		String cmd = splitInput[0];

		if (cmd.equals("MAIL FROM")) {
			displayResults(PrintResults.WRONGORDER);
			return currentState;
		}

		if (!cmd.equals("RCPT TO")) {
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			return currentState;
		}

		// trim white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check for angle brackets are at the beginning and end
		if (!(path.length() > 0 && path.charAt(0) == '<' && path.charAt(path
				.length() - 1) == '>')) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// split on @ sign to break up local-part and domain
		splitInput = path.split("@");

		// length should equal two
		if (!(splitInput.length == 2)) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// strip off angle bracket from local-part
		String pathPart = splitInput[0].substring(1);

		// make local part doesn't contain non-ASCII,special, or space
		// characters
		if (!(pathPart.matches("[^<>()\\.,\\\\:@;\\s\\[\\]\"]+") && pathPart
				.matches("[\\p{ASCII}]*"))) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// reset pathPart to be domain with ending angle bracket stripped
		pathPart = splitInput[1].substring(0, splitInput[1].length() - 1);

		// split up domain on periods
		String[] domain = pathPart.split("\\.");

		// check if there at least something a two character long domain
		if (!(domain[0].trim().length() >= 2)) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}
		// method for looping through domain parts
		if (checkDomain(domain)) {
			displayResults(PrintResults.OK);
			emailInfo.add("To: " + path);
			// add RCPT TO:
			recipients++;
			return currentState;
		} else {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}
	}

	private static ProtocolState checkMailFrom(String input,
			ProtocolState currentState) {
		if (input.equals("DATA")) {
			displayResults(PrintResults.WRONGORDER);
			return currentState;
		}

		// split on first colon
		String[] splitInput = input.split(":", 2);

		/*
		 * when split on ':' there should only be two parts the 'mail from' and
		 * the 'path'
		 */
		if (splitInput.length != 2) {
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			return currentState;
		}

		String cmd = splitInput[0];

		if (cmd.equals("RCPT TO")) {
			displayResults(PrintResults.WRONGORDER);
			return currentState;
		}

		if (!cmd.equals("MAIL FROM")) {
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			return currentState;
		}

		// trim white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check for angle brackets are at the beginning and end
		if (!(path.length() > 0 && path.charAt(0) == '<' && path.charAt(path
				.length() - 1) == '>')) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// split on @ sign to break up local-part and domain
		splitInput = path.split("@");

		// length should equal two
		if (!(splitInput.length == 2)) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// strip off angle bracket from local-part
		String pathPart = splitInput[0].substring(1);

		// make local part doesn't contain non-ASCII,special, or space
		// characters
		if (!(pathPart.matches("[^<>()\\.,\\\\:@;\\s\\[\\]\"]+") && pathPart
				.matches("[\\p{ASCII}]*"))) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}

		// reset pathPart to be domain with ending angle bracket stripped
		pathPart = splitInput[1].substring(0, splitInput[1].length() - 1);

		// split up domain on periods
		String[] domain = pathPart.split("\\.");

		// check if there at least something a two character long domain
		if (!(domain[0].trim().length() >= 2)) {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}
		// method for looping through domain parts
		if (checkDomain(domain)) {
			displayResults(PrintResults.OK);
			// add From: path
			emailInfo.add("From: " + path);
			return ProtocolState.RCPT_TOSTATE;
		} else {
			displayResults(PrintResults.BADFORM);
			return currentState;
		}
	}

	private static void writeToFile(ArrayList<String> emailInfo) {
		if (emailInfo.size() < 1) {
			return;
		}
		/*
		 * index for where data starts 0 has mail from: 1-recipients are
		 * recipients Data begins at index recipients + 1 and goes till end of
		 * arraylist
		 */
		for (int i = 1; i <= recipients; i++) {

			String directoryName = emailInfo.get(i);

			// a little cryptic but basically just stripping the angle brackets
			// to form the file path name
			directoryName = directoryName.substring(directoryName.indexOf('<')+1,
					directoryName.length() - 1);

			for (int j = 0; j < emailInfo.size(); j++) {

				try (PrintWriter myPrinter = new PrintWriter(new BufferedWriter(
						new FileWriter("forward/" + directoryName,
								true)))) {
					myPrinter.println(emailInfo.get(j));
					myPrinter.close();
				} catch (IOException e) {
					e.getStackTrace();
				}
			}
		}
	}

	/*
	 * method that take in a string array and checks if the domain token is
	 * properly formated
	 */
	private static boolean checkDomain(String[] domainArray) {
		boolean pass;

		// check if array is not null and has values
		if (domainArray != null && domainArray.length > 0) {

			// boolean for correct format
			pass = true;

			/*
			 * check if each part is made up of at least 2 characters. Must
			 * begin with a letter but can contain as many letters or digits
			 * after it
			 */
			for (int i = 0; i < domainArray.length; i++) {
				if (!domainArray[i].matches("[a-zA-Z][a-zA-Z\\d]+")) {

					// wrong order or wrong type of characters so it fails
					pass = false;

					// at least one part of domain failed so break out of loop
					break;
				}
			}
			// array was null or empty
		} else {
			pass = false;
		}
		return pass;
	}

	// helper method that prints out results based code
	private static void displayResults(PrintResults code) {
		switch (code) {
		case OK:
			System.out.println("250 ok");
			break;
		case BEGINDATA:
			System.out.println("354 Start mail input; end with <CRLF>.<CRLF>");
			break;
		case CMDNOTRECOGNIZED:
			System.out.println("500 Syntax error: command unrecognized");
			break;
		case BADFORM:
			System.out.println("501 Syntax error in parameters or arguments");
			break;
		case WRONGORDER:
			System.out.println("503 Bad sequence of commands");
			break;
		}
	}
}