package com.krish.empower.decision_table;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krish.empower.base.ExpiryCache;
import com.krish.empower.util.Utils;

public final class DecisionTable {
	private static Logger logger = LoggerFactory.getLogger(DecisionTable.class);
	
	private String name;
	private Map<String, DTColumn> evalColumns=new HashMap<>();
	private Map<String, DTColumn> retColumns=new HashMap<>();
	private List<DTRow> dtRows = new ArrayList<>();
	private DTRow defaultRow = null;
	private MatchPolicy matchPolicy = null;
	private NoMatchPolicy noMatchPolicy=null;
	
	private static Lock rlock = new ReentrantLock(true);
	private static JexlEngine jexlEngine = new JexlBuilder().cache(-1).create();
	
	//dtable caching service
	private static ExpiryCache<DecisionTable> decisionTables = new ExpiryCache<>(300000);
	//invokable caching service
	private static ExpiryCache<Object> invokables = new ExpiryCache<>(300000);
	//expression caching service
	private static ExpiryCache<JexlScript> sripts = new ExpiryCache<>(300000);
	
	public DecisionTable() {
		init("", MatchPolicy.FIRST_MATCH, NoMatchPolicy.RETURN_DEFAULT);
	}
	
	public DecisionTable(String name) {
		init(name, MatchPolicy.FIRST_MATCH, NoMatchPolicy.RETURN_DEFAULT);
	}
	public DecisionTable(String name, MatchPolicy mp, NoMatchPolicy nmp) {
		init(name, mp, nmp);
	}

	public static DecisionTable fromJson(String filePath) {
		DecisionTable dt = decisionTables.get(filePath);
		if(dt==null) {
			try {
				rlock.lock();
				dt = new JSONRuleBuilder(filePath).getDecisionTable();
				decisionTables.put(filePath,dt);
					
			}finally {
				rlock.unlock();
			}
		}
		return dt;
	}
	
	public void init(String name, MatchPolicy mp, NoMatchPolicy nmp) {
		this.name = name;
		this.matchPolicy = mp;
		this.noMatchPolicy=nmp;
	}
	
	public void addEvalColumn(DTColumn c) {
		evalColumns.put(c.getName(), c);
	}
	public void addReturnColumn(DTColumn c) {
		retColumns.put(c.getName(), c);
	}
	public List<DTRow> getDTRows() {
		return this.dtRows;
	}
	public void setDTRows(List<DTRow> rows) {
		this.dtRows = rows;
	}
	
	public DTRow getDefaultRow() {
		return defaultRow;
	}

	public void setDefaultRow(DTRow defaultRow) {
		this.defaultRow = defaultRow;
	}
	
	public List<Map<String, RetDTCell>> evaluate(Map<String, String> values, Object input){
		List<Map<String, RetDTCell>> respCells = new ArrayList();
		boolean flag;
		int rowCount = dtRows.size();
		for(DTRow dtRow: dtRows) {
			flag = evaluateRow(dtRow, values, input);
			if(flag) {
				Map<String, DTCell> dtCells = dtRow.getRetDtCellMap();
				Map<String, RetDTCell> retDTCells = processRetDTCells(dtCells, values, input);
				respCells.add(retDTCells);
				if(this.matchPolicy == MatchPolicy.FIRST_MATCH) {
					break;
				}
			}
		}
		if(respCells.size()==0 && this.noMatchPolicy == NoMatchPolicy.RETURN_DEFAULT) {
			Map<String, DTCell> dtCells = defaultRow.getRetDtCellMap();
			Map<String, RetDTCell> retDTCells = processRetDTCells(dtCells, values, input);
			respCells.add(retDTCells);
		}
		
		return respCells;
	}
	
	private Map<String, RetDTCell> processRetDTCells(Map<String, DTCell> dtCells, Map<String, String> values, Object input){
		Map<String, RetDTCell> retCells = new HashMap<>();
		Set<String> keys = dtCells.keySet();
		for(String key: keys) {
			DTCell dtCell = dtCells.get(key);
			String colName = dtCell.getColumnName();
			DataType dataType = retColumns.get(colName).getDatatype();
			Object value = getRetCellValue(dtCell, values, input);
			RetDTCell retCell = new RetDTCell(colName, dataType, value);
			retCells.put(colName, retCell);
		}
		return retCells;
	}
	
