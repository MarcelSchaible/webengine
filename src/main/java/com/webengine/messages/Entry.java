package com.webengine.messages;

/**
 * this class is the representation of a document entry on a RingPeer
 * 
 * @author mario
 *
 */
public class Entry implements Comparable{

	private String key; //centroid of document D
	private String value; //URL of document D

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry other = (Entry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		
		return true;
	}
	
	
	
	
	
	
	
	
	public Entry(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * a unique id of the message
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * ttl of the search request
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	public Entry() {
		super();
	}
	
	public int compareTo(Object other){
		
		Entry otherentry = (Entry) other;
		
	    return this.key.compareTo(otherentry.key);
	  }

}
