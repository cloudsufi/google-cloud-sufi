package com.google.adapter.connector;

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.adapter.exceptions.SystemException;
import com.google.adapter.util.ReadPropertyFile;
import com.google.adapter.util.ValidationParameter;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SAPPropertiesTest {

  private SAPProperties properties;
  private String fileName;
  private static ReadPropertyFile readPropertyFile;


  @BeforeAll
  static void setUpBeforeClass() {
    readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() {
    properties = new SAPProperties();
  }

  @Test
  void testGetDefault() throws JCoException {
    Properties connection = readPropertyFile.readPropertyFile();
    assertThat(SAPProperties.getDefault(connection), Matchers.notNullValue());
  }

  @Test
  void testGetDestinationName() {
    assertThat(properties.getDestinationName(), Matchers.notNullValue());

  }

  @Test
  void testGetDefaultNegative() throws JCoException {
    Exception exception = null;
    ReadPropertyFile readPropertyFile = new ReadPropertyFile(
        "Google_SAP_Connection_negative.properties");
    Properties connection = readPropertyFile.readPropertyFile();
    connection.remove(DestinationDataProvider.JCO_LANG);
    connection.put(DestinationDataProvider.JCO_LANG, "ENN");
    try {
      SAPProperties.getDefault(connection);
    } catch (Exception e) {
      exception = e;
    }
    MatcherAssert
        .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
            Matchers.is("650"));
  }

  @Test
  void testValidateParameters() {
    Properties connection = readPropertyFile.readPropertyFile();
    properties.validateParameters(connection,new ValidationParameter());
    assertThat(properties.validateParameters(connection,new ValidationParameter()), Matchers.is(true));

  }

}

