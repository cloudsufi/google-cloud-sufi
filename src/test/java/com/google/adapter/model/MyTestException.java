package com.google.adapter.model;

import java.util.ArrayList;

public class MyTestException {

  private String message;

  private ArrayList<TestJcoException> testJcoException;

  public MyTestException() {
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public ArrayList<TestJcoException> getTestJcoException() {
    return testJcoException;
  }

  public void setTestJcoException(ArrayList<TestJcoException> testJcoException) {
    this.testJcoException = testJcoException;
  }


}
