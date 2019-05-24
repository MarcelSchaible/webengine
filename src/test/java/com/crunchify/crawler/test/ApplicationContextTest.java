
package com.crunchify.crawler.test;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.webengine.CrunchifyController;
import com.webengine.PeerNeighborhood;
import com.webengine.crawler.CrawlerController;



@ContextConfiguration("file:src/main/webapp/WEB-INF/webengine-servlet.xml")
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationContextTest {
	
	
	@Autowired
	private CrunchifyController c;
	
	@Autowired
	java.nio.file.Path paths;
	
	@Autowired
	PropertyPlaceholderConfigurer properties;
	
	@Autowired
	PeerNeighborhood neighbourhod;
	
	@Autowired
	CrawlerController crawler;
	
	@Autowired
	ServletContext context;
	
	@Test
	public void TestWebAppIndexFolder(){
		Assert.assertNotNull(context);
	}
	
	@Test
	public void TestSomething(){
		Assert.assertNotNull(c);
		Assert.assertNotNull(crawler);			
	}
	
	

}
