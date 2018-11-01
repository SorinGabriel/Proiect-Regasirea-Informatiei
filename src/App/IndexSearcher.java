package App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class IndexSearcher {

	String indexDir = "E:\\Lucene\\Index";
	String dataDir = "E:\\Lucene\\Data";
	Indexer indexer;
	Searcher searcher;

	public static void main(String[] args) throws IOException {

		String input = "";
		File f = new File("input.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		IndexSearcher is;
		is = new IndexSearcher();
		while ((input = br.readLine()) != null) {
			try {
				is.search(input);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	private void search(String searchQuery) throws IOException, ParseException {
		System.out.println("\nSearched term: " + searchQuery);
		searcher = new Searcher(indexDir);
		long startTime = System.currentTimeMillis();
		TopDocs hits = searcher.search(searchQuery);
		long endTime = System.currentTimeMillis();

		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));

		double idf = Math.log10(searcher.getNumDocs() / hits.totalHits);
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.getDocument(scoreDoc);
			System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH));
			System.out.println("Score: " + scoreDoc.score);
			int freq = searcher.getFrequency(scoreDoc, searchQuery);
			double tf = 1 + Math.log10(freq);
			System.out.println("TF: " + tf + "\n");
		}
		System.out.println("IDF: " + idf);
		searcher.close();
	}

}
