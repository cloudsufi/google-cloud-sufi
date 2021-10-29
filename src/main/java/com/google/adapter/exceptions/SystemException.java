package com.google.adapter.exceptions;

import com.google.adapter.beans.ErrorDetails;
import com.google.adapter.util.ExceptionUtils;

/**
 * Created By Sahil Kapoor This class is to return the exception message,cause and error code
 */
public class SystemException extends RuntimeException {

  private ErrorDetails errorDetails;
  private ExceptionUtils exceptionUtils;

  /**
   * This method is to get the user defined exception
   *
   * @param message      message
   * @param exception Exception
   */
  public SystemException(String message, Exception exception) {
    super(message, exception);
    this.exceptionUtils = new ExceptionUtils();
    this.errorDetails = this.exceptionUtils.convertErrorDetails(exception, message);
  }

  /**
   * This method will check if thrown exception is instance of SystemException then it will return the same,
   * else it will return the new instance of SystemException
   * @param message Exception message
   * @param exception Exception object
   * @return SystemException
   */
  public static SystemException throwException(String message, Exception exception) {
    if (exception instanceof SystemException) {
      return (SystemException)exception;
    }
    return new SystemException(message, exception);
  }

  public ErrorDetails getErrorDetails() {
    return errorDetails;
  }
}