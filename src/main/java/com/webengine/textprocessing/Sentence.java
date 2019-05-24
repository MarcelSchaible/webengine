package com.webengine.textprocessing;

import java.util.ArrayList;
import java.util.Collection;

public class Sentence {

	private Collection<Word> words = new ArrayList<Word>();

	/**
	 * @return the words
	 */
	public Collection<Word> getWords() {
		return words;
	}

	/**
	 * @param words the words to set
	 */
	public void setWords(Collection<Word> words) {
		this.words = words;
	}
	
	
}
