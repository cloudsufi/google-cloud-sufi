package com.google.adapter.exceptions;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NullParameterExceptionTest {


  @Test
  public void nullParameterTest() {

    try {
      NullParameterException test = new NullParameterException("FormatParsing610ExceptionTest");
      throw new NullParameterException("FormatParsing610ExceptionTest");

    } catch (NullParameterException ex) {
      assertEquals("FormatParsing610ExceptionTest", ex.getMessage());
    }

  }
}
