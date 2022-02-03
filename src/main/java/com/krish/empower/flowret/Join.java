package com.krish.empower.flowret;

public class Join extends Unit {

  private String next = null;

  protected Join(String name, String next) {
    super(name, UnitType.P_JOIN);
    this.next = next;
  }

  protected String getNext() {
    return next;
  }

  @Override
  protected String getComponentName() {
    return "join";
  }

  @Override
  protected String getUserData() {
    return null;
  }

}
