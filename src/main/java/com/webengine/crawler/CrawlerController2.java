package com.webengine.crawler;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//import org.slf4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.net.UrlEscapers;
import com.webengine.IGraphService;
import com.webengine.ILocalSearchService;
import com.webengine.INeighbourhoodService;
import com.webengine.ITextProcessor;
import com.webengine.ServiceHelper;
import com.webengine.textprocessing.TextProcessor;
import com.webengine.graph.GraphAPI_DE;
import com.webengine.graph.GraphAPI_EN;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * This class is the controller responsible for the crawling (i.e. it holds all
 * properties required and references to other services required)
 * 
 * @author robert
 *
 */
@Component
public class CrawlerController2  {

	private final static Logger LOGGER = Logger.getLogger(CrawlerController2.class.getName());
	
	
	List<String> currentaddedseeds = new ArrayList<String>();
	
	
	private String domain = "";



	
	IpManager ipManager;

	
	UrlHelper urlhelper;

		
	private int firststart = -1;
	
	private boolean skipgraphdbrestart;
	
	private String seeds;

	private String localHost;

	private String localPath;

	private boolean started=false;
	
	
	private String localPort;
	
	private INeighbourhoodService neighbourhodService;
	
	
	Crawler currentcrawler;
	CrawlConfig config;
	PageFetcher pageFetcher; 
	RobotstxtConfig robotsconfig;
	
	CrawlController controller;
	
	ILocalSearchService luceneService;
	
	TextProcessor textProcessor;
	

	GraphAPI_EN graphApi_EN;
	
	
	GraphAPI_DE graphApi_DE;
		
	
	public CrawlerController2() {
		
	}
			
	
	public CrawlerController2(CrawlConfig config, /*PageFetcher pageFetcher, RobotstxtConfig robotsconfig,*/
			 String seeds, 
			 String localHost,
			 String localPort, 
			String localPath, String domain, INeighbourhoodService neighbourhodService, ILocalSearchService luceneService, 
			IpManager ipManager, ServiceHelper helper, UrlHelper urlhelper,  TextProcessor textProcessor, GraphAPI_EN graphApi_EN, GraphAPI_DE graphApi_DE, int firststart, boolean skipgraphdbrestart, Crawler thiscrawler)
	
	
					throws Exception {
		
		
		
	//	PageFetcher pageFetcher = new PageFetcher(config);
	//    RobotstxtConfig robotsconfig = new RobotstxtConfig();
		
		
	//	controller = new CrawlController(config, pageFetcher, new RobotstxtServer(robotsconfig, pageFetcher), thiscrawler);
		
		LOGGER.info("Crawl binary content: " +config.isIncludeBinaryContentInCrawling());
		
		
		this.neighbourhodService=neighbourhodService;
		this.seeds= seeds;
		this.localHost=localHost;
		this.localPort=localPort;
		this.localPath=localPath;
		this.domain=domain;
		this.luceneService=luceneService;
		this.ipManager=ipManager;
		this.urlhelper=urlhelper;
		this.textProcessor=textProcessor;
		this.graphApi_EN=graphApi_EN;
		this.graphApi_DE=graphApi_DE;
		this.currentcrawler=thiscrawler;
		this.config=config;
		
		this.firststart = firststart;
		this.skipgraphdbrestart = skipgraphdbrestart;
		
		if (localHost == null  || localHost.equals("")) 
		{	LOGGER.info("localhost null");
		
		this.localHost =  neighbourhodService.getPreferredLocalIPAddress()  ; // localHost== null?"localhost":localHost ;
		} else {
			
			this.localHost = neighbourhodService.getPreferredInternalLocalIPAddress();
			
		}
		
		
		
		this.localPath = localPath == null ?"/":localPath;		
		if (!this.localPath.startsWith("/")) {
			this.localPath = "/" + this.localPath;
		}
		if (this.localPath.length()>1 && !this.localPath.endsWith("/")) {
			this.localPath = this.localPath+"/";
		}
		
		if (localPort == null  || localPort.equals("") ) 
		{	LOGGER.info("localPort null");
		
		this.localPort = Integer.toString(helper.getPeerPort());  ; // localHost== null?"localhost":localHost ;
		}
		
		

	}

	//@PostConstruct
	public void init() {
		LOGGER.info("initializing Controller... Seeds: " +seeds.split(",")[0]);

		addSeeds(seeds.split(","));
	}

	

	

	

