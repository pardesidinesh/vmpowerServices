package com.krish.empower.flowret;

abstract public class Unit {

  private String name = null;
  private UnitType type = null;

  protected Unit(String name, UnitType type) {
    this.name = name;
    this.type = type;
  }

  protected String getName() {
    return name;
  }

  protected UnitType getType() {
    return type;
  }

  abstract protected String getComponentName();

  abstract protected String getUserData();

}
