package suffixTreeClusterer;

import java.util.List;
import java.util.Set;

import main.Article;
import main.Phrase;
import suffixTree.SuffixTree;

public final class ArticleSet {
	public final List<Article> articles;
	public final SuffixTree tree = new SuffixTree();

	public ArticleSet(List<Article> articles){
		this.articles = articles;

		for (Article article : articles){
			for (Phrase phrase : article.phrases()){
				tree.addSentence(phrase, article);
			}
		}
	}

	public Set<STCluster> baseClusters(double minWeight) {
		return tree.baseClusters(minWeight);
	}
}
