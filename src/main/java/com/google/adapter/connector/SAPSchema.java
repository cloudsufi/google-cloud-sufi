package com.google.adapter.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.*;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ExceptionUtils;
import com.google.adapter.util.JsonUtil;
import com.google.adapter.util.ReadErrorPropertyFile;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.jco.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SAPSchema {

  private JCoFunctionTemplate fTemplate = null;
  private JCoFunction functionMetaData = null;
  private ErrorCapture errorCapture;
  private SAPConnector sapConn;
  private SAPDataConverter converter;

  /**
   * public constructor to initialize default values
   *
   * @param sapConn      SAPConnector
   * @param errorCapture ErrorCapture
   */
  public SAPSchema(SAPConnector sapConn, ErrorCapture errorCapture) {
    this.errorCapture = errorCapture;
    this.sapConn = sapConn;
    converter = new SAPDataConverter(sapConn,errorCapture);
  }


  /**
   * This method will return RFC schema
   *
   * @param rfcMethod String
   * @return JsonNode
   * @throws JCoException In case of exception
   */
  public JsonNode getRFCSchema(String rfcMethod) throws JCoException {
    JsonNode node;
    RFCMethod rfc = new RFCMethod(rfcMethod, "", AdapterConstants.SCHEMA);
    buildParams(rfc);
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    FilterProvider filters =
            new SimpleFilterProvider().addFilter(AdapterConstants.RFCFILTER, new JsonPropertyFilter())
                    .addFilter(AdapterConstants.REQRESFILTER, new RFCRequestResponseFilter(true));
    JsonNode requestJson = mapper.setFilterProvider(filters).valueToTree(rfc);
    filters = new SimpleFilterProvider()
            .addFilter(AdapterConstants.RFCFILTER, new JsonPropertyFilter())
            .addFilter(AdapterConstants.REQRESFILTER, new RFCRequestResponseFilter(false));
    JsonNode responseJson = mapper.setFilterProvider(filters).valueToTree(rfc);
    node = mapper.createObjectNode();
    ((ObjectNode) node)
            .set(AdapterConstants.REQDETAIL, requestJson.get(AdapterConstants.PROPERTIES));
    ((ObjectNode) node)
            .set(AdapterConstants.RESDETAIL, responseJson.get(AdapterConstants.PROPERTIES));
    JsonUtil.removeEmptyNodes(node);
    return node;
  }

  /**
   * This method will build RFC Parameter for RFC Schema
   *
   * @param method RFCMethod
   * @throws JCoException In case of any error from JCo
   */
  private void buildParams(RFCMethod method) throws JCoException {
    // STEP-1 get RFC param list
    fTemplate = sapConn.getRepository().getFunctionTemplate(method.getFunction());
    if (fTemplate == null) {
      throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
              .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
    }
    Map<String, RFCParameter> map = new HashMap<>();
    createImportJcoParam(fTemplate.getImportParameterList(), AdapterConstants.I, method, map);
    createChangingJcoParam(fTemplate.getChangingParameterList(), AdapterConstants.C, method, map);
    createComplexJcoParam(fTemplate.getExportParameterList(), AdapterConstants.E, method, map);
    createTableJcoParam(fTemplate.getTableParameterList(), AdapterConstants.T, method, map);
    checkSetOnly(method);
    addComplexType(method);
    if (method instanceof BAPIMethod) {
      JCoFunction function = sapConn.getRepository().getFunction("SWO_QUERY_PARAMETERS");
      if (function == null) {
        throw new RuntimeException("SWO_QUERY_PARAMETERS not found in SAP.");
      }
      JCoParameterList in = function.getImportParameterList();
      in.setValue("VERB", ((BAPIMethod) method).getMethodName());
      in.setValue("OBJTYPE", ((BAPIMethod) method).getObjectType());
      in.setValue("TEXT", " ");
      in.setValue("VERBTYPE", "M");
      function.execute(sapConn.getDestination());
      JCoTable parms = function.getTableParameterList().getTable("INFO");
      for (int i = 0; i < parms.getNumRows(); i++) {
        parms.setRow(i);
        String pname = parms.getString("FUNCPNAME");
        String etype = parms.getString("ELEMTYPE");
        RFCParameter param = map.get(pname);
        if (param != null) {
          param.setElemType(etype);
        }
      }
    }
  }

  /**
   * Add value in list complex types
   *
   * @param method RFCMethod
   */
  private void addComplexType(RFCMethod method) {
    if (method.getChildren() != null) {
      for (RFCParameter p : method.getChildren()) {
        listComplexTypes(p, method);
      }
    }
  }


  /**
   * Check the value of setOnly in case of tRFC or qRFC
   * @param method RFCMethod Object
   */
  public void checkSetOnly(RFCMethod method){
    if (((fTemplate.getExportParameterList() == null)
            || (fTemplate.getExportParameterList().getFieldCount() < 1))||
            ((fTemplate.getChangingParameterList() == null)
                    || (fTemplate.getChangingParameterList().getFieldCount() < 1))){
      method.setInOnly(true);
    }
  }

  /**
   * Create JCO Import parameter
   *
   * @param list   JCoListMetaData
   * @param group  String
   * @param method RFCMethod
   * @param map    Map
   * @throws JCoException JCoException
   */
  private void createImportJcoParam(JCoListMetaData list, String group, RFCMethod method,
                                    Map<String, RFCParameter> map) throws JCoException {

    createComplexJcoParam(list, group, method, map);
  }

  /**
   * Create JCO Complex Parameter
   *
   * @param list   JCoListMetaData
   * @param group  String
   * @param method RFCMethod
   * @param map    Map
   * @throws JCoException JCoException
   */
  private void createComplexJcoParam(JCoListMetaData list, String group, RFCMethod method,
                                     Map<String, RFCParameter> map) throws JCoException {
    int size = (list != null) ? list.getFieldCount() : 0;
    for (int i = 0; i < size; i++) {
      RFCParameter param;
      if (list.isStructure(i) || list.isTable(i)) {
        Map<String, String> fieldsMap = getNestedMetaData(list.getRecordTypeName(i));
        param = new RFCParameter(list, i, group, RFCParameter.BAPI_PARAMETER, fieldsMap,
                list.getRecordTypeName(i));
      } else {
        param = new RFCParameter(list, i, group, RFCParameter.BAPI_PARAMETER);
      }
      map.put(list.getName(i), param);
      method.addChild(param);
    }
  }

  /**
   * Get value of Nested Meta Data partially to reduce the complexity of one method
   *
   * @param fieldsMap Map
   */
  private void partialNestedMetaData(Map<String, String> fieldsMap) {
    JCoTable resultTable = functionMetaData.getExportParameterList()
            .getTable(AdapterConstants.LINES_DESCR);
    JCoTable structure = functionMetaData.getTableParameterList()
            .getTable(AdapterConstants.DFIES_TAB);
    Map<String, String> rollToTabMap = new HashMap<>();
    for (int j = 0; j < resultTable.getNumRows(); j++) {
      resultTable.setRow(j);
      if (resultTable.getString(AdapterConstants.TYPEKIND)
              .equalsIgnoreCase(AdapterConstants.TTYP)) {
        JCoTable fieldsTable = resultTable.getTable(AdapterConstants.FIELDS);
        for (int k = 0; k < fieldsTable.getNumRows(); k++) {
          fieldsTable.setRow(k);
          rollToTabMap.put(fieldsTable.getString(AdapterConstants.ROLLNAME),
                  fieldsTable.getString(AdapterConstants.TABNAME));
        }
      }
    }
    runStru(resultTable,rollToTabMap,fieldsMap);
    for (int j = 0; j < structure.getNumRows(); j++) {
      structure.setRow(j);
      fieldsMap.put(structure.getString(AdapterConstants.TABNAME) + "|" +
                      structure.getString(AdapterConstants.FIELDNAME),
              structure.getString(AdapterConstants.KEYFLAG));
    }
  }


  public void runStru(JCoTable resultTable, Map<String, String> rollToTabMap, Map<String, String> fieldsMap){
    for (int j = 0; j < resultTable.getNumRows(); j++) {
      resultTable.setRow(j);
      if (resultTable.getString(AdapterConstants.TYPEKIND).equalsIgnoreCase(AdapterConstants.STRU)) {
        String rollName = resultTable.getString(AdapterConstants.TYPENAME);
        JCoTable fieldsTable = resultTable.getTable(AdapterConstants.FIELDS);
        for (int k = 0; k < fieldsTable.getNumRows(); k++) {
          fieldsTable.setRow(k);
          String tabName =
                  rollToTabMap.get(rollName) != null ? rollToTabMap.get(rollName) : "";
          String fieldName = fieldsTable.getString(AdapterConstants.FIELDNAME);
          String keyFlag = fieldsTable.getString(AdapterConstants.KEYFLAG);
          fieldsMap.put(tabName + "|" + fieldName, keyFlag);
        }
      }
    }
  }

  /**
   * Actual method to get Nested metadata
   *
   * @param recordType String
   * @return Map
   * @throws JCoException JCoException
   */
  private Map<String, String> getNestedMetaData(String recordType) throws JCoException {
    Map<String, String> fieldsMap = new HashMap<>();
    functionMetaData = sapConn.getRepository().getFunction(AdapterConstants.DDIF_FIELDINFO_GET);
    if (functionMetaData == null) {
      throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
              .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
    }
    try {
      functionMetaData.getImportParameterList().setValue(AdapterConstants.TABNAME, recordType);
      functionMetaData.getImportParameterList()
              .setValue(AdapterConstants.ALL_TYPES, AdapterConstants.X);
      functionMetaData.getImportParameterList()
              .setValue(AdapterConstants.LANGU, AdapterConstants.EN);
      functionMetaData.getImportParameterList()
              .setValue(AdapterConstants.UCLEN, AdapterConstants.ZERO);
      functionMetaData.execute(sapConn.getDestination());
    } catch (JCoException e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
    partialNestedMetaData(fieldsMap);
    return fieldsMap;
  }

  /**
   * Create changing JCo Parameter
   *
   * @param list   JCoListMetaData
   * @param group  String
   * @param method RFCMethod
   * @param map    Map
   * @throws JCoException JCoException
   */
  private void createChangingJcoParam(JCoListMetaData list, String group, RFCMethod method,
                                      Map<String, RFCParameter> map) throws JCoException {
    createComplexJcoParam(list, group, method, map);
  }

  /**
   * Create JCo Table Parameter
   *
   * @param list   JCoListMetaData
   * @param group  String
   * @param method RFCMethod
   * @param map    Map
   * @throws JCoException JCoException
   */
  private void createTableJcoParam(JCoListMetaData list,
                                   String group, RFCMethod method,
                                   Map<String, RFCParameter> map) throws JCoException {
    createComplexJcoParam(list, group, method, map);
  }

  /**
   * List all the complex type from metadata
   *
   * @param e      JcoElement
   * @param method RFCMethod
   */
  private void listComplexTypes(JcoElement e, RFCMethod method) {
    if (e.isStructure() || e.isTable()) {
      method.addType(e.getRecordType(), e);
      for (JcoElement e2 : e.getFields()) {
        listComplexTypes(e2, method);
      }
    }
  }

  /**
   * Return the schema for provided BAPI
   * @param bapiName like BoName.methodName
   * @return Schema in the form of Json node
   * @throws JCoException JCoException
   */
  public JsonNode getBAPISchema(String bapiName) throws JCoException {
    String rfcName = converter.getRfcForBapi(bapiName);
    return getRFCSchema(rfcName);
  }

  //IDOC Schema

  public JsonNode getIDOCSchema(String idocType, String idocName, String release) throws JCoException {
    return getIdocSchema(idocType, idocName, release);
  }

  public JsonNode getIdocSchema(String idocType, String idocName, String revision) throws JCoException {
    String extension = null;
    if (idocName.contains(".")) {
      String[] ss = idocName.split("\\.", 2);
      idocName = ss[0];
      extension = ss[1];
    }
    return getIdocSchema(idocType, idocName, revision, extension);
  }

  /**
   * Get Schema Structure for a selected IDOC
   * @param idocType Idoc type
   * @param idocName Idoc Name
   * @param revision Revision number
   * @param extension Extension
   * @return Schema in the form of Json Node
   * @throws JCoException JCoException
   */
  public JsonNode getIdocSchema(String idocType, String idocName, String revision, String extension) throws JCoException {
    IDocType selected = null;

    try {
      List<IDocType> idocTypesList = sapConn.getIDocTypes();
      for (IDocType type : idocTypesList) {
        String idocT = type.getDevclass();
        String idocN = type.getBaseName();
        if (idocT.equals(idocType) && idocName.equals(idocN)) {
          selected = type;
        }
      }
      if(selected!=null){
        buildIDOCSegments(selected, revision);
      } else{
        new ExceptionUtils();
        throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
                .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
      }
      ObjectMapper mapper = new ObjectMapper();
      List<Object> idoc = new ArrayList<>();
      idoc.add(IDocSegmentDC40.getEDI_DC40());
      idoc.addAll(selected.getSegments());
      return mapper.valueToTree(idoc);
    } catch (JCoException e) {
      if(selected!=null){
        buildIDOCSegments(selected, revision);
      } else{
        throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
                .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
      }
      ObjectMapper mapper = new ObjectMapper();
      List<Object> idoc = new ArrayList<>();
      idoc.add(IDocSegmentDC40.getEDI_DC40());
      for (IDocSegment segment : selected.getSegments()) {
        idoc.add(segment);
      }
      return mapper.valueToTree(idoc);
    }
  }

  /**
   * This method will update the IDOC type like Matmas01 with it's segments metadata
   * @param iDocType Idoc type
   * @param release Release number
   * @throws JCoException JCoException
   */
  public void buildIDOCSegments(IDocType iDocType, String release) throws JCoException {
    IDocRepository repo = JCoIDoc.getIDocRepository(sapConn.getDestination());
    iDocType.buildSegments(repo, release, "");

  }

}
