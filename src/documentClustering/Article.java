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
	public final Date date;
	public final Set<String> topics;
	public final Set<String> places;
	public final Set<String> people;
	public final Set<String> orgs;
	public final Set<String> exchanges;

	private List<String> words;
	private Set<String> distinctWords;
	private Map<String, Integer> tf;
	public Tfidf tfidf;

	public Article(String title, String body, Date date, Set<String> topics,
			Set<String> places, Set<String> people, Set<String> orgs, Set<String> exchanges){
		this.title = title;
		this.body = body;
		this.date = date;
		this.topics = topics;
		this.places = places;
		this.people = people;
		this.orgs = orgs;
		this.exchanges = exchanges;
	}

	public int tf(String s){
		return tf().containsKey(s) ? tf().get(s) : 0;
	}

	public double tfidf(String s){
		return tfidf.get(s);
	}

	public Tfidf tfidf(Map<String, Double> df){
		if (tfidf != null) return tfidf;

		tfidf = new Tfidf();
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

	public double cosineSimilarityTo(Article other){
		return CosineSimilarity.of(this.tfidf, other.tfidf);
	}

	public double cosineSimilarityTo(Cluster cluster) {
		return CosineSimilarity.of(tfidf, cluster.tfidf());
	}
}
