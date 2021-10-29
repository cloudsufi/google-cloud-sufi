package com.google.adapter.constants;


/**
 * This class is represent constants
 */
public class AdapterConstants {

  public static final String FILE_PATH_IDOC = System.getProperty("user.home");
  public static final int JCO_CONNECTION_ERROR = 102;
  public static final int JCO_RUNTIME_EXCEPTION = 122;
  public static final int JCO_INVALID_PARAMETER_ERROR_CODE = 101;
  public static final int JCO_ERROR_SYSTEM_FAILURE = 104;
  public static final int DESTINATION_EXCEPTION_120 = 120;
  public static final int ADAPTER_EXCEPTION_1000 = 1000;
  public static final int JCO_NOT_FOUND = 1001;
  public static final int JCO_IDOC_ERROR= 1002;
  public static final String RFC_LIST_1 = "RFC_FUNCTION_SEARCH_WITHGROUP";
  public static final String RFC_LIST_2 = "TABLE_ENTRIES_GET_VIA_RFC";
  public static final String FUNCNAME = "FUNCNAME";
  public static final String GROUPNAME = "GROUPNAME";
  public static final String STEXT = "STEXT";
  public static final String DEVCLASS = "DEVCLASS";
  public static final String APPL = "APPL";
  public static final String MISC = "MISC";
  public static final String TABNAME = "TABNAME";
  public static final String TAPLT = "TAPLT";
  public static final String LANGU = "LANGU";
  public static final String EN = "EN";
  public static final String SEL_TAB = "SEL_TAB";
  public static final String ZEILE = "ZEILE";
  public static final String SPRSL = "SPRSL = 'E'";
  public static final String LINETYPE = "lineType";
  public static final String SCHEMA = "schema";
  public static final String RFCFILTER = "RFCFilter";
  public static final String REQRESFILTER = "ReqResFilter";
  public static final String REQDETAIL = "REQUEST_DETAIL";
  public static final String RESDETAIL = "RESPONSE_DETAIL";
  public static final String PROPERTIES = "properties";
  public static final String I = "I";
  public static final String C = "C";
  public static final String E = "E";
  public static final String T = "T";
  public static final String X = "X";
  public static final String ZERO = "00";
  public static final String DDIF_FIELDINFO_GET = "DDIF_FIELDINFO_GET";
  public static final String ALL_TYPES = "ALL_TYPES";
  public static final String UCLEN = "UCLEN";
  public static final String LINES_DESCR = "LINES_DESCR";
  public static final String DFIES_TAB = "DFIES_TAB";
  public static final String TYPEKIND = "TYPEKIND";
  public static final String TTYP = "TTYP";
  public static final String FIELDS = "FIELDS";
  public static final String ROLLNAME = "ROLLNAME";
  public static final String TYPENAME = "TYPENAME";
  public static final String STRU = "STRU";
  public static final String FIELDNAME = "FIELDNAME";
  public static final String KEYFLAG = "KEYFLAG";
  public static final String STRING = "string";
  public static final String DATE_TIME = "date_time";
  public static final String NUMBER = "number";
  public static final String BASE64STRING = "base64string";
  public static final String INTEGER = "integer";
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";
  public static final String JCO_ELEMENT_PATTERN_1 = "[01]+$";
  public static final String CHILDREN = "children";
  public static final String COMPLEXTYPES = "complexTypes";

  public static final String SWO_QUERY_API_OBJTYPES = "SWO_QUERY_API_OBJTYPES";
  public static final String OBJECT_NAME = "OBJECT_NAME";
  public static final String OBJTYPES = "OBJTYPES";
  public static final String OBJTYPE = "OBJTYPE";
  public static final String SWO_QUERY_OBJTYPE_INFO = "SWO_QUERY_OBJTYPE_INFO";
  public static final String VERBS = "VERBS";
  public static final String EDITELEM = "EDITELEM";
  public static final String ABAPNAME = "ABAPNAME";


  public static final String RPY_BOR_TREE_INIT = "RPY_BOR_TREE_INIT";
  public static final String BOR_TREE = "BOR_TREE";
  public static final String FILTER_MISCELLANEOUS = "FILTER_MISCELLANEOUS";
  public static final String ID = "ID";
  public static final String PARENT = "PARENT";
  public static final String NAME = "NAME";
  public static final String EXT_NAME = "EXT_NAME";
  public static final String SHORT_TEXT = "SHORT_TEXT";
  public static final String SWO_QUERY_API_METHODS = "SWO_QUERY_API_METHODS";
  public static final String API_METHODS = "API_METHODS";
  public static final String METHOD = "METHOD";
  public static final String METHODNAME = "METHODNAME";
  public static final String FUNCTION = "FUNCTION";
  public static final String SHORTTEXT = "SHORTTEXT";
  public static final String EMPTY = "";

  public static final String BAPI = "BAPI";
  public static final String RFC = "RFC";
  public static final String AUTOCOMMIT = "autoCommit";
  public static final String TABENTRY = "TABENTRY";
  public static final String FALSE = "false";
  public static final String TRUE = "true";
  public static final String BAPI_TRANSACTION_COMMIT = "BAPI_TRANSACTION_COMMIT";
  public static final String WAIT = "WAIT";
  public static final String CHAR_1 = "CHAR_1";
  public static final String BAPI_TRANSACTION_ROLLBACK = "BAPI_TRANSACTION_ROLLBACK";

  public static final String STYPE_BYTE = "BYTE";
  public static final String STYPE_XSTRING = "XSTRING";
  public static final String STYPE_STRUCTURE = "STRUCTURE";
  public static final String STYPE_TABLE = "TABLE";
  public static final String UTF_8 = "UTF-8";
  public static final String EXTENSION_IN = "EXTENSION_IN";
  public static final String TID = "TID";
  public static final String TRFC = "TRFC";
  public static final String QRFC = "QRFC";
  public static final String OUT_RCVPRN = "OUT_RCVPRN";
  public static final String MESTYP = "MESTYP";
  /**
   * SNC_QOP field probable values
   */
  public static final String SNC_QOP = "12389";

  /**
   * Pattern to check IP Addresses
   */
  public static final String IPADDRESS_PATTERN =
      "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
  /**
   * Pattern to check Client
   */
  public static final String CLIENT_PATTERN = "[0-9]{3}";
  /**
   * Pattern to check Language
   */
  public static final String LANGUAGE_PATTERN = "[a-zA-Z]{2}";
  /**
   * Pattern to check Non Mandatory Parameters
   */
  public static final String NON_MANDAT_PARAM_PATTERN = "[0-9]{1,3}";


  //IDOC

  public static final String CHAR = "char";
  public static final String DATE = "date";
  public static final String DATETIME = "datetime";
  public static final String TIME = "TIME";



  /**
   * Private Constructor to hide implicit public one
   */
  private AdapterConstants() {
  }
}
