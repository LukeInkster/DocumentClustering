package suffixTree;

import main.Article;
import main.Word;

public class Edge {
    public final Article article;
    public int firstIndex;    // Index of the first word found on the edge.
    public int lastIndex;     // Index of the last word found on the edge.
    public Node fromNode;
    public Node toNode;

    public Edge(Article article, int firstIndex, int lastIndex, Node fromNode, Node toNode) {
    	this.article = article;
    	this.firstIndex = firstIndex;
    	this.lastIndex = lastIndex;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public int span() {
        return lastIndex - firstIndex;
    }

    public String toString() {
        String temp = "";
        for(Word word : article.toWordObjects()) {
            temp += word + " ";
        }

        return temp;
    }
}
