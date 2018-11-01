package App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ro.RomanianCustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Indexer {

	private IndexWriter writer;

	public Indexer(String indexDirectoryPath) throws IOException {
		// this directory will contain the indexes
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));

		File f = new File("stopwords.txt");

		BufferedReader br = new BufferedReader(new FileReader(f));
		ArrayList<String> stopwords = new ArrayList<String>();
		String str;
		while ((str = br.readLine()) != null) {
			stopwords.add(str);
		}

		CharArraySet stopSet = new CharArraySet(stopwords, true);

		// create the indexer
		writer = new IndexWriter(indexDirectory, new RomanianCustomAnalyzer(Version.LUCENE_36, stopSet), true,
				IndexWriter.MaxFieldLength.UNLIMITED);
	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	private Document getDocument(File file) throws IOException {
		Document document = new Document();

		// Field contentField = new Field(LuceneConstants.CONTENTS, new
		// FileReader(file));
		/* pe aici am facut eu artificii dar nu a mers */
		String content = null;
		Field contentField = new Field(LuceneConstants.CONTENTS, new FileReader(file), Field.TermVector.YES);
		if (file.getName().toLowerCase().endsWith(".pdf")) {
			try {
				PDDocument PDFdoc = PDDocument.load(file);
				content = new PDFTextStripper().getText(PDFdoc);
				PDFdoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			contentField = new Field(LuceneConstants.CONTENTS, content, Field.Store.YES, Field.Index.ANALYZED,
					Field.TermVector.YES);
		} else if (file.getName().toLowerCase().endsWith(".html")) {
			InputStream stream = new FileInputStream(file);
			JTidyHTMLHandler html = new JTidyHTMLHandler();
			content = html.getDocument(stream);
			contentField = new Field(LuceneConstants.CONTENTS, content, Field.Store.YES, Field.Index.ANALYZED,
					Field.TermVector.YES);
		} else if (file.getName().toLowerCase().endsWith(".ro") || file.getName().toLowerCase().endsWith(".com")) {
			URL url = new URL("http://" + file.getName());
			URLConnection connection = url.openConnection();
			InputStream stream = connection.getInputStream();
			JTidyHTMLHandler html = new JTidyHTMLHandler();
			content = html.getDocument(stream);
			// System.out.println(content);
			contentField = new Field(LuceneConstants.CONTENTS, content, Field.Store.YES, Field.Index.ANALYZED,
					Field.TermVector.YES);
		}

		// index file name
		Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.YES); // NOT_ANALYZED
		// index file path
		Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.YES); // NOT_ANALYZED

		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);

		return document;
	}

	private void indexFile(File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}

	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();

		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return writer.numDocs();
	}
}