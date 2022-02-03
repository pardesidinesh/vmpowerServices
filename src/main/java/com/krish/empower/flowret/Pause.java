package com.krish.empower.flowret;

public class Pause extends Unit {

  private String next = null;

  protected Pause(String name, String next) {
    super(name, UnitType.PAUSE);
    this.next = next;
  }

  protected String getNext() {
    return next;
  }

  @Override
  protected String getComponentName() {
    return "pause";
  }

  @Override
  protected String getUserData() {
    return null;
  }

}
