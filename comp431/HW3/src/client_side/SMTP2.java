package client_side;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SMTP2 {

	/**
	 * @param args
	 * 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		BufferedReader myBuffer;
		String nextLine = null;
		myBuffer=new BufferedReader(new FileReader(args[0]));
			do {
				try {
					nextLine = myBuffer.readLine();
					System.out.println(nextLine);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
				
			} while (nextLine != null);
			myBuffer.close();

	}

}
