package documentClustering;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Tfidf {
	private Map<String, Double> map = new HashMap<String, Double>();

	public void put(String word, double tfidf) {
		map.put(word, tfidf);
	}

	public double get(String word) {
		return map.containsKey(word) ? map.get(word) : 0;
	}

	public Set<String> words() {
		return map.keySet();
	}

	public Set<Entry<String, Double>> entrySet(){
		return map.entrySet();
	}
}
