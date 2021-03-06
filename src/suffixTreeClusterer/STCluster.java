package suffixTreeClusterer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Article;
import main.Phrase;
import main.Purity;

public final class STCluster {
    public Set<Article> articles;
	public List<Phrase> phrases;
	public double weight;
	String label;

	public STCluster(int articleCapacity, int phraseCapacity) {
		articles = new HashSet<Article>(articleCapacity);
		phrases = new ArrayList<Phrase>(phraseCapacity);
	}

	public STCluster(Phrase phrase) {
		this(4, 1);
		phrases.add(phrase);
	}

	/**
	 * Cluster weight is the product of:
	 * - the number of articles
	 * - the length of sentences
	 * - the sum of the weight of each word's part of the sentences.
	 */
	public double weight() {
		double wordWeight = phrases.stream().mapToDouble(p -> p.weight()).sum();
		return weight = articles.size() * phrasesWeight() * wordWeight;
	}

	private double phrasesWeight() {
		double sum = phrases.stream().mapToDouble(p -> p.words.size()).sum();
		return sum < 2 ? 0.5 : Math.min(6, sum);
	}

	/**
	 * The similarity of two clusters is the average proportion of each cluster's articles
	 * which are also found in the other cluster
	 */
	public double similarityTo(STCluster other) {
		Set<Article> articleSet = new HashSet<Article>(articles);
		double sharedArticles = other.articles.stream().filter(articleSet::contains).count();

		return (sharedArticles / (double) articles.size()
				+ sharedArticles / (double) other.articles.size()) / 2.0 ;
	}

	public static STCluster merge(Set<STCluster> clusters) {
		STCluster newCluster = new STCluster(clusters.size() * 2, clusters.size());
		Set<Article> allArticles = new HashSet<>();

		for (STCluster c : clusters) {
			allArticles.addAll(c.articles);
			newCluster.phrases.addAll(c.phrases);
		}

		newCluster.articles.addAll(allArticles);
		return newCluster;
	}

	public static STCluster merge(List<STCluster> clusters) {
		Set<STCluster> clusterSet = new HashSet<>();
		clusterSet.addAll(clusters);
		return merge(clusterSet);
	}

	public static STCluster merge(STCluster a, STCluster b) {
		Set<STCluster> clusterSet = new HashSet<>();
		clusterSet.add(a);
		clusterSet.add(b);
		return merge(clusterSet);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Cluster: " + label);
		sb.append(", Weight: " + weight);
		sb.append(", Number docs: " + articles.size());
		for (Phrase p : phrases) sb.append(p.toString());
		return sb.toString();
	}

	public double purity(){
		return Purity.of(articles);
	}
}
