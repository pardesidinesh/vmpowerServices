package com.krish.empower.flowret.test_parallel_dyn;

import com.krish.empower.flowret.ProcessComponentFactory;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.UnitType;

public class TestComponentFactoryParallelSupps implements ProcessComponentFactory {

  @Override
  public Object getObject(ProcessContext pc) {
    Object o = null;

    if ((pc.getCompType() == UnitType.S_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE_DYNAMIC)) {
      o = new TestRuleParallelSupps(pc);
    }
    else if (pc.getCompType() == UnitType.STEP) {
      o = new TestStepParallelSupps(pc);
    }

    return o;
  }

}
