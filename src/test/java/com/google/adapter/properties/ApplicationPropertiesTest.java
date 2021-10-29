package com.google.adapter.properties;


import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class ApplicationPropertiesTest {

  @Test
  public void propertiesTest() {
    assertThat(ApplicationProperties.INSTANCE.getAppName(),
        Matchers.is("adapter application for google"));
  }

}
