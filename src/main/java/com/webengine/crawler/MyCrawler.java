package com.webengine.crawler;



import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.tika.parser.txt.CharsetDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//import com.aliasi.matrix.Vector;
import com.webengine.INeighbourhoodService;
import com.webengine.textprocessing.Sentence;
import com.webengine.textprocessing.TextProcessingResult;
import com.webengine.textprocessing.Word;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.validator.*;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * this class is managed by the crawler framework and not by spring
 * 
 * @author robert
 *
 */
	


@Component
public class MyCrawler extends WebCrawler {
	
	@Autowired
	Crawler crawler;
	
	
	
	
	private final static Pattern FILTERS = Pattern
			.compile(".*(\\.(css|js|gif|jpg|pdf" + "|png|mp3|mp3|zip|gz))$");
	
	private final static Logger LOGGER = Logger.getLogger(MyCrawler.class.getName()); 

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {

		String href = url.getURL().toLowerCase();
		
		LOGGER.info("Shouldvisit: " + href);;
		
		//Crawler mycrawler = new Crawler();
		
		String configuredDomain = ((CrawlController) myController).getCurrentCrawler().getLocalDomain().toLowerCase();
		boolean result = false;
		if (configuredDomain != null && configuredDomain.trim().length()> 0){
			 result = !FILTERS.matcher(href).matches()
					&& href.startsWith(configuredDomain);	
		} else {
			IpManager ipmanager = ((CrawlController) myController).getCurrentCrawler().getIpManager();
			List<String> localIps =ipmanager.getLocalIps();
			
			List<String> remoteIps = new ArrayList<String>(); 
			boolean localipinurl = false;
			
			for (int i = 0; i<localIps.size(); i++) {
				
				if (href.contains(localIps.get(i).toString())) {
					
					remoteIps.add(localIps.get(i).toString());
					localipinurl=true;
				}
				
			}
					
			if (!localipinurl)	{
			remoteIps =ipmanager.getIps(url.getDomain());
			}
			
			for (String remoteIp : remoteIps) {
				result = result || localIps.contains(remoteIp);
			}						
		}		
		
		
		LOGGER.info("Result is: " +result);
		
		return result;
	}

	

    
	
	
	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		
		
		HashMap<String, Integer> wordcounts = new HashMap<String, Integer>();
		
		LOGGER.info("-> visit("+page.getWebURL().toString()+")");
		
		
		//CrawlerController crawlerController = ((CrawlerController) this.myController);
		
				CrawlController crawlerController = ((CrawlController) this.myController);
				
				
				
		
		
		WebURL wurl = new WebURL();
		
		try {
		wurl.setURL(URLDecoder.decode(page.getWebURL().toString(), "UTF-8"));
		} catch (Exception exp) {}
		
