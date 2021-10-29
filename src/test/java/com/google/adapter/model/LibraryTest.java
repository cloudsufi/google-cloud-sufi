package com.google.adapter.model;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Created by sk sanjay on 10th April 2020.
 */
public class LibraryTest {

  /**
   *
   */
  @Test
  public void testSomeLibraryMethod() {
    Library classUnderTest = new Library();
    assertThat(classUnderTest.someLibraryMethod(), Matchers.is(true));
  }
}