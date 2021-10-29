package com.google.adapter.exceptions;


/**
 * Created by sk sanjay on 02nd April 2020.
 */
public class Http400BadRequest extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param exception Exception
   */
  public Http400BadRequest(String exception) {
    super(exception);
  }

}