package com.krish.empower.decision_table;

public final class RetDTCell {
	private String columnName = null;
	private Object value = null;
	private DataType dataType = null;
	
	public RetDTCell(String columnName, DataType dataType, Object value) {
		this.columnName = columnName;
		this.dataType = dataType;
		this.value = value;
	}
	
	public String getColumnName() { return columnName; }
	public String getString() { return (String)value; }
	public Integer getInteger() {return (Integer)value; }
	public Long getLong() {return (Long)value; }
	public Double getDouble() {return (Double)value; }
	public Boolean getBoolean(){return (Boolean)value; }
	public DataType getDataType() {return dataType; }
	public Object getValue(){return value; }
	
}

