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

package suffixTreeClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import main.Article;
import main.Phrase;

public final class STCluster implements Comparable<STCluster> {
	public List<Article> articles;
	public List<Phrase> phrases;
	public double weight;
	String label;

	public STCluster(int articleCapacity, int phraseCapacity) {
		articles = new ArrayList<Article>(articleCapacity);
		phrases = new ArrayList<Phrase>(phraseCapacity);
	}

	public STCluster(Phrase phrase) {
		this(4, 1);
		phrases.add(phrase);
	}

	// Computes the weight of the clusters based on the contained documents.
	public double computeWeight() {
		// The weight is equal to the product between the number of documents, the (adjusted) length of
		// the sentences and the sum of the weight of each word's part of the sentences.
		double wordWeight = phrases.stream().mapToDouble(p -> p.weight()).sum();
		return weight = articles.size() * phrasesWeight() * wordWeight;
	}

	/*
	 * Returns the 'distance' between this cluster and another. The distance can
	 * be thought of as the average similarity of the documents in the two clusters.
	 * i.e. a measure of how overlapping the clusters are. If the two clusters are exactly
	 * identical then the similarity would be 1. If there is no overlap then the distance would be 0.
	 */
	public double similarity(STCluster other) {
		Set<Article> articleSet = new HashSet<Article>(articles);

		// Check which of the documents from the other cluster are found in this cluster.
		double common = other.articles.stream().filter(articleSet::contains).count();

		double dist_forward = common / (double) articles.size();
		double dist_backward = common / (double) other.articles.size();

		// Return the average distance between these two clusters.
		return (dist_forward + dist_backward) / 2.0 ;
	}

	// Joins clusters from the set into one cluster containing the union of the articles
	public static STCluster merge(Set<STCluster> clusters) {
		STCluster newCluster = new STCluster(clusters.size() * 2, clusters.size());
		Set<Article> allArticles = new HashSet<>();

		// Each document must appear a single time in the new cluster, as must
		// each Phrase in each original cluster.
		for (STCluster c : clusters) {
			allArticles.addAll(c.articles);
			newCluster.phrases.addAll(c.phrases);
		}

		// Add the documents to the new cluster.
		newCluster.articles.addAll(allArticles);
		return newCluster;
	}

	public static STCluster merge(List<STCluster> clusters) {
		Set<STCluster> clusterSet = new HashSet<>();
		clusterSet.addAll(clusters);
		return merge(clusterSet);
	}

	private double phrasesWeight() {
		double sum = phrases.stream().mapToDouble(p -> p.words.size()).sum();
		return sum < 2 ? 0.5 : Math.min(6, sum);
	}

	public int compareTo(STCluster other) {
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

	public Optional<String> mostCommonTopic(){
		List<String> allTopics = articles
			.stream()
			.flatMap(a -> a.topics.stream())
			.collect(Collectors.toList());

		return allTopics
			.stream()
			.max((x,y) ->
				Collections.frequency(allTopics, x) -
				Collections.frequency(allTopics, y));
	}

	/**
	 * @return The proportion of articles that contain the most common topic in the cluster
	 */
	public double purity(){
		Optional<String> mostCommon = mostCommonTopic();

		if (!mostCommon.isPresent()) return 1;

		return (double)articles.stream().filter(a -> a.topics.contains(mostCommon.get())).count() /
				(double)articles.size();
	}
}
