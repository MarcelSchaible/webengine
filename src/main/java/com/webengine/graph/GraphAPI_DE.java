package com.webengine.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webengine.RelationshipTypes;
import com.webengine.fts.LuceneService;
import com.webengine.textprocessing.Word;

@Service
public class GraphAPI_DE {

	private final static Logger LOGGER = Logger.getLogger(GraphAPI_DE.class.getName());

	
	private GraphDatabaseService graphDB_de;

	
	@Value("${config.graphdb.folder}")
	private String graphdbfolder;
	
	
	@PostConstruct
	void setup() {
		String db_path = graphdbfolder+"_de"; //System.getProperty("user.dir") + "/cooccsdatabase_de";
		File database = new File(db_path);
		graphDB_de = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(database)
				.setConfig(GraphDatabaseSettings.keep_logical_logs, "false")
				.newGraphDatabase() ;
				
				
				
				
				//.newEmbeddedDatabase(database);
	}

public void restartDB() {
		
		if (graphDB_de!=null) {
			
			graphDB_de.shutdown();
			
			setup();
		}
		
	}
	
	
	public void addDocumentNode(String filename) {

		LOGGER.info("->addDocumentNode() is started...");
		boolean f_flag = false;
		int count = 0;
		ResourceIterator<Node> nodelist = null;
		try (Transaction tx = graphDB_de.beginTx()) {
			nodelist = graphDB_de.findNodes(Labels.SINGLE_NODE);
			while (nodelist.hasNext()) {
				Node docnode = nodelist.next();
				
				if (docnode.hasProperty("docname"))
				if (docnode.getProperty("docname").equals(filename)) {
					f_flag = true;
					LOGGER.info(filename + " docnode was already present.");
				}

			} 
			// wenn es das wort schon gibt leg es nicht an
			if (f_flag == false) {
				Node docnode = graphDB_de.createNode(Labels.SINGLE_NODE);
				docnode.setProperty("docname", filename);
				
				LOGGER.info(filename + " docnode added.");
			}
			tx.success();
		} catch (Exception exp ) {
			LOGGER.info("Exception occurred in´addDocumentNode: " +exp.getMessage() + " : " +exp.getStackTrace());
		} finally {

			if (nodelist != null) {
				
					nodelist.close();
				
			}
		}

		
		LOGGER.info("<-addDocumentNode() ended...");
		
	}
	
	
	public boolean checkDocumentNode(String filename) {
		boolean result = false;
		
		LOGGER.info("->checkDocumentNode() is started...");
		
		int count = 0;
		ResourceIterator<Node> nodelist = null;
		try (Transaction tx = graphDB_de.beginTx()) {
			nodelist = graphDB_de.findNodes(Labels.SINGLE_NODE);
			while (nodelist.hasNext()) {
				Node docnode = nodelist.next();
				
				if (docnode.hasProperty("docname"))
				if (docnode.getProperty("docname").equals(filename)) {
					result = true;
					LOGGER.info(filename + " docnode is already present.");
				}

			}
			
			tx.success();
		} catch (Exception exp ) {
			LOGGER.info("Exception occurred in´checkDocumentNode: " +exp.getMessage() + " : " +exp.getStackTrace());
		} finally {

			if (nodelist != null) {
				
					nodelist.close();
				
			}
		}
		
		
		
		
		LOGGER.info("<-checkDocumentNode() ended...");
		
		return result;
	}
	
	
	
	
	
	
	
	

	public Vector<String> listNodes() {
		
		Vector <String>wordnodes= new Vector<String>(); 
		ResourceIterator<Node> nodelist = null;
			try (Transaction tx = graphDB_de.beginTx()) {
				nodelist = graphDB_de.findNodes(Labels.SINGLE_NODE);
				while (nodelist.hasNext()) {
					Node wordnode = nodelist.next();
					
					
					if (wordnode.hasProperty("name")) {
						
						wordnodes.add(wordnode.getProperty("name").toString());
						
					}
					
					
					
					

				}
				
				tx.success();
			} catch (Exception exp ) {
				LOGGER.info("Exception occurred in listNodes: " +exp.getMessage() + " : " +exp.getStackTrace());
			} finally {

				if (nodelist != null) {
					
						nodelist.close();
					
				}
			}
		
			return wordnodes;
		}
	
	
	
