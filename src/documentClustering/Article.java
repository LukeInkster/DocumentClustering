package documentClustering;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Article {

	public final String title;
	public final String body;
	public final String topics;
	public final Date date;

	private List<String> words;
	private Set<String> distinctWords;
	private Map<String, Integer> tf;
	private Map<String, Double> tfidf;

	public Article(String title, String body, String topics, Date date){
		this.title = title;
		this.body = body;
		this.topics = topics;
		this.date = date;
	}

	public int tf(String s){
		return tf().containsKey(s) ? tf().get(s) : 0;
	}

	public double tfidf(String s){
		if (this.tfidf == null) throw new RuntimeException("tfidf not initialised. Call tfidf(df) first");
		if (!this.tfidf.containsKey(s)) return 0;
		return this.tfidf.get(s);
	}

	public Map<String, Double> tfidf(Map<String, Double> df){
		if (tfidf != null) return tfidf;

		tfidf = new HashMap<String, Double>();
		tf.forEach((k, v) -> {
			tfidf.put(k, Math.log(((double)v)/df.get(k)));
		});

		return tfidf;
	}

	public Map<String, Integer> tf(){
		if (this.tf != null) return tf;

		tf = new HashMap<String, Integer>();
		for (String word : bodyWords()){
			if (tf.containsKey(word)){
				tf.put(word, tf.get(word) + 1);
			}
			else tf.put(word, 1);
		}

		distinctWords = new HashSet<String>();
		distinctWords.addAll(tf.keySet());

		return tf;
	}

	public double cosineSimilarityTo(Article other){
		return CosineSimilarity.of(this, other);
	}

	public List<String> bodyWords(){
		return words == null ? words = toWords(body) : words;
	}

	private static List<String> toWords(String s){
		return Arrays.asList(s.split("\\s+"));
	}

	public Set<String> distinctWords() {
		if (distinctWords == null) tf();
		return distinctWords;
	}
}
