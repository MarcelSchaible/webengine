package com.webengine.messages;

/**
 * this class is the representation of a search request
 * 
 * @author robert
 *
 */
public class SearchRequest {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((askingPeer == null) ? 0 : askingPeer.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((searchString == null) ? 0 : searchString.hashCode());
		result = prime * result + ttl;
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
		SearchRequest other = (SearchRequest) obj;
		if (askingPeer == null) {
			if (other.askingPeer != null)
				return false;
		} else if (!askingPeer.equals(other.askingPeer))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (searchString == null) {
			if (other.searchString != null)
				return false;
		} else if (!searchString.equals(other.searchString))
			return false;
		if (ttl != other.ttl)
			return false;
		return true;
	}

	private String id;
	private int ttl;
	private String searchString;
	private String askingPeer;
	private byte direction;
	private String centroid="";
	
	
	public SearchRequest(String id, int ttl, String searchString) {
		super();
		this.id = id;
		this.ttl = ttl;
		this.searchString = searchString;
	}

	/**
	 * a unique id of the message
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	/**
	 * ttl of the search request
	 * @return
	 */
	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * 
	 * @return the peer that initially produced the search request
	 */
	public String getAskingPeer() {
		return askingPeer;
	}

	public void setAskingPeer(String askingPeer) {
		this.askingPeer = askingPeer;
	}

	
	public byte getDirection() {
		return direction;
	}

	public void setDirection(byte direction) {
		this.direction = direction;
	}
	
	
	public String getCentroid() {
		return centroid;
	}

	public void setCentroid(String centroid) {
		this.centroid = centroid;
	}
	
	
	public SearchRequest() {
		super();
	}

}
