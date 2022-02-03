package com.krish.empower.flowret;

public interface EventHandler {

  public void invoke(EventType event, ProcessContext pc);

}
