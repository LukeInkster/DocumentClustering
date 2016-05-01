package suffixTreeClusterer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * Driver code for the Suffix Tree (ST) clustering system.
 *
 * @author harryross - harryross263@gmail.com.
 */
public class SuffixTreeClustering {

	/**
	 * Takes the filenames of the documents to be clustered as arguments.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(
					"Please provide the paths of files to cluster (i.e $ java SuffixTreeClustering file1 [file2+])");
			System.exit(0);
		}

		Queue<File> minDegreeFiles = new ArrayDeque<>();
		Queue<File> mstFiles = new ArrayDeque<>();
		for (String s : args) {
			minDegreeFiles.add(new File(s));
			mstFiles.add(new File(s));
		}

//		DocumentSource minDegreeSource = new ReutersSource(minDegreeFiles);
//
//		Set<Cluster> minDegreeClusters = ClusterFinder.Find(minDegreeSource, Integer.MAX_VALUE, 0, new MinDegreeClusterMerger(0.99));
//
//		System.out.println("Number of clusters found using minDegree: " + minDegreeClusters.size());
	}
}