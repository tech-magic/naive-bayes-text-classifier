import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class FileParser {

	private String getRealWord(String rawString) {
		while(rawString.length() > 0) {
			if(!isAlphaNumeric(rawString.charAt(0))) {
				rawString = rawString.substring(1);
			} else {
				break;
			}
		}

		while(rawString.length() > 0) {
			if(!isAlphaNumeric(rawString.charAt(rawString.length() - 1))) {
				rawString = rawString.substring(0, rawString.length() - 1);
			} else {
				break;
			}
		}
		return rawString;
	}

	private boolean isAlphaNumeric(char character) {
		int characterAsInt = character;
		if(characterAsInt >= 65 && characterAsInt <= 90) {
			return true;
		} else if(characterAsInt  >= 97 && characterAsInt <= 122) {
			return true;
		} else if (characterAsInt  >= 48 && characterAsInt <= 57) {
			return true;
		} else {
			return false;
		}
	}

	//Read a file and return a hash of the word counts in that file
	public Map<String, Integer> parseFile(String fileName) throws FileNotFoundException, IOException {

		BufferedReader inputBuffer = new BufferedReader(new FileReader(fileName));
		Map<String, Integer> wordCountMap = new HashMap<String, Integer>();

	    //Grab all the words with their word count
		boolean eof = false;
		String currWord = null;
		String currLine = null;

	    while ( !eof ) {

	    	currLine = inputBuffer.readLine();

	    	if(currLine == null) {
	    		eof = true;
	    	} else {
	    		StringTokenizer tokenizer = new StringTokenizer(currLine);
	    		int index = 0;
	    		while(tokenizer.hasMoreTokens()) {
	    			currWord = tokenizer.nextToken().trim().toLowerCase();
	    			if(currWord.length() > 0 && currWord.length() < 50) {
	    				if(index == 0 && currWord.endsWith(":")) {
	    					break;
	    				} else {
	    					index++;

	    					currWord = getRealWord(currWord);
	    					if(currWord.length() > 0) {
	    						Integer currWordCount = wordCountMap.get(currWord);
	    						if(currWordCount != null && currWordCount.intValue() != 0) {
	    							//an entry for the current word is already there in the hash map
	    							Integer newWordCount = new Integer(currWordCount.intValue() + 1);
	    							wordCountMap.remove(currWord);
	    							wordCountMap.put(currWord, newWordCount);
	    						} else {
	    							wordCountMap.put(currWord, new Integer(1));
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}

	    }

	    inputBuffer.close();

	    return wordCountMap;
	}

}
