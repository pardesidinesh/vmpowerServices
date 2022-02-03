package com.krish.empower.decision_table;

public final class DTColumn {
	private String name;
	private ColumnType columnType;
	private DataType datatype;
	
	public DTColumn(String name, ColumnType columnType, DataType datatype) {
		this.name=name;
		this.columnType=columnType;
		this.datatype=datatype;
	}

	public String getName() {
		return name;
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public DataType getDatatype() {
		return datatype;
	}
	
}
