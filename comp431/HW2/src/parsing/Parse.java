package parsing;

public class Parse {

	private boolean dataSeen=false;
	/*main parse method that checks from left to right
	 * the correctness of the mail from command.
	 */
	public void parseInput(String input,String command) {
			
			//handles if first input is null
			if(input==null){
				return;
			}
			//send to appropriate method based
			//on type of command
			if(command=="DATA"){
				parseData(input);
			}else{
				parseCommand(input,command);
			}
			
	}
	
	private void parseData(String data){
		
	}
	
	private void parseCommand(String input,String command){
		//print input first
		System.out.println(input);
		
		//trim whitespace from beginning and end of input
		input=input.trim();
		
		//split on first colon 
		String[] splitInput = input.split(":",2);
		
		/*when split on ':' there should only be two parts
		* the 'mail from' and the 'path'
		*/
		if(splitInput.length == 2){
			
			/*check if mail from is correct a bunch of talk has gone
			 * into whether or not it should be case insensitive
			 * but the grammar uses uppercase so that's what I used
			 */
			System.out.println(splitInput[0]);
			if (splitInput[0].equals(command)) {
				//trim white space since grammar is ambiguous 
				String path=splitInput[1].trim();
				
				//check for angle brackets are at the beginning and end
				if(path.charAt(0)=='<' && path.charAt(path.length()-1)=='>'){
					
					//split on @ sign to break up local-part and domain
					splitInput=path.split("@");
					
					//length should equal two
					if(splitInput.length==2){
						
						//strip off angle bracket from local-part
						String pathPart=splitInput[0].substring(1);
						
						//make local part doesn't contain non-ASCII,special, or space characters
						if(pathPart.matches("[^<>()\\.,\\\\:@;\\s\\[\\]\"]+")&&pathPart.matches("[\\p{ASCII}]*")){
							
							//reset pathPart to be domain with ending angle bracket stripped
							pathPart=splitInput[1].substring(0, splitInput[1].length()-1);
							
							//split up domain on periods
							String[] domain=pathPart.split("\\.");
							
							//check if there at least something a two character long domain
							if(domain[0].trim().length()>=2){
								
								//method for looping through domain parts
								if(checkDomain(domain)){
									displayResults(0);
								}else{
									displayResults(5);
								}	
							}else{
								displayResults(5);
							}
						}else{
							displayResults(4);
						}
					}else{
						displayResults(3);
					}
				}else{
					displayResults(2);
				}	
			}else{
				displayResults(1);	
			}
		}else{
			displayResults(1);
		}
	}
	
	/*method that take in a string array and checks if the
	 *domain token is properly formated
	 */
	private static boolean checkDomain(String[] domainArray){
		boolean pass;
		
		//check if array is not null and has values
		if(domainArray!=null && domainArray.length>0){
			
			//boolean for correct format
			pass=true;
			
			/*check if each part is made up of at least
			 * 2 characters. Must begin with a letter but can contain
			 * as many letters or digits after it
			 */
			for (int i = 0; i < domainArray.length; i++) {
				if(!domainArray[i].matches("[a-zA-Z][a-zA-Z\\d]+")){
					
					//wrong order or wrong type of characters so it fails
					pass=false;
					
					//at least one part of domain failed so break out of loop
					break;
				}
			}
			
		//array was null or empty
		}else{
			pass=false;
		}
		return pass;
	}
	
	//helper method that prints out results based code
	private static void displayResults(int tokenCode){
		if(tokenCode<0){
			return;
		}
		switch(tokenCode){
			case 0:
				System.out.println("250 ok");
				break;
			case 1:
				System.out.println("ERROR -- mail-from-cmd");
				break;
			case 2:
				System.out.println("ERROR -- path");
				break;
			case 3:
				System.out.println("ERROR -- mailbox");
				break;
			case 4:
				System.out.println("ERROR -- local-part");
				break;
			case 5:
				System.out.println("ERROR -- domain");
				break;		
		}
	}
}

