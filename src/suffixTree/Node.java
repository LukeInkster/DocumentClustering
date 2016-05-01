package suffixTree;

import java.util.HashMap;
import java.util.Iterator;

import main.Word;

public final class Node {
    // The last node of the suffix obtained by considering all nodes from
	// the root to it. It is the point where the next insertion must be made.
    public Node suffixNode;

    // The edges to the child nodes.
    private HashMap<Word, Edge> edges;

    public Node() {
        edges = new HashMap<Word, Edge>(4);
    }

    public Iterator<Edge> edges() {
    	return edges.values().iterator();
    }

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
        return isLeaf() ? "Leaf" : "Edges: " + edges.size();
    }
}
