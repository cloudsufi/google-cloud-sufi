package com.google.adapter.util;

import com.google.adapter.connector.SAPProperties;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dhiraj.kumar
 */
public class ReadPropertyFile {

  private static final Logger logger = LoggerFactory.getLogger(ReadPropertyFile.class);
  private String fileName;

  public ReadPropertyFile(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Read Connection Property file
   *
   * @return Connection parameter after reading from properties file
   */
  public Properties readPropertyFile() {
    Properties prop = new Properties();
    InputStream input;
    Properties connection = new SAPProperties();
    try {
      input = ReadPropertyFile.class.getResourceAsStream("/" + fileName);
      prop.load(input);
      Set<String> propertyNames = prop.stringPropertyNames();
      for (String Property : propertyNames) {
        connection.put(Property, prop.getProperty(Property));
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return connection;
  }
}
