import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.*;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MIMEBuild {
	
	private static final String FROMMYSELF="From: <ssmith96@cs.unc.edu>";
	private static final String FILENAME = "./outgoing";
	private static final String FIRSTPARTTO = "To: <";
	private static final String SECPARTTO = "@cs.unc.edu>";
	private static final String SUBJECT="Subject: Extra Credit";
	private static final String MIMEVER = "MIME-Version: 1.0";
	private static final String CONTENTMIXED = "Content-Type: multipart/mixed; boundary=98766789";
	private static final String BOUNDARY = "--98766789";
	private static final String NEWLINE = "\n";
	private static final String TYPE="jpg";
	private static final String ENCODING="Content-Transfer-Encoding: base64";
	private static final String CONTENTTYPE="Content-Type: ";
	private static final String IMAGE="image/jpeg";
	private static final String TEXT="text/plain";
	private static final String ENDBOUNDARY ="--98766789--";

	public static void main(String[] args) {

		if (!(args.length == 2)&&args[0]!=null&&args[1]!=null) {
			System.out.println("wrong number of arguements");
			System.exit(1);
		}

		BufferedReader rcptBuffer = new BufferedReader(new InputStreamReader(
				System.in));

		String rcpt;
		try {
			PrintWriter myPrinter = new PrintWriter(new BufferedWriter(
					new FileWriter(FILENAME, true)));
			
			myPrinter.println(FROMMYSELF);
			
			while ((rcpt = rcptBuffer.readLine())!=null) {
				myPrinter.println(FIRSTPARTTO + rcpt + SECPARTTO);
			}
			rcptBuffer.close();
			
			myPrinter.println(SUBJECT);
			myPrinter.println(MIMEVER);
			myPrinter.println(CONTENTMIXED);
			myPrinter.println(NEWLINE);
			myPrinter.println(BOUNDARY);
			myPrinter.println(ENCODING);
			myPrinter.println(CONTENTTYPE+TEXT);
			myPrinter.println(NEWLINE);
			
			BufferedReader textFileBuffer = new BufferedReader(new FileReader(
					args[0]));
			String nextText;
			while((nextText=textFileBuffer.readLine())!=null){
				myPrinter.println(DatatypeConverter.printBase64Binary(nextText.getBytes()));
			}
			textFileBuffer.close();
			
			myPrinter.println(BOUNDARY);
			myPrinter.println(ENCODING);
			myPrinter.println(CONTENTTYPE+IMAGE);
			myPrinter.println(NEWLINE);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BufferedImage image = ImageIO.read(new File(args[1]));
			ImageIO.write(image, TYPE, outputStream);
			byte[] imgByteArray = outputStream.toByteArray();
			myPrinter.println(DatatypeConverter.printBase64Binary(imgByteArray));
			
			outputStream.close();
			myPrinter.println(ENDBOUNDARY);
			myPrinter.close();		
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
