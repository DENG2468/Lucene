package org.apache.lucene.demo.facet;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.*;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE;

// 主类
public class AssociationsFacetsExample {
	// 使用内存目录来存储索引和分类（taxonomy）数据
	private final Directory indexDir = new RAMDirectory();
	private final Directory taxoDir = new RAMDirectory();

	// 配置Facets
	private final FacetsConfig config;

	// 构造函数，初始化FacetsConfig
	public AssociationsFacetsExample() {
		config = new FacetsConfig();
		// 设置字段"tags"为多值字段
		config.setMultiValued("tags", true);
		// 设置字段"tags"的索引字段名为"$tags"
		config.setIndexFieldName("tags", "$tags");
		// 设置字段"genre"为多值字段
		config.setMultiValued("genre", true);
		// 设置字段"genre"的索引字段名为"$genre"
		config.setIndexFieldName("genre", "$genre");
	}

	// 创建索引的方法
	private void index() throws IOException {
		// 配置IndexWriter
		IndexWriterConfig iwc = new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(CREATE);
		IndexWriter indexWriter = new IndexWriter(indexDir, iwc);
		DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

		// 创建文档并添加到索引
		Document doc = new Document();
		doc.add(new IntAssociationFacetField(3, "tags", "lucene"));
		doc.add(new FloatAssociationFacetField(0.87F, "genre", "computing"));
		indexWriter.addDocument(config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new IntAssociationFacetField(1, "tags", "lucene"));
		doc.add(new IntAssociationFacetField(2, "tags", "solr"));
		doc.add(new FloatAssociationFacetField(0.75F, "genre", "computing"));
		doc.add(new FloatAssociationFacetField(0.34F, "genre", "software"));
		indexWriter.addDocument(config.build(taxoWriter, doc));

		// 关闭IndexWriter和DirectoryTaxonomyWriter
		indexWriter.close();
		taxoWriter.close();
	}

	// 汇总关联面度的方法
	private List<FacetResult> sumAssociations() throws IOException {
		// 打开索引和分类读取器
		DirectoryReader indexReader = DirectoryReader.open(indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir);

		// 收集面度数据
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);

		// 创建Facets对象
		Facets tags = new TaxonomyFacetSumIntAssociations("$tags", taxoReader, config, fc);
		Facets genre = new TaxonomyFacetSumFloatAssociations("$genre", taxoReader, config, fc);

		// 存储结果
		List<FacetResult> results = new ArrayList<>();
		results.add(tags.getTopChildren(10, "tags"));
		results.add(genre.getTopChildren(10, "genre"));

		// 关闭读取器
		indexReader.close();
		taxoReader.close();
		return results;
	}

	// 钻取（drill down）面度的方法
	private FacetResult drillDown() throws IOException {
		// 打开索引和分类读取器
		DirectoryReader indexReader = DirectoryReader.open(indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir);

		// 创建钻取查询
		DrillDownQuery q = new DrillDownQuery(config);
		q.add("tags", "solr");

		// 收集面度数据
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, q, 10, fc);

		// 创建Facets对象
		Facets facets = new TaxonomyFacetSumFloatAssociations("$genre", taxoReader, config, fc);

		// 获取结果
		FacetResult result = facets.getTopChildren(10, "genre");

		// 关闭读取器
		indexReader.close();
		taxoReader.close();
		return result;
	}

	// 运行汇总关联面度的方法
	public List<FacetResult> runSumAssociations() throws IOException {
		index();
		return sumAssociations();
	}

	// 运行钻取面度的方法
	public FacetResult runDrillDown() throws IOException {
		index();
		return drillDown();
	}

	// 主方法，用于演示
	public static void main(String[] args) throws Exception {
		System.out.println("Sum associations example:");
		System.out.println("-------------------------");
		List<FacetResult> results = new AssociationsFacetsExample().runSumAssociations();
		System.out.println(("tags: " + (results.get(0))));
		System.out.println(("genre: " + (results.get(1))));
	}
}