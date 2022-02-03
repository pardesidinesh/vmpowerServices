package com.krish.empower.flowret;

import com.krish.empower.jdocs.ErrorTuple;

import java.util.List;


public class RouteResponse {

  private UnitResponseType unitResponseType = null;
  private List<String> branches = null;
  private String workBasket = "";
  private ErrorTuple errorTuple = new ErrorTuple();

  public RouteResponse(UnitResponseType unitResponseType, List<String> branches, String workBasket) {
    init(unitResponseType, branches, workBasket, new ErrorTuple());
  }

  public RouteResponse(UnitResponseType unitResponseType, List<String> branches, String workBasket, ErrorTuple errorTuple) {
    init(unitResponseType, branches, workBasket, errorTuple);
  }

  private void init(UnitResponseType unitResponseType, List<String> branches, String workBasket, ErrorTuple errorTuple) {
    this.unitResponseType = unitResponseType;
    if (branches != null) {
      this.branches = branches;
    }
    if (workBasket != null) {
      this.workBasket = workBasket;
    }
    this.errorTuple = errorTuple;
  }

  public UnitResponseType getUnitResponseType() {
    return unitResponseType;
  }

  public List<String> getBranches() {
    return branches;
  }

  public String getWorkBasket() {
    return workBasket;
  }

  public ErrorTuple getErrorTuple() {
    return errorTuple;
  }

}
