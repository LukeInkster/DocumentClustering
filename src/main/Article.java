package main;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Article {

	public final String title;
	public final String body;
	public final String topics;
	public final Date date;

	private List<String> words;
	private Map<String, Integer> tf;

	public Article(String title, String body, String topics, Date date){
		this.title = title;
		this.body = body;
		this.topics = topics;
		this.date = date;
	}

	public Map<String, Double> tfidf(Map<String, Integer> df){
		Map<String, Double> tfidf = new HashMap<String, Double>();
		tf.forEach((k, v) -> {
			tfidf.put(k, ((double)v)/((double)df.get(k)));
		});
		return tfidf;
	}

	public Map<String, Integer> tf(){
		if (this.tf != null) return this.tf;

		Map<String, Integer> tf = new HashMap<String, Integer>();
		for (String word : bodyWords()){
			if (tf.containsKey(word)){
				tf.put(word, tf.get(word) + 1);
			}
			else tf.put(word, 1);
		}

		return this.tf = tf;
	}

	public List<String> bodyWords(){
		return words == null ? words = toWords(body) : words;
	}

	private static List<String> toWords(String s){
		return Arrays.asList(s.split("\\s+"));
	}

}
