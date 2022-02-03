package com.krish.empower.jdocs;

class ArrayToken extends Token {

  public enum FilterType {

    NAME_VALUE, INDEX, EMPTY

  }

  public class Filter {

    private FilterType type = null;

    private String field = null;

    private String value = null;

    private int index = -1;

    public Filter(String field, String value) {
      this.field = field;
      this.value = value;
      type = FilterType.NAME_VALUE;
    }

    public Filter(int index) {
      this.index = index;
      type = FilterType.INDEX;
    }

    public Filter() {
      type = FilterType.EMPTY;
    }

    public FilterType getType() {
      return type;
    }

    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }

    public int getIndex() {
      return index;
    }

  }

  private Filter filter = null;

  public ArrayToken(String name, String field, String value, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(field, value);
  }

  public ArrayToken(String name, int index, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter(index);
  }

  public ArrayToken(String name, boolean isLeaf) {
    super(name, isLeaf);
    filter = new Filter();
  }

  @Override
  public boolean isArray() {
    return true;
  }

  public Filter getFilter() {
    return filter;
  }

}
