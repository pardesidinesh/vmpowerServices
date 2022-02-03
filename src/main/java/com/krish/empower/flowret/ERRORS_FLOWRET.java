package com.krish.empower.flowret;


import java.util.Map;

import com.krish.empower.jdocs.ErrorMap;

public class ERRORS_FLOWRET extends ErrorMap {

  public static void load() {
    Map<String, String> map = errors;
    map.put("flowret_err_1", "Cannot start a case which is already started. Case Id -> {0}");
    map.put("flowret_err_2", "Could not resume case. No process definition found. Case id -> {0}");
    map.put("flowret_err_3", "Cannot start a case which has already completed. Case id -> {0}");
    map.put("flowret_err_4", "Value object does not conform to process variable data type -> {0}");
    map.put("flowret_err_5", "Unexpected condition encountered. Case id -> {0}");
    map.put("flowret_err_6", "Cannot resume a case that has already completed. Case id -> {0}");
    map.put("flowret_err_7", "Unexpected exception encountered");
    map.put("flowret_err_8", "Route cannot pend upon successful execution");
    map.put("flowret_err_9", "A parallel route cannot have next specified");
    map.put("flowret_err_10", "A dynamic parallel route cannot have branches specified");
    map.put("flowret_err_11", "Journey file for case id {0} does not exist");
    map.put("flowret_err_12", "Case id {0} -> could not find an exec path to pend to. This application cannot be repaired");
  }

}
