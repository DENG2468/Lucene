import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Paths;


//生成索引
public class Test {
    //生成索引的存放位置
    private final static String IDX_DIR = "D:\\codeNeed\\lucene\\lucene8.3.0";
    public static void main(String[] args) {
        IndexWriter indexWriter = null;
        try {
            //创建索引库
            Directory dir = FSDirectory.open(Paths.get(IDX_DIR));
            //新建分析器对象
            Analyzer analyzer = new SmartChineseAnalyzer();
            //新建配置对象
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            //创建一个IndexWriter对象
            indexWriter = new IndexWriter(dir, config);
            //创建文档对象
            Document document1 = new Document();
            //添加文档字段,字段名可以自己指定,Store.YES 说明该字段要被存储,false则不需要被存储到文档列表
            document1.add(new StringField("id", "1L", Field.Store.YES));
            document1.add(new TextField("title", "三旬老汉隔人暴扣", Field.Store.YES));
            document1.add(new TextField("content", "某23号三旬老汉哥一记抢断之后运球过人后隔人劈扣拿下2分!", Field.Store.YES));

            Document document2 = new Document();
            document2.add(new StringField("id", "2L", Field.Store.YES));
            document2.add(new TextField("title", "浓眉哥怒砍3双", Field.Store.YES));
            document2.add(new TextField("content", "浓眉哥本场状态佳,仅前三节比赛就拿下3双,将比赛胜局牢牢锁定!", Field.Store.YES));

            //写出索引数据
            indexWriter.addDocument(document1);
            indexWriter.addDocument(document2);
            indexWriter.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
