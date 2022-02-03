
package com.krish.empower.flowret;

public class Branch {

  private String name = null;
  private String next = null;

  protected String getName() {
    return name;
  }

  protected String getNext() {
    return next;
  }

  protected Branch(String name, String next) {
    this.name = name;
    this.next = next;
  }

}
