package cleaning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.Main;

public class Thesaurus {
	public static Map<String, String> map;
	private static Map<String, Double> df = new HashMap<String, Double>();
	private static final boolean stemming = false;

	public static boolean containsKey(String s){
		return map.containsKey(s);
	}

	public static void setIdf(Map<String, Double> idf){
		for (Map.Entry<String, Double> e : idf.entrySet()){
			df.put(stem(e.getKey()), 1.0/e.getValue());
		}
		map = getWords();
	}

	private static Map<String, String> getWords() {
		Map<String, String> map = new HashMap<String, String>();
		Stream<Map.Entry<String, String>> entries =
			toBufferedReader(new File("data" + File.separator + "thesaurus.txt"))
				.lines()
				.map(line -> line.toLowerCase())
				.filter(s -> !s.contains(" "))
				.map(line ->
					Arrays.stream(
						line.split(",")
					)
					.filter(s -> !s.contains("-"))
					.filter(s -> !s.contains("."))
					.map(Cleaner::clean)
					.collect(Collectors.toList()))
				.map(Thesaurus::toMap)
				.flatMap(subMap ->
					subMap
					.entrySet()
					.stream()
				);

		for (Map.Entry<String, String> e : entries.collect(Collectors.toList())){
			Double keyDf = df(e.getKey());
			Double valueDf = df(e.getValue());
			if (keyDf > valueDf) {
				// if the key is more common than the value, leave the key
			}
			else if (!map.containsKey(e.getKey())){
				// if the value is more common than the key, and we aren't already mapping
				// the key to something, then map the key to the value
				map.put(stem(e.getKey()), stem(e.getValue()));
			}
			else {
				Double existingDf = df(map.get(e.getKey()));
				if (valueDf > existingDf){
					// if the value is more common than the key, and the value is more common
					// than the existing value for this key, then update that mapping
					map.put(stem(e.getKey()), stem(e.getValue()));
				}
			}
		}
		return map;
	}

	private static Map<String, String> toMap(List<String> list){
		if (list.size() < 2) return new HashMap<String, String>();
		String first = stem(list.get(0));
		Map<String, String> result = new HashMap<String, String>();
		for (String s : list.subList(1, list.size())){
			result.put(stem(s), first);
		}
		return result;
	}

	private static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}

	private static double df(String s){
		Double d = df.get(s);
		return d == null ? 0.0 : d;
	}

	public static String map(String s){
		if (Main.clean == false) return s;
		String result = map.get(s);
		return result == null ? s : result;
	}

	private static String stem(String s) {
		return stemming ? Stemmer.stem(s) : s;
	}

}