	public double getNodeDistance(String term1, String term2) {
		
				
		double distance=Double.MAX_VALUE;
		
	
		try (Transaction tx = graphDB_de.beginTx()) 
	    {  
			
			Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", term1.toLowerCase());
			
			if (temp!=null) {
				
				Node temp2 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", term2.toLowerCase());
				
				if (temp2!=null) {
		    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


					WeightedPath p = finder.findSinglePath( temp, temp2 );
			
					if (p!=null) {
						
						distance = p.weight();
			
						
						
					}
					
						
				}
			}
			
			
			tx.success();
	    }catch (Exception exp ) {
			LOGGER.info("Exception occurred in nodedistance: " +exp.getMessage() + " : " +exp.getStackTrace());
		}
	
		
		
		
		return distance;
	}
	
	
	
	
	public void addNode(Word word) {
	//	LOGGER.info("->addNode() is started...");
		boolean f_flag = false;
		int count = 0;
		ResourceIterator<Node> nodelist =null;
		try (Transaction tx = graphDB_de.beginTx()) {
			nodelist = graphDB_de.findNodes(Labels.SINGLE_NODE);
			while (nodelist.hasNext()) {
				Node wordnode = nodelist.next();
				
				
				if (wordnode.hasProperty("name"))
				if (wordnode.getProperty("name").equals(word.getStemmedWord().toLowerCase())) {
					f_flag = true;
					count = (int) wordnode.getProperty("occur");
					count = count + 1;
					wordnode.setProperty("occur", count);
					//LOGGER.info(word.getStemmedWord() + " node was already present. Count updated.");
				}

			}
			// wenn es das wort schon gibt leg es nicht an
			if (f_flag == false) {
				Node wordnode = graphDB_de.createNode(Labels.SINGLE_NODE);
				wordnode.setProperty("name", word.getStemmedWord().toLowerCase());
				wordnode.setProperty("occur", 1); // no of occurrences in
													// database
				//LOGGER.info(word.getStemmedWord() + " node added.");
			}
			tx.success();
		} catch (Exception exp ) {
			LOGGER.info("Exception occurred in addNode: " +exp.getMessage() + " : " +exp.getStackTrace());
		} finally {

			if (nodelist != null) {
				
					nodelist.close();
				
			}
		}
		
		
		
	//	LOGGER.info("<-addNode() ended...");
	}

	public void addRelation(Word word1, Word word2, int deltaSig) {
		//LOGGER.info("->addRelation() is started...");
		try (Transaction tx = graphDB_de.beginTx()) {
			if (!word1.getStemmedWord().equals(word2.getStemmedWord())) {
				Node n1 = graphDB_de.findNode(Labels.SINGLE_NODE, "name", word1.getStemmedWord().toLowerCase());
				Node n2 = graphDB_de.findNode(Labels.SINGLE_NODE, "name", word2.getStemmedWord().toLowerCase());
				int count = 0;
				boolean rel_found = false;
				if (n1 != null && n2 != null) {
					Iterable<Relationship> allRelationships = n1.getRelationships();
					for (Relationship relationship : allRelationships) {
						if (n2.equals(relationship.getOtherNode(n1))) {
							count = (int) relationship.getProperty("count");
							count = count + 1;
							relationship.setProperty("count", count);
							//LOGGER.info("Relation already existed between nodes " + word1.getStemmedWord() + " and "
							//		+word2.getStemmedWord() + ". Count updated.");
							rel_found = true;
							break;
						}
						
					}
						if (!rel_found) {
							Relationship newRelationship = n1.createRelationshipTo(n2,
									RelationshipTypes.IS_CONNECTED);
							newRelationship.setProperty("count", 1);
							newRelationship.setProperty("dice", 0); // for
																	// calculating
																	// Dice
																	// ratio
							newRelationship.setProperty("cost", Double.MAX_VALUE); // for
																	// Dijkstra
							//LOGGER.info("Relation inserted with nodes " + word1.getStemmedWord() + " and "
								//	+ word2.getStemmedWord());
						}

					
				} else {
					throw new IllegalArgumentException("node not found");
				}
			}
			tx.success();
		} catch (Exception exp ) {
			LOGGER.info("Exception occurred in´addRelation: " +exp.getMessage() + " : " +exp.getStackTrace());
		}
		// wenn es eines der beiden WÃ¶rter nicht gibt, dann leg die Relation
		// nicht an
		// evtl. Exception
	//	LOGGER.info("<-addRelation() ended...");
	}

	
	
