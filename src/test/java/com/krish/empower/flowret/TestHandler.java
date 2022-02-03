package com.krish.empower.flowret;

public class TestHandler implements EventHandler {

  @Override
  public void invoke(EventType event, ProcessContext pc) {
    System.out.println("Received event -> " + event.toString() + ", isPendAtSameStep -> " + pc.isPendAtSameStep());

    if (event == EventType.ON_PROCESS_PEND) {
      System.out.println("Pend workbasket -> " + pc.getPendWorkBasket());
    }
  }

}
