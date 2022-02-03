package com.krish.empower.decision_table;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.regex.Pattern;

public class DefaultContainerPopulation {
    public static final String SINGLE_VAR_EL_REGEX = "([a-zA-Z]+[\\d]*)+";
    public static final String LIST_EL_REGEX = "([a-zA-Z]+[\\d]*)+(\\[\\d+\\])";
    public static final String ASSOC_ARR_EL_REGEX = "([a-zA-Z]+[\\d]*)+(\\([a-zA-Z0-9_]+\\))";
    public static final String VAR_FMT1_EL_REGEX = "(([a-zA-Z]+[\\d]*)+)(\\.(([a-zA-Z]+[\\d]*)+))*";
    public static final String VAR_FMT2_EL_REGEX = "((([a-zA-Z]+[\\d]*)+)|(([a-zA-Z]+[\\d]*)+(\\[\\d+\\]))|(([a-zA-Z]+[\\d]*)+(\\([a-zA-Z0-9]+\\))))"
        +"(\\.(((([a-zA-Z]+[\\d]*)+)|(([a-zA-Z]+[\\d]*)+(\\[\\d+\\]))|(([a-zA-Z]+[\\d]*)+(\\([a-zA-Z0-9]+\\))))))*";
    public static final String DONE = "DONE";
    public static final String CONTINUE = "CONTINUE";

    protected Logger logger = LoggerFactory.getLogger(DefaultContainerPopulation.class);

    private static final String THIS_CLASS = "DefaultContainerPopulationImpl.";

    public static final Pattern singleVartPattern = Pattern.compile(SINGLE_VAR_EL_REGEX);
    public static final Pattern ListPattern = Pattern.compile(LIST_EL_REGEX);
    public static final Pattern mapPattern = Pattern.compile(ASSOC_ARR_EL_REGEX);
    public static final Pattern varfmt1Pattern = Pattern.compile(VAR_FMT1_EL_REGEX);
    public static final Pattern varfmt2Pattern = Pattern.compile(VAR_FMT2_EL_REGEX);

    static{
        // register the custom Converter implementtions
        ConvertUtils.register(new CustomXMLGregorianCalendarConverter(), XMLGregorianCalendar.class);
        ConvertUtils.register(new CustomDateConverter(),Date.class);
        ConvertUtils.register(new CustomBigIntegerConverter(), BigInteger.class);
        ConvertUtils.register(new CustomBigDecimalConverter(), BigDecimal.class);
    }

    public Object setProperty(Object container, String ContainerClass, String value, String fieldExpr)throws Exception {
        if (container == null) {
            Class<?> containerClazz = Class.forName(ContainerClass);
            container = containerClazz.newInstance();
        }
            // set the required property
            this.setProperty(container, value, fieldExpr);

            return container;
    }

