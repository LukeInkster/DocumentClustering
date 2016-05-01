package junk;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractOverlappingClusterMerger implements IClusterMerger {

	/**
	 * The degree to which the number of documents in a base cluster must
	 * overlap before the two base clusters can be merged into one cluster.
	 */
	double minOverlapDegree;

	/**
	 * Returns a list of {@link GraphVertex} objects describing the list of base
	 * clusters passed to it.
	 *
	 * @param baseClusters
	 *            - clusters for which to generate info.
	 * @return
	 */
	public Set<GraphVertex> generateVertices(Set<STCluster> baseClusters) {
		Set<GraphVertex> vertices = new HashSet<>();
		for (STCluster bc : baseClusters) {
			vertices.add(new GraphVertex(bc));
		}

		return vertices;
	}

	public void setOverlapDegree(double overlapDegree_) {
		this.minOverlapDegree = overlapDegree_;
	}

	class GraphVertex implements Cloneable {
		/* The cluster that this vertex belongs to. */
		private STCluster cluster;

		/*
		 * Whether or not this vertex has been discovered. Used to keep track of
		 * a BFS.
		 */
		private boolean discovered;

		public GraphVertex(STCluster cluster) {
			this.cluster = cluster;
		}

		public STCluster cluster() {
			return cluster;
		}

		public boolean isDiscovered() {
			return discovered;
		}

		public void setDiscovered(boolean value) {
			discovered = value;
		}

		public GraphVertex clone() {
			return new GraphVertex(cluster);
		}
	}
}
