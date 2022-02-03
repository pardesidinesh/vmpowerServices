package com.krish.empower.flowret.test_parallel;

import com.krish.empower.flowret.ProcessComponentFactory;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.UnitType;

public class TestComponentFactoryParallel implements ProcessComponentFactory {

  @Override
  public Object getObject(ProcessContext pc) {
    Object o = null;

    if ((pc.getCompType() == UnitType.S_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE_DYNAMIC)) {
      o = new TestRuleParallel(pc);
    }
    else if (pc.getCompType() == UnitType.STEP) {
      o = new TestStepParallel(pc);
    }

    return o;
  }

}
