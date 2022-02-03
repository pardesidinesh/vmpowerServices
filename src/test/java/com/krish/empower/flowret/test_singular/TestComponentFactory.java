package com.krish.empower.flowret.test_singular;


import com.krish.empower.flowret.ProcessComponentFactory;
import com.krish.empower.flowret.ProcessContext;
import com.krish.empower.flowret.test_singular.TestStep;
import com.krish.empower.flowret.UnitType;

/*
 * @author Deepak Arora
 */
public class TestComponentFactory implements ProcessComponentFactory {

  @Override
  public Object getObject(ProcessContext pc) {
    Object o = null;

    if ((pc.getCompType() == UnitType.S_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE) || (pc.getCompType() == UnitType.P_ROUTE_DYNAMIC)) {
      o = new TestRule(pc);
    }
    else if (pc.getCompType() == UnitType.STEP) {
      o = new TestStep(pc);
    }

    return o;
  }

}
