package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.adapter.configuration.SAPCacheHelper;
import com.google.adapter.configuration.lucene.LuceneIndexBuilder;
import com.google.adapter.configuration.lucene.LuceneKey;
import com.google.adapter.configuration.util.CacheProperties;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;

import com.google.adapter.model.*;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.JsonUtil;
import com.google.adapter.util.ReadErrorPropertyFile;
import com.google.common.base.Strings;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.JCoContext;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAPConnector class is the wrapper class to SAP JCo and all the call to SAP JCo will begin from
 * this class only.
 *
 * @author dhiraj.kumar
 */
public class SAPConnector {

  private static final Logger logger = LoggerFactory.getLogger(SAPConnector.class);
  private final SAPProperties sapProps;
  private JCoDestination jcoClient = null;
  private ErrorCapture errorCapture;
  private JCoRepository jcoRepo = null;
  private JCoTable table = null;
  private List<String> entriesTable = null;
  private boolean isCached = false;
  private Executor executor = Executors.newFixedThreadPool(2);
  private SAPCacheHelper cache;
  private BAPIObject mBORootObject = null;
  private SAPDataConverter converter;
  private LuceneIndexBuilder luceneIndexBuilder;
  private IDocRepository idocRepo = null;
  private String destinationName;

  /**
   * Constructor for SAPConnector
   *
   * @param errorCapture Initialize errorCapture
   * @param properties Get parameters from Properties file
   */
  public SAPConnector(ErrorCapture errorCapture, Properties properties) {
    this.sapProps = SAPProperties.getDefault(properties);
    this.errorCapture = errorCapture;
    this.cache = SAPCacheHelper.getInstance();
    this.destinationName = this.sapProps.getDestinationName();
    converter = new SAPDataConverter(this, errorCapture);
    initialize();
  }

