package com.krish.empower.resource;

import java.text.MessageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.krish.empower.processor.JiraProcessor;

@RestController
@CrossOrigin
@RequestMapping("/empower/jira")
public class JiraResource {
	
	@Autowired
	public JiraProcessor jiraProcessor;
	
	@PostMapping("/upload")
	public ResponseEntity getSearchLogs(@RequestParam("id") String jiraId, @RequestParam("files") MultipartFile[] files) {
		jiraProcessor.uploadFile2Jira(jiraId, files);
		return ResponseEntity.ok().body(MessageFormat.format("File uploaded successfull for the issue {0}", jiraId));
	}

	@GetMapping("/tickets-open")
	public ResponseEntity getOpenTickets(){
		int count = jiraProcessor.getOPenTicketsCountByQuery();
		return ResponseEntity.ok().body(count);
	}

}
