package com.krish.empower.flowret.test_parallel_dyn;


import com.krish.empower.flowret.*;
import com.krish.empower.jdocs.BaseUtils;

import java.util.ArrayList;
import java.util.List;

public class TestRuleParallelSupps implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRuleParallelSupps(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    List<String> branches = new ArrayList<>();
    RouteResponse resp = null;
    String name = pc.getCompName();
    ProcessVariables pvs = pc.getProcessVariables();
    String execPathPvName = "supp_exec_path_name";
    String processSuppsPvName = "process_supps";

    while (true) {
      if (BaseUtils.compareWithMany(name, "route_0")) {
        Boolean processSupps = pvs.getBoolean(processSuppsPvName);
        if (processSupps == null) {
          pvs.setValue(processSuppsPvName, ProcessVariableType.BOOLEAN, true);
          branches.add("yes");
        }
        else {
          pvs.setValue(processSuppsPvName, ProcessVariableType.BOOLEAN, false);
          branches.add("no");
        }

        break;
      }

      if (BaseUtils.compareWithMany(name, "route_1_c")) {
        pvs.setValue(execPathPvName, ProcessVariableType.STRING, pc.getExecPathName());

        // simulate 5 supps
        branches.add("ai_index_1");
        branches.add("ai_index_2");
        branches.add("ai_index_3");
        branches.add("ai_index_4");
        branches.add("ai_index_5");

        break;
      }

      break;
    }

    resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);

    return resp;
  }

}
