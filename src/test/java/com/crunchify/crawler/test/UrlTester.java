package com.crunchify.crawler.test;

import java.io.File;
import java.nio.file.Paths;

import  org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.webengine.crawler.CrawlerController;
import com.webengine.crawler.UrlHelper;
import com.webengine.crawler.UrlType;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;




public class UrlTester {

	@Test
	public void addSeed(){		
			UrlHelper urlHelper = new UrlHelper();		
			Assert.assertEquals(UrlType.REMOTE,urlHelper.getType("http://www.google.de"));
			Assert.assertEquals(UrlType.LOCAL,urlHelper.getType("file:///var/www/html"));
			Assert.assertEquals(UrlType.LOCAL,urlHelper.getType("C:\\Windows"));			
	}
	
	@Test
	public void testToUri(){
		try {
			CrawlConfig config = org.mockito.Mockito.mock(CrawlConfig.class);
			Mockito.when(config.getCrawlStorageFolder()).thenReturn("crawler");
			CrawlerController c = new CrawlerController(config,
						 null, null, null, "localhost", 80, "");
			String uri = c.toURI("/var/www/html",new File("/var/www/html/test.html") );
			Assert.assertEquals("http://localhost:80/test.html",uri);
			uri = c.toURI("/var/www/html",new File("/var/www/html/sub/test.html") );			
			Assert.assertEquals("http://localhost:80/sub/test.html",uri);
			//uri = c.toURI("C:\\public_html",new File("C:\\public_html\\sub\\test.html") );			
			Assert.assertEquals("http://localhost:80/sub/test.html",uri);
			
			 c = new CrawlerController(config,
					 null, null, null, "localhost", 80, "/");
			 uri = c.toURI("/var/www/html",new File(new File("/var/www/html"),"test.html") );
			Assert.assertEquals("http://localhost:80/test.html",uri);
			uri = c.toURI("/var/www/html",new File("/var/www/html/sub/test.html") );
			Assert.assertEquals("http://localhost:80/sub/test.html",uri);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
