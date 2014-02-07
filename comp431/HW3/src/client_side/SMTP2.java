package client_side;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SMTP2 {

	private static ProtocolState currentState;

	// enum for maintaining state
	private enum ProtocolState {
		MAILFROMSTATE, RCPT_TOSTATE, DATA
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

			try {

				nextFileLine = fileBuffer.readLine();

				processFileInput(nextFileLine);

				if (!(currentState == ProtocolState.DATA)) {
					nextResponseLine = responseBuffer.readLine();

					processServerResponse(nextResponseLine);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

		} while (nextFileLine != null && nextResponseLine != null);
		fileBuffer.close();

	}

	private static void processFileInput(String line) {
		System.out.println(line);

	}

	private static void processServerResponse(String response) {
		System.out.println(response);
	}

}
