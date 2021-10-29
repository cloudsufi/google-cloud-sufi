package com.google.adapter.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class Http500InternalServerErrorTest {

  @Test
  public void testHttp500InternalServerError() {
    Http500InternalServerError http500InternalServerError = new Http500InternalServerError(
        "null pointer exception");
    assertThat(http500InternalServerError.getMessage(), Matchers.is("null pointer exception"));
  }
}
