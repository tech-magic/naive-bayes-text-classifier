import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class ClassificationModule {

	private KnowledgeRepository repository;
	private FileParser parser;
	private NaiveBayesTextClassifierUI gui;

	public ClassificationModule(KnowledgeRepository repository, FileParser parser, NaiveBayesTextClassifierUI gui) {
		this.repository = repository;
		this.parser = parser;
		this.gui = gui;
	}

	public void categorize(File inputFile) throws FileNotFoundException, IOException {
		List<ClassificationResults> classificationResultsList = this.classify(this.parser.parseFile(inputFile.getAbsolutePath()));

		this.gui.clearStatus();
		this.gui.addToStatus("=== Starting Classification ===");
		this.gui.addToStatus("Input File -> " + inputFile.getAbsolutePath());
		this.gui.addToStatus("");

		for(int i = 0; i < classificationResultsList.size(); i++) {
			ClassificationResults currentResult = classificationResultsList.get(i);
			this.gui.addToStatus("Category ->" + currentResult.category + " | Score ->" + currentResult.score.toString());
		}

		if(classificationResultsList.size() > 0) {
			ClassificationResults answer = classificationResultsList.get(0);
			this.gui.addToStatus("");
			this.gui.addToStatus("Most probable candidate category is -> " + answer.category +
				" (with a score of " + Double.toString(answer.score) + ")");
		}

		this.gui.addToStatus("");
		this.gui.addToStatus("=== Ending Classification ===");
	}

	//Get the classification of the file from its word counts given by wordCountMap
	private List<ClassificationResults> classify(Map<String, Integer> wordCountMap) throws FileNotFoundException, IOException {

		//Step 1
	    //Calculate the total number of words in each category and
	    //the total number of all words for all the categories overall in our word database
	    Map<String, Integer> categoryWordCountMap = new HashMap<String, Integer>();
	    Map<String, Integer> wordDatabase = this.repository.getWordDatabase();
	    int totalWordsInAllCategories = 0;
	    Set<String> databaseKeySet = wordDatabase.keySet();
	    Iterator<String> databaseKeyIterator = databaseKeySet.iterator();

	    while(databaseKeyIterator.hasNext()) {
	    	String currDatabaseKey = databaseKeyIterator.next();
	    	int currWordCount = wordDatabase.get(currDatabaseKey).intValue();
	    	String currCategory = "";

	    	StringTokenizer tokenizer = new StringTokenizer(currDatabaseKey, CommonConstants.CLASS_WORD_DELIMITER);
	    	int currIndex = 0;
	    	while(tokenizer.hasMoreTokens()) {
	    		if(currIndex == 0) {
	    			currCategory = tokenizer.nextToken();
	    		} else {
	    			tokenizer.nextToken();
	    		}
	    		currIndex++;
	    	}

	    	if(currCategory.trim().length() != 0) {
	    		Integer categoryWordCount = categoryWordCountMap.get(currCategory);
	    		if(categoryWordCount != null && categoryWordCount.intValue() != 0) {
					Integer newWordCount = new Integer(categoryWordCount.intValue() + currWordCount);
					categoryWordCountMap.remove(currCategory);
					categoryWordCountMap.put(currCategory, newWordCount);
	    		} else {
	    			categoryWordCountMap.put(currCategory, new Integer(currWordCount));
	    		}
	    	}

	    	totalWordsInAllCategories += currWordCount;
	    }
	    //this.displayMapInt("Classification : Ending Step 1", categoryWordCountMap, "Total # of words in all categories is " + totalWordsInAllCategories, new String[]{"Category", "# of Total Words per Category"});

	    //Step 2
	    //Run through words in the file supplied and calculate the probability for each category
	    Map<String, Double> categoryScoreMap = new HashMap<String, Double>();

	    Set<String> wordSetFromFile = wordCountMap.keySet();
	    Iterator<String> wordIteratorFromFile = wordSetFromFile.iterator();
	    while(wordIteratorFromFile.hasNext()) {

	    	String currWord = wordIteratorFromFile.next();
	    	Set<String> categories = categoryWordCountMap.keySet();
	    	Iterator<String> categoryIterator = categories.iterator();

	    	while(categoryIterator.hasNext()) {
	    		String currCategory = categoryIterator.next();
	    		Integer totalWordsInCategory = categoryWordCountMap.get(currCategory);

	    		String currKey = currCategory + CommonConstants.CLASS_WORD_DELIMITER + currWord;
	    		Integer currWordCount = wordDatabase.get(currKey);

	    		if(currWordCount != null && currWordCount.intValue() != 0) {
	    			this.addToMap(categoryScoreMap, currCategory, currWordCount, totalWordsInCategory);
	    		} else {
	    			this.addToMap(categoryScoreMap, currCategory, new Integer(0), totalWordsInCategory);
	    		}
	    	}

	    }

	    //Step 3
	    //Add in the probability that the text is of a specific category
	    Set<String> categories = categoryWordCountMap.keySet();
    	Iterator<String> categoryIterator = categories.iterator();
    	while(categoryIterator.hasNext()) {
    		String currCategory = categoryIterator.next();
    		Integer totalWordsInCategory = categoryWordCountMap.get(currCategory);
    		this.addToMap(categoryScoreMap, currCategory, totalWordsInCategory, new Integer(totalWordsInAllCategories));
    	}

    	//Step 4
    	//Judge the category by considering the category having highest score
    	Iterator<String> categoryIterator2 = categories.iterator();
    	while(categoryIterator2.hasNext()) {
    		String currCategory = categoryIterator2.next();
    		Double categoryScore = categoryScoreMap.get(currCategory);

    		//System.out.println("[Category :" + currCategory + "] [Score :" + categoryScore.doubleValue() + "]");
    	}

    	//Step 5 - Sort the categories descending by their respective scores
    	return this.getSortedClassifiedList(categoryScoreMap);
    }

	private List<ClassificationResults> getSortedClassifiedList(Map<String, Double> classifiedMap) {
		List<ClassificationResults> classificationResultsList = new LinkedList<ClassificationResults>();
		for(String key : classifiedMap.keySet()){
		    Double val = classifiedMap.get(key);
		    classificationResultsList.add(new ClassificationResults(key, val));
		}
		Collections.sort(classificationResultsList);

		return classificationResultsList;
	}

	private void addToMap(Map<String, Double> categoryScoreMap, String category, Integer logUpper, Integer logLower) {

		double logValue;
		if(logUpper.intValue() == 0) {
			logValue = CommonConstants.ASSUMED_PROBABILITY / (logLower.intValue() * 1.0);
		} else {
			logValue = (logUpper.intValue() * 1.0) / (logLower.intValue() * 1.0);
		}
		logValue = Math.log10(logValue);
		Double additionalValue = new Double(logValue);

		Double categoryScore = categoryScoreMap.get(category);
		if(categoryScore != null && categoryScore.doubleValue() != 0.0) {
			Double newValue = new Double(categoryScore.doubleValue() + additionalValue.doubleValue());
			categoryScoreMap.remove(category);
			categoryScoreMap.put(category, newValue);
		} else {
			categoryScoreMap.put(category, additionalValue);
		}
	}
}
