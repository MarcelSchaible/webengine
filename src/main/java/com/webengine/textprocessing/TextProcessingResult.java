package com.webengine.textprocessing;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TextProcessingResult {
	/**
	 * where to find the document.
	 */
	private String uri;
	
	/**
	 * title of the document
	 */
	private String title;
	/**
	 * language of the document.
	 */
	private String language;
	/**
	 * hash of the document.
	 */
	private String hash;
	/**
	 * Sentences of the document.
	 */
	private List<Sentence> sentences = new ArrayList<Sentence>();
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}
	/**
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	/**
	 * @return the sentences
	 */
	public List<Sentence> getSentences() {
		return sentences;
	}
	/**
	 * @param sentences the sentences to set
	 */
	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

}
