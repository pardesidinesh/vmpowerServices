package com.krish.empower.flowret;

import com.krish.empower.jdocs.Document;

public interface ISlaQueueManager {

  void enqueue(ProcessContext pc, Document milestones);

  void dequeue(ProcessContext pc, String wb);

  void dequeueAll(ProcessContext pc);

}
