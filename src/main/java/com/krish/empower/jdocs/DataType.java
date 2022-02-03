package com.krish.empower.jdocs;

public enum DataType {
  STRING("string"),
  DATE("date"),
  BOOLEAN("boolean"),
  INTEGER("integer"),
  LONG("long"),
  DECIMAL("decimal");

  private String dataType;

  DataType(String dataType) {
    this.dataType = dataType;
  }

  public String toString() {
    return dataType;
  }


}
