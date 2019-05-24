package com.webengine.textprocessing;

public class Word {
	
/** the stemmed word.*/
private String stemmedWord;
/** tag of the word*/
private String tag;

/**
 * Getter for stemmedWord.
 * @return
 */
public String getStemmedWord() {
	return stemmedWord;
}
/**
 * Setter for stemmedWord.
 * @return
 */
public void setStemmedWord(String stemmedWord) {
	this.stemmedWord = stemmedWord;
}
/**
 * @return the tag
 */
public String getTag() {
	return tag;
}
/**
 * @param tag the tag to set
 */
public void setTag(String tag) {
	this.tag = tag;
}


}
