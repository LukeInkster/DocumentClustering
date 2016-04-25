package main;

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
		double t = IntStream
			.range(0, allWords.size())
			.mapToDouble(x -> aVals[x] * bVals[x])
			.sum();
		double u = Math.sqrt(Arrays.stream(aVals).map(x -> x * x).sum());
		double v = Math.sqrt(Arrays.stream(bVals).map(x -> x * x).sum());
		return t/(u*v);
	}

	private static void normalise(double[] vals) {
		double sum = Math.sqrt(Arrays.stream(vals).map(x -> x * x).sum());
		for (int i = 0; i < vals.length; i++){
			vals[i] = vals[i] / sum;
		}
	}

	private static Set<String> unionWords(Article a, Article b) {
		Set<String> words = new HashSet<String>();
		words.addAll(a.distinctWords());
		words.addAll(b.distinctWords());
		return words;
	}
}
