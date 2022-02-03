package com.krish.empower.jdocs;

public class CONSTS_JDOCS {

  /**
   *
   */
  public static final String NEW_LINE = System.getProperty("line.separator");

  // these are the fields that appear in the format string i.e. the whole string for a leaf node
  public class FORMAT_FIELDS {

    public static final String KEY = "jdocs_arr_pk";
    public static final String TYPE = "type";
    public static final String REGEX = "regex";
    public static final String FORMAT = "format"; // used only for date
    public static final String NULL_ALLOWED = "null_allowed";

  }

  public enum API {
    DELETE_PATH,
    GET_ARRAY_SIZE,
    GET_ARRAY_INDEX,
    GET,
    SET,
    GET_ARRAY_VALUE,
    SET_ARRAY_VALUE,
    PATH_EXISTS,
    CONTENT
  }

}
