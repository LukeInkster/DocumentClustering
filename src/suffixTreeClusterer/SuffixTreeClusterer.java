package suffixTreeClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Article;
import suffixTree.SuffixTree;

public final class SuffixTreeClusterer {
	/**
	 * Read all documents and get the base clusters. The weight of each one is computed
	 * and is used to sort them in ascending order. The first maxClusters clusters are
	 * created by merging base clusters. Any base clusters that haven't been merged into
	 * a cluster are merged into an aggregate final cluster labeled 'Other'.
	 */
	public static Set<STCluster> Cluster(List<Article> articles, int maxClusters, double minClusterWeight) {
		SuffixTree suffixTree = new SuffixTree(articles);

		Set<STCluster> baseClusterSet = suffixTree.baseClusters(minClusterWeight);

		if (baseClusterSet.isEmpty()) {
			System.out.println("No base clusters were found, this indicates an error in the program.");
			return new HashSet<STCluster>();
		}

		// Select the first 'maxClusters' base clusters.
		List<STCluster> baseClusterList = new ArrayList<>();
		baseClusterList.addAll(baseClusterSet);
		Collections.sort(baseClusterList);
		int limit = Math.min(maxClusters, baseClusterList.size());

		baseClusterSet.clear();
		baseClusterSet.addAll(baseClusterList.subList(0, limit));
		Set<STCluster> finalClusters = baseClusterSet;//merger.mergeClusters(baseClusterSet);

		if (limit < baseClusterSet.size()) {
			// Some base clusters remained, group them under a single cluster.
			STCluster other = STCluster.merge(baseClusterList.subList(limit, baseClusterList.size()));
			other.label = "Other";
			finalClusters.add(other);
		}

		return finalClusters;
	}
}
