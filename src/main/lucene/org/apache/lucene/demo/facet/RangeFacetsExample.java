package org.apache.lucene.demo.facet;


import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.Closeable;
import java.io.IOException;

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE;


public class RangeFacetsExample implements Closeable {
	private final Directory indexDir = new ByteBuffersDirectory();

	private IndexSearcher searcher;

	private final long nowSec = System.currentTimeMillis();

	final LongRange PAST_HOUR = new LongRange("Past hour", ((nowSec) - 3600), true, nowSec, true);

	final LongRange PAST_SIX_HOURS = new LongRange("Past six hours", ((nowSec) - (6 * 3600)), true, nowSec, true);

	final LongRange PAST_DAY = new LongRange("Past day", ((nowSec) - (24 * 3600)), true, nowSec, true);

	public RangeFacetsExample() {
	}

	public void index() throws IOException {
		IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(CREATE));
		for (int i = 0; i < 100; i++) {
			Document doc = new Document();
			long then = (nowSec) - (i * 1000);
			doc.add(new NumericDocValuesField("timestamp", then));
			doc.add(new LongPoint("timestamp", then));
			indexWriter.addDocument(doc);
		}
		searcher = new IndexSearcher(DirectoryReader.open(indexWriter));
		indexWriter.close();
	}

	private FacetsConfig getConfig() {
		return new FacetsConfig();
	}

	public FacetResult search() throws IOException {
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);
		Facets facets = new LongRangeFacetCounts("timestamp", fc, PAST_HOUR, PAST_SIX_HOURS, PAST_DAY);
		return facets.getTopChildren(10, "timestamp");
	}

	public TopDocs drillDown(LongRange range) throws IOException {
		DrillDownQuery q = new DrillDownQuery(getConfig());
		q.add("timestamp", LongPoint.newRangeQuery("timestamp", range.min, range.max));
		return searcher.search(q, 10);
	}

	public DrillSideways.DrillSidewaysResult drillSideways(LongRange range) throws IOException {
		DrillDownQuery q = new DrillDownQuery(getConfig());
		q.add("timestamp", LongPoint.newRangeQuery("timestamp", range.min, range.max));
		DrillSideways.DrillSidewaysResult result = new DrillSideways(searcher, getConfig(), null, null) {
			@Override
			protected Facets buildFacetsResult(FacetsCollector drillDowns, FacetsCollector[] drillSideways, String[] drillSidewaysDims) throws IOException {
				assert drillSidewaysDims[0].equals("timestamp");
				return new LongRangeFacetCounts("timestamp", drillSideways[0], PAST_HOUR, PAST_SIX_HOURS, PAST_DAY);
			}
		}.search(q, 10);
		return result;
	}

	@Override
	public void close() throws IOException {
		searcher.getIndexReader().close();
		indexDir.close();
	}

	public static void main(String[] args) throws Exception {
		RangeFacetsExample example = new RangeFacetsExample();
		example.index();
		System.out.println("Facet counting example:");
		System.out.println("-----------------------");
		System.out.println(example.search());
		System.out.println("\n");
		System.out.println("Facet drill-down example (timestamp/Past six hours):");
		System.out.println("---------------------------------------------");
		TopDocs hits = example.drillDown(example.PAST_SIX_HOURS);
		System.out.println(((hits.totalHits) + " totalHits"));
		System.out.println("\n");
		System.out.println("Facet drill-sideways example (timestamp/Past six hours):");
		System.out.println("---------------------------------------------");
		DrillSideways.DrillSidewaysResult sideways = example.drillSideways(example.PAST_SIX_HOURS);
		System.out.println(((sideways.hits.totalHits) + " totalHits"));
		System.out.println(sideways.facets.getTopChildren(10, "timestamp"));
		example.close();
	}
}

