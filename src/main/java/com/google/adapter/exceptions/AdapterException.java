package com.google.adapter.exceptions;

/**
 * Created by sk sanjay on 02nd April 2020.
 */
public class AdapterException extends RuntimeException {

  static final boolean ENABLE_SUPPRESION = true;
  static final boolean WRITABLE_STACK_TRACE = true;
  private static final long serialVersionUID = 1L;

  public AdapterException(String message) {
    super(message);
  }

}
