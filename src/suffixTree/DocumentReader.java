// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "DocumentClustering" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "DocumentClustering" nor
// may "DocumentClustering" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package suffixTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import main.Article;

public final class DocumentReader {
	// Each sentence must end in an unique word
	// to be inserted properly in the suffix tree.
	// For example, $0, $1, ... $100, ...
	private static final String END_MARKER = "#";

	private DocumentSource source;
	//private LinkedHashMap<String, Word> words;
	//private HashMap<Word, Integer> wordDf;

	private List<Article> articles;
	private int phraseCount;
	private SuffixTree tree;

	public DocumentReader(DocumentSource source) {
		this.source = source;
		this.words = new LinkedHashMap<String, Word>();
		this.articles = new ArrayList<Article>();
		this.wordDf = new HashMap<Word, Integer>();
		this.tree = new SuffixTree();
	}

	public DocumentReader(List<Article> articles){
		this.articles = articles;
	}

	// Reads all documents from the specified source.
	public void read() {
		while (source.hasArticle()) {
			System.out.println("Source has document");
			readArticle(source);
		}
	}

	public Set<Cluster> baseClusters(double minWeight) {
		return tree.baseClusters(minWeight);
	}

	public List<Article> articles() {
		return articles;
	}

	public SuffixTree tree() {
		return tree;
	}

	// Reads all sentences from a document and updates the statistics.
	private Article readArticle(DocumentSource source) {
		Article article = new Article();

		while (source.hasSentence()) {
			readSentence(article, source);
		}

		articles.add(article);
		return article;
	}

	// Reads and parses a sentence from the specified document.
	private void readSentence(Article article, DocumentSource source) {
		int startIndex = article.wordCount(); // The number of words before the
										// sentence.
		int endIndex;

		while (source.hasWord()) {
			// Obtain the word, then update the document and the statistics.
			String wordStr = source.nextWord();
			Word word = words.get(wordStr);

			if (word != null) {
				// The word has been found before (possible in other documents
				// too).
				if (!article.contains(word)) {
					// This is the first time the word has been found
					// in the current document, add an entry for it.
					int newCount = wordDf.get(word) + 1;
					wordDf.put(word, newCount);
				}
			} else {
				// The first time when the word is found
				// in any docuemtn, add an entry for it.
				word = new Word(wordStr);
				words.put(wordStr, word);
				wordDf.put(word, 1);
			}

			article.addWord(word);
		}

		// Add a sentence end marker (required by the suffix tree).
		String marker = END_MARKER + Integer.toString(phraseCount++);
		Word markerWord = new Word(marker);
		words.put(marker, markerWord);
		wordDf.put(markerWord, 1);
		article.addWord(markerWord);

		// Add the read sentence to the suffix tree.
		endIndex = article.wordCount();
		tree.addSentence(article, startIndex, endIndex);
	}
}
