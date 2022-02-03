package com.krish.empower.decision_table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;
import com.krish.empower.util.Utils;

public abstract class DecisionTableReader {
	protected String version;
	protected String filePath;
	protected MatchPolicy mp;
	protected NoMatchPolicy nmp;
	protected List<DTColumn> cols;
	protected List<DTRow> rows;
	protected DTRow defaultRow;

	public abstract DecisionTable getDecisionTable();

	protected final void loadDocumentModel() {
		if (!JDocument.isDocumentModelLoaded("decision_table")) {
			String json = Utils.getResourceAsString(JSONRuleBuilder.class, "/flowret/models/decision_table.json");
			Document modelObj = new JDocument(json);
			JDocument.setDocumentModel("decision_table", modelObj);
		}
	}
	
	private Map<String, DTCell> getCellsMap(List<DTCell> evalCells){
		Map<String, DTCell> map = new HashMap<>();
		for(DTCell cell: evalCells) {
			map.put(cell.getColumnName(), cell);
		}
		return map;
	}
	
	public final String getJson() {
		loadDocumentModel();
		Document d = new JDocument("decision_table", null);
		d.setString("$.decision_table.version", version);
		d.setString("$.decision_table.match_policy", mp.toString().toLowerCase());
		d.setString("$.decision_table.no_match_policy", mp.toString().toLowerCase());
		
		for(int indx=0;indx<cols.size();indx++) {
			DTColumn dtCol = cols.get(indx);
			String si = String.valueOf(indx);
			d.setString("$.decision_table.cols[%].name", dtCol.getName(), si);
			d.setString("$.decision_table.cols[%].type", dtCol.getColumnType().toString().toLowerCase(), si);
			d.setString("$.decision_table.cols[%].data_type", dtCol.getDatatype().toString().toLowerCase(), si);
		}
		
		for(int rindx=0;rindx<rows.size();rindx++) {
			String sri = String.valueOf(rindx);
			DTRow dtRow = rows.get(rindx);
			Map<String, DTCell> evalCellsMap = getCellsMap(dtRow.getEvalDTCells());
			Map<String, DTCell> retCellsMap = getCellsMap(dtRow.getRetDTCells());
			for(int cindx=0;cindx<cols.size();cindx++) {
				String sci = String.valueOf(cindx);
				String name = cols.get(cindx).getName();
				DTCell cell = evalCellsMap.get(name);
				if(cell==null) {
					cell = retCellsMap.get(name);
				}
				String str = null;
				if(cell!=null) {
					if(cols.get(cindx).getColumnType() == ColumnType.EVALUATE) {
						str = cell.getOprType().getOperatorString() +" "+cell.getValue();
					}else {
						str = cell.getValue();
					}
					if(str!=null && str.isEmpty()) {
						str = null;
					}
				}
				d.setString("$.decision_table.rows[%].cols[%].name", name, sri, sci);
				d.setString("$.decision_table.rows[%].cols[%].value", str, sri, sci);
				
			}
		}
		
		if(defaultRow!=null) {
			Map<String, DTCell> evalCellsMap = getCellsMap(defaultRow.getEvalDTCells());
			Map<String, DTCell> retCellsMap = getCellsMap(defaultRow.getRetDTCells());
			for(int cindx=0;cindx<cols.size();cindx++) {
				String sci = String.valueOf(cindx);
				String name = cols.get(cindx).getName();
				DTCell cell = evalCellsMap.get(name);
				if(cell==null) {
					cell = retCellsMap.get(name);
				}
				String str = null;
				if(cell!=null) {
					if(cols.get(cindx).getColumnType() == ColumnType.EVALUATE) {
						str = cell.getOprType().getOperatorString() +" "+cell.getValue();
					}else {
						str = cell.getValue();
					}
					if(str!=null && str.isEmpty()) {
						str = null;
					}
				}
				d.setString("$.decision_table.default_row[%].name", name, sci);
				d.setString("$.decision_table.default_row[%].value", str, sci);
				
			}
		}
		
		
		return d.getPrettyPrintJson();
	}

}
