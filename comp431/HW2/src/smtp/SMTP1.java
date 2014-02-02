package smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SMTP1 {

	private static int recipients = 0;

	private enum ProtocalState {MAILFROMSTATE, RCPT_TOSTATE, DATA};

	public static void main(String[] args) {
		// variable for line of input
		String nextLine = "";

		// create parser object
		//

		// create protocal enum for state changes
		// begins in MAILFROM state
		ProtocalState state = ProtocalState.MAILFROMSTATE;

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
			System.out.println(state.toString());
		} while (nextLine != null);
	}

	private static ProtocalState parseInput(String input,
			ProtocalState currentState) {
		// print input first
		System.out.println(input);

		if (currentState == ProtocalState.DATA) {

			if (input.equals(".")) {
				displayResults(0);
				// write array to file

				// change state back to MAILFROMSTATE
				return ProtocalState.MAILFROMSTATE;
			} else {
				// add to array
				return currentState;
			}

		} else if (currentState == ProtocalState.MAILFROMSTATE) {
			return checkMailFrom(input.trim(), currentState);
		} else if (currentState == ProtocalState.RCPT_TOSTATE) {
			return checkRcptTo(input.trim(), currentState);
		} else {
			return currentState;
		}
	}

	private static ProtocalState checkRcptTo(String input,
			ProtocalState currentState) {
		if (input.equals("DATA")) {
			if (recipients > 0) {
				displayResults(4);
				return ProtocalState.DATA;
			} else {
				displayResults(5);
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
			displayResults(1);
			return currentState;
		}

		String cmd = splitInput[0];

		if (cmd.equals("MAIL FROM")) {
			displayResults(5);
			return currentState;
		}

		if (!cmd.equals("RCPT TO")) {
			displayResults(1);
			return currentState;
		}

		// trim white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check for angle brackets are at the beginning and end
		if (!(path.charAt(0) == '<' && path.charAt(path.length() - 1) == '>')) {
			displayResults(2);
			return currentState;
		}

		// split on @ sign to break up local-part and domain
		splitInput = path.split("@");

		// length should equal two
		if (!(splitInput.length == 2)) {
			displayResults(2);
			return currentState;
		}

		// strip off angle bracket from local-part
		String pathPart = splitInput[0].substring(1);

		// make local part doesn't contain non-ASCII,special, or space
		// characters
		if (!(pathPart.matches("[^<>()\\.,\\\\:@;\\s\\[\\]\"]+") && pathPart
				.matches("[\\p{ASCII}]*"))) {
			displayResults(2);
			return currentState;
		}

		// reset pathPart to be domain with ending angle bracket stripped
		pathPart = splitInput[1].substring(0, splitInput[1].length() - 1);

		// split up domain on periods
		String[] domain = pathPart.split("\\.");

		// check if there at least something a two character long domain
		if (!(domain[0].trim().length() >= 2)) {
			displayResults(2);
			return currentState;
		}
		// method for looping through domain parts
		if (checkDomain(domain)) {
			displayResults(0);
			// add RCPT TO:
			recipients++;
			return currentState;
		} else {
			displayResults(2);
			return currentState;
		}
	}

	private static ProtocalState checkMailFrom(String input,
			ProtocalState currentState) {
		if (input == "DATA") {
			displayResults(5);
			return currentState;
		}

		// split on first colon
		String[] splitInput = input.split(":", 2);

		/*
		 * when split on ':' there should only be two parts the 'mail from' and
		 * the 'path'
		 */
		if (splitInput.length != 2) {
			displayResults(1);
			return currentState;
		}

		String cmd = splitInput[0];

		if (cmd.equals("RCPT TO")) {
			displayResults(5);
			return currentState;
		}

		if (!cmd.equals("MAIL FROM")) {
			displayResults(1);
			return currentState;
		}

		// trim white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check for angle brackets are at the beginning and end
		if (!(path.charAt(0) == '<' && path.charAt(path.length() - 1) == '>')) {
			displayResults(2);
			return currentState;
		}

		// split on @ sign to break up local-part and domain
		splitInput = path.split("@");

		// length should equal two
		if (!(splitInput.length == 2)) {
			displayResults(2);
			return currentState;
		}

		// strip off angle bracket from local-part
		String pathPart = splitInput[0].substring(1);

		// make local part doesn't contain non-ASCII,special, or space
		// characters
		if (!(pathPart.matches("[^<>()\\.,\\\\:@;\\s\\[\\]\"]+") && pathPart
				.matches("[\\p{ASCII}]*"))) {
			displayResults(2);
			return currentState;
		}

		// reset pathPart to be domain with ending angle bracket stripped
		pathPart = splitInput[1].substring(0, splitInput[1].length() - 1);

		// split up domain on periods
		String[] domain = pathPart.split("\\.");

		// check if there at least something a two character long domain
		if (!(domain[0].trim().length() >= 2)) {
			displayResults(2);
			return currentState;
		}
		// method for looping through domain parts
		if (checkDomain(domain)) {
			displayResults(0);
			// add From: path
			return ProtocalState.RCPT_TOSTATE;
		} else {
			displayResults(2);
			return currentState;
		}
	}

	/*
	 * method that take in a string array and checks if thedomain token is
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
	private static void displayResults(int tokenCode) {
		if (tokenCode == 0) {
			System.out.println("250 ok");
		} else if (tokenCode == 1) {
			System.out.println("500 Syntax error: command unrecognized");
		} else if (tokenCode == 2) {
			System.out.println("501 Syntax error in parameters or arguments");
		} else if (tokenCode == 4) {
			System.out.println("354 Start mail input; end with <CRLF>.<CRLF>");
		} else if (tokenCode == 5) {
			System.out.println("503 Bad sequence of commands");
		} else {
			return;
		}
	}
}
