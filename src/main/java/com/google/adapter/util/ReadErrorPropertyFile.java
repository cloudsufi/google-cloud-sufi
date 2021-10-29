package com.google.adapter.util;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.beans.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sahil Kapoor This class is to read the standard error which we are present in the
 * Property file
 */
public final class ReadErrorPropertyFile {

  private static final Logger logger = LoggerFactory.getLogger(ReadErrorPropertyFile.class);
  private static ReadErrorPropertyFile readProperties = null;
  private final JsonNode errorDetails;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Constructor for ReadErrorPropertyFile
   */
  private ReadErrorPropertyFile(String fileName) throws IOException {
    errorDetails = JSONReader.readJson(fileName, JsonNode.class);
  }

  public static ReadErrorPropertyFile getInstance() {
    return getInstance(null);
  }

  /**
   * Create a singleton instance for ReadErrorPropertyFile
   * @param fileName File name
   * @return instance of {@link ReadErrorPropertyFile}
   */
  public static ReadErrorPropertyFile getInstance(final String fileName) {
    if (Objects.isNull(fileName) || fileName.isEmpty()) {
      if (Objects.nonNull(readProperties)) {
        return readProperties;
      }

      throw new NullPointerException("File name cannot be null or empty");
    }

    if (readProperties == null) {
      synchronized (ReadErrorPropertyFile.class) {
        if (readProperties == null) {
          try {
            readProperties = new ReadErrorPropertyFile(fileName);
          } catch (IOException e) {
            logger.error("error_mapping.json file not found in main/resources folder");
          }
        }
      }
    }

    return readProperties;
  }

  /**
   * This method will return {@link ErrorDetails} object based on primary code.
   * @param primaryCode Primary error code
   * @return {@link ErrorDetails}
   */
  public ErrorDetails getErrorDetails(String primaryCode) {
    ErrorDetails instance = mapper.convertValue(errorDetails.get(primaryCode), ErrorDetails.class);
    if (Objects.nonNull(instance)) {
      instance.setTimestamp(new Date());
    }
    return instance;
  }

  /*
   * @param key is the parameter which is holding the value in the property file
   * @return returns the value of the entry set.
   */
 /* public static String getConfig(String key) {
    Properties prop = new Properties();
    Map<String, String> map = new HashMap<>();
    try {
      prop.load(ReadPropertyFile.class.getResourceAsStream("/error.properties"));
    } catch (IOException | NullPointerException e) {
      logger.error(e.getMessage());
    }

    for (Map.Entry<Object, Object> entry : prop.entrySet()) {
      map.put((String) entry.getKey(), (String) entry.getValue());
      if (map.containsKey(key)) {
        return map.get(key);
      }
    }
    return null;
  }*/

  /*
   * @param key is the parameter which is holding the value in the property file
   * @return returns the value of the entry set.
   */
 /* public static String getCodes(String key) {
    Properties prop = new Properties();
    Map<String, String> map = new HashMap<>();
    try {
      prop.load(ReadPropertyFile.class.getResourceAsStream("/error_codes.properties"));
    } catch (IOException | NullPointerException e) {
      logger.error(e.getMessage());
    }

    for (Map.Entry<Object, Object> entry : prop.entrySet()) {
      map.put((String) entry.getKey(), (String) entry.getValue());
      if (map.containsKey(key)) {
        return map.get(key);
      }
    }
    return null;
  }*/
}
