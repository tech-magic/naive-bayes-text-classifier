import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TrainingModule {

	private KnowledgeRepository repository;
	private FileParser parser;
	private NaiveBayesTextClassifierUI gui;

	private boolean abortTraining;

	public TrainingModule(KnowledgeRepository repository, FileParser parser, NaiveBayesTextClassifierUI gui) {
		this.repository = repository;
		this.parser = parser;
		this.gui = gui;

		this.abortTraining = false;
	}

	public void trainKnowledgebase(File ngDirectory) throws FileNotFoundException, IOException {

		this.gui.clearStatus();
		this.gui.addToStatus("=== Training Session Started ===");

		String[] subDirList = ngDirectory.list();
		if(subDirList != null) {

			int totalNoOfCategories = subDirList.length;
			this.gui.clearTrainingStats();

			for(int i = 0; i < totalNoOfCategories; i++) {
				File currDirectory = new File(ngDirectory.getAbsolutePath() + "/" + subDirList[i]);

				this.gui.updateTrainingStats(i, totalNoOfCategories - i, totalNoOfCategories);

				if(currDirectory.exists() && currDirectory.isDirectory()) {
					String className = subDirList[i];
					String[] subFileList = currDirectory.list();
					if(subFileList != null) {
						this.gui.addToStatus("");
						this.gui.addToStatus("=== Start of Training Documents for class " + className + " ===");

						for(int j = 0; j < subFileList.length; j++) {
							File currFile = new File(currDirectory.getAbsolutePath() + "/" + subFileList[j]);
							if(currFile.exists()) {
								this.addWords(className, this.parser.parseFile(currFile.getAbsolutePath()));
								this.gui.addToStatus("Class ->" + className);
								this.gui.addToStatus("File ->" + currFile.getAbsolutePath());
								this.gui.addToStatus("");
							}

							if(this.abortTraining) {
								this.gui.clearTrainingStats();
								this.gui.addToStatus("");
								this.gui.addToStatus("=== Training Session Aborted ===");
								return;
							}
						}

						this.gui.addToStatus("=== End of Training Documents for class " + className + " ===");
					}
				}
			}

			this.gui.clearTrainingStats();

		}

		this.gui.addToStatus("");
		this.gui.addToStatus("=== Training Session Ended Successfully ===");
	}

	//Add words from a hash to the word counts for a category
	private void addWords(String category, Map<String, Integer> wordCountMap) throws FileNotFoundException, IOException {

		Set<String> wordSet = wordCountMap.keySet();
		Iterator<String> wordIterator = wordSet.iterator();

		Map<String, Integer> currWordDatabase = this.repository.getWordDatabase();
		//this.displayMapInt("Current Word Database", currWordDatabase, null, new String[]{"<Class-Word>", "Occurance"});

		while(wordIterator.hasNext()) {
			String currWord = wordIterator.next();
			Integer currWordMapWordCount = wordCountMap.get(currWord);

			String currKey = category + CommonConstants.CLASS_WORD_DELIMITER + currWord;
			Integer currDatabaseWordCount = currWordDatabase.get(currKey);
			if(currDatabaseWordCount != null && currDatabaseWordCount.intValue() != 0) {
				//an entry for the current word in current category is already there in the word database
				Integer newWordCount = new Integer(currDatabaseWordCount.intValue() + currWordMapWordCount);
				currWordDatabase.remove(currKey);
				currWordDatabase.put(currKey, newWordCount);
			} else {
				currWordDatabase.put(currKey, currWordMapWordCount);
			}

		}

	    this.repository.overwriteWordDatabase(currWordDatabase);
	    //this.displayMapInt("Updated Word Database", currWordDatabase, null, new String[]{"<Class-Word>", "Occurance"});
	}

	public void abortTraining() {
		this.abortTraining = true;
	}

}
