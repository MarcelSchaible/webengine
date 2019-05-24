package com.webengine;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.webengine.fts.ViewDocument;
import com.webengine.messages.Entry;

public interface ILocalSearchService {

	/**
	 * add a document to the index (if the index already contains this document
	 * it will be updated)
	 * 
	 * @param content
	 *            content of the document
	 * @param uri
	 *            uri of the document
	 * @param title
	 *            title of the document
	 */
	void addDocument(String content, String uri, String title, String centroid);

	/**
	 * search the lucene index
	 * 
	 * @param queryString
	 *            search string
	 * @return list of documents in the lucen index maching the search string
	 * @throws IOException
	 */
	List<ViewDocument> search(String queryString) throws IOException;

	public int getNumberDocuments();
	
	public Vector<Entry> getLocalEntries();
	
}