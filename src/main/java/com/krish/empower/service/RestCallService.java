package com.krish.empower.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import static com.krish.empower.util.Utils.getHttpMethod;

@Service
public class RestCallService {
	@Autowired
	private RestTemplate restTemplate;
	
	public String exchangeGetRequest(String url) {
		String response = restTemplate.getForObject(url, String.class);
		return response;
	}
	
	public ResponseEntity<String> exchangeServiceRequest(String uri, HttpHeaders headers, String reqBody, String reqType){
		if(StringUtils.isBlank(reqBody)) {
			reqBody="parameters";
		}
		HttpMethod reqMethod = getHttpMethod(reqType);
		HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);
		ResponseEntity<String> respnse = restTemplate.exchange(uri, reqMethod, entity, String.class);
		
		return respnse;
	}
}
