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

		Map<String, Integer> tf = calculateTermFrequencies(articles);

		System.out.println("Finished calculating term frequencies in "
				+ (System.currentTimeMillis() - start) + "ms");

//		System.out.println(articles.get(0).title);
//		System.out.println(articles.get(0).date);
//		System.out.println(articles.get(0).topics);
//		for (Map.Entry<String, Integer> e : tf.entrySet()){
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}
	}

	private static Map<String, Integer> calculateTermFrequencies(List<Article> articles) {
		Map<String, Integer> tf = new HashMap<String, Integer>();

		articles
			.stream()
			.map(a -> a.df())
			.flatMap(df -> df.entrySet().stream())
			.forEach((entry) -> {
				if (tf.containsKey(entry.getKey())){
					tf.put(entry.getKey(), tf.get(entry.getKey()) + entry.getValue());
				}
				else tf.put(entry.getKey(), 1);
			});

		return tf;
	}
}
