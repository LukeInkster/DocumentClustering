package main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import clustering.Cluster;
import clustering.CosineSimilarity;
import clustering.KMeans;

public class Main {
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		File data = new File("data");
		List<Article> articles = new Parser(data).parse().subList(0, 5000);

		System.out.println("Finished reading " + articles.size() + " articles in "
				+ (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();

		Map<String, Double> idf = idf(articles);

		System.out.println("Finished calculating term frequencies in "
				+ (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();

		for (Article a:articles){
			a.tfidf(idf);
		}
		//System.out.println(idf.keySet().stream().map(x -> x + "\n").collect(Collectors.toList()));

		//System.out.println(articles.get(0).bodyWords().stream().filter(x -> x.equals("cocoa")).count());
		//System.out.println(articles.get(0).bodyWords().size());
		//System.out.println(articles.stream().filter(x -> x.bodyWords().contains("cocoa")).count());

		System.out.println("Finished calculating tfidf values in "
				+ (System.currentTimeMillis() - start) + "ms");

		//System.out.println(articles.get(0).tfidf.entrySet().stream().map(x -> x.getKey() + " : " + x.getValue()+ "\n").collect(Collectors.toList()));

		//System.out.println(Thesaurus.map.entrySet().stream().sorted((x,y) -> x.getValue().compareTo(y.getValue())).map(e -> e.getKey() + " -> " + e.getValue() + "\n").collect(Collectors.toList()));

		System.out.println(articles
				.subList(1, articles.size())
				.stream()
				.max((x,y) -> (int)(
						(CosineSimilarity.of(articles.get(0).tfidf, x.tfidf) -
						CosineSimilarity.of(articles.get(0).tfidf, y.tfidf)) * 1000000))
				.get().body);

		System.out.println(articles.stream().flatMap(x -> x.topics.stream()).distinct().collect(Collectors.toList()).size());
		System.out.println(articles.stream().flatMap(x -> x.distinctWords().stream()).distinct().collect(Collectors.toList()).size());

		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(0).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(1).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(2).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(3).tfidf));
		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(4).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(5).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(6).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(7).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(8).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(9).tfidf));
//		System.out.println(CosineSimilarity.of(articles.get(0).tfidf, articles.get(10).tfidf));

		List<Cluster> clusters = KMeans.cluster(articles, 120);
		for (Cluster c : clusters.stream().sorted((x,y) -> (int)((x.purity() - y.purity())*10000)).collect(Collectors.toList())){
			System.out.println("size: " + c.articles.size() + " purity: " + c.purity() + " topic:" + c.mostCommonTopic());
		}
		System.out.println("average purity: " + clusters.stream().mapToDouble(x -> x.purity()).average().getAsDouble());
		System.out.println("weighted average purity: " + weightedPurity(clusters));
	}

	private static double weightedPurity(List<Cluster> clusters){
		return clusters.stream().mapToDouble(x -> x.purity() * x.articles.size()).sum()
				/ clusters.stream().mapToInt(x -> x.articles.size()).sum();
	}

	private static Map<String, Double> idf(List<Article> articles) {
		Map<String, Double> df = new HashMap<String, Double>();

		articles
			.stream()
			.map(a -> a.tf())
			.forEach(tf -> {
				tf.forEach((k, v) -> {
					if (df.containsKey(k)) df.put(k, df.get(k) + 1.0);
					else df.put(k, 1.0);
				});
			});

		df.forEach((k,v) -> df.put(k, Math.log(((double)articles.size())/v)));

		return df;
	}
}
