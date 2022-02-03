package com.krish.empower.flowret.test_parallel;

import com.krish.empower.flowret.InvokableStep;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.StepResponse;
import com.krish.empower.flowret.StepResponseFactory;

public class TestStepParallel implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;

  public TestStepParallel(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    String stepName = pc.getStepName();
    return StepResponseFactory.getResponse(stepName);
  }

}

