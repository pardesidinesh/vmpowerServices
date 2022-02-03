package com.krish.empower.flowret;

import java.util.HashMap;
import java.util.Map;

public class Route extends Unit {

  private Map<String, Branch> branches = new HashMap<>();
  private String componentName = null;
  private String userData = null;
  private String next = null;

  protected Route(String name, String componentName, String userData, Map<String, Branch> branches, UnitType type) {
    super(name, type);
    this.branches = branches;
    this.componentName = componentName;
  }

  protected Route(String name, String componentName, String userData, String next, UnitType type) {
    super(name, type);
    this.next = next;
    this.componentName = componentName;
  }

  protected Branch getBranch(String name) {
    return branches.get(name);
  }

  @Override
  protected String getComponentName() {
    return componentName;
  }

  @Override
  protected String getUserData() {
    return userData;
  }

  protected String getNext() {
    return next;
  }

}
