package com.google.adapter.util;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReadPropertyFileTest {

  private ReadPropertyFile readPropertyFile;

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() {
    readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
  }

  @Test
  void testGetDestinationName() {
    assertThat(readPropertyFile.readPropertyFile(), Matchers.notNullValue());

  }
}
