package suffixTree;

import java.util.HashMap;
import java.util.Iterator;

import main.Word;

public final class Node {
    // The suffix node is the last node of the suffix obtained
    // by considering all nodes from the root to it.
    // It is the point where the next insertion must be made.
    private Node suffixNode;
    private HashMap<Word, Edge> edges; // The edges to the child nodes.

    public Node() {
        edges = new HashMap<Word, Edge>(4);
    }

    public Node suffixNode() { return suffixNode; }
    public void setSuffixNode(Node value) { suffixNode = value; }
    public Iterator<Edge> edges() { return edges.values().iterator(); }

    public boolean hasEdge(Word word) {
        return edges.containsKey(word);
    }

    public void addEdge(Word word, Edge edge) {
        edges.put(word, edge);
    }

    public Edge getEdge(Word word) {
        return edges.get(word);
    }

    public boolean isLeaf() {
        return edges.isEmpty();
    }

    public String toString() {
        return isLeaf() ? "Leaf" : "Edges: " + Integer.toString(edges.size());
    }
}
