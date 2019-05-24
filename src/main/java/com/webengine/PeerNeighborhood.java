package com.webengine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.webengine.crawler.IpManager;
import com.webengine.graph.GraphAPI_DE;
import com.webengine.graph.GraphAPI_EN;
import com.webengine.crawler.*;


import java.net.DatagramSocket;


/*
 * 
 * <dependency>
    		<groupId>javax.json</groupId>
    		<artifactId>javax.json-api</artifactId>
    		<version>1.1.2</version>
		</dependency>
		
		<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>servlet-api</artifactId>
			<version>2.5</version>
		</dependency>
		
		
 */

@Component
public class PeerNeighborhood implements INeighbourhoodService {

	private final static Logger LOGGER = Logger.getLogger(PeerNeighborhood.class.getName());

	@Value("${neighbours.filename}")
	private String neighboursfilename;

	@Value("${blackList.filename}")
	private String blackListfilename;

	@Value("${candidates.filename}")
	private String candidatesfilename;
	
	@Value("${peer.maxneighbours}")
	private int maxneighbours;

	@Value("${config.crawler.graphdb}")
	private int createGraphDatabase;
	
	@Value("${peer.networkmode}")
	private int networkmode;
	
	
	
	@Autowired
	private ServiceHelper helper;
	
	@Autowired
	private ILocalSearchService luceneService;
	
	@Autowired
	private Crawler crawlerService;

	private List<String> blacklist = new ArrayList<String>();
	private List<String> neighbors = new ArrayList<String>();
	private List<String> fixedneighbors = new ArrayList<String>();
	private List<String> candidates = new ArrayList<String>();
	
	
	
	@Value("${config.crawler.localHost}")
	private String localip;
	
	private String localipinternal = "";
	
	
	@Autowired
	private IpManager ipManager;
	
	Timer checkGraphTimer;
	CheckGraphDatabasesTask checkGraphThread;
	
	
	Timer checkIndexGraphTimer = new Timer();  
	
	
	
	double meantermendistance = Double.MAX_VALUE;
	
	double mingraphendistance = -1;
	double maxgraphendistance = -1;
	
	
	double meantermdedistance = Double.MAX_VALUE;
	
	double mingraphdedistance = -1;
	double maxgraphdedistance = -1;
	
	boolean firstCheckDone = false;
	boolean checkinggraphdb = false;
	
	public double getmeantermendistance() {
		
		return meantermendistance;

	}
	
	
	public double getmingraphendistance() {
		
		return mingraphendistance;

	}
	
	public double getmaxgraphendistance() {
		
		return maxgraphendistance;

	}
	
	
	
	
	public double getmeantermdedistance() {
		
		return meantermdedistance;

	}
	
	
	public double getmingraphdedistance() {
		
		return mingraphdedistance;

	}
	
	public double getmaxgraphdedistance() {
		
		return maxgraphdedistance;

	}
	
	
	
	
	class CheckIndexGraphTask extends TimerTask {

    	
    	
		CheckIndexGraphTask( ){
    		
    		
    	}
    	
    	
        @Override
        public void run() {
            LOGGER.info("CheckIndexGraphTask is started.");

            
            if (crawlerService!=null   && luceneService!=null) {
            	
            	List maxseeds = crawlerService.getFirstSeeds();
            	
            	
            	LOGGER.info("Index size: " + luceneService.getNumberDocuments() + "   Max First Seeds: " +maxseeds.size() + "   Added Seeds: " + crawlerService.getAddedSeeds().size());
            	
            	            	
            	
            }
            
            LOGGER.info("CheckIndexGraphTask ended.");   
			
        }
        
    }
	
	
	
	class CheckGraphDatabasesTask extends Thread { //extends TimerTask {

    	
    	
