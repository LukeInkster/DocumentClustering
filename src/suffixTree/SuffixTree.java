package suffixTree;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;

import main.Article;
import main.Phrase;
import main.Word;
import suffixTreeClusterer.STCluster;

public final class SuffixTree {

    public Node root;

    private Suffix activePoint;
    private List<Phrase> phrases = new ArrayList<Phrase>();
    public static Article tempArticle;

    public SuffixTree() {
        root = new Node();
        tempArticle = new Article();
    }

    public void addPhrase(Phrase phrase, Article article) {
        if (phrases.isEmpty()) {
            activePoint = new Suffix(root, 0, -1);
        }

        int endOfPhrase = tempArticle.wordCount() + (phrase.endIndex - phrase.startIndex);
        for (Word word : phrase.words) {
            tempArticle.addWord(word);
            int wordIndex = tempArticle.wordCount() - 1;
            addWord(wordIndex, article, endOfPhrase);
        }

        phrases.add(phrase);
    }

    private void addWord(int wordIndex, Article article, int endOfPhraseIndex) {
        Node parent = null;
        Node lastParent = null;
        Word word = tempArticle.wordAt(wordIndex);

        // An edge is added (if necessary) for all nodes found between the active one and the
        // last one. The active node is the first node which is not a leaf (a leaf node will never
        // change its type again and will be ignored in the next steps). The end node is the first
        // node for which an edge must not be added (and the same for its successors, because they
        // are suffixes for the end node and already have the required edges).
        while(true) {
            parent = activePoint.origin;

            // If the node is explicit (already has edges) check if
            // an edge labeled with the current word must be added.
            if(activePoint.isExplicit()) {
                if(parent.hasEdge(word)) {
                    break; // Word already added to an edge.
                }
            }
            else if(activePoint.isImplicit()) {
                // The edge must be split before the word can be added.
                Edge edge = parent.edge(tempArticle.wordAt(activePoint.firstIndex));

                if(tempArticle.wordAt(edge.firstIndex + activePoint.span() + 1).equals(word)) {
                    break; // Word already in the right place.
                }

                parent = splitEdge(edge, activePoint, article);
            }

            // The edge not found, create one.
            Node newNode = new Node();
            Edge newEdge = new Edge(article, tempArticle, wordIndex, endOfPhraseIndex - 1, parent, newNode);
            parent.addEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root)) {
                lastParent.suffixNode = parent;
            }
            lastParent = parent;

            // Find the next suffix.
            if(activePoint.origin == root) {
                // If the active node is the root of the tree the next suffix follows the natural order.
                activePoint.firstIndex++;
            }
            else {
                // For internal nodes a link is used.
                activePoint.origin = activePoint.origin.suffixNode;
            }

            // The suffix must be adjusted at each update.
            makeCanonic(activePoint);
        }

        // Connect the last node to its parent.
        if((lastParent != null) && (lastParent != root)) {
            lastParent.suffixNode = parent;
        }

        // The end point becomes the active point for the next step.
        activePoint.lastIndex++;
        makeCanonic(activePoint);
    }

    // Returns a set containing the base clusters with weight > minWeight.
    public Set<STCluster> baseClusters(double minWeight) {
        Set<STCluster> clusters =  new HashSet<STCluster>();
        Stack<Edge> edges = new Stack<Edge>();

        // Search the clusters on all edges from the root.
        for (Edge edge : root.edges()){
            edges.push(edge);

            if(!edge.child.isLeaf())
            	getBaseClustersImpl(edge.child, clusters, edges, minWeight);

            edges.pop();
        }

        return clusters;
    }

    private Node splitEdge(Edge edge, Suffix suffix, Article article) {
        Node newNode = new Node();
        Edge newEdge = new Edge(article, tempArticle, edge.firstIndex, edge.firstIndex + suffix.span(), suffix.origin, newNode);

        // Replace the old edge with the new one.
        suffix.origin.addEdge(tempArticle.wordAt(edge.firstIndex), newEdge);
        newNode.suffixNode = suffix.origin;

        // Adjust the new edge (the associated node remains a leaf).
        edge.firstIndex = edge.firstIndex + suffix.span() + 1;
        edge.parent = newNode;
        newNode.addEdge(tempArticle.wordAt(edge.firstIndex), edge);
        return newNode;
    }

    private void makeCanonic(Suffix suffix) {
        if(suffix.isExplicit() || suffix.origin == null) {
            return;
        }

        Word word = tempArticle.wordAt(suffix.firstIndex);
        Edge edge = suffix.origin.edge(word);

        while(edge.span() <= suffix.span()) {
            suffix.firstIndex = suffix.firstIndex + edge.span() + 1;
            suffix.origin = edge.child;

            if(suffix.firstIndex <= suffix.lastIndex) {
                // Search can continue at the next level.
                word = tempArticle.wordAt(suffix.firstIndex);
                edge = suffix.origin.edge(word);
            }
        }
    }

    private Phrase makePhrase(Stack<Edge> edges) {
        Phrase phrase = new Phrase();

        for(Edge edge : edges) {
            for(int i = edge.firstIndex; i <= edge.lastIndex; i++) {
                phrase.words.add(tempArticle.wordAt(i));
            }
        }

        return phrase;
    }

    private STCluster getBaseClustersImpl(Node node, Set<STCluster> clusters, Stack<Edge> edges, double minWeight) {
        // Create a new cluster and set the associated phrase.
        STCluster cluster = new STCluster(makePhrase(edges));

        for (Edge edge : node.edges()){
            Node childNode = edge.child;

            if (childNode.isLeaf()) {
                // Add the document to the cluster.
                if (!cluster.articles.contains(edge.article)) {
                    cluster.articles.add(edge.article);
                }
            }
            else {
                // The edge leads to an internal node. All documents that belong to the cluster
            	// associated with this internal node must be added to the current cluster.
                edges.push(edge);
                STCluster child = getBaseClustersImpl(childNode, clusters, edges, minWeight);
                edges.pop();

                for (Article article : child.articles) {
                    if (!cluster.articles.contains(article)) {
                        cluster.articles.add(article);
                    }
                }
            }
        }

        // The cluster is selected if it's weight is at least the minimum requested weight.
        if (cluster.computeWeight() > minWeight) clusters.add(cluster);

        return cluster;
    }
}
