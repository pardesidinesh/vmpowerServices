package com.krish.empower.flowret.test_singular;

import com.krish.empower.flowret.InvokableRoute;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.RouteResponse;
import com.krish.empower.flowret.RouteResponseFactory;

public class TestRule implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRule(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    String stepName = pc.getStepName();
    return RouteResponseFactory.getResponse(stepName);
  }

}