    	CheckGraphDatabasesTask( ){
    		
    		
    	}
    	
    	
        @Override
        public void run() {
            LOGGER.info("CheckGraphDatabasesTask is started.");

            boolean forcecheck = false;	
            
            if (crawlerService!=null   && luceneService!=null) {
            	
            	List maxseeds = crawlerService.getFirstSeeds();
            	
            	
            	LOGGER.info("Index size: " + luceneService.getNumberDocuments() + "   Max First Seeds: " +maxseeds.size() + "   Added Seeds: " + crawlerService.getAddedSeeds().size());
            	
            	
	            	if ( ( /* (luceneService.getNumberDocuments()>=maxseeds.size()) || */ (crawlerService.firstRunFinished()==true))  /*&& (crawlerService.getController()!=null)   && (crawlerService.getController().isFinished() )*/                && firstCheckDone==false) {
	            		
	                	//firstCheckDone=true;
	                	
	                	//crawlerService.getGraphApi_DE().restartDB();
	                	//crawlerService.getGraphApi_EN().restartDB();
	                	
	                	
	                	load();
	                	
	                	firstCheckDone=true;
	                	forcecheck=true;
	                }
            	
            	
            }
            
            
            if (((crawlerService.docheckdb()==true) && (firstCheckDone==true)) || (forcecheck==true)   ) {
            	
            	if (!checkinggraphdb()) { //neu
            		checkGraphDB(); 
            		crawlerService.setdocheckdb(false);
            	}
            	
            }
            	
            
            
            /*
            
            if (meantermendistance==Double.MAX_VALUE)
            if ((firstCheckDone==true) &&  (crawlerService.getController().isFinished()==true)  &&  (!crawlerService.preventcheckdb())  ) {
            	
            	checkinggraphdb=true;
            	
            	
            	
            	crawlerService.getGraphApi_EN().updateDiceandCosts();
            	
            	
            	LOGGER.info("Graphdatabase first run is finished.");
            	
            	
            	GraphAPI_EN graphen = crawlerService.getGraphApi_EN();
            	
            	Vector words = graphen.listNodes();
            	LOGGER.info("Words in en database: " +words.size());
            	LOGGER.info("Words in en database: " +words.toString());
            	
            	
            	HashMap <String, Double>enpairs = new HashMap<String, Double>();
            	Vector distances = new Vector();
            	
            	if (words.size()>=100) {
            		meantermendistance=0;
            		while (enpairs.keySet().size()<200) {
            			
            			Random r = new Random();
    				   	int value1 = r.nextInt(words.size());
            			
    				   	Random r2 = new Random();
    				   	int value2 = r2.nextInt(words.size());
            			
    				   	String key =  words.get(value1)+"###"+words.get(value2);
    				   	String key2 = words.get(value2)+"###"+words.get(value1);		
    				   			
    				   	if (!enpairs.containsKey(key) && !enpairs.containsKey(key2) && value1!=value2)	{
    				   		
    				   		double dist = graphen.getNodeDistance(words.get(value1).toString(), words.get(value2).toString()); //distance berechnen
    				   		
    				   		if (dist<Double.MAX_VALUE) {
    				   			Double distance = new Double(dist);
    				   			
    				   		
    				   			enpairs.put(key, distance);
    				   			distances.add(dist);
    				   			
    				   			meantermendistance+=dist;
    				   			LOGGER.info("Put Word distance: " +words.get(value1).toString() + " " + words.get(value2).toString() + " "+ dist + " " +meantermendistance);
    				   		}
    				   		
    				   	}
    				   	
    				   	
    				   	
            			
            		} //while
            		
            		meantermendistance=meantermendistance/ ((double)distances.size());
            		LOGGER.info("Mean term en distance: "+ meantermendistance);
            		
            		double sumcurdist =0;
            		
            		for (int i=0; i<distances.size(); i++) {
            			
            			double curdist = (double)distances.get(i);
            			//LOGGER.info("Curdist 1: "+ curdist);
            			
            			//LOGGER.info("curdist 2: "+ (curdist-meantermdistance) );
            			
            			curdist = (curdist-meantermendistance) * (curdist-meantermendistance);
            			
            			//LOGGER.info("curdist 3: "+ (curdist) );
            			
            			
            			sumcurdist+=curdist;
            			
            		}
            		//LOGGER.info("Sumcurdist 1: "+ sumcurdist);
            		
            		sumcurdist=sumcurdist/(double)(distances.size()-1);
            		//LOGGER.info("Sumcurdist 2: "+ sumcurdist);
            		
            		sumcurdist = Math.sqrt(sumcurdist);
            		LOGGER.info("Standard deviation: "+ sumcurdist);
            		
            		mingraphendistance=meantermendistance-(3*sumcurdist);
            		if (mingraphendistance<0) mingraphendistance=0;
            		
            		maxgraphendistance=meantermendistance+(3*sumcurdist);
            		
            		LOGGER.info("mingraphendistance maxgraphendistance "+ mingraphendistance + " " +maxgraphendistance);
            		
            	}
            	
            	checkinggraphdb = false;
            	
            } else {
            	
            	LOGGER.info("Graphdatabase first run is not finished.");
            	
            }
            
            
            
            
            if (meantermdedistance==Double.MAX_VALUE)
            	 if ((firstCheckDone==true) &&  (crawlerService.getController().isFinished()==true) &&  (!crawlerService.preventcheckdb())   ) {
                	
                	 checkinggraphdb=true;
                	
                	 crawlerService.getGraphApi_DE().updateDiceandCosts();;
                	 
                	LOGGER.info("Graphdatabase first run is finished.");
                	
                	
                	GraphAPI_DE graphde = crawlerService.getGraphApi_DE();
                	
                	Vector words = graphde.listNodes();
                	LOGGER.info("Words in de database: " +words.size());
                	LOGGER.info("Words in de database: " +words.toString());
                	
                	
                	HashMap <String, Double>enpairs = new HashMap<String, Double>();
                	Vector distances = new Vector();
                	
                	if (words.size()>=100) {
                		meantermdedistance=0;
                		while (enpairs.keySet().size()<200) {
                			
                			Random r = new Random();
        				   	int value1 = r.nextInt(words.size());
                			
        				   	Random r2 = new Random();
        				   	int value2 = r2.nextInt(words.size());
                			
        				   	String key =  words.get(value1)+"###"+words.get(value2);
        				   	String key2 = words.get(value2)+"###"+words.get(value1);		
        				   			
        				   	if (!enpairs.containsKey(key) && !enpairs.containsKey(key2) && value1!=value2)	{
        				   		
        				   		double dist = graphde.getNodeDistance(words.get(value1).toString(), words.get(value2).toString()); //distance berechnen
        				   		
        				   		if (dist<Double.MAX_VALUE) {
        				   			Double distance = new Double(dist);
        				   			
        				   		
        				   			enpairs.put(key, distance);
        				   			distances.add(dist);
        				   			
        				   			meantermdedistance+=dist;
        				   			LOGGER.info("Put Word distance: " +words.get(value1).toString() + " " + words.get(value2).toString() + " "+ dist + " " +meantermdedistance);
        				   		}
        				   		
        				   	}
        				   	
        				   	
        				   	
                			
                		} //while
                		
                		meantermdedistance=meantermdedistance/ ((double)distances.size());
                		LOGGER.info("Mean term de distance: "+ meantermdedistance);
                		
                		double sumcurdist =0;
                		
                		for (int i=0; i<distances.size(); i++) {
                			
                			double curdist = (double)distances.get(i);
                			//LOGGER.info("Curdist 1: "+ curdist);
                			
                			//LOGGER.info("curdist 2: "+ (curdist-meantermdistance) );
                			
                			curdist = (curdist-meantermdedistance) * (curdist-meantermdedistance);
                			
                			//LOGGER.info("curdist 3: "+ (curdist) );
                			
                			
                			sumcurdist+=curdist;
                			
                		}
                		//LOGGER.info("Sumcurdist 1: "+ sumcurdist);
                		
                		sumcurdist=sumcurdist/(double)(distances.size()-1);
                		//LOGGER.info("Sumcurdist 2: "+ sumcurdist);
                		
                		sumcurdist = Math.sqrt(sumcurdist);
                		LOGGER.info("Standard deviation: "+ sumcurdist);
                		
                		

                		mingraphdedistance=meantermdedistance-(3*sumcurdist);
                		if (mingraphdedistance<0) mingraphdedistance=0;
                		
                		maxgraphdedistance=meantermdedistance+(3*sumcurdist);
                		
                		
                		LOGGER.info("mingraphdedistance maxgraphdedistance "+ mingraphdedistance + " " +maxgraphdedistance);
                		
                		
                	}
                	
                	checkinggraphdb=false;
                	
                } else {
                	
                	LOGGER.info("Graphdatabase first run is not finished.");
                	
                }
            
            */

            
            
   
            LOGGER.info("CheckGraphDatabasesTask ended."); 
            
			
        }
        
    }
	
	
	
	
	public void checkGraphDB() {
		

        LOGGER.info("checkGraphDB is started.");

        
        
        double lmeantermendistance = Double.MAX_VALUE;
    	
    	double lmingraphendistance = -1;
    	double lmaxgraphendistance = -1;
    	
    	
    	double lmeantermdedistance = Double.MAX_VALUE;
    	
    	double lmingraphdedistance = -1;
    	double lmaxgraphdedistance = -1;
        
        
        boolean doit=true;
        
        
        
    //    if (meantermendistance==Double.MAX_VALUE)
        if ((doit==true) /*&&  (crawlerService.getController().isFinished()==true)  &&  (!crawlerService.preventcheckdb())*/  ) {
        	
        	checkinggraphdb=true;
        	
        	
        	
      //  	crawlerService.getGraphApi_EN().updateDiceandCosts();
        	
        	
        	LOGGER.info("Graphdatabase first run is finished.");
        	
        	
        	GraphAPI_EN graphen = crawlerService.getGraphApi_EN();
        	
        	Vector words = graphen.listNodes();
        	LOGGER.info("Words in en database: " +words.size());
        	LOGGER.info("Words in en database: " +words.toString());
        	
        	
        	HashMap <String, Double>enpairs = new HashMap<String, Double>();
        	Vector distances = new Vector();
        	
        	if (words.size()>=100) {
        		lmeantermendistance=0;
        		int count =0;
        		while ((enpairs.keySet().size()<100) &&  (count<500)) {
             			count++;
        			Random r = new Random();
				   	int value1 = r.nextInt(words.size());
        			
				   	Random r2 = new Random();
				   	int value2 = r2.nextInt(words.size());
        			
				   	String key =  words.get(value1)+"###"+words.get(value2);
				   	String key2 = words.get(value2)+"###"+words.get(value1);		
				   			
				   	if (!enpairs.containsKey(key) && !enpairs.containsKey(key2) && value1!=value2)	{
				   		
				   		double dist = graphen.getNodeDistance(words.get(value1).toString(), words.get(value2).toString()); //distance berechnen
				   		
				   		if (dist<Double.MAX_VALUE) {
				   			Double distance = new Double(dist);
				   			
				   		
				   			enpairs.put(key, distance);
				   			distances.add(dist);
				   			
				   			lmeantermendistance+=dist;
				   			LOGGER.info("Put Word distance: " +words.get(value1).toString() + " " + words.get(value2).toString() + " "+ dist + " " +lmeantermendistance);
				   		}
				   		
				   	}
				   	
				   	
				   	
        			
        		} //while
        		
        		
        		
        		if (lmeantermendistance>0) {
        		
        		lmeantermendistance=lmeantermendistance/ ((double)distances.size());
        		LOGGER.info("Mean term en distance: "+ lmeantermendistance);
        		
        		double sumcurdist =0;
        		
        		for (int i=0; i<distances.size(); i++) {
        			
        			double curdist = (double)distances.get(i);
        			//LOGGER.info("Curdist 1: "+ curdist);
        			
        			//LOGGER.info("curdist 2: "+ (curdist-meantermdistance) );
        			
        			curdist = (curdist-lmeantermendistance) * (curdist-lmeantermendistance);
        			
        			//LOGGER.info("curdist 3: "+ (curdist) );
        			
        			
        			sumcurdist+=curdist;
        			
        		}
        		//LOGGER.info("Sumcurdist 1: "+ sumcurdist);
        		
        		sumcurdist=sumcurdist/(double)(distances.size()-1);
        		//LOGGER.info("Sumcurdist 2: "+ sumcurdist);
        		
        		sumcurdist = Math.sqrt(sumcurdist);
        		LOGGER.info("Standard deviation: "+ sumcurdist);
        		
        		lmingraphendistance=lmeantermendistance-(3*sumcurdist);
        		if (lmingraphendistance<0) lmingraphendistance=0;
        		
        		lmaxgraphendistance=lmeantermendistance+(3*sumcurdist);
        		
        		LOGGER.info("mingraphendistance maxgraphendistance "+ lmingraphendistance + " " +lmaxgraphendistance);
        		
        		
        		mingraphendistance = lmingraphendistance;
        		maxgraphendistance = lmaxgraphendistance;
        		meantermendistance = lmeantermendistance;
        		
        		
        		}
        		
        		
        	}
        	
        	checkinggraphdb = false;
        	
        } 
        
        
        
        
      //  if (meantermdedistance==Double.MAX_VALUE)
        	 if ((doit==true) /*&&  (crawlerService.getController().isFinished()==true) &&  (!crawlerService.preventcheckdb()) */  ) {
            	
            	 checkinggraphdb=true;
            	
            //	 crawlerService.getGraphApi_DE().updateDiceandCosts();;
            	 
            	LOGGER.info("Graphdatabase first run is finished.");
            	
            	
            	GraphAPI_DE graphde = crawlerService.getGraphApi_DE();
            	
            	Vector words = graphde.listNodes();
            	LOGGER.info("Words in de database: " +words.size());
            	LOGGER.info("Words in de database: " +words.toString());
            	
            	
            	HashMap <String, Double>enpairs = new HashMap<String, Double>();
            	Vector distances = new Vector();
            	
            	if (words.size()>=100) {
            		lmeantermdedistance=0;
            		int count=0;
            		while ((enpairs.keySet().size()<100)    &&  (count<500)) {
            			count++;
            			Random r = new Random();
    				   	int value1 = r.nextInt(words.size());
            			
    				   	Random r2 = new Random();
    				   	int value2 = r2.nextInt(words.size());
            			
    				   	String key =  words.get(value1)+"###"+words.get(value2);
    				   	String key2 = words.get(value2)+"###"+words.get(value1);		
    				   			
    				   	if (!enpairs.containsKey(key) && !enpairs.containsKey(key2) && value1!=value2)	{
    				   		
    				   		double dist = graphde.getNodeDistance(words.get(value1).toString(), words.get(value2).toString()); //distance berechnen
    				   		
    				   		if (dist<Double.MAX_VALUE) {
    				   			Double distance = new Double(dist);
    				   			
    				   		
    				   			enpairs.put(key, distance);
    				   			distances.add(dist);
    				   			
    				   			lmeantermdedistance+=dist;
    				   			LOGGER.info("Put Word distance: " +words.get(value1).toString() + " " + words.get(value2).toString() + " "+ dist + " " +lmeantermdedistance);
    				   		}
    				   		
    				   	}
    				   	
    				   	
    				   	
            			
            		} //while
            		
            		
            		if (lmeantermdedistance>0) {
            		
            		lmeantermdedistance=lmeantermdedistance/ ((double)distances.size());
            		LOGGER.info("Mean term de distance: "+ lmeantermdedistance);
            		
            		double sumcurdist =0;
            		
            		for (int i=0; i<distances.size(); i++) {
            			
            			double curdist = (double)distances.get(i);
            			//LOGGER.info("Curdist 1: "+ curdist);
            			
            			//LOGGER.info("curdist 2: "+ (curdist-meantermdistance) );
            			
            			curdist = (curdist-lmeantermdedistance) * (curdist-lmeantermdedistance);
            			
            			//LOGGER.info("curdist 3: "+ (curdist) );
            			
            			
            			sumcurdist+=curdist;
            			
            		}
            		//LOGGER.info("Sumcurdist 1: "+ sumcurdist);
            		
            		sumcurdist=sumcurdist/(double)(distances.size()-1);
            		//LOGGER.info("Sumcurdist 2: "+ sumcurdist);
            		
            		sumcurdist = Math.sqrt(sumcurdist);
            		LOGGER.info("Standard deviation: "+ sumcurdist);
            		
            		

            		lmingraphdedistance=lmeantermdedistance-(3*sumcurdist);
            		if (lmingraphdedistance<0) lmingraphdedistance=0;
            		
            		lmaxgraphdedistance=lmeantermdedistance+(3*sumcurdist);
            		
            		
            		LOGGER.info("mingraphdedistance maxgraphdedistance "+ lmingraphdedistance + " " +lmaxgraphdedistance);
            		
            		mingraphdedistance = lmingraphdedistance;
            		maxgraphdedistance = lmaxgraphdedistance;
            		meantermdedistance = lmeantermdedistance;
            		
            		}
            	}
            	
            	checkinggraphdb=false;
            	
            } 
        
        

        
        
		
    
		
		
	}
	
	
	
	
	
	
	
	
	
