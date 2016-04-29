package documentClustering;

import java.util.HashSet;
import java.util.Set;

public class CosineSimilarity {
	public static double of(Tfidf a, Tfidf b){
		return sumProducts(a, b)
			/ (vecLen(a) * vecLen(b));
	}

	private static double sumProducts(Tfidf a, Tfidf b) {
		return unionWords(a, b).stream()
				.mapToDouble(x -> a.get(x) * b.get(x)).sum();
	}

	private static double vecLen(Tfidf tfidf) {
		return tfidf.vecLen();
	}

	private static Set<String> unionWords(Tfidf a, Tfidf b) {
		Set<String> words = new HashSet<String>();
		words.addAll(a.words());
		words.addAll(b.words());
		return words;
	}
}
