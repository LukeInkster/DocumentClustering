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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import main.Article;

public final class SuffixTree {
    public final class Node {
        // The suffix node is the last node of the suffix obtained
        // by considering all nodes from the root to it.
        // It is the point where the next insertion must be made.
        private Node suffixNode_;
        private HashMap<Word, Edge> edges_; // The edges to the child nodes.

        public Node() {
            edges_ = new HashMap<Word, Edge>(4);
        }

        public Node suffixNode() { return suffixNode_; }
        public void setSuffixNode(Node value) { suffixNode_ = value; }
        public Iterator<Edge> Edges() { return edges_.values().iterator(); }

        public boolean hasEdge(Word word) {
            return edges_.containsKey(word);
        }

        public void addEdge(Word word, Edge edge) {
            edges_.put(word, edge);
        }

        public Edge getEdge(Word word) {
            return edges_.get(word);
        }

        public boolean IsLeaf() {
            return edges_.isEmpty();
        }

        public String toString() {
            return IsLeaf() ? "Leaf" : "Edges: " + Integer.toString(edges_.size());
        }
    }

    public final class Edge {
        private Article document_; // The document containing the words.
        private int firstIndex_;    // The index of the first word found on the edge.
        private int lastIndex_;     // The index of the last word found on the edge.
        private Node prevNode_;     // The first node connected by the edge.
        private Node nextNode_;     // The second node connected by the edge.

        public Edge(Article doc, int first, int last,
                    Node previous, Node next) {
            document_ = doc;
            firstIndex_ = first;
            lastIndex_ = last;
            prevNode_ = previous;
            nextNode_ = next;
        }

        public Article Article() { return document_; }

        public int firstIndex() { return firstIndex_; }
        public int LastIndex() { return lastIndex_; }

        public void setFirstIndex(int value) { firstIndex_ = value; }
        public void SetLastIndex(int value) { lastIndex_ = value; }

        public Node PreviousNode() { return prevNode_; }
        public void setPreviousNode(Node value) { prevNode_ = value; }

        public Node NextNode() { return nextNode_; }
        public void SetNextNode(Node value) { nextNode_ = value; }

        public int Span() {
            return lastIndex_ - firstIndex_;
        }

        public String toString() {
            String temp = "";
            for(int i = firstIndex_; i <= lastIndex_; i++) {
                temp += tempDoc.wordAt(i).GetWord() + " ";
            }

            return temp;
        }
    }

    // Represents a suffix. Used while building the suffix tree.
    public final class Suffix {
        private Node origin_;
        private int firstIndex_;
        private int lastIndex_;

        public Suffix() {}
        public Suffix(Node origin, int first, int last) {
            origin_ = origin;
            firstIndex_ = first;
            lastIndex_ = last;
        }

        public Node origin() { return origin_; }
        public void setOrigin(Node value) { origin_ = value; }

        public int firstIndex() { return firstIndex_; }
        public void setFirstIndex(int value) { firstIndex_ = value; }

        public int LastIndex() { return lastIndex_; }
        public void SetLastIndex(int value) { lastIndex_ = value; }

        public boolean isExplicit() {
            return firstIndex_ > lastIndex_;
        }

        public boolean isImplicit() {
            return firstIndex_ <= lastIndex_;
        }

        public int span() {
            return lastIndex_ - firstIndex_;
        }
    }

    private Suffix activePoint;
    private Node root_;
    private int phrases;
    private Article tempDoc;

    public SuffixTree() {
        root_ = new Node();
        tempDoc = new Article();
    }

    public void addSentence(Article article, int start, int end) {
        if(phrases == 0) {
            activePoint = new Suffix(root_, 0, -1);
        }

        // Add the sentence (it is presumed that it includes the terminator).
        int oldCount = tempDoc.wordCount();

        for(int i = start; i < end; i++) {
            tempDoc.addWord(article.wordAt(i));
            addWord(tempDoc.wordCount() - 1, article, oldCount + (end - start));
        }

        phrases++;
    }

    // Returns a set containing the base clusters with weight > minWeight.
    public Set<Cluster> baseClusters(double minWeight) {
        Set<Cluster> clusters =  new HashSet<>();
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // Search the clusters on all edges originating from the root.
        Iterator<Edge> edgeIt = root_.Edges();

        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            edges.add(edge);

            if(!edge.NextNode().IsLeaf()) {
                getBaseClustersImpl(edge.NextNode(), clusters, edges, minWeight);
            }

            edges.remove(edges.size() - 1);
        }

