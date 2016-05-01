// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "ArticleClustering" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "ArticleClustering" nor
// may "ArticleClustering" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package suffixTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import main.Article;
import main.Phrase;
import main.Word;
import suffixTreeClusterer.Cluster;

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
    public Set<Cluster> baseClusters(double minWeight) {
        Set<Cluster> clusters =  new HashSet<>();
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // Search the clusters on all edges originating from the root.
        Iterator<Edge> edgeIt = root.edges();

        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            edges.add(edge);

            if(!edge.nextNode.isLeaf()) {
                getBaseClustersImpl(edge.nextNode, clusters, edges, minWeight);
            }

            edges.remove(edges.size() - 1);
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
                    break; // The word is already added to an edge.
                }
            }
            else if(activePoint.isImplicit()) {
                // The edge must be split before the word can be added.
                Edge edge = parent.getEdge(tempArticle.wordAt(activePoint.firstIndex));

                if(tempArticle.wordAt(edge.firstIndex + activePoint.span() + 1).equals(word)) {
                    // The word is already in the right place.
                    break;
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
        edge.prevNode = newNode;
        newNode.addEdge(tempArticle.wordAt(edge.firstIndex), edge);
        return newNode;
    }

    private void makeCanonic(Suffix suffix) {
        if(suffix.isExplicit() || suffix.origin == null) {
            return;
        }

        Word word = tempArticle.wordAt(suffix.firstIndex);
        Edge edge = suffix.origin.getEdge(word);

        while(edge.span() <= suffix.span()) {
            suffix.firstIndex = suffix.firstIndex + edge.span() + 1;
            suffix.origin = edge.nextNode;

            if(suffix.firstIndex <= suffix.lastIndex) {
                // Search can continue at the next level.
                word = tempArticle.wordAt(suffix.firstIndex);
                edge = suffix.origin.getEdge(word);
            }
        }
    }

    private Phrase makePhrase(List<Edge> edges) {
        Phrase phrase = new Phrase();

        for(int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);

            for(int j = edge.firstIndex; j <= edge.lastIndex; j++) {
                phrase.words.add(tempArticle.wordAt(j));
            }
        }

        return phrase;
    }

    private Cluster getBaseClustersImpl(Node node, Set<Cluster> clusters,
    		List<Edge> edges, double minWeight) {
        // Create a new cluster and set the associated sentence.
        Cluster cluster = new Cluster(makePhrase(edges));
        Iterator<Edge> edgeIt = node.edges();

        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            Node nextNode = edge.nextNode;

            if(nextNode.isLeaf()) {
                // Add the document to the cluster.
                if(!cluster.articles.contains(edge.article)) {
                    cluster.articles.add(edge.article);
                }
            }
            else {
                // The edge leads to an internal node.
                // All documents that belong to the cluster associated
                // with this internal node must be added to the current cluster.
                edges.add(edge);
                Cluster child = getBaseClustersImpl(nextNode, clusters, edges, minWeight);
                edges.remove(edges.size() - 1);
                int count = child.articles.size();

                for(int i = 0; i < count; i++) {
                    Article doc = child.articles.get(i);

                    if(!cluster.articles.contains(doc)) {
                        cluster.articles.add(doc);
                    }
                }
            }
        }

        // The cluster is selected only if its weight
        // is at least equal to the minimum requested weight.
        cluster.ComputeWeight();

        if(cluster.weight > minWeight) {
            clusters.add(cluster);
        }

        return cluster;
    }
}
