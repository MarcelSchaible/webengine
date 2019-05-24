package com.webengine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webengine.fts.SearchService;
import com.webengine.fts.ViewDocument;

import com.webengine.messages.Entry;
import com.webengine.messages.SearchRequest;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * This class is the central controller for incoming http request.
 * 
 * The requests are processed using several services
 * 
 * @author robert
 *
 */
@Controller
public class CrunchifyController {

	private final static Logger LOGGER = Logger.getLogger(CrunchifyController.class.getName());

	@Autowired
	private SearchService searcher;

	@Value("${peer.name}")
	private String name;

	@Value("${ttl}")
	private int ttl;
	
	@Value("${searchresults.centroid}")
	private int maxsearchresultscentroid;
	
	@Value("${searchresults.fulltext}")
	private int maxsearchresultsfulltext;

	@Autowired
	private INeighbourhoodService neighbourhoodService;

	

	
	@Autowired
	private ServiceHelper helper;
	
	
	Set<String> processedAllocateMessageIds = new HashSet<String>();
	
	char smallestString = '\u0000';
	char largestString = '\uFFFF';
	
	public String getName() {
		return name;
	}

	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * this message handles incoming search Requests from other peers (note not
	 * from the web interface)
	 * 
	 * @param messageString
	 *            a json encoded representation of an Message object, that
	 *            contains all the parameters of the search request
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/searchDocuments", method = RequestMethod.POST)
	public ModelAndView searchRequest(@RequestParam(value = "message", required = true) String messageString,
			HttpServletRequest request) {
		LOGGER.info(String.format("-> searchDocuments(searchParam: %s)", messageString));
		addToNeighbours(request);
		

		SearchRequest message = ServiceHelper.parseFromJson(messageString, SearchRequest.class);
		
		this.neighbourhoodService.addCandidate(message.getAskingPeer());
	
		List<ViewDocument> results = new ArrayList<ViewDocument>();

		results = searcher.search(message);
		LOGGER.info("<- searchDocuments()");
		return new ModelAndView(new MappingJackson2JsonView(), "searchDetails", results);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * this message is used to process incoming search requests from the html
	 * interface
	 * 
	 * @param searchString
	 *            Query String from the HTML interface
	 * @param request
	 * @return
	 */
	@RequestMapping("/search")
	public String search(@RequestParam(value = "searchParam", required = false) String searchString,
			HttpServletRequest request) {

		LOGGER.info(String.format("-> search(searchParam: %s)", searchString));
		
		if (searchString!=null && !searchString.equals("")) {
			
			searchString = Jsoup.parse(searchString).text(); 
			
			LOGGER.info(String.format("Cleaned Query: %s)", searchString));
			
			
		SearchRequest m = new SearchRequest(createUniquId(), ttl, searchString);

		byte direction=1;
		/*
		if(Math.random() < 0.5) {
			direction = 0;
		} else {
			direction = 1;
		}*/
		
		m.setAskingPeer(this.neighbourhoodService.getPreferredLocalIPAddress()+":"+this.helper.getPeerPort());
		m.setDirection(direction);
		
		String centroid = searcher.getCentroid(m);
		
		if (centroid.equals("")) {
			
			String [] splittedquery = searchString.split(" ");
			centroid = splittedquery[0].toLowerCase();
			if (centroid.equals("")) {
				centroid = searchString.toLowerCase();
			}
			
		}
		
		m.setCentroid(centroid); //!!!must be adapted
		
		double quality = searcher.getQuality(m);
		int qual= new Double(quality).intValue();
		
		
		List<ViewDocument> results = searcher.search(m);
		
		if (results.size()>0)
		Collections.sort(results, new Comparator<ViewDocument>() {
		    @Override
		    public int compare(ViewDocument o1, ViewDocument o2) {
		        return o1.compareTo(o2);
		    }
		});
		
		
		
		List<ViewDocument> resultshelpcentroid = new ArrayList<ViewDocument>();
		List<ViewDocument> resultshelpfulltext= new ArrayList<ViewDocument>();
		
		
		LOGGER.info("Listing results and scores: ");;
		
		int size = 1000;

		if (results.size()<size) 
			size = results.size();

			
			for (int i=0; i<size; i++) {
				
				if ((results.get(i).getCentroidsearch()==true) && (resultshelpcentroid.size()<maxsearchresultscentroid)) {
				
					ViewDocument hd = results.get(i);
					
					if (hd.getTitle().length()>100) 
						hd.setTitle(hd.getTitle().substring(0, 99));
					
				//	if (hd.getSourcePeer().length()>50) 
					//	hd.setSourcePeer(hd.getSourcePeer().substring(0, 49));
					
					resultshelpcentroid.add(hd);
				
				}
				
				if ((results.get(i).getCentroidsearch()==false) && (resultshelpfulltext.size()<maxsearchresultsfulltext)) {
					
					ViewDocument hd = results.get(i);
					
					if (hd.getTitle().length()>100) 
						hd.setTitle(hd.getTitle().substring(0, 99));
					
				//	if (hd.getSourcePeer().length()>50) 
					//	hd.setSourcePeer(hd.getSourcePeer().substring(0, 49));
					
					resultshelpfulltext.add(hd);
				}
					

				LOGGER.info(results.get(i).getTitle()+ " " + results.get(i).getScore());;
				
				
			}
			
	
		 List<ViewDocument> finalresults = new ArrayList<ViewDocument>();
			
		 finalresults.addAll(resultshelpcentroid);
		 finalresults.addAll(resultshelpfulltext);
		

		request.setAttribute("results", finalresults);
		request.setAttribute("searchString", searchString);
		request.setAttribute("centroid", centroid); //!!!must be adapted
		request.setAttribute("quality", qual); 
		
	} else {
		
		request.setAttribute("quality", -1); 
		
	}
		
		LOGGER.info("<- search()");
		return "welcome";
	}

	

	
	
	
	/**
	 * This method is called when another peer sends a ping message to the peer
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/ping", method = RequestMethod.POST)
	public String ping(@RequestParam(value = "askingPeer", required = true) String askingPeer, HttpServletRequest request) {

		LOGGER.info(String.format("-> ping()"));
		request.setAttribute("message", "Peer is running");
		//request.setAttribute("firstEntry", "");
		//request.setAttribute("lastEntry", "");
		
		this.neighbourhoodService.addCandidate(askingPeer);
		
		
			
			request.setAttribute("firstEntry", "");
			request.setAttribute("lastEntry", "");
			request.setAttribute("nextnextneighbour", "");
			request.setAttribute("previousneighbour", "");
			//<% out.print("\n"); %>
		
		
		
		LOGGER.info("<- ping()");
		return "ping";

	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 *  adds the sender of a search request to the neighbours of a peer
	 * @param reqeust incoming search request
	 */
	private void addToNeighbours(HttpServletRequest request){
		
		try {
			URI uri = new URI(request.getRequestURI());
			String host = uri.getHost();
			
			LOGGER.info("addtoneighbours called: " +host);;
			
			
			this.neighbourhoodService.addCandidate(host);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.warn(request.getRequestURI()+" is not a valid uri");
		}
		
	}
	/**
	 * 
	 * @return eine eindeutig Id.
	 */
	private String createUniquId() {
		return UUID.randomUUID().toString();
	}

}