	//this function when called, updates the Dice ratio and costs for all the relationships present in the database
			public void updateDiceandCosts()
			{
				LOGGER.info("->updateDiceandCosts() is started...");
				int countA, countB, countAB;
				double dice;
				ResourceIterator<Node> nodelist = null;
				
				try (Transaction tx5 = graphDB_de.beginTx()) 
			    {  	
					nodelist = graphDB_de.findNodes( Labels.SINGLE_NODE );
					while(nodelist.hasNext())
					{
						Node wordnode = nodelist.next();
						//al.add((String)user.getProperty("name"));
						if (wordnode.hasProperty("name")) {
						String node1=(String)wordnode.getProperty("name");
						countA=(int)wordnode.getProperty("occur");
						Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", node1.toLowerCase());
						Iterable<Relationship> allRelationships = temp.getRelationships();
					    for (Relationship relationship : allRelationships) 
					    {
					       Node n2=relationship.getOtherNode(temp);
					        countB=(int)n2.getProperty("occur");
					        countAB=(int)relationship.getProperty("count");
					        
					        
					        /***********  ********/
					        
					        int helpk=0;
					        
					        if (countB<=countA) { helpk=countB; } else
								helpk=countA;
							
							if (countAB>=helpk)
								countAB=helpk;
					        
							/***************************************/			
							
							
							
					        
					        dice=(double)(2*countAB)/(countA+countB);
					        
					        if (dice>1) dice=1.0;
					        
					        relationship.setProperty("dice", dice);
					        relationship.setProperty("cost", 1/(dice+0.01));
					        
					    }
					    
						}
					}
					LOGGER.info("Update of Dice finished.");
					tx5.success();
					
			    } catch (Exception exp ) {
					LOGGER.info("Exception occurred in´updateDiceandCosts: " +exp.getMessage() + " : " +exp.getStackTrace());
				} finally {
					
					if (nodelist!=null)
						nodelist.close();
				}
					
					
	

			LOGGER.info("<-updateDiceandCosts() ended...");
			}
			
			
			
			
			
