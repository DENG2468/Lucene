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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HighlightExample {
    private static final String INDEX_DIR = "D:/projectNeed/lucene/luceneResult";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TITLE = "title";

    public static void main(String[] args) throws IOException, InvalidTokenOffsetsException, ParseException {
        // 创建索引
        createIndex();

        // 搜索
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        // 解析查询
        String queryString = "Lucene";
        String defaultField = FIELD_CONTENT; // 使用实际的字段名作为默认搜索字段
        QueryParser parser = new QueryParser(defaultField, analyzer);
        parser.setAnalyzer(analyzer); // 设置分析器
        Query query = parser.parse(queryString);

        // 排序
        Sort sort = new Sort(new SortField(FIELD_TITLE, SortField.Type.STRING, true));
        TopFieldCollector collector = TopFieldCollector.create(sort, 10, 10);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 高亮显示
        QueryScorer queryScorer = new QueryScorer(query, FIELD_CONTENT);
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), queryScorer);
        StringBuilder sb = new StringBuilder();
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            String content = doc.get(FIELD_CONTENT);
            String highlightedContent = highlighter.getBestFragment(analyzer, FIELD_CONTENT, content);
            sb.append("<p>").append(highlightedContent).append("</p>");
        }

        System.out.println(sb.toString());
        reader.close();
        directory.close();
    }

    private static void createIndex() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField(FIELD_TITLE, "Lucene Introduction", Field.Store.YES));
        doc.add(new TextField(FIELD_CONTENT, "Lucene is a powerful text search engine library.", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new TextField(FIELD_TITLE, "Apache Lucene", Field.Store.YES));
        doc.add(new TextField(FIELD_CONTENT, "Apache Lucene is a high-performance, scalable information retrieval library.", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.close();
    }
}