package com.krish.empower.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.krish.empower.constants.ConstantUtil;

import static com.krish.empower.util.Utils.formatDateFromSource;
import static com.krish.empower.util.Utils.formatDateFromTimestamp;

public class ServiceUtil {
	public static JSONArray processBeansArray(JSONArray beansRoot) {
		JSONArray resultList = new JSONArray();
		JSONObject beans = beansRoot.optJSONObject(0);
		if(beans!=null) {
			resultList = beans.optJSONArray("beans");
		}
		return resultList;
	}
	
	public static JSONArray processBeansObject(JSONObject beansRoot) {
		JSONArray resultList = new JSONArray();
		if(beansRoot.has("contexts")) {
			JSONObject contexts = beansRoot.optJSONObject("contexts");
			if(contexts.has("application-1")) {
				JSONObject applnObj = contexts.optJSONObject("application-1");
				JSONObject beans = applnObj.optJSONObject("beans");
				Iterator<String> keys = beans.keys();
				while(keys.hasNext()) {
					String key = keys.next();
					JSONObject beanObj = beans.getJSONObject(key);
					beanObj.put("bean", key);
					resultList.put(beanObj);
					
				}
			}
		}
		return resultList;
	}
	
	public static JSONArray processEnvironmentPropSourceObject(JSONObject envRoot) {
		JSONArray resultList = new JSONArray();
		JSONArray propSrcList = envRoot.optJSONArray("propertySources");
		for (int i = 0; i < propSrcList.length(); i++) {
			JSONObject propSrcObj = propSrcList.getJSONObject(i);
			JSONObject envPropObj = buildEnvPropertyObj(propSrcObj);
			resultList.put(envPropObj);
		}
		return resultList;
	}
	
