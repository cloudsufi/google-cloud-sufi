package com.google.adapter.exceptions;


/**
 * Created by Sahil Kapoor
 */
public class NullParameterException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param exception Exception
   */
  public NullParameterException(String exception) {
    super(exception);
  }

}