package suffixTreeClusterer;

import java.util.List;
import java.util.Random;

import main.Article;
import suffixTree.SuffixTree;

public class STKMeans {
	public static List<STCluster> cluster(List<Article> articles, int numClusters){
		numClusters = Math.min(numClusters, articles.size());
		SuffixTree tree = new SuffixTree(articles);
		double minWeight = 10;
		List<STCluster> clusters = tree.baseClusters(minWeight);
		while (clusters.size() < numClusters){
			clusters = tree.baseClusters(minWeight /= 10.0);
		}

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
