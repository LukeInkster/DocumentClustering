package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		return Arrays.stream(data.listFiles())
			.filter(f -> f.getName().startsWith(dataPrefix))
			.map(Parser::toBufferedReader)
			.flatMap(this::splitFile)
			.map(this::parseArticle)
			.collect(Collectors.toList());
	}

	private Stream<String> splitFile(BufferedReader br) {
		try {
			List<String> articleStrings = new ArrayList<String>();
			String ln;
			while ((ln = br.readLine()) != null)
			{
				if (!ln.startsWith(startArticle)) continue;

				StringBuilder sb = new StringBuilder();
				while (!ln.startsWith(endArticle)) {
					sb.append(ln).append("\n");
					ln = br.readLine();
				}

				articleStrings.add(sb.toString());
			}
			return articleStrings.stream();
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	private Article parseArticle(String s) {
		String topics = 	parseTag("TOPICS",s );
		String title = 		parseTag("TITLE", s);
		String body = 		parseTag("BODY", s);
		String text_date = 	parseTag("DATE", s);

		if (body.endsWith(reuterSuffix1) || body.endsWith(reuterSuffix2))
		{
			int last = body.length() - reuterSuffix1.length();
			body = body.substring(0, last);
		}

		Date date = dateFormat.parse(text_date, new ParsePosition(0));
		return new Article(title, body, topics, date);
	}

	private String parseTag(String tag, String text){
		return parseTag(tag, text, true);
	}

	private String parseTag(String tag, String text, boolean allowEmpty) {
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";
		int startTagIndex = text.indexOf(startTag);
		if (startTagIndex < 0) {
			if (allowEmpty)	return "";
			throw new IllegalArgumentException("no start, tag=" + tag + " text=" + text);
		}
		int start = startTagIndex + startTag.length();
		int end = text.indexOf(endTag, start);
		if (end < 0) throw new IllegalArgumentException("no end, tag=" + tag + " text=" + text);
		return text.substring(start, end);
	}

	public static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}
}

