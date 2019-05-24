package com.webengine;

import java.util.List;
import java.util.Set;

/**
 * This interface contains methods to manage the known list of Neighbours
 * 
 * @author robert
 *
 */
public interface INeighbourhoodService {

	/**
	 * add a host to the list aof candidates
	 * 
	 * this list will be processed periodically. Candidates that contain a
	 * running peer are moved to neighbours list, other ones are moved to
	 * blacklist.
	 * 
	 * @param host
	 */
	public void addCandidate(String host);

	/**
	 * 
	 * @return all valid neighbours (checkCandidata returned true)
	 */
	public List<String> getNeighbours();

	/***
	 * host http://<host>:8080/ping.html, returns
	 * 
	 * @param
	 * @return true in case there is a peer availale at the given host,
	 *         otherwise false
	 */
	public boolean checkCandidate(String host);
	
	//public String getLocalIPAddress();

	public String getPreferredLocalIPAddress();

	public boolean callURL(String host, boolean b);
	
	
	
	double getmeantermendistance();
	
	
	double getmingraphendistance();
	
	double getmaxgraphendistance();
	
	
	
	
	double getmeantermdedistance();
	
	
	double getmingraphdedistance();
	
	double getmaxgraphdedistance();

	
	public void addNeighbour(String remoteip);

	public boolean initialised();

	public boolean checkinggraphdb();

	public void checkGraphDB();

	public String getPreferredInternalLocalIPAddress();

	public void startCheckGraphDatabasesTask();
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
