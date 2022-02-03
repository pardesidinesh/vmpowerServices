package com.krish.empower.flowret.test_parallel_dyn;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.krish.empower.flowret.ERRORS_FLOWRET;
import com.krish.empower.flowret.EventHandler;
import com.krish.empower.flowret.FileDao;
import com.krish.empower.flowret.Flowret;
import com.krish.empower.flowret.ProcessComponentFactory;
import com.krish.empower.flowret.Rts;
import com.krish.empower.flowret.TestHandler;
import com.krish.empower.jdocs.BaseUtils;

public class TestFlowretParallelDynamic {

  private static FileDao dao = null;
  private static ProcessComponentFactory factory = null;
  private static EventHandler handler = null;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Please specify directory path as the first and only argument e.g. C:/Temp/");
      System.out.println("Do not forget to supply the trailing /");
      System.exit(1);
    }

    String dirPath = args[0];
    String journey = "parallel_dyn_test";
    String json = BaseUtils.getResourceAsString(TestFlowretParallelDynamic.class, "/flowret/" + journey + ".json");

    init(dirPath);
    Rts rts = Flowret.instance().getRunTimeService(dao, factory, handler, null);

    if (new File(dirPath + "flowret_journey-3.json ").exists() == false) {
      rts.startCase("3", json, null, null);
    }
    else {
      rts.resumeCase("3");
    }

    close();
  }

  @BeforeAll
  protected static void init(String dirPath) {
    ERRORS_FLOWRET.load();
    dao = new FileDao(dirPath);
    factory = new TestComponentFactoryParallelSupps();
    handler = new TestHandler();
    Flowret.init(10, 30000, "-");
  }

  @AfterAll
  protected static void close() {
    Flowret.instance().close();
  }

}
