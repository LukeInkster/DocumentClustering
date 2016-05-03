package suffixTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import main.Article;
import main.Main;
import main.Phrase;
import main.Word;
import suffixTreeClusterer.STCluster;

public final class SuffixTree {
    public Node root;

    private Suffix activePoint;
    private List<Phrase> phrases = new ArrayList<Phrase>();
    private List<Word> allWords;

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

		if (Main.printTree) root.print();
    }

    public void addPhrase(Phrase phrase, Article article) {
        // If this is the first phrase, we need to make an empty suffix for the root
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

        while(true) {
            parent = activePoint.node;

            // If the node is explicit, and already has the edge we're looking for, we're done
            if(activePoint.isExplicit()) {
                if(parent.hasEdge(word)) {
                    break;
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
            if (activePoint.node == root) activePoint.startIndex++;
            // For internal nodes a link is used.
            else activePoint.node = activePoint.node.suffixNode;

            // The suffix must be adjusted at each update.
            makeCanonic(activePoint);
        }

        if((lastParent != null) && (lastParent != root)) {
            lastParent.suffixNode = parent;
        }

        activePoint.endIndex++;
        makeCanonic(activePoint);
    }

    private Node splitEdge(Edge edge, Suffix suffix, Article article) {
        Node newNode = new Node();
        Edge newEdge = new Edge(article, allWords, edge.startIndex, edge.startIndex + suffix.span(), suffix.node, newNode);

        // Replace the old edge with the new one.
        suffix.node.addEdge(allWords.get(edge.startIndex), newEdge);
        newNode.suffixNode = suffix.node;

        edge.startIndex = edge.startIndex + suffix.span() + 1;
        edge.parent = newNode;
        newNode.addEdge(allWords.get(edge.startIndex), edge);
        return newNode;
    }

    private void makeCanonic(Suffix activePoint) {
        if (activePoint.isExplicit() || activePoint.node == null) return;

        Word word = allWords.get(activePoint.startIndex);
        Edge edge = activePoint.node.edge(word);

        while (edge.span() <= activePoint.span()) {
            activePoint.startIndex = activePoint.startIndex + edge.span() + 1;
            activePoint.node = edge.child;

            if(activePoint.startIndex <= activePoint.endIndex) {
                // Search can continue at the next level.
                word = allWords.get(activePoint.startIndex);
                edge = activePoint.node.edge(word);
            }
        }
    }

    private Phrase makePhrase(Stack<Edge> edges) {
        Phrase phrase = new Phrase();

        for (Edge edge : edges) {
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
        STCluster cluster = new STCluster(makePhrase(edges));

        for (Edge edge : node.edges()){
            Node childNode = edge.child;

            if (childNode.isLeaf()) {
                if (!cluster.articles.contains(edge.article)) {
                    cluster.articles.add(edge.article);
                }
            }
            else {
                // The child is an internal node. Articles belonging to the cluster associated with
                // this internal node are added to the current cluster.
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

        if (cluster.weight() > minWeight) clusters.add(cluster);
        return cluster;
    }
}
