package documentClustering;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		System.out.println(idf.keySet().stream().map(x -> x + "\n").collect(Collectors.toList()));

		System.out.println(articles.get(0).bodyWords().stream().filter(x -> x.equals("cocoa")).count());
		System.out.println(articles.get(0).bodyWords().size());
		System.out.println(articles.stream().filter(x -> x.bodyWords().contains("cocoa")).count());

		System.out.println("Finished calculating tfidf values in "
				+ (System.currentTimeMillis() - start) + "ms");

		System.out.println(articles.get(0).tfidf.entrySet().stream().map(x -> x.getKey() + " : " + x.getValue()+ "\n").collect(Collectors.toList()));

		//System.out.println(Thesaurus.map.entrySet().stream().sorted((x,y) -> x.getValue().compareTo(y.getValue())).map(e -> e.getKey() + " -> " + e.getValue() + "\n").collect(Collectors.toList()));

		System.out.println(articles
				.subList(1, articles.size())
				.stream()
				.max((x,y) -> (int)((CosineSimilarity.of(articles.get(0).tfidf,x.tfidf)
				- CosineSimilarity.of(articles.get(0).tfidf, y.tfidf)) * 1000000))
				.get().body);


		System.out.println(articles.stream().flatMap(x -> x.distinctWords().stream()).distinct().collect(Collectors.toList()).size());
		//System.out.println(articles.get(0).body);
		//articles.get(0).tfidf(df).forEach((k,v) -> {System.out.println(k + ": " + v);});
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
//		System.out.println(articles.get(0).title);
//		System.out.println(articles.get(0).date);
//		System.out.println(articles.get(0).topics);
//		for (Map.Entry<String, Integer> e : tf.entrySet()){
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}

		List<Cluster> clusters = KMeans.cluster(articles, 135);
		for (Cluster c : clusters){
			System.out.println(c.articles.size());
		}
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
