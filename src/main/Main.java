package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kMeans.Cluster;
import kMeans.CosineSimilarity;
import kMeans.KMeans;
import suffixTreeClusterer.STCluster;
import suffixTreeClusterer.STKMeans;

public class Main {
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		List<Article> articles = new Parser(new File("data")).parse().subList(0, 500);

//		List<Article> articles = testArticles();

		System.out.println("Finished reading " + articles.size() + " articles in "	+ (System.currentTimeMillis() - start) + "ms\n");

		//SENTENCES OF FIRST ARTICLE
		System.out.println(articles.get(0).phrases().stream().map(s -> s + "\n").collect(Collectors.toList()));
		System.out.println(articles.get(0).body);

		System.out.println(articles.size());
		List<STCluster> stClusters = STKMeans.cluster(articles, 135);

		//SuffixTreeClusterer.articleSet.tree.root.print();
		//System.out.println(SuffixTreeClusterer.articleSet.tree.root.edgeWords());
		for (STCluster c : stClusters
				.stream()
				.sorted((x,y) -> (int)((x.purity() - y.purity()) * 10000))
				.collect(Collectors.toList())){
			System.out.println("size: " + c.articles.size() + " purity: " + c.purity());
		}
		System.out.println("average purity: " + stClusters.stream().mapToDouble(x -> x.purity()).average().getAsDouble());
		System.out.println("weighted average purity: " + stWeightedPurity(stClusters));

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

		System.out.println("Unique topics: " + articles.stream()
			.flatMap(x -> x.topics.stream()).distinct().collect(Collectors.toList()).size());
		System.out.println("Unique words: " + articles.stream()
			.flatMap(x -> x.distinctWords().stream()).distinct().collect(Collectors.toList()).size());

		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(0).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(1).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(2).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(3).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(4).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(5).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(6).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(7).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(8).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(9).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(10).tfidf));

		//KMEANS PURITIES
		List<Cluster> clusters = KMeans.cluster(articles, 120);
		for (Cluster c : clusters.stream().sorted((x,y) -> (int)((x.purity() - y.purity())*10000)).collect(Collectors.toList())){
			System.out.println("size: " + c.articles.size() + " purity: " + c.purity());
		}
		System.out.println("average purity: " + clusters.stream().mapToDouble(x -> x.purity()).average().getAsDouble());
		System.out.println("weighted average purity: " + weightedPurity(clusters));
	}

	private static List<Article> testArticles() {
		Date d = new Date();
		Set<String> topics = new HashSet<String>();
		topics.add("cats");
		Set<String> e = new HashSet<String>();
		Article article1 = new Article("title1", "cat ate cheese", d, topics, e, e, e, e);
		Article article2 = new Article("title2", "mouse ate cheese too", d, topics, e, e, e, e);
		Article article3 = new Article("title3", "cat ate mouse too", d, topics, e, e, e, e);
		return new ArrayList<Article>(Arrays.asList(article1, article2, article3));
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
