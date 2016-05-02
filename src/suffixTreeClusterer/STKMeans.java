package suffixTreeClusterer;

import java.util.List;
import java.util.Random;

import main.Article;
import suffixTree.SuffixTree;

public class STKMeans {
	public static List<STCluster> cluster(List<Article> articles, int numSTClusters){
		SuffixTree tree = new SuffixTree(articles);
		List<STCluster> clusters = tree.baseClusters(0.1);

		Random r = new Random();
		while (clusters.size() > numSTClusters){
			STCluster a = clusters.remove(r.nextInt(clusters.size()));
			STCluster b = clusters.remove(r.nextInt(clusters.size()));
			clusters.add(STCluster.merge(a, b));
		}

		return clusters;
	}
}
