package com.google.adapter.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class Http401UnauthorizedTest {

  @Test
  public void testAdapterException() {
    Http401Unauthorized http401UnauthorizedTest = new Http401Unauthorized("Unauthorized exception");
    assertThat(http401UnauthorizedTest.getMessage(), Matchers.is("Unauthorized exception"));
  }
}
