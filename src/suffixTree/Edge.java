package suffixTree;

import main.Article;

public class Edge {
    public final Article article;
    public int firstIndex;    // Index of the first word found on the edge.
    public int lastIndex;     // Index of the last word found on the edge.
    public Node prevNode;     // First node connected by the edge.
    public Node nextNode;     // Second node connected by the edge.

    public Edge(Article doc, int first, int last, Node previous, Node next) {
        article = doc;
        firstIndex = first;
        lastIndex = last;
        prevNode = previous;
        nextNode = next;
    }

    public int span() {
        return lastIndex - firstIndex;
    }

    public String toString() {
        String temp = "";
        for(int i = firstIndex; i <= lastIndex; i++) {
            temp += article.wordAt(i).GetWord() + " ";
        }

        return temp;
    }
}