	public static JSONArray processEnvironmentDirectObject(JSONObject envRoot) {
		JSONArray resultList = new JSONArray();
		Iterator<String> keys = envRoot.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			JSONObject propObj = envRoot.optJSONObject(key);
			if(propObj!=null) {
				JSONObject envPropObj = buildEnvPropertyObj(propObj, key);
				resultList.put(envPropObj);
			}
			
		}
		return resultList;
	}
	
	public static JSONArray processTracesFromObject(String jsonText) {
		JSONArray resultList = new JSONArray();
		JSONObject sourceObj = new JSONObject(jsonText);
		JSONArray traces = sourceObj.optJSONArray("traces");
		for (int i = 0; i < traces.length(); i++) {
			JSONObject traceSrcObj = traces.getJSONObject(i);
			JSONObject traceObj = buildTraceWithObjSrc(traceSrcObj);
			if(traceObj!=null) {
				resultList.put(traceObj);
			}
		}
		return resultList;
	}
	
	public static JSONArray processTracesFromList(String jsonText) {
		JSONArray resultList = new JSONArray();
		JSONArray sourceList = new JSONArray(jsonText);
		for (int i = 0; i < sourceList.length(); i++) {
			JSONObject traceSrcObj = sourceList.optJSONObject(i);
			JSONObject traceObj = buildTraceWithListSrc(traceSrcObj);
			if(traceObj!=null) {
				resultList.put(traceObj);
			}
		}
		return resultList;
	}
	
	private static JSONObject buildTraceWithObjSrc(JSONObject srcObj) {
		JSONObject reqObj = srcObj.optJSONObject("request");
		String path = reqObj.optString("uri");
		JSONObject trceObj = new JSONObject();
		JSONObject respObj = srcObj.optJSONObject("response");
		JSONObject respHeadersObj = respObj.optJSONObject("headers");
		String timeStamp = srcObj.optString("timestamp").replace("T", " ");
		timeStamp = formatDateFromSource(timeStamp.substring(0, timeStamp.lastIndexOf(".")));
		String contType = (respHeadersObj.has("Content-Type"))?respHeadersObj.optJSONArray("Content-Type").optString(0):"";
		trceObj.put("timestamp", timeStamp);
		trceObj.put("method", reqObj.optString("method"));
		trceObj.put("path", path);
		trceObj.put("status", respObj.optString("status"));
		trceObj.put("conttype", contType);
		trceObj.put("timetaken", srcObj.optString("timeTaken")+" ms");
		trceObj.put("request", srcObj);
		return trceObj;
	}
	
	private static JSONObject buildTraceWithListSrc(JSONObject srcObj) {
		JSONObject infoObj = srcObj.optJSONObject("info");
		String path = infoObj.optString("path");
		JSONObject trceObj = new JSONObject();
		JSONObject hdrsObj = infoObj.optJSONObject("headers");
		JSONObject respObj = hdrsObj.optJSONObject("response");
		Long lngtimeStamp = srcObj.optLong("timestamp");
		trceObj.put("timestamp", formatDateFromTimestamp(lngtimeStamp));
		trceObj.put("method", infoObj.optString("method"));
		trceObj.put("path", path);
		trceObj.put("status", respObj.optString("status"));
		trceObj.put("conttype", respObj.optString("Content-Type"));
		trceObj.put("timetaken", infoObj.optString("timeTaken")+" ms");
		trceObj.put("request", srcObj);
		return trceObj;
	}
	
	private static JSONObject buildEnvPropertyObj(JSONObject propsObj, String type) {
		JSONObject envPropObj = new JSONObject();
		JSONArray props = new JSONArray();
		envPropObj.put("title", type);
		Iterator<String> keys = propsObj.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			String val = propsObj.optString(key);
			JSONObject refObj = new JSONObject();
			refObj.put("key", key);
			refObj.put("value", val);
			refObj.put("sub", "");
			props.put(refObj);
		}
		envPropObj.put("props", props);
		return envPropObj;
	}
	
	private static JSONObject buildEnvPropertyObj(JSONObject propSrcObj) {
		JSONObject envPropObj = new JSONObject();
		JSONArray props = new JSONArray();
		envPropObj.put("title", propSrcObj.optString("name"));
		JSONObject propsObj = propSrcObj.optJSONObject("properties");
		Iterator<String> keys = propsObj.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			JSONObject valPropObj = propsObj.optJSONObject(key);
			JSONObject refObj = new JSONObject();
			refObj.put("key", key);
			refObj.put("value", valPropObj.optString("value"));
			String substr = (valPropObj.has("origin"))?valPropObj.optString("origin"):"";
			refObj.put("sub", substr);
			props.put(refObj);
		}
		envPropObj.put("props", props);
		return envPropObj;
	}
	
	public static JSONObject processDiskSpaceDetails(JSONObject healthObj) {
		double total=0d;
		double free=0d;
		double threshold = 0d;
		JSONObject spaceResult = new JSONObject();
		if(healthObj.has("details")) {
			JSONObject dtlsObj = healthObj.optJSONObject("details");
			JSONObject dskSpcObj = dtlsObj.optJSONObject("diskSpace");
			spaceResult.put("status", dskSpcObj.optString("status"));
			dtlsObj = dskSpcObj.optJSONObject("details");
			total = dtlsObj.getDouble("total");
			free = dtlsObj.getDouble("free");
			threshold = dtlsObj.getDouble("threshold");
		}else {
			JSONObject dskSpcObj = healthObj.optJSONObject("diskSpace");
			spaceResult.put("status", dskSpcObj.optString("status"));
			total = dskSpcObj.getDouble("total");
			free = dskSpcObj.getDouble("free");
			threshold = dskSpcObj.getDouble("threshold");
		}
		total = total/(1024*1024*1024);
		free = free/(1024*1024*1024);
		threshold = threshold/(1024*1024);
		spaceResult.put("total", String.format("%.2f GB", total));
		spaceResult.put("free", String.format("%.2f GB", free));
		spaceResult.put("threshold", String.format("%.2f MB", threshold));
		return spaceResult;
	}
	
	public static JSONObject processJmsDetails(JSONObject jmsObj) {
		JSONObject jmsReslt = new JSONObject();
		String jmsStatus = ConstantUtil.NOT_AVAILABLE;
		String jmsProvider = ConstantUtil.NOT_AVAILABLE;
		String connFactoryStatus = ConstantUtil.NOT_AVAILABLE;
		String connFactoryProvider = ConstantUtil.NOT_AVAILABLE;
		String topicStatus = ConstantUtil.NOT_AVAILABLE;
		String topicProvider = ConstantUtil.NOT_AVAILABLE;
		if(jmsObj.has("details")) {
			JSONObject dtlsObj = jmsObj.optJSONObject("details");
			if(dtlsObj.has("jms")) {
				JSONObject jmsSrcObj = dtlsObj.optJSONObject("jms");
				jmsStatus= jmsSrcObj.optString("status");
				JSONObject jmsDtlsObj = jmsSrcObj.optJSONObject("details");
				if(jmsDtlsObj.has("connectionFactory")) {
					JSONObject connFactoryObj = jmsDtlsObj.optJSONObject("connectionFactory");
					connFactoryStatus= connFactoryObj.optString("status");
					JSONObject connDtlsObj = connFactoryObj.optJSONObject("details");
					connFactoryProvider = connDtlsObj.optString("provider",ConstantUtil.NOT_AVAILABLE);
				}
				if(jmsDtlsObj.has("topicConnectionFactory")) {
					JSONObject topicFactoryObj = jmsDtlsObj.optJSONObject("topicConnectionFactory");
					topicStatus= topicFactoryObj.optString("status");
					JSONObject topicDtlsObj = topicFactoryObj.optJSONObject("details");
					topicProvider = topicDtlsObj.optString("provider",ConstantUtil.NOT_AVAILABLE);
				}
			}
		}else {
			if(jmsObj.has("jms")) {
				JSONObject jmsSrcObj = jmsObj.optJSONObject("jms");
				jmsStatus= jmsSrcObj.optString("status");
				if(jmsSrcObj.has("connectionFactory")) {
					JSONObject connFactoryObj = jmsSrcObj.optJSONObject("connectionFactory");
					connFactoryStatus= connFactoryObj.optString("status",ConstantUtil.NOT_AVAILABLE);
					connFactoryProvider = connFactoryObj.optString("provider",ConstantUtil.NOT_AVAILABLE);
				}
				if(jmsSrcObj.has("topicConnectionFactory")) {
					JSONObject topicFactoryObj = jmsSrcObj.optJSONObject("topicConnectionFactory");
					topicStatus= topicFactoryObj.optString("status",ConstantUtil.NOT_AVAILABLE);
					topicProvider = topicFactoryObj.optString("provider",ConstantUtil.NOT_AVAILABLE);
				}
			}
		}
		jmsReslt.put("status", jmsStatus);
		jmsReslt.put("provider", jmsProvider);
		jmsReslt.put("connectionStatus", connFactoryStatus);
		jmsReslt.put("connectionProvider", connFactoryProvider);
		jmsReslt.put("topicStatus", topicStatus);
		jmsReslt.put("topicProvider", topicProvider);
		return jmsReslt;
	}
	
	public static JSONObject processDBDetails(JSONObject dbSrcObj) {
		JSONObject dbReslt = new JSONObject();
		JSONObject refScope = new JSONObject();
		String mumStatus = ConstantUtil.NOT_AVAILABLE;
		String authStatus = ConstantUtil.NOT_AVAILABLE;
		String dbStatus = ConstantUtil.NO_DB;
		JSONObject dbObj = null;
		if(dbSrcObj.has("details")) {
			JSONObject dtlsObj = dbSrcObj.optJSONObject("details");
			refScope = dtlsObj.optJSONObject("refreshScope");
			if(dtlsObj.has("db")) {
				JSONObject dbxObj = dtlsObj.optJSONObject("db");
				dbStatus = dbxObj.optString("staus", ConstantUtil.NOT_AVAILABLE);
				dbObj = dbxObj.optJSONObject("details");
			}
		}else {
			refScope = dbSrcObj.optJSONObject("refreshScope");
			if(dbSrcObj.has("db")) {
				JSONObject dbxObj = dbSrcObj.optJSONObject("db");
				dbStatus = dbxObj.optString("staus", ConstantUtil.NOT_AVAILABLE);
				dbObj = dbxObj.optJSONObject("details");
			}
		}
		
		if(dbObj!=null) {
			if(dbObj.has("mumDataSource")) {
				JSONObject dsObj = dbObj.optJSONObject("mumDataSource");
				mumStatus = dsObj.optString("staus", ConstantUtil.NOT_AVAILABLE);
			}
			if(dbObj.has("authDataSource")) {
				JSONObject dsObj = dbObj.optJSONObject("authDataSource");
				authStatus = dsObj.optString("staus", ConstantUtil.NOT_AVAILABLE);
			}
		}
		
		dbReslt.put("refreshScope", refScope.optString("status", ConstantUtil.NOT_AVAILABLE));
		dbReslt.put("status", dbStatus);
		dbReslt.put("mumDB", getDBStatusObject(mumStatus));
		dbReslt.put("authDB", getDBStatusObject(authStatus));
		return dbReslt;
	}
	
	private static JSONObject getDBStatusObject(String status) {
		JSONObject respObj = new JSONObject();
		respObj.put("status", status);
		if(ConstantUtil.DB_STATUS_UP.equalsIgnoreCase(status)) {
			respObj.put("type", "Oracle");
			respObj.put("msg", "Hello");
		}else {
			respObj.put("type", ConstantUtil.NOT_AVAILABLE);
			respObj.put("msg", ConstantUtil.NOT_AVAILABLE);
		}
		return respObj;
	}
	
	public static JSONObject processHealthDetails(JSONObject infoObj, JSONObject healthObj) {
		JSONObject healthInfoObj = new JSONObject();
		JSONObject infoAppObj = infoObj.optJSONObject("app");
		healthInfoObj.put("name", infoAppObj.optString("name", ConstantUtil.NOT_AVAILABLE));
		healthInfoObj.put("desc", infoAppObj.optString("description", ConstantUtil.NOT_AVAILABLE));
		healthInfoObj.put("version", infoAppObj.optString("version", ConstantUtil.NOT_AVAILABLE));
		healthInfoObj.put("bom", infoAppObj.optString("platformBomVersion", ConstantUtil.NOT_AVAILABLE));
		JSONObject diskObj = processDiskSpaceDetails(healthObj);
		JSONObject jmsObj = processJmsDetails(healthObj);
		JSONObject dbObj = processDBDetails(healthObj);
		healthInfoObj.put("disk", diskObj);
		healthInfoObj.put("jms", jmsObj);
		healthInfoObj.put("db", dbObj);
		return healthInfoObj;
	}
	
	public static List<String> loadServices(JSONObject swaggerRoot){
		List<String> serviceList = new ArrayList<>();
		Iterator<String> keys = swaggerRoot.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			serviceList.add(key);
		}
		Collections.sort(serviceList);
		return serviceList;
	}
}
