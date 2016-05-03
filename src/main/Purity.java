package main;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Purity {
	private static Optional<String> mostCommonTopic(Collection<Article> articles){
		List<String> allTopics = articles
			.stream()
			.flatMap(a -> a.topics.stream())
			.collect(Collectors.toList());

		return allTopics
			.stream()
			.max((x,y) ->
				Collections.frequency(allTopics, x) -
				Collections.frequency(allTopics, y));
	}

	/**
	 * @return The proportion of articles that contain the most common topic in the cluster
	 */
	public static double of(Collection<Article> articles){
		Optional<String> mostCommon = mostCommonTopic(articles);

		if (!mostCommon.isPresent()) return 1;

		return (double)articles.stream().filter(a -> a.topics.contains(mostCommon.get())).count() /
				(double)articles.size();
	}
}
