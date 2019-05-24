package com.crunchify.crawler.test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.webengine.PeerNeighborhood;
import com.webengine.ServiceHelper;
import com.webengine.crawler.IpManager;
import com.webengine.fts.SearchService;
import com.webengine.messages.SearchRequest;
import com.webengine.messages.SearchResponse;;



public class JSONTester {

	@Test
	public void TestParseJson() {
		String json = "{\"searchDetails\":"
				+ "[{\"href\":\"http://www.spiegel.de/politik/deutschland/fluechtlinge-kripogewerkschaft-will-illegale-einreise-entkriminalisieren-a-1050152.html\","
				+ "\"content\":null,\"title\":\"Link\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/politik/deutschland/fluechtlinge-kripogewerkschaft-will-illegale-einreise-entkriminalisieren-a-1050152.html#ref=rss\","
				+ "\"content\":null,\"title\":\"Link\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/thema/sachsen/\",\"content\":null,\"title\":\"Sachsen - SPIEGEL ONLINE\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/politik/deutschland/fluechtlinge-heiko-maas-will-keine-schutzzone-um-asylbewerberheime-a-1049655.html\",\"content\":null,\"title\":\"Heiko Maas lehnt Schutzzonen vor Unterkünften für Flüchtlinge ab - SPIEGEL ONLINE\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/politik/deutschland/fluechtlingsfeier-in-heidenau-abgesagt-sachsen-kapituliert-kommentar-a-1050263.html\",\"content\":null,\"title\":\"Flüchtlingsfeier in Heidenau abgesagt: Sachsen kapituliert - Kommentar - SPIEGEL ONLINE\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/panorama/justiz/new-york-polizisten-sollen-obdachlose-fotografieren-a-1047816.html\",\"content\":null,\"title\":\"New York: Polizisten sollen Obdachlose fotografieren - SPIEGEL ONLINE\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/politik/\",\"content\":null,\"title\":\"Politik - SPIEGEL ONLINE\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/thema/new_york_city/\",\"content\":null,\"title\":\"Link\",\"lastModified\":0},"
				+ "{\"href\":\"http://www.spiegel.de/thema/heidenau/\",\"content\":null,\"title\":\"Heidenau - SPIEGEL ONLINE\",\"lastModified\":0}]}";
		SearchResponse s = ServiceHelper.parseFromJson(json, SearchResponse.class);
		Assert.assertNotNull(s);
		Assert.assertEquals(9, s.getSearchDetails().size());
	}

	@Test
	public void testMessage() {
		SearchRequest m = new SearchRequest("TestMessage", 3, "Afrika");
		String jsonMessage = ServiceHelper.parseToJson(m);
		Assert.assertNotNull(jsonMessage);
		SearchRequest m2 = ServiceHelper.parseFromJson(jsonMessage, SearchRequest.class);
		Assert.assertEquals(m, m2);
	}

	@Test
	public void testSaveFile() {
		PeerNeighborhood n = new PeerNeighborhood();
		String[] ff = new String[] { "a", "b", "c" };
		String filename = "test.txt";
		n.save(filename, Arrays.asList(ff));
		Assert.assertEquals(3, n.load(filename).size());
		try {
			Files.delete(Paths.get(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void TestPerformPostCall(){
		SearchRequest message = new SearchRequest();
		message.setAskingPeer("Robert");
		message.setId("12");
		message.setSearchString("Robert");
		message.setTtl(4);
		
				
		Map<String, String> postDataParams = new HashMap<String, String>();
		postDataParams.put("message", ServiceHelper.parseToJson(message));
		String url = "http://www.robert-eberhardt.org:8080/peer-0.0.1-SNAPSHOT/searchDocuments.html";
		SearchService s = new SearchService();	
		String response = s.performPostCall(url, postDataParams);
		Assert.assertNotNull(response);
	}
	
	@Test
	public void testDnsResolver() {

		IpManager c = new IpManager();
		Assert.assertFalse(c.getIps("google.de") == null);
		Assert.assertFalse(c.getIps("google.de").isEmpty());

	}

	@Test
	public void TestGetNetAdress() {
		IpManager c = new IpManager();
		Assert.assertFalse(c.getLocalIps().isEmpty());
	}

	@Test
	public void TestCallUrl() {
		ServiceHelper h = new ServiceHelper();
		h.setPeerPort(8080);
		h.setPeerPath("/peer-0.0.1-SNAPSHOT");

		PeerNeighborhood n = new PeerNeighborhood();
		n.setServiceHelper(h);
		boolean res = n.callURL("s18102888.onlinehome-server.info");
		Assert.assertTrue(res);
	}

}
