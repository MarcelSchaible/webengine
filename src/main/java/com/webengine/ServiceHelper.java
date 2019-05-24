package com.webengine;

import java.io.IOException;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.webengine.messages.Entry;

import com.webengine.messages.SearchRequest;

@Component
public class ServiceHelper {

	@Value("${config.peer.port}")
	private int neighbourPort;

	@Value("${config.peer.appPath}")
	private String neighbourPath;

	/**
	 * convert string to json object
	 * 
	 * @param json
	 * @param type
	 * @return instance of type, with the attributes from the json param.
	 */
	public static <T extends Object> T parseFromJson(String json, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		T response = null;
		try {
			response = mapper.readValue(json, type);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * convert a message object to the json string representation
	 * 
	 * @param message
	 * @return
	 */
	public static String parseToJson(SearchRequest message) {
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		try {
			response = mapper.writeValueAsString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	
	public static String parseToJson2(Entry message) {
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		try {
			response = mapper.writeValueAsString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	
	
	
	
	public static String parseToJson4(Vector message) {
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		try {
			response = mapper.writeValueAsString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	
	public static String parseToJson5(Vector<Entry> message) {
		ObjectMapper mapper = new ObjectMapper();
		
		
		//ObjectWriter ow = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Vector.class, Entry.class));
		//ObjectWriter ow = mapper.writerFor(mapper.getTypeFactory().constructCollectionType(Vector.class, Entry.class));
		
		//ObjectWriter ow = mapper.writerWithType(new TypeReference<Vector <Entry>>() {});
		
		String response = null;
		try {
			
		
			//response = ow.writeValueAsString(message);
			
			//System.out.println("Reposnse: " +response);;
			
			//ow.writeValue(System.out, message);
			
			
			response = mapper.writeValueAsString(message);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	
	
	
	
	
	
	/**
	 * 
	 * @param host
	 *            domain of the other peer
	 * @param method
	 *            method to call at the other peer
	 * @return url of another peer to call a specified method
	 */
	public String BuildUrl(String host, String method) {
		
		String help = "";
		String url = "";
		
		if (host.indexOf(":")!=-1) {  // host enthält zb :8080 port
			
			url = "http://" + host + "" + this.neighbourPath + "/" + method;
			
		} else {
			
			url = "http://" + host + ":" + this.neighbourPort + this.neighbourPath + "/" + method;
		}
		
		
		return url;
	}

	public void setPeerPort(int i) {
		this.neighbourPort = i;

	}

	public void setPeerPath(String string) {
		this.neighbourPath = string;

	}

	
	
	public int getPeerPort() {
		return this.neighbourPort;

	}

	public String getPeerPath() {
		return this.neighbourPath;

	}
	
	
}