	private void addSeeds(String[] seeds) {
		
		LOGGER.info("addSeeds called " + seeds[0]);
		for (String pageUrl : seeds) {

			LOGGER.info("PageURL: " +pageUrl);
			
			UrlHelper urlhelper= new UrlHelper();
			
			UrlType type = urlhelper.getType(pageUrl);
			LOGGER.info("URL type: " +type.toString());
			
			
			
			switch (type) {
			case REMOTE:
				
				if (firststart==1) 
					this.currentcrawler.addToFirstSeeds(pageUrl);
				
				
				if (currentaddedseeds.size()<20)
				if (!this.currentcrawler.getAddedSeeds().contains(pageUrl)) {
					
					
					
					if (controller==null) {
						
						PageFetcher pageFetcher = new PageFetcher(config);
					    RobotstxtConfig robotsconfig = new RobotstxtConfig();
						
						try {
						controller = new CrawlController(config, pageFetcher, new RobotstxtServer(robotsconfig, pageFetcher), currentcrawler);
						} catch (Exception exp) {
							if (pageFetcher!=null)
							pageFetcher.shutDown();
						} 
						
					}
					
					
					
					
					
					if (controller!=null) {
					
					currentaddedseeds.add(pageUrl);
					this.currentcrawler.addToAddedSeeds(pageUrl);
					controller.addSeed(pageUrl);
					
					}
				
				}
				break;
			case LOCAL:
				
				for (String uri : getAllHtmlFiles(new File(pageUrl))) {
					LOGGER.info("Trying to add seed: " +uri);
					
					if (firststart==1) 
						this.currentcrawler.addToFirstSeeds(uri);
					
					if (currentaddedseeds.size()<20)
					if (!this.currentcrawler.getAddedSeeds().contains(uri)) {
						
						
						if (controller==null) {
							
							PageFetcher pageFetcher = new PageFetcher(config);
						    RobotstxtConfig robotsconfig = new RobotstxtConfig();
							
							try {
							controller = new CrawlController(config, pageFetcher, new RobotstxtServer(robotsconfig, pageFetcher), currentcrawler);
							} catch (Exception exp) {
								
								if (pageFetcher!=null)
								pageFetcher.shutDown();
							} 
							
						}
						
						
						if (controller!=null) {
						
						currentaddedseeds.add(uri);
						this.currentcrawler.addToAddedSeeds(uri);
						controller.addSeed(uri);
						
						}
						
						
						LOGGER.info("Added seed: " +uri);
						
					}
					
					
				}
				break;
			default:
				break;
			}

		}
	}
	
	
	
	
	public void startCrawl() {
		
		
		currentcrawler.updateNumberCurrentAddedseeds(currentaddedseeds.size());
		
		if (currentaddedseeds.size()>0) {
		
			
			
		//	if (currentcrawler.getCreateGraphDatabase()==1) {
		//		started=true;
		//		controller.start(MyCrawler.class, 1);
		//	} else {
			
			/*
			if (skipgraphdbrestart==false) {
			currentcrawler.getGraphApi_DE().restartDB();
			currentcrawler.getGraphApi_EN().restartDB();
			
			
			//currentcrawler.getGraphApi_DE().updateDiceandCosts();
			//currentcrawler.getGraphApi_EN().updateDiceandCosts();
			
			
			}	*/
			
			currentcrawler.setGraphDBWritten(false);
			
			if (controller!=null) {
				started=true;
				controller.start(MyCrawler.class, currentcrawler.getNumberOfCrawlers());
				
			}
				
				
		//	}
		
		}
	}
	

	private List<String> getAllHtmlFiles(File file) {
		
		LOGGER.info("->getAllHtmlFiles " +file.getPath());
		
		List<String> seeds = new ArrayList<String>();
		String rootPath = FilenameUtils.separatorsToUnix(file.getPath());
		if (file.isDirectory()) {		

			Collection<File> htmlFiles = FileUtils.listFiles(file, new String[] { "html", "htm" , "txt", "pdf" }, true);

			for (File htmlFile : htmlFiles) {
				String uri = toURI(rootPath, htmlFile);
				seeds.add(uri);
				LOGGER.info("getAllHtmlFiles seeds add1: " +uri);
			}
		}else{
			seeds.add(toURI(rootPath, file));
			LOGGER.info("getAllHtmlFiles seeds add2: " +toURI(rootPath, file));
			
		}
		
		LOGGER.info("<-getAllHtmlFiles " +file.getPath());
		return seeds;
	}

	public String toURI(String rootPath, File htmlFile) {
		String path = FilenameUtils.separatorsToUnix(htmlFile.getAbsolutePath());
		String filename = path.substring(rootPath.length(), path.length());
		if (filename.startsWith("/")){
			filename = filename.substring(1);
		}
		String uri = "http://"+localHost + ":" + localPort + localPath +  UrlEscapers.urlFragmentEscaper().escape(filename);

		//URL url = file.toURI().toURL();
		try {
		String encodedString = UrlEscapers.urlFragmentEscaper().escape(filename);
		//new URLCodec().encode(filename);
		//URLEncoder.encode(filename, "UTF-8");
		
		LOGGER.info("encodedstring: " +encodedString);
		
		
		} catch (Exception exp) {}
		
		
		
		return uri;
	}
	
	/**
	 * Getter for textprocessor.
	 * @return
	 */

	
	public boolean isFinished() {
		
		boolean result = true;
		
		if (controller!=null) {
		
			if (started==true) {
			 
				LOGGER.info("Crawlers were started. Finished?: " + controller.seemsFinished());;
				result = controller.seemsFinished();
			
			}
			
		}
		
		return result;
	}
	
	
	
	public void stopController() {
		
		LOGGER.info("Stopping Controller... ");;

		if (controller!=null) {
		
		//	PageFetcher pf = controller.getPageFetcher();
		//	pf.shutDown();
			
			controller.shutdown();
			
			
		} else {
			
			LOGGER.info("Controller is null.");

			
		}
		
	
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

			
}