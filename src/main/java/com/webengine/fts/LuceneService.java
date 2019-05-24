package com.webengine.fts;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webengine.ILocalSearchService;
import com.webengine.messages.Entry;


/**
 * 
 * <p>
 * 
 */
@Service
public class LuceneService implements ILocalSearchService {

	private final static Logger LOGGER = Logger.getLogger(LuceneService.class.getName());

	@Value("${config.search.indexDir}")
	private String indexDir;
	
	@Value("${peer.name}")
	private String peername;
	
	private int addedDocs=-1;
	
	@Autowired
	StandardAnalyzer analyzer;

	@Autowired
	private IndexWriter writer;

	//@Autowired
	//
	
	private DirectoryReader reader;

	private IndexSearcher searcher;
	private IndexReader ireader;

	@Autowired
	private QueryParser parser;

	@Autowired
	ServletContext context;

	/** Index all text files under a directory. */
	public static void main(String[] args) {

		//ILocalSearchService f = new LuceneService();
		//f.addDocument("Tested", "file://test.txt", "TestDatei");

	}

	public LuceneService() {

	}

	/**
	 * initialize the lucene indexer
	 */
//	@PostConstruct
	//
	public void init() {
		LOGGER.info("-> init()");
		LOGGER.info("Using " + indexDir+ " for lucene index ");
		
	
		searcher = buildSearcher();
			
		
		
		LOGGER.info("<- init()");
	}

