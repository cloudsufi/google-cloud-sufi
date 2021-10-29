package com.google.adapter;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Created by sk sanjay on 10th April 2020.
 */
public class ApplicationStarterTest {

  /**
   *
   */
  @Test
  public void testAppHasAGreeting() {
    ApplicationStarter classUnderTest = new ApplicationStarter();
    assertThat(classUnderTest.getGreeting(), Matchers.is("Hello world."));
  }
}