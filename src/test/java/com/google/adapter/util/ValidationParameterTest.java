package com.google.adapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.adapter.connector.SAPProperties;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class ValidationParameterTest {

  public static SAPProperties properties;
  private ValidationParameter validationParameter = new ValidationParameter();

  @BeforeAll
  public static void initData() throws JCoException {
    ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
    Properties connection = readPropertyFile.readPropertyFile();
    properties = SAPProperties.getDefault(connection);
  }

  @Test
  public void ValidIPAddressTest() throws JCoException {
    properties.get("jco.client.ashost");
    boolean valid = validationParameter.validate(properties);
    assertEquals(valid, true);
  }

  @Test
  public void nullValue() throws JCoException {
    properties.put("1.1.1.1", "1.2.3.4");
    boolean valid = validationParameter.notNullConstraint(properties);
    assertEquals(valid, true);
  }

  @Test
  public void nullValuetwo() throws JCoException {
    properties.put("1.1.1.1", "");
    boolean valid = validationParameter.notNullConstraint(properties);
    assertEquals(valid, false);
  }

  @Test
  public void checkClientTest() throws JCoException {
    properties.get(DestinationDataProvider.JCO_CLIENT);
    boolean valid = validationParameter.checkClient(properties);
    assertEquals(valid, true);
  }

}
