package suffixTree;

import java.util.List;
import java.util.stream.Collectors;

import main.Article;
import main.Word;

public class Edge {
    public final Article article;
    public final List<Word> allWords;
    public int startIndex;    // Index of the first word found on the edge.
    public int endIndex;     // Index of the last word found on the edge.
    public Node child;
    public Node parent;

    public Edge(Article article, List<Word> allWords, int firstIndex, int lastIndex, Node parent, Node child) {
    	this.article = article;
    	this.allWords = allWords;
    	this.startIndex = firstIndex;
    	this.endIndex = lastIndex;
        this.child = child;
        this.parent = parent;
    }

    public int span() {
        return endIndex - startIndex;
    }

    public String toString() {
        return allWords
        		.subList(startIndex, endIndex+1)
        		.stream()
        		.map(word -> word.word)
        		.collect(Collectors.joining(" "));
    }

	public void print(String indent) {
		System.out.println(indent + toString());
		child.print(indent);
	}
}