        return clusters;
    }

    public Node root() {
    	return root_;
    }

    private void addWord(int wordIndex, Article document, int maxIndex) {
        Node parent = null;
        Node lastParent = null; // Used to create links between the nodes.
        Word word = tempDoc.wordAt(wordIndex);

        // An edge is added (if necessary) for all nodes found
        // between the active one and the last one. The active node
        // is the first node which is not a leaf (a leaf node will never
        // change its type again and will be ignored in the next steps).
        // The end node is the first node for which an edge must not be added
        // (and the same for its successors, because they are suffixes for
        //  the end node and already have the required edges).
        while(true) {
            parent = activePoint.origin();

            // If the node is explicit (already has edges) check if
            // an edge labeled with the current word must be added.
            if(activePoint.isExplicit()) {
                if(parent.hasEdge(word)) {
                    break; // The word is already added to an edge.
                }
            }
            else if(activePoint.isImplicit()) {
                // The edge must be split before the word can be added.
                Edge edge = parent.getEdge(tempDoc.wordAt(activePoint.firstIndex_));

                if(tempDoc.wordAt(edge.firstIndex() + activePoint.span() + 1).equals(word)) {
                    // The word is already in the right place.
                    break;
                }

                parent = splitEdge(edge, activePoint, document);
            }

            // The edge could not be found, it must be created now.
            // At the same time, the new node must be connected to the last visited one.
            Node newNode = new Node();
            Edge newEdge = new Edge(document, wordIndex, maxIndex - 1,
                                    parent, newNode);
            parent.addEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root_)) {
                lastParent.setSuffixNode(parent);
            }
            lastParent = parent;

            // Figure out the next suffix.
            if(activePoint.origin() == root_) {
                // If the active node is the root of the tree
                // the next suffix follows the natural order.
                activePoint.setFirstIndex(activePoint.firstIndex() + 1);
            }
            else {
                // For internal nodes a link is used.
                activePoint.setOrigin(activePoint.origin().suffixNode());
            }

            // The suffix must be adjusted at each update.
            makeCanonic(activePoint);
        }

        // Connect the last node to its parent.
        if((lastParent != null) && (lastParent != root_)) {
            lastParent.setSuffixNode(parent);
        }

        // The end point becomes the active point for the next step.
        activePoint.SetLastIndex(activePoint.LastIndex() + 1);
        makeCanonic(activePoint);
    }

    private Node splitEdge(Edge edge, Suffix suffix, Article document) {
        Node newNode = new Node();
        Edge newEdge = new Edge(document, edge.firstIndex(),
                                edge.firstIndex() + suffix.span(),
                                suffix.origin(), newNode);

        // Replace the old edge with the new one.
        suffix.origin().addEdge(tempDoc.wordAt(edge.firstIndex()), newEdge);
        newNode.setSuffixNode(suffix.origin());

        // Adjust the new edge (the associated node remains a leaf).
        edge.setFirstIndex(edge.firstIndex() + suffix.span() + 1);
        edge.setPreviousNode(newNode);
        newNode.addEdge(tempDoc.wordAt(edge.firstIndex()), edge);
        return newNode;
    }

    private void makeCanonic(Suffix suffix) {
        if(suffix.isExplicit()) {
            return;
        }

        Word word = tempDoc.wordAt(suffix.firstIndex());
        Edge edge = suffix.origin().getEdge(word);

        while(edge.Span() <= suffix.span()) {
            suffix.setFirstIndex(suffix.firstIndex() + edge.Span() + 1);
            suffix.setOrigin(edge.NextNode());

            if(suffix.firstIndex() <= suffix.LastIndex()) {
                // Search can continue at the next level.
                word = tempDoc.wordAt(suffix.firstIndex_);
                edge = suffix.origin().getEdge(word);
            }
        }
    }

    private Phrase makePhrase(List<Edge> edges) {
        Phrase phrase = new Phrase();

        for(int i = 0; i < edges.size(); i++) {
            SuffixTree.Edge edge = edges.get(i);

            for(int j = edge.firstIndex(); j <= edge.LastIndex(); j++) {
                phrase.words.add(tempDoc.wordAt(j));
            }
        }

        return phrase;
    }

    private Cluster getBaseClustersImpl(Node node, Set<Cluster> clusters,
    		List<Edge> edges, double minWeight) {
        // Create a new cluster and set the associated sentence.
        Cluster cluster = new Cluster(makePhrase(edges));
        Iterator<Edge> edgeIt = node.Edges();

        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            Node nextNode = edge.NextNode();

            if(nextNode.IsLeaf()) {
                // Add the document to the cluster.
                if(!cluster.Articles().contains(edge.Article())) {
                    cluster.Articles().add(edge.Article());
                }
            }
            else {
                // The edge leads to an internal node.
                // All documents that belong to the cluster associated
                // with this internal node must be added to the current cluster.
                edges.add(edge);
                Cluster child = getBaseClustersImpl(nextNode, clusters, edges, minWeight);
                edges.remove(edges.size() - 1);
                int count = child.Articles().size();

                for(int i = 0; i < count; i++) {
                    Article doc = child.Articles().get(i);

                    if(!cluster.Articles().contains(doc)) {
                        cluster.Articles().add(doc);
                    }
                }
            }
        }

        // The cluster is selected only if its weight
        // is at least equal to the minimum requested weight.
        cluster.ComputeWeight();

        if(cluster.Weight() > minWeight) {
            clusters.add(cluster);
        }

        return cluster;
    }
}
