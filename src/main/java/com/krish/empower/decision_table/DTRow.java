package com.krish.empower.decision_table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DTRow {
	private List<DTCell> evalDTCells = new ArrayList<>();
	private List<DTCell> retDTCells = new ArrayList<>();
	private Map<String, DTCell> retDtCellMap= new HashMap<>();
	
	public void addCell(DTCell cell) {
		if(cell!=null) {
			if(cell.getOprType()!=null) {
				evalDTCells.add(cell);
			}else {
				retDtCellMap.put(cell.getColumnName(), cell);
				retDTCells.add(cell);
			}
		}
	}

	public List<DTCell> getEvalDTCells() {
		return evalDTCells;
	}

	public List<DTCell> getRetDTCells() {
		return retDTCells;
	}

	public Map<String, DTCell> getRetDtCellMap() {
		return retDtCellMap;
	}
	
	
}
