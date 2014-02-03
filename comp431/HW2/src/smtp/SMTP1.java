package smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SMTP1 {
	
	// regex expressions used for parsing
	private static final String NOSPECIALCHARACTERS="[^<>()\\.,\\\\:@;\\s\\[\\]\"]+";
	private static final String ASCIISAFE="[\\p{ASCII}]*";
	private static final String DOMAINREGEX="[a-zA-Z][a-zA-Z\\d]+";
	
	// int for counting mail recipients
	private static int recipients;
	
	// arraylist for email write data
	private static ArrayList<String> emailInfo;
	
	// enum for maintaining state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, DATA
	};

	// enum for handling what to print
	private enum PrintResults {
		OK, BEGINDATA, WRONGORDER, CMDNOTRECOGNIZED, BADFORM
	};

	public static void main(String[] args) {
		
		// variable for line of input
		String nextLine = "";

		// create protocol enum for state changes
		// begins in MAILFROM state
		ProtocolState state = ProtocolState.MAILFROMSTATE;

		// create arrayList to hold all email info
		emailInfo = new ArrayList<String>();
		
		//variable for counting how many recipients
		recipients=0;
		
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
			
			//pass current state and input to be parsed
			//returns next state to go to
			state = parseInput(nextLine, state);
			
		} while (nextLine != null);
	}

	private static ProtocolState parseInput(String input,
			ProtocolState currentState) {

		// if first line of file is null
		// simple return the currentState
		if (input == null) {
			return currentState;
		}

		// print input first
		System.out.println(input);
		
		//if in data state every input is ok except a
		//lone period
		if (currentState == ProtocolState.DATA) {

			if (input.equals(".")) {
				
				// print 250 OK
				displayResults(PrintResults.OK);
				
				// write array of email senders/recipients
				// to file
				writeToFile(emailInfo);

				// reset mail objects for new message
				resetEmail();

				// change state back to MAILFROMSTATE
				return ProtocolState.MAILFROMSTATE;
				
			} else {
				//every other kind of input just gets added to
				//the arraylist to be written later
				emailInfo.add(input);
				
				return currentState;
			}
		
		} else if (currentState == ProtocolState.MAILFROMSTATE) {
			
			//if the current state is the mailfrom pass trimmed input to the mailfrom parse method
			return checkMailFrom(input.trim(), currentState);

		} else if (currentState == ProtocolState.RCPT_TOSTATE) {
			
			//if the current state is the rcpt to state pass trimmed input to the rcpt parse method
			//it will return a protocolState
			return checkRcptTo(input.trim(), currentState);

		} else {

			return currentState;
		}
	}
	
	/* method that parses input with the expected state 
	 * being the MAIL FROM state. It prints the appropriate 
	 * message (error or ok). It also can add From:
	 * information to the emailInfo arraylist for writing
	 * to a file later. Returns the next state.
	 */
	private static ProtocolState checkMailFrom(String input,
			ProtocolState currentState) {
		
		//if DATA was sent it's in the wrong order
		if (input.equals("DATA")) {
			
			displayResults(PrintResults.WRONGORDER);
			
			return currentState;
		}

		// split on first colon
		String[] splitInput = input.split(":", 2);

		/*
		 * when split on ':' there should only be two parts the 'MAIL FROM'||'RCPT TO' and
		 * the 'path'
		 */
		if (splitInput.length != 2) {
			
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			
			return currentState;
		}
		
		//input still good so see which type of command was sent
		String cmd = splitInput[0];

		//wrong command
		if (cmd.equals("RCPT TO")) {
			
			displayResults(PrintResults.WRONGORDER);
			
			return currentState;
		}
		
		//anything except MAIL FROM is an unknown command
		if (!cmd.equals("MAIL FROM")) {
			
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			
			return currentState;
		}

		// trim white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check for something after command and angle brackets 
		//are at the beginning and end
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
		String pathPart = splitInput[0].substring(splitInput[0].indexOf('<')+1);

		// make sure local part doesn't contain non-ASCII,
		//special, or space characters
		if (!(pathPart.matches(NOSPECIALCHARACTERS) && pathPart
				.matches(ASCIISAFE))) {
			
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
			
			// successful parse so add From: path
			emailInfo.add("From: " + path);
			
			//change to next state
			return ProtocolState.RCPT_TOSTATE;
			
		} else {
			
			//unsuccessful parse
			displayResults(PrintResults.BADFORM);
			
			//remain in MAIL FROM: state
			return currentState;
		}
	}
	
	/* method that parses input with the expected state 
	 * being the RCPT TO state. It prints the appropriate 
	 * message (error or ok). It also can add recipient
	 * information to the emailInfo arraylist for writing
	 * to a file later. Returns the next state.
	 */
	private static ProtocolState checkRcptTo(String input,
			ProtocolState currentState) {
		
		//initial check if input equals DATA
		if (input.equals("DATA")) {
			
			/*if at least one recipient then it
			* is a legal operation else it is
			* in the wrong order
			*/
			
			if (recipients > 0) {
				
				displayResults(PrintResults.BEGINDATA);
				
				//change state to DATA state
				return ProtocolState.DATA;
				
			} else {
				
				//wrong order error message
				displayResults(PrintResults.WRONGORDER);
				
				//still in same state
				return currentState;
			}
		}

		//input still has a possibility of being right
		//so split on first colon
		String[] splitInput = input.split(":", 2);

		// when split on ':' there should only be two parts the 'MAIL FROM' || 'RCPT TO' and the 'path'
		if (splitInput.length != 2) {
			
			//print 500 error
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			
			return currentState;
		}
		
		//input still good so see which type of command was sent
		String cmd = splitInput[0];
		
		//correct format but wrong order
		if (cmd.equals("MAIL FROM")) {
			
			displayResults(PrintResults.WRONGORDER);
			
			return currentState;
		}

		//if anything other than RCPT TO it is
		//not a valid command
		if (!cmd.equals("RCPT TO")) {
			
			//error
			displayResults(PrintResults.CMDNOTRECOGNIZED);
			
			return currentState;
		}

		//we know the command was correct now trim 
		//white space since grammar is ambiguous
		String path = splitInput[1].trim();

		// check if anything is after the command and 
		// check for angle brackets are at the beginning and end
		if (!(path.length() > 0 && path.charAt(0) == '<' && path.charAt(path
				.length() - 1) == '>')) {
			
			//bad form	
			displayResults(PrintResults.BADFORM);
			
			return currentState;
		}

		// path is still good so
		// split on @ sign to break up local-part and domain
		splitInput = path.split("@");

		// length should equal two
		if (!(splitInput.length == 2)) {
			
			displayResults(PrintResults.BADFORM);
			
			return currentState;
		}

		/*Since path in good form the local-part will be in
		* in the first part of the splitInput array
		* so lets strip off angle bracket from local-part
		*/
		String pathPart = splitInput[0].substring(splitInput[0].indexOf('<')+1);
		
		// make sure local part doesn't contain non-ASCII,special, or space
		// characters
		if (!(pathPart.matches(NOSPECIALCHARACTERS) && pathPart
				.matches(ASCIISAFE))) {
			
			displayResults(PrintResults.BADFORM);
			
			return currentState;
		}

		// since local part was good
		//make pathPart to be domain with ending angle bracket stripped
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
			
			// reset parse was successful at recipient to
			// email information array
			emailInfo.add("To: " + path);
			
			// add counter RCPT TO:
			recipients++;
			
			//still in RCPT TO state since we can have 
			//multiple people
			return currentState;
			
		} else {
			
			//domain was malformed
			displayResults(PrintResults.BADFORM);
			
			return currentState;
		}
	}
		
	/*method that takes in an arraylist containing email
	 * information to be written to files. The method
	 * writes all the mail data into each of the 
	 * recipients own file. If their file already exists
	 * it will append the new data to that file
	 */
	private static void writeToFile(ArrayList<String> emailInfo) {
		//if less than two it means there is not at least 
		//one from: and one to:
		if (emailInfo.size() < 2) {
			return;
		}
		
		/*skip over from: data at the0 index and 
		* loop for all recipients. Write email data 
		* into each recipient's corresponding file
		*/
		for (int i = 1; i <= recipients; i++) {

			String fileName = emailInfo.get(i);

			// stripping the angle brackets
			// to form the file path name
			fileName = fileName.substring(fileName.indexOf('<') + 1,
					fileName.length() - 1);
			
			//create a writer
			try (PrintWriter myPrinter = new PrintWriter(new BufferedWriter(
					new FileWriter("forward/" + fileName, true)))) {
				
				//write all email information in each recipients file
				for (int j = 0; j < emailInfo.size(); j++) {
					
					myPrinter.println(emailInfo.get(j));
					
				}
				
				//close writer
				myPrinter.close();
				
			} catch (IOException e) {
				
				e.getStackTrace();
				
			}
		}
	}

	
	/* method that take in a string array and checks if the domain token is
	 * properly formated
	 */
	private static boolean checkDomain(String[] domainArray) {
		boolean pass;

		// check if array is not null and has values
		if (domainArray != null && domainArray.length > 0) {

			// boolean for correct format
			pass = true;
			
			/* check if each part is made up of at least 2 characters. Must
			 * begin with a letter but can contain as many letters or digits
			 * after it
			 */
			for (int i = 0; i < domainArray.length; i++) {
				
				if (!domainArray[i].matches(DOMAINREGEX)) {

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
	
	/* helper method that clears my email Information arraylist
	 * and sets the current number of mail recipients to zero
	 */
	private static void resetEmail() {
		
		// empty arraylist with email information
		emailInfo.clear();
		
		// set recipients back zero
		recipients = 0;
	}

	// helper method that prints out results based code
	private static void displayResults(PrintResults code) {
		
		switch (code) {
		
		case OK:
			
			System.out.println("250 OK");
			
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