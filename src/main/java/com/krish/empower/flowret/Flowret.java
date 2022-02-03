package com.krish.empower.flowret;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.krish.empower.flowret.CONSTS_FLOWRET.DAO;
import com.krish.empower.jdocs.UnifyException;

public class Flowret {

  private static Flowret singleton = new Flowret();
  private int maxThreads = 10;
  private int idleTimeout = 30000;
  private ExecutorService es = null;
  private volatile boolean writeAuditLog = true;
  private volatile boolean writeProcessInfoAfterEachStep = true;

  /**
   * @return an instance of Flowret
   */
  public static Flowret instance() {
    return singleton;
  }

  /**
   * Get the run time service of Flowret
   *
   * @param dao      An object called on by Flowret for persisting the state of the process to the data store
   * @param factory  An object called upon by Flowret to get an instance of an object on which to invoke step and route execute methods
   * @param listener An object on which the application call back events are passed
   * @param slaQm    An object on which the SLA enqueue and dequeue events are passed
   * @return
   */
  public Rts getRunTimeService(FlowretDao dao, ProcessComponentFactory factory, EventHandler listener, ISlaQueueManager slaQm) {
    return new Rts(dao, factory, listener, slaQm);
  }

  /**
   * Get the work manager service of Flowret
   *
   * @param dao   An object called on by Flowret for persisting the state of the process to the data store
   * @param wm    An object whose methods are called by Flowret to do work management functions
   * @param slaQm An object on which the SLA enqueue and dequeue events are passed
   * @return
   */
  public Wms getWorkManagementService(FlowretDao dao, WorkManager wm, ISlaQueueManager slaQm) {
    return new Wms(dao, wm, slaQm);
  }

  private Flowret() {
  }

  /**
   * Method that is called for initializing Flowret
   *
   * @param maxThreads  specifies the number of threads used for parallel processing
   * @param idleTimeout specifies the time out in milliseconds after which parallel processing threads will die out if idle
   * @param typeIdSep   specifies the separator character to use to separate the type and the id in the document name used to persist in the data store
   */
  public static void init(int maxThreads, int idleTimeout, String typeIdSep) {
    Flowret am = instance();
    am.maxThreads = maxThreads;
    am.idleTimeout = idleTimeout;
    am.es = new ThreadPoolExecutor(am.maxThreads, am.maxThreads, am.idleTimeout, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(am.maxThreads * 2), new RejectedItemHandler());
    DAO.SEP = typeIdSep;
    ERRORS_FLOWRET.load();
  }

  /**
   * Method that is used to close Flowret
   */
  public static void close() {
    singleton.es.shutdown();
    try {
      singleton.es.awaitTermination(5, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      // should never happen
      throw new UnifyException("flowret_err_7", e);
    }
    singleton.es = null;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public int getIdleTimeout() {
    return idleTimeout;
  }

  protected ExecutorService getExecutorService() {
    return es;
  }

  public void setWriteAuditLog(boolean writeAuditLog) {
    this.writeAuditLog = writeAuditLog;
  }

  public void setWriteProcessInfoAfterEachStep(boolean writeProcessInfoAfterEachStep) {
    this.writeProcessInfoAfterEachStep = writeProcessInfoAfterEachStep;
  }

  public boolean isWriteAuditLog() {
    return writeAuditLog;
  }

  public boolean isWriteProcessInfoAfterEachStep() {
    return writeProcessInfoAfterEachStep;
  }

}
