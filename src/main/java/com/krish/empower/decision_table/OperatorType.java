package com.krish.empower.decision_table;

public enum OperatorType {
	GT_EQ(">="),
	GT(">"),
	EQ("="),
	LT("<"),
	LT_EQ("<="),
	NOT_EQ("<>"),
	IN ("IN");
	
	private String opr = null;
	
	OperatorType(String opr) { this.opr = opr; }
	
	public String getOperatorString() { return opr;}
	public static OperatorType from(String s) {
		OperatorType opr = null;
		
		switch (s) {
		case ">=":
			opr = GT_EQ;
			break;
			
		case ">":
			opr = GT;
			break;
			
		case "=":
			opr = EQ;
			break;
			
		case "<":
			opr = LT;
			break;
			
		case "<=":
			opr = LT_EQ;
			
		case "<>":
		case "!=":
			opr = NOT_EQ;
			break;
			
		case "IN":
			opr = IN;
			break;
		default:
			//nothing to do. We return null
		}
		return opr;
	}
}
