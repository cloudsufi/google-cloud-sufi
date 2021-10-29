package com.google.adapter.connector;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.adapter.model.JcoElement;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ReadPropertyFile;
import com.sap.conn.jco.JCoException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SAPSchemaTest {

  private SAPProperties sapProps;
  private SAPSchema sapSchema;
  private SAPConnector sapConnector;
  private ErrorCapture errorCapture;
  private JcoElement jcoElement;
  private static final Logger log = Logger.getLogger(SAPSchemaTest.class.getName());

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
    sapSchema = new SAPSchema(sapConnector, errorCapture);
  }

  @Test
  void testGetIDOCSchema() throws JCoException {
    JsonNode schema = sapSchema.getIDOCSchema("MATMAS", "MATMAS01", "");
    log.log(Level.INFO, schema.toString());
    assertThat(schema.size(), Matchers.greaterThan(1));
  }

}
