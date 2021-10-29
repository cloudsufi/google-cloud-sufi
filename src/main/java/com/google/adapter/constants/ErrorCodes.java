package com.google.adapter.constants;

import com.google.adapter.util.EnumLookup;

/**
 * @author Dhiraj Kumar This class is to establish enumeration to throw exception messages as per
 * the caught exception
 */
public enum ErrorCodes {

  COMMUNICATION_ERROR_102("102"),
  ADAPTER_EXCEPTION_1000("1000"),
  AUTHORIZATION_EXCEPTION_103("103"),
  ABAP_EXCEPTION_111("111"),
  MISSING_PARAMETER_EXCEPTION_101("101"),
  RESOURCE_EXCEPTION_106("106"),
  DESTINATION_EXCEPTION_120("120"),
  SCHEMA_EXCEPTION_1001("1001"),
  JCO_RUNTIME_EXCEPTION("122"),
  JCO_ERROR_SYSTEM_FAILURE("104"),
  JCO_IDOC_ERROR("1002");
  /**
   * Search error defination according to code
   */
  private static final EnumLookup<ErrorCodes, String> BY_CODE = EnumLookup.of(
      ErrorCodes.class, ErrorCodes::getCode, "code");

  private final String code;

  /**
   * Constructor to initialize with error code
   *
   * @param code Error Code
   */
  ErrorCodes(String code) {
    this.code = code;
  }

  /**
   * Return matched code
   *
   * @return Error code
   */
  public static EnumLookup<ErrorCodes, String> byCode() {
    return BY_CODE;
  }

  /**
   * Public method to find error code
   *
   * @return code
   */
  public String getCode() {
    return code;
  }
}