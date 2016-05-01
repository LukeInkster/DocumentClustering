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
// * The name "DocumentClustering" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "DocumentClustering" nor
// may "DocumentClustering" appear in their names without prior written
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
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import main.Article;

public final class Cluster implements Comparable<Cluster> {
	private List<Article> articles;
	private List<Phrase> phrases;
	private double weight;
	private String label;

	public Cluster(int docCapacity, int phraseCapacity) {
		articles = new ArrayList<Article>(docCapacity);
		phrases = new ArrayList<Phrase>(phraseCapacity);
	}

	public Cluster(Phrase phrase) {
		this(4, 1);
		phrases.add(phrase);
	}

	// Computes the weight of the clusters based on the contained documents.
	public void ComputeWeight() {
		// The weight is equal to the product between the number of documents,
		// the (adjusted) length of the sentences and the sum of the weight
		// of each words part of the sentences.
		double wordWeight = 0;
		int count = phrases.size();

		for (int i = 0; i < count; i++) {
			wordWeight += phrases.get(i).Weight();
		}

		weight = articles.size() * phrasesWeight() * wordWeight;
	}

	/*
	 * Returns the 'distance' between this cluster and another. The distance can
	 * be thought of as the average similarity of the documents in the two clusters.
	 * i.e. a measure of how overlapping the clusters are. If the two clusters are exactly
	 * identical then the similarity would be 1. If there is no overlap then the distance would be 0.
	 */
	public double similarity(Cluster other) {
		Hashtable<Article, Article> hash = new Hashtable<Article, Article>();

		for (int i = 0; i < articles.size(); i++) {
			Article doc = articles.get(i);
			hash.put(doc, doc);
		}

		// Check which of the documents from the other clusters
		// are found in the hash table.
		double common = 0;
		for (int i = 0; i < other.articles.size(); i++) {
			if (hash.containsKey(other.articles.get(i))) {
				common++;
			}
		}

		double dist_forward = common / (double) articles.size();
		double dist_backward = common / (double) other.articles.size();

		// Return the average distance between these two clusters.
		return (dist_forward + dist_backward) / 2.0 ;
	}

	// Unifies all clusters from the specified list
	// into a single cluster containing the union of the documents.
	public static Cluster Merge(Set<Cluster> clusters) {
		assert(clusters != null);

		Cluster newCluster = new Cluster(clusters.size() * 2, clusters.size());
		Set<Article> allArticles = new HashSet<>();

		// Each document must appear a single time in the new cluster, as must
		// each Phrase in each original cluster.
		for (Cluster c : clusters) {
			allArticles.addAll(c.articles);
			newCluster.phrases().addAll(c.phrases());
		}

		// Add the documents to the new cluster.
		newCluster.articles.addAll(allArticles);
		return newCluster;
	}

	public static Cluster Merge(List<Cluster> clusters) {
		Set<Cluster> clusterSet = new HashSet<>();
		clusterSet.addAll(clusters);
		return Merge(clusterSet);
	}

	public double Weight() {
		return weight;
	}

	public List<Article> Articles() {
		return articles;
	}

	public List<Phrase> phrases() {
		return phrases;
	}

	public void setPhrases(List<Phrase> value) {
		phrases = value;
	}

	public String label() {
		return label;
	}

	public void setLabel(String value) {
		label = value;
	}

	private double phrasesWeight() {
		double sum = 0;
		int count = phrases.size();

		for (int i = 0; i < count; i++) {
			sum += phrases.get(i).words.size();
		}

		if (sum < 2) {
			return 0.5;
		} else {
			return Math.min(6, sum);
		}
	}

	public int compareTo(Cluster other) {
		return other == this ? 0 : ((int) other.weight - (int) weight);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster: ");
		sb.append(label);
		sb.append("; Weight: ");
		sb.append(weight);
		sb.append("; Number docs: ");
		sb.append(articles.size());
		for (Phrase p : phrases) {
			sb.append(p.toString());
		}
		return sb.toString();
	}
}
