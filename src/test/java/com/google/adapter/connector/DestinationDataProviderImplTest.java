package com.google.adapter.connector;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DestinationDataProviderImplTest {

  private DestinationDataProviderImpl classUnderTest;

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() {
    classUnderTest = new DestinationDataProviderImpl();
  }

  @Test
  public void testGetDestinationProperties() {
    String key = "sample";
    assertThat(classUnderTest.getDestinationProperties(key), Matchers.anything(null));
  }

  @Test
  public void testSetProperties() {
    assertThat(DestinationDataProviderImpl.getInstance(), Matchers.notNullValue());
  }

  @Test
  public void testGetInstance() {
    assertThat(DestinationDataProviderImpl.getInstance(), Matchers.notNullValue());
  }

  @Test
  public void testSupportsEvents() {
    assertThat(classUnderTest.supportsEvents(), Matchers.notNullValue());
  }
}
