package com.krish.empower.flowret;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.UnifyException;

public final class Wms {

  private static Logger logger = LoggerFactory.getLogger(Wms.class);

  // variables are protected so that they can be accessed by classes in the same package
  protected FlowretDao dao = null;
  protected WorkManager wm = null;
  protected ISlaQueueManager slaQm = null;
  protected ProcessInfo pi = null;
  protected ProcessDefinition pd = null;
  protected Document slad = null;

  protected Wms(FlowretDao dao, WorkManager wm, ISlaQueueManager slaQm) {
    this.dao = dao;
    this.wm = wm;
    this.slaQm = slaQm;
  }

  public void changeWorkBasket(String caseId, String newWb) {
    setup(caseId);

    // update process info
    ExecPath ep = pi.getExecPath(pi.getPendExecPath());
    String prevWb = ep.getPrevPendWorkBasket();
    String currWb = ep.getPendWorkBasket();
    String tbcSlaWb = ep.getTbcSlaWorkBasket();
    ep.setPrevPendWorkBasket(currWb);
    ep.setPendWorkBasket(newWb);

    // get pc as per updated pi
    ProcessContext pc = ProcessContext.forWms(pd, pi);

    boolean isError = false;
    try {
      // call the work manager on the application
      if (wm != null) {
        wm.changeWorkBasket(pc, currWb, newWb);
      }
    }
    catch (Exception e) {
      isError = true;
      logger.error("Error encountered while invoking work manager in the application. Case id -> {}, error message -> {}", pi.getCaseId(), e.getMessage());

      // undo the changes
      ep.setPrevPendWorkBasket(prevWb);
      ep.setPendWorkBasket(currWb);
    }

    if (isError == false) {
      try {
        // enqueue / dequeue as required
        if (currWb.equals(newWb) == false) {
          if (currWb.equals(tbcSlaWb) == false) {
            if (slaQm != null) {
              Utils.dequeueWorkBasketMilestones(pc, currWb, slad, slaQm);
            }
          }

          if (newWb.equals(tbcSlaWb) == false) {
            if ((slad != null) && (slaQm != null)) {
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, newWb, slad, slaQm);
            }
          }
        }
      }
      catch (Exception e) {
        isError = true;
        logger.error("Error encountered while invoking sla queue manager in the application. Case id -> {}, error message -> {}", pi.getCaseId(), e.getMessage());
      }
    }

    // write audit log
    Utils.writeAuditLog(dao, pi, null, null, "Wms");

    // process info
    Document d = pi.getDocument();
    dao.write(CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + pi.getCaseId(), d);
  }

  private void setup(String caseId) {
    String key = CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + caseId;

    // check that the document should exist
    Document d = dao.read(key);
    if (d == null) {
      throw new UnifyException("flowret_err_11", caseId);
    }

    // read the process definition and get process info
    pd = Utils.getProcessDefinition(d);
    pi = Utils.getProcessInfo(dao, caseId, pd);

    key = CONSTS_FLOWRET.DAO.JOURNEY_SLA + CONSTS_FLOWRET.DAO.SEP + caseId;
    slad = dao.read(key);
  }

  public String getPendWorkbasket(String caseId) {
    setup(caseId);
    return pi.getPendWorkBasket();
  }

}
