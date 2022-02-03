package com.krish.empower.processor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

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
public class JiraProcessor {
	
	@Autowired
	private PropertyConfigurer propertyConfigurer;
	
	public void uploadFile2Jira(String jiraId, MultipartFile[] files){
		String authKey = getAuthKey(propertyConfigurer.getJiraUserId(), propertyConfigurer.getJiraPassword());
		uploadFile2JiraIssue(authKey, jiraId, files);
		
	}
	public int getOPenTicketsCountByQuery(){
		return 0;
	}
	
	private String getAuthKey(String userName, String password) {
		return Base64.encodeBase64String((userName+":"+password).getBytes());
	}
	
	private Map<String, String> uploadFile2JiraIssue(String authToken, String ticketId, MultipartFile[] files) {
		String JiraUrl = MessageFormat.format(propertyConfigurer.getJiraAttachmentUri(), ticketId);
		Map<String, String> headers = getJiraHeaderMap(authToken);
		Map<String, String> respMap = new HashMap<>();
		try {
			HttpClientBuilder clientBuilder = Utils.getClientBuilder(propertyConfigurer.getProxyUrl(), Integer.parseInt(propertyConfigurer.getProxyPort()), 
					propertyConfigurer.getProxyUsername(), propertyConfigurer.getProxyPassword());
			
			respMap = Utils.prepareFileUploadRequest(clientBuilder, propertyConfigurer.getJiraAttachmentUri(), "post", headers, null, files);
		}catch (Exception e) {
			// TODO: handle exception
			respMap.put("status_code", "500");
			respMap.put("response", e.getMessage());
		}
		return respMap;
	}
	
	private Map<String, String> getJiraHeaderMap(String authToken){
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic "+authToken);
		headers.put("X-Atlassian-token", "no-check");
		headers.put("Accept", MediaType.APPLICATION_JSON);
		return headers;
	}
}
