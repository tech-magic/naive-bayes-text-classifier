
public class ClassificationResults implements Comparable<ClassificationResults> {
	String category;
	Double score;

	public ClassificationResults(String key, Double value) {
		this.category = key;
		this.score = value;
	}

	public int compareTo(ClassificationResults o) {
		return o.score.compareTo(this.score);
	}
}
