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
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		File data = new File("data");

		List<Article> articles = new Parser(data).parse().subList(0, 5000);

		//List<Article> articles = testArticles();

		System.out.println("Finished reading " + articles.size() + " articles in "	+ (System.currentTimeMillis() - start) + "ms\n");

		//SENTENCES OF FIRST ARTICLE
//		System.out.println(articles.get(0).phrases().stream().map(s -> s + "\n").collect(Collectors.toList()));
//		System.out.println(articles.get(0).body);
		System.out.println("starting suffix tree");
		suffixTree(articles);
		System.out.println("starting kmeans");
		kMeans(articles);

		//System.out.println(idf.keySet().stream().map(x -> x + "\n").collect(Collectors.toList()));

		//System.out.println(articles.get(0).bodyWords().stream().filter(x -> x.equals("cocoa")).count());
		//System.out.println(articles.get(0).bodyWords().size());
		//System.out.println(articles.stream().filter(x -> x.bodyWords().contains("cocoa")).count());

		//System.out.println(articles.get(0).tfidf.entrySet().stream().map(x -> x.getKey() + " : " + x.getValue()+ "\n").collect(Collectors.toList()));

		//System.out.println(Thesaurus.map.entrySet().stream().sorted((x,y) -> x.getValue().compareTo(y.getValue())).map(e -> e.getKey() + " -> " + e.getValue() + "\n").collect(Collectors.toList()));

		// MOST SIMILAR TO FIRST ARTICLE
//		System.out.println(articles
//				.subList(1, articles.size())
//				.stream()
//				.max((x,y) -> (int)(
//						(CosineSimilarity.of(articles.get(0).tfidf, x.tfidf) -
//						CosineSimilarity.of(articles.get(0).tfidf, y.tfidf)) * 1000000))
//				.get().body);

//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(0).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(1).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(2).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(3).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(4).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(5).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(6).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(7).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(8).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(9).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(10).tfidf));

		//KMEANS PURITIES
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
