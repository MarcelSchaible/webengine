package com.webengine.fts;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webengine.ILocalSearchService;
import com.webengine.INeighbourhoodService;

import com.webengine.ServiceHelper;
import com.webengine.crawler.Crawler;
import com.webengine.graph.GraphAPI_DE;
import com.webengine.graph.GraphAPI_EN;
import com.webengine.messages.SearchRequest;
import com.webengine.messages.SearchResponse;
import com.webengine.textprocessing.Sentence;
import com.webengine.textprocessing.TextProcessingResult;
import com.webengine.textprocessing.Word;
import com.webengine.messages.Entry;

/**
 * this is the service providing methods to handle incoming search requests
 * 
 * @author robert
 *
 */
@Service
public class SearchService {

	private final static Logger LOGGER = Logger.getLogger(SearchService.class.getName());

	@Autowired
	private ILocalSearchService luceneService;

	@Autowired
	private INeighbourhoodService neighbourhoodService;

	
	
	
	
	@Autowired
	private Crawler crawlerService;
	
	@Value("${numberofForwards}")
	private int numberOfForwards;

	@Value("${peer.name}")
	private String peername;
	
	
	@Autowired
	private ServiceHelper helper;

	HashMap<String, Timestamp> processedMessageIds = new HashMap<String,Timestamp>();

	
	public double getQuality(SearchRequest message) {

		LOGGER.info("getQuality: " + message.getSearchString());
		
		double quality = -1;
		try {
			
		
			TextProcessingResult result = crawlerService.getTextProcessor().process(message.getSearchString());
			
			LOGGER.info("Query language is: " + result.getLanguage());
			
			if (result.getLanguage().equals("eng")) {
				

				
				GraphAPI_EN graphen = crawlerService.getGraphApi_EN();
				
				//String [] splittedsentence = message.getSearchString().split(" ");
				
				Vector querywords = new Vector();
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					
					LOGGER.info("All nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						LOGGER.info(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}
					
					for (final Word word : nouns /*sent.getWords()*/) {
						
						querywords.add(word.getStemmedWord().toLowerCase());
						
					}
				
				}
				
				LOGGER.info("Querywords: " +querywords.toString());
				
				double maxdist = -1;
				double sumdists=0.0;
				double addeddists = 0.0;
				
				if (querywords.size()>1) {
					
					for (int i=0; i<querywords.size(); i++) {
						
						
						for (int j=i+1; j<querywords.size(); j++) {
							
							
							double curdist = graphen.getNodeDistance(querywords.get(i).toString(), querywords.get(j).toString());
							
							if (curdist<Double.MAX_VALUE) {
								
								if (curdist>maxdist)
									maxdist=curdist;
								
								sumdists=sumdists+curdist;
								addeddists++;
								
								
							}
							
							
						}
						
						
					}
					
	
				}
				
				
				double helpvalue = -1;
				
				if (addeddists>0.0)
					helpvalue = sumdists / addeddists;
				
				
				if (neighbourhoodService.getmeantermendistance()<Double.MAX_VALUE && neighbourhoodService.getmingraphendistance()!=-1 && neighbourhoodService.getmaxgraphendistance()!=-1) {
				
					if (neighbourhoodService.getmaxgraphendistance()>neighbourhoodService.getmingraphendistance()) 
					quality = (helpvalue/*maxdist*/ - neighbourhoodService.getmingraphendistance()) / (neighbourhoodService.getmaxgraphendistance()-neighbourhoodService.getmingraphendistance());
				
					
					
					
				}
						
					
				
				LOGGER.info("Query Quality is: " +quality + "   Maxdist:" +maxdist + "  Average: " +helpvalue);;
				
				if (quality>=0 && quality<=1) {
					
					quality = (1 - quality)*100;
					
					LOGGER.info("Final Query Quality is: " +quality + "   Maxdist:" +maxdist);;
					
				} else {
					
					quality = -1;
					
				}
				
				
						
			} else if (result.getLanguage().equals("deu") ) {
				
				GraphAPI_DE graphde = crawlerService.getGraphApi_DE();
				
				//String [] splittedsentence = message.getSearchString().split(" ");
				
				Vector querywords = new Vector();
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					
					LOGGER.info("All nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						LOGGER.info(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}
					
					for (final Word word : nouns /*sent.getWords()*/) {
						
						querywords.add(word.getStemmedWord().toLowerCase());
						
					}
				
				}
				
				LOGGER.info("Querywords: " +querywords.toString());
				
				double maxdist = -1;
				double sumdists=0.0;
				double addeddists = 0.0;
				
				if (querywords.size()>1) {
					
					for (int i=0; i<querywords.size(); i++) {
						
						
						for (int j=i+1; j<querywords.size(); j++) {
							
							
							double curdist = graphde.getNodeDistance(querywords.get(i).toString(), querywords.get(j).toString());
							
							if (curdist<Double.MAX_VALUE) {
								
								if (curdist>maxdist)
									maxdist=curdist;
								
								sumdists=sumdists+curdist;
								addeddists++;
								
								
							}
							
							
						}
						
						
					}
					
	
				}
				
				
				double helpvalue = -1;
				
				if (addeddists>0.0)
					helpvalue = sumdists / addeddists;
				
				
				if (neighbourhoodService.getmeantermdedistance()<Double.MAX_VALUE && neighbourhoodService.getmingraphdedistance()!=-1 && neighbourhoodService.getmaxgraphdedistance()!=-1) {
				
					if (neighbourhoodService.getmaxgraphdedistance()>neighbourhoodService.getmingraphdedistance()) 
					quality = (helpvalue/*maxdist*/ - neighbourhoodService.getmingraphdedistance()) / (neighbourhoodService.getmaxgraphdedistance()-neighbourhoodService.getmingraphdedistance());
				
					
					
					
				}
						
					
				
				LOGGER.info("Query Quality is: " +quality + "   Maxdist:" +maxdist + "  Average: " +helpvalue);;
				
				if (quality>=0 && quality<=1) {
					
					quality = (1 - quality)*100;
					
					LOGGER.info("Final Query Quality is: " +quality + "   Maxdist:" +maxdist);;
					
				} else {
					
					quality = -1;
					
				}
				
			}

		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		return quality;
		
	}
	
	
	
	public String  getCentroid(SearchRequest message) {

		LOGGER.info("getCentroid: " + message.getSearchString());
		String centroid="";
		try {
			
		
			TextProcessingResult result = crawlerService.getTextProcessor().process(message.getSearchString());
			
			LOGGER.info("Query language is: " + result.getLanguage());
			
			if (result.getLanguage().equals("eng")) {
				

				
				GraphAPI_EN graphen = crawlerService.getGraphApi_EN();
				
				//String [] splittedsentence = message.getSearchString().split(" ");
				
				Vector querywords = new Vector();
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					
					LOGGER.info("All nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						LOGGER.info(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}
					
					for (final Word word : nouns /*sent.getWords()*/) {
						
						querywords.add(word.getStemmedWord().toLowerCase());
						
					}
				
				}
				
				LOGGER.info("Querywords: " +querywords.toString());
				
				if (querywords.size()>1) {
					
					HashMap curresult = (HashMap) graphen.getCentroidbySpreadingActivation(querywords);
	
					if (curresult.containsKey("centroid")) {
						
						centroid = (String)curresult.get("centroid");
						
					} else {
						
						centroid = (String) querywords.get(0);
						
					}
					
					
				}
				
				
						
			} else if (result.getLanguage().equals("deu") ) {
				
				GraphAPI_DE graphde = crawlerService.getGraphApi_DE();
				
				//String [] splittedsentence = message.getSearchString().split(" ");
				
				Vector querywords = new Vector();
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					
					LOGGER.info("All German nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						LOGGER.info(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}
					
					for (final Word word : nouns /*sent.getWords()*/) {
						
						querywords.add(word.getStemmedWord().toLowerCase());
						
					}
				
				}
				
				LOGGER.info("German Querywords: " +querywords.toString());
				
				
				if (querywords.size()>1) {
					
					HashMap curresult = (HashMap) graphde.getCentroidbySpreadingActivation(querywords);
	
					if (curresult.containsKey("centroid")) {
						
						centroid = (String)curresult.get("centroid");
						
					} else {
						
						centroid = (String) querywords.get(0);
						
					}
					
					
				}
				
				
				
				
			}

		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		return centroid;
		
	}
	
	
	/**
	 * 
	 * @param message
	 *            a search request
	 * @return a list of documents matching the search request delivered by the
	 *         message parameter.
	 */
	public List<ViewDocument> search(SearchRequest message) {

		LOGGER.info("-> search()");
		System.setProperty("http.keepAlive", "false");
         
		List<ViewDocument> result = new ArrayList<ViewDocument>();
		if (processedMessageIds.containsKey(message.getId()) || message.getSearchString() == null
				|| message.getSearchString().trim().isEmpty()) {
			LOGGER.info("already processed this message or it contains no search string");
	
			
			synchronized(processedMessageIds) {
				
			 List<String> keys = new ArrayList<String>();
			 
			 keys.addAll(processedMessageIds.keySet());
			
			 for(int i=0; i<keys.size(); i++) {
				 
				if (processedMessageIds.containsKey(keys.get(i))) {
				 
					Timestamp oldmsgtimestamp = processedMessageIds.get(keys.get(i));
		            
		            Timestamp curtimestamp = new Timestamp(System.currentTimeMillis());
		            
		            long helptime = oldmsgtimestamp.getTime();
		            long helptime2 = curtimestamp.getTime();
		            
		            long diff = (helptime2 - helptime);
		           
		            if (diff>60000) { 
		            	
		            	processedMessageIds.remove(keys.get(i));
		            	LOGGER.info("Deleting old query id");
		            	
		            }
	            
				}
			 }
		
			}
		
		} else {
			
			
			
			synchronized(processedMessageIds) {
				
				 List<String> keys = new ArrayList<String>();
				 
				 keys.addAll(processedMessageIds.keySet());
				
				 for(int i=0; i<keys.size(); i++) {
					 
					if (processedMessageIds.containsKey(keys.get(i))) {
					 
						Timestamp oldmsgtimestamp = processedMessageIds.get(keys.get(i));
			            
			            Timestamp curtimestamp = new Timestamp(System.currentTimeMillis());
			            
			            long helptime = oldmsgtimestamp.getTime();
			            long helptime2 = curtimestamp.getTime();
			            
			            long diff = (helptime2 - helptime);
			           
			            if (diff>60000) { 
			            	
			            	processedMessageIds.remove(keys.get(i));
			            	LOGGER.info("Deleting old query id");
			            }
		            
					}
				 }
			
				}
			
			
			

			
			Timestamp curtimestamp = new Timestamp(System.currentTimeMillis());

			processedMessageIds.put(message.getId(),curtimestamp);

			try {
				// local search
				
				LOGGER.info("Local full-text search init: " );
				
				try {
				
				result.addAll(luceneService.search(message.getSearchString()));

				Vector<Entry> localentries = luceneService.getLocalEntries();
				
				LOGGER.info("Local Centroid (entry) search init: " + localentries.size());
				
				
				for (int i =0; i<localentries.size(); i++) {
					
					Entry entry = (Entry)localentries.get(i);
										
					if (entry.getKey().equals(message.getCentroid())) {
						
						ViewDocument vd = new ViewDocument();
						
						vd.setHref(entry.getValue());
						vd.setCentroidsearch(true);
						
						
						String origtitle=entry.getValue();
						String title=origtitle;
						URL url = new URL(entry.getValue());
						int port=url.getPort();
						String host = url.getHost();
						//String protocol = url.getProtocol()
						
						if (port!=-1) {
							
							String helpstring = ":"+port;
							
							title=origtitle.replaceFirst(helpstring, "");
						
						}
						
						title=title.replaceFirst(host, "[..]");
						
						int helpindex = title.indexOf("[..]");
						String protocol = title.substring(0, helpindex );
						title=title.replaceFirst(protocol, "");
						
						
						if (!title.equals("")) {
						
							vd.setTitle(title);  //!!! set from nlp
							vd.setScore(1.0f);
	
							
							if (!peername.equals("")) {
								
								if (peername.length()>20) 
									peername=peername.substring(0,16)+"...";
								
								vd.setTitle(peername+": " +title);
								
							//	vd.setSourcePeer(peername);
								
							}
							
							
							if (result.size()<20)
							result.add(vd);
						
						}
					}
					
					
					
				} //for 
				
				} catch (Exception luceneexp ) 
				{ 
					LOGGER.warn("Error locally searching for " + message.getSearchString(), luceneexp);
				};
				
				
				
				
				
				
				
				
				
				
				
				//Forwarding query
				if (message.getTtl() > 0) //for flooding search (forwarding)
				{
					// remote search
					message.setTtl(message.getTtl() - 1);
				
					//always perform flooding search anyways
					LOGGER.info("Flooding Search Init: " );

											
					LOGGER.info("Forwarding query to normal neighbours: " );
						
						// remote search
						message.setTtl(message.getTtl() - 1);
						Map<String, String> postDataParams = new HashMap<String, String>();
						postDataParams.put("message", ServiceHelper.parseToJson(message));

						// flooding search 
						List<String> neighbourList = new ArrayList<String>(neighbourhoodService.getNeighbours());
						for (int count = 0; count < Math.min(numberOfForwards, neighbourList.size()); count++) {
							String neighbour = neighbourList.get(count);
							String url = helper.BuildUrl(neighbour, "searchDocuments.html");

							String response = performPostCall(url, postDataParams);
							SearchResponse responseObject = ServiceHelper.parseFromJson(response, SearchResponse.class);
							if (responseObject != null) {
								result.addAll(responseObject.getSearchDetails());
							}
						} 
						

					
			//	}
				
				

						
					} //ttl
				
			} catch (Exception e) {
				LOGGER.warn("error searching for " + message.getSearchString(), e);
			}
		}
		return result;
	}


	/**
	 * perfomrs a http post call to the request Url
	 * 
	 * @param requestURL
	 *            Url, where the call should be send to
	 * @param postDataParams
	 *            Dictionary of parameter the should be added to the post call
	 * @return the reponse of the http post call
	 */
	public String performPostCall(String requestURL, Map<String, String> postDataParams) {

		URL url;
		String response = "";
		
		
		
		
		HttpURLConnection conn = null;	
		OutputStream os = null;
		InputStream is = null;
		InputStreamReader isr = null;
		
		BufferedReader rd = null;
		BufferedWriter writer = null;
		OutputStreamWriter osw = null;
		
		
		
		
		
		
		
		try {
			url = new URL(requestURL);

			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			os = conn.getOutputStream();
			osw = new OutputStreamWriter(os, "UTF-8");
			writer = new BufferedWriter(osw);
			
			writer.write(getPostDataString(postDataParams));

			writer.flush();
			
			
			
			int responseCode = conn.getResponseCode();

			if (responseCode == HttpsURLConnection.HTTP_OK) {
				
				is = conn.getInputStream();
				
				isr = new InputStreamReader(is);
				
				rd = new BufferedReader(isr);
				
				
				
				String line;
				while ((line = rd.readLine()) != null) {
					response += line;
				}
				
			//  rd.close();
			} 
			
			
		} catch (Exception e) {
			LOGGER.warn(String.format("Error loading documents from " + requestURL, e.getMessage()));
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

		
		

		return response;
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
}
