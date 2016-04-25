package documentClustering;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		File data = new File("data");
		List<Article> articles = new Parser(data).parse();

		System.out.println("Finished reading " + articles.size() + " articles in "
				+ (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();

		Map<String, Double> df = df(articles);

		System.out.println("Finished calculating term frequencies in "
				+ (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();

		for (Article a:articles){
			a.tfidf(df);
		}

		System.out.println("Finished calculating tfidf values in "
				+ (System.currentTimeMillis() - start) + "ms");

//		Map.Entry<String, Integer> max = null;
//		for (Map.Entry<String, Integer> entry : df.entrySet()){
//			if (max == null || entry.getValue() > max.getValue()){
//				max = entry;
//			}
//		}
//		System.out.println(max);

		//System.out.println(articles.get(0).body);
		//articles.get(0).tfidf(df).forEach((k,v) -> {System.out.println(k + ": " + v);});
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(0)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(1)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(2)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(3)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(4)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(5)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(6)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(7)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(8)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(9)));
		System.out.println(CosineSimilarity.of(articles.get(0), articles.get(10)));
//		System.out.println(articles.get(0).title);
//		System.out.println(articles.get(0).date);
//		System.out.println(articles.get(0).topics);
//		for (Map.Entry<String, Integer> e : tf.entrySet()){
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}
	}

	private static Map<String, Double> df(List<Article> articles) {
		double incr = 1.0/articles.size();
		Map<String, Double> df = new HashMap<String, Double>();

		articles
			.stream()
			.map(a -> a.tf())
			.forEach(tf -> {
				tf.forEach((k, v) -> {
					if (df.containsKey(k)) df.put(k, df.get(k) + incr);
					else df.put(k, incr);
				});
			});

		return df;
	}
}
