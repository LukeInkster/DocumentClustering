package suffixTree;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import main.Article;
import main.Phrase;
import main.Word;
import suffixTreeClusterer.STCluster;

public final class SuffixTree {

    public Node root;

    private Suffix activePoint;
    private int phrases;
    private Article tempArticle;

    public SuffixTree() {
        root = new Node();
        tempArticle = new Article();
    }

    public void addSentence(Phrase phrase, Article article) {
        if(phrases == 0) {
            activePoint = new Suffix(root, 0, -1);
        }

        for(Word word : phrase.words) {
            tempArticle.addWord(word);
            addWord(word, article, phrase.startIndex, phrase.endIndex);
        }

        phrases++;
    }

    // Returns a set containing the base clusters with weight > minWeight.
    public Set<STCluster> baseClusters(double minWeight) {
        Set<STCluster> clusters =  new HashSet<STCluster>();
        Stack<Edge> edges = new Stack<Edge>();

        // Search the clusters on all edges from the root.
        for (Edge edge : root.edges()){
            edges.push(edge);

            if(!edge.toNode.isLeaf())
            	getBaseClustersImpl(edge.toNode, clusters, edges, minWeight);

            edges.pop();
        }

        return clusters;
    }

    private void addWord(Word word, Article article, int wordIndex, int maxIndex) {
        Node parent = null;
        Node lastParent = null; // Used to create links between the nodes.

        // An edge is added (if necessary) for all nodes found
        // between the active one and the last one. The active node
        // is the first node which is not a leaf (a leaf node will never
        // change its type again and will be ignored in the next steps).
        // The end node is the first node for which an edge must not be added
        // (and the same for its successors, because they are suffixes for
        //  the end node and already have the required edges).
        while(true) {
            parent = activePoint.origin;
            if (parent == null) break;

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

            // The edge could not be found, it must be created now.
            // At the same time, the new node must be connected to the last visited one.
            Node newNode = new Node();
            Edge newEdge = new Edge(article, wordIndex, maxIndex - 1, parent, newNode);
            parent.addEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root)) {
                lastParent.suffixNode = parent;
            }
            lastParent = parent;

            // Figure out the next suffix.
            if(activePoint.origin == root) {
                // If the active node is the root of the tree
                // the next suffix follows the natural order.
                activePoint.firstIndex = activePoint.firstIndex + 1;
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
        activePoint.lastIndex = activePoint.lastIndex + 1;
        makeCanonic(activePoint);
    }

    private Node splitEdge(Edge edge, Suffix suffix, Article document) {
        Node newNode = new Node();
        Edge newEdge = new Edge(document, edge.firstIndex,
                                edge.firstIndex + suffix.span(),
                                suffix.origin, newNode);

        // Replace the old edge with the new one.
        suffix.origin.addEdge(tempArticle.wordAt(edge.firstIndex), newEdge);
        newNode.suffixNode = suffix.origin;

        // Adjust the new edge (the associated node remains a leaf).
        edge.firstIndex = edge.firstIndex + suffix.span() + 1;
        edge.fromNode = newNode;
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
            suffix.origin = edge.toNode;

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
        // Create a new cluster and set the associated sentence.
        STCluster cluster = new STCluster(makePhrase(edges));

        for (Edge edge : node.edges()){
            Node nextNode = edge.toNode;

            if(nextNode.isLeaf()) {
                // Add the document to the cluster.
                if(!cluster.articles.contains(edge.article)) {
                    cluster.articles.add(edge.article);
                }
            }
            else {
                // The edge leads to an internal node. All documents that belong to the cluster
            	// associated with this internal node must be added to the current cluster.
                edges.push(edge);
                STCluster child = getBaseClustersImpl(nextNode, clusters, edges, minWeight);
                edges.pop();
                int count = child.articles.size();

                for(int i = 0; i < count; i++) {
                    Article doc = child.articles.get(i);

                    if(!cluster.articles.contains(doc)) {
                        cluster.articles.add(doc);
                    }
                }
            }
        }

        // The cluster is selected if it's weight is at least the minimum requested weight.
        if(cluster.computeWeight() > minWeight) clusters.add(cluster);

        return cluster;
    }
}
