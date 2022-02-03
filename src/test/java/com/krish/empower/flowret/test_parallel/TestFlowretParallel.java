package com.krish.empower.flowret.test_parallel;

import com.krish.empower.flowret.*;
import com.krish.empower.flowret.test_singular.TestFlowret;
import com.krish.empower.jdocs.BaseUtils;
import com.krish.empower.jdocs.UnifyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;


public class TestFlowretParallel {

  private static String dirPath = null;
  private static Rts rts = null;

  private static FileDao dao = null;
  private static ProcessComponentFactory factory = null;
  private static EventHandler handler = null;

  @BeforeAll
  protected static void setEnv() throws Exception {
    if (dirPath == null) {
      //      System.out.println("Please specify directory path as the first and only argument e.g. C:/Temp/");
      //      System.out.println("Do not forget to supply the trailing /");
      //      System.exit(1);
      dirPath = "C:/Deepak/Temp/";
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
  }

  @AfterAll
  protected static void close() {
    Flowret.instance().close();
  }

  @BeforeEach
  protected void beforeEach() {
    StepResponseFactory.clear();
  }

  // 3 branches, happy path i.e. all branches proceed
  public static void setScenario1() {
    // nothing to do
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends, 3rd branch pends
  public static void setScenario2() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, all 3 error pend
  public static void setScenario2_1() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends and 3rd branch raises ticket
  // then a further pend after ticket is raised
  public static void setScenario3() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "reject");

    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends and 3rd branch raises ticket
  // with a pend, then a further pend after ticket is raised
  public static void setScenario4() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PEND, "", "reject");

    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PROCEED, "", "");
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    ProcessContext pc = null;
    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      pc = rts.startCase("1", json, null, slaJson);
    }

    try {
      while (true) {
        System.out.println();
        rts.resumeCase("1");
      }
    }
    catch (UnifyException e) {
      System.out.println("Exception -> " + e.getMessage());
    }
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test");
  }

  @Test
  void testScenario2() {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test");
  }

  @Test
  void testScenario2_1() {
    setScenario2_1();
    init(new FileDao(dirPath), new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test");
  }

  @Test
  void testScenario3() {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test");
  }

  @Test
  void testScenario4() {
    setScenario4();
    init(new FileDao(dirPath), new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test");
  }

}
