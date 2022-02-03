package com.krish.empower.processor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.krish.empower.config.PropertyConfigurer;
import com.krish.empower.payload.ElasticSearchRequest;

import static com.krish.empower.util.Utils.addFieldValue2Date;

@Component
public class ElasticLogProcessor {
	
	private PropertyConfigurer propertyConfigurer;
	
	@Autowired
	public ElasticLogProcessor(PropertyConfigurer propertyConfigurer) {
		this.propertyConfigurer=propertyConfigurer;
	}
	
	public static Client ignoreSSLClient()throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null,  new TrustManager[]{ new X509TrustManager() {
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return new X509Certificate[0];
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// TODO Auto-generated method stub
				
			}
		}}, new SecureRandom());
		
		return ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier((s1, s2)->true).build();
		
	}
	
	public List<String> getIndexPatterns() throws Exception{
		
		Client cleint = ignoreSSLClient();
		
		WebTarget target = cleint.target(propertyConfigurer.getElasticLoginUri());
		
		Response loginResponse = loginElasticSearch(target);
		
		target = cleint.target(propertyConfigurer.getElasticIndexUri());
		
		return getIndexPatterns(target, loginResponse);
	}
	
	public List<HashMap<String, String>> getRespectiveLogs(ElasticSearchRequest request) throws Exception{
		
		Client cleint = ignoreSSLClient();
		
		WebTarget target = cleint.target(propertyConfigurer.getElasticLoginUri());
		
		Response loginResponse = loginElasticSearch(target);
		
		target = cleint.target(propertyConfigurer.getElasticSearchUri());
		
		return prepareSearchLogs(target, loginResponse, request);
	}
	
	private Response loginElasticSearch(WebTarget loginTarget) {
		JSONObject reqjson = new JSONObject();
		reqjson.put("username", propertyConfigurer.getElasticUserId());
		reqjson.put("password", propertyConfigurer.getElasticPassword());
		MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.add("Authorization", "Basic "+propertyConfigurer.getElasticAuthCode());
		headers.add("kbn-version", "6.1.1");
		return loginTarget.request(MediaType.APPLICATION_JSON).headers(headers)
				.post(Entity.entity(reqjson.toString(), MediaType.APPLICATION_JSON_TYPE));
	}
	
	private List<String> getIndexPatterns(WebTarget patternClient, Response loginResponse){
		MultivaluedHashMap<String, Object> headers =  getRequestHeaders(loginResponse);
		Response response = patternClient.request(MediaType.APPLICATION_JSON).headers(headers).get();
		JSONObject respjson = new JSONObject(response.readEntity(String.class));
		JSONArray pattrns = respjson.optJSONArray("saved_objects");
		return null;
	}
	
	private List<HashMap<String, String>> prepareSearchLogs(WebTarget searchClient, Response loginResponse,ElasticSearchRequest request)throws IOException{
		MultivaluedHashMap<String, Object> headers =  getRequestHeaders(loginResponse);
		InputStream searchStream = ElasticLogProcessor.class.getClassLoader().getResourceAsStream("search_request.txt");
		String fileRequest = IOUtils.toString(searchStream, StandardCharsets.UTF_8.toString());
		fileRequest = fileRequest.replace("${correlId}", request.getCorrelationId());
		fileRequest = fileRequest.replace("${indexPattern}", request.getIndexPattern());
		Date endDtm =  new Date();
		fileRequest = fileRequest.replace("${lte}", String.valueOf(addFieldValue2Date(endDtm, 1, Calendar.HOUR_OF_DAY)));
		fileRequest = fileRequest.replace("${gte}", request.getStartDate());
		Response response = searchClient.request(MediaType.APPLICATION_JSON).headers(headers)
				.post(Entity.entity(fileRequest, MediaType.APPLICATION_JSON_TYPE));
		
		JSONObject respjson = new JSONObject(response.readEntity(String.class));
		JSONArray hits = respjson.optJSONArray("/responses/0/hits/hits");
		return null;
	}
	
	private MultivaluedHashMap<String, Object> getRequestHeaders(Response loginResp){
		MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.add("Authorization", "Basic "+propertyConfigurer.getElasticAuthCode());
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		headers.add("kbn-version", "6.1.1");
		headers.add("Host", "vrhelx201.eu.nag.net");
		headers.add("Acept", "*/*");
		headers.add("Cache-Control", "no-cache");
		headers.add("Cookie", "kibana_sid="+loginResp.getCookies().get("kibana_sid")
				.getValue() +"; 2a8ad5235e5c3f8731ea156ba0188dbb="+loginResp.getCookies()
				.get("2a8ad5235e5c3f8731ea156ba0188dbb").getValue()+";");
		return headers;
	}
	
	
	
}
