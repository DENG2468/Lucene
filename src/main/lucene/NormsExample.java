import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class NormsExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/normsIndex";

    public static void main(String[] args) throws IOException {
        // 创建索引
        createIndex();

        // 打开索引
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // 执行搜索
        Query query = new MatchAllDocsQuery(); // 匹配所有文档的查询
        TopDocs results = searcher.search(query, 10);

        // 打印结果
        System.out.println("Documents with norms:");
        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = searcher.doc(hit.doc);
            String normValue = doc.get("norms");
            System.out.println("Norms: " + normValue);
        }

        reader.close();
        directory.close();
    }

    private static void createIndex() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // 设置字段的索引选项为存储规范化值
        FieldType normsType = new FieldType();
        normsType.setTokenized(true);
        normsType.setIndexOptions(IndexOptions.DOCS);
        normsType.setStoreTermVectors(true);

        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("content", "Lucene is a powerful text search engine library.", Field.Store.YES));
        doc.add(new Field("norms", "123", normsType)); // 存储规范化值

        indexWriter.addDocument(doc);
        indexWriter.close();
    }
}
