package com.google.adapter.properties;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ApplicationProperties {
  INSTANCE;

  private final Properties properties;
  private final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

  ApplicationProperties() {

    properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream("application.yml"));
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  public String getAppName() {
    return properties.getProperty("app.name");
  }
}
