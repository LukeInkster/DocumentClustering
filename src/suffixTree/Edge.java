package suffixTree;

import java.util.stream.Collectors;

import main.Article;

public class Edge {
    public final Article article;
    public final Article tempArticle;
    public int firstIndex;    // Index of the first word found on the edge.
    public int lastIndex;     // Index of the last word found on the edge.
    public Node child;
    public Node parent;

    public Edge(Article article, Article tempArticle, int firstIndex, int lastIndex, Node parent, Node child) {
    	this.article = article;
    	this.tempArticle = tempArticle;
    	this.firstIndex = firstIndex;
    	this.lastIndex = lastIndex;
        this.child = child;
        this.parent = parent;
    }

    public int span() {
        return lastIndex - firstIndex;
    }

    public String toString() {
        return tempArticle.toWordObjects()
        		.subList(firstIndex, lastIndex+1)
        		.stream()
        		.map(word -> word.word)
        		.collect(Collectors.joining(" "));
    }

	public void print(String indent) {
		System.out.println(indent + toString());
		child.print(indent);
	}
}
