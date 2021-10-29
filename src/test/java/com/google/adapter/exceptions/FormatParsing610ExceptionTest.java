package com.google.adapter.exceptions;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FormatParsing610ExceptionTest {


  @Test
  public void numberFormatException() {

    try {
      FormatParsing610Exception test = new FormatParsing610Exception(
          "FormatParsing610ExceptionTest");
      throw new FormatParsing610Exception("FormatParsing610ExceptionTest");

    } catch (FormatParsing610Exception ex) {
      assertEquals("FormatParsing610ExceptionTest", ex.getMessage());
    }

  }
}
