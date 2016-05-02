package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cleaning.Cleaner;
import kMeans.Cluster;
import kMeans.CosineSimilarity;
import kMeans.Tfidf;

public class Article {

	public final String title;
	public final String body;
	public final Date date;
	public final Set<String> topics;
	public final Set<String> places;
	public final Set<String> people;
	public final Set<String> orgs;
	public final Set<String> exchanges;

	public Tfidf tfidf;

	private List<String> words;
	private List<Word> suffixTreeWords;
	private List<Phrase> phrases;
	private Set<String> distinctWords;
	private Map<String, Integer> tf;

	public Article(){
		title = "";
		body = "";
		date = new Date();
		topics = new HashSet<String>();
		places = new HashSet<String>();
		people = new HashSet<String>();
		orgs = new HashSet<String>();
		exchanges = new HashSet<String>();
		words = new ArrayList<String>();
		suffixTreeWords = new ArrayList<Word>();
	}

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

	public List<Phrase> phrases(){
		return phrases == null ? phrases = phrases(body) : phrases;
	}

	public List<Word> toWordObjects(){
		if (suffixTreeWords == null){
			suffixTreeWords = phrases()
					.stream()
					.flatMap(p -> p.words.stream())
					.collect(Collectors.toList());
		}
		return suffixTreeWords;
	}

	public Word wordAt(int index){
		return toWordObjects().get(index);
	}

	public void addWord(Word word) {
		words.add(word.word);
		suffixTreeWords.add(word);
	}

	private List<Phrase> phrases(String s) {
		List<Phrase> phrases = Cleaner.cleanAndSplitToSentences(s)
				.stream()
				.map(list -> phrases(list))
				.collect(Collectors.toList());

		int startIndex = 0;
		for (Phrase p : phrases){
			p.startIndex = startIndex;
			p.endIndex = startIndex + p.size();
			startIndex += p.size();
		}

		return phrases;
	}

	private Phrase phrases(List<String> list) {
		return new Phrase(
				Stream.concat(list
					.stream()
					.map(word -> new Word(word, tfidf == null ? 0 : tfidf(word))),
					Stream.of(Phrase.endMarker()))
				.collect(Collectors.toList())
			);
	}

	private static List<String> toWords(String s){
		return Cleaner.cleanAndSplit(s);
	}

	public Set<String> distinctWords() {
		if (distinctWords == null) tf();
		return distinctWords;
	}

	public boolean contains(String word){
		return distinctWords.contains(word);
	}

	public boolean contains(Word word){
		return distinctWords.contains(word.word);
	}

	public int wordCount(){
		return bodyWords().size();
	}

	public double cosineSimilarityTo(Article other){
		return CosineSimilarity.of(this.tfidf, other.tfidf);
	}

	public double cosineSimilarityTo(Cluster cluster) {
		return CosineSimilarity.of(tfidf, cluster.tfidf());
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distinctWords == null) ? 0 : distinctWords.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Article other = (Article) obj;
		if (distinctWords == null) {
			if (other.distinctWords != null)
				return false;
		} else if (!distinctWords.equals(other.distinctWords))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
