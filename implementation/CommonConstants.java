
public class CommonConstants {

	/* The knowledge repository */
	public static final String WORD_DATABASE_FILE = "words.db";

	/* Used to store entries in the knowledge base */
	public static final String KEY_VALUE_DELIMITER = "qpj";
	public static final String CLASS_WORD_DELIMITER = "qjb";

	/* Is used when a word is found new to a given category during classification */
	public static final double ASSUMED_PROBABILITY = 0.01;

	/* UI settings for the status console */
	public static final int SCREEN_LINES_UPPER_BOUND = 200;
	public static final int SCREEN_LINES_LOWER_BOUND = 150;
	public static final String TRAIN_BUTTON_CAPTION_TRAIN = "Train";
	public static final String TRAIN_BUTTON_CAPTION_ABORT = "Abort";
	public static final String CLASSIFY_BUTTON_CAPTION_CLASSIFY = "Classify";
}
