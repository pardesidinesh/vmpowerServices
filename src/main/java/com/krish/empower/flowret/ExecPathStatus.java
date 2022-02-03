package com.krish.empower.flowret;

public enum ExecPathStatus {
  // A thread will have this status if
  // it is running or
  // it has pended on a step and terminated
  STARTED,

  // A thread will have this status if
  // if has terminated without a pend on a step
  // this means that either the thread has successfully reached a join condition
  // or a thread which was waiting on child threads has terminated because one of the child threads pended
  COMPLETED
}
