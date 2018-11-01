package App;

import java.io.IOException;

public class IndexCreator {

	String indexDir = "E:\\Lucene\\Index";
	String dataDir = "E:\\Lucene\\Data";
	Indexer indexer;

	public static void main(String[] args) throws IOException {
		IndexCreator ic = new IndexCreator();
		ic.createIndex();
	}

	private void createIndex() throws IOException {
		indexer = new Indexer(indexDir);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}

}