    public void setProperty(Object container, String value, String fieldExpr)throws Exception {
        String thisMethod = THIS_CLASS + "setProperty: ";
        long currTimeMillis = System.currentTimeMillis();
        Object subContainer = null;
        Object tempContainer = container;
        String leftOverExpr = fieldExpr;
        int dotIdx = -1;
        while((dotIdx = leftOverExpr.indexOf("."))>0) {
            String exprToken= leftOverExpr.substring(0, dotIdx);
            if(exprToken.matches(SINGLE_VAR_EL_REGEX)) {
                if(PropertyUtils.getProperty(container, exprToken)==null) {
                    currTimeMillis=System.currentTimeMillis();
                    subContainer=PropertyUtils.getPropertyType(tempContainer,exprToken).getComponentType();
                    PropertyUtils.setProperty(container, exprToken, subContainer);
                    System.out.println("Nested EL (in while) set time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+exprToken);
                }
            }else if(exprToken.matches(LIST_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                subContainer = this.setListProperty(tempContainer, value, exprToken);
                System.out.println("List EL (in while) set time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+exprToken);
            }else if(exprToken.matches(ASSOC_ARR_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                subContainer = this.setAssocArrayProperty(tempContainer, value, exprToken);
                System.out.println("Nested Assoc Array EL (in while) set time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+exprToken);
            }else{
                throw new Exception(thisMethod + " Invalid field expression!");
            }
            if(dotIdx < leftOverExpr.length()-1){
                leftOverExpr=leftOverExpr.substring(dotIdx+1, leftOverExpr.length());
                tempContainer = subContainer;
            }
        }
        if(dotIdx<0){
            if(leftOverExpr.matches(SINGLE_VAR_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                Class<?> datatype = PropertyUtils.getPropertyType(tempContainer, leftOverExpr);
                Object convertedFiledValue = ConvertUtils.convert(value, datatype);
                System.out.println("Set Nested EL time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
            }else if(fieldExpr.matches(LIST_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                this.setListProperty(tempContainer, value, leftOverExpr);
                System.out.println("List EL set time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
            }else if(fieldExpr.matches(ASSOC_ARR_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                this.setAssocArrayProperty(tempContainer, value, leftOverExpr);
                System.out.println("Nested Assoc Array EL set time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
            }else{
                throw new Exception(thisMethod + " Invalid field expression!");
            }
        }
    }

    public void setProperty2(Object container, String value, String fieldExpr) throws Exception {
        String thisMethod = THIS_CLASS + "setProperty: ";
        //if(varfmt2Pattern.matcher(fieldExpr).matches() ==false){
        if (fieldExpr.matches(VAR_FMT2_EL_REGEX) == false) {
            PropertyUtils.setProperty(container, fieldExpr, value);
            return;
        }
        //if((varfmt2Pattern.matcher(fieldExpr).matches()) {
        if (fieldExpr.matches(VAR_FMT2_EL_REGEX)) {
            String parentExpr = "";
            String[] tokens = fieldExpr.split("\\.");
            for(String exprToken :tokens){
                Object subContainer = parentExpr == "" ? container : PropertyUtils.getProperty(container, parentExpr);
                if (exprToken.matches(ASSOC_ARR_EL_REGEX)) {
                    //logger.debug(thisMethod = "Inside ASSOC_ARR_EL_REGEX..... " = exprToken);
                    //currTimeMillis = System.currentTimeMillis();
                    this.setAssocArrayProperty(subContainer, value, exprToken);
                    //System.out.println("nested AssocArray EL (in while) set time token = " + (System.currentTimeMillis() - currTimeMillis) + " for " + exprToken);
                }
                else if(exprToken.matches(LIST_EL_REGEX)) {
                    this.setListProperty(subContainer, value, exprToken);
                }
                parentExpr += parentExpr==""? exprToken : "."+exprToken;
            }
            PropertyUtils.setProperty(container, fieldExpr, value);
            return;
        }
        throw new Exception(thisMethod + " Invalid field expression => "+fieldExpr);
    }

    public String setPropertyWithRecursion(Object container, String value, String fieldExpr) throws Exception{
        String thisMethod = THIS_CLASS + "setPropertyWithRecursion: ";
        /** recursion break condition block - start **/
        long currTimeMillis = System.currentTimeMillis();
        logger.debug(thisMethod +" Starting recursion for  ... "+fieldExpr);
        int dotIdx = fieldExpr.indexOf(".");
        if(dotIdx<0){
            if(fieldExpr.matches(SINGLE_VAR_EL_REGEX)){
                Class<?> datatype = PropertyUtils.getPropertyType(container, fieldExpr);
                Object convertedFiledValue = ConvertUtils.convert(value, datatype);
                PropertyUtils.setProperty(container, fieldExpr, convertedFiledValue);
                System.out.println("Set time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+fieldExpr);
            }else if(fieldExpr.matches(LIST_EL_REGEX)){
                this.setListProperty(container, value, fieldExpr);
            }else if(fieldExpr.matches(ASSOC_ARR_EL_REGEX)){
                this.setAssocArrayProperty(container, value, fieldExpr);
            }else{
                throw new Exception(thisMethod + " Invalid field expression!");
            }
            return DONE;
        }
        /** recursion break condition block - end **/
        String exprToken = fieldExpr.substring(0, dotIdx);
        String leftOverExpr = null;
        if(dotIdx < fieldExpr.length()-1){
            leftOverExpr = fieldExpr.substring(dotIdx+1, fieldExpr.length());
        }
        String retValue = null;
        Object subContainer = null;
        while(leftOverExpr.length()>0){
            if(exprToken.matches(SINGLE_VAR_EL_REGEX)){
                logger.debug(thisMethod +" Inside NESTED_EL_REGEX ");
                if(PropertyUtils.getProperty(container, exprToken)==null){
                    currTimeMillis=System.currentTimeMillis();
                    subContainer=PropertyUtils.getPropertyType(container,exprToken).newInstance();
                    PropertyUtils.setProperty(container, exprToken, subContainer);
                    System.out.println("Nested EL set time taken = "+(System.currentTimeMillis()-currTimeMillis)+" for "+exprToken);
                }
            }else if(exprToken.matches(LIST_EL_REGEX)){
                currTimeMillis=System.currentTimeMillis();
                subContainer = this.setListProperty(container, value, exprToken);
                System.out.println("List EL set time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+exprToken);
            }else if(exprToken.matches(ASSOC_ARR_EL_REGEX)){
                subContainer = this.setAssocArrayProperty(container, value, exprToken);
            }else{
                throw new Exception(thisMethod + " Invalid field expression!");
            }

            //recursive call
            retValue = this.setPropertyWithRecursion(subContainer, value, leftOverExpr);
            if(DONE.equals(retValue)){
                System.out.println("While (on DONE) time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
                return DONE;
            }
            dotIdx = leftOverExpr.indexOf(".");
            if(dotIdx<0){
                System.out.println("While (on Break) time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
                break;
            }
        }
        System.out.println("While time token = "+(System.currentTimeMillis()-currTimeMillis)+" for "+leftOverExpr);
        return CONTINUE;
    }

   protected Object setListProperty(Object container, String value, String exprToken) throws Exception{
       String thisMethod = THIS_CLASS + "setListProperty: ";
       Object subContainer = null;
       String listVarName = exprToken.substring(0, exprToken.indexOf("["));
       int listIdx = Integer.parseInt(exprToken.substring(exprToken.indexOf("[")+1, exprToken.indexOf("]")));
       if((subContainer=PropertyUtils.getProperty(container, listVarName))==null){
           subContainer=new ArrayList<>();
           try{
               PropertyUtils.setProperty(container, listVarName, subContainer);
           }catch (IllegalArgumentException ex){
               throw new Exception(thisMethod + " Trying to set list for '"+listVarName+ "', found "+PropertyUtils.getPropertyType(container, listVarName));
           }
       }
       if(listIdx> ((List)subContainer).size()){
           throw new Exception(" List Index is out of bound: "+exprToken);
       }

       Field listField = container.getClass().getDeclaredField(listVarName);
       ParameterizedType listType = null;
       try{
           listType = (ParameterizedType) listField.getGenericType();
       }catch (IllegalArgumentException ex){
           throw new Exception(thisMethod + " List definition without generics is not supported! ");
       }
       Class listTypeClass = null;
       try{
           listTypeClass = (Class) listType.getActualTypeArguments()[0];
       }catch (ArrayIndexOutOfBoundsException ex){
           throw new Exception(thisMethod + " Invalid list definition for : "+listVarName);
       }
       Object listContainerObj = null;
       if(this.isSimpleType(listTypeClass)){
           ((List)subContainer).add(listIdx, ConvertUtils.convert(value, listTypeClass));
       }else{
           if(((List)subContainer).size() > listIdx){
               listContainerObj = ((List)subContainer).get(listIdx);
           }else{
               listContainerObj = listTypeClass.newInstance();
               ((List)subContainer).add(listIdx, listContainerObj);
           }
       }
       return listContainerObj;
   }

   protected Object setAssocArrayProperty(Object container, String value, String exprToken) throws Exception{
       String thisMethod = THIS_CLASS + "setAssocArrayProperty: ";
       Object subContainer = null;
       String assocArrVarName = exprToken.substring(0, exprToken.indexOf("("));
       String assocArrKey = exprToken.substring(exprToken.indexOf("(")+1, exprToken.indexOf(")"));
       if((subContainer=PropertyUtils.getProperty(container, assocArrVarName))==null){
           subContainer=new HashMap<>();
           try{
               PropertyUtils.setProperty(container, assocArrVarName, subContainer);
           }catch (IllegalArgumentException ex){
               throw new Exception(thisMethod + " Trying to set Map type for '"+assocArrVarName+ "', found "+PropertyUtils.getPropertyType(container, assocArrVarName));
           }
       }
       Field assocArrField = container.getClass().getDeclaredField(assocArrVarName);
       ParameterizedType assocArrEntryType = null;
       try{
           assocArrEntryType = (ParameterizedType) assocArrField.getGenericType();
       }catch (ClassCastException ex){
           throw new Exception(thisMethod + " Assoc Array or Map definition without generics is not supported "+assocArrVarName);
       }
       Class assocArrEntryValueTypeClass = null;
       Object assocArrEntryValueObj=  null;
       try{
           assocArrEntryValueTypeClass = (Class) assocArrEntryType.getActualTypeArguments()[0];
       }catch (ArrayIndexOutOfBoundsException ex){
           throw new Exception(thisMethod + " Invalid Map definition for : "+assocArrVarName);
       }
       if(this.isSimpleType(assocArrEntryValueTypeClass)){
           ((Map)subContainer).put(assocArrKey, ConvertUtils.convert(value, assocArrEntryValueTypeClass));
       }else{
           if((assocArrEntryValueObj = ((Map)subContainer).get(assocArrKey))==null){
               assocArrEntryValueObj = assocArrEntryValueTypeClass.getComponentType();
               ((Map)subContainer).put(assocArrKey, assocArrEntryValueObj);
           }else{
               //do nothing
           }
       }
       return assocArrEntryValueObj;
   }

   protected boolean isSimpleType(Class typeClass){
        return String.class.equals(typeClass)
                ||Integer.class.equals(typeClass)
                ||Float.class.equals(typeClass);
   }

   public static class CustomDateConverter implements Converter{
        public Object convert(Class dataType, Object dateValue){
            boolean isUnparsable = true;
            Date parsedDate = null;
            java.sql.Date convertedDate = null;
            String dateFormat = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd,yyyy/MM/dd HH:mm:ss,yyyyMMdd,MM-dd-yyyy,MM/dd/yyyy,yyddd";
            String[] dateFormats = dateFormat.split(",");
            for(String format: dateFormats){
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    parsedDate = sdf.parse((String)dateValue);
                    if(parsedDate!=null){
                        convertedDate = new java.sql.Date(parsedDate.getTime());
                    }
                    isUnparsable=false;
                    break;
                }catch (ParseException ex){
                    continue;
                }
            }
            if(isUnparsable){
                System.out.println("CustomDateConverter.convert: *** Couldn't parse date String: "+dateValue);
            }
            return convertedDate;
        }
   }

    public static class CustomBigDecimalConverter implements Converter{
        public Object convert(Class dataType, Object value){
            return (value != null && value.toString().trim().length()>0)?new BigDecimal(value.toString()):null;
        }
    }
    public static class CustomBigIntegerConverter implements Converter{
        public Object convert(Class dataType, Object value){
            return (value != null && value.toString().trim().length()>0)?new BigInteger(value.toString()):null;
        }
    }

    public static class CustomXMLGregorianCalendarConverter implements Converter{
        public Object convert(Class dataType, Object value){
            Object convertedFieldValue = null;
            Date date = (Date) ConvertUtils.convert((String)value, Date.class);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            try{
                convertedFieldValue = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            }catch (DatatypeConfigurationException ex){
                ex.printStackTrace();
            }
            return convertedFieldValue;
        }
    }
}



