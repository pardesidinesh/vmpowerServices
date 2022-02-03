package com.krish.empower.processor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.krish.empower.config.PropertyConfigurer;
import com.krish.empower.service.RestCallService;
import com.krish.empower.util.ServiceUtil;

@Component
public class SwaggerProcessor {
	@Autowired
	private PropertyConfigurer propertyConfigurer;
	
	@Autowired
	private RestCallService restCallService;
	
	private JSONObject swaggerJson;
	private List<String> serviceList;
	private HashMap<String, List<String>> endpoints;
	private JSONObject urlHeaders;
	
	@Autowired
	public SwaggerProcessor() {
		urlHeaders = new JSONObject();
		swaggerJson = loadSwaggerJson();
	}
	
	public JSONObject loadSwaggerJson() {
		String swaggerUrl = propertyConfigurer.getSwaggerUri();
		String swaggerJsonText = restCallService.exchangeGetRequest(swaggerUrl);
		JSONObject swggerJson = new JSONObject(swaggerJsonText);
		return swggerJson;
	}
	
	public List<String> getServiceList(){
		if(serviceList==null) {
			serviceList = ServiceUtil.loadServices(swaggerJson);
		}
		return serviceList;
	}
	
	public List<String> getEndpointList(String serviceName){
		if(!endpoints.containsKey(serviceName)) {
			List<String> serviceEndpoints = ServiceUtil.loadServices(swaggerJson);
			endpoints.put(serviceName, serviceEndpoints);
		}
		return endpoints.get(serviceName);
	}
	
	public List<LinkedHashMap<String, String>> getServiceDetails(String env){
		List<LinkedHashMap<String, String>> serviecDtls = new LinkedList<>();
		String serviceInfoUrl = propertyConfigurer.getServiceInfoUrl().replaceAll("\\{environment\\}", env);
		List<String> serviceListDummy = getServiceList();
		for(String serviceName : serviceListDummy) {
			JSONObject serviceUrlObj = swaggerJson.optJSONObject(serviceName);
			JSONObject endpointsObj = serviceUrlObj.optJSONObject("paths");
			LinkedHashMap<String, String> servcObj = loadServiceDetails(serviceName, endpointsObj.keySet().size()+"", serviceInfoUrl);
			serviecDtls.add(servcObj);
		}
		return serviecDtls;
	}
	
	private LinkedHashMap<String, String> loadServiceDetails(String serviceName, String urlCount, String serviceUrl){
		LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
		serviceUrl = serviceUrl.replaceAll("\\{serviceName\\}", serviceName);
		String infoJsonText = restCallService.exchangeGetRequest(serviceUrl+"info");
		String healthJsonText = restCallService.exchangeGetRequest(serviceUrl+"health");
		JSONObject infoObj = new JSONObject(infoJsonText);
		JSONObject healthObj = new JSONObject(healthJsonText);
		JSONObject infoAppObj = infoObj.optJSONObject("app");
		resultMap.put("service", serviceName);
		resultMap.put("version", infoAppObj.optString("version"));
		resultMap.put("bom", infoAppObj.optString("platformBomVersion", "NA"));
		resultMap.put("urls", urlCount);
		resultMap.put("status", healthObj.optString("status"));
		if(healthObj.has("details")) {
			healthObj = healthObj.optJSONObject("details");
		}
		String dbStatus = (healthObj.has("db"))?healthObj.optJSONObject("db").optString("status"):"Down";
		resultMap.put("db", dbStatus);
		String spcStatus = (healthObj.has("diskSpace"))?healthObj.optJSONObject("diskSpace").optString("status"):"Down";
		resultMap.put("space", spcStatus);
		String jmsStatus = (healthObj.has("jms"))?healthObj.optJSONObject("jms").optString("status"):"Down";
		resultMap.put("jms", jmsStatus);
		return resultMap;
	}
}
