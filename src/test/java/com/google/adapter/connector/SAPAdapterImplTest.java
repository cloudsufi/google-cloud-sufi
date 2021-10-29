package com.google.adapter.connector;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.RFCMethod;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ExceptionUtils;
import com.google.adapter.util.StrUtil;
import com.google.adapter.util.JsonUtil;
import com.google.adapter.util.ReadPropertyFile;
import com.sap.conn.jco.JCoException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SAPAdapterImplTest {

  private SAPProperties sapProps;
  private SAPAdapterImpl sapAdapterImpl;
  private ErrorCapture errorCapture;
  private ExceptionUtils exceptionUtils;
  ObjectMapper mapper = new ObjectMapper();
  private static final Logger log = Logger.getLogger(SAPAdapterImplTest.class.getName());

  @BeforeAll
  static void setUpBeforeClass() {}

  @AfterAll
  static void tearDownAfterClass() {}

  @BeforeEach
  void setUp() throws JCoException {
    ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
    Properties connection = readPropertyFile.readPropertyFile();
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void testPing() {
    try {
      assertThat(sapAdapterImpl.ping(), Matchers.equalTo(true));
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testGetRFCSchema() {
    String rfcName = "RFC_READ_TABLE";
    JsonNode node = sapAdapterImpl.getRFCSchema(rfcName);
    assertThat(node.size(), Matchers.is(2));
  }

  @Test
  public void testGetBAPISchema() {
    String bapiName = "BusinessArea.GetDetail";
    JsonNode node = sapAdapterImpl.getBAPISchema(bapiName);
    System.out.println(node);
    assertThat(node.size(), Matchers.is(2));
  }

  @Test
  public void testGetBAPIList() {
    JsonNode node = sapAdapterImpl.getBAPIList();
    System.out.println(node);
    assertThat(node.size(), Matchers.is(8));
  }

  @Test
  public void testExecuteRFC() {
    HashMap<String, String> opProps = new HashMap<>();
    opProps.put("RFC", "ZRFC_CHECK_PROGRAM_ID");
    opProps.put("autoCommit", "true");
    try {
      BufferedReader br =
              new BufferedReader(new FileReader("src/test/resources/ZRFC_CHECK_PROGRAM_ID.json"));
      StringBuffer json = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      JsonNode response = sapAdapterImpl.executeRFC(json.toString(), opProps, "", "");
      log.log(Level.INFO, response.toString());
      assertThat(response.size(), Matchers.is(2));

    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testExecuteBAPI() {
    HashMap<String, String> opProps = new HashMap<>();
    opProps.put("BAPI", "PurchaseOrderFRE.CreateFromData1");
    try {
      BufferedReader br =
              new BufferedReader(new FileReader("src/test/resources/BAPI_PO_CREATE1.json"));
      StringBuffer json = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      JsonNode response = sapAdapterImpl.executeBAPI(json.toString(), opProps, null, "");
      log.log(Level.INFO, response.toString());
      assertThat(response.size(), Matchers.is(11));

    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testGetIDOCList() {
    HashMap<String, String> idocProfile = new HashMap<>();
    idocProfile.put("OUT_RCVPRN", "LSGOOGLE");
    sapProps.setIdocprofiles(idocProfile);
    JsonNode json = sapAdapterImpl.getIDOCList();
    log.log(Level.INFO, json.toString());
    assertThat(json.size(), Matchers.is(16));
  }

  @Test
  public void testGetOutboundIDOCList() {
    HashMap<String, String> idocProfile = new HashMap<>();
    idocProfile.put("OUT_RCVPRN", "LSGOOGLE");
    sapProps.setIdocprofiles(idocProfile);
    JsonNode json = sapAdapterImpl.getOutboundIdocList();
    log.log(Level.INFO, json.toString());
    assertThat(json.size(), Matchers.is(5));
  }

  @Test
  void testGetIdocSchema() {
    JsonNode schema = sapAdapterImpl.getIdocSchema("MATMAS", "MATMAS05", "", "");
    log.log(Level.INFO, schema.toString());
    assertThat(schema.size(), Matchers.greaterThan(1));
  }

  @Test
  public void testActivateListenerRFC() {
    Map<String, String> operationProps = new HashMap<>();
    operationProps.put("type", "IDOC");
    operationProps.put("idocType", "MATMAS");
    operationProps.put("idoc", "MATMAS05");
    operationProps.put("releaseNumber", "754");
    try {
      sapAdapterImpl.subscribe(operationProps, null, errorCapture, sapProps);
      ExecutorService executorService = Executors.newFixedThreadPool(1);
      executorService.execute(
              () -> {
                HashMap<String, String> opProps = new HashMap<>();
                opProps.put("RFC", "ZFM_SEND_IDOC_BD10");
                //      opProps.put("autoCommit", "true");
                BufferedReader br = null;
                try {
                  br = new BufferedReader(new FileReader("src/test/resources/BD10_IDOC_TRIGGER.json"));
                } catch (FileNotFoundException e) {
                  throw SystemException.throwException(e.getMessage(), e);
                }
                StringBuffer json = new StringBuffer();
                String line;
                try {
                  while ((line = br.readLine()) != null) {
                    json.append(line);
                  }
                } catch (IOException ioe) {
                  throw SystemException.throwException(ioe.getMessage(), ioe);
                }
                JsonNode response = sapAdapterImpl.executeRFC(json.toString(), opProps, "", "");
              });

      if (executorService.awaitTermination(10, TimeUnit.SECONDS)) executorService.shutdown();
      sapAdapterImpl.unSubscribe(operationProps);
      Assertions.assertTrue(new File(AdapterConstants.FILE_PATH_IDOC).length()>0);
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testInBoundRFC() {
    Map<String, String> opProps = new HashMap<>();
    Map<String, String> operationProps = new HashMap<>();
    operationProps.put("type", "RFC");
    operationProps.put("rfc", "ZRFC_CHANGING_INPUT");
    try {
      sapAdapterImpl.subscribe(operationProps, null, errorCapture, sapProps);
      JsonNode node = mapper.readValue(new File("src/test/resources/ZRFC_CALLING.json"), JsonNode.class);
      opProps.put("RFC", "ZRFC_CHANGING_INPUT");
      Assertions.assertThrows(Exception.class, () -> {
        sapAdapterImpl.executeRFC(node.get(0).toString(), opProps, "", "");
      });

      opProps.put("RFC", "ZRFC_CHANGING_INPUT");
      sapAdapterImpl.executeRFC(node.toString(), opProps, "", "");
      sapAdapterImpl.unSubscribe(operationProps);
      File file = new File(AdapterConstants.FILE_PATH_IDOC);
      Assertions.assertTrue(StrUtil.fileFinder(AdapterConstants.FILE_PATH_IDOC, "ZRFC_CHANGING"));
    } catch (Exception e) {
      Assertions.fail("Test case for Inbound RFC failed.");
    }
  }


  @Test
  void testPostIDOC() {
    try {
      BufferedReader br =
              new BufferedReader(new FileReader("src/test/resources/testPostIDOC.json"));
      StringBuffer json = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      // JSONObject data = new JSONObject(json.toString());
      JsonNode response =
              executeIDOC(json.toString(), "MATMAS", "MATMAS05", "", "754");
      assertThat(response.size(), Matchers.greaterThan(2));
      log.log(Level.INFO, response.toString());
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  JsonNode executeIDOC(String payloadFile, String idocType, String idocName,
                       String uniqueIdentifier, String release) throws Exception {
    HashMap<String, String> idocProfiles = new HashMap<>();
    idocProfiles.put("SNDPOR", "SAPS4H");
    idocProfiles.put("RCVPRN", "LSGOOGLE");
    idocProfiles.put("SNDPRT", "LS");
    idocProfiles.put("SNDPRN", "S4HCLNT100");
    idocProfiles.put("RCVPOR", "LSGOOGLE");
    idocProfiles.put("RCVPRT", "LS");
    sapProps.setIdocprofiles(idocProfiles);
    BufferedReader br = new BufferedReader(new FileReader("src/test/resources/testPostIDOC.json"));
    StringBuffer json = new StringBuffer();
    String line = null;
    while ((line = br.readLine()) != null) {
      json.append(line);
    }
    JsonNode response =
            sapAdapterImpl.postIDOC(json.toString(), idocType, idocName, uniqueIdentifier, release);
    br.close();
    return response;
  }

  @Test
  public void testGetProgramID() {
    HashMap<String, String> opProps = new HashMap<>();
    opProps.put("RFC", "RFC_READ_TABLE");
    try {
      BufferedReader br =
              new BufferedReader(new FileReader("src/test/resources/RFC_READ_PRGM_ID.json"));
      StringBuffer json = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      JsonNode response = sapAdapterImpl.executeRFC(json.toString(), opProps, "", "");
      log.log(Level.INFO, response.toString());
      assertThat(response.size(), Matchers.is(3));

    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testIdocStatus() {
    JsonNode response = sapAdapterImpl.getIdocStatus("123426");
    log.log(Level.INFO, response.toString());
    assertThat(response.size(), Matchers.greaterThan(2));
  }

  @Test
  public void testMultipleActivateListenerRFC() throws JCoException {
    Map<String, String> operationProps = new HashMap<>();
    operationProps.put("type", "IDOC");
    operationProps.put("idocType", "MATMAS");
    operationProps.put("idoc", "MATMAS05");
    operationProps.put("releaseNumber", "754");

    ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
    Properties connection = readPropertyFile.readPropertyFile();
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    SAPAdapterImpl sapAdapterImpl2 = new SAPAdapterImpl(errorCapture, connection);
    SAPAdapterImpl sapAdapterImpl3 = new SAPAdapterImpl(errorCapture, connection);

//    Map<String, String> operationProps2 = new HashMap<>();
//    operationProps2.put("type", "IDOC");
//    operationProps2.put("idocType", "MATMAS");
//    operationProps2.put("idoc", "MATMAS05");
//    operationProps2.put("releaseNumber", "754");
    try {
      sapAdapterImpl.subscribe(operationProps, null, errorCapture, sapProps);
      createFile();
      sapAdapterImpl.unSubscribe(operationProps);

      sapAdapterImpl2.subscribe(operationProps, null, errorCapture, sapProps);
      createFile();
      sapAdapterImpl2.unSubscribe(operationProps);

      sapAdapterImpl3.subscribe(operationProps, null, errorCapture, sapProps);
      createFile();
      sapAdapterImpl3.unSubscribe(operationProps);

      File file = new File(AdapterConstants.FILE_PATH_IDOC);
      Assertions.assertTrue(StrUtil.fileFinder(AdapterConstants.FILE_PATH_IDOC, "MATMAS05"));
    } catch (Exception e) {
      Assertions.fail("Test case for Multiple Activation failed.");
    }
  }

  public void createFile() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(
            () -> {
              HashMap<String, String> opProps = new HashMap<>();
              opProps.put("RFC", "ZFM_SEND_IDOC_BD10");
              //      opProps.put("autoCommit", "true");
              BufferedReader br = null;
              try {
                br = new BufferedReader(new FileReader("src/test/resources/BD10_IDOC_TRIGGER.json"));
              } catch (FileNotFoundException e) {
                throw SystemException.throwException(e.getMessage(), e);
              }
              StringBuffer json = new StringBuffer();
              String line;
              try {
                while ((line = br.readLine()) != null) {
                  json.append(line);
                }
              } catch (IOException ioe) {
                throw SystemException.throwException(ioe.getMessage(), ioe);
              }
              JsonNode response = sapAdapterImpl.executeRFC(json.toString(), opProps, "", "");
            });

    if (executorService.awaitTermination(10, TimeUnit.SECONDS)) executorService.shutdown();
  }
}
