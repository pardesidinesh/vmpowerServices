package com.krish.empower.jdocs;

class Token {

  private String field;

  private boolean isLeaf;

  public Token(String field, boolean isLeaf) {
    this.field = field;
    this.isLeaf = isLeaf;
  }

  public String getField() {
    return field;
  }

  public boolean isArray() {
    return false;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

}
