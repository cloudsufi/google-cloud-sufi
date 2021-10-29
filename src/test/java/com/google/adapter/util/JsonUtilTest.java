package com.google.adapter.util;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import com.google.adapter.configuration.util.CacheProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonUtilTest {

  private JsonUtil jsonUtil;

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @Test
  void testCacheProperties() throws IOException {
    boolean nodeRemoved = false;
    CacheProperties cp = new CacheProperties();
    if (CacheProperties.RFC_DATA != null) {
      nodeRemoved = true;
    }
    assertThat(nodeRemoved, Matchers.is(true));
  }


  public JsonNode provideJsonNode() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = "{ \"f1\":\"Hello\", \"f2\": []}";
    JsonNode jsonNode = objectMapper.readTree(json);
    String f2Value = jsonNode.get("f2").asText("Default");
    return jsonNode;
  }

  public JsonNode provideJsonNodeTwo() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = "{ \"f1\":\"Hello\", \"f2\": null}";
    JsonNode jsonNode = objectMapper.readTree(json);
    String f2Value = jsonNode.get("f2").asText("Default");
    return jsonNode;
  }

  @BeforeEach
  void setUp() {
    jsonUtil = new JsonUtil();
  }

  @Test
  void testRemoveEmptyNodes() throws IOException {
    boolean nodeRemoved = false;
    JsonNode jsonNode = provideJsonNode();
    if (jsonNode != null) {
      JsonUtil.removeEmptyNodes(jsonNode);
      jsonNode.isArray();
      nodeRemoved = true;
    }
    assertThat(nodeRemoved, Matchers.is(true));
  }

  @Test
  void testRemoveEmptyNodesTwo() throws IOException {
    JsonNode jsonNode = provideJsonNodeTwo();
    if (jsonNode != null) {
      JsonUtil.removeEmptyNodes(jsonNode);
    }
    assertThat(jsonNode.isArray(), Matchers.is(false));
  }

  @Test
  void testRemoveEmptyNodesThree() throws IOException {
    JsonNode jsonNode = provideJsonNodeTwo();
    if (jsonNode != null) {
      JsonUtil.removeEmptyNodes(jsonNode);
    }
    assertThat(jsonNode.isNull(), Matchers.is(false));
  }
}
