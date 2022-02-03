package com.krish.empower.decision_table;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

public class DTInput {
	private Map<String, String> values = new HashedMap<>();

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}
	
}
