package com.google.adapter.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class AdapterExceptionTest {

  @Test
  public void testAdapterException() {
    AdapterException adapterException = new AdapterException("adapter exception");
    assertThat(adapterException.getMessage(), Matchers.is("adapter exception"));
  }
}
