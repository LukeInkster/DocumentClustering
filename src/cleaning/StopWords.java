package cleaning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import main.Main;

public class StopWords {
	public static Set<String> words = getWords();

	public static boolean contains(String s){
		return words.contains(s);
	}

	public static boolean isSafe(String s){
		if (Main.clean == false) return true;
		return !contains(s);
	}

	private static Set<String> getWords() {
		try {
			return Arrays.stream(
				toBufferedReader(new File("data" + File.separator + "stopwords.txt"))
					.readLine()
					.split(",")
				)
				.map(Cleaner::clean)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}
}
