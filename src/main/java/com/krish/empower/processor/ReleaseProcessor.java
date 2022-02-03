package com.krish.empower.processor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.krish.empower.config.PropertyConfigurer;
import com.krish.empower.service.RestCallService;
import com.krish.empower.util.ServiceUtil;
import com.krish.empower.util.Utils;

@Component
public class ReleaseProcessor {
	
	public static final String TARGET_PROD_RELEASE_QUERY_BASE="\"Tearget Production Release\" in (\"{0}\") and issuetype= Defect and \"Test Environment\" ";
	
	@Autowired
	private PropertyConfigurer propertyConfigurer;
	
	
	
	
	private String getAuthKey(String userName, String password) {
		return Base64.encodeBase64String((userName+":"+password).getBytes());
	}
	
	private Map<String, String> getJiraHeaderMap(String authToken){
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic "+authToken);
		headers.put("X-Atlassian-token", "no-check");
		headers.put("Accept", ContentType.APPLICATION_JSON.toString());
		return headers;
	}
	
	public String getJiraSearchResult(String authKey, String jqlQuery) {
		String response = executeJiraEndPoint(authKey, jqlQuery);
		JSONObject respObj = new JSONObject(response);
		return respObj.getString("total");
	}
	
	public String executeJiraEndPoint(String authKey, String jqlQuery) {
		Map<String, String> headers = getJiraHeaderMap(authKey);
		Map<String, String> params = new HashMap<>();
		params.put("jql", jqlQuery);
		params.put("maxResults", "0");
		
		String responseStr = "{\"total\":\"0\"}";
		try {
			HttpClientBuilder clientBuilder = Utils.getClientBuilder(propertyConfigurer.getProxyUrl(), Integer.parseInt(propertyConfigurer.getProxyPort()), 
					propertyConfigurer.getProxyUsername(), propertyConfigurer.getProxyPassword());
			
			Map<String, String> respMap = Utils.prepareServerRequest(clientBuilder, propertyConfigurer.getJiraReleaseUri(), "get", headers, params, null);
			responseStr = respMap.get("response");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return responseStr;
	}
}
