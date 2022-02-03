package com.krish.empower.payload;

import java.util.Date;

public class ValueResponse {
	private boolean success;
	private String message;
	private Date timestamp;
	private String path;
	private String respValue;
	private String correlationId;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getRespValue() {
		return respValue;
	}
	public void setRespValue(String respValue) {
		this.respValue = respValue;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	

}
