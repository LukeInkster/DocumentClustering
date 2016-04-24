package main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws IOException {
		System.out.println("Reading articles");
		long start = System.currentTimeMillis();

		File data = new File("data");
		List<Article> articles = new Parser(data).parse();

		System.out.println("Finished reading " + articles.size() + " articles in "
				+ (System.currentTimeMillis() - start) + "ms");
		System.out.println("Calculating Term Frequencies");
		start = System.currentTimeMillis();

		Map<String, Integer> df = df(articles);

		System.out.println("Finished calculating term frequencies in "
				+ (System.currentTimeMillis() - start) + "ms");

		Map.Entry<String, Integer> max = null;
		for (Map.Entry<String, Integer> entry : df.entrySet()){
			if (max == null || entry.getValue() > max.getValue()){
				max = entry;
			}
		}
		System.out.println(max);

		System.out.println(articles.get(0).body);
		articles.get(0).tfidf(df).forEach((k,v) -> {System.out.println(k + ": " + v);});

//		System.out.println(articles.get(0).title);
//		System.out.println(articles.get(0).date);
//		System.out.println(articles.get(0).topics);
//		for (Map.Entry<String, Integer> e : tf.entrySet()){
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}
	}

	private static Map<String, Integer> df(List<Article> articles) {
		Map<String, Integer> df = new HashMap<String, Integer>();

		articles
			.stream()
			.map(a -> a.tf())
			.forEach(tf -> {
				tf.forEach((k, v) -> {
					if (df.containsKey(k)) df.put(k, df.get(k) + 1);
					else df.put(k, 1);
				});
			});

		return df;
	}
}
