package cleaning;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cleaner {
	private static String notWordAndNotWhitespace = "[\\W&&[^\\s]]";
	private static String notWord = "\\W+";

	public static String clean(String s){
		return s.replaceAll(notWordAndNotWhitespace, "").toLowerCase();
	}

	public static List<String> cleanAndSplit(String s) {
		return Arrays.stream(s
				.replace("-", " ")
				.replace(",", " ")
				.replace(".", " ")
				.replaceAll(notWordAndNotWhitespace, "")
				.toLowerCase()
				.split(notWord)
			)
			.filter(Cleaner::isNotNumber)
			.filter(Cleaner::isNotEmptyOrWhitespace)
//			.map(Thesaurus::map)
			.map(Stemmer::stem)
			.filter(StopWords::isSafe)
			.collect(Collectors.toList());
	}

	public static boolean isNotNumber(String s){
		return !s.matches("[0-9]+");
	}

	public static boolean isNotEmptyOrWhitespace(String s){
		return !s.matches("\\s*");
	}

	public static List<List<String>> cleanAndSplitToSentences(String s) {
		return Arrays.stream(s
				.replaceAll("[0-9+]\\.[0-9+]", "")
				.split("\\.")
			)
			.map(x -> cleanAndSplit(x))
			.filter(x -> !x.isEmpty())
			.collect(Collectors.toList());
	}
}
