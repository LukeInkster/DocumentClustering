package kMeans;

import java.util.HashSet;
import java.util.Set;

public class CosineSimilarity {
	public static double of(Tfidf a, Tfidf b){
		return sumProducts(a, b)
			/ (vecLen(a) * vecLen(b));
	}

	private static double sumProducts(Tfidf a, Tfidf b) {
		return intersectionWords(a, b).stream()
				.mapToDouble(x -> a.get(x) * b.get(x)).sum();
	}

	private static double vecLen(Tfidf tfidf) {
		return tfidf.vecLen();
	}

	private static Set<String> intersectionWords(Tfidf a, Tfidf b) {
		Set<String> words = new HashSet<String>(a.words());
		words.retainAll(b.words());
		return words;
	}
}
