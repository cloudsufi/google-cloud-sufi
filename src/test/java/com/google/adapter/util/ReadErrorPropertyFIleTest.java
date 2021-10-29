package com.google.adapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReadErrorPropertyFIleTest {

  static InputStream input;
  static Properties prop;
  private ReadErrorPropertyFile readFile;

  @AfterAll
  static void tearDownAfterClass() {
  }

  @BeforeEach
  public void setup() {
    readFile = ReadErrorPropertyFile.getInstance("error_mapping.json");
  }


  @Test
  public void testGetInstance() {
    ReadErrorPropertyFile readFileLocal = ReadErrorPropertyFile.getInstance();
    System.out.println(readFileLocal.equals(null));
    boolean check = false;
    if (readFileLocal.equals(null)) {
      check = true;
    }
    assertEquals(check, false);
  }
  @Test
  public void getConfigTest() {
    boolean check = false;
    if (readFile.getErrorDetails("102") != null) {
      check = true;
    }
    assertEquals(check, true);
  }

  @Test
  public void getConfigFour() {
    boolean check = false;
    if (readFile.getErrorDetails("106") != null) {
      check = true;
    }
    assertEquals(check, true);
  }

  @Test
  public void getConfigTestThree() {
    boolean check = false;
    if (readFile.getErrorDetails("99") == null) {
      check = false;
    }
    assertEquals(check, false);
  }

  @Test
  public void getCodesOne() {
    boolean check = false;
    if (readFile.getErrorDetails("99") == null) {
      check = false;
    }
    assertEquals(check, false);
  }

  @Test
  public void getCodesTwo() {
    boolean check = false;
    if (readFile.getErrorDetails("102") != null) {
      check = true;
    }
    assertEquals(check, true);
  }

}

