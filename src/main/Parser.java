package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cleaning.Thesaurus;

public class Parser {
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SS", Locale.US);

	private static final String reuterSuffix1 = " Reuter\n&#3;";
	private static final String reuterSuffix2 = " REUTER\n&#3;";

	private static final String dataPrefix = "reut2-";
	private static final String startArticle = "<REUTERS";
	private static final String endArticle = "</REUTERS";

	private File data;

	public Parser(File data){
		this.data = data;
	}

	public List<Article> parse()
	{
		List<Article> articles = Arrays.stream(data.listFiles())
			.filter(f -> f.getName().startsWith(dataPrefix))
			.map(Parser::toBufferedReader)
			.flatMap(this::splitFile)
			.map(this::parseArticle)
			.filter(a -> !a.topics.isEmpty()) // So topics can be used to measure purity
			.filter(a -> !a.body.isEmpty()) // Ignore empty articles
			.collect(Collectors.toList());

		thesaurusInit(articles);
		for (Article a:articles) a.bodyWords();
		Map<String, Double> idf = idf(articles);
		for (Article a:articles) a.tfidf(idf);

		return articles;
	}

	public static void thesaurusInit(List<Article> articles) {
		Map<String, Double> idf = earlyIdf(articles);
		Thesaurus.setIdf(idf);
//		for (Article a:articles) a.tfidf(idf);
	}

	private Stream<String> splitFile(BufferedReader br) {
		Stream.Builder<String> articleStrings = Stream.builder();
		for (String ln; (ln = lineFrom(br)) != null;)
		{
			if (!ln.startsWith(startArticle)) continue;

			StringBuilder sb = new StringBuilder();
			while (!ln.startsWith(endArticle)) {
				sb.append(ln).append("\n");
				ln = lineFrom(br);
			}
			articleStrings.add(sb.toString());
		}
		return articleStrings.build();
	}

	private Article parseArticle(String s) {
		String titleTag = parseTag("TITLE", s);
		String bodyTag = parseTag("BODY", s);
		String dateTag = parseTag("DATE", s);
		Set<String> topics = parseListTag("TOPICS", s);
		Set<String> places = parseListTag("PLACES", s);
		Set<String> people = parseListTag("PEOPLE", s);
		Set<String> orgs = parseListTag("ORGS", s);
		Set<String> exchanges = parseListTag("EXCHANGES", s);

		if (bodyTag.endsWith(reuterSuffix1) || bodyTag.endsWith(reuterSuffix2))
		{
			int last = bodyTag.length() - reuterSuffix1.length();
			bodyTag = bodyTag.substring(0, last);
		}

		Date date = dateFormat.parse(dateTag, new ParsePosition(0));
		return new Article(titleTag, bodyTag, date, topics, places, people, orgs, exchanges);
	}

	private Set<String> parseListTag(String tag, String text) {
		return new HashSet<String>(
				Arrays.stream(
						parseTag(tag, text)
							.replace("</D>", " ")
							.replace("<D>", " ")
							.split("\\s+"))
					.filter(x -> !x.equals(""))
					.collect(Collectors.toList())
				);
	}

	private String parseTag(String tag, String text) {
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";
		int startTagIndex = text.indexOf(startTag);
		if (startTagIndex < 0) return "";
		int start = startTagIndex + startTag.length();
		int end = text.indexOf(endTag, start);
		if (end < 0) throw new IllegalArgumentException("no end, tag=" + tag + " text=" + text);
		return text.substring(start, end);
	}

	private static Map<String, Double> earlyIdf(List<Article> articles) {
		Map<String, Double> idf = new HashMap<String, Double>();

		articles
			.stream()
			.map(a -> Arrays.stream(a.body.split("\\s+")))
			.forEach(tf -> {
				tf.forEach(s -> {
					if (idf.containsKey(s)) idf.put(s, idf.get(s) + 1.0);
					else idf.put(s, 1.0);
				});
			});

		idf.forEach((k,v) -> idf.put(k, Math.log(((double)articles.size())/v)));

		return idf;
	}

	private static Map<String, Double> idf(List<Article> articles) {
		Map<String, Double> idf = new HashMap<String, Double>();

		articles
			.stream()
			.map(a -> a.tf())
			.forEach(tf -> {
				tf.forEach((k, v) -> {
					if (idf.containsKey(k)) idf.put(k, idf.get(k) + 1.0);
					else idf.put(k, 1.0);
				});
			});

		idf.forEach((k,v) -> idf.put(k, Math.log(((double)articles.size())/v)));

		return idf;
	}

	private static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}

	private String lineFrom(BufferedReader br) {
		try { return br.readLine();	}
		catch (IOException e) { throw new RuntimeException(e); }
	}
}

