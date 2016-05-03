package suffixTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import main.Article;
import main.Phrase;
import main.Word;
import suffixTreeClusterer.STCluster;

public final class SuffixTree {

    public Node root;

    private Suffix activePoint;
    private List<Phrase> phrases = new ArrayList<Phrase>();
    public List<Word> allWords;

    public SuffixTree() {
        root = new Node();
        allWords = new ArrayList<Word>();
    }

    public SuffixTree(List<Article> articles){
        root = new Node();
        allWords = new ArrayList<Word>();

		for (Article article : articles){
			for (Phrase phrase : article.phrases()){
				addPhrase(phrase, article);
			}
		}

		root.print();
    }

    public void addPhrase(Phrase phrase, Article article) {
        if (phrases.isEmpty()) {
            activePoint = new Suffix(root, 0, -1);
        }

        int endOfPhraseIndex = allWords.size() + phrase.size();
        for (Word word : phrase.words) {
            addWord(word, article, endOfPhraseIndex);
        }

        phrases.add(phrase);
    }

    private void addWord(Word word, Article article, int endOfPhraseIndex) {
        int wordIndex = allWords.size();
    	allWords.add(word);

        Node parent = null;
        Node lastParent = null;

        // An edge is added (if necessary) for all nodes found between the active one and the
        // last one. The active node is the first node which is not a leaf (a leaf node will never
        // change its type again and will be ignored in the next steps). The end node is the first
        // node for which an edge must not be added (and the same for its successors, because they
        // are suffixes for the end node and already have the required edges).
        while(true) {
            parent = activePoint.origin;

            // If the node is explicit, check if an edge for the current word must be added.
            if(activePoint.isExplicit()) {
                if(parent.hasEdge(word)) {
                    break; // Word's already added to an edge.
                }
            }
            else if(activePoint.isImplicit()) {
                // The edge must be split before the word can be added.
                Edge edge = parent.edge(allWords.get(activePoint.startIndex));

                if(allWords.get(edge.startIndex + activePoint.span() + 1).equals(word)) {
                    break; // Word's already in the right place.
                }

                parent = splitEdge(edge, activePoint, article);
            }

            // The edge not found, create one.
            Node newNode = new Node();
            Edge newEdge = new Edge(article, allWords, wordIndex, endOfPhraseIndex - 1, parent, newNode);
            parent.addEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root)) {
                lastParent.suffixNode = parent;
            }
            lastParent = parent;

            // If the active node is the root of the tree the next suffix follows the natural order.
            if(activePoint.origin == root) activePoint.startIndex++;
            // For internal nodes a link is used.
            else activePoint.origin = activePoint.origin.suffixNode;

            // The suffix must be adjusted at each update.
            makeCanonic(activePoint);
        }

        // Connect the last node to its parent.
        if((lastParent != null) && (lastParent != root)) {
            lastParent.suffixNode = parent;
        }

        // The end point becomes the active point for the next step.
        activePoint.endIndex++;
        makeCanonic(activePoint);
    }

    private Node splitEdge(Edge edge, Suffix suffix, Article article) {
        Node newNode = new Node();
        Edge newEdge = new Edge(article, allWords, edge.startIndex, edge.startIndex + suffix.span(), suffix.origin, newNode);

        // Replace the old edge with the new one.
        suffix.origin.addEdge(allWords.get(edge.startIndex), newEdge);
        newNode.suffixNode = suffix.origin;

        // Adjust the new edge (the associated node remains a leaf).
        edge.startIndex = edge.startIndex + suffix.span() + 1;
        edge.parent = newNode;
        newNode.addEdge(allWords.get(edge.startIndex), edge);
        return newNode;
    }

    private void makeCanonic(Suffix suffix) {
        if(suffix.isExplicit() || suffix.origin == null) return;

        Word word = allWords.get(suffix.startIndex);
        Edge edge = suffix.origin.edge(word);

        while(edge.span() <= suffix.span()) {
            suffix.startIndex = suffix.startIndex + edge.span() + 1;
            suffix.origin = edge.child;

            if(suffix.startIndex <= suffix.endIndex) {
                // Search can continue at the next level.
                word = allWords.get(suffix.startIndex);
                edge = suffix.origin.edge(word);
            }
        }
    }

    private Phrase makePhrase(Stack<Edge> edges) {
        Phrase phrase = new Phrase();

        for(Edge edge : edges) {
        	for (Word word : allWords.subList(edge.startIndex, edge.endIndex + 1)){
        		phrase.words.add(word);
        	}
        }

        return phrase;
    }

    /**
     * Returns a set containing the base clusters with weight > minWeight.
     */
    public List<STCluster> baseClusters(double minWeight) {
        Set<STCluster> clusters =  new HashSet<STCluster>();
        Stack<Edge> edges = new Stack<Edge>();

        // Search the clusters on all edges from the root.
        for (Edge edge : root.edges()){
            edges.push(edge);

            if(!edge.child.isLeaf())
            	baseClustersRecursive(edge.child, clusters, edges, minWeight);

            edges.pop();
        }

        return new ArrayList<STCluster>(clusters);
    }

    private STCluster baseClustersRecursive(Node node, Set<STCluster> clusters, Stack<Edge> edges, double minWeight) {
        // Create a new cluster and set the associated phrase.
        STCluster cluster = new STCluster(makePhrase(edges));

        for (Edge edge : node.edges()){
            Node childNode = edge.child;

            if (childNode.isLeaf()) {
                if (!cluster.articles.contains(edge.article)) {
                    cluster.articles.add(edge.article);
                }
            }
            else {
                // The edge leads to an internal node. All articles that belong to the cluster
            	// associated with this internal node must be added to the current cluster.
                edges.push(edge);
                STCluster child = baseClustersRecursive(childNode, clusters, edges, minWeight);
                edges.pop();

                for (Article article : child.articles) {
                    if (!cluster.articles.contains(article)) {
                        cluster.articles.add(article);
                    }
                }
            }
        }

        // The cluster is selected if it's weight is at least the minimum weight.
        if (cluster.weight() > minWeight) clusters.add(cluster);
        return cluster;
    }
}
