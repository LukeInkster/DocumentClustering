package documentClustering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class CosineSimilarity {
	public static double of(Article a, Article b){
		Set<String> allWords = unionWords(a, b);
		double[] aVals = new double[allWords.size()];
		double[] bVals = new double[allWords.size()];
		int i = 0;
		for (String w : allWords){
			aVals[i] = a.tfidf(w);
			bVals[i] = b.tfidf(w);
			++i;
		}

//		System.out.println(sumProducts(aVals, bVals));
//		System.out.println(vecLen(aVals));
//		System.out.println(vecLen(bVals));

		return //Math.cos(
				sumProducts(aVals, bVals) /
				(vecLen(aVals) * vecLen(bVals));
			//);
	}

	private static double sumProducts(double[] aVals, double[] bVals) {
		if (aVals.length != bVals.length)
			throw new RuntimeException("Array lengths must be equal");

		return IntStream
			.range(0, aVals.length)
			.mapToDouble(x -> aVals[x] * bVals[x])
			.sum();
	}

	private static double vecLen(double[] vals) {
		return Math.sqrt(
				Arrays.stream(vals)
					.map(x -> x * x)
					.sum()
				);
	}

	private static Set<String> unionWords(Article a, Article b) {
		Set<String> words = new HashSet<String>();
		words.addAll(a.distinctWords());
		words.addAll(b.distinctWords());
		return words;
	}
}
