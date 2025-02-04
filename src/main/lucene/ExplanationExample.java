import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class ExplanationExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/luceneResult";

    public static void main(String[] args) throws IOException, ParseException {
        // 创建索引
        createIndex();

        // 打开索引
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // 解析查询
        String queryString = "Lucene";
        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        Query query = parser.parse(queryString);

        // 执行搜索并获取解释
        TopDocs topDocs = searcher.search(query, 10);
        ScoreDoc scoreDoc = topDocs.scoreDocs[0];
        Explanation explanation = searcher.explain(query, scoreDoc.doc);

        System.out.println("Explanation for the top score:");
        System.out.println(explanation.toString());

        reader.close();
        directory.close();
    }

    private static void createIndex() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField("content", "Lucene is a powerful text search engine library.", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.close();
    }
}
