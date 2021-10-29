package com.google.adapter.util;

import com.google.adapter.beans.ErrorDetails;

/**
 * Created by Sahil Kapoor This class is to revert the message in the runtime when exception is
 * thrown
 */
public class ErrorCapture {

  private ExceptionUtils exceptionUtils;

  /**
   * Public Constructor for ErrorCapture
   * @param exceptionUtils Initialize exceptionUtils
   */
  public ErrorCapture(ExceptionUtils exceptionUtils) {
    this.exceptionUtils = exceptionUtils;
  }

  /**
   * @param exception is to catch the exception group to throw the user defined exception
   * @param errorMessage errorMessage
   * @return Error details after conversion
   */
  public ErrorDetails exceptionLogic(Exception exception,  String errorMessage) {
    return exceptionUtils.convertErrorDetails(exception, errorMessage);
  }
}
