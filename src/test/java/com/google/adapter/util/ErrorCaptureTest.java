package com.google.adapter.util;

import com.google.adapter.connector.SAPAdapterImpl;
import com.google.adapter.exceptions.SystemException;
import com.sap.conn.jco.JCoException;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorCaptureTest {

  private ErrorCapture errorCapture;
  private ExceptionUtils exceptionUtils;
  private SAPAdapterImpl sapAdapterImpl;
  private Exception exception = null;

  public ErrorCaptureTest() {
  }

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
    Properties connection = readPropertyFile.readPropertyFile();
    exceptionUtils = new ExceptionUtils();
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);
  }

  @Test
  void testExceptionLogic() {
    try {
      sapAdapterImpl.ping();
    } catch (Exception e) {
      exception = e;
    }
    MatcherAssert
        .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
            Matchers.is("650"));
  }
}