	private IndexSearcher buildSearcher() {
		
		IndexSearcher is = null;
		
		
		if (reader == null) {
			
			Path p = Paths.get(indexDir);
			
			Directory id;
			try {
				id = FSDirectory.open(p); 

				
				
				
				if (DirectoryReader.indexExists(id))
				reader = DirectoryReader.open(id);
				
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
		}
		
		
		if (reader!=null)
		try {
			is = new IndexSearcher(reader);
			} catch (Exception infexp) {
				
				LOGGER.warn("Searcher cannot be created as lucene index is empty. ");
				
			}
			
		
		
		return is;
	}

	
	
	
	/* (non-Javadoc)
	 * @see com.webengine.ILocalSearchService#addDocument(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void addDocument(String content, String uri, String title, String centroid) {
		LOGGER.info("-> addDocument()");
		try {
			indexDoc(writer, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), uri, 32, title, centroid);
			writer.commit();
			
			if(addedDocs==-1) {
			addedDocs=addedDocs+2;;
			} else {
				
				addedDocs++;
			}
			
		} catch (Exception e) {
			LOGGER.warn(String.format("Error occured during adding %s ", uri), e);
		}
		LOGGER.info("<- addDocument()");
	}

	/* (non-Javadoc)
	 * @see com.webengine.ILocalSearchService#search(java.lang.String)
	 */
	@Override
	public List<ViewDocument> search(String queryString) throws IOException {
		LOGGER.info(String.format("-> search(queryString: %s)", queryString));

		
		List<ViewDocument> result = new ArrayList<ViewDocument>();
		
		try {
		
		if (reader==null)
		init();

	//	try {
			Date start = new Date();
			Query query = parser.parse(queryString);
			
			
			if (reader!=null) {
				DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
				if (newReader != null) {
					LOGGER.info("update searcher according to change in reader");
					reader = newReader;
					searcher = buildSearcher();
				}
			}
			
			
			if (searcher!=null) {
			
				TopDocs docs = searcher.search(query, Integer.MAX_VALUE);
				Date end = new Date();
				LOGGER.info("Time: " + (end.getTime() - start.getTime()) + "ms");
				for (ScoreDoc d : docs.scoreDocs) {
					Document doc = searcher.doc(d.doc);
					//d.score
					
					
					ViewDocument vd = new ViewDocument();
					vd.setHref(doc.get("path"));
					String title = doc.get("title");
					if (title == null || title.trim().length() == 0) {
						title = "Link";
					}
					vd.setTitle(title);
					vd.setScore(d.score);
					
					if (!peername.equals("")) {
						
						if (peername.length()>20) 
							peername=peername.substring(0,16)+"...";
						
						vd.setTitle(peername+": " +title);
						
					//	vd.setSourcePeer(peername);
						
					}
					
					if (result.size()<10)
					result.add(vd);
				}
			
			}
			
		} catch (Exception e) {
			LOGGER.warn(String.format("unable to parse or search for search string: %s", queryString), e);
		}
		LOGGER.info("<- search(): " + result.size());
		return result;
	}

	
	public Vector<Entry> getLocalEntries() {
		
		LOGGER.info("Generating local entries from lucene index");
		
		Vector<Entry> localentries = new Vector<Entry>();
		
		try {
		
		if (reader==null)
			init();
		
		if (reader!=null) {

		//DirectoryReader newReader;
	//	try {
		DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
			if (newReader != null) {
				LOGGER.info("update searcher according to change in reader");
				reader = newReader;
				searcher = buildSearcher();
			}
	//	} catch (Exception e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
		}
		
		if (searcher!=null) {
		
		//try {
			IndexReader ireader = searcher.getIndexReader();
			
			int maxDoc = ireader.maxDoc();
			
			LOGGER.info("# of documents found: " +maxDoc);
			
			for (int i=0; i<maxDoc; i++)
			{
			     Document doc = ireader.document(i);

			     String centroid = doc.get("centroid");
			     String path = doc.get("path");
			     
			     LOGGER.info("Entry: " +centroid + " ### " +path);
			     
			     
			     if ((centroid != null) && (!centroid.equals("null")) && (centroid.trim().length() > 0) && (path!=null)) {
			    	 
			    	 centroid=centroid.trim();
			    	 
			    	 centroid= Character.toLowerCase(centroid.charAt(0)) + centroid.substring(1).toLowerCase();
			    	 
			    	 Entry newentry = new Entry(centroid, path);
			    	 
			    	 if (!localentries.contains(newentry))
			    	 localentries.add(newentry);
			    	 
			     }
			     
			     
			     
			}
			
			//09.02.19 ggf. memory leak
			//ireader.close();
			
		}
			
		} catch (Exception exp) {
			
			LOGGER.warn(String.format("unable to retrieve local entries"), exp);
			
		}
		
		
		
		return localentries;
		
	}
	
	
	
	
public int getNumberDocuments() {
		
		
		return addedDocs;
		
	}
	
	
	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For
	 * good throughput, put multiple documents into your input file(s). An
	 * example of this is in the benchmark module, which can create "line doc"
	 * files, one document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param path
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */

	/** Indexes a single document */
	void indexDoc(IndexWriter writer, InputStream content, String uri, long lastModified, String title, String centroid)
			throws IOException {

		// make a new, empty document
		Document doc = new Document();

		// Add the path of the file as a field named "path". Use a
		// field that is indexed (i.e. searchable), but don't tokenize
		// the field into separate words and don't index term frequency
		// or positional information:
		Field pathField = new StringField("path", uri, Field.Store.YES);
		if (title != null) {
			Field titleField = new StringField("title", title, Field.Store.YES);
			doc.add(titleField);
			
		    String[] words = title.split(" ");
		    //centroid -> words[0]
			
			//real field
			
			
		}
		
		Field centroidField = new StringField("centroid", centroid, Field.Store.YES);
		doc.add(centroidField);
		
		doc.add(pathField);

		// Add the last modified date of the file a field named "modified".
		// Use a LongField that is indexed (i.e. efficiently filterable with
		// NumericRangeFilter). This indexes to milli-second resolution, which
		// is often too fine. You could instead create a number based on
		// year/month/day/hour/minutes/seconds, down the resolution you require.
		// For example the long value 2011021714 would mean
		// February 17, 2011, 2-3 PM.
		doc.add(new LongField("modified", lastModified, Field.Store.NO));

		// Add the contents of the file to a field named "contents". Specify a
		// Reader,
		// so that the text of the file is tokenized and indexed, but not
		// stored.
		// Note that FileReader expects the file to be in UTF-8 encoding.
		// If that's not the case searching for special characters will fail.
		doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8))));

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			// New index, so we just add the document (no old document can be
			// there):
			LOGGER.info("adding " + uri);
			writer.addDocument(doc);
		} else {
			// Existing index (an old copy of this document may have been
			// indexed) so
			// we use updateDocument instead to replace the old one matching the
			// exact
			// path, if present:
			LOGGER.info("updating " + uri);
			writer.updateDocument(new Term("path", uri.toString()), doc);
		}
	}
}
