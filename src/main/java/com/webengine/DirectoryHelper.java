package com.webengine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * This is only a class to provide some Helper methods
 * @author robert
 *
 */
@Component
public class DirectoryHelper {
	
	private final static Logger LOGGER = Logger.getLogger(DirectoryHelper.class
			.getName());
	
	@Autowired
	ServletContext context;
	
	@Value("${config.search.indexDir}")
	String indexDir;
	
    /**
     * 
     * @return the full path of the directory where lucene stores the index
     */
	public String getIndexDirectory() {
		LOGGER.info("-> getIndexDirectory()");
		Path p = Paths.get(context.getRealPath(File.separator), indexDir);
		LOGGER.info("<- getIndexDirectory(): " +p.toAbsolutePath().toString());
		return p.toAbsolutePath().toString();
	}

}