		if ((wurl.getURL()!=null) && (wurl.getURL().length()>0) ) {
			page.setWebURL(wurl);
			

			URL crawlurl;
			try {
				crawlurl = new URL(page.getWebURL().getURL());
				String host = crawlurl.getHost();
			

				String externalip = crawlerController.getCurrentCrawler().getExternalLocalHost();
				
				if ((externalip != null)  && (!externalip.equals(""))) 
				{	
					
					LOGGER.info("Replaced local crawling URL with external router URL...");
					
					String cururl = page.getWebURL().toString();
					
					cururl = cururl.replace( host, externalip);
					
					WebURL wurl2 = new WebURL();
					wurl2.setURL(cururl);
					
					page.setWebURL(wurl2);
					
				}
				
				
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		//URLDecoder.decode(String s, String enc)
		 
		
		
		
		int createGraphDatabase = crawlerController.getCurrentCrawler().getCreateGraphDatabase();
		LOGGER.info("Creategraphdb " + createGraphDatabase);
		
		
		if (page.getParseData() instanceof BinaryParseData) {
			BinaryParseData binaryParseData = (BinaryParseData) page.getParseData();
			LOGGER.info("BinaryParseData "+binaryParseData.getHtml());
			LOGGER.info("OutgoingLinks "+binaryParseData.getOutgoingUrls());
	
		//	crawlerController.getCurrentCrawler().getLuceneService()
		//	.addDocument(binaryParseData.getHtml(),
		//			page.getWebURL().getURL(),page.getWebURL().getURL(), "");
		
			
			
			try {
				
				
				CharsetDetector detector = new CharsetDetector();
				detector.setText(binaryParseData.getHtml().getBytes());
				detector.detect();
				
				LOGGER.info("Charset of binary data: " + detector.detect().getName()  + " " +detector.detect().getString()) ;
				
				
				
				
				TextProcessingResult result;
				
					result = crawlerController.getCurrentCrawler().getTextProcessor().process(binaryParseData.getHtml());
					
					if (result.getLanguage().equals("deu") || result.getLanguage().equals("eng")) {
					
						String title="";
					
					
					result.setUri(page.getWebURL().toString());
					
					String filename="";
					try {
						URL url = new URL(page.getWebURL().getURL());
						filename=url.getFile();
						
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
						result.setTitle(page.getWebURL().getURL());
					
					
					
					boolean docingraphdb = false;
					
					if (createGraphDatabase==1) {
					
					if (result.getLanguage().equals("eng")) {
						
						
						if (!filename.equals(""))	{				
						docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(filename);
						
							if (!docingraphdb)
							crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(filename);
						
						} else {
							
							docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(result.getUri());
							
							if (!docingraphdb)
								crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(result.getUri());
							
						}
						
					}
						
						if (result.getLanguage().equals("deu")) {
							if (!filename.equals(""))	{				
								docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(filename);
								
									if (!docingraphdb)
									crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(filename);
								
								} else {
									
									docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(result.getUri());
									
									if (!docingraphdb)
										crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(result.getUri());
									
								}
		
					}	
					
						
						
						if (!docingraphdb)
							crawlerController.getCurrentCrawler().setGraphDBWritten(true);
						
						
						
						
						
					}
						
						
						
						
						
						
				
				//	LOGGER.info("TextProcessing Result: " + result.getSentences());
					
					for (final Sentence sent : result.getSentences()){
						
						List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
						
						for (final Word word : nouns /*sent.getWords()*/){
							
							//LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

							if (word.getTag().startsWith("N")) {
								
								if (wordcounts.containsKey(word.getStemmedWord())) {
									
									int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
									value++;
									Integer inte = new Integer(value);
									wordcounts.put(word.getStemmedWord(), inte);
									
								} else {
									
									int value =1;
									Integer inte = new Integer(value);
									wordcounts.put(word.getStemmedWord(), inte);
									
								}
								
								
							}
							
							
						}
						
						
						if (nouns.size()<=15) {
						
					//	LOGGER.info("All nouns in sentence: "+nouns);
						//for (int i=0; i<nouns.size(); i++) {
						//	System.out.print(nouns.get(i).getStemmedWord() + "   ");
						
						//	LOGGER.info("");
							
						//}
						
						//update nodes
						if (createGraphDatabase==1) {
						if (!docingraphdb)
						for (final Word word : nouns /*sent.getWords()*/) {
							if (result.getLanguage().equals("eng")) {
							crawlerController.getCurrentCrawler().getGraphApi_EN().addNode(word);
							}
								
								if (result.getLanguage().equals("deu")) {
								crawlerController.getCurrentCrawler().getGraphApi_DE().addNode(word);
								
								
							}	
								
						}
						}	
							
						//update relations
						for (final Word word : nouns /*sent.getWords()*/){
							
						//	LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

							if (word.getTag().startsWith("N")) {
								
								List<Word> othernouns = nouns.stream().filter(currword -> currword!=word).collect(Collectors.toList());;
							//	LOGGER.info("All nouns in sentence except current word/noun: "+othernouns);
								for (int i=0; i<othernouns.size(); i++)
								//	System.out.print(othernouns.get(i).getStemmedWord() + "   ");
								
								//	LOGGER.info("");
								
									if (createGraphDatabase==1) {
								if (!docingraphdb)
								if (result.getLanguage().equals("deu")) {
									
									for (Word otherword : othernouns){
										// eine nennung von 2 wörten in einem Satz
									crawlerController.getCurrentCrawler().getGraphApi_DE().addRelation(word, otherword, 1);
									}
								
								}
								
								if (!docingraphdb)
								if (result.getLanguage().equals("eng")) {
									
									for (Word otherword : othernouns){
										// eine nennung von 2 wörten in einem Satz
										crawlerController.getCurrentCrawler().getGraphApi_EN().addRelation(word, otherword, 1);
									}
								
								}
									}
								
								
								
							/*	if (wordcounts.containsKey(word.getStemmedWord())) {
									
									int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
									value++;
									Integer inte = new Integer(value);
									wordcounts.put(word.getStemmedWord(), inte);
									
								} else {
									
									int value =1;
									Integer inte = new Integer(value);
									wordcounts.put(word.getStemmedWord(), inte);
									
								} */
								
								
							}
							
							
						}
						}
					}  //for sentences
					
					
					if (createGraphDatabase==1) {
					if (!docingraphdb)
					if (result.getLanguage().equals("eng")) {
						//crawlerController.getCurrentCrawler().getGraphApi_EN().updateDiceandCosts();
					}
					if (!docingraphdb)
						if (result.getLanguage().equals("deu")) {
						//crawlerController.getCurrentCrawler().getGraphApi_DE().updateDiceandCosts();
						
						
					}	
					}
					
					if (createGraphDatabase==1) {
					//	crawlerController.getCurrentCrawler().getGraphApi_DE().restartDB();
						//crawlerController.getCurrentCrawler().getGraphApi_EN().restartDB();
					}
					
						HashMap<String, Double> sortedMap2 = sortByValuesDec(wordcounts);
						
						
						//LOGGER.info(sortedMap2);
			    	  					
						
						int count = 0;
						Vector query = new Vector();
						
						for (String key : sortedMap2.keySet()) {			
								  
							 // if (sortedMap2.containsKey(key))
					    	 // LOGGER.info("Wortcount: " + key + "  " + sortedMap2.get((String)key));
							if (key.trim().length()>0) {
						    	  count++;
						    	  
						    	  if (count <5)
						    		  query.add(key.trim());
								}
					      }	
						
						
						
						
						String centroid = "";
						
						//if (!docingraphdb)
						if (result.getLanguage().equals("eng")) {
													
							//centroid = (String) crawlerController.getGraphApi_EN().getCentroidbySpreadingActivation(query).get("centroid");
						}
							
							if (result.getLanguage().equals("deu")) {
							//	centroid = (String)crawlerController.getGraphApi_DE().getCentroidbySpreadingActivation(query).get("centroid");
							
							
						}	
						
						
		
						
					
					int largestcount = 0;
					String mostfrequentnoun = "";
				
					for (Map.Entry<String, Integer> entry : wordcounts.entrySet())
					{
					    //LOGGER.info(entry.getKey() + "/" + entry.getValue());
					    
						int value = ((Integer)entry.getValue()).intValue();
					    if (value > largestcount) {
					    	
					    	String trimmed = entry.getKey().trim();
					    	
					    	if (trimmed!=null && trimmed.length()>0 && !trimmed.equals(" ")) {
						    	
					    		largestcount= value;
						    	mostfrequentnoun = trimmed;
					    	
					    	}
					    }
					    
					}
				
					LOGGER.info("TextProcessing Centroid: " + mostfrequentnoun.toLowerCase());
				
		
					if (centroid.equals(""))
						centroid = mostfrequentnoun.toLowerCase();
					
					LOGGER.info("Final document Centroid: " + centroid.toLowerCase());
					
					
					
					String origtitle=page.getWebURL().getURL();
					title=origtitle;
					URL url = new URL(page.getWebURL().getURL());
					int port=url.getPort();
					String host = url.getHost();
							
					
					if (port!=-1) {
						
						String helpstring = ":"+port;
						
						title=origtitle.replaceFirst(helpstring, "");
					
					}
					
					title=title.replaceFirst(host, "[..]");
					
					int helpindex = title.indexOf("[..]");
					String protocol = title.substring(0, helpindex );
					title=title.replaceFirst(protocol, "");
					
					if (title.equals("")) 
						title="Link";
					
					
					
					
					crawlerController.getCurrentCrawler().getLuceneService()
						.addDocument(binaryParseData.getHtml(),
								page.getWebURL().getURL(),title, centroid);

					
					//handle outgoing links
					
					
				}
				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			
			
			
			
			
		} //PDF
		else 
		
		
		if (page.getParseData() instanceof TextParseData) {
			TextParseData textParseData = (TextParseData) page.getParseData();
			
			
			//LOGGER.info("textParseData: " + page.getContentCharset());
			//new String(s.getBytes("ISO-8859-1"), "UTF-8");
			
			//new String(textParseData.getTextContent().getBytes("ISO-8859-1"), "UTF-8");

			
			try {
				
				String plaintext=textParseData.getTextContent();
						//new String(textParseData.getTextContent().getBytes(), "UTF-8");
				//LOGGER.info("textParseData: " + plaintext + " default: " +Charset.defaultCharset() );
				
				CharsetDetector detector = new CharsetDetector();
				detector.setText(plaintext.getBytes());
				detector.detect();
				
				LOGGER.info("Charset of text: " + detector.detect().getName()  + " " +detector.detect().getString()) ;
				
				String converted="";
				
				converted = detector.detect().getString();
				
				
			//	LOGGER.info("Alternative: " + detector.getString(plaintext.getBytes(), detector.detect().getName()));
				
				
				
			TextProcessingResult result;
			
				result = crawlerController.getCurrentCrawler().getTextProcessor().process(converted/*detector.getString(plaintext.getBytes(), detector.detect().getName())*/);
				
				if (result.getLanguage().equals("deu") || result.getLanguage().equals("eng")) {
				
					String title="";
				
				
				result.setUri(page.getWebURL().toString());
				
				String filename="";
				try {
					URL url = new URL(page.getWebURL().getURL());
					filename=url.getFile();
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				result.setTitle(page.getWebURL().getURL());
				
				
				
				boolean docingraphdb = false;
				
				if (createGraphDatabase==1) {
				
				if (result.getLanguage().equals("eng")) {
					
					
					if (!filename.equals(""))	{				
					docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(filename);
					
						if (!docingraphdb)
						crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(filename);
					
					} else {
						
						docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(result.getUri());
						
						if (!docingraphdb)
							crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(result.getUri());
						
					}
					
				}
					
					if (result.getLanguage().equals("deu")) {
						if (!filename.equals(""))	{				
							docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(filename);
							
								if (!docingraphdb)
								crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(filename);
							
							} else {
								
								docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(result.getUri());
								
								if (!docingraphdb)
									crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(result.getUri());
								
							}
	
				}	
					
					
					
					if (!docingraphdb)
						crawlerController.getCurrentCrawler().setGraphDBWritten(true);
					

				
				} //createGraphDatabase
					
					
					
					
					
					
			
			//	LOGGER.info("TextProcessing Result: " + result.getSentences());
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					for (final Word word : nouns /*sent.getWords()*/){
						
						//LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

						if (word.getTag().startsWith("N")) {
							
							if (wordcounts.containsKey(word.getStemmedWord())) {
								
								int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
								value++;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							} else {
								
								int value =1;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							}
							
							
						}
						
						
					}
					
					
					
					if (nouns.size()<=15) {
					/*LOGGER.info("All nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						System.out.print(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}*/
					
					//update nodes
					if (createGraphDatabase==1) {
					if (!docingraphdb)
					for (final Word word : nouns /*sent.getWords()*/) {
						if (result.getLanguage().equals("eng")) {
						crawlerController.getCurrentCrawler().getGraphApi_EN().addNode(word);
						}
							
							if (result.getLanguage().equals("deu")) {
							crawlerController.getCurrentCrawler().getGraphApi_DE().addNode(word);
							
							
						}	
							
					}
					}	
						
					//update relations
					for (final Word word : nouns /*sent.getWords()*/){
						
					//	LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

						if (word.getTag().startsWith("N")) {
							
							List<Word> othernouns = nouns.stream().filter(currword -> currword!=word).collect(Collectors.toList());;
						//	LOGGER.info("All nouns in sentence except current word/noun: "+othernouns);
							for (int i=0; i<othernouns.size(); i++)
							//	System.out.print(othernouns.get(i).getStemmedWord() + "   ");
							
						//		LOGGER.info("");
							
								if (createGraphDatabase==1) {
							if (!docingraphdb)
							if (result.getLanguage().equals("deu")) {
								
								for (Word otherword : othernouns){
									// eine nennung von 2 wörten in einem Satz
								crawlerController.getCurrentCrawler().getGraphApi_DE().addRelation(word, otherword, 1);
								}
							
							}
							
							if (!docingraphdb)
							if (result.getLanguage().equals("eng")) {
								
								for (Word otherword : othernouns){
									// eine nennung von 2 wörten in einem Satz
									crawlerController.getCurrentCrawler().getGraphApi_EN().addRelation(word, otherword, 1);
								}
							
							}
								}
							
							
							/*
							if (wordcounts.containsKey(word.getStemmedWord())) {
								
								int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
								value++;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							} else {
								
								int value =1;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							}*/
							
							
						}
						
						
					}
					}
				}  //for sentences
				
				
				if (createGraphDatabase==1) {
				if (!docingraphdb)
				if (result.getLanguage().equals("eng")) {
					//crawlerController.getCurrentCrawler().getGraphApi_EN().updateDiceandCosts();
				}
				if (!docingraphdb)
					if (result.getLanguage().equals("deu")) {
					//crawlerController.getCurrentCrawler().getGraphApi_DE().updateDiceandCosts();
					
					
				}	
				}
				
				if (createGraphDatabase==1) {
				//	crawlerController.getCurrentCrawler().getGraphApi_DE().restartDB();
				//	crawlerController.getCurrentCrawler().getGraphApi_EN().restartDB();
				}
					
					HashMap<String, Double> sortedMap2 = sortByValuesDec(wordcounts);
					
					
				//	LOGGER.info(sortedMap2);
		    	  					
					
					int count = 0;
					Vector query = new Vector();
					
					for (String key : sortedMap2.keySet()) {			
							  
						 // if (sortedMap2.containsKey(key))
				    	 // LOGGER.info("Wortcount: " + key + "  " + sortedMap2.get((String)key));
						
						if (key.trim().length()>0) {
				    	  count++;
				    	  
				    	  if (count <5)
				    		  query.add(key.trim());
						}
				    	  
				    	  
				      }	
					
					
					
					
					String centroid = "";
					
					//if (!docingraphdb)
					if (result.getLanguage().equals("eng")) {
												
						//centroid = (String) crawlerController.getGraphApi_EN().getCentroidbySpreadingActivation(query).get("centroid");
					}
						
						if (result.getLanguage().equals("deu")) {
						//	centroid = (String)crawlerController.getGraphApi_DE().getCentroidbySpreadingActivation(query).get("centroid");
						
						
					}	
					
					
	
					
				
				int largestcount = 0;
				String mostfrequentnoun = "";
			
				for (Map.Entry<String, Integer> entry : wordcounts.entrySet())
				{
				   // LOGGER.info(entry.getKey() + "/" + entry.getValue());
				    
					int value = ((Integer)entry.getValue()).intValue();
				    if (value > largestcount) {
				    	
				    	String trimmed = entry.getKey().trim();
				    	
				    	if (trimmed!=null && trimmed.length()>0 && !trimmed.equals(" ")) {
					    	
				    		largestcount= value;
					    	mostfrequentnoun = trimmed;
				    	
				    	}
				    	
				    }
				    
				}
			
				LOGGER.info("TextProcessing Centroid: " + mostfrequentnoun.toLowerCase());
			
	
				if (centroid.equals(""))
					centroid = mostfrequentnoun.toLowerCase();
				
				LOGGER.info("Final document Centroid: " + centroid.toLowerCase());
				
				
				
				String origtitle=page.getWebURL().getURL();
				title=origtitle;
				URL url = new URL(page.getWebURL().getURL());
				int port=url.getPort();
				String host = url.getHost();
						
				
				if (port!=-1) {
					
					String helpstring = ":"+port;
					
					title=origtitle.replaceFirst(helpstring, "");
				
				}
				
				title=title.replaceFirst(host, "[..]");
				
				int helpindex = title.indexOf("[..]");
				String protocol = title.substring(0, helpindex );
				title=title.replaceFirst(protocol, "");
				
				
				
				if (title.equals("")) 
					title="Link";
				
				
				
				
				/*
				
				CharsetDetector detector2 = new CharsetDetector();
				detector2.setText(converted.getBytes());
				detector2.detect();
				
				LOGGER.info("Charset of converted text: " + detector2.detect().getName()  + " " +detector2.detect().getString()) ;
				
				
				String decodedToUTF8 = new String(converted.getBytes(detector2.detect().getName()), "UTF-8");
				
				LOGGER.info("decoded: " + decodedToUTF8);
				*/
				
				
				crawlerController.getCurrentCrawler().getLuceneService()
					.addDocument(converted,
							page.getWebURL().getURL(),title, centroid);

				//handle outgoing links
				
				
				
			}
			
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			
		} //Text Daten 
		else
		
		
		
		
		
		
		
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			
			//LOGGER.info("htmlParseData: " + htmlParseData.getText() + " default: " +Charset.defaultCharset() );
			
			
			try {
				
				
				CharsetDetector detector = new CharsetDetector();
				detector.setText(htmlParseData.getText().getBytes());
				detector.detect();
				
				LOGGER.info("Charset of html data: " + detector.detect().getName()  + " " +detector.detect().getString()) ;
				
				
			TextProcessingResult result;
			
				result = crawlerController.getCurrentCrawler().getTextProcessor().process(htmlParseData.getText());
				
				if (result.getLanguage().equals("deu") || result.getLanguage().equals("eng")) {
				
					String title="";
					
				result.setTitle(htmlParseData.getTitle());
				result.setUri(page.getWebURL().toString());
				
				String filename="";
				try {
					URL url = new URL(page.getWebURL().getURL());
					filename=url.getFile();
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				boolean docingraphdb = false;
				
				
				if (createGraphDatabase==1) {
				if (result.getLanguage().equals("eng")) {
					
					
					if (!filename.equals(""))	{				
					docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(filename);
					
						if (!docingraphdb)
						crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(filename);
					
					} else {
						
						docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_EN().checkDocumentNode(result.getUri());
						
						if (!docingraphdb)
							crawlerController.getCurrentCrawler().getGraphApi_EN().addDocumentNode(result.getUri());
						
					}
					
				}
					
					if (result.getLanguage().equals("deu")) {
						if (!filename.equals(""))	{				
							docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(filename);
							
								if (!docingraphdb)
								crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(filename);
							
							} else {
								
								docingraphdb = crawlerController.getCurrentCrawler().getGraphApi_DE().checkDocumentNode(result.getUri());
								
								if (!docingraphdb)
									crawlerController.getCurrentCrawler().getGraphApi_DE().addDocumentNode(result.getUri());
								
							}
	
				}	
					
					
					if (!docingraphdb)
						crawlerController.getCurrentCrawler().setGraphDBWritten(true);
					
				
				}
					
					
					
					
					
					
			
			//	LOGGER.info("TextProcessing Result: " + result.getSentences());
				
				for (final Sentence sent : result.getSentences()){
					
					List<Word> nouns = sent.getWords().stream().filter(currword -> currword.getTag().startsWith("N")).collect(Collectors.toList());;
					
					/*
					LOGGER.info("All nouns in sentence: "+nouns);
					for (int i=0; i<nouns.size(); i++) {
						System.out.print(nouns.get(i).getStemmedWord() + "   ");
					
						LOGGER.info("");
						
					}*/
					
					for (final Word word : nouns /*sent.getWords()*/){
						
						//LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

						if (word.getTag().startsWith("N")) {
							
							if (wordcounts.containsKey(word.getStemmedWord())) {
								
								int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
								value++;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							} else {
								
								int value =1;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							}
							
							
						}
						
						
					}
					
					
					
					
					
					if (nouns.size()<=15) {
					
					//update nodes
					
					if (createGraphDatabase==1) {
					if (!docingraphdb)
					for (final Word word : nouns /*sent.getWords()*/) {
						if (result.getLanguage().equals("eng")) {
							crawlerController.getCurrentCrawler().getGraphApi_EN().addNode(word);
						}
							
							if (result.getLanguage().equals("deu")) {
								crawlerController.getCurrentCrawler().getGraphApi_DE().addNode(word);
							
							
						}	
							
					}
					}	
						
					//update relations
					for (final Word word : nouns /*sent.getWords()*/){
						
						//LOGGER.info("Word: " + word+ " " + word.getStemmedWord() + " Tag: "+ word.getTag());

						if (word.getTag().startsWith("N")) {
							
							List<Word> othernouns = nouns.stream().filter(currword -> currword!=word).collect(Collectors.toList());;
						//	LOGGER.info("All nouns in sentence except current word/noun: "+othernouns);
						//	for (int i=0; i<othernouns.size(); i++)
							//	System.out.print(othernouns.get(i).getStemmedWord() + "   ");
							
								//LOGGER.info("");
							if (createGraphDatabase==1) {
							if (!docingraphdb)
							if (result.getLanguage().equals("deu")) {
								
								for (Word otherword : othernouns){
									// eine nennung von 2 wörten in einem Satz
									crawlerController.getCurrentCrawler().getGraphApi_DE().addRelation(word, otherword, 1);
								}
							
							}
							
							if (!docingraphdb)
							if (result.getLanguage().equals("eng")) {
								
								for (Word otherword : othernouns){
									// eine nennung von 2 wörten in einem Satz
									crawlerController.getCurrentCrawler().getGraphApi_EN().addRelation(word, otherword, 1);
								}
							
							}
							}
							
							
							/*
							if (wordcounts.containsKey(word.getStemmedWord())) {
								
								int value = ((Integer)wordcounts.get(word.getStemmedWord())).intValue();
								value++;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							} else {
								
								int value =1;
								Integer inte = new Integer(value);
								wordcounts.put(word.getStemmedWord(), inte);
								
							}*/
							
							
						}
						
						
					}
					}
				}  //for sentences
				
				if (createGraphDatabase==1) {
				if (!docingraphdb)
				if (result.getLanguage().equals("eng")) {
				//	crawlerController.getCurrentCrawler().getGraphApi_EN().updateDiceandCosts();
				}
				if (!docingraphdb)
					if (result.getLanguage().equals("deu")) {
					//	crawlerController.getCurrentCrawler().getGraphApi_DE().updateDiceandCosts();
					
					
				}	
				}
				
					
				if (createGraphDatabase==1) {
					//	crawlerController.getCurrentCrawler().getGraphApi_DE().restartDB();
					//	crawlerController.getCurrentCrawler().getGraphApi_EN().restartDB();
					}
				
				
					HashMap<String, Double> sortedMap2 = sortByValuesDec(wordcounts);
					
					
					LOGGER.info("Most frequent words: "+sortedMap2);
		    	  					
					
					int count = 0;
					Vector query = new Vector();
					
					for (String key : sortedMap2.keySet()) {			
							  
						 // if (sortedMap2.containsKey(key))
				    	 // LOGGER.info("Wortcount: " + key + "  " + sortedMap2.get((String)key));
							if (key.trim().length()>0) {
					    	  count++;
					    	  
					    	  if (count <5)
					    		  query.add(key.trim());
							}
				      }	
					
					
					
					
					String centroid = "";
					
					//if (!docingraphdb)
					if (result.getLanguage().equals("eng")) {
												
						//centroid = (String) crawlerController.getCurrentCrawler().getGraphApi_EN().getCentroidbySpreadingActivation(query).get("centroid");
					}
						
						if (result.getLanguage().equals("deu")) {
						//	centroid = (String)crawlerController.getGraphApi_DE().getCentroidbySpreadingActivation(query).get("centroid");
						
						
					}	
					
					
	
					
				
				int largestcount = 0;
				String mostfrequentnoun = "";
		
				for (Map.Entry<String, Integer> entry : wordcounts.entrySet())
				{
				   // LOGGER.info(entry.getKey() + "/" + entry.getValue());
				    
					int value = ((Integer)entry.getValue()).intValue();
				    if (value > largestcount) {
				    	
				    	String trimmed = entry.getKey().trim();
				    	
				    	if (trimmed!=null && trimmed.length()>0 && !trimmed.equals(" ")) {
					    	
				    		largestcount= value;
					    	mostfrequentnoun = trimmed;
				    	
				    	}
				    }
				    
				}
			
				
				if (query.contains("h5n1") || query.contains("H5N1"))
					mostfrequentnoun="h5n1";
					
				
				
				LOGGER.info("TextProcessing Centroid: " + mostfrequentnoun.toLowerCase());
			

				if (centroid.equals(""))
					centroid = mostfrequentnoun.toLowerCase();
				
				LOGGER.info("Final document Centroid: " + centroid.toLowerCase());
				

				
				
				if (htmlParseData.getTitle()==null) {
					
					String origtitle=page.getWebURL().getURL();
					title=origtitle;
					URL url = new URL(page.getWebURL().getURL());
					int port=url.getPort();
					String host = url.getHost();
							
					
					if (port!=-1) {
						
						String helpstring = ":"+port;
						
						title=origtitle.replaceFirst(helpstring, "");
					
					}
					
					title=title.replaceFirst(host, "[..]");
					
					int helpindex = title.indexOf("[..]");
					String protocol = title.substring(0, helpindex );
					title=title.replaceFirst(protocol, "");
					
										
					
					
					
				} else {
					
					if  (htmlParseData.getTitle().equals("")) {
						
						String origtitle=page.getWebURL().getURL();
						title=origtitle;
						URL url = new URL(page.getWebURL().getURL());
						int port=url.getPort();
						String host = url.getHost();
								
						
						if (port!=-1) {
							
							String helpstring = ":"+port;
							
							title=origtitle.replaceFirst(helpstring, "");
						
						}
						
						title=title.replaceFirst(host, "[..]");
						
						int helpindex = title.indexOf("[..]");
						String protocol = title.substring(0, helpindex );
						title=title.replaceFirst(protocol, "");
	
						
					} else {
						
						title=htmlParseData.getTitle();
						
					}
					
				}
				
				
				

				if (title.equals("")) 
					title="Link";
				
				
				
				
				
				
				
				
				
				crawlerController.getCurrentCrawler().getLuceneService()
					.addDocument(htmlParseData.getText(),
							page.getWebURL().getURL(),title, centroid);

			}
			
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			
			
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			INeighbourhoodService neighbourhoodservice =  crawlerController.getCurrentCrawler().getNeighbourhodService();
			
			IpManager ipmanager = crawlerController.getCurrentCrawler().getIpManager();
			List<String> localIps =ipmanager.getLocalIps();
			//!!!was wenn keine domain, aber ips verlinkt sind

			
			InetAddressValidator ipvali = InetAddressValidator.getInstance();
			DomainValidator domainvali = DomainValidator.getInstance(true);
			
			
			for (WebURL webURL : links) {

				int port= -1;
				String host="";
				
				LOGGER.info("Checking: " + webURL.getURL());
				
				LOGGER.info("Subdomain: " + webURL.getSubDomain());
				
				LOGGER.info("Domain: "+webURL.getDomain());
				
				try {
					URL url2 = new URL(webURL.getURL());
					
					port=url2.getPort();
					host=url2.getHost();
					
					LOGGER.info("Port: "+url2.getPort());
					LOGGER.info("Host: "+url2.getHost());
				

					boolean localipinurl = false;
					boolean hostvalid= false;
					
					for (int i = 0; i<localIps.size(); i++) {
						
						if (webURL.getURL().contains(localIps.get(i).toString())) {
							
							LOGGER.info("Local IP directly in weburl");
							localipinurl=true;
							hostvalid=true;
						}
						
						
					}
				

					if (localipinurl==false) 
					if (!domainvali.isValid(host)) {
						
						LOGGER.info("Remote IP as host found...checking if valid");
						
						//ipv6 address?
						if (host.startsWith("[") && host.startsWith("]")) {
							
							String newhost = host.substring(1);
							
							newhost = newhost.substring(0, newhost.length() - 1);
							hostvalid= ipvali.isValid(newhost);
							
						} else {
			
							hostvalid = ipvali.isValid(host);

						}
						
					} else {
						
						hostvalid=true;
						
						LOGGER.info("Local IP maybe indirectly in host found...checking if so");
						
						List ipsfordomain = ipmanager.getIps(host);
						
						for (int i = 0; i<ipsfordomain.size(); i++) {
							
							if (localIps.contains(ipsfordomain.get(i).toString())) {
								
								LOGGER.info("Local IP indirectly in weburl");
								localipinurl=true;
								
							}
							
						}
						
					}	
					
				
					
				if (hostvalid==true) {	
					
				if (port!=-1) {
				//LOGGER.info("trying to add host with port: " + webURL.getSubDomain()+"."+webURL.getDomain());
					LOGGER.info("trying to add host with port: " + host+"."+port);
					neighbourhoodservice.addCandidate(host+":"+port);
					
				
				} else {
					//LOGGER.info("trying to add host: " + webURL.getSubDomain()+"."+webURL.getDomain());
					LOGGER.info("trying to add host without port: " + host);
					neighbourhoodservice.addCandidate(host);
					
				}
				
				/*
				if (localipinurl==false) {
					if (webURL.getSubDomain() != null && webURL.getSubDomain().trim().length() > 0){
						
						neighbourhoodservice.addCandidate(webURL.getSubDomain()+"."+webURL.getDomain());
						LOGGER.info("trying to add remote domain and subdomain: " + webURL.getSubDomain()+"."+webURL.getDomain());
						
					}else{
						neighbourhoodservice.addCandidate(webURL.getDomain());	
						LOGGER.info("trying to add remote domain : " + webURL.getDomain());
						
					}
				
				} else {
					
					
					neighbourhoodservice.addCandidate(webURL.getSubDomain()+"."+webURL.getDomain());
					
					LOGGER.info("trying to add local domain and subdomain and port: " + webURL.getSubDomain()+"."+webURL.getDomain());
					
					
					
				}*/
				
				
				
				}
				
				
				
				
				
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}  //read weburl
		} //HTML Daten
		
	//	if (createGraphDatabase==1) {
		//	crawlerController.getCurrentCrawler().getGraphApi_DE().restartDB();
			//crawlerController.getCurrentCrawler().getGraphApi_EN().restartDB();
		//}
		
		LOGGER.info("<- visit("+page.getWebURL().toString()+")");
	}
	
	
	
	private HashMap sortByValuesDec(HashMap map) { 
        List list = new LinkedList(map.entrySet());

        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                return -((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
             }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
               Map.Entry entry = (Map.Entry) it.next();
               sortedHashMap.put(entry.getKey(), entry.getValue());
        } 
        return sortedHashMap;
   }
	
	
}