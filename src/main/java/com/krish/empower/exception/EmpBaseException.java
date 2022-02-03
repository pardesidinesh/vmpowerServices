package com.krish.empower.exception;

public class EmpBaseException extends Exception{
	protected String strErrorCode = "";
	
	protected String strSQLState = "";
	
	protected String strErrorMessage = "";
	
	protected String strClassName = "";
	
	protected String strMethodName = "";
	
	protected String strNestedException = "";
	
	protected Throwable nestedException = null;
	
	protected EmpBaseException() {
		
	}
	
	protected EmpBaseException(String strErrorMessage, String strClassName, String strMethodName, Throwable nestedException) {
		this.strErrorMessage=strErrorMessage;
		this.strClassName=strClassName;
		this.strMethodName=strMethodName;
		this.nestedException=nestedException;
		
	}
	
	protected EmpBaseException(String strErrorMessage, String strClassName, String strMethodName, String strErrorCode, Throwable nestedException) {
		this.strErrorMessage=strErrorMessage;
		this.strClassName=strClassName;
		this.strMethodName=strMethodName;
		this.strErrorCode=strErrorCode;
		this.nestedException=nestedException;
		
	}
	
	protected EmpBaseException(String strErrorMessage, String strClassName, String strMethodName, String strErrorCode, String strSQLState, Throwable nestedException) {
		this.strErrorMessage=strErrorMessage;
		this.strClassName=strClassName;
		this.strMethodName=strMethodName;
		this.strSQLState = strSQLState;
		this.strErrorCode=strErrorCode;
		this.nestedException=nestedException;
		
	}

	public String getStrErrorCode() {
		return strErrorCode;
	}

	public String getStrSQLState() {
		return strSQLState;
	}

	public String getStrErrorMessage() {
		return strErrorMessage;
	}

	public String getStrClassName() {
		return strClassName;
	}

	public String getStrMethodName() {
		return strMethodName;
	}

	public String getStrNestedException() {
		return strNestedException;
	}

	public Throwable getNestedException() {
		return nestedException;
	}

	public void setNestedException(Throwable nestedException) {
		this.nestedException = nestedException;
	}
	
	
}
