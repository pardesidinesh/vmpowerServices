package com.krish.empower.resource;

import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.krish.empower.processor.DashboardProcessor;
import com.krish.empower.processor.SwaggerProcessor;

@RestController
@CrossOrigin
@RequestMapping("empower/live")
public class DashboardResource {
	
	@Autowired
	private SwaggerProcessor swaggerProcessor;
	
	@Autowired
	private DashboardProcessor dashboardProcessor;
	
	@RequestMapping(value="/services/info/{env}", produces = "application/json", method = RequestMethod.GET)
	public List<LinkedHashMap<String, String>> getServiceDetails(@PathVariable("env") String env){
		return swaggerProcessor.getServiceDetails(env);
	}
	
	@RequestMapping(value="/service/info/{env}/{serviceName}", produces = "application/json", method = RequestMethod.GET)
	public JSONObject getServiceDetails(@PathVariable("env") String env, @PathVariable("serviceName") String serviceName){
		return dashboardProcessor.loadServiceDetails(env, serviceName);
	}
	
	@RequestMapping(value="/service/metrics/{env}/{serviceName}", produces = "application/json", method = RequestMethod.GET)
	public JSONObject getServiceMetrics(@PathVariable("env") String env, @PathVariable("serviceName") String serviceName){
		return dashboardProcessor.loadServiceMetrics(env, serviceName);
	}
}