	private Object getRetCellValue(DTCell dtCell, Map<String, String> values, Object input) {
		String s = dtCell.getValue();
		Object value = null;
		DataType dataType = retColumns.get(dtCell.getColumnName()).getDatatype();
		if(!s.isEmpty()) {
			if(s.charAt(0)=='#') {
				value = processInvokable(s.substring(1).trim(), values, input);
			}else if(s.startsWith("\\#")) {
				value = getRetCellValue(s.substring(1).trim(), dataType);
			}else if(s.charAt(0)=='?') {
				value = processScript(s.substring(1).trim(), values);
			}else if(s.startsWith("\\?")) {
				value = getRetCellValue(s.substring(1).trim(), dataType);
			}else{
				value = getRetCellValue(s.trim(), dataType);
			}
		}else {
			value = getRetCellValue(s.trim(), dataType);
		}
		return value;
	}
	
	private Object getRetCellValue(String txt, DataType datatype) {
		switch(datatype) {
			case BOOLEAN:
				return (txt.isEmpty())?null:Boolean.valueOf(txt);
			case DOUBLE:
				return (txt.isEmpty())?null:Double.parseDouble(txt);
			case BIGDECIMAL:
				return (txt.isEmpty())?null:(new BigDecimal(txt));
			case INTEGER:
				return (txt.isEmpty())?null:Integer.parseInt(txt);
			case LONG:
				return (txt.isEmpty())?null:Long.parseLong(txt);
			default:
				return txt;
		}
	}
	
