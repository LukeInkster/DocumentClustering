package main;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Article {

	public final String title;
	public final String body;
	public final String topics;
	public final Date date;

	private List<String> words;

	public Article(String title, String body, String topics, Date date){
		this.title = title;
		this.body = body;
		this.topics = topics;
		this.date = date;
	}

	public List<String> bodyWords(){
		return words == null ? words = toWords(body) : words;
	}

	private static List<String> toWords(String s){
		return Arrays.asList(s.split("\\s+"));
	}

}