	/**
	 * load neighbours, blacklist and candidates from the file system (the
	 * filenames are configured in the app.properties)
	 */
	public void load() {
		
		LOGGER.info("-> load()");
		
		LOGGER.info("All local IPs: " + ipManager.getLocalIps());
	
		
		if (networkmode==1) {
			
			LOGGER.info("networkmode: " + networkmode);
		
		try {
			//getHTML("http://www.docanalyser.de/ipposter/ipposter.php?reset=true");
			//getHTML("http://www.docanalyser.de/ipposter/ipposter.php?reset=true");
			
			
			localip=getPreferredLocalIPAddress();
			
			
			//Vector nips = getHTML("http://www.docanalyser.de/ipposter/regip.php?ip="+localip+":"+helper.getPeerPort());
				
			String inring="no";
			
			
			
			Vector nips = getHTML("http://www.docanalyser.de/ipposter/regip.php?ip="+localip+":"+helper.getPeerPort()+"&ringpeer="+inring);
			
			
			LOGGER.info("Current neighbour candidates online: ");
			
			for (int i=0; i<nips.size(); i++) {
				
				LOGGER.info(nips.get(i));
				
			}
		
		
		} catch (Exception ex) {
			
			LOGGER.info("Exception occurred on load(): " + ex.getMessage());
			
		}

		
		
		
		
		
		neighbors = load(neighboursfilename);
		fixedneighbors = load(neighboursfilename);
		blacklist = load(blackListfilename);
		candidates = load(candidatesfilename);
		LOGGER.info("Listing neighbour peers: ");
		for(String ip:neighbors){
			LOGGER.info("Neighbour: " +ip);
		}
		
		
		
		
		
		} else {
			
						
				LOGGER.info("networkmode: " + networkmode);
			
		}
		
		
		LOGGER.info("<- load()");
		
		
		
	}

	
	public String getLocalIP() {
		
		return localip.trim();
		
	}
	
	
	