	private Object processInvokable(String methodStr, Map<String, String> values, Object input) {
		try {
			int pos = methodStr.lastIndexOf(".");
			String className = methodStr.substring(0, pos);
			String methodName = methodStr.substring(pos+1);
			Class cls = Class.forName(className);
			Object objRef = cls.getDeclaredConstructor().newInstance();
			Object methodRef = invokables.get(methodStr);
			if(methodRef==null) {
				methodRef = cls.getDeclaredMethod(methodName, Map.class, Object.class);
				invokables.put(methodStr, methodRef);
			}
			Method method = (Method)methodRef;
			return method.invoke(objRef, values, input);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Object processScript(String script, Map<String, String> values) {
		JexlScript jexlScript = sripts.get(script);
		if(jexlScript==null) {
			jexlScript = jexlEngine.createScript(script);
			sripts.put(script, jexlScript);
		}
		JexlContext context = new MapContext();
		values.forEach((key, value)-> context.set(key, value));
		return jexlScript.execute(context);
	}
	
	public List<Map<String, RetDTCell>> evaluate(Map<String, String> values){
		return evaluate(values, null);
	}
	
	private boolean evaluateRow(DTRow dtRow, Map<String, String> values, Object input) {
		boolean respFlag = true;
		List<DTCell> dtCells = dtRow.getEvalDTCells();
		for(DTCell cell: dtCells) {
			respFlag = evaluateCell(cell, values, input);
			if(!respFlag) {
				break;
			}
		}
		return respFlag;
	}
	
	private boolean evaluateCell(DTCell cell, Map<String, String> values, Object input) {
		String columnName = cell.getColumnName();
		String value = values.get(columnName);
		if(value == null) {
			return false;
		}
		
		DTColumn col = evalColumns.get(columnName);
		DataType datatype = col.getDatatype();
		switch(datatype) {
			case LONG:
			case INTEGER:
				return evaluateLong(value, cell, values, input);
			case DOUBLE:
				return evaluateDouble(value, cell, values, input);
			case BIGDECIMAL:
				return evaluateDecimal(value, cell, values, input);
			case STRING:
				return evaluateString(value, cell, values, input);
			case BOOLEAN:
				return evaluateBoolean(value, cell, values, input);
		}
		return true;
	}
	
	private String[] getTrimmedValues(String values) {
		List<String> list = new ArrayList<>();
		int index =0;
		int fromIndx = 0;
		String value = "";
		
		outer:
		while(true) {
			index = values.indexOf(',', fromIndx);
			fromIndx = 0;
			switch(index) {
				case 0:
					values = values.substring(1);
					break;
				case -1:
					value = values;
					list.add(value.trim());
					break outer;
				default:
					if(values.charAt(index-1)=='\\') {
						String s = values.substring(0, index-1);
						values = s + values.substring(index);
						fromIndx = index;
						continue;
					}
					value = values.substring(0, index);
					values = values.substring(index+1);
					break;
					
			}
			list.add(value.trim());
		}
		String[] retVals = new String[list.size()];
		return list.toArray(retVals);
	}
	
	private Object[] getTypedValues(String[] svals, Class clazz, Map<String, String> values, Object input) {
		Object ovals[]= null;
		if(clazz == Long.class) {
			Long[] lvals = new Long[svals.length];
			for(int indx=0; indx<lvals.length;indx++) {
				if(svals[indx].charAt(0)=='#') {
					lvals[indx] = (Long)processInvokable(svals[indx].substring(1).trim(), values, input);
				}else {
					lvals[indx] = Long.valueOf(svals[indx]);
				}
			}
			ovals = lvals;
		}
		else if(clazz == Integer.class) {
			Integer[] ivals = new Integer[svals.length];
			for(int indx=0; indx<ivals.length;indx++) {
				if(svals[indx].charAt(0)=='#') {
					ivals[indx] = (Integer)processInvokable(svals[indx].substring(1).trim(), values, input);
				}else {
					ivals[indx] = Integer.valueOf(svals[indx]);
				}
			}
			ovals = ivals;
		}
		else if(clazz == Double.class) {
			Double[] dvals = new Double[svals.length];
			for(int indx=0; indx<dvals.length;indx++) {
				if(svals[indx].charAt(0)=='#') {
					dvals[indx] = (Double)processInvokable(svals[indx].substring(1).trim(), values, input);
				}else {
					dvals[indx] = Double.valueOf(svals[indx]);
				}
			}
			ovals = dvals;
		}
		else if(clazz == BigDecimal.class) {
			BigDecimal[] bdvals = new BigDecimal[svals.length];
			for(int indx=0; indx<bdvals.length;indx++) {
				if(svals[indx].charAt(0)=='#') {
					bdvals[indx] = (BigDecimal)processInvokable(svals[indx].substring(1).trim(), values, input);
				}else {
					bdvals[indx] = new BigDecimal(svals[indx]);
				}
			}
			ovals = bdvals;
		}
		else if(clazz == String.class) {
			String[] svalsTmp = new String[svals.length];
			for(int indx=0; indx<svalsTmp.length;indx++) {
				if(svals[indx].charAt(0)=='#') {
					svalsTmp[indx] = (String)processInvokable(svals[indx].substring(1).trim(), values, input);
				}else {
					svalsTmp[indx] = svals[indx];
				}
			}
			ovals = svalsTmp;
		}
		return ovals;
	}
	
	private boolean evaluateLong(String value, DTCell c, Map<String, String> values, Object input) {
		long actualVal = Long.valueOf(value);
		if(c.getOprType() == OperatorType.IN) {
			Long[] lvals = (Long[])getTypedValues(getTrimmedValues(c.getValue()), Long.class, values, input);
			for(int indx=0; indx<lvals.length; indx++) {
				if(actualVal == lvals[indx]) {
					return true;
				}
			}
		}else {
			long val2=0;
			String str = c.getValue();
			if(str.charAt(0)=='#') {
				val2 = (Long)processInvokable(str.substring(1).trim(), values, input);
			}else {
				val2 = Long.valueOf(str);
			}
			switch(c.getOprType()) {
				case EQ:
					return (actualVal==val2);
				case NOT_EQ:
					return (actualVal!=val2);
				case GT:
					return (actualVal>val2);
				case LT:
					return (actualVal<val2);
				case GT_EQ:
					return (actualVal>=val2);
				case LT_EQ:
					return (actualVal<=val2);
			}
		}
		return false;
	}
	
	private boolean evaluateDouble(String value, DTCell c, Map<String, String> values, Object input) {
		double actualVal = Double.valueOf(value);
		if(c.getOprType() == OperatorType.IN) {
			Double[] dvals = (Double[])getTypedValues(getTrimmedValues(c.getValue()), Double.class, values, input);
			for(int indx=0; indx<dvals.length; indx++) {
				if(actualVal == dvals[indx]) {
					return true;
				}
			}
		}else {
			double val2=0;
			String str = c.getValue();
			if(str.charAt(0)=='#') {
				val2 = (Double)processInvokable(str.substring(1).trim(), values, input);
			}else {
				val2 = Double.valueOf(str);
			}
			switch(c.getOprType()) {
				case EQ:
					return (actualVal==val2);
				case NOT_EQ:
					return (actualVal!=val2);
				case GT:
					return (actualVal>val2);
				case LT:
					return (actualVal<val2);
				case GT_EQ:
					return (actualVal>=val2);
				case LT_EQ:
					return (actualVal<=val2);
			}
		}
		return false;
	}
	
	private boolean evaluateDecimal(String value, DTCell c, Map<String, String> values, Object input) {
		BigDecimal actualVal = new BigDecimal(value);
		if(c.getOprType() == OperatorType.IN) {
			BigDecimal[] bdvals = (BigDecimal[])getTypedValues(getTrimmedValues(c.getValue()), BigDecimal.class, values, input);
			for(int indx=0; indx<bdvals.length; indx++) {
				if(actualVal.compareTo(bdvals[indx])==0) {
					return true;
				}
			}
		}else {
			BigDecimal val2=null;
			String str = c.getValue();
			if(str.charAt(0)=='#') {
				val2 = (BigDecimal)processInvokable(str.substring(1).trim(), values, input);
			}else {
				val2 = new BigDecimal(str);
			}
			int result = actualVal.compareTo(val2);
			switch(c.getOprType()) {
				case EQ:
					return (result==0);
				case NOT_EQ:
					return (result!=0);
				case GT:
					return (result>0);
				case LT:
					return (result<0);
				case GT_EQ:
					return (result>=0);
				case LT_EQ:
					return (result<=0);
			}
		}
		return false;
	}
	
	private boolean evaluateString(String value, DTCell c, Map<String, String> values, Object input) {
		String actualVal = value;
		if(c.getOprType() == OperatorType.IN) {
			String[] svals = (String[])getTypedValues(getTrimmedValues(c.getValue()), String.class, values, input);
			if(Utils.compareWithMany(actualVal, svals)) {
				return true;
			}
		}else {
			String val2=null;
			String str = c.getValue();
			if(str.charAt(0)=='#') {
				val2 = (String)processInvokable(str.substring(1).trim(), values, input);
			}else {
				val2 = str;
			}
			int result = actualVal.compareTo(val2);
			switch(c.getOprType()) {
				case EQ:
					return (result==0);
				case NOT_EQ:
					return (result!=0);
				case GT:
					return (result>0);
				case LT:
					return (result<0);
				case GT_EQ:
					return (result>=0);
				case LT_EQ:
					return (result<=0);
			}
		}
		return false;
	}
	private boolean evaluateBoolean(String value, DTCell c, Map<String, String> values, Object input) {
		boolean actualVal = Boolean.valueOf(value);
		
		boolean val2=false;
		String str = c.getValue();
		if(str.charAt(0)=='#') {
			val2 = (Boolean)processInvokable(str.substring(1).trim(), values, input);
		}else {
			val2 = Boolean.valueOf(str);
		}
		switch(c.getOprType()) {
			case EQ:
				return (actualVal==val2);
			case NOT_EQ:
				return (actualVal!=val2);
		}
		return false;
	}
	
}
