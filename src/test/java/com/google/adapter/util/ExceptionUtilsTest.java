package com.google.adapter.util;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.adapter.connector.SAPAdapterImpl;
import com.google.adapter.connector.SAPConnector;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.RFCMethod;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExceptionUtilsTest {

  public Properties connection = null;
  private ErrorCapture errorCapture;
  private ExceptionUtils exceptionUtils;
  private SAPAdapterImpl sapAdapterImpl;
  private SAPConnector sapConnector;
  private ValidationParameter validationParameter = new ValidationParameter();

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() throws JCoException {
    ReadPropertyFile readPropertyFile = new ReadPropertyFile(
            "Google_SAP_Connection_negative.properties");
    connection = readPropertyFile.readPropertyFile();
    exceptionUtils = new ExceptionUtils();
    errorCapture = new ErrorCapture(exceptionUtils);
    sapConnector = new SAPConnector(errorCapture, connection);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);

  }

  @Test
  void testExceptionLogic() {
    Exception exception = null;
    try {
      sapAdapterImpl.ping();
    } catch (Exception e) {
      exception = e;
    }
    MatcherAssert
            .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                    Matchers.is("650"));
  }


  @Test
  public void validateTest() {
    connection.remove(DestinationDataProvider.JCO_ASHOST);
    MatcherAssert
            .assertThat((validationParameter.validate(connection)), Matchers.is(true));
  }

  @Test
  public void checkLangTest() {
    connection.remove(DestinationDataProvider.JCO_LANG);
    MatcherAssert
            .assertThat((validationParameter.checkLang(connection)), Matchers.is(true));
  }

  @Test
  public void checkClientTest() {
    connection.remove(DestinationDataProvider.JCO_CLIENT);
    MatcherAssert
            .assertThat((validationParameter.checkClient(connection)), Matchers.is(true));
  }

  @Test
  public void checkPoolCapacityTest() {
    connection.remove(DestinationDataProvider.JCO_POOL_CAPACITY);
    assertThat(validationParameter.checkPoolCapacity(connection), Matchers.is(true));
  }

  @Test
  public void checkPeakLimitTest() {
    connection.remove(DestinationDataProvider.JCO_PEAK_LIMIT);
    assertThat(validationParameter.checkPeakLimit(connection), Matchers.is(true));
  }

  @Test
  public void testRFCMethods() {
    Exception exception = null;
    try {
      JsonNode node = sapAdapterImpl.getRFCList(null, false);
      node.get("Basis");
    } catch (Exception e) {
      exception = e;
    }
    MatcherAssert
            .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                    Matchers.is("650"));
  }

  @Test
  void testInitialize() {
    boolean exception = false;
    try {
      connection.clear();
      sapConnector.initialize();
    } catch (Exception e) {
      exception = true;
    }
    MatcherAssert
            .assertThat(exception, Matchers.is(false));
  }
}
