package com.krish.empower.flowret;

import com.krish.empower.jdocs.ErrorTuple;


public class StepResponse {

  private UnitResponseType unitResponseType = null;
  private String ticket = "";
  private String workBasket = "";
  private ErrorTuple errorTuple = new ErrorTuple();

  public StepResponse(UnitResponseType unitResponseType, String ticket, String workBasket) {
    init(unitResponseType, ticket, workBasket, new ErrorTuple());
  }

  public StepResponse(UnitResponseType unitResponseType, String ticket, String workBasket, ErrorTuple errorTuple) {
    init(unitResponseType, ticket, workBasket, errorTuple);
  }

  private void init(UnitResponseType unitResponseType, String ticket, String workBasket, ErrorTuple errorTuple) {
    this.unitResponseType = unitResponseType;
    if (ticket != null) {
      this.ticket = ticket;
    }
    if (workBasket != null) {
      this.workBasket = workBasket;
    }
    this.errorTuple = errorTuple;
  }

  public UnitResponseType getUnitResponseType() {
    return unitResponseType;
  }

  public String getWorkBasket() {
    return workBasket;
  }

  public String getTicket() {
    return ticket;
  }

  public ErrorTuple getErrorTuple() {
    return errorTuple;
  }

}
