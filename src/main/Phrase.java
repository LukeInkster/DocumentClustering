package main;
import java.util.ArrayList;
import java.util.List;

public final class Phrase {
    private static int phraseCount = 0;
	public final List<Word> words;
    public int startIndex;
    public int endIndex;

    public Phrase() {
    	this.words = new ArrayList<Word>();
    }

    public Phrase(List<Word> words) {
    	this.words = words;
    }

    public int size(){
    	return words.size();
    }

    public double Weight() {
        return words.stream().mapToDouble(w -> w.tfidf).sum();
    }

    public String toString() {
    	StringBuilder sb = new StringBuilder();

        for(Word word : words) {
            sb.append(word.GetWord());
            sb.append(" ");
        }

        return sb.toString();
    }

	public static Word endMarker() {
		return new Word("#" + phraseCount++);
	}
}
