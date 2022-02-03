package com.krish.empower.jdocs;

public class PathValue {

  private String path;
  private Object value;
  private DataType dataType;

  public PathValue(String path, Object value, DataType dataType) {
    this.path = path;
    this.value = value;
    this.dataType = dataType;
  }

  public String getPath() {
    return path;
  }

  public Object getValue() {
    return value;
  }

  public DataType getDataType() {
    return dataType;
  }

}
