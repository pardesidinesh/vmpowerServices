package com.krish.empower.processor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.krish.empower.config.PropertyConfigurer;
import com.krish.empower.service.RestCallService;
import com.krish.empower.util.ServiceUtil;
import com.krish.empower.util.Utils;

@Component
public class DashboardProcessor {
	
	@Autowired
	private PropertyConfigurer propertyConfigurer;
	
	@Autowired
	private RestCallService restCallService;
	
	public JSONObject loadServiceDetails(String env, String serviceName) {
		JSONObject serviceDetailsObj = new JSONObject();
		String serviceInfoUrl = propertyConfigurer.getServiceInfoUrl().replaceAll("\\{environment\\}", env).replaceAll("\\{serviceName\\}", serviceName);
		String beansJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"beans");
		String envJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"env");
		String healthJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"health");
		JSONArray beanList = new JSONArray();
		JSONArray envList = new JSONArray();
		JSONArray traceList = new JSONArray();
		if(beansJsonText.startsWith("[")) {
			JSONArray rawList = new JSONArray(beansJsonText);
			beanList = ServiceUtil.processBeansArray(rawList);
		}else{
			JSONObject rawObj = new JSONObject(beansJsonText);
			beanList = ServiceUtil.processBeansObject(rawObj);
		}
		JSONObject envJson = new JSONObject(envJsonText);
		if(envJson.has("propertySources")) {
			envList = ServiceUtil.processEnvironmentPropSourceObject(envJson);
			String traceJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"httptrace");
			traceList = ServiceUtil.processTracesFromObject(traceJsonText);
		}else{
			envList = ServiceUtil.processEnvironmentDirectObject(envJson);
			String traceJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"trace");
			traceList = ServiceUtil.processTracesFromList(traceJsonText);
		}
		JSONObject healthObj = new JSONObject(healthJsonText);
		JSONObject diskSpcObj = ServiceUtil.processDiskSpaceDetails(healthObj);
		serviceDetailsObj.put("beans", beanList);
		serviceDetailsObj.put("env", envList);
		serviceDetailsObj.put("disk", diskSpcObj);
		serviceDetailsObj.put("trace", traceList);
		serviceDetailsObj.put("pid", "1");
		return serviceDetailsObj;
	}
	
	public JSONObject loadServiceMetrics(String env, String serviceName) {
		JSONObject serviceMetricsObj = new JSONObject();
		String serviceInfoUrl = propertyConfigurer.getServiceInfoUrl().replaceAll("\\{environment\\}", env).replaceAll("\\{serviceName\\}", serviceName);
		String metricsJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"metrics");
		JSONObject metricsJsonObj = new JSONObject(metricsJsonText);
		if(metricsJsonObj.has("names")) {
			serviceMetricsObj.put("uptime", "NA");
			serviceMetricsObj.put("process", "NA");
			serviceMetricsObj.put("system", "NA");
			serviceMetricsObj.put("cpus", "1");
			serviceMetricsObj.put("gcCnt", "0");
			serviceMetricsObj.put("gcTotal", "0s");
			serviceMetricsObj.put("gcMax", "0s");
			serviceMetricsObj.put("heapUsed", "0");
			serviceMetricsObj.put("heapSize", "0");
			serviceMetricsObj.put("nonHeapUsed", "0");
			serviceMetricsObj.put("nonHeapSize", "0");
			serviceMetricsObj.put("liveThreads", "0");
			serviceMetricsObj.put("daemonThreads", "0");
		}else {
			int gcCnt = 0;
			long gcTimeSpent = 0;
			long gcMaxTimeSpent = 0;
			if(metricsJsonObj.has("gc.copy.count")) {
				 gcCnt = metricsJsonObj.getInt("gc.copy.count");
				 gcTimeSpent = Utils.convertMills2Units(metricsJsonObj.getLong("gc.copy.time"));
				 gcMaxTimeSpent =  Utils.convertMills2Units(metricsJsonObj.getLong("gc.marksweepcompact.time"));
			}else if(metricsJsonObj.has("gc.ps_scavenge.count")) {
				 gcCnt = metricsJsonObj.getInt("gc.ps_scavenge.count");
				 gcTimeSpent = Utils.convertMills2Units(metricsJsonObj.getLong("gc.ps_scavenge.time"));
				 gcMaxTimeSpent =  Utils.convertMills2Units(metricsJsonObj.getLong("gc.ps_marksweep.time"));
			}
			serviceMetricsObj.put("uptime", Utils.convertMills2Units(metricsJsonObj.getLong("uptime")));
			serviceMetricsObj.put("process", "NA");
			serviceMetricsObj.put("system", "NA");
			serviceMetricsObj.put("cpus", metricsJsonObj.getInt("processors"));
			serviceMetricsObj.put("gcCnt", gcCnt);
			serviceMetricsObj.put("gcTotal", gcTimeSpent+" s");
			serviceMetricsObj.put("gcMax", gcMaxTimeSpent+" s");
			serviceMetricsObj.put("heapUsed", Utils.convertMills2Units(metricsJsonObj.getLong("heap.used")));
			serviceMetricsObj.put("heapSize", Utils.convertMills2Units(metricsJsonObj.getLong("heap.committed")));
			serviceMetricsObj.put("nonHeapUsed", Utils.convertMills2Units(metricsJsonObj.getLong("nonheap.used")));
			serviceMetricsObj.put("nonHeapSize", Utils.convertMills2Units(metricsJsonObj.getLong("nonheap.committed")));
			serviceMetricsObj.put("liveThreads", metricsJsonObj.getInt("threads"));
			serviceMetricsObj.put("daemonThreads", metricsJsonObj.getInt("threads.daemon"));
		}
		return serviceMetricsObj;
	}
	
	public JSONObject loadServiceHealthInfo(String env, String serviceName) {
		String serviceInfoUrl = propertyConfigurer.getServiceInfoUrl().replaceAll("\\{environment\\}", env).replaceAll("\\{serviceName\\}", serviceName);
		String infoJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"info");
		String healthJsonText = restCallService.exchangeGetRequest(serviceInfoUrl+"health");
		JSONObject infoObj = new JSONObject(infoJsonText);
		JSONObject healthObj = new JSONObject(healthJsonText);
		JSONObject serviceInfoObj = ServiceUtil.processHealthDetails(infoObj, healthObj);
		return serviceInfoObj;
	}
}
