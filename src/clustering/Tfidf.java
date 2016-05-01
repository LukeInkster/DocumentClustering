package clustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Tfidf {
	private Map<String, Double> map;
	private double vecLen = -1;

	public Tfidf(int i){
		map = new HashMap<String, Double>(i);
	}

	public Tfidf(){
		map = new HashMap<String, Double>();
	}

	public void put(String word, double tfidf) {
		map.put(word, tfidf);
	}

	public double get(String word) {
		Double d = map.get(word);
		return d == null ? 0 : d;
	}

	public Set<String> words() {
		return map.keySet();
	}

	public Set<Entry<String, Double>> entrySet(){
		return map.entrySet();
	}

	public Collection<Double> values(){
		return map.values();
	}

	public double vecLen() {
		if (vecLen != -1) return vecLen;
		return vecLen = Math.sqrt(values().stream().mapToDouble(x -> x * x).sum());
	}
}
