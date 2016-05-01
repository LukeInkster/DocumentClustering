package suffixTreeClusterer;

import java.util.HashSet;
import java.util.Set;

/**
 * Merges base clusters to produce final clusters using a minimum overlap threshold.
 */
public class MinDegreeClusterMerger extends AbstractOverlappingClusterMerger {

	public MinDegreeClusterMerger(double minOverlapDegree) {
		this.minOverlapDegree = minOverlapDegree;
	}

	public Set<STCluster> mergeClusters(Set<STCluster> baseClustersToMerge) {
		// Build a graph of similar base clusters.
		Set<GraphVertex> vertices = generateVertices(baseClustersToMerge);
		ClusterGraph cg = ClusterGraph.buildGraph(vertices, minOverlapDegree);

		// Find the different connected components within the graph produced above.
		Set<ClusterGraph> connectedComponents = cg.getConnectedComponents();

		// Unify the clusters from each connected component
		// into a single one and add them to the resulting list.
		Set<STCluster> clusters = new HashSet<>();
		for (ClusterGraph connectedComponent : connectedComponents) {
			Set<STCluster> clustersInConnectedComponent = connectedComponent.getClusters();
			clusters.add(STCluster.merge(clustersInConnectedComponent));
		}

		return clusters;
	}
}
