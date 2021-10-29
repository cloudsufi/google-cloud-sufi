package com.google.adapter.beans;

import static org.hamcrest.MatcherAssert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorDetailsTest {

  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  java.util.Date date = sdf.parse("2020-04-28");
  private ErrorDetails errorDetails;

  public ErrorDetailsTest() throws ParseException {
  }

  @BeforeAll
  static void setUpBeforeClass() {
  }

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  void setUp() {

    errorDetails = new ErrorDetails(date, "COMMUNICATION_ERROR_102", "102", "Wrong Credentials",
        "102");
  }

  @Test
  void testGetTimestamp() {
    assertThat(errorDetails.getTimestamp(), Matchers.is(date));

  }

  @Test
  void testGetPrimaryMessage() {
    assertThat(errorDetails.getPrimaryMessage(), Matchers.is("COMMUNICATION_ERROR_102"));

  }

  @Test
  void testGetPrimaryCode() {
    assertThat(errorDetails.getPrimaryCode(), Matchers.is("102"));

  }

  @Test
  void testGetSecondaryMessage() {
    assertThat(errorDetails.getSecondaryMessage(), Matchers.is("Wrong Credentials"));

  }

  @Test
  void testGetSecondaryCode() {
    assertThat(errorDetails.getSecondaryCode(), Matchers.is("102"));

  }
}
