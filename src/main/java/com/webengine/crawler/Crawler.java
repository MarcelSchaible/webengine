package com.webengine.crawler;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.webengine.CrunchifyController;
import com.webengine.ILocalSearchService;
import com.webengine.INeighbourhoodService;
import com.webengine.ITextProcessor;
import com.webengine.ServiceHelper;
import com.webengine.graph.GraphAPI_DE;
import com.webengine.graph.GraphAPI_EN;
import com.webengine.textprocessing.TextProcessor;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * This class is a wrapper for the quartz job configured to crawl the documents
 * @author robert
 *
 */
@Component
public class Crawler  {

	private final static Logger LOGGER = Logger.getLogger(Crawler.class.getName());

	//@Autowired
	//CrawlConfig config;

	//@Autowired
	//CrawlerController crawlerController;

	@Value("${config.crawler.numberofCrawlers}")
	int numberOfCrawlers;

	@Autowired
	private INeighbourhoodService neighbourhodService;
	
	@Value("${config.crawler.seeds}") 
	String specifiedSeeds; 
	@Value("${config.crawler.localHost}")
	String localHost;
	@Value("${config.crawler.localPort}")
	String localPort;
	@Value("${config.crawler.localPath}")
	String localPath;
	
	@Value("${config.crawler.StorageFolder}")
	String crawlerstor;
	
	@Value("${config.crawler.graphdb}")
	private int createGraphDatabase;
	
	@Value("${config.crawler.maxfilesize}")
	private int maxfilesize;
	
	@Value("${config.crawler.maxdepth}")
	private int maxdepth;

	@Value("${config.crawler.uri}")
	private String domain = "";
	
	
	@Autowired
	private ILocalSearchService luceneService;
	
	@Autowired
	private ServiceHelper helper;
	
	@Autowired
	private IpManager ipManager;

	@Autowired
	UrlHelper urlhelper;

	@Autowired
	private TextProcessor textProcessor;
	
	
	@Autowired
	private GraphAPI_EN graphApi_EN;
	
	@Autowired
	private GraphAPI_DE graphApi_DE;
	
	List<String> addedseeds = new ArrayList<String>();

	List<String> firstseeds = new ArrayList<String>();

	
	int numberCurrentAddedseeds = 0;
	
	
	
	CrawlerController2 controller;
	
	
	//CrawlerController crawlerController;
	/**
	 * this methods starts a crawling process.
	 */
	int count =1;
	
	boolean firstrunstarted=false;
	boolean firstrunfinished=false;
	
	boolean graphDBWritten = false;
	
	boolean docheckdb=false;
	
	
	
	
	
	
	
