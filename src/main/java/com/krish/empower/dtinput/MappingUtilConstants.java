package com.krish.empower.dtinput;

public interface MappingUtilConstants {
    public static final String STRING = "STRING";
    public static final String ARRAY = "ARRAY";
    public static final String DECIMAL = "DOUBLE";
    public static final String INTEGER = "INTEGER";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String DATE = "DATE";
    public static final String DELIMITED = "DELIMITED";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String LONG = "LONG";
    public static final String STRING_ARRAY = "STRING_ARRAY";
    public static final String LONG_ARRAY = "LONG_ARRAY";
    public static final String INTEGER_ARRAY = "INTEGER_ARRAY";
    public static final String BIGDECIMEL_ARRAY = "BIGDECIMEL_ARRAY";
    public static final String BOOLEAN_ARRAY = "BOOLEAN_ARRAY";
    public static final String NOW = "NOW";
    public static final String JSON = "JSON";
    public static final String MAP = "MAP";
    public static final String JSON_BLOCK = "JSON_BLOCK";
    public static final String FIXED_POS_FIXED_LEN="FIXED_POS_FIXED_LEN";
    public static final String JAVA_OBJ = "JAVA_OBJ";

    //Errors
    public static final String JDOC_ERROR_CODE_49 = "jdoc_err_49";
    public static final String SOURCE_ATTRIBUTE_ERROR = "Source Attribute is required - %s";
    public static final String ROOT_JSON_EXPR_ERROR = "%s Attribute is required - %s";
    public static final String CONTAINER_CLASS_ERROR = "$.container_class Attribute is required when the target_record_format is JAVA_OBJ";
    public static final String DELIMITER_ERROR = "$.container_class Attribute is required when the target_record_format is delimiter";
    public static final String LENGTH_ERROR = "length Attribute is required when the target_record_format is FIXED_POS_FIXED_LEN";
    public static final String TARGET_PATH_ERROR = "target_path Attribute is required when the target_record_format is JSON";
    public static final String PATH_NOT_FOUND = "Following path was not found - %s";
    public static final String EXCLUDING_PATH="Excluding following path - %s";
    public static final String INVALID_SOURCE_ERROR = "Invalid source format - %s, Expected format is: %s - %s";
    public static final String INVALID_TARGET_DATA_TYPE_ERROR = "Invalid target datatype: %s, Accepted values are: %s - %s";
    public static final String FUNCTION_DEF_UNAVAILABLE = "Function definition for function Id: %s is not available.";
    public static final String SOURCE_ERROR = "Source is required";
    public static final String REGISTER_FN_ERROR = "The Function you are trying to register: %s exists. Please check if you are overriding the existing function or it is already registered.";
    public static final String EXCLUDING_BLOCK = "Excluding following path - %s";
}
