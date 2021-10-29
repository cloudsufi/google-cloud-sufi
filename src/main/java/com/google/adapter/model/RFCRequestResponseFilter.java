package com.google.adapter.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class RFCRequestResponseFilter extends SimpleBeanPropertyFilter {

  private boolean isRequest;

  /**
   * Public constructor to initialize object with boolean value
   *
   * @param isRequest boolean
   */
  public RFCRequestResponseFilter(boolean isRequest) {
    this.isRequest = isRequest;
  }

  /**
   * Method to serialize fields
   *
   * @param pojo     Object
   * @param jgen     JsonGenerator
   * @param provider SerializerProvider
   * @param writer   PropertyWriter
   * @throws Exception Exception
   */
  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider,
      PropertyWriter writer) throws Exception {
    RFCParameter parameter = (RFCParameter) pojo;
    if (include(writer)) {
      checkIsRequest(pojo, jgen, provider, writer, parameter);
    } else if (!jgen.canOmitFields()) { // since 2.3
      writer.serializeAsOmittedField(pojo, jgen, provider);
    }
  }

  /**
   * Check and set if it's value is request or not
   *
   * @param pojo      Object
   * @param jgen      JsonGenerator
   * @param provider  SerializerProvider
   * @param writer    PropertyWriter
   * @param parameter RFCParameter
   * @throws Exception Exception
   */
  private void checkIsRequest(Object pojo, JsonGenerator jgen, SerializerProvider provider,
      PropertyWriter writer, RFCParameter parameter) throws Exception {
    if (isRequest) {
      setIsRequest(pojo, jgen, provider, writer, parameter);
    } else {
      setValueIfNotIsRequest(pojo, jgen, provider, writer, parameter);
    }
  }

  /**
   * Set value if it is a type of request
   *
   * @param pojo      Object
   * @param jgen      JsonGenerator
   * @param provider  SerializerProvider
   * @param writer    PropertyWriter
   * @param parameter RFCParameter
   * @throws Exception Exception
   */
  private void setIsRequest(Object pojo, JsonGenerator jgen, SerializerProvider provider,
      PropertyWriter writer, RFCParameter parameter) throws Exception {
    if (parameter.getIconType() == RFCParameter.IN_PARAMETER
        || parameter.getIconType() == RFCParameter.INOUT_PARAMETER) {
      writer.serializeAsField(pojo, jgen, provider);
    }
  }

  /**
   * Set value if it is a type of request
   *
   * @param pojo      Object
   * @param jgen      JsonGenerator
   * @param provider  SerializerProvider
   * @param writer    PropertyWriter
   * @param parameter RFCParameter
   * @throws Exception Exception
   */
  private void setValueIfNotIsRequest(Object pojo, JsonGenerator jgen, SerializerProvider provider,
      PropertyWriter writer, RFCParameter parameter) throws Exception {
    if (parameter.getIconType() == RFCParameter.OUT_PARAMETER
        || parameter.getIconType() == RFCParameter.INOUT_PARAMETER) {
      writer.serializeAsField(pojo, jgen, provider);
    }
  }

  /*    *//**
   * If return true then it would be serialized
   * @param writer BeanPropertyWriter
   * @return boolean
   *//*
    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }*/

  /**
   * If return true then it would be serialized
   *
   * @param writer BeanPropertyWriter
   * @return boolean
   */
  @Override
  protected boolean include(PropertyWriter writer) {
    return true;
  }
}

