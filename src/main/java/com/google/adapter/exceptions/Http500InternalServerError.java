package com.google.adapter.exceptions;


/**
 * Created by sk sanjay on 02nd April 2020.
 */
public class Http500InternalServerError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param exception exception
   */
  public Http500InternalServerError(String exception) {
    super(exception);
  }

}