	/**
	 * saves a list of strings to the given filename as several lines
	 * 
	 * @param filename
	 *            name of the file
	 * @param lines,
	 *            list of string written to a file
	 */
	public void save(String filename, List<String> lines) {

		try {

			Files.write(Paths.get(filename), String.join("\n", lines).getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			LOGGER.warn("error writing content to " + filename, e);
		}
	}

	/**
	 * loads a list of strings from the given filename as several lines
	 * 
	 * @param filename
	 *            name of the file
	 * @return list of lines in the file
	 * 
	 */
	public List<String> load(String filename) {

		List<String> result = new ArrayList<String>();
		List<String> help = new ArrayList<String>();
		
		if (!filename.trim().equals("")) {
		try {
			Path p = Paths.get(filename);
			if (Files.exists(p)) {
			
				help.addAll(Files.readAllLines(p));
			} else {
				LOGGER.warn("file " + filename + " does not exist");

			}
		} catch (IOException e) {
			LOGGER.warn("error loading content from " + filename, e);
		}
		}
		
		for (int i=0; i<help.size(); i++) {
			
			String helpstr = (String)help.get(i);
			
			if (!helpstr.trim().equals(""))
			result.add(helpstr.trim());
			
		}
		
		
		
		return result;
	}

	@PostConstruct
	public void init() {
		//load();
		
		System.setProperty("http.keepAlive", "false");
		
		//checkGraphTimer = new Timer();  
		//checkGraphTimer.schedule(new CheckGraphDatabasesTask(), 10000, 10000); //delay in milliseconds

		checkIndexGraphTimer = new Timer();  
		checkIndexGraphTimer.schedule(new CheckIndexGraphTask(), 20000, 20000); //delay in milliseconds


	}
	
	
	public void startCheckGraphDatabasesTask() {
		
		checkGraphThread = new CheckGraphDatabasesTask();  
		checkGraphThread.start();
		//checkGraphTimer.schedule(new CheckGraphDatabasesTask(), 10000, 10000); //delay in milliseconds
		
	}
	

	/**
	 * add an url to thhe blacklist
	 * 
	 * @param url
	 */
	public void addBlacklist(String url) {

		if (!blacklist.contains(url) || !neighbors.contains(url)) {
			blacklist.add(url);
		} else if (!blacklist.contains(url) || neighbors.contains(url)) {
			blacklist.add(url);
			neighbors.remove(url);
		}
	}

	/**
	 * add an url to the candidates list
	 * 
	 * @param url
	 */
	public void addCandidate(String url) {
		
		String ipa = getPreferredLocalIPAddress();			
		ipa = ipa+":"+helper.getPeerPort();
			
		if (url!=null) {
			LOGGER.info("Trying to add candidate: " +url);
			
			synchronized (candidates) {
				if (!candidates.contains(url) && !blacklist.contains(url) && !neighbors.contains(url) && !url.equals(ipa)            ) {
					candidates.add(url);
					LOGGER.info("Added candidate: " +url);
				}
			}
		}
		
	}

	
	
	
	/**
	 * add an url to the neighbour list
	 * 
	 * @param url
	 */
	public void addNeighbour(String url) {
		
		String ipa = getPreferredLocalIPAddress();			
		ipa = ipa+":"+helper.getPeerPort();
			
		if (url!=null) {
			LOGGER.info("Trying to add a new neighbour: " +url);
			
			synchronized (neighbors) {
				if (!neighbors.contains(url) && !blacklist.contains(url) && !url.equals(ipa)            ) {
					neighbors.add(url);
					LOGGER.info("Added new neighbour: " +url);
				}
			}
		}
		
	}
	
	
	
	public List<String> getNeighbours() {
		return neighbors;
	}

	/**
	 * sends a ping to another peer
	 * 
	 * @param host
	 *            url of the peer that should be contacted
	 * @return true, if and only if the other peer reponsed with http status
	 *         code 200
	 */
	public boolean callURL(String host, boolean ringmode) {
		LOGGER.info(String.format("-> callURL(%s)", host));

		//curl -X POST -i "http://127.0.0.1:8080/peer/ping.html" --data "askingPeer=192.168.0.4:8080"
		
		String url = helper.BuildUrl(host, "ping.html");
		this.LOGGER.info("using " + url + " to  check peer " + host);
		boolean result = false;
		
		String ipa = getPreferredLocalIPAddress();
		
		ipa = ipa+":"+helper.getPeerPort();
		
		
		if (!host.equals(ipa)) {
		
		
		String firstKey="";
		String lastKey="";
		String nextnextneighbour="";
		String previousneighbour="";
		
		
		HttpURLConnection conn = null;	
		OutputStream os = null;
		InputStream is = null;
		InputStreamReader isr = null;
		
		BufferedReader rd = null;
		
		BufferedWriter writer = null;
		OutputStreamWriter osw = null;
		
		//HttpURLConnection conn = null;
		//InputStream is = null;
		
		
		try {
			URL pingurl = new URL(url);
		//	HttpURLConnection conn = (HttpURLConnection) pingurl.opennection();
		//	s = conn.getInputStream();
		//	int responseCode = conn.getResponseCode();

			conn = (HttpURLConnection) pingurl.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			os = conn.getOutputStream();
			
			osw = new OutputStreamWriter(os, "UTF-8");
			
			writer = new BufferedWriter(osw);
			
			Map<String, String> postDataParams = new HashMap<String, String>();
			postDataParams.put("askingPeer", getPreferredLocalIPAddress()+":"+helper.getPeerPort());
			
			
			writer.write(getPostDataString(postDataParams));

			writer.flush();
			//writer.close();
			//osw.close();
			//os.close();
			
			
			int responseCode = conn.getResponseCode();
			
			
			
			if (200 == responseCode) {
				//!!! schaue ob der inputstrream "Peer is running" enthält, wenn ja
				// füge den host in die liste neighbours sonst blacklist
				//result = true;
				
				is = conn.getInputStream();
				
				isr = new InputStreamReader(is);
				
				rd = new BufferedReader(isr);
				
			      String line;
			      while ((line = rd.readLine()) != null) {
			    	  
			    	  String read = line.trim();
			    	  if (!read.equals("")) {
			    		  
			    		  if (read.contains("Peer is running")) {
			    			  result=true;
			    			  LOGGER.info("Read: Peer is running");;
			    			  
			    		  }
			    		  
			    		  if ((result==true) && (ringmode==true)) {
			    			  
			    			  
			    			  
			    		  } //result true asked peer is running
			    		  
			    	  }  
			    	  
			    	  
			    	
			    	  
			          
			    	  
			      }
			    //  rd.close();
				
				
			    if ((result==true) && (ringmode==true)) {
			    	
			    	
			    	
			    }	
		  		
		  		
			} //200 response code

		} catch (Exception e) {
			LOGGER.warn(String.format("Connecting %s failed with %s", host, e.getMessage()));
		} finally {

			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {

				}
			}
			
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {

				}
			}
			
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {

				}
			}
			
			
			
			
			
			
			
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {

				}
			}
			
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {

				}
			}
			
			
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {

				}
			}
			
			
			
			
			
			
			
			
			 if (conn != null) {
			        conn.disconnect();
			    }
			
			
		}
		
		
		
		
		
		
		
		
	} else {
		
		LOGGER.info("Cannot ping own address.");
		
		result = true;
		
	}
		
		
		
		
		LOGGER.info(String.format("<- callURL(..): %s", Boolean.toString(result)));
		return result;
	}

	
	private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}
	
	
	public static void main(String[] args) {
		URL pingurl;
		InputStream s = null;
		try {
			pingurl = new URL("http://www.google.de");
			URLConnection conn = pingurl.openConnection();
			s = conn.getInputStream();
			String statusCode = conn.getHeaderField("Status-Code");
			if ("200".equals(statusCode)) {
				// schaue o der inpoutstrream "Peer is running" enthï¿½lt wenn ja
				// füge den host in die liste neighbours sonst blacklist
			}
			
		} catch (Exception e) {

		} finally {
			try {
				s.close();
			} catch (IOException e) {

			}
		}
	}

	
	
	public static Vector getHTML(String urlToRead) {//throws Exception {
	      Vector result = new Vector();	      
	      HttpURLConnection conn = null;
	      InputStreamReader isr = null;
	      InputStream is = null;
	      BufferedReader rd = null;
	      URL url;
	      
			InputStream s = null;
			try {
				url = new URL(urlToRead);
				
				conn = (HttpURLConnection) url.openConnection();
	     
	      conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			 conn.setRequestMethod("GET");
	      
	      is = conn.getInputStream();
	      isr=new InputStreamReader(is);
	      
	      rd = new BufferedReader(isr);
	      String line;
	      while ((line = rd.readLine()) != null) {
	    	  
	    	  String read = line.trim();
	    	  if (!read.equals(""))
	          result.add(read);
	    	  
	      }
	      //rd.close();
	      
	      
			} catch (Exception exp ) {
	      
	      
			LOGGER.warn(String.format("Connecting %s failed with %s", urlToRead, exp.getMessage()));
	} finally {


		if (rd != null) {
			try {
				rd.close();
			} catch (IOException e) {

			}
		}
		
		if (isr != null) {
			try {
				isr.close();
			} catch (IOException e) {

			}
		}
		
		
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {

			}
		}
		
		
		
		
		
		
		
		
		 if (conn != null) {
		        conn.disconnect();
		    }
		
		
	}
	      
	      
	      
	      
	      
	      
	      
	      
	      return result;
	   }
	
	
	
	public String getLocalIPAddress_AUS() {
		
		LOGGER.info("getLocalIPAddress : " + localip);
		
		
		if (localip.equals("")) {
		
			InetAddress ip;
	        String hostname;
	        String ipaddress = "";
	        try {
	            ip = InetAddress.getLocalHost();
	            hostname = ip.getHostName();
	            ipaddress = ip.getHostAddress();
	            
	            LOGGER.info("Your current host and IP address : " + ip);
	            LOGGER.info("Your local IP address : " + ip.getHostAddress());
	            LOGGER.info("Your current Hostname : " + hostname);
	            
	            localip=ipaddress;
	 
	        } catch (UnknownHostException e) {
	 
	            e.printStackTrace();
	        }
        
		}       
        
        return localip.trim();
		
	}
	
	
	
	
	
	
	public String getPreferredLocalIPAddress() {
		
		LOGGER.info("getPreferredLocalIPAddress : " + localip);
		
		if (localip==null || localip.equals("")) {
		
			try(final DatagramSocket socket = new DatagramSocket()){
				  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				  localip = socket.getLocalAddress().getHostAddress();
				  LOGGER.info("Preferred local IP: " + localip);;
				} catch (SocketException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        
		}       
        
        return localip.trim();
		
	}
	
	
	
	
	public String getPreferredInternalLocalIPAddress() {
		
		LOGGER.info("getPreferredInternalLocalIPAddress : " + localipinternal);
		
		if (localipinternal==null || localipinternal.equals("")) {
		
			try(final DatagramSocket socket = new DatagramSocket()){
				  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				  localipinternal = socket.getLocalAddress().getHostAddress();
				  LOGGER.info("Preferred internal local IP: " + localipinternal);;
				} catch (SocketException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        
		}       
        
        return localipinternal.trim();
		
	}
	
	
	
	/**
	 * this method is called via the quartz job to check the url in the
	 * candidates list
	 */
	public void checkCandidates() {
		LOGGER.info("-> checkCandidates()");
		
		//LOGGER.info(System.getProperty("catalina.home"));
		//LOGGER.info(System.getProperty("CATALINA_BASE"));
		
		LOGGER.info(System.getenv("CATALINA_HOME"));
		//LOGGER.info(System.getenv("CATALINA_BASE"));
		LOGGER.info(System.getProperty("catalina.home"));
		
		if (networkmode==1) {
			
			LOGGER.info("networkmode: " + networkmode);
		
		
		 if (firstCheckDone==true) {
		
		LOGGER.info("Printing current blacklist: "+blacklist.toString());
		
		synchronized (neighbors) {
			Vector <String>help = new Vector<String>();
			
			
			LOGGER.info("Printing current neighbours: ");
			for (String host: neighbors) {
				
				LOGGER.info("Neighbour: " +host);
				
				
				
				if (!callURL(host, false)) {
					if (!callURL(host, false)) {
						if (!callURL(host, false)) {
					
							if (!blacklist.contains(host)) {
								//blacklist.add(host);
							}
							
							help.add(host);
					
						}
					}
				}
				
			}
			
			for (String host : help) {
				
				if (neighbors.contains(host)) {
					
					LOGGER.info("Removing neighbour (if not fixed one) " + host);;
					
					if (!fixedneighbors.contains(host))
					neighbors.remove(host);
					
				}
				
			}
			
		
		}
		
	
		
		
		 InetAddress ip;
	        String hostname;
	        try {
	            ip = InetAddress.getLocalHost();
	            hostname = ip.getHostName();
	            
	            LOGGER.info("Your current host and IP address : " + ip);
	         //   LOGGER.info("Your local IP address : " + ip.getHostAddress());
	         //   LOGGER.info("Your current Hostname : " + hostname);
	 
	        } catch (UnknownHostException e) {
	 
	            e.printStackTrace();
	        }
		
	        try {
	        Enumeration en = NetworkInterface.getNetworkInterfaces();
	        while(en.hasMoreElements()){
	            NetworkInterface ni=(NetworkInterface) en.nextElement();
	            Enumeration ee = ni.getInetAddresses();
	            while(ee.hasMoreElements()) {
	                InetAddress ia= (InetAddress) ee.nextElement();
	             //   LOGGER.info(ia.getHostAddress());
	            }
	         }
	        } catch (Exception exc) {}
	        
	        
	        
	        try {
				
				
				String ipa = getPreferredLocalIPAddress();
				
				//!!!evtl. mit Port
				//Vector nips = getHTML("http://www.docanalyser.de/ipposter/regip.php?ip="+ipa+":"+helper.getPeerPort());
				
				//Vector nips = getHTML("http://www.docanalyser.de/ipposter/ipposter.php?ip="+ipa+":"+helper.getPeerPort());
				
				String inring="no";
				
				
				
				Vector nips = getHTML("http://www.docanalyser.de/ipposter/regip.php?ip="+localip+":"+helper.getPeerPort()+"&ringpeer="+inring);
				
				
				
				ipa = ipa+":"+helper.getPeerPort();
				
				for (int i=0; i<fixedneighbors.size(); i++) {
				
				
					String neighborips = fixedneighbors.get(i).toString().trim();
					
					if (!neighborips.equals("")) {
					
					if ( (((String)fixedneighbors.get(i)).indexOf(ipa)==-1) && !neighbors.contains((String)fixedneighbors.get(i)) ) {
						
						LOGGER.info("Adding to candidate list (fixed neighbour): " + fixedneighbors.get(i));
						//neighbors.add((String)nips.get(i));
						addCandidate((String)fixedneighbors.get(i));
						
					} else {
						
						LOGGER.info("Cannot add own or duplicate address to candidate list: " + nips.get(i));
						
					}
				
				
					}
				}
				
				
				LOGGER.info("Own address: " + ipa);
				LOGGER.info("Current neighbour candidates online (own included): ");
				
				for (int i=0; i<nips.size(); i++) {
					
					LOGGER.info(nips.get(i));
					
					String neighborips = nips.get(i).toString().trim();
					
					if (!neighborips.equals("")) {
					
					if ( (((String)nips.get(i)).indexOf(ipa)==-1) && !neighbors.contains((String)nips.get(i)) ) {
						
						LOGGER.info("Adding to candidate list: " + nips.get(i));
						//neighbors.add((String)nips.get(i));
						addCandidate((String)nips.get(i));
						
					} else {
						
						LOGGER.info("Cannot add own or duplicate address to candidate list: " + nips.get(i));
						
					}
					
				}
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
			
			
			} catch (Exception ex) {
				
				LOGGER.info("Exception occurred on checkcandidates(): " + ex.getMessage());
				
				
			}    
	        
	        
	        
		
		List<String> currentCandidates = new ArrayList<String>();
		List<String> currentNeighbours = new ArrayList<String>();
		List<String> currentBlackList = new ArrayList<String>();
		// make a copy of the list according to the fact that
		synchronized (candidates) {
			
			
			LOGGER.info("Printing current candidates: ");
			
			for (String host: candidates) {
				
				LOGGER.info("Candidate: " +host);
				
			}
			
			
			currentCandidates.addAll(this.candidates);
			candidates.clear();
		}
		
		// check each candidate
		for (String host : currentCandidates) {
			if (checkCandidate(host)) {
				currentNeighbours.add(host);
			} else {
				//currentBlackList.add(host);
			//	addCandidate(host);  //evtl. drin lassen 06.09.
			}
		}
		synchronized (neighbors) {
			for (String host : currentBlackList) {
				blacklist.add(host);
			}
			for (String host : currentNeighbours) {
				
				
				if( (!host.startsWith(getPreferredLocalIPAddress()+":"+helper.getPeerPort() ))   && (neighbors.size()<maxneighbours)  &&   ( !neighbors.contains(host)))
				neighbors.add(host);
			}
		}

		
		

		

		
		
		
		
		
		
		
             } else {
            	 
            	 LOGGER.info("Checkcandidates not successful as local crawl not finished." );
                 
            	 
             }
		 
		} else {
			
							
				LOGGER.info("networkmode: " + networkmode);
			
			
		}
		 
		LOGGER.info("<- checkCandidates()");
	}

	/**
	 * @param host
	 *            domain of another peer
	 * @return true if and only if, there is peer installed on the given host
	 *         and listening for http requests at the port and path configured
	 *         in the app.properties file.
	 */
	public boolean checkCandidate(String host) {

		return callURL(host, false);
	}

	
	
	public boolean initialised() {
				
		
		return firstCheckDone;
	}
	
	
	
	
	
	
	
	public void setServiceHelper(ServiceHelper h) {
		this.helper = h;

	}
	
	
	public boolean checkinggraphdb() {
		
		return checkinggraphdb;
		
	}
	

}
