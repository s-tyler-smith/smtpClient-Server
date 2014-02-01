package parsing;

public class ParsedObject {

	public boolean successfulParse;
	public String infoString;

	public ParsedObject(boolean initSuccess, String initInfo) {
		if (initInfo != null) {
			infoString = initInfo;
		}
		successfulParse = initSuccess;

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

}
