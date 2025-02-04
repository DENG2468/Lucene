import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

public class DocValuesExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/docvaluesIndex";

    public static void main(String[] args) throws IOException {
        // 创建索引
        createIndex();

        // 打开索引
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // 执行排序和范围查询
        Sort sort = new Sort(new SortField("price", SortField.Type.LONG, true));
        Query query = new MatchAllDocsQuery(); // 匹配所有文档的查询
        TopDocs results = searcher.search(query, 10, sort);

        System.out.println("Sorted documents by price:");
        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = searcher.doc(hit.doc);
            BytesRef bytesRef = doc.getBinaryValue("price");
            if (bytesRef != null) {
                try {
                    long price = Long.parseLong(bytesRef.utf8ToString());
                    System.out.println("Price: " + price);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price format for document: " + hit.doc);
                }
            }else{
                System.out.println("bytesRef == null");
            }
        }
        reader.close();
        directory.close();
    }

    private static void createIndex() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField("title", "Apache Lucene", Field.Store.YES));
        doc.add(new TextField("content", "Lucene is a powerful text search engine library.", Field.Store.YES));
        doc.add(new NumericDocValuesField("price", 150L)); // 使用 NumericDocValuesField 存储数值

        indexWriter.addDocument(doc);
        indexWriter.close();
    }
}