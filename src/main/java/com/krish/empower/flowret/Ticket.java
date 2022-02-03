package com.krish.empower.flowret;

public class Ticket {

  private String name = null;
  private String step = null;

  protected Ticket(String name, String step) {
    this.name = name;
    this.step = step;
  }

  protected String getName() {
    return name;
  }

  protected String getStep() {
    return step;
  }

}
