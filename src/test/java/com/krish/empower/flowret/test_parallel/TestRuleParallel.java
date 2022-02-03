package com.krish.empower.flowret.test_parallel;

import com.krish.empower.flowret.InvokableRoute;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.RouteResponse;
import com.krish.empower.flowret.UnitResponseType;

import java.util.ArrayList;
import java.util.List;


public class TestRuleParallel implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRuleParallel(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    List<String> branches = new ArrayList<>();
    RouteResponse resp = null;
    String stepName = pc.getStepName();

    if (stepName.equalsIgnoreCase("route_1")) {
      branches.add("1");
      branches.add("2");
      branches.add("3");
      resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);
    }

    return resp;
  }

}
