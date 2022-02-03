package com.krish.empower.decision_table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;
import com.krish.empower.jdocs.UnifyException;
import com.krish.empower.util.Utils;

public class JSONRuleBuilder extends DecisionTableReader{
	
	private Map<String, DTColumn> colsMap = new HashMap<>();
	
	public JSONRuleBuilder(String filePath) {
		this.filePath = filePath;
	}
	
	public DecisionTable getDecisionTable() {
		String jsonContent = Utils.getResourceAsString(JSONRuleBuilder.class, filePath);
		loadDocumentModel();
		Document doc = new JDocument("decision_table", jsonContent);
		version = doc.getString("$.decision_table.version");
		mp = getMatchPolicy(doc);
		nmp = getNoMatchPolicy(doc);
		cols = getColumns(doc);
		rows = getRows(doc);
		defaultRow = getDefaultRow(doc);
		DecisionTable dtable = new DecisionTable(filePath, mp, nmp);
		if(defaultRow != null) {
			dtable.setDefaultRow(defaultRow);
		}
		for(DTColumn column: cols) {
			if(column.getColumnType()==ColumnType.EVALUATE) {
				dtable.addEvalColumn(column);
			}else {
				dtable.addReturnColumn(column);
			}
		}
		return dtable;
	}
	
	private MatchPolicy getMatchPolicy(Document d) {
		String val = d.getString("$.decision_table.match_policy");
		return MatchPolicy.valueOf(val.toUpperCase());
	}
	private NoMatchPolicy getNoMatchPolicy(Document d) {
		String val = d.getString("$.decision_table.no_match_policy");
		return NoMatchPolicy.valueOf(val.toUpperCase());
	}
	
	private List<DTColumn> getColumns(Document d){
		List<DTColumn> cols = new ArrayList<>();
		for(int indx=0, size = d.getArraySize("$.decision_table.cols[]"); indx< size; indx++) {
			String si = String.valueOf(indx);
			String name = d.getString("$.decision_table.cols[%].name", si);
			String type = d.getString("$.decision_table.cols[%].type", si);
			String datatype = d.getString("$.decision_table.cols[%].data_type", si);
			ColumnType ctype = ColumnType.valueOf(type.toUpperCase());
			DataType dtype = DataType.valueOf(datatype.toUpperCase());
			DTColumn dtcol = new DTColumn(name, ctype, dtype);
			cols.add(dtcol);
			colsMap.put(name, dtcol);
		}
		return cols;
	}
	
	private List<DTRow> getRows(Document d){
		List<DTRow> rows = new ArrayList<>();
		for(int ri=0, rsize = d.getArraySize("$.decision_table.rows[]"); ri< rsize; ri++) {
			String sri = String.valueOf(ri);
			DTRow row = new DTRow();
			for(int ci=0, csize = d.getArraySize("$.decision_table.rows[%].cols[]",sri); ci< csize; ci++) {
				String sci = String.valueOf(ci);
				String name = d.getString("$.decision_table.rows[%].cols[%].name", sri, sci);
				String value = d.getString("$.decision_table.rows[%].cols[%].value", sri, sci);
				OperatorType oprType = null;
				DTColumn dtCol = colsMap.get(name);
				if(value != null) {
					if(dtCol.getColumnType() == ColumnType.EVALUATE) {
						int delimIndx = value.indexOf(' ');
						String strOpr = value.substring(0, delimIndx);
						oprType = OperatorType.from(strOpr);
						if(oprType == null) {
							throw new UnifyException("base_err_34", strOpr, filePath, value);
						}
						value = value.substring(delimIndx+1);
					}
				}else if(dtCol.getColumnType() == ColumnType.RETURN) {
					value = "";
				}
				
				if(value!=null) {
					DTCell  cell = new DTCell(name, value, oprType);
					row.addCell(cell);
				}
			}
			rows.add(row);
		}
		
		return rows;
	}
	
	private DTRow getDefaultRow(Document d){
		DTRow defltRow = null;
		if(d.pathExists("$.decision_table.default_row[]")) {
			defltRow = new DTRow();
			for(int ci=0, csize = d.getArraySize("$.decision_table.default_row[]"); ci< csize; ci++) {
				String sci = String.valueOf(ci);
				String name = d.getString("$.decision_table.default_row[%].name", sci);
				String value = d.getString("$.decision_table.default_row[%].value", sci);
				OperatorType oprType = null;
				DTColumn dtCol = colsMap.get(name);
				if(value != null) {
					if(dtCol.getColumnType() == ColumnType.EVALUATE) {
						int delimIndx = value.indexOf(' ');
						String strOpr = value.substring(0, delimIndx);
						oprType = OperatorType.from(strOpr);
						if(oprType == null) {
							throw new UnifyException("base_err_34", strOpr, filePath, value);
						}
						value = value.substring(delimIndx+1);
					}
				}else if(dtCol.getColumnType() == ColumnType.RETURN) {
					value = "";
				}
				
				if(value!=null) {
					DTCell  cell = new DTCell(name, value, oprType);
					defltRow.addCell(cell);
				}
			}
		}
		return defltRow;
	}
}
