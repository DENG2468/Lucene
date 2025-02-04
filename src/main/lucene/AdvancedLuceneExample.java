import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

public class AdvancedLuceneExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/luceneResult";

    public static void main(String[] args) throws IOException, ParseException {
        // 创建索引
        createIndex();

        // 搜索
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        // 解析查询
        String queryString = "Lucene";
        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse(queryString);

        // 构建布尔查询
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query, BooleanClause.Occur.SHOULD);

        // 添加范围查询
        TermRangeQuery rangeQuery = new TermRangeQuery(
                "price",
                new BytesRef("100"), // 将字符串转换为 BytesRef
                new BytesRef("200"), // 将字符串转换为 BytesRef
                true, true // includeLower 和 includeUpper
        );
        booleanQuery.add(rangeQuery, BooleanClause.Occur.SHOULD);

        // 添加短语查询
        BooleanQuery.Builder phraseQueryBuilder = new BooleanQuery.Builder();
        TermRangeQuery phraseRangeQuery = new TermRangeQuery(
                "title",
                new BytesRef("apache"), // 将字符串转换为 BytesRef
                new BytesRef("lucene"), // 将字符串转换为 BytesRef
                true, false // includeLower 和 includeUpper
        );
        phraseQueryBuilder.add(phraseRangeQuery, BooleanClause.Occur.SHOULD);
        BooleanQuery phraseQuery = phraseQueryBuilder.build();
        booleanQuery.add(phraseQuery, BooleanClause.Occur.SHOULD);

        // 执行搜索
        TopDocs results = searcher.search(booleanQuery.build(), 10);

        // 打印结果
        for (ScoreDoc hit : results.scoreDocs) {
            Document doc = searcher.doc(hit.doc);
            System.out.println("Title: " + doc.get("title") + ", Score: " + hit.score);
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
        doc.add(new TextField("price", "150", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.close();
    }
}
