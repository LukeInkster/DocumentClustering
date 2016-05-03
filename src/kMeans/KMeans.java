package kMeans;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import main.Article;

public class KMeans {
	public static List<Cluster> cluster(List<Article> articles, int numClusters){
		numClusters = Math.min(numClusters, articles.size());
		List<Cluster> clusters = new ArrayList<Cluster>(numClusters);
		for (int i = 0; i < numClusters; i++){
			clusters.add(new Cluster(articles.get((int)(Math.random() * articles.size()))));
		}

		int numIterations = 15;
		for (int i = 0; i < numIterations; i++){
			long start = System.currentTimeMillis();
			clusters = recalculateClusters(clusters, articles);
			if (i < numIterations - 2) clusters = restartDeadClusters(clusters, articles);
			System.out.println("KMeans iteration " + i + " completed in " +
					(System.currentTimeMillis() - start) + " ms");
		}
		return clusters;
	}

	private static List<Cluster> restartDeadClusters(List<Cluster> clusters, List<Article> articles) {
		return clusters
			.stream()
			.map(c -> !c.tfidf().entrySet().isEmpty() ? c
					: new Cluster(articles.get((int)(Math.random() * articles.size())))
			)
			.collect(Collectors.toList());
	}

	private static List<Cluster> recalculateClusters(List<Cluster> clusters, List<Article> articles) {
		List<Cluster> newClusters = new ArrayList<Cluster>(clusters.size());
		for (int i = 0; i < clusters.size(); i++){
			newClusters.add(new Cluster());
		}
		for (Article a : articles){
			int closestClusterIndex = 0;
			double closestClusterSimilarity = a.cosineSimilarityTo(clusters.get(0));
			for (int i = 1; i < clusters.size(); i++){
				double d = a.cosineSimilarityTo(clusters.get(i));
				if (d > closestClusterSimilarity){
					closestClusterSimilarity = d;
					closestClusterIndex = i;
				}
			}
			newClusters.get(closestClusterIndex).add(a);
		}
		return newClusters;
	}
}
