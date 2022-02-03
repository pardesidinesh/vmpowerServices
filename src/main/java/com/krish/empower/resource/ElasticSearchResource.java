package com.krish.empower.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

import com.krish.empower.payload.ElasticSearchRequest;
import com.krish.empower.processor.ElasticLogProcessor;

@Named
@CrossOrigin
@Path("/empower/log")
public class ElasticSearchResource {
	
	@Inject
	public ElasticLogProcessor elasticLogProcessor;
	
	@GET
	@Path("/index-patterns")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIndexPatterns() {
		List<String> indexs = new ArrayList<>();
		try {
			indexs = elasticLogProcessor.getIndexPatterns();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return Response.ok(indexs).build();
	}
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSearchLogs(@RequestBody ElasticSearchRequest requestBean) {
		List<HashMap<String, String>> logs = new ArrayList<>();
		try {
			logs = elasticLogProcessor.getRespectiveLogs(requestBean);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return Response.ok(logs).build();
	}

}
