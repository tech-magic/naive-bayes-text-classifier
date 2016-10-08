import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class KnowledgeRepository {

	// Word Database is given as a Map
	// The map keys are strings of the form category-word:
	//For example, if the word "potato" appears in the category "veggies" with a count of three,
	//there will be a hash entry with category "veggies-potato" and score "3"
	public Map<String, Integer> getWordDatabase() throws FileNotFoundException, IOException {

		Map<String, Integer> wordDatabase = new HashMap<String, Integer>();

		File wordDatabaseFile = new File(CommonConstants.WORD_DATABASE_FILE);
		if(!wordDatabaseFile.exists()) {
			wordDatabaseFile.createNewFile();
			return wordDatabase;
		}

		BufferedReader inputBuffer = new BufferedReader(new FileReader(wordDatabaseFile));

		boolean eof = false;
		String currKey = null;
		int currCount = 0;
		String currLine = null;

	    while ( !eof ) {

	    	currLine = inputBuffer.readLine();

	    	if(currLine == null) {
	    		eof = true;
	    	} else {
	    		try {
					String[] keyValuePairs = currLine.split(CommonConstants.KEY_VALUE_DELIMITER);
					if (keyValuePairs != null && keyValuePairs.length == 2) {
						currKey = keyValuePairs[0];
						currCount = Integer.parseInt(keyValuePairs[1]);
						wordDatabase.put(currKey, new Integer(currCount));
					} else {
						System.out.println("=========Invalid Entry ======== ->" + currLine);
					}
				} catch (Exception ex) {
					System.out.println("=========Invalid Entry with Exception ======== ->" + currLine);
				}
	    	}

	    }

	    inputBuffer.close();

	    return wordDatabase;
	}

	//Overwrites the word database
	//The map keys are strings of the form category-word:
	//For example, if the word "potato" appears in the category "veggies" with a count of three,
	//there will be a hash entry with category "veggies-potato" and score "3"
	public void overwriteWordDatabase(Map<String, Integer> databaseEntries) throws IOException {
		BufferedWriter outputBuffer = new BufferedWriter(new FileWriter(CommonConstants.WORD_DATABASE_FILE));

		Set<String> keySet = databaseEntries.keySet();
		Iterator<String> keyIterator = keySet.iterator();

		while(keyIterator.hasNext()) {
			String currKey = keyIterator.next();
			int currCount = databaseEntries.get(currKey).intValue();
			String currLine = currKey + CommonConstants.KEY_VALUE_DELIMITER + Integer.toString(currCount) + "\r\n";
			outputBuffer.write(currLine);
		}

		outputBuffer.flush();
		outputBuffer.close();

	}

}
