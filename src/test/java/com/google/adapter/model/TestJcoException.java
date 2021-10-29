package com.google.adapter.model;

public class TestJcoException {

  private int groupId;

  private String key;

  private String message;

  public TestJcoException() {
  }

  public TestJcoException(int groupId, String key, String message) {
    super();
    this.groupId = groupId;
    this.key = key;
    this.message = message;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}


