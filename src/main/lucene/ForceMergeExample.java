import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class ForceMergeExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/forceMergeIndex";

    public static void main(String[] args) throws IOException {
        System.out.println("Creating index...");

        // 创建索引
        createIndex();

        System.out.println("Opening index...");

        // 打开索引
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        System.out.println("Forcing merge...");

        // 强制合并索引中的所有段
        indexWriter.forceMerge(1); // 合并为1个段

        System.out.println("Merge complete. Closing writer...");

        indexWriter.close();
        directory.close();

        System.out.println("Index merged and closed.");
    }

    private static void createIndex() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField("content", "Lucene is a powerful text search engine library.", Field.Store.YES));
        indexWriter.addDocument(doc);

        System.out.println("Index created with 1 document.");

        indexWriter.close();
    }
}