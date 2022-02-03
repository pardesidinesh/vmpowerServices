package com.krish.empower.flowret.test_parallel_dyn;


import com.krish.empower.flowret.*;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author Deepak Arora
 */
public class TestStepParallelSupps implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;
  private static Map<String, String> responses = new ConcurrentHashMap<>();

  public TestStepParallelSupps(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    StepResponse response = null;

    String s = MessageFormat.format("Step -> {0}, execution path -> {1}", pc.getStepName(), pc.getExecPathName());
    System.out.println(s);

    response = get(100, UnitResponseType.OK_PROCEED, null);
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException e) {
    }

    System.out.println("Exiting " + s);

    return response;
  }

  private StepResponse get(int percent, UnitResponseType first, UnitResponseType second) {
    int num = RandomGen.get(1, 100);
    if (num <= percent) {
      if (first == UnitResponseType.OK_PROCEED) {
        return new StepResponse(first, null, null);
      }
      else {
        return new StepResponse(first, null, "some_wb");
      }
    }
    else {
      if (second == UnitResponseType.OK_PROCEED) {
        return new StepResponse(second, null, null);
      }
      else {
        return new StepResponse(second, null, "some_wb");
      }
    }
  }

}
