package suffixTreeClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Article;
import suffixTree.Node;

public final class SuffixTreeClusterer {

	public static ArticleSet articleSet;

	public static Node ParseSource(List<Article> articles) {
		ArticleSet reader = new ArticleSet(articles);
		return reader.tree.root;
	}

	/**
	 * Returns a list with all clusters from the document that meet conditions
	 * specified in the parameters.
	 *
	 * @param source
	 *            The source from where to read the documents.
	 * @param clusterOverlapDegree
	 *            The minimum overlapping degree for two clusters to be combined
	 *            into a single one.
	 * @param maxClusters
	 *            The maximum number of clusters to add to the result lists. The
	 *            rest of the documents are added to a cluster named "Other".
	 * @param minClusterWeight
	 *            The minimum weight of a cluster to be considered.
	 * @return A list with all clusters meeting the specified conditions.
	 */
	public static Set<STCluster> Cluster(List<Article> articles, int maxClusters, double minClusterWeight, IClusterMerger merger) {
		// Read all documents and get the base clusters.
		// The weight of each one is computed and is used to sort them
		// in ascending order. The first maxClusters clusters are created by
		// merging base clusters. Any base clusters that haven't been merged
		// into a cluster are merged into an aggregate final cluster labeled
		// 'Other'.
		articleSet = new ArticleSet(articles);

		Set<STCluster> baseClusterSet = articleSet.baseClusters(minClusterWeight);

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
		Set<STCluster> finalClusters = merger.mergeClusters(baseClusterSet);

		if (limit < baseClusterSet.size()) {
			// Some base clusters remained, group them under a single cluster.
			STCluster other = STCluster.merge(baseClusterList.subList(limit, baseClusterList.size()));
			other.label = "Other";
			finalClusters.add(other);
		}

		return finalClusters;
	}
}
