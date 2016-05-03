package suffixTreeClusterer;

import java.util.List;
import java.util.Random;

import main.Article;
import suffixTree.SuffixTree;

public class STClusterer {
	public static List<STCluster> cluster(List<Article> articles, int numClusters){
		numClusters = Math.min(numClusters, articles.size());
		SuffixTree tree = new SuffixTree(articles);
		double minWeight = 10;
		List<STCluster> clusters = tree.baseClusters(minWeight);

		// Ensure a manageable number of base clusters
		while (clusters.size() < numClusters || clusters.size() > numClusters * 3){
			if (clusters.size() < numClusters){
				clusters = tree.baseClusters(minWeight /= 1.5);
			}
			else {
				clusters = tree.baseClusters(minWeight *= 1.5);
			}
		}

		System.out.println("merging clusters");
		Random r = new Random();
		while (clusters.size() > numClusters) {
			STCluster a = clusters.remove(r.nextInt(clusters.size()));

			double bestSimilarity = -1;
			int bestSimilarityIndex = -1;
			for (int i = 0; i < clusters.size(); i++) {
				STCluster b = clusters.get(i);
				if (a == b) continue;
				double similarity = a.similarityTo(b);
				if (similarity > bestSimilarity) {
					bestSimilarityIndex = i;
					bestSimilarity = similarity;
				}
			}
			clusters.add(STCluster.merge(a, clusters.remove(bestSimilarityIndex)));
		}

		return clusters;
	}
}
