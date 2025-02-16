package org.apache.lucene.demo.xmlparser;


import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.xml.CorePlusExtensionsParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.w3c.dom.Document;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class FormBasedXmlQueryDemo extends HttpServlet {
	//private KoreanAnalyzer queryTemplateManager;

	private CorePlusExtensionsParser xmlParser;

	private IndexSearcher searcher;

	private Analyzer analyzer = new StandardAnalyzer();

	public FormBasedXmlQueryDemo() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			openExampleIndex();
			String xslFile = config.getInitParameter("xslFile");
			String defaultStandardQueryParserField = config.getInitParameter("defaultStandardQueryParserField");
			//queryTemplateManager = new KoreanAnalyzer();
			xmlParser = new CorePlusExtensionsParser(defaultStandardQueryParserField, analyzer);
		} catch (Exception e) {
			throw new ServletException("Error loading query template", e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Properties completedFormFields = new Properties();
		Enumeration<?> pNames = request.getParameterNames();
		while (pNames.hasMoreElements()) {
			String propName = ((String) (pNames.nextElement()));
			String value = request.getParameter(propName);
			if ((value != null) && ((value.trim().length()) > 0)) {
				completedFormFields.setProperty(propName, value);
			}
		} 
		try {
			Document xmlQuery = getQueryAsDOM(completedFormFields);
			Query query = xmlParser.getQuery(xmlQuery.getDocumentElement());
			TopDocs topDocs = searcher.search(query, 10);
			if (topDocs != null) {
				ScoreDoc[] sd = topDocs.scoreDocs;
				org.apache.lucene.document.Document[] results = new org.apache.lucene.document.Document[sd.length];
				for (int i = 0; i < (results.length); i++) {
					results[i] = searcher.doc(sd[i].doc);
					request.setAttribute("results", results);
				}
			}
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
			dispatcher.forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Error processing query", e);
		}
	}

	private Document getQueryAsDOM(Properties completedFormFields) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			// 创建根元素 <query>
			Element queryElement = doc.createElement("query");
			doc.appendChild(queryElement);

			// 遍历 Properties 对象中的每个条目并创建 <field> 元素
			for (String propName : completedFormFields.stringPropertyNames()) {
				String value = completedFormFields.getProperty(propName);
				Element fieldElement = doc.createElement("field");
				fieldElement.setAttribute("name", propName);
				fieldElement.setAttribute("value", value);
				queryElement.appendChild(fieldElement);
			}

			return doc;
		} catch (Exception e) {
			// 处理可能的解析错误
			e.printStackTrace();
			return null;
		}
	}
	private void openExampleIndex() throws IOException {
		ByteBuffersDirectory rd = new ByteBuffersDirectory();
		IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(rd, iwConfig);
		InputStream dataIn = getServletContext().getResourceAsStream("/WEB-INF/data.tsv");
		BufferedReader br = new BufferedReader(new InputStreamReader(dataIn, StandardCharsets.UTF_8));
		String line = br.readLine();
		final FieldType textNoNorms = new FieldType(TextField.TYPE_STORED);
		textNoNorms.setOmitNorms(true);
		while (line != null) {
			line = line.trim();
			if ((line.length()) > 0) {
				StringTokenizer st = new StringTokenizer(line, "\t");
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("location", st.nextToken(), textNoNorms));
				doc.add(new Field("salary", st.nextToken(), textNoNorms));
				doc.add(new Field("type", st.nextToken(), textNoNorms));
				doc.add(new Field("description", st.nextToken(), textNoNorms));
				writer.addDocument(doc);
			}
			line = br.readLine();
		} 
		writer.close();
		IndexReader reader = DirectoryReader.open(rd);
		searcher = new IndexSearcher(reader);
	}
}

