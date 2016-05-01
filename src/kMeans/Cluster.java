package clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import main.Article;

public class Cluster {
	public final List<Article> articles;
	private Tfidf tfidf;

	public Cluster(){
		this.articles = new ArrayList<Article>();
	}

	public Cluster(Article article){
		this.articles = Arrays.asList(article);
	}

	public Cluster(List<Article> articles){
		this.articles = articles;
	}

	public void add(Article a){
		articles.add(a);
	}

	public Tfidf tfidf(){
		if (tfidf != null) return tfidf;
		if (articles.size() == 1) return tfidf = articles.get(0).tfidf;
		Set<String> allWords = allWords();
		tfidf = new Tfidf(allWords.size());
		for (String word : allWords){
			double avgTfidf = articles
					.stream()
					.mapToDouble(a -> a.tfidf(word))
					.average()
					.getAsDouble();

			if (avgTfidf > 0.0001) tfidf.put(word, avgTfidf);
		}
		//System.out.println(tfidf.entrySet().size());
		return tfidf;
	}

	private Set<String> allWords() {
		Set<String> words = new HashSet<String>();
		for (Article a : articles){
			words.addAll(a.distinctWords());
		}
		return words;
	}

	public Optional<String> mostCommonTopic(){
		List<String> allTopics = articles
			.stream()
			.flatMap(a -> a.topics.stream())
			.collect(Collectors.toList());

		return allTopics
			.stream()
			.max((x,y) ->
				Collections.frequency(allTopics, x) -
				Collections.frequency(allTopics, y));
	}

	/**
	 * @return The proportion of articles that contain the most common topic in the cluster
	 */
	public double purity(){
		Optional<String> mostCommon = mostCommonTopic();

		if (!mostCommon.isPresent()) return 1;

		return (double)articles.stream().filter(a -> a.topics.contains(mostCommon.get())).count() /
				(double)articles.size();
	}
}
