package com.webengine.crawler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * this class is used to check manage and resolve ip addresses in order to
 * resolve if a url is "local" or not.
 * 
 * @author robert
 *
 */
@Component
public class IpManager {

	private final static Logger LOGGER = Logger.getLogger(IpManager.class.getName());

	private List<String> localIps = new ArrayList<String>();

	
	@Value("${config.crawler.localHost}")
	private String presetlocalip;
	
	
	/**
	 * tries to resolve all ips for a given domain
	 * 
	 * @param domain
	 * @return list of ips found for that domain
	 */
	public List<String> getIps(String domain) {
		// TODO Auto-generated method stub
		List<String> result = Collections.emptyList();
		try {
			result = Arrays.asList(InetAddress.getAllByName(domain)).stream().map(s -> s.getHostAddress())
					.collect(Collectors.toList());

		} catch (UnknownHostException e) {
			LOGGER.info("unable to resolve ip for " + domain);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @return all ip's configured at the local machine
	 */
	public List<String> getLocalIps() {
		if (localIps.isEmpty()) {
			InitLocalHostIps();
		}
		return localIps;
	}

	private void InitLocalHostIps() {

		this.localIps.clear();
		try {
			List<NetworkInterface> ns = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface n : ns) {
				List<String> as = Collections.list(n.getInetAddresses()).stream().map(s -> s.getHostAddress())
						.collect(Collectors.toList());
				localIps.addAll(as);
			}
			localIps = localIps.stream().distinct().collect(Collectors.toList());

			
			if (presetlocalip!=null) {
				
				if (!presetlocalip.equals("")) {
				
					LOGGER.info("IPManager: Extra localip added: " + presetlocalip);
					
					localIps.add(presetlocalip);
				
				}
				
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
