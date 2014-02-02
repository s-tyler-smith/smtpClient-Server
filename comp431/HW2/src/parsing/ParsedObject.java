package parsing;

public class ParsedObject {

	private boolean successfulParse;
	private String infoString,messageString;

	public ParsedObject(boolean initSuccess, String initInfo) {
		if (initInfo != null) {
			infoString = initInfo;
			//messageString=initMessage;
		}
		successfulParse = initSuccess;

	}
	public String getMessageString() {
		return messageString;
	}

	public boolean getSuccessfulParse() {
		return successfulParse;
	}

	public String getInfoString() {
		return infoString;
	}

	public void setSuccessfulParse(boolean newBool) {
		successfulParse = newBool;
	}

	public void setInfoString(String newInfoString) {
		infoString = newInfoString;
	}
	public void setMessageString(String newMessageString) {
		infoString = newMessageString;
	}

}
