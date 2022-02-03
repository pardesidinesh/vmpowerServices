package com.krish.empower.flowret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessDefinition {

  private String name = null;
  private Map<String, Ticket> tickets = new HashMap<>();
  private List<ProcessVariable> processVariables = new ArrayList<>();
  private Map<String, Unit> units = null;

  protected ProcessDefinition() {
    this.units = new HashMap<>();
  }

  protected String getName() {
    return name;
  }

  protected void setName(String name) {
    this.name = name;
  }

  protected void addUnit(Unit unit) {
    units.put(unit.getName(), unit);
  }

  protected Unit getUnit(String name) {
    return units.get(name);
  }

  protected Ticket getTicket(String name) {
    return tickets.get(name);
  }

  protected void setTicket(Ticket ticket) {
    tickets.put(ticket.getName(), ticket);
  }

  protected void setProcessVariables(List<ProcessVariable> processVariables) {
    this.processVariables = processVariables;
  }

  protected List<ProcessVariable> getProcessVariables() {
    return processVariables;
  }

}
