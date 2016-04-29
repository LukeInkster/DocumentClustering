package documentClustering;

import java.util.ArrayList;
import java.util.List;

public class KMeans {
	public static List<Cluster> cluster(List<Article> articles, int numClusters){
		List<Cluster> clusters = new ArrayList<Cluster>(numClusters);
		for (int i = 0; i < numClusters; i++){
			clusters.add(new Cluster(articles.get((int)(Math.random() * articles.size()))));
		}

		for (int i = 0; i < 10; i++){
			long start = System.currentTimeMillis();
			clusters = recalculateClusters(clusters, articles);
			System.out.println("KMeans iteration " + i + " completed in " +
					(System.currentTimeMillis() - start) + " ms");
		}
		return clusters;
	}

	private static List<Cluster> recalculateClusters(List<Cluster> clusters, List<Article> articles) {
		List<Cluster> newClusters = new ArrayList<Cluster>(clusters.size());
		for (int i = 0; i < clusters.size(); i++){
			newClusters.add(new Cluster());
		}
		for (Article a : articles){
			int closestClusterIndex = 0;
			double closestClusterDistance = a.cosineSimilarityTo(clusters.get(0));
			for (int i = 1; i < clusters.size(); i++){
				double d = a.cosineSimilarityTo(clusters.get(i));
				if (d < closestClusterDistance){
					closestClusterDistance = d;
					closestClusterIndex = i;
				}
			}
			newClusters.get(closestClusterIndex).add(a);
		}
		return newClusters;
	}
}