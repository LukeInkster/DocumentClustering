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

import main.Parser;

public class Thesaurus {
	public static Map<String, String> map = getWords();

	public static boolean containsKey(String s){
		return map.containsKey(s);
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
					.map(Parser::clean)
					.collect(Collectors.toList()))
				.map(Thesaurus::toMap)
				.flatMap(subMap ->
					subMap
					.entrySet()
					.stream()
				);

		entries.forEach(e -> {
			map.put(e.getKey(), e.getValue());
		});
		return map;
	}

	public static Map<String, String> toMap(List<String> list){
		if (list.size() < 2) return new HashMap<String, String>();
		String first = list.get(0);
		return list.subList(1, list.size())
			.stream()
			.distinct()
			.collect(Collectors.toMap(s -> s, s -> first));
	}

	public static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}

	public static Stream<String> mapAll(Stream<String> stream){
		return stream.map(s -> {
			String result = map.get(s);
			return result == null ? s : result;
		});
	}

	public static String map(String s){
		String result = map.get(s);
		return result == null ? s : result;
	}

}
