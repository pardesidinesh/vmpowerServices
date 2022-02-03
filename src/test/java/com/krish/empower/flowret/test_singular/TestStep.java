package com.krish.empower.flowret.test_singular;

import com.krish.empower.flowret.InvokableStep;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.StepResponse;
import com.krish.empower.flowret.StepResponseFactory;

public class TestStep implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;

  public TestStep(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    String stepName = pc.getStepName();
    if (stepName.equals("step13")) {
      // only there to set a break point
      int i = 0;
    }
    StepResponse sr = StepResponseFactory.getResponse(stepName);
    return sr;
  }

}
