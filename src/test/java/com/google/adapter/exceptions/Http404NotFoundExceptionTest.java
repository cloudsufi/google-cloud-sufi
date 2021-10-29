package com.google.adapter.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class Http404NotFoundExceptionTest {

  @Test
  public void testHttp404NotFoundException() {
    Http404NotFoundException http404NotFoundException = new Http404NotFoundException(
        "Not found exception");
    assertThat(http404NotFoundException.getMessage(), Matchers.is("Not found exception"));
  }
}
