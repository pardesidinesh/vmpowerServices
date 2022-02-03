package com.krish.empower.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@PropertySource("classpath:config.properties")
public class PropertyConfigurer {
	@Value("${swaggerURL}")
	private String swaggerUri;

	@Value("${serviceInfoUrl}")
	private String serviceInfoUrl;
	
	@Value("${elastic.search.login.url}")
	private String elasticLoginUri;
	@Value("${elastic.search.search.url}")
	private String elasticSearchUri;
	@Value("${elastic.search.auth.code}")
	private String elasticAuthCode;
	@Value("${elastic.search.auth.userId}")
	private String elasticUserId;
	@Value("${elastic.search.auth.password}")
	private String elasticPassword;
	@Value("${elastic.search.index.url}")
	private String elasticIndexUri;
	
	@Value("${jira.auth.userId}")
	private String jiraUserId;
	@Value("${jira.auth.password}")
	private String jiraPassword;
	@Value("${jira.issue.attachment.uri}")
	private String jiraAttachmentUri;
	@Value("${jira.release.uri}")
	private String jiraReleaseUri;
	
	@Value("${proxy.url}")
	private String proxyUrl;
	@Value("${proxy.port}")
	private String proxyPort;
	@Value("${proxy.user}")
	private String proxyUsername;
	@Value("${proxy.password}")
	private String proxyPassword;

	public String getSwaggerUri() {
		return swaggerUri;
	}

	public String getServiceInfoUrl() {
		return serviceInfoUrl;
	}
	
	public String getElasticIndexUri() {
		return elasticIndexUri;
	}

	public String getElasticLoginUri() {
		return elasticLoginUri;
	}

	public String getElasticSearchUri() {
		return elasticSearchUri;
	}

	public String getElasticAuthCode() {
		return elasticAuthCode;
	}

	public String getElasticUserId() {
		return elasticUserId;
	}

	public String getElasticPassword() {
		return elasticPassword;
	}

	public String getJiraUserId() {
		return jiraUserId;
	}

	public String getJiraPassword() {
		return jiraPassword;
	}

	public String getJiraAttachmentUri() {
		return jiraAttachmentUri;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public String getJiraReleaseUri() {
		return jiraReleaseUri;
	}
	
}
