package suffixTree;

import main.Article;

public class Edge {
    private Article article; // The document containing the words.
    private int firstIndex;    // The index of the first word found on the edge.
    private int lastIndex;     // The index of the last word found on the edge.
    private Node prevNode;     // The first node connected by the edge.
    private Node nextNode;     // The second node connected by the edge.

    public Edge(Article doc, int first, int last, Node previous, Node next) {
        article = doc;
        firstIndex = first;
        lastIndex = last;
        prevNode = previous;
        nextNode = next;
    }

    public Article article() { return article; }

    public int firstIndex() { return firstIndex; }
    public int LastIndex() { return lastIndex; }

    public void setFirstIndex(int value) { firstIndex = value; }
    public void SetLastIndex(int value) { lastIndex = value; }

    public Node previousNode() { return prevNode; }
    public void setPreviousNode(Node value) { prevNode = value; }

    public Node nextNode() { return nextNode; }
    public void SetNextNode(Node value) { nextNode = value; }

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