			public HashMap getCentroidbySpreadingActivation(Vector query)
			{
				LOGGER.info("->getCentroidbySpreadingActivation() DE started...");
				HashMap result = new HashMap();
				
				
				int originalquerysize = query.size();
				String centroid = "";
				double shortestaveragepathlength = Double.MAX_VALUE;
				double timeelapsed = 0.0;	
				
				HashMap centroidcandidatesdata = new HashMap();
				
				
				Vector termcolors = new Vector();
				HashMap node2colors = new HashMap();
				HashMap<String, Double> nodedistances = new HashMap<String, Double>();
				
				double arearadius = 10.0;
				
				Vector centroidcandidates = new Vector();
				
				
				if (!query.isEmpty()) {
					
					if (query.size()>1) {	
						
				
						try (Transaction tx2 = graphDB_de.beginTx()) 
					    {  
						
						
						//query cleaning !!!!
							
							
							//Check which terms are in the graph database
							Vector helpquery = new Vector();
												
							for (int i=0; i<query.size(); i++) {
								
								
								
								Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString().toLowerCase());
								
								if (temp!=null) {
									helpquery.add(query.get(i).toString());
								}
								
							}
							
							query = helpquery;
							LOGGER.info("Helpquery size: " +query.size() + "  "+query);;
							
							
							//Check if all query terms can be reached in the graph database from one another 
							//(remove one that cannot be reached by one or all of the other terms)
							//
							
							int helpquerysize = query.size();
							
							HashSet helpqueryset = new HashSet();
							HashSet helpqueryset2bremoved = new HashSet();
							
							Vector helpquery2 = new Vector();
							
							for (int i=0; i<query.size(); i++) {
								
								helpqueryset.add(query.get(i).toString());
								
							}
							
							
							HashMap numberofreachednodes = new HashMap();
							
								Iterator iteratorq1 = helpqueryset.iterator(); 

								   while (iteratorq1.hasNext()){
									   
									   String queryterm = iteratorq1.next().toString();
									   
									   Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", queryterm.toLowerCase());
									   
									   //if (temp!=null)
									   //LOGGER.info("Temp1 node: " +temp.getProperty("name"));;
									   
									   
									   Iterator iteratorq2 = helpqueryset.iterator(); 

										   while (iteratorq2.hasNext()){
											   String queryterm2 = iteratorq2.next().toString(); 
											   
											   if (!queryterm.equals(queryterm2)) {
												   Node temp2 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", queryterm2.toLowerCase());
												   
										//		   if (temp2!=null)
													 //  LOGGER.info("Temp2 node: " +temp2.getProperty("name"));;
												   
													//   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


												//		WeightedPath p = finder.findSinglePath( temp, temp2 );
													   
												   
												   PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), 100, 1 );

													Path p = finder.findSinglePath( temp, temp2 );
																								

													if (p!=null) {
													
														// LOGGER.info("Path found");;
														
														if (numberofreachednodes.containsKey(queryterm)) {
															
															HashSet helpset = (HashSet)numberofreachednodes.get(queryterm);
															helpset.add(queryterm2);
															numberofreachednodes.put(queryterm, helpset);
															
														} else {
															
															HashSet helpset= new HashSet();
															helpset.add(queryterm2);
															numberofreachednodes.put(queryterm, helpset);
														}
														
														
														
													}
												   
											   }
											   
										   }
									   }
								   
								   LOGGER.info("Number of reached nodes: " +numberofreachednodes.toString());;
								   
								   String mostreachableterm = "";
								   int numberofneighbours = 0;
								   
								   Iterator iteratorq3 = numberofreachednodes.keySet().iterator();
								   while (iteratorq3.hasNext()){
									   String queryterm = iteratorq3.next().toString(); 
									   
									   HashSet helphashset = (HashSet) numberofreachednodes.get(queryterm);
									   
									   if (helphashset.size()>numberofneighbours) {
										   
										   numberofneighbours = helphashset.size();
										   mostreachableterm = queryterm;
									   }
								   }
								
								   HashSet helphashset = new HashSet();
								   
								   if (!mostreachableterm.equals("")) {
								   
									   helphashset.addAll((HashSet) numberofreachednodes.get(mostreachableterm));
									   
									   helphashset.add(mostreachableterm);
								   
								   
								   }
								   
							
							/*
							String term2bremoved = "";
							
							for (int k=0; k<helpquerysize; k++) {

								if (!term2bremoved.equals("")) {
								
									helpqueryset.remove(term2bremoved);
									term2bremoved = "";
									
								}
								
								if (helpqueryset.size()>0) {
								
								Iterator iterator = helpqueryset.iterator(); 

								   while (iterator.hasNext()){
									   
									   String queryterm = iterator.next().toString();
									   
									   Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", queryterm);
									   
									   
									   Iterator iterator2 = helpqueryset.iterator(); 

										   while (iterator2.hasNext()){
											   String queryterm2 = iterator2.next().toString(); 
											   
											   if (!queryterm.equals(queryterm2)) {
												   Node temp2 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", queryterm2);
												   
												   PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), 100, 1 );


													Path p = finder.findSinglePath( temp, temp2 );
												
													if (p==null) {
													
														term2bremoved=queryterm;
													}
												   
											   }
											   
										   }
									   }
									   
								   
									}
								   
								   
								   }*/
								   
								   
								   helpqueryset = helphashset;
								
							for (int i=0; i<query.size(); i++) {
								
								String helpterm = query.get(i).toString();
								
								if (helpqueryset.contains(helpterm)) {
									
									helpquery2.add(helpterm);
									
								}
								
							}
							
							
							query = helpquery2;
							LOGGER.info("Helpquery2 size: " +query.size() + "  "+query);;
							
						
							//hierher2
							if ((query.size()==query.size())  && (query.size()>1)) {
							
							
							double largestdistanceofqueryterms = 0;
							double largestpathlength = 0;
							
							for (int i=0; i<query.size(); i++) {
								
								for (int j=i+1; j<query.size(); j++) {
									
									Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString().toLowerCase());
									Node temp2 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", query.get(j).toString().toLowerCase());
									

							    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


										WeightedPath p = finder.findSinglePath( temp, temp2 );
								
										if (p!=null)
											if (p.weight()>largestdistanceofqueryterms) {
												largestpathlength = p.length();
												largestdistanceofqueryterms=p.weight();
											}
									
								}
								
							}
							
							
							
							//arearadius = Math.ceil(largestdistanceofqueryterms / 2.0)+Math.ceil((20*largestdistanceofqueryterms) / 100.0); //Math.ceil(largestdistanceofqueryterms / 2.0)+1;
							
							arearadius = Math.ceil((largestdistanceofqueryterms / 2.0) + ((20*largestdistanceofqueryterms) / 100.0))  ; //Math.ceil(largestdistanceofqueryterms / 2.0)+1;
							
							
							LOGGER.info("largestdistanceofqueryterms: " +largestdistanceofqueryterms + "  " +largestpathlength + "  arearadius: "+arearadius);
							
							
							
							double count=0; 
							
							
							long start = System.currentTimeMillis();	
							
							while ((centroidcandidates.size()<=query.size()) && (count<10)   ) { //10   //neu count

							count++;
							LOGGER.info("Activation rounds to execute: " +count );
							
							
							if (count>2) 
								arearadius = arearadius + (arearadius / 2.0);//arearadius + 1; //(arearadius / 2.0); 
							
							termcolors = new Vector();
							node2colors = new HashMap();	
							centroidcandidates = new Vector();
							nodedistances = new HashMap<String, Double>();
							
							
							
							int color = 0;
							
							for(Iterator i=query.iterator(); i.hasNext(); ){
								

								color++;
								termcolors.add(color);
													
								String curQueryTerm = i.next().toString();
								centroidcandidates.add(curQueryTerm);
								
								
								HashSet helpset = new HashSet();
								helpset.add(color);
								
								node2colors.put(curQueryTerm, helpset);
								//LOGGER.info("Query Term: " + curQueryTerm + " Color: " + color);
								nodedistances.put(curQueryTerm, 0.0);
							}
						
						
						
						
						
						
						for(Iterator i=query.iterator(); i.hasNext(); ) {
							
							
							String curQueryTerm = i.next().toString();
							
							HashSet visited = new HashSet();
							HashMap<String, Double> visiteddistance = new HashMap(); 
							
							LinkedList<String> queue = new LinkedList<String>();
							
							visited.add(curQueryTerm);
							queue.add(curQueryTerm);
							visiteddistance.put(curQueryTerm, 0.0);
							
							Node firstsourcenode = graphDB_de.findNode(Labels.SINGLE_NODE,"name", curQueryTerm.toLowerCase());
						
							int steps=0;
							
							while ((queue.size()!=0) && (steps<count)) {
								
								steps++;
								
								String sourcenodename = queue.poll();
							//	LOGGER.info("Activating: "+ sourcenodename);
														
								Node temp = graphDB_de.findNode(Labels.SINGLE_NODE,"name", sourcenodename.toLowerCase());
								Iterable<Relationship> allRelationships = temp.getRelationships();
							    for (Relationship relationship : allRelationships) 
							    {
							       Node destinationnode=relationship.getOtherNode(temp);
							       
							       String destinationnodename =  destinationnode.getProperty("name").toString();
							       
							       if (!visited.contains(destinationnodename)) {
							    	   
							    	   /*
							    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


										WeightedPath p = finder.findSinglePath( firstsourcenode, destinationnode );
						
							    	   if (p!=null)
							    	   if ( p.weight() < arearadius) {
							    		   
							    		   visited.add(destinationnodename);
							    		   queue.add(destinationnodename);
							    		   
							    	   }*/
							    	   
							    	   
							    	   double hv1 = visiteddistance.get(sourcenodename).doubleValue();
							    	   double hv2 = Double.MAX_VALUE;
							    	   
							    	   
							    	 //  LOGGER.info("hv1: " + sourcenodename + " " + destinationnodename + " " + hv1);
							    	//   LOGGER.info("hv2: " + temp.getProperty("occur") + "  " +destinationnode.getProperty("occur") +   " " + relationship.getProperty("count") + " " + relationship.getProperty("cost"));
							    	 //  double hv2 = ((Double)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   if (relationship.getProperty("cost") instanceof Integer) {
							    	//	   LOGGER.info("Object is Integer");
							    		   hv2 = ((Integer)relationship.getProperty("cost")).doubleValue();
							    	   }
							    	   
							    	   if (relationship.getProperty("cost") instanceof Float) {
							    	//	   LOGGER.info("Object is Float");
							    		   hv2 = ((Float)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   }
							    		   
							    	   if (relationship.getProperty("cost") instanceof Double) {
							    		//   LOGGER.info("Object is Double");
							    		   hv2 = ((Double)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   }
							    	   
							    	   /*
							    	   int countA=(int)temp.getProperty("occur");
							    	   int countB=(int)destinationnode.getProperty("occur");
							    	   
							    	   
								        int countAB=(int)relationship.getProperty("count");
								        
								        
								       
								        
								        int helpk=0;
								        
								        if (countB<=countA) { helpk=countB; } else
											helpk=countA;
										
										if (countAB>=helpk)
											countAB=helpk;
								        
										
										
										
										
								        
								        double dice=(double)(2*countAB)/(countA+countB);
								        
								        if (dice>1) dice=1.0;
								        
								        relationship.setProperty("dice", dice);
								        relationship.setProperty("cost", 1/(dice+0.01));
							    	   
								        LOGGER.info("Result: " + 1/(dice+0.01));
								    	*/
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   
							    		   
							    	   if (visiteddistance.containsKey(sourcenodename)   &&  (hv1+hv2<=arearadius)    ) {
							    	   
							    	   visited.add(destinationnodename);
						    		   queue.add(destinationnodename);
							    	   
						    		   visiteddistance.put(destinationnodename, hv1 + hv2);
						    		   
							    	   }
							    	   
							    	   
							    	   
							       } else {   //testen ob kleinere distanz zu einem bereits visited knoten gefunden wurde
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   double hv1 = visiteddistance.get(sourcenodename).doubleValue();
							    	   double hv2 = Double.MAX_VALUE;
							    	   
							    	   
							    	 //  LOGGER.info("hv1: " + sourcenodename + " " + destinationnodename + " " + hv1);
							    	 //  LOGGER.info("hv2: " + temp.getProperty("occur") + "  " +destinationnode.getProperty("occur") +   " " + relationship.getProperty("count") + " " + relationship.getProperty("cost"));
							    	  // double hv2 = ((Double)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   if (relationship.getProperty("cost") instanceof Integer) {
							    	//	   LOGGER.info("Object is Integer");
							    		   hv2 = ((Integer)relationship.getProperty("cost")).doubleValue();
							    	   }
							    	   
							    	   if (relationship.getProperty("cost") instanceof Float) {
							    	//	   LOGGER.info("Object is Float");
							    		   hv2 = ((Float)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   }
							    		   
							    	   if (relationship.getProperty("cost") instanceof Double) {
							    		//   LOGGER.info("Object is Double");
							    		   hv2 = ((Double)relationship.getProperty("cost")).doubleValue();
							    	   
							    	   }
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   
							    	   if (visiteddistance.containsKey(sourcenodename)   &&  (hv1+hv2<=arearadius)    ) {
							    		   
							    		   visiteddistance.put(destinationnodename, hv1+hv2);
							    		   
							    		   
							    		   
							    	   }
								    	   
		  	   
							    	   
							       }
							       
							       
							       
							    } //allrelationships
								
								
								
							
								
								
							} //while queue
							
						
							
							
							//HashSet help = (HashSet)node2colors.get(curQueryTerm);
							
							Iterator iterator = visited.iterator(); 
						      
							   // check values
							   while (iterator.hasNext()){
								   
								   String nodename = iterator.next().toString();
								  // LOGGER.info("Value: "+ nodename+ " ");  
								   
								   if (node2colors.containsKey(nodename)) {
									   
									   HashSet helpset =  (HashSet)node2colors.get(nodename);
									   
									   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
									   
									   node2colors.put(nodename, helpset);
									   
									   
									   
									   double curdistance = 0.0;
									   
									 //  if (nodedistances.containsKey(nodename))
									   curdistance =  ((Double)nodedistances.get(nodename)).doubleValue();
									   
											   curdistance = curdistance + visiteddistance.get(nodename).doubleValue();
							   
									   nodedistances.put(nodename, curdistance);
									   
									   
								   } else {
									   
									   HashSet helpset = new HashSet();
									   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
			
									   node2colors.put(nodename, helpset);
									   nodedistances.put(nodename, visiteddistance.get(nodename).doubleValue());
									   
									   
								   }
							   
							   
							   }
							
							  // LOGGER.info("Value: "+ node2colors.get(curQueryTerm).toString()); 
							  // LOGGER.info("Value: "+ node2colors.get("Grenzwert").toString());
							
							
							
							
						} //for all query terms
						
						
						
						
						
						
						Iterator iterator = node2colors.keySet().iterator(); 
					      
						   // check values
						   while (iterator.hasNext()){
							   
							   String nodename = iterator.next().toString();
							   
							   if ((((HashSet)node2colors.get(nodename)).size()==termcolors.size()) /*&& (!helpqueryset.contains(nodename))*/) {
								   if (!centroidcandidates.contains(nodename)    /*&& (centroidcandidates.size()<20)*/)
								   centroidcandidates.add(nodename);
								   
								   
								   
							   } else {
								   
								   
								   if (nodedistances.containsKey(nodename)) {
									   nodedistances.remove(nodename);
								   }
								   
								   
							   }
							   
							   
						   }
						
						   LOGGER.info("Centroid candidates: " +centroidcandidates.size() + "  " + centroidcandidates.toString());
						

						} // centroidcandidates.size()<5
							
							long stop = System.currentTimeMillis();
							timeelapsed = ((double)(stop-start)/(double)1000);
									
							LOGGER.info("Centroid determination took "+timeelapsed+" seconds.");
							
							
							double averagepathlength = 0;
							
							
							double maxdistance = Double.MAX_VALUE;
							
							
							for (int i=0; i<centroidcandidates.size(); i++) {
								
								String candidate = centroidcandidates.get(i).toString();
								
								if (nodedistances.containsKey(candidate)) {
									   if (((Double)nodedistances.get(candidate)).doubleValue()<maxdistance) {
										   
										   maxdistance = ((Double)nodedistances.get(candidate)).doubleValue();
										   shortestaveragepathlength = maxdistance / query.size();
										   centroid = candidate;
										   
									   }
								   }
								
								/*averagepathlength=0;
								
								Node n1 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", candidate.toLowerCase());
								
								for (int j=0; j<query.size(); j++) {
									
									String curQueryTerm = query.get(j).toString();
									
									Node n2 = graphDB_de.findNode(Labels.SINGLE_NODE,"name", curQueryTerm.toLowerCase());
									
									if ((n1!=null) && (n2!=null)) {
										
										PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );
										   
										WeightedPath p = finder.findSinglePath(n1, n2);
										
										if (p!=null) {
											
											averagepathlength+=p.weight();
											
										}
									
									}
							
								}
								
								averagepathlength = averagepathlength / query.size();
								
								centroidcandidatesdata.put(candidate, averagepathlength);
								
								if (averagepathlength<shortestaveragepathlength) 
								{
									shortestaveragepathlength = averagepathlength;
									centroid = candidate;
								}*/
							
							} //for centroidcandidates
							
							
							
							
					    } // if querysize==
						   
						tx2.success();
					    } catch (Exception exp ) {
							LOGGER.info("Exception occurred in´spreadingactivation: "+exp.toString() + " " +exp.getMessage() + " : " +exp.getStackTrace().toString());
							exp.printStackTrace();
					    }
							
						
						
						
						
						
						
					//	graphDB.shutdown();
						
						
					}
				}
				
				LOGGER.info("node2colors: " + node2colors.size() + "   " +centroid +  "   " + shortestaveragepathlength);
				
				result.put("centroid", centroid);
				result.put("shortestaveragepathlength", shortestaveragepathlength);
				result.put("activatednodes", node2colors.size());
				result.put("timeelapsed", timeelapsed);
				result.put("centroidcandidatesdata", centroidcandidatesdata);

				LOGGER.info("<-getCentroidbySpreadingActivation() DE ended...");
				
				return result;
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
	
}
