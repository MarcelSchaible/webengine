package com.webengine.crawler;

import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class UrlHelper {

	private final static Logger LOGGER = Logger.getLogger(UrlHelper.class.getName());

	public UrlType getType(String string) {
		
		
		URL url = null;
		try {
			url = new URL(string);
		} catch (Exception e) {
			LOGGER.info(string + " is not a valid URL");
		}
		if (url != null && (url.getProtocol().toLowerCase().startsWith("http")
				|| url.getProtocol().toLowerCase().startsWith("ftp"))) {
			return UrlType.REMOTE;
		} else {
			return UrlType.LOCAL;
		}
	}

}
