package com.google.adapter.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class Http400BadRequestTest {

  @Test
  public void testAdapterException() {
    Http400BadRequest http400BadRequest = new Http400BadRequest("bad request exception");
    assertThat(http400BadRequest.getMessage(), Matchers.is("bad request exception"));
  }
}
