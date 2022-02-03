package com.krish.empower.flowret;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
public class StepResponseFactory {

  private static Map<String, Queue<StepResponse>> actions = new HashMap<>();

  public synchronized static void addResponse(String stepName, UnitResponseType urt, String wb, String ticket) {
    StepResponse r = new StepResponse(urt, ticket, wb);
    Queue<StepResponse> q = actions.get(stepName);
    if (q == null) {
      q = new LinkedList<>();
      actions.put(stepName, q);
    }
    q.add(r);
  }

  public synchronized static StepResponse getResponse(String stepName) {
    StepResponse r = new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    Queue<StepResponse> q = actions.get(stepName);
    if (q != null) {
      if (q.size() > 0) {
        r = q.remove();
      }
    }

    // for setting a break point only
    if (stepName.equals("step16")) {
      int i = 0;
    }

    return r;
  }

  public synchronized static void clear() {
    actions.clear();
  }

}
