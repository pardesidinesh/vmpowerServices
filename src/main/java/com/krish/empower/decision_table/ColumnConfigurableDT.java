package com.krish.empower.decision_table;

import java.util.List;
import java.util.Map;

import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;
import com.krish.empower.util.Utils;

public class ColumnConfigurableDT {
	public static List<Map<String, RetDTCell>> evaluate(String dtPath, Document appJson, Map<String, String> additionalVars){
		try {
			String inputCont = Utils.getResourceAsString(ColumnConfigurableDT.class, dtPath.replace(".json", "_ip.json"));
			Document mappigDoc = new JDocument(inputCont);
			DTMappingUtil mappingUtil = new DTMappingUtil();
			DTInput dtInput = (DTInput)mappingUtil.map(mappigDoc, "decision_table", appJson, additionalVars);
			
			DecisionTable dtable = DecisionTable.fromJson(dtPath);
			return dtable.evaluate(dtInput.getValues());
		}catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
	}
}
