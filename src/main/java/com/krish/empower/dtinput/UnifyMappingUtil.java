package com.krish.empower.dtinput;

import com.krish.empower.decision_table.DecisionTable;
import com.krish.empower.decision_table.DefaultContainerPopulation;
import com.krish.empower.jdocs.BaseUtils;
import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;
import com.krish.empower.jdocs.UnifyException;
import com.krish.empower.util.DateUtils;
import com.krish.empower.util.Utils;
import org.apache.commons.jexl3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class UnifyMappingUtil {
    private static Logger logger = LoggerFactory.getLogger(UnifyMappingUtil.class);
    private final JexlEngine jexl = new JexlBuilder().create();
    private DefaultContainerPopulation defaultContainerPopulation;
    private Object container;
    private String containerClass;
    private Document srcJson;
    private Map<String, String> additionalVariables;
    private String recordFormat;
    private String delimiter;
    private Document responseDocument;
    private StringBuilder mappedResponse;
    private String source;
    private String target;
    private Map<String, Integer> exprVsCurrIdxMap;
    private boolean isExclusion=false;
    private boolean isInclusion=true;
    private Map<String, String> utilVars;
    private static Map<String, Function<Object[], Object>> funcDefMap=new ConcurrentHashMap<>();
    private static Map<String, String> targetDatatypes = new HashMap<>();

    static{
        targetDatatypes.put(MappingUtilConstants.STRING, MappingUtilConstants.STRING);
        targetDatatypes.put(MappingUtilConstants.ARRAY, MappingUtilConstants.ARRAY);
        targetDatatypes.put(MappingUtilConstants.INTEGER, MappingUtilConstants.INTEGER);
        targetDatatypes.put(MappingUtilConstants.DECIMAL, MappingUtilConstants.DECIMAL);
        targetDatatypes.put(MappingUtilConstants.BOOLEAN, MappingUtilConstants.BOOLEAN);
        targetDatatypes.put(MappingUtilConstants.DATE, MappingUtilConstants.DATE);
        targetDatatypes.put(MappingUtilConstants.TIMESTAMP, MappingUtilConstants.TIMESTAMP);
        targetDatatypes.put(MappingUtilConstants.LONG, MappingUtilConstants.LONG);
        targetDatatypes.put(MappingUtilConstants.STRING_ARRAY, MappingUtilConstants.STRING_ARRAY);
        targetDatatypes.put(MappingUtilConstants.LONG_ARRAY, MappingUtilConstants.LONG_ARRAY);
        targetDatatypes.put(MappingUtilConstants.INTEGER_ARRAY, MappingUtilConstants.INTEGER_ARRAY);
        targetDatatypes.put(MappingUtilConstants.BIGDECIMEL_ARRAY, MappingUtilConstants.BIGDECIMEL_ARRAY);
        targetDatatypes.put(MappingUtilConstants.BOOLEAN_ARRAY, MappingUtilConstants.BOOLEAN_ARRAY);

        funcDefMap.put("@SUBSTRING", UnifyMappingUtil::substring);
        funcDefMap.put("@TO_LOWERCASE", UnifyMappingUtil::toLowercase);
        funcDefMap.put("@TO_UPPERCASE", UnifyMappingUtil::toUppercase);
        funcDefMap.put("@ADD_BIZ_DAYS", UnifyMappingUtil::addBusinessDays);
        funcDefMap.put("@ADD_CAL_DAYS", UnifyMappingUtil::addCalenderDays);
        funcDefMap.put("@MAP_VALUE", UnifyMappingUtil::mapValue);
        funcDefMap.put("@TO_LONG", UnifyMappingUtil::toLong);
        funcDefMap.put("@TO_DOUBLE", UnifyMappingUtil::toDouble);
        funcDefMap.put("@TO_FLOAT", UnifyMappingUtil::toFloat);
        funcDefMap.put("@TO_INT", UnifyMappingUtil::toInteger);
        funcDefMap.put("@CONCAT", UnifyMappingUtil::concat);
        funcDefMap.put("@CURRENT_DATE", UnifyMappingUtil::currentDate);
        funcDefMap.put("@FORMAT_AS_DATE", UnifyMappingUtil::formatAsDate);
        funcDefMap.put("@DATE_DIFF_IN_DAYS", UnifyMappingUtil::dateDiffInDays);
        funcDefMap.put("@GET_ZONED_DATETIME", UnifyMappingUtil::getZonedDateTime);
        funcDefMap.put("@CONCAT_REPLACE_STRINGS", UnifyMappingUtil::concatAndReplaceStrings);
        //funcDefMap.put("@ENCRYPT_ATTRIBUTE", UnifyMappingUtil::getHypedEncryptedData);
    }

    private static Object substring(Object[] vargs){
        String source = (String)vargs[0];
        int start = Integer.valueOf(String.valueOf(vargs[1]));
        int end = Integer.valueOf(String.valueOf(vargs[2]));
        String finalStr = StringUtils.isNotBlank(source)?source.substring(start, end):source;
        return finalStr;
    }
    private static Object toLowercase(Object[] vargs){
       String source = (String)vargs[0];
       return source.toLowerCase();
    }
    private static Object toUppercase(Object[] vargs){
        String source = (String)vargs[0];
        return source.toUpperCase();
    }
    private static Object toInt(Object[] vargs) {
        String source = (String)vargs[0];
        return Integer.valueOf(source);
    }
    private static Object mapValue(Object[] vargs){
        String source = (String)vargs[0];
        String decisionTbalePath = (String)vargs[1];
        DecisionTable decisionTable = DecisionTable.fromJson(decisionTbalePath);
        Map<String, String> values = new HashMap<>();
        values.put("input", BaseUtils.getEmptyWhenNull(source));

        return decisionTable.evaluate(values).get(0).get("output").getString();
    }
    private static Object toLong(Object[] vargs){
        String source = (String)vargs[0];
        return Long.valueOf(source);
    }
    private static Object toDouble(Object[] vargs){
        String source = (String)vargs[0];
        return Double.valueOf(source);
    }
    private static Object toFloat(Object[] vargs){
        String source = (String)vargs[0];
        return Float.valueOf(source);
    }
    private static Object toInteger(Object[] vargs){
        return null;
    }
    private static Object concat(Object[] vargs){
        String source1 = (String)vargs[0];
        String source2 = (String)vargs[1];
        return source1.concat(" "+ source2);
    }
    private static Object addCalenderDays(Object[] vargs) {
        String source = (String)vargs[0];
        Integer daysToAdd = Integer.valueOf(String.valueOf(vargs[1]));
        String targetDateFormat = (String)vargs[2];
        String targetTzId = (String)vargs[3];
        Timestamp creationTime = BaseUtils.isNullOrEmpty(source) ? new Timestamp(System.currentTimeMillis()): BaseUtils.getTimestampFromString(source);
        Timestamp updatedTime = DateUtils.getNextCalenderDay(creationTime, daysToAdd);
        SimpleDateFormat sdf = new SimpleDateFormat(targetDateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of(targetTzId)));
        return sdf.format(updatedTime);
    }
    private static Object addBusinessDays(Object[] vargs) {
        String source = (String)vargs[0];
        Integer daysToAdd = Integer.valueOf(String.valueOf(vargs[1]));
        String targetDateFormat = (String)vargs[2];
        String targetTzId = (String)vargs[3];
        Timestamp creationTime = BaseUtils.isNullOrEmpty(source) ? new Timestamp(System.currentTimeMillis()): BaseUtils.getTimestampFromString(source);
        Timestamp updatedTime = DateUtils.getNextBusinessDay(creationTime, daysToAdd);
        SimpleDateFormat sdf = new SimpleDateFormat(targetDateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of(targetTzId)));
        return sdf.format(updatedTime);
    }
    private static Object currentDate(Object[] vargs){
        String dateFormat = (String)vargs[0];
        return BaseUtils.fromDate(new Date(), dateFormat);
    }
    private static Object formatAsDate(Object[] vargs){
        String source = (String)vargs[0];
        String sourceFormat = (String)vargs[1];
        String targetFormat = (String)vargs[2];
        SimpleDateFormat sourceSDF = new SimpleDateFormat(sourceFormat);
        SimpleDateFormat targetSDF = new SimpleDateFormat(targetFormat);
        if(vargs.length>5){
            String sourceTimezone = (String)vargs[3];
            String targetTimezone = (String)vargs[4];
            sourceSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(sourceTimezone)));
            targetSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(targetTimezone)));

            try {
                return targetSDF.format(sourceSDF.parse(source));
            }catch (ParseException ex){
                throw new RuntimeException(ex);
            }
        }
        else if(vargs.length>4){
            String sourceTimezone = (String)vargs[3];
            sourceSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(sourceTimezone)));
            targetSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(sourceTimezone)));

            try {
                return targetSDF.format(sourceSDF.parse(source));
            }catch (ParseException ex){
                throw new RuntimeException(ex);
            }
        }
        return BaseUtils.fromLocalDateString(source, sourceFormat, targetFormat);
    }
    private static Object dateDiffInDays(Object[] vargs){
        String source = (String)vargs[0];
        String sourceFormat = (String)vargs[1];
        String target = (String)vargs[2];
        String targetFormat = (String)vargs[3];
        String sourceTimezone = (String)vargs[4];
        String targetTimezone = (String)vargs[5];
        SimpleDateFormat sourceSDF = new SimpleDateFormat(sourceFormat);
        SimpleDateFormat targetSDF = new SimpleDateFormat(targetFormat);
        sourceSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(sourceTimezone)));
        targetSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of(targetTimezone)));
        try {
            return ChronoUnit.DAYS.between(sourceSDF.parse(source).toInstant(), targetSDF.parse(target).toInstant());
        }catch (ParseException ex){
            throw new RuntimeException(ex);
        }
    }
    private static Object getZonedDateTime(Object[] vargs){
        String source = (String)vargs[0];
        String sourceFormat = (String)vargs[1];
        String targetFormat = (String)vargs[2];
        DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern(sourceFormat);
        DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern(targetFormat);
        ZonedDateTime datetime = ZonedDateTime.parse(source, inFormatter);
        return datetime.format(outFormatter);
    }
    private static Object concatAndReplaceStrings(Object[] vargs){
        StringBuilder finalString = new StringBuilder();
        for(int indx=0, arrayLen = vargs.length-1; indx<arrayLen; indx++){
            if("false" == (vargs[indx+1])){
                if(((String)(vargs[indx+2])).isEmpty()){
                    if(finalString.indexOf((String)vargs[indx])>=0){
                        indx+=2;
                        continue;
                    }
                    finalString.append(vargs[indx].toString());
                    finalString.append(",");
                    indx+=2;
                }else{
                    if(finalString.indexOf((String)vargs[indx+2])>=0){
                        indx+=2;
                        continue;
                    }
                    finalString.append(vargs[indx+2].toString());
                    finalString.append(",");
                    indx+=2;
                }
            }
        }
        if(finalString.toString().endsWith(",")){
            finalString.deleteCharAt(finalString.length()-1);
        }
        return finalString.toString();
    }
    /*private static Object getHypedEncryptedData(Object[] objects){
        String encryptedText = null;
        Properties props = new Properties();
        props.setProperty("DISABLE_CACHING", App.instance().getProperty("comm.encrypt.disable.caching"));
        props.setProperty("KEY_IDENTIFIER", App.instance().getProperty("comm.encrypt.key.identifier"));
        props.setProperty("ENVIRONMENT", App.instance().getProperty("comm.encrypt.env"));
        props.setProperty("PROVIDER", App.instance().getProperty("comm.encrypt.provider"));
        props.setProperty("KMS_APIGEE_SVC_ENDPOINT", App.instance().getProperty("comm.encrypt.apigee.endpoint"));
        props.setProperty("KMS_APIGEE_CLIENT_ID", App.instance().getProperty("comm.encrypt.apigee.client_id"));
        props.setProperty("KMS_APIGEE_CLIENT_SECRET", App.instance().getProperty("comm.encrypt.apigee.client.secret"));
        try {
            CryptoServiceNGProvider cspNG = new CryptoServiceNG(props, "KMS Properties");
            encryptedText = cspNG.encrypt(Objects.toString(objects[0]));
        }catch (Exception ex){
            logger.error("Exception Occured while executing encryption of attributes: "+ex);
        }
        return encryptedText;
    }*/

    public Object map(String mappingConfigPath, String recordLayoutId, String srcJsonString, Map<String, String> additionalVars){
        Document mappingConfigDoc = Utils.getResourceAsDocument(UnifyMappingUtil.class, mappingConfigPath);
        return map(mappingConfigDoc, recordLayoutId, srcJsonString, additionalVars);
    }

    public Object map(Document mappingConfigDoc, String recordLayoutId, String srcJsonString, Map<String, String> additionalVars){
        Document srcJsonDoc = new JDocument(srcJsonString);
        return map(mappingConfigDoc, recordLayoutId, srcJsonDoc, additionalVars);
    }

    public Object map(Document mappingConfigDoc, String recordLayoutId, Document srcJsonDoc, Map<String, String> additionalVars, boolean isValidate){
        if(isValidate){
            validateMappingConfig(mappingConfigDoc, recordLayoutId);
        }
        return map(mappingConfigDoc, recordLayoutId, srcJsonDoc, additionalVars);
    }

    public Object map(Document mappingConfigDoc, String recordLayoutId, Document srcJsonDoc, Map<String, String> additionalVars){
        try{
            init(mappingConfigDoc, srcJsonDoc, additionalVars);
            String rootJsonExpr= "$."+recordLayoutId+".fields[]";
            mappingConfigDoc = getSubDocument(rootJsonExpr, mappingConfigDoc);
            int fieldSize = mappingConfigDoc.getArraySize("$.fields[]");
            for(int indx=0; indx<fieldSize; indx++){
                String strIndx = String.valueOf(indx);
                evaluateByDataType(strIndx, mappingConfigDoc);
            }
            switch(recordFormat){
                case MappingUtilConstants.JSON:
                    return responseDocument.getJson();

                case MappingUtilConstants.DELIMITED:
                case MappingUtilConstants.FIXED_POS_FIXED_LEN:
                    return mappedResponse.toString();

                case MappingUtilConstants.JAVA_OBJ:
                    return container;
            }
        }catch (RuntimeException ex){
            logger.error("Runtime exception Occurred: ", ex);
            throw ex;
        }catch (Exception ex){
            logger.error("Exception Occurred: ", ex);
            throw new RuntimeException(ex);
        }
        return null;
    }

    public void validateMappingConfig(Document mappingConfigDoc, String recordLayoutId){
        String recordFormat;
        if(isValidPath("$.target_record_format", mappingConfigDoc)){
            recordFormat = mappingConfigDoc.getString("$.target_record_format");
        }else{
            recordFormat = MappingUtilConstants.JSON;
        }

        if(MappingUtilConstants.DELIMITED.equalsIgnoreCase(recordFormat)){
            if(!isValidPath("$.delimiter", mappingConfigDoc)){
                logger.error(MappingUtilConstants.DELIMITER_ERROR);
                throw new RuntimeException(MappingUtilConstants.DELIMITER_ERROR);
            }
        }

        if(MappingUtilConstants.JAVA_OBJ.equalsIgnoreCase(recordFormat)){
            if(!isValidPath("$.container_class", mappingConfigDoc)){
                logger.error(MappingUtilConstants.CONTAINER_CLASS_ERROR);
                throw new RuntimeException(MappingUtilConstants.CONTAINER_CLASS_ERROR);
            }
        }

        String rootJsonExpr = "$."+recordLayoutId+".fields[]";
        if(!isValidPath(rootJsonExpr, mappingConfigDoc)){
            String e = String.format(MappingUtilConstants.ROOT_JSON_EXPR_ERROR, rootJsonExpr, mappingConfigDoc.getPrettyPrintJson());
            logger.error(e);
            throw new RuntimeException(e);
        }else{
            mappingConfigDoc = getSubDocument(rootJsonExpr, mappingConfigDoc);
            int fieldSize = mappingConfigDoc.getArraySize("$.fields[]");
            for(int indx=0; indx<fieldSize; indx++){
                String fldIndx = String.valueOf(indx);
                String srcDatatype = "";
                if(mappingConfigDoc.pathExists("$.fields[%].array_root_node", fldIndx)){
                    srcDatatype = MappingUtilConstants.ARRAY;
                }
                if(mappingConfigDoc.pathExists("$.fields[%].json_block", fldIndx)){
                    srcDatatype = MappingUtilConstants.JSON_BLOCK;
                }
                String targetDatatype = mappingConfigDoc.getString("$.fields[%].target_data_type",fldIndx);
                if(StringUtils.isBlank(targetDatatype)){
                    targetDatatype = MappingUtilConstants.STRING;
                }
                validateFieldLevelAttributes(srcDatatype, targetDatatype, fldIndx, mappingConfigDoc, recordFormat);
            }
        }
    }

    private void init(Document mappingConfigDoc, Document srcJsonDoc, Map<String, String> additionalVars){
        srcJson = srcJsonDoc;
        additionalVariables = additionalVars;
        utilVars = new HashMap<>();
        responseDocument = new JDocument();
        mappedResponse = new StringBuilder();
        exprVsCurrIdxMap = new HashMap<>();
        defaultContainerPopulation = new DefaultContainerPopulation();

        if(mappingConfigDoc.pathExists("$.target_record_format")){
            recordFormat = mappingConfigDoc.getString("$.target_record_format");
        }else{
            recordFormat = MappingUtilConstants.JSON;
        }
        if(MappingUtilConstants.DELIMITED.equalsIgnoreCase(recordFormat)){
            delimiter = mappingConfigDoc.getString("$.delimiter");
        }else if(MappingUtilConstants.JAVA_OBJ.equalsIgnoreCase(recordFormat)){
            containerClass = mappingConfigDoc.getString("$.container_class");
        }
    }

    private void evaluateByDataType(String fieldIndex, Document mappingConfigDoc){
        source = mappingConfigDoc.getString("$.fields[%].source", fieldIndex);
        List<String> varArgsList = new ArrayList<>();
        List<String> targetVarArgsList;
        String[] targetVarArgs = {};

        if(StringUtils.isNotBlank(source) && source.contains("%")){
            if(isValidPath("$.fields[%].source_jdoc_indeces[]", mappingConfigDoc, fieldIndex)){
                int jdocIndexSize = mappingConfigDoc.getArraySize("$.fields[%].source_jdoc_indeces[]", fieldIndex);
                for(int indx=0; indx<jdocIndexSize; indx++){
                    String jdocIndx = String.valueOf(indx);
                    String jdocIndxVal = mappingConfigDoc.getArrayValueString("$.fields[%].source_jdoc_indeces[%]", fieldIndex, jdocIndx);
                    varArgsList.add(additionalVariables.get(jdocIndxVal));
                }
            }
        }
        String[] varArgs = varArgsList.toArray(new String[varArgsList.size()]);
        String targetDatatype = mappingConfigDoc.getString("$.fields[%].target_data_type",fieldIndex);
        if(targetDatatype==null){
            targetDatatype = MappingUtilConstants.STRING;
        }
        int length = 0;
        String targetPath = "";
        if(isValidPath("$.fields[%].length", mappingConfigDoc, fieldIndex)){
            length = Integer.valueOf(mappingConfigDoc.getString("$.fields[%].length", fieldIndex));
        }
        if(isValidPath("$.fields[%].target_path", mappingConfigDoc, fieldIndex)){
            targetPath = mappingConfigDoc.getString("$.fields[%].target_path", fieldIndex);
        }
        if(StringUtils.isNotBlank(source) && source.startsWith("$") && source.contains("__curr_index")){
            targetVarArgsList = fetchTargetVarArgsList(mappingConfigDoc, fieldIndex);
            targetVarArgs = targetVarArgsList.toArray(new String[targetVarArgsList.size()]);
        }

        String sourceType = "";
        if(isValidPath("$.fields[%].array_root_node", mappingConfigDoc, fieldIndex)){
            sourceType = MappingUtilConstants.ARRAY;
        }
        if(isValidPath("$.fields[%].json_block", mappingConfigDoc, fieldIndex)){
            sourceType = MappingUtilConstants.JSON_BLOCK;
        }
        switch(sourceType){
            case MappingUtilConstants.ARRAY:
                evaluateArrayFields(mappingConfigDoc, fieldIndex, targetVarArgs);
                break;

            case MappingUtilConstants.JSON_BLOCK:
                if((isExclusion = executeExclusion(mappingConfigDoc, fieldIndex))==false
                && (isInclusion = executeInclusion(mappingConfigDoc, fieldIndex))==true){
                    String rootJsonExpr= "$.fields[%].fields[]";
                    Document jsonBlockMappingConfigDoc = getSubDocument(rootJsonExpr, mappingConfigDoc, fieldIndex);
                    int blockFieldSize = jsonBlockMappingConfigDoc.getArraySize("$.fields[]");
                    for(int indx=0; indx<blockFieldSize; indx++){
                        String strIndx = String.valueOf(indx);
                        evaluateByDataType(strIndx, jsonBlockMappingConfigDoc);
                    }
                }else{
                    logger.warn(String.format(MappingUtilConstants.EXCLUDING_BLOCK+mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson()));
                    target = "";
                }
                break;
            default:
                if((isExclusion = executeExclusion(mappingConfigDoc, fieldIndex))==false
                        && (isInclusion = executeInclusion(mappingConfigDoc, fieldIndex))==true){
                    if(StringUtils.isNotBlank(source)){
                        if(source.startsWith("#")){
                            target = additionalVariables.get(source);
                        }else if(source.startsWith("$")){
                            String evaluatedSrc = evalNestedArray(source);
                            if(evaluatedSrc.contains("__curr_index")){
                                evaluatedSrc = evaluatedSrc.replace("__curr_index", utilVars.get("__curr_index"));
                            }
                            if(isValidPath(evaluatedSrc, srcJson, varArgs)){
                                try{
                                    target = Objects.toString(srcJson.getValue(evaluatedSrc, varArgs), "");
                                }catch (UnifyException uex){
                                    if(MappingUtilConstants.JDOC_ERROR_CODE_49.equalsIgnoreCase(uex.getErrorCode())){
                                        target = Objects.toString(srcJson.getArrayValueString(evaluatedSrc, varArgs), "");
                                    }else{
                                        target = "";
                                    }
                                }
                            }else{
                                target = "";
                            }
                        }else if(source.startsWith("@")){
                            String functionId = (source.contains(":"))?source.substring(0, source.indexOf(":")):source;
                            Function<Object[], Object> functionDef = funcDefMap.get(functionId);
                            if(functionDef==null){
                                String ex = String.format(MappingUtilConstants.FUNCTION_DEF_UNAVAILABLE, functionId);
                                logger.error(ex);
                                throw new RuntimeException(ex);
                            }
                            Object[] functionDetails = {};
                            if(source.contains(":")){
                                functionDetails = source.substring(source.indexOf(":")+1).split(Pattern.quote("|"));
                            }
                            for(int indx=0; indx<functionDetails.length; indx++){
                                String token = (String)functionDetails[indx];
                                if(StringUtils.isNotBlank(token) && token.startsWith("$")){
                                    String evaluatedSrc = evalNestedArray(token);
                                    if(evaluatedSrc.contains("__curr_index")){
                                        evaluatedSrc = evaluatedSrc.replace("__curr_index", utilVars.get("__curr_index"));
                                    }
                                    if(isValidPath(evaluatedSrc, srcJson, varArgs)){
                                        try{
                                            functionDetails[indx] = Objects.toString(srcJson.getValue(evaluatedSrc, varArgs), "");
                                        }catch (UnifyException uex){
                                            if(MappingUtilConstants.JDOC_ERROR_CODE_49.equalsIgnoreCase(uex.getErrorCode())){
                                                functionDetails[indx] = srcJson.getArrayValueString(evaluatedSrc, varArgs);
                                            }else{
                                                functionDetails[indx] = "";
                                            }
                                        }
                                    }else{
                                        functionDetails[indx] = "";
                                    }
                                }else if(token.startsWith("#")){
                                    functionDetails[indx] = additionalVariables.get(token);
                                }else if(token.startsWith("###")){
                                    functionDetails[indx] = "";
                                }
                            }
                            Object[] functionParams = Arrays.copyOf(functionDetails, functionDetails.length+1);
                            functionParams[functionDetails.length] = srcJson.getJson();
                            Object evaluatedSrc = functionDef.apply(functionParams);
                            target = Objects.toString(evaluatedSrc, "");
                        }else{
                            target=source;
                        }
                    }
                    target = appendOrPrepend(mappingConfigDoc,fieldIndex);
                }else{
                    logger.warn(String.format(MappingUtilConstants.EXCLUDING_PATH+mappingConfigDoc.getContent("$.fields[%]",true, true, fieldIndex).getPrettyPrintJson(), source));
                    target="";
                }
                if(length>0){
                    target = truncateOrPad(length, targetDatatype);
                }
                validateSource(mappingConfigDoc, fieldIndex);
                populateLeafNode(recordFormat, targetPath, targetDatatype, targetVarArgs);
        }
    }

    private String evalNestedArray(String token){
        String parentExpr = token.substring(0, token.lastIndexOf("."));
        String parentArrExpr = getParentArrExprIfNested(parentExpr);
        boolean isNestedArr = parentArrExpr != null;
        if(isNestedArr){
            exprVsCurrIdxMap.put(parentArrExpr, Integer.valueOf(utilVars.get("__curr_index")));
        }
        return reviseJsonExpr(token);
    }

    private String reviseJsonExpr(String jsonExpr){
        String revisedJsonExpr = jsonExpr;
        Set<Map.Entry<String, Integer>> entrySet = exprVsCurrIdxMap.entrySet();
        for (Map.Entry<String, Integer> entry: entrySet){
            revisedJsonExpr = revisedJsonExpr.replace(entry.getKey(), replace(entry.getKey()));
        }
        revisedJsonExpr = revisedJsonExpr.replaceAll("\\[\\]", "[__curr_index]");
        return revisedJsonExpr;
    }

    private List<String> fetchTargetVarArgsList(Document inputDoc, String fieldIndex){
        List<String> targetVarArgsList = new ArrayList<>();
        if(isValidPath("$.fields[%].target_jdoc_indeces[]", inputDoc, fieldIndex)){
            int jdocIndexSize = inputDoc.getArraySize("$.fields[%].target_jdoc_indeces[]", fieldIndex);
            for(int indx=0; indx<jdocIndexSize; indx++){
                String jdocIndx = String.valueOf(indx);
                String jdocIndxVal = inputDoc.getArrayValueString("$.fields[%].target_jdoc_indeces[%]", fieldIndex, jdocIndx);
                targetVarArgsList.add(additionalVariables.get(jdocIndxVal));
            }
        }
        return targetVarArgsList;
    }

    private void evaluateArrayFields(Document mappingConfigDoc, String fieldIndex, String[] targetVarArgs){
        String arrayRootNode = mappingConfigDoc.getString("$.fields[%].array_root_node", fieldIndex);
        String parentExpr = arrayRootNode.substring(0, arrayRootNode.lastIndexOf("."));
        String currArrExpr = arrayRootNode.substring(arrayRootNode.lastIndexOf(".")+1);
        String parentArrExpr = getParentArrExprIfNested(parentExpr);
        boolean isNestedArr = parentArrExpr != null;
        if(isNestedArr){
            exprVsCurrIdxMap.put(parentArrExpr, Integer.valueOf(utilVars.get("__curr_index")));
        }
        int arrayIterations = evaluateArrayIterations(mappingConfigDoc, fieldIndex, arrayRootNode, isNestedArr, currArrExpr);
        String rootJsonExpr = "$.fields[%].fields[]";
        Document arrayMappingConfigDoc = getSubDocument(rootJsonExpr, mappingConfigDoc, fieldIndex);
        for(int indx=0;indx<arrayIterations;indx++){
            utilVars.put("__curr_index", String.valueOf(indx));
            if((isExclusion = executeExclusion(mappingConfigDoc, fieldIndex))==false
                    && (isInclusion = executeInclusion(mappingConfigDoc, fieldIndex))==true){
                int arrayFieldSize = arrayMappingConfigDoc.getArraySize("$.fields[]");
                for(int arrIndx=0; arrIndx<arrayFieldSize; arrIndx++){
                    String strIndx = String.valueOf(arrIndx);
                    evaluateByDataType(strIndx, arrayMappingConfigDoc);
                }
            }else{
                int totalArrFieldLength = 0;
                int arrayFieldSize = arrayMappingConfigDoc.getArraySize("$.fields[]");
                if(arrayFieldSize > 0 ){
                    for(int arrIndx=0; arrIndx<arrayFieldSize; arrIndx++){
                        String strIndx = String.valueOf(arrIndx);
                        if (MappingUtilConstants.JSON.equalsIgnoreCase(recordFormat)){
                            fetchTargetJsonPath(arrayMappingConfigDoc, strIndx);
                        }else{
                            totalArrFieldLength = calculateTotalFieldLength(arrayMappingConfigDoc, strIndx, totalArrFieldLength);
                        }
                    }
                }
                if (!MappingUtilConstants.JSON.equalsIgnoreCase(recordFormat)){
                    target="";
                    if(totalArrFieldLength > 0){
                        target = truncateOrPad(totalArrFieldLength, MappingUtilConstants.STRING);
                    }
                    populateLeafNode(recordFormat, "", "", targetVarArgs);
                }
            }
        }
    }

    private String getParentArrExprIfNested(String parentExpr){
        String parentArrExpr = null;
        String[] tokens = parentExpr.split("\\,");
        for(String token: tokens){
            if(token.matches("[a-zA-Z][a-zA-Z0-9]*\\[\\]")){
                parentArrExpr = token;
            }
        }
        return parentArrExpr;
    }

    private String replace(String s){
        return s.replaceAll("\\[\\]", "[" + exprVsCurrIdxMap.get(s)+"]");
    }

    private void validateFieldLevelAttributes(String sourceDatatype, String targetDatatype, String fieldIndex, Document mappingConfigDoc, String recordFormat){
        switch(sourceDatatype){
            case MappingUtilConstants.ARRAY:
                String rootJsonExpr= "$.fields[%].fields[]";
                if(!isValidPath(rootJsonExpr, mappingConfigDoc, fieldIndex)){
                    String ex = String.format(MappingUtilConstants.ROOT_JSON_EXPR_ERROR, rootJsonExpr, mappingConfigDoc.getPrettyPrintJson());
                    logger.error(ex);
                    throw new RuntimeException(ex);
                }
                Document arrayMappingConfigDoc = getSubDocument(rootJsonExpr, mappingConfigDoc, fieldIndex);
                int arrayFieldSize = arrayMappingConfigDoc.getArraySize("$.fields[]");
                for(int arrIndx=0; arrIndx<arrayFieldSize; arrIndx++){
                    String strIndx = String.valueOf(arrIndx);
                    String datatype="";
                    if(arrayMappingConfigDoc.pathExists("$.fields[%].array_root_node",fieldIndex)){
                        datatype = MappingUtilConstants.ARRAY;
                    }
                    String targetType = arrayMappingConfigDoc.getString("$.fields[%].target_data_type",strIndx);
                    if(StringUtils.isBlank(targetType)){
                        targetType = MappingUtilConstants.STRING;
                    }
                    validateFieldLevelAttributes(datatype, targetType, strIndx, arrayMappingConfigDoc, recordFormat);
                }
                break;
            case MappingUtilConstants.JSON_BLOCK:
                String jsonBlockExpr = "$.fields[%].fields[]";
                Document jsonBlockMappingConfigDoc = getSubDocument(jsonBlockExpr, mappingConfigDoc, fieldIndex);
                int blockFieldSize = jsonBlockMappingConfigDoc.getArraySize("$.fields[]");
                for(int arrIndx=0; arrIndx<blockFieldSize; arrIndx++){
                    String strIndx = String.valueOf(arrIndx);
                    String datatype="";
                    if(jsonBlockMappingConfigDoc.pathExists("$.fields[%].array_root_node",fieldIndex)){
                        datatype = MappingUtilConstants.ARRAY;
                    }
                    String targetType = jsonBlockMappingConfigDoc.getString("$.fields[%].target_data_type",strIndx);
                    if(StringUtils.isBlank(targetType)){
                        targetType = MappingUtilConstants.STRING;
                    }
                    validateFieldLevelAttributes(datatype, targetType, strIndx, jsonBlockMappingConfigDoc, recordFormat);
                }
            default:
                if(!targetDatatypes.containsKey(targetDatatype.toUpperCase())){
                    String ex = String.format(MappingUtilConstants.INVALID_TARGET_DATA_TYPE_ERROR, targetDatatype, targetDatatypes.keySet().toString(),
                            mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson());
                    logger.error(ex);
                    throw new RuntimeException(ex);
                }
                if(!isValidPath("$.fields[%].length", mappingConfigDoc, fieldIndex)
                    && MappingUtilConstants.FIXED_POS_FIXED_LEN.equalsIgnoreCase(recordFormat)
                    && !MappingUtilConstants.ARRAY.equalsIgnoreCase(sourceDatatype)){
                    String ex = String.format(MappingUtilConstants.LENGTH_ERROR+" - %s", mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson());
                    logger.error(ex);
                    throw new RuntimeException(ex);
                }
                if(!isValidPath("$.fields[%].target_path", mappingConfigDoc, fieldIndex)
                        && MappingUtilConstants.JSON.equalsIgnoreCase(recordFormat)
                        && !MappingUtilConstants.ARRAY.equalsIgnoreCase(sourceDatatype)){
                    String ex = String.format(MappingUtilConstants.TARGET_PATH_ERROR+" - %s", mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson());
                    logger.error(ex);
                    throw new RuntimeException(ex);
                }
                if(!isValidPath("$.fields[%].source", mappingConfigDoc, fieldIndex)
                        && !MappingUtilConstants.ARRAY.equalsIgnoreCase(sourceDatatype)){
                    String ex = String.format(MappingUtilConstants.SOURCE_ATTRIBUTE_ERROR+" - %s", mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson());
                    logger.error(ex);
                    throw new RuntimeException(ex);
                }
        }
    }

    private void fetchTargetJsonPath(Document arrayMappingConfigDoc, String arrayFieldIndex){
        String srcDatatype="";
        if(isValidPath("$.fields[%].array_root_node", arrayMappingConfigDoc, arrayFieldIndex)){
            srcDatatype = MappingUtilConstants.ARRAY;
        }
        String targetDatatype = arrayMappingConfigDoc.getString("$.fields[%].target_data_type", arrayFieldIndex);
        if(StringUtils.isBlank(targetDatatype)){
            targetDatatype = MappingUtilConstants.STRING;
        }

        switch (srcDatatype){
            case MappingUtilConstants.ARRAY:
                String arrayRootNode = arrayMappingConfigDoc.getString("$.fields[%].array_root_node", arrayFieldIndex);
                String parentExpr = arrayRootNode.substring(0, arrayRootNode.lastIndexOf("."));
                String currArrExpr = arrayRootNode.substring(arrayRootNode.lastIndexOf(".")+1);
                String parentArrExpr = getParentArrExprIfNested(parentExpr);
                boolean isNestedArr = parentArrExpr != null;
                if(isNestedArr){
                    exprVsCurrIdxMap.put(parentArrExpr, Integer.valueOf(utilVars.get("__curr_index")));
                }
                int arrayIterations = evaluateArrayIterations(arrayMappingConfigDoc, arrayFieldIndex, arrayRootNode, isNestedArr, currArrExpr);
                String rootJsonExpr = "$.fields[%].fields[]";
                Document arraySubMappingConfigDoc = getSubDocument(rootJsonExpr, arrayMappingConfigDoc, arrayFieldIndex);
                for(int indx=0;indx<arrayIterations;indx++){
                    int arrayFldSz = arraySubMappingConfigDoc.getArraySize("$.fields[]");
                    for(int arrIndx=0;arrIndx<arrayFldSz;arrIndx++) {
                        utilVars.put("__curr_index", String.valueOf(arrIndx));
                        fetchTargetJsonPath(arraySubMappingConfigDoc, String.valueOf(arrIndx));
                    }
                }
                break;
            default:
                if(isValidPath("$.fields[%].target_path", arrayMappingConfigDoc, arrayFieldIndex)){
                    String targetPath = arrayMappingConfigDoc.getString("$.fields[%].target_path", arrayFieldIndex);
                    String[] targetVarArgs = {};
                    if(StringUtils.isNotBlank(targetPath) && targetPath.startsWith("$") && targetPath.contains("__curr_index")){
                        targetPath = targetPath.replace("__curr_index", utilVars.get("__curr_index"));
                    }
                    if(StringUtils.isNotBlank(targetPath) && targetPath.startsWith("$") && targetPath.contains("%")){
                        List<String> targetVarArgsList = fetchTargetVarArgsList(arrayMappingConfigDoc, arrayFieldIndex);
                        targetVarArgs = targetVarArgsList.toArray(new String[targetVarArgsList.size()]);
                    }
                    target="";
                    populateLeafNode(MappingUtilConstants.JSON, targetPath, targetDatatype, targetVarArgs);
                }
        }
    }

    private Integer calculateTotalFieldLength(Document arrayMappingConfigDoc, String arrayFieldIndex, int totalArrFldLength){
        if(isValidPath("$.fields[%].array_root_node", arrayMappingConfigDoc, arrayFieldIndex)){
            String arrayRootNode = arrayMappingConfigDoc.getString("$.fields[%].array_root_node", arrayFieldIndex);
            String parentExpr = arrayRootNode.substring(0, arrayRootNode.lastIndexOf("."));
            String currArrExpr = arrayRootNode.substring(arrayRootNode.lastIndexOf(".")+1);
            String parentArrExpr = getParentArrExprIfNested(parentExpr);
            boolean isNestedArr = parentArrExpr != null;
            if(isNestedArr){
                exprVsCurrIdxMap.put(parentArrExpr, Integer.valueOf(utilVars.get("__curr_index")));
            }
            int arrayIterations = evaluateArrayIterations(arrayMappingConfigDoc, arrayFieldIndex, arrayRootNode, isNestedArr, currArrExpr);
            String rootJsonExpr = "$.fields[%].fields[]";
            Document arraySubMappingConfigDoc = getSubDocument(rootJsonExpr, arrayMappingConfigDoc, arrayFieldIndex);
            for(int indx=0;indx<arrayIterations;indx++) {
                int arrayFldSz = arraySubMappingConfigDoc.getArraySize("$.fields[]");
                if(arrayFldSz>0){
                    for(int arrIndx=0;arrIndx<arrayFldSz;arrIndx++) {
                        String arraySubFeildindex = String.valueOf(arrIndx);
                        totalArrFldLength = calculateTotalFieldLength(arraySubMappingConfigDoc, arraySubFeildindex, totalArrFldLength);
                    }
                }
            }
        }else{
            totalArrFldLength+=Integer.valueOf(arrayMappingConfigDoc.getString("$.fields[%].length",arrayFieldIndex));
        }
        return totalArrFldLength;
    }

    private int evaluateArrayIterations(Document arrayMappingConfigDoc, String arrayFieldIndex, String arrayRootNode, boolean isNestedArr, String currArrExpr){
        int arrayIterations = 0;
        if(isValidPath("$.fields[%].array_iterations", arrayMappingConfigDoc, arrayFieldIndex)){
            arrayIterations = arrayMappingConfigDoc.getInteger("$.fields[%].array_iterations", arrayFieldIndex);
        }
        if(arrayIterations<1){
            if(isValidPath("$.fields[%].array_root_node", arrayMappingConfigDoc, arrayFieldIndex)){
                if(isNestedArr){
                    arrayRootNode = reviseJsonExpr(arrayRootNode);
                    arrayRootNode = arrayRootNode.replace(arrayRootNode.substring(arrayRootNode.lastIndexOf(".")+1), currArrExpr);
                }
                arrayIterations = srcJson.getArraySize(arrayRootNode);
            }
        }
        return arrayIterations;
    }

    private void validateSource(Document mappingConfigDoc, String fieldIndex){
        if(isValidPath("$.fields[%].validation_regex", mappingConfigDoc, fieldIndex) && !isExclusion && isInclusion){
            String validationRegex = mappingConfigDoc.getString("$.fields[%].validation_regex", fieldIndex);
            if(!target.matches(validationRegex)){
                String ex = String.format(MappingUtilConstants.INVALID_SOURCE_ERROR, target, validationRegex, mappingConfigDoc.getContent("$.fields[%]", true, true, fieldIndex).getPrettyPrintJson());
                logger.error(ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private void populateLeafNode(String recordFormat, String targetPath, String targetDatatype, String[] targetVarArgs){
        switch (recordFormat){
            case MappingUtilConstants.JSON:
                if(!target.isEmpty() || (isExclusion && targetPath.contains("[]")) || (!isInclusion && targetPath.contains("[]"))){
                    if(StringUtils.isNotBlank(targetPath) && targetPath.startsWith("$") && targetPath.contains("[]")){
                        targetPath = targetPath.replace("[]","["+utilVars.get("__curr_index")+"]");
                    }
                    switch(targetDatatype){
                        case MappingUtilConstants.INTEGER:
                            responseDocument.setInteger(targetPath, Integer.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.DECIMAL:
                            responseDocument.setBigDecimal(targetPath, BigDecimal.valueOf(Double.valueOf(target)), targetVarArgs);
                            break;
                        case MappingUtilConstants.LONG:
                            responseDocument.setLong(targetPath, Long.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.BOOLEAN:
                            responseDocument.setBoolean(targetPath, Boolean.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.STRING_ARRAY:
                            responseDocument.setArrayValueString(targetPath, target, targetVarArgs);
                            break;
                        case MappingUtilConstants.INTEGER_ARRAY:
                            responseDocument.setArrayValueInteger(targetPath, Integer.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.LONG_ARRAY:
                            responseDocument.setArrayValueLong(targetPath, Long.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.BOOLEAN_ARRAY:
                            responseDocument.setArrayValueBoolean(targetPath, Boolean.valueOf(target), targetVarArgs);
                            break;
                        case MappingUtilConstants.BIGDECIMEL_ARRAY:
                            responseDocument.setArrayValueBigDecimal(targetPath, BigDecimal.valueOf(Long.valueOf(target)), targetVarArgs);
                            break;
                        default:
                            responseDocument.setString(targetPath, target, targetVarArgs);
                    }
                }
                break;
            case MappingUtilConstants.DELIMITED:
                mappedResponse.append(target+delimiter); break;
            case MappingUtilConstants.FIXED_POS_FIXED_LEN:
                mappedResponse.append(target); break;
            case MappingUtilConstants.JAVA_OBJ:
                try{
                    container = defaultContainerPopulation.setProperty(container, containerClass, target, targetPath);
                }catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex){
                    logger.error("Exception occured while instantiating the container class : "+containerClass);
                    throw new RuntimeException(ex);
                }catch (Exception ex){
                    logger.error("Exception Occurred while executing DefaultContainerPopulation: ", ex);
                    throw new RuntimeException(ex);
                }
                break;
        }
    }

    private Document getSubDocument(String rootjsonExpr, Document mappingConfigDoc, String... args){
        Document fieldsDoc = new JDocument();
        fieldsDoc.setContent(mappingConfigDoc, rootjsonExpr, "$.fields[]", args);
        return fieldsDoc;
    }

    private String truncateOrPad(int length, String targetDatatype){
        StringBuilder output = new StringBuilder();
        IntStream.range(0, length).forEach(i ->output.append(" "));
        if(StringUtils.isBlank(target)){
            target="";
        }
        switch (targetDatatype){
            case MappingUtilConstants.STRING:
            case MappingUtilConstants.TIMESTAMP:
            case MappingUtilConstants.DATE:
            case MappingUtilConstants.BOOLEAN:
                return (target+output).substring(0, length);//Right Padding
            case MappingUtilConstants.INTEGER:
            case MappingUtilConstants.DECIMAL:
            case MappingUtilConstants.LONG:
                if (target.length() > length){
                    return target.substring(0, length);
                }else{
                    String refStr = output+source;
                    return refStr.substring(refStr.length()-length);//Left Padding
                }
        }
        return target;
    }

    private boolean executeExclusion(Document mappingConfigDoc, String fieldIndex){
        if(isValidPath("$.fields[%].exclude_if", mappingConfigDoc, fieldIndex)){
            String expression = mappingConfigDoc.getString("$.fields[%].exclude_if.expression", fieldIndex);
            if(StringUtils.isNotBlank(expression)){
                JexlContext params = new MapContext();
                int paramSize = mappingConfigDoc.getArraySize("$.fields[%].exclude_if.params[]");
                if(paramSize>0){
                    String paramKey = "", paramValue="";
                    for(int indx=0;indx<paramSize;indx++){
                        String paramIndx = String.valueOf(indx);
                        if(isValidPath("$.fields[%].exclude_if.params[%].p_key", mappingConfigDoc, fieldIndex, paramIndx)){
                            paramKey = mappingConfigDoc.getString("$.fields[%].exclude_if.params[%].p_key", fieldIndex, paramIndx);
                        }
                        if(isValidPath("$.fields[%].exclude_if.params[%].p_val", mappingConfigDoc, fieldIndex, paramIndx)){
                            paramValue = mappingConfigDoc.getString("$.fields[%].exclude_if.params[%].p_val", fieldIndex, paramIndx);
                        }
                        paramValue = evalNestedArray(paramValue);
                        if(StringUtils.isNotBlank(paramValue) && paramValue.contains("__curr_index")){
                            paramValue = paramValue.replace("__curr_index",utilVars.get("__curr_index"));
                        }
                        if(isValidPath(paramValue, srcJson)){
                            try{
                                paramValue = Objects.toString(srcJson.getValue(paramValue), null);
                            }catch (UnifyException uex){
                                if(MappingUtilConstants.JDOC_ERROR_CODE_49.equalsIgnoreCase(uex.getErrorCode())){
                                    paramValue = Objects.toString(srcJson.getArrayValueString(paramValue), "");
                                }else{
                                    paramValue = "";
                                }
                            }
                        }
                        if(StringUtils.isNotBlank(paramKey) && !paramKey.startsWith("$") && StringUtils.isNotBlank(paramValue) && !paramValue.startsWith("$")){
                            params.set(paramKey, paramValue);
                        }else{
                            return true;
                        }
                    }
                }
                JexlExpression expr =  jexl.createExpression(expression);
                return Boolean.valueOf(String.valueOf(expr.evaluate(params)));
            }
        }
        return false;
    }

    private boolean executeInclusion(Document mappingConfigDoc, String fieldIndex){
        if(isValidPath("$.fields[%].include_if", mappingConfigDoc, fieldIndex)){
            String expression = mappingConfigDoc.getString("$.fields[%].include_if.expression", fieldIndex);
            if(StringUtils.isNotBlank(expression)){
                JexlContext params = new MapContext();
                int paramSize = mappingConfigDoc.getArraySize("$.fields[%].include_if.params[]");
                if(paramSize>0){
                    String paramKey = "", paramValue="";
                    for(int indx=0;indx<paramSize;indx++){
                        String paramIndx = String.valueOf(indx);
                        if(isValidPath("$.fields[%].include_if.params[%].p_key", mappingConfigDoc, fieldIndex, paramIndx)){
                            paramKey = mappingConfigDoc.getString("$.fields[%].include_if.params[%].p_key", fieldIndex, paramIndx);
                        }
                        if(isValidPath("$.fields[%].include_if.params[%].p_val", mappingConfigDoc, fieldIndex, paramIndx)){
                            paramValue = mappingConfigDoc.getString("$.fields[%].include_if.params[%].p_val", fieldIndex, paramIndx);
                        }
                        paramValue = evalNestedArray(paramValue);

                        if(isValidPath(paramValue, srcJson)){
                            paramValue = Objects.toString(srcJson.getValue(paramValue), null);
                        }
                        if(StringUtils.isNotBlank(paramKey) && !paramKey.startsWith("$") && StringUtils.isNotBlank(paramValue) && !paramValue.startsWith("$")){
                            params.set(paramKey, paramValue);
                        }else{
                            return true;
                        }
                    }
                }
                JexlExpression expr =  jexl.createExpression(expression);
                return Boolean.valueOf(String.valueOf(expr.evaluate(params)));
            }
        }
        return false;
    }

    private String appendOrPrepend(Document mappingConfigDoc, String fieldIndex){
        if(target==null){
            target="";
        }
        if(isValidPath("$.fields[%].append", mappingConfigDoc, fieldIndex)){
            target = target+mappingConfigDoc.getString("$.fields[%].append", fieldIndex);
        }
        if(isValidPath("$.fields[%].prepend", mappingConfigDoc, fieldIndex)){
            target = mappingConfigDoc.getString("$.fields[%].prepend", fieldIndex)+target;
        }
        return target;
    }

    private boolean isValidPath(String path, Document srcJsonDoc, String... fldIndeces){
        if(null!=path && path.startsWith("$")){
            try{
                if(srcJsonDoc.pathExists(path,fldIndeces)){
                    return true;
                }
            }catch (Exception ex){
                return false;
            }
        }
        return false;
    }

    public static void registerFunction(String funcId, Function<Object[], Object> funcDef){
        funcDefMap.put(funcId, funcDef);
    }
    //Slide 22 done from bottom

}
