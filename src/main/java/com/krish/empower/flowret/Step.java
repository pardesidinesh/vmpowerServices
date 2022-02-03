package com.krish.empower.flowret;

public class Step extends Unit {

  private String next = null;
  private String componentName = null;
  private String userData = null;

  protected Step(String name, String componentName, String next, String userData) {
    super(name, UnitType.STEP);
    this.next = next;
    this.componentName = componentName;
    this.userData = userData;
  }

  protected String getNext() {
    return next;
  }

  @Override
  protected String getComponentName() {
    return componentName;
  }

  @Override
  protected String getUserData() {
    return userData;
  }

}
