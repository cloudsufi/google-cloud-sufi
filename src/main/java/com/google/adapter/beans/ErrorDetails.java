package com.google.adapter.beans;

import java.util.Date;

/**
 * Created by sk sanjay on 02nd April 2020.
 */
public class ErrorDetails {

  private Date timestamp;
  private String primaryMessage;
  private String primaryCode;
  private String secondaryMessage;
  private String secondaryCode;

  /**
   * Empty Constructor
   */
  public ErrorDetails() {
  }

  /**
   * @param timestamp        time stamp
   * @param primaryMessage   primaryMessage
   * @param primaryCode      primaryCode
   * @param secondaryMessage secondaryMessage
   * @param secondaryCode    secondaryCode
   */
  public ErrorDetails(Date timestamp,
      String primaryMessage, String primaryCode,
      String secondaryMessage, String secondaryCode) {
    super();
    this.timestamp = timestamp;
    this.primaryMessage = primaryMessage;
    this.primaryCode = primaryCode;
    this.secondaryMessage = secondaryMessage;
    this.secondaryCode = secondaryCode;
  }

  //getters
  public Date getTimestamp() {
    return timestamp;
  }

  public String getPrimaryMessage() {
    return primaryMessage;
  }

  public String getPrimaryCode() {
    return primaryCode;
  }

  public String getSecondaryMessage() {
    return secondaryMessage;
  }

  public String getSecondaryCode() {
    return secondaryCode;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setPrimaryMessage(String primaryMessage) {
    this.primaryMessage = primaryMessage;
  }

  public void setPrimaryCode(String primaryCode) {
    this.primaryCode = primaryCode;
  }

  public void setSecondaryMessage(String secondaryMessage) {
    this.secondaryMessage = secondaryMessage;
  }

  public void setSecondaryCode(String secondaryCode) {
    this.secondaryCode = secondaryCode;
  }
}
