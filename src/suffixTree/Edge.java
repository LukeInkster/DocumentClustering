package suffixTree;

import main.Article;
import main.Word;

public class Edge {
    public final Article article;
    public int firstIndex;    // Index of the first word found on the edge.
    public int lastIndex;     // Index of the last word found on the edge.
    public Node child;
    public Node parent;

    public Edge(Article article, int firstIndex, int lastIndex, Node child, Node parent) {
    	this.article = article;
    	this.firstIndex = firstIndex;
    	this.lastIndex = lastIndex;
        this.child = child;
        this.parent = parent;
    }

    public int span() {
        return lastIndex - firstIndex;
    }

    public String toString() {
        String temp = "";

        for(Word word : SuffixTree.tempArticle.toWordObjects().subList(firstIndex, lastIndex+1)) {
            temp += word.word + " ";
        }

        return temp;
    }
}
