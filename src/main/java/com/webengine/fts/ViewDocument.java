package com.webengine.fts;

import com.webengine.messages.Entry;

public class ViewDocument implements Comparable{

	private String href;
	private String content;
	private String title;
	private boolean centroidsearch=false;
	private float score;
	//private String SourcePeer;
	
	private String halloballo = "hallo";
	
	private long lastModified;
	
	
	
	
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float curscore) {
		this.score = curscore;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public boolean getCentroidsearch() {
		return centroidsearch;
	}
	
	public void setCentroidsearch(boolean centroidsearch) {
		this.centroidsearch = centroidsearch;
	}
	
	
	//public String getSourcePeer() {
	//	return SourcePeer;
	//}
	//public void setSourcePeer(String peername) {
	//	this.SourcePeer = peername;
	//}
	
	
	
	public String getHalloballo() {
		return halloballo;
	}
	
	public int compareTo(Object other){
		
		ViewDocument otherdoc = (ViewDocument) other;
		
		
		if (this.score == otherdoc.getScore())
	        return 0;
	    else if (this.score > otherdoc.getScore())
	        return -1;
	    else
	        return 1;
	    }
		
		
	  
	
	
}
