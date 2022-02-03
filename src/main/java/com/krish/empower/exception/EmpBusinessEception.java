package com.krish.empower.exception;

public class EmpBusinessEception extends EmpBaseException{
	private static final long serialVersionUID = 1L;
	
	public EmpBusinessEception() {
		
	}
	
	public EmpBusinessEception(String strErrorCode) {
		this.strErrorCode = strErrorCode;
	}
	
	public EmpBusinessEception(String strErrorMessage, String strClassName, String strMethodName) {
		this.strErrorMessage=strErrorMessage;
		this.strClassName=strClassName;
		this.strMethodName=strMethodName;
		this.nestedException=null;
	}
	
	public EmpBusinessEception(String strErrorMessage, String strClassName, String strMethodName, Throwable nestedException) {
		super(strErrorMessage, strClassName, strMethodName, nestedException);
	}
	
	protected EmpBusinessEception(String strErrorMessage, String strClassName, String strMethodName, String strErrorCode, String strSQLState, Throwable nestedException) {
		super(strErrorMessage, strClassName, strMethodName,strErrorCode, strSQLState, nestedException);
		
	}
}
