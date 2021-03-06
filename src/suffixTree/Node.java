package suffixTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Word;

public final class Node {
    // The last node of the suffix obtained by considering all nodes from
	// the root to it. It is the point where the next insertion must be made.
    public Node suffixNode;
    private HashMap<Word, Edge> edges;

    public Node() {
        edges = new HashMap<Word, Edge>();
    }

    public List<Edge> edges() {
    	return new ArrayList<Edge>(edges.values());
    }

    public List<Word> edgeWords() {
    	return new ArrayList<Word>(edges.keySet());
    }

    public boolean hasEdge(Word word) {
        return edges.containsKey(word);
    }

    public void addEdge(Word word, Edge edge) {
        edges.put(word, edge);
    }

    public Edge edge(Word word) {
        return edges.get(word);
    }

    public boolean isLeaf() {
        return edges.isEmpty();
    }

    public String toString() {
        return isLeaf() ? "Leaf" : "Edges: " + edges.size();
    }

    public void print(){
    	print("");
    }

    public void print(String indent){
    	for (Edge e : edges.values()) e.print(indent + "  ");
    }

    public int size(){
    	return isLeaf() ? 1 : 1 + edges.values().stream().mapToInt(edge -> edge.child.size()).sum();
    }
}
