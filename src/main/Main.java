package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kMeans.Cluster;
import kMeans.KMeans;
import suffixTreeClusterer.STCluster;
import suffixTreeClusterer.STClusterer;

public class Main {
	public static boolean clean = false;
	public static boolean printTree = true;
	public static boolean useDemoData = true;

	private static int maxArticles = 1000;

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		List<Article> articles = useDemoData ? testArticles() : realArticles();

		if (useDemoData){
			for (Article a : articles){
				System.out.println(a.body);
			}
		}

		System.out.println("Finished reading " + articles.size() + " articles in "	+ (System.currentTimeMillis() - start) + "ms\n");

		System.out.println("starting suffix tree");
		suffixTree(articles);
		System.out.println("starting kmeans");
		kMeans(articles);
	}

	private static void kMeans(List<Article> articles) {
		List<Cluster> clusters = KMeans.cluster(articles, 120);
		for (Cluster c : clusters.stream().sorted((x,y) -> (int)((x.purity() - y.purity())*10000)).collect(Collectors.toList())){
			System.out.println("size: " + c.articles.size() + " purity: " + c.purity());
		}
		System.out.println("weighted average purity: " + weightedPurity(clusters));
	}

	private static void suffixTree(List<Article> articles) {
		List<STCluster> stClusters = STClusterer.cluster(articles, 135);

		for (STCluster c : stClusters
				.stream()
				.sorted((x,y) -> (int)((x.purity() - y.purity()) * 10000))
				.collect(Collectors.toList())){
			System.out.println("size: " + c.articles.size() + " purity: " + c.purity());
		}
		System.out.println("weighted average purity: " + stWeightedPurity(stClusters));
	}

	private static List<Article> realArticles() {
		File data = new File("data");
		List<Article> articles = new Parser(data).parse();
		return articles.subList(0, Math.min(maxArticles, articles.size()));
	}

	private static List<Article> testArticles() {
		Date d = new Date();
		Set<String> topics1 = new HashSet<String>(Arrays.asList("cat", "cheese"));
		Set<String> topics2 = new HashSet<String>(Arrays.asList("mouse", "cheese"));
		Set<String> topics3 = new HashSet<String>(Arrays.asList("cat", "mouse"));
		Set<String> e = new HashSet<String>();
		Article article1 = new Article("title1", "cat ate cheese", d, topics1, e, e, e, e);
		Article article2 = new Article("title2", "mouse ate cheese too", d, topics2, e, e, e, e);
		Article article3 = new Article("title3", "cat ate mouse too", d, topics3, e, e, e, e);
		List<Article> articles = new ArrayList<Article>(Arrays.asList(article1, article2, article3));
		Parser.tfidfInit(articles);
		return articles;
	}

	private static double stWeightedPurity(List<STCluster> stClusters) {
		return stClusters.stream().mapToDouble(x -> x.purity() * x.articles.size()).sum()
				/ stClusters.stream().mapToInt(x -> x.articles.size()).sum();
	}

	private static double weightedPurity(List<Cluster> clusters){
		return clusters.stream().mapToDouble(x -> x.purity() * x.articles.size()).sum()
				/ clusters.stream().mapToInt(x -> x.articles.size()).sum();
	}
}