  /**
   * Initialize the SAP Component Creates destination provide object and initializes JCo Destination
   * manager.
   */
  public void initialize() {
    try {
      DestinationDataProviderImpl destProvider = DestinationDataProviderImpl.getInstance();
      destProvider.setProperties(sapProps);
      jcoClient = JCoDestinationManager.getDestination(sapProps.getDestinationName());
      this.luceneIndexBuilder = LuceneIndexBuilder.getInstance(new StandardAnalyzer());
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Ping function tests the SAP Connectivity
   *
   * @return true if connection is positive
   * @throws JCoException if connection is false
   */
  public boolean ping() throws JCoException {
    jcoClient.ping();
    return true;
  }

  /**
   * This tests the SAP Connectivity and return SSO
   *
   * @return true if connection is positive
   * @throws JCoException if connection is false
   */
  public String generateSSO() throws JCoException {
    jcoClient.ping();
    return jcoClient.getAttributes().getSSOTicket();
  }

  /** Call Standard RFC to get List of RFC */
  private CompletableFuture<JCoTable> callListRfc() {
    CompletableFuture<JCoTable> rfcList =
            CompletableFuture.supplyAsync(
                    () -> {
                      try {
                        return invokeFunction(AdapterConstants.RFC_LIST_1, 0);
                      } catch (JCoException e) {
                        throw SystemException.throwException(e.getMessage(), e);
                      }
                    },
                    executor);
    return rfcList;
  }

  /** Call Standard RFC to get List of RFC */
  private CompletableFuture<JCoTable> callComponentRfc() {
    CompletableFuture<JCoTable> rfcRelationship =
            CompletableFuture.supplyAsync(
                    () -> {
                      try {
                        return invokeTABLE_ENTRIES_GET_VIA_RFC(AdapterConstants.RFC_LIST_2);
                      } catch (JCoException e) {
                        throw SystemException.throwException(e.getMessage(), e);
                      }
                    },
                    executor);
    return rfcRelationship;
  }

  /**
   * This method will check if caching is already done or not
   *
   * @return true if already cached
   */
  public boolean checkCached() {
    return cache
            .getRfcListCache()
            .containsKey(this.prepareCacheKey(CacheProperties.RFC_LIST_SUFFIX))
            && cache
            .getRfcComponentCache()
            .containsKey(this.prepareCacheKey(CacheProperties.RFC_COMP_LIST_SUFFIX));
  }


  /**
   * Get list of RFC using two standard RFCs
   * @param filterText Filter text
   * @param refreshRequired Is caching required or not
   * @return Json node containing list of RFC methods
   * @throws ExecutionException ExecutionException
   * @throws InterruptedException InterruptedException
   * @throws IOException IOException
   * @throws ParseException ParseException
   */
  public JsonNode getRFCList(String filterText, boolean refreshRequired)
          throws ExecutionException, InterruptedException, IOException, ParseException {
    ObjectMapper mapper = new ObjectMapper();
    List<RFCMethod> miscRfcList = new ArrayList<>();
    Map<String, List<RFCMethod>> applicationToRfcMap = new HashMap<>();

    List<RFCMethod> rfcMethodList =
            (refreshRequired || !checkCached()) ? refreshCache() : loadFromCache(filterText);

    entriesTable.stream()
            .forEach(
                    entry -> {
                      final List<RFCMethod> appRfcList = new ArrayList<>();
                      rfcMethodList.stream()
                              .forEach(
                                      rfcMethod -> {
                                        if (rfcMethod.getApplication() == Character.valueOf(entry.charAt(1))) {
                                          appRfcList.add(rfcMethod);
                                        } else if (Objects.isNull(rfcMethod.getApplication()) || rfcMethod.getApplication().charValue() == 32) {
                                          miscRfcList.add(rfcMethod);
                                        }
                                      });

                      if (!appRfcList.isEmpty()) {
                        applicationToRfcMap.put(entry.substring(2), appRfcList);
                      }
                    });

    applicationToRfcMap.put(
            AdapterConstants.MISC, miscRfcList.stream().distinct().collect(Collectors.toList()));

    return mapper.convertValue(applicationToRfcMap, JsonNode.class);
  }

  /**
   * This method will clear cache and load new data inside it for RFC_COMP_LIST_SUFFIX and
   * RFC_LIST_SUFFIX
   * @return List of RFCMethod
   * @throws IOException IOException
   * @throws ExecutionException ExecutionException
   * @throws InterruptedException InterruptedException
   */
  public List<RFCMethod> refreshCache()
          throws IOException, ExecutionException, InterruptedException {
    table = callListRfc().get();
    entriesTable = entriesList(callComponentRfc().get());
    cache.clearAllCache();

    logger.info(
            "initialization of caching for Rfc Component List {}",
            prepareCacheKey(CacheProperties.RFC_COMP_LIST_SUFFIX));
    cache
            .getRfcComponentCache()
            .put(prepareCacheKey(CacheProperties.RFC_COMP_LIST_SUFFIX), entriesTable);

    List<RFCMethod> rfcMethodList = prepareRfcMethodList(table);

    this.luceneIndexBuilder.addContentToIndex(rfcMethodList, LuceneKey.RFC, false);
    cache.getRfcListCache().put(prepareCacheKey(CacheProperties.RFC_LIST_SUFFIX), rfcMethodList);
    return rfcMethodList;
  }

  private List<String> entriesList(JCoTable entriesTable) {
    List<String> stringList = new ArrayList<>();
    do{
      stringList.add(entriesTable.getString(0));
    }while (entriesTable.nextRow());

    return stringList;
  }

  /**
   * This method will load data from cache initialized early based on filterText provided
   * @param filterText Filter text
   * @return List of RFCMethod
   * @throws IOException IOException
   * @throws ParseException ParseException
   */
  public List<RFCMethod> loadFromCache(String filterText) throws IOException, ParseException {

    logger.info(
            "fetching list from caching for Rfc Component List {}",
            prepareCacheKey(CacheProperties.RFC_COMP_LIST_SUFFIX));
    entriesTable =
            Optional.ofNullable(
                    cache
                            .getRfcComponentCache()
                            .get(prepareCacheKey(CacheProperties.RFC_COMP_LIST_SUFFIX)))
                    .get();

    List<RFCMethod> rfcMethodList = new ArrayList<>();
    if (Strings.isNullOrEmpty(filterText)) {
      logger.info(
              "fetching list from caching for Rfc List {}",
              prepareCacheKey(CacheProperties.RFC_LIST_SUFFIX));
      rfcMethodList =
              Optional.ofNullable(
                      cache.getRfcListCache().get(prepareCacheKey(CacheProperties.RFC_LIST_SUFFIX)))
                      .get();
    } else {
      // Initialize ret from indexing based on filter text
      rfcMethodList = searchIndexedRFC(filterText);
    }

    return rfcMethodList;
  }

  /**
   * This method will prepare RFCMethod instance from data available in JCoTable This JCoTable
   * contains data for RFC's
   *
   * @param table
   * @return {@link RFCMethod}
   */
  private List<RFCMethod> prepareRfcMethodList(JCoTable table) {
    List<RFCMethod> rfcMethodList = new ArrayList<>();
    do{

      String sText = table.getString(AdapterConstants.STEXT);
      if (sText.contains("\\")) {
        sText = sText.replaceAll("\\\\", "");
      }

      RFCMethod rfcMethod =
              new RFCMethod(
                      table.getString(AdapterConstants.FUNCNAME),
                      table.getString(AdapterConstants.GROUPNAME),
                      sText,
                      table.getString(AdapterConstants.DEVCLASS),
                      prepareCacheKey(CacheProperties.RFC_IDX_LIST_SUFFIX));

      rfcMethod.setApplication(table.getChar(AdapterConstants.APPL));
      rfcMethodList.add(rfcMethod);
    }while (table.nextRow());
    return rfcMethodList;
  }

  /**
   * This method look out for RFCMethod details in Lucene indexed documents
   *
   * @param filterText
   * @return {@link List<RFCMethod>}
   * @throws IOException
   */
  private List<RFCMethod> searchIndexedRFC(String filterText) throws IOException, ParseException {
    List<RFCMethod> rfcMethodList = this.luceneIndexBuilder.search(LuceneKey.RFC.any(filterText),
            CacheProperties.RFC_INDX_SIZE, RFCMethod.class);
    return rfcMethodList;
  }

  /**
   * Get list of RFC as return value in JCo Table
   *
   * @param functionName which we get after invoking one RFC
   * @param resultTableName Name of table which comes out in result
   * @return JCo Table
   * @throws JCoException when there's error in fetching RFC list
   */
  public JCoTable invokeFunction(String functionName, Object resultTableName) throws JCoException {
    JCoFunction function = getRepository().getFunction(functionName);
    if (function == null) {
      throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
              .getSecondaryMessage(), new RuntimeException(AdapterConstants.JCO_NOT_FOUND + ""));
    }
    try {
      function.execute(getDestination());
    } catch (JCoException e) {
      logger.error(function + " " + e.getMessage());
      throw SystemException.throwException(e.getMessage(), e);
    }
    if (resultTableName instanceof String) {
      return function.getTableParameterList().getTable((String) resultTableName);
    } else {
      return function.getTableParameterList().getTable((Integer) resultTableName);
    }
  }

  /**
   * Fetches relation between other RFC
   *
   * @param rfc RFC Name
   * @return JCo table which defines relation between other RFCs
   * @throws JCoException when there's error in fetching RFC list
   */
  public JCoTable invokeTABLE_ENTRIES_GET_VIA_RFC(String rfc) throws JCoException {
    JCoFunction function = getRepository().getFunction(rfc);
    if (function == null) {
      throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
              .getSecondaryMessage(), new NullPointerException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
    }
    function.getImportParameterList().setValue(AdapterConstants.TABNAME, AdapterConstants.TAPLT);
    function.getImportParameterList().setValue(AdapterConstants.LANGU, AdapterConstants.EN);
    JCoTable selTable = function.getTableParameterList().getTable(AdapterConstants.SEL_TAB);
    selTable.appendRow();
    selTable.setValue(AdapterConstants.ZEILE, AdapterConstants.SPRSL);
    try {
      function.execute(getDestination());
    } catch (JCoException e) {
      logger.error(AdapterConstants.RFC_LIST_2 + " " + e.getMessage());
      throw SystemException.throwException(e.getMessage(), e);
    }
    return function.getTableParameterList().getTable(AdapterConstants.TABENTRY);
  }

  /**
   * Return JCo Destination
   *
   * @return JCoDestination
   */
  public JCoDestination getDestination() {
    return jcoClient;
  }

  /**
   * Return repository based on connection parameter
   *
   * @return JCo Repository
   * @throws JCoException when there's error in fetching Repository
   */
  public JCoRepository getRepository() throws JCoException {
    if (jcoRepo == null) {
      jcoRepo = jcoClient.getRepository();
    }
    return jcoRepo;
  }


  /**
   * Return keyForRfcList based on keySuffix parameter
   * @param keySuffix Suffix key
   * @return keyForRfcList
   */
  public String prepareCacheKey(String keySuffix) {
    return this.destinationName + keySuffix;
  }

  /**
   * Build BAPI Object Model
   *
   * @return BAPIObject
   * @throws JCoException JCoException
   */
  public BAPIObject buildBOModel() throws JCoException {
    if (mBORootObject != null) {
      return mBORootObject;
    }
    BAPIObject rootObject = null;
    Map<String, BAPIObject> id2ObjectMap = new HashMap<>();
    Map<String, List<BAPIObject>> objType2ObjectMap = new HashMap<>();
    JCoTable borTree =
            invokeFunction(AdapterConstants.RPY_BOR_TREE_INIT, AdapterConstants.BOR_TREE);
    if (borTree == null) borTree = invokeFunctionWithParam();
    if (borTree != null) {
      for (int i = 0; i < borTree.getNumRows(); i++) {
        borTree.setRow(i);
        BAPIObject parentObject = id2ObjectMap.get(borTree.getString(AdapterConstants.PARENT));
        BAPIObject object =
                new BAPIObject(
                        borTree.getString(AdapterConstants.ID),
                        borTree.getString(AdapterConstants.NAME),
                        borTree.getString(AdapterConstants.EXT_NAME),
                        borTree.getString(AdapterConstants.SHORT_TEXT));
        id2ObjectMap.put(borTree.getString(AdapterConstants.ID), object);
        List<BAPIObject> objects =
                objType2ObjectMap.computeIfAbsent(
                        borTree.getString(AdapterConstants.NAME), k -> new ArrayList<>(1));
        objects.add(object);
        if (parentObject != null) {
          parentObject.addChild(object);
        } else {
          rootObject = object;
        }
      }
    }

    JCoTable apiMethods =
            invokeFunction(AdapterConstants.SWO_QUERY_API_METHODS, AdapterConstants.API_METHODS);
    for (int i = 0; i < apiMethods.getNumRows(); i++) {
      apiMethods.setRow(i);
      BAPIMethod bapiMethod =
              new BAPIMethod(
                      apiMethods.getString(AdapterConstants.OBJTYPE),
                      apiMethods.getString(AdapterConstants.METHOD),
                      apiMethods.getString(AdapterConstants.METHODNAME),
                      apiMethods.getString(AdapterConstants.FUNCTION),
                      apiMethods.getString(AdapterConstants.SHORTTEXT));
      List<BAPIObject> objects =
              objType2ObjectMap.get(apiMethods.getString(AdapterConstants.OBJTYPE));
      if (objects == null) {
        logger.info(
                "WARNING: No object found for method "
                        + bapiMethod
                        + " (objtype:"
                        + apiMethods.getString(AdapterConstants.OBJTYPE)
                        + ")");
      } else {
        for (BAPIObject object : objects) {
          object.addMethod(bapiMethod);
          bapiMethod.setObjectName(object.getExtName());
        }
      }
    }

    for (List<BAPIObject> objects : objType2ObjectMap.values()) {
      for (BAPIObject object : objects) {
        if (!object.childOrSelfHasMethods()) {
          object.getParent().removeChild(object);
        }
      }
    }
    mBORootObject = rootObject;
    return rootObject;
  }

  /**
   * SAP call to fetch list using standard RFC(BAPI)
   *
   * @return JCo Table
   * @throws JCoException JCoException
   */
  private JCoTable invokeFunctionWithParam() throws JCoException {
    JCoFunction function = getRepository().getFunction(AdapterConstants.RPY_BOR_TREE_INIT);
    if (function == null) {
      throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
              .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
    }
    function
            .getImportParameterList()
            .setValue(AdapterConstants.FILTER_MISCELLANEOUS, AdapterConstants.EMPTY);
    function.execute(getDestination());
    if (AdapterConstants.BOR_TREE instanceof String) {
      return function.getTableParameterList().getTable(AdapterConstants.BOR_TREE);
    } else {
      return function.getTableParameterList().getTable(Integer.parseInt(AdapterConstants.BOR_TREE));
    }
  }


  /**
   * This method helps in executing BAPI and return BAPI response
   * @param payload Payload name
   * @param operationProps Operation properties map
   * @param tID transactional id
   * @param queueName sap queue name
   * @return Json node
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  public JsonNode executeBAPI(String payload, Map<String, String> operationProps,String tID, String queueName)
          throws JCoException, IOException {
    JsonNode resNode = null;
    String bapiName = operationProps.get(AdapterConstants.BAPI);
    if (bapiName != null) {
      String rfcName = converter.getRfcForBapi(bapiName);
      operationProps.put(AdapterConstants.RFC, rfcName);
      resNode = executeRFC(payload, operationProps,tID,queueName);
    }
    return resNode;
  }

  /**
   * This method will return list of BAPI's available.
   *
   * @return JsonNode
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  public JsonNode getBAPIList() throws JCoException, IOException {
    JsonNode resNode;
    ObjectMapper mapper = new ObjectMapper();
    FilterProvider filters =
            new SimpleFilterProvider().addFilter(AdapterConstants.RFCFILTER, new JsonPropertyFilter());
    resNode = mapper.readTree(mapper.writer(filters).writeValueAsString(buildBOModel()));
    return resNode;
  }


  /**
   * This method execute RFC payload and return response from SAP
   * @param payload Payload name
   * @param operationProps Operation properties map
   * @param tID transactional id
   * @param queueName sap queue name
   * @return Json node
   * @throws IOException IOException
   * @throws JCoException JCoException
   */
  public JsonNode executeRFC(String payload, Map<String, String> operationProps,String tID, String queueName)
          throws IOException, JCoException {
    String autoCommit =
            operationProps.getOrDefault(AdapterConstants.AUTOCOMMIT, AdapterConstants.FALSE);
    // starting commit transaction
    if (autoCommit.equals(AdapterConstants.TRUE)) {
      return executeStatefulRFC(payload, operationProps,tID,queueName);
    } else {
      return executeRFM(payload, operationProps, tID, queueName);
    }
  }

  /**
   * This method will execute RFC
   *
   * @param payload Payload name
   * @param operationProps Operation properties map
   * @return JsonNode
   */
  private JsonNode executeRFM(String payload, Map<String, String> operationProps, String tID, String queueName)
          throws JCoException, IOException {
    JsonNode obj = null;
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(payload);
    String rfcType = fetchRFCType(operationProps);
    String rfcName = operationProps.get(rfcType);
    String tId = getTID(tID);
//    String rfcName = operationProps.get(AdapterConstants.RFC);
    if (rfcName != null) {
      JCoFunction function = getRepository().getFunction(rfcName);
      converter.setJCoRequestFromJson(function, root);
      if (rfcType.equalsIgnoreCase(AdapterConstants.TRFC)) {
        function.execute(getDestination(), tId);
        obj = mapper.readTree("{ \"" + AdapterConstants.TID + "\" :" + "\"" + tId + "\"" + "} ");
        return obj;
      } else if (rfcType.equalsIgnoreCase(AdapterConstants.QRFC)) {
        function.execute(getDestination(), tId, queueName);
        obj = mapper.readTree("{ \"" + AdapterConstants.TID + "\" :" + "\"" + tId + "\"" + "} ");
        return obj;
      } else {
        function.execute(getDestination());
      }
      obj = mapper.createObjectNode();
      converter.getJsonReplyFromJCo(function, rfcName, mapper, obj);
    }
    return obj;
  }

  public String getTID(String tID) throws JCoException {
    if (tID == null || tID.isEmpty()) {
      return getDestination().createTID();
    } else {
      return tID;
    }
  }

  /**
   * Get the type of RFC
   * @param operationProps Name and type of RFC
   * @return Type of RFC
   */
  public String fetchRFCType(Map<String, String> operationProps) {
    String key = null;
    Set<String> set = operationProps.keySet();
    for (String string : set) {
      key = string;
    }
    return key;
  }

  /**
   * This method execute RFC payload and return response from SAP
   * @param payload Payload name
   * @param operationProps Operation properties map
   * @return JsonNode Json node
   * @throws IOException IOException
   * @throws JCoException JCoException
   */
  private JsonNode executeStatefulRFC(String payload, Map<String, String> operationProps, String tID, String queueName)
          throws IOException, JCoException {
    JsonNode obj;
    // starting session
    JCoContext.begin(getDestination());
    obj = executeRFM(payload, operationProps, tID, queueName);
    JCoFunction functionTrans =
            getRepository().getFunction(AdapterConstants.BAPI_TRANSACTION_COMMIT);
    functionTrans.getImportParameterList().setValue(AdapterConstants.WAIT, AdapterConstants.CHAR_1);
    functionTrans.execute(getDestination());

    JCoFunction functionRollBack =
            getRepository().getFunction(AdapterConstants.BAPI_TRANSACTION_ROLLBACK);
    functionRollBack.execute(getDestination());
    return obj;
  }

  /**
   * This method return list of IDOCs
   * @return Json Node
   * @throws JCoException JCoException
   */
  public JsonNode getIDOCList() throws JCoException {
    return getInboundIdocList();
  }

  /**
   * This method will return all the Idoc types configured with a program ID configuration
   * @return Json node
   * @throws JCoException JCoException
   */
  private JsonNode getInboundIdocList() throws JCoException {
    JsonNode idocList;
    String receiverPartner = sapProps.getIdocprofiles().get(AdapterConstants.OUT_RCVPRN);
    List<String> fields = new ArrayList<>();
    fields.add(AdapterConstants.MESTYP);

    List<String> idocTypes = getConfiguredIdocList("EDP21", receiverPartner, fields, "SNDPRN");

    if (idocTypes.isEmpty()) {
      throw new AdapterException(
              "No configured IDOC type found for provided partner profile "
                      + receiverPartner
                      + ".  "
                      + "Please configure inbound IDocs in SAP, before sending");
    }
    List<IDocType> typeList = getInboundIDocfromType(idocTypes);
    idocList = JsonUtil.getJson(typeList);
    logger.info("Inbound IDoc list: " + idocList);
    return idocList;
  }

  /**
   * Get configured IDoc list
   * @param tableName Idoc table name
   * @param partnerName Partner name
   * @param fields List of fields
   * @param partnerField Name of partner field
   * @return List of configured IDOC
   * @throws JCoException JCoException
   */
  private List<String> getConfiguredIdocList(
          String tableName, String partnerName, List<String> fields, String partnerField)
          throws JCoException {

    JCoFunction function = getRepository().getFunction("RFC_READ_TABLE");
    function.getImportParameterList().setValue("QUERY_TABLE", tableName);
    function.getImportParameterList().setValue("DELIMITER", ";");
    JCoTable optionsTable = function.getTableParameterList().getTable("OPTIONS");
    optionsTable.appendRow();
    optionsTable.setValue("TEXT", partnerField + " EQ '" + partnerName + "'");
    JCoTable fieldsTable = function.getTableParameterList().getTable("FIELDS");

    for (String fieldName : fields) {
      fieldsTable.appendRow();
      fieldsTable.setValue("FIELDNAME", fieldName);
    }

    function.execute(getDestination());
    JCoTable dataTable = function.getTableParameterList().getTable("DATA");
    List<String> idocTypes = new ArrayList<>();
    for (int i = 0; i < dataTable.getNumRows(); i++) {
      dataTable.setRow(i);
      String idoc = dataTable.getString("WA");
      idoc = idoc.replaceAll(" ", "");
      idocTypes.add(idoc);
    }
    return idocTypes;
  }

  /**
   * Get idoc types list based on provided idoc types name
   * @param idocTypes List of Idoc type name
   * @return List of Idoc Types
   * @throws JCoException JCoException
   */
  public List<IDocType> getInboundIDocfromType(List<String> idocTypes) throws JCoException {
    List<IDocType> selectedTypes = new ArrayList<>();
    List<String> includedList = new ArrayList<>();
    List<IDocType> allTypes = getIDocTypes();
    for (IDocType type : allTypes) {
      String toCompare;
      if (type.getExt() != null)
        toCompare = type.getDevclass() + ";" + type.getBaseName() + ";" + type.getExt();
      else toCompare = type.getDevclass() + ";" + type.getBaseName() + ";";

      if (idocTypes.contains(type.getDevclass()) && !includedList.contains(toCompare)) {
        selectedTypes.add(type);
        includedList.add(toCompare);
        idocTypes.remove(toCompare);
      }
    }
    return selectedTypes;
  }

  /**
   * Get list of Idoc Types
   * @return List of Idoc types
   * @throws JCoException JCoException
   */
  public List<IDocType> getIDocTypes() throws JCoException {
    List<IDocType> list = new ArrayList<>();
    Map<String, String> idoc2msg = new HashMap<>();
    JCoFunction function = getRepository().getFunction("IDOCTYPES_LIST_WITH_MESSAGES");
    function.getImportParameterList().setValue("PI_RELEASE", "");
    function.execute(getDestination());
    JCoTable idocs = function.getTableParameterList().getTable("PT_MESSAGES");
    for (int i = 0; i < idocs.getNumRows(); i++) {
      idocs.setRow(i);
      String idocName = idocs.getString("IDOCTYP");
      String idocDesc = idocs.getString("DESCRP");
      String devClass = idocs.getString("MESTYP");
      String releaseIn = idocs.getString("RELEASED");
      if ((releaseIn != null) && (!releaseIn.isEmpty())) {
        list.add(new IDocType(idocName, idocDesc, devClass, releaseIn));
        idoc2msg.put(idocName, devClass);
      }
    }
    JCoTable extns = function.getTableParameterList().getTable("PT_EXTTYPES");
    for (int i = 0; i < extns.getNumRows(); i++) {
      extns.setRow(i);
      String idocName = extns.getString("IDOCTYP");
      String idocDesc = extns.getString("DESCRP");
      String devClass = idoc2msg.get(idocName);
      String releaseIn = extns.getString("RELEASED");
      String ext = extns.getString("CIMTYP");

      if (releaseIn == null) {
        releaseIn = "";
      }
      list.add(new IDocType(idocName, idocDesc, devClass, releaseIn, ext));
    }
    return list;
  }


  public IDocRepository getIDocRepsitory() throws JCoException {
    if (idocRepo == null) {
      idocRepo = JCoIDoc.getIDocRepository(jcoClient);
    }
    return idocRepo;
  }


  private static final String MESTYP = "MESTYP";
  private static final String IDOCTYP = "IDOCTYP";
  private static final String CIMTYP = "CIMTYP";
  private static final String OUT_RCVPRN = "OUT_RCVPRN";
  private static final String EDI_DC40 = "EDI_DC40";
  private static final String DOCREL = "DOCREL";
  private static final String MANDT = "MANDT";
  private static final String DIRECT = "DIRECT";
  private static final String ARCKEY = "ARCKEY";
  private static final String OUTMOD = "OUTMOD";


  /**
   * This method will return all the Idoc types configured with a program ID configuration
   * @return Json node
   * @throws JCoException JCoException
   */
  public JsonNode getOutboundIdocList() throws JCoException {
    logger.info("sapProperties : " + sapProps);
    JsonNode idocList;
    List<String> fields = new ArrayList<>();
    fields.add(MESTYP);
    fields.add(IDOCTYP);
    fields.add(CIMTYP);
    String receiverPartner = sapProps.getIdocprofiles().get(OUT_RCVPRN);
    List<String> idocTypes =
            getConfiguredIdocList("EDP13", receiverPartner, fields, "RCVPRN");

    if (idocTypes.isEmpty()) {
      throw SystemException.throwException(AdapterConstants.JCO_RUNTIME_EXCEPTION + "",
              new AdapterException(AdapterConstants.JCO_RUNTIME_EXCEPTION + "~No configured IDoc type found for provided partner profile \"\n" +
                      "                        + receiverPartner + \".  \"\n" +
                      "                        + \"Please configure outbound IDocs in SAP, before sending"));
    }
    List<IDocType> typeList = getIDocfromType(idocTypes);
    idocList = JsonUtil.getJson(typeList);
    logger.info("Outbound IDoc list: " + idocList);
    return idocList;
  }

  /**
   * This method return idoc Types list based on provided idoc types name
   * @param idocTypes List of idoc types
   * @return list of IDocType
   * @throws JCoException JCoException
   */
  public List<IDocType> getIDocfromType(List<String> idocTypes) throws JCoException {
    List<IDocType> selectedTypes = new ArrayList<>();
    List<IDocType> allTypes = getIDocTypes();
    for (IDocType type : allTypes) {
      String toCompare;
      if (type.getExt() != null)
        toCompare = type.getDevclass() + ";" + type.getBaseName() + ";" + type.getExt();
      else
        toCompare = type.getDevclass() + ";" + type.getBaseName() + ";";
      if (idocTypes.contains(toCompare)) {
        selectedTypes.add(type);
        idocTypes.remove(toCompare);
      }
    }
    return selectedTypes;
  }

  /**
   * This Method will post IDOC to SAP
   * @param payload Payload data
   * @param idocType Idoc Type
   * @param idoc Idoc name
   * @param uniqueIdentifier Unique identifier, can be used as to pass manual tid
   * @param release release number
   * @return Json node
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  public JsonNode postIDOC(String payload, String idocType, String idoc, String uniqueIdentifier,
                           String release) throws JCoException, IOException {
    ObjectNode oNode;
    String tid = null;
    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.readTree(payload);
    logger.info("idoc Payload {0}", payload);

    JsonNode metaNode = json.get("meta");
    JsonNode tidNode = metaNode.get("tid");
    tid = tidNode.toString().replace("\"", "");

    if(tid == null || tid.isEmpty())
    {
      tid = getDestination().createTID();
    }
    JsonNode idocNode = json.get("IDOC");
    if (idocNode == null)
      throw SystemException.throwException("Error while executing send IDoc. Input payload does not contains the mandatory IDoc element.",
              new AdapterException(AdapterConstants.JCO_RUNTIME_EXCEPTION + ""));
    /**
     * Check for EDI_DC40 Need to set "DIRECT": "1,"OUTMOD": "2","IDOCTYP": "MATMAS01",
     * "MESTYP": "MATMAS", "MANDT": "800",
     */
    ObjectNode edidc40 = null;
    if (!idocNode.has(EDI_DC40)) {
      edidc40 = mapper.createObjectNode();
    } else
      edidc40 = (ObjectNode) idocNode.get(EDI_DC40);
    Map<String, String> idocProfile = sapProps.getIdocprofiles();

    for (String key : idocProfile.keySet()) {
      if (!edidc40.has(key) && !key.equals(OUT_RCVPRN))
        edidc40.put(key, idocProfile.get(key));
    }
    String ext = "";
    if (idoc.contains(".")) {
      String[] encodedIdocName = idoc.split("\\.");
      ext = encodedIdocName[1];
      idoc = encodedIdocName[0];
    }
    edidc40.put(DOCREL, release);
    if (!edidc40.has(DIRECT))
      edidc40.put(DIRECT, "1");
    if (!edidc40.has(OUTMOD))
      edidc40.put(OUTMOD, "2");
    if (!edidc40.has(IDOCTYP))
      edidc40.put(IDOCTYP, idoc);
    if (!edidc40.has(MESTYP))
      edidc40.put(MESTYP, idocType);
    if (!edidc40.has(MANDT))
      edidc40.put(MANDT, sapProps.getDestination_JCO_CLIENT());
    if (!edidc40.has(ARCKEY))
      edidc40.put(ARCKEY, tid);
    if (!edidc40.has(IDOCTYP))
      edidc40.put(IDOCTYP, idoc);
    if (!edidc40.has(MESTYP))
      edidc40.put(MESTYP, idocType);
    if (!edidc40.has(CIMTYP))
      edidc40.put(CIMTYP, ext);
    ((ObjectNode) idocNode).set(EDI_DC40, edidc40);

//      String tid = getDestination().createTID();
    JCoFunction function = getRepository().getFunction("IDOC_INBOUND_ASYNCHRONOUS");
    converter.conIdocJsonToJCO(function, json);
    logger.info( "idoc tid {0}", tid);
    function.execute(getDestination());
    String[] array = mapper.convertValue(IDocType.IDOCStatus.get("00"), String[].class);
    oNode = mapper.createObjectNode();
    oNode.put("TID", tid);
    oNode.put("statusCode", "00");
    oNode.put("status", array[0]);
    oNode.put("statusDescription", array[1]);
    getDestination().confirmTID(tid);
    logger.info( function.toXML());
    return oNode;
  }
}
