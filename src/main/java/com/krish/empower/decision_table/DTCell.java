package com.krish.empower.decision_table;

public final class DTCell {
	private String columnName;
	private String value;
	private OperatorType oprType;
	
	public DTCell(String columnName, String value, OperatorType oprType) {
		this.columnName = columnName;
		this.value=value;
		this.oprType=oprType;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getValue() {
		return value;
	}

	public OperatorType getOprType() {
		return oprType;
	}
	
}
