package main;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cleaning.Cleaner;
import clustering.Cluster;
import clustering.CosineSimilarity;
import clustering.Tfidf;

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

	public Tfidf tfidf(Map<String, Double> idf){
		if (tfidf != null) return tfidf;

		tfidf = new Tfidf(tf.size());
		tf.forEach((k, v) -> {
			if (v == 0) tfidf.put(k, 0);
			else tfidf.put(k, (((double)v)/bodyWords().size()) * idf.get(k));
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
		return Cleaner.cleanAndSplit(s);
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
