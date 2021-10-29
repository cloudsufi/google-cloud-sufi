package com.google.adapter.connector;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.RFCMethod;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ReadPropertyFile;
import com.sap.conn.jco.JCoException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryparser.classic.ParseException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SAPConnectorTest {

  private SAPProperties sapProps;
  private SAPConnector sapConnector;
  private ErrorCapture errorCapture;
  private static final Logger log = Logger.getLogger(SAPConnectorTest.class.getName());

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() throws JCoException, IOException {
    ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
    Properties connection = readPropertyFile.readPropertyFile();
    sapProps = SAPProperties.getDefault(connection);
    sapConnector = new SAPConnector(errorCapture, connection);
  }

  @Test
  void testPing() {
    try {
      assertThat(sapConnector.ping(), Matchers.equalTo(true));
    } catch (JCoException e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testRFCMethodTwo() {
    boolean exception = false;
    try {
      sapConnector.invokeFunction("abc", 0);
    } catch (Exception e) {
      exception = true;
    }
    MatcherAssert
            .assertThat(exception, Matchers.is(true));
  }

  @Test
  public void testRFCMethodThree() {
    boolean exception = false;
    try {
      sapConnector.invokeTABLE_ENTRIES_GET_VIA_RFC("abc");
    } catch (Exception e) {
      exception = true;
    }
    MatcherAssert
            .assertThat(exception, Matchers.is(true));
  }

  @Test
  public void testSAPConnectorFour() {
    boolean toStringValue = false;
    String string = sapConnector.toString();
    if (string.contains("SAPConnector")) {
      toStringValue = true;
    }
    MatcherAssert
            .assertThat(toStringValue, Matchers.is(true));
  }

  @Test
  public void testRFCMethodFive() {
    boolean toStringValue = false;
    RFCMethod rfc = new RFCMethod("", "", "");
    String string = rfc.toString();
    if (string.contains("RFC")) {
      toStringValue = true;
    }
    MatcherAssert
            .assertThat(toStringValue, Matchers.is(true));
  }

  @Test
  public void testKeyForRfcCompList() {
    String string = sapConnector.prepareCacheKey("key");
    MatcherAssert
        .assertThat(string, Matchers.is("10.132.0.4:100:bpinst:EN:key"));
  }

  @Test
  public void testGetRFCList() throws ExecutionException, InterruptedException, IOException, ParseException {
    JsonNode node = sapConnector.getRFCList(null, true);
//    Map<String, List<RFCMethod>> map2 = sapConnector.getRFCList(null, false);
    MatcherAssert
        .assertThat(node.size(), Matchers.is(24));
  }

  @Test
  public void testExecuteRFC() {
    HashMap<String, String> opProps = new HashMap<String, String>();
    opProps.put("RFC", "RFC_READ_TABLE");
    try {
      BufferedReader br =
          new BufferedReader(new FileReader("src/test/resources/RFC_READ_TABLE.json"));
      StringBuffer json = new StringBuffer();
      String line = null;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      JsonNode response = sapConnector.executeRFC(json.toString(), opProps,null,null);
      assertThat(response.size(), Matchers.is(1));
      log.log(Level.INFO, response.toString());
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Test
  public void testExecuteBAPI() {
    HashMap<String, String> opProps = new HashMap<String, String>();
    opProps.put("BAPI", "PurchaseOrderFRE.CreateFromData1");
    try {
      BufferedReader br =
          new BufferedReader(new FileReader("src/test/resources/BAPI_PO_CREATE1.json"));
      StringBuffer json = new StringBuffer();
      String line = null;
      while ((line = br.readLine()) != null) {
        json.append(line);
      }
      JsonNode response = sapConnector.executeBAPI(json.toString(), opProps,null,null);
      log.log(Level.INFO, response.toString());
      assertThat(response.size(), Matchers.is(11));

    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }
}

