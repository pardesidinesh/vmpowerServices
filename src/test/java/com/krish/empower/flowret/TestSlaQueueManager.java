package com.krish.empower.flowret;

import com.krish.empower.jdocs.Document;

public class TestSlaQueueManager implements ISlaQueueManager {

  @Override
  public void enqueue(ProcessContext pc, Document milestones) {
    System.out.println("Received enqueue request. Json below");
    System.out.println(milestones.getPrettyPrintJson());
  }

  @Override
  public void dequeue(ProcessContext pc, String wb) {
    System.out.println("Received dequeue request for workbasket -> " + wb);
  }

  @Override
  public void dequeueAll(ProcessContext pc) {
    System.out.println("Received dequeue all request");
  }

}
