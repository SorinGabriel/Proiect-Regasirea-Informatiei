package App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ro.RomanianCustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;

	public Searcher(String indexDirectoryPath) throws IOException {
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));
		indexSearcher = new IndexSearcher(indexDirectory);

		File f = new File("stopwords.txt");

		BufferedReader br = new BufferedReader(new FileReader(f));
		ArrayList<String> stopwords = new ArrayList<String>();
		String str;
		while ((str = br.readLine()) != null) {
			stopwords.add(str);
		}

		CharArraySet stopSet = new CharArraySet(stopwords, true);

		queryParser = new QueryParser(Version.LUCENE_36, LuceneConstants.CONTENTS,
				new RomanianCustomAnalyzer(Version.LUCENE_36, stopSet));
	}

	public TopDocs search(String searchQuery) throws IOException, ParseException {
		queryParser.escape(searchQuery);
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}

	public int getFrequency(ScoreDoc score, String word) throws IOException, ParseException {
		word = queryParser.parse(word).toString(LuceneConstants.CONTENTS);
		TermFreqVector tfv = indexSearcher.getIndexReader().getTermFreqVector(score.doc, LuceneConstants.CONTENTS);
		if (tfv.indexOf(word) >= 0)
			return tfv.getTermFrequencies()[tfv.indexOf(word)];

		return 0;
	}

	public int getNumDocs() {
		return indexSearcher.getIndexReader().numDocs();
	}

	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}

	public void close() throws IOException {
		indexSearcher.close();
	}
}