	public String getExternalLocalHost() {
		
		LOGGER.info("getExternalLocalHost : " );
		        
        return localHost;
		
	}
	
	
	
	
	public void crawl(){		
		
		LOGGER.info("Crawl is called: " );
	
		count++;
		
		//<dependency>
		//<groupId>edu.uci.ics</groupId>
		//<artifactId>crawler4j</artifactId>
		//<version>4.1</version>
	//</dependency>
		
		String seeds = "";
		
		String base= System.getenv("CATALINA_HOME");
		
		if (base==null)
			base= System.getProperty("catalina.home");
		
		if (base==null) {
			
			LOGGER.info("Using specified seeds");
			seeds =  specifiedSeeds;
		
		} else {
			
			
			if (localPath!=null && !localPath.equals("")) {
			
				LOGGER.info("Using automatic seeds");
				
				if (!localPath.startsWith("/")) {
					localPath = "/" + localPath;
				}
				
				seeds = base+"/webapps"+localPath;
			
			} else {
				
				
				seeds=specifiedSeeds;
				
			}
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
		try {
			
			
			if (controller==null ) {

				LOGGER.info("Crawler: controller==null" );
				
				
				
				CrawlConfig config = new CrawlConfig();
		        config.setCrawlStorageFolder(crawlerstor);
		        config.setIncludeBinaryContentInCrawling(true);
		        config.setMaxDownloadSize(maxfilesize * 1024 * 1024);
		        config.setMaxDepthOfCrawling(maxdepth);
		        
		        boolean skipgraphdbrestart = false;
		        
		        if (neighbourhodService.checkinggraphdb()) {
					  
					  
					  LOGGER.info("Crawler: neighbourhodService.checkinggraphdb()=true " );
					  
			        	skipgraphdbrestart = true;
			        	
			        	
			        	
				  } else {
				  
				  LOGGER.info("Crawler: neighbourhodService.checkinggraphdb()=false " );
				  
			  }
		        
		        /*
		         * Instantiate the controller for this crawl.
		         */
		        
		    // Threadbug Feb 2019
		    //    PageFetcher pageFetcher = new PageFetcher(config);
		    //    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		     
		        
		        
		        // RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		         controller = new CrawlerController2(config, /*pageFetcher,  robotstxtConfig,   */
		   			 seeds, 
		   			 localHost,
		   			 localPort, 
		   			localPath, domain, neighbourhodService, luceneService, ipManager, helper, urlhelper, textProcessor, graphApi_EN, graphApi_DE, 1, skipgraphdbrestart, this);
			
				
				controller.init();
				firstrunstarted=true;
				controller.startCrawl();
				
				
				//	crawlerController.init();
					
				//	crawlerController.start(MyCrawler.class, numberOfCrawlers);		
			
			
			} else {
				
				LOGGER.info("Crawler: controller!=null" );
				
				docheckdb=false;
				
				LOGGER.info("Index size: " + luceneService.getNumberDocuments() + "   Max First Seeds: " +getFirstSeeds().size() + "   Added Seeds: " + getAddedSeeds().size() + "   Number of recently added Seeds: "+numberCurrentAddedseeds);
            	
				LOGGER.info("Currently performing graphdb check?: " + neighbourhodService.checkinggraphdb() + "   Has the graphdb been changed during last crawl?: " +  graphDBWritten);
				
				
				if (controller.isFinished()) {
					
					controller.stopController();
					
					boolean graphdbupdated=false;
					boolean skipgraphdbrestart = false;
					
					//LOGGER.info("Crawler: controller finished: " + neighbourhodService.checkinggraphdb() + "  "+numberCurrentAddedseeds + " " +  graphDBWritten);
					
				//	if (!neighbourhodService.checkinggraphdb())
					if ((numberCurrentAddedseeds>0) && (graphDBWritten==true)) {    //did the last crawl add new documents
						
						//LOGGER.info("Crawler: numberCurrentAddedseeds: "+numberCurrentAddedseeds + "    graphDBWritten: " +graphDBWritten);
						
						docheckdb=false;
						
	                	getGraphApi_DE().updateDiceandCosts();
	                   	getGraphApi_EN().updateDiceandCosts();
						
	                   	graphdbupdated=true;
	                   	
	                   //	neighbourhodService.checkGraphDB();
	                   	
	                 
					}
					
					
					
					
					
					//if (firstrunstarted==true)
					if ((addedseeds.size()>=firstseeds.size())  && (firstrunfinished==false)   ) {
						
						if (graphdbupdated==false) {
							docheckdb=false;	
							getGraphApi_DE().updateDiceandCosts();
							getGraphApi_EN().updateDiceandCosts();
						}
	                   	
						//neighbourhodService.checkGraphDB();

						//getGraphApi_DE().restartDB();
	                	//getGraphApi_EN().restartDB();
	                	
	                	graphdbupdated=true;
	                	
	                	//firstrunfinished=true; //neu
	                	skipgraphdbrestart = true;
					}
					
					
					
					if (graphdbupdated==true) {
					
						if (!neighbourhodService.checkinggraphdb()) {
							docheckdb=false;
							getGraphApi_DE().restartDB();
	                		getGraphApi_EN().restartDB();
						}
						
						if ((addedseeds.size()>=firstseeds.size())  && (firstrunfinished==false)   )
							firstrunfinished=true;
	                	                	
						//neighbourhodService.checkGraphDB();
						
						if (!neighbourhodService.checkinggraphdb()) {
							docheckdb=true;
							neighbourhodService.startCheckGraphDatabasesTask();
						}
					}
					
					
					
					
					/*
					  if (neighbourhodService.checkinggraphdb()) {
						  
						  
						  LOGGER.info("Crawler: neighbourhodService.checkinggraphdb()=true " );
						  
				        	skipgraphdbrestart = true;
				        	
				        	
				        	
					  } else {
						  
						  LOGGER.info("Crawler: neighbourhodService.checkinggraphdb()=false " );
						  
					  }*/
				        	
			
				        
					
					CrawlConfig config = new CrawlConfig();
			        config.setCrawlStorageFolder(crawlerstor);
			        config.setIncludeBinaryContentInCrawling(true);
			        config.setMaxDownloadSize(maxfilesize * 1024 * 1024);
			        config.setMaxDepthOfCrawling(maxdepth);
			       
			        
			        /*
			         * Instantiate the controller for this crawl.
			         */
	
	//Threadbug Feb 2019		        
	//		        PageFetcher pageFetcher = new PageFetcher(config);
	//		        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			       // RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			        
			         controller = new CrawlerController2(config, /*pageFetcher,  robotstxtConfig,*/
			   			 seeds, 
			   			 localHost,
			   			 localPort, 
			   			localPath, domain, neighbourhodService, luceneService, ipManager, helper, urlhelper, textProcessor, graphApi_EN, graphApi_DE, 0, skipgraphdbrestart, this);
				
					
					controller.init();
					
					controller.startCrawl();
					
					
					
					
				} else {
					
					LOGGER.info("Crawler: controller not finished");
					
					
				}
				
				
				
				
				
				
				
				
				
				
				
				
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			} catch (Exception exp) {}
			
			LOGGER.info("Crawl ended" );
		
		
		
	}
	
	
	public List<String> getAddedSeeds() {
		
		return addedseeds;
		
	}
	
	
	public void addToAddedSeeds(String uri) {
		
		if (!addedseeds.contains(uri))
		addedseeds.add(uri);
		
	}
	
	
	
	public List<String> getFirstSeeds() {
		
		return firstseeds;
		
	}
	
	
	public void addToFirstSeeds(String uri) {
		
		if (!firstseeds.contains(uri))
		firstseeds.add(uri);
		
	}
	
	public boolean docheckdb() {
		
		LOGGER.info("docheckdb(): " + docheckdb);
		
		return docheckdb;
		
	}
	
	
	public void setdocheckdb(boolean docheck) {
		
		LOGGER.info("setdocheckdb(): " + docheck);
		
		docheckdb=docheck;
		
	}
	
	
	
	public boolean firstRunFinished() {
		
		LOGGER.info("firstRunFinished(): " + firstrunfinished );
		
		/*
		if (firstrunstarted==true && controller!=null && firstrunfinished==false) {
			LOGGER.info("firstrunstarted==true && controller!=null && firstrunfinished==false" );
			
			if (controller.isFinished()) {
				LOGGER.info("controller finished" );
				
				
				
				firstrunfinished=true;
			}
			
		}*/
		
		
		return firstrunfinished;
		
	}

	
	/**
	 * 
	 * @return reference to the lucenservice (service resp. for managing the
	 *         apache lucene index)
	 */
	public ILocalSearchService getLuceneService() {
		return luceneService;
	}

	/**
	 * 
	 * @return string from the properties file that can be used to determine if
	 *         a url is local or not
	 */
	public String getLocalDomain() {
		return domain;
	}

	public INeighbourhoodService getNeighbourhodService() {
		return neighbourhodService;
	}

	public IpManager getIpManager() {
		return ipManager;
	}
	
	/**
	 * Getter for textprocessor.
	 * @return
	 */

	public ITextProcessor getTextProcessor() {
		return this.textProcessor;
		
	}
	
	/**
	 * Getter for graph api.
	 * @return
	 */
		public GraphAPI_DE getGraphApi_DE() {
			return graphApi_DE;
		}


		/**
		 * Getter for graph api.
		 * @return
		 */
			public GraphAPI_EN getGraphApi_EN() {
				return graphApi_EN;
			}

			
			public int getCreateGraphDatabase() {
				return createGraphDatabase;
			}
			
			
			public int getNumberOfCrawlers() {
				return numberOfCrawlers;
			}
			
			public CrawlerController2 getController() {
				
				return controller;
				
			}


			public void updateNumberCurrentAddedseeds(int size) {
				
				numberCurrentAddedseeds = size;
			}
			
			public void setGraphDBWritten(boolean written) {
				
				LOGGER.info("setGraphDBWritten(): " + written );
				
				graphDBWritten = written;
			}
			
			
			
}
