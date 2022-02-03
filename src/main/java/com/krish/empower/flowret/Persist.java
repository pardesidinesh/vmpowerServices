package com.krish.empower.flowret;

public class Persist extends Unit {

  private String next = null;

  protected Persist(String name, String next) {
    super(name, UnitType.PERSIST);
    this.next = next;
  }

  protected String getNext() {
    return next;
  }

  @Override
  protected String getComponentName() {
    return "persist";
  }

  @Override
  protected String getUserData() {
    return null;
  }

}
