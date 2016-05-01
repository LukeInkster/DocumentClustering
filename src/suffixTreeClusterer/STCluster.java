package suffixTreeClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import main.Article;
import main.Phrase;

public final class STCluster implements Comparable<STCluster> {
	public List<Article> articles;
	public List<Phrase> phrases;
	public double weight;
	String label;

	public STCluster(int articleCapacity, int phraseCapacity) {
		articles = new ArrayList<Article>(articleCapacity);
		phrases = new ArrayList<Phrase>(phraseCapacity);
	}

	public STCluster(Phrase phrase) {
		this(4, 1);
		phrases.add(phrase);
	}

	// Computes the weight of the clusters based on the contained articles.
	public double computeWeight() {
		// The weight is equal to the product between the number of articles, the (adjusted) length of
		// the sentences and the sum of the weight of each word's part of the sentences.
		double wordWeight = phrases.stream().mapToDouble(p -> p.weight()).sum();
		return weight = articles.size() * phrasesWeight() * wordWeight;
	}

	/*
	 * Returns the 'distance' between this cluster and another. The distance can
	 * be thought of as the average similarity of the articles in the two clusters.
	 * i.e. a measure of how overlapping the clusters are. If the two clusters are exactly
	 * identical then the similarity would be 1. If there is no overlap then the distance would be 0.
	 */
//	public double similarity(STCluster other) {
//		Set<Article> articleSet = new HashSet<Article>(articles);
//
//		// Check which of the articles from the other cluster are found in this cluster.
//		double common = other.articles.stream().filter(articleSet::contains).count();
//
//		double dist_forward = common / (double) articles.size();
//		double dist_backward = common / (double) other.articles.size();
//
//		// Return the average distance between these two clusters.
//		return (dist_forward + dist_backward) / 2.0 ;
//	}

	// Joins clusters from the set into one cluster containing the union of the articles
	public static STCluster merge(Set<STCluster> clusters) {
		STCluster newCluster = new STCluster(clusters.size() * 2, clusters.size());
		Set<Article> allArticles = new HashSet<>();

		// Each article must appear a single time in the new cluster, as must
		// each Phrase in each original cluster.
		for (STCluster c : clusters) {
			allArticles.addAll(c.articles);
			newCluster.phrases.addAll(c.phrases);
		}

		// Add the articles to the new cluster.
		newCluster.articles.addAll(allArticles);
		return newCluster;
	}

	public static STCluster merge(List<STCluster> clusters) {
		Set<STCluster> clusterSet = new HashSet<>();
		clusterSet.addAll(clusters);
		return merge(clusterSet);
	}

	private double phrasesWeight() {
		double sum = phrases.stream().mapToDouble(p -> p.words.size()).sum();
		return sum < 2 ? 0.5 : Math.min(6, sum);
	}

	public int compareTo(STCluster other) {
		return other == this ? 0 : ((int) other.weight - (int) weight);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster: " + label);
		sb.append("; Weight: " + weight);
		sb.append("; Number docs: " + articles.size());
		for (Phrase p : phrases) sb.append(p.toString());
		return sb.toString();
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
