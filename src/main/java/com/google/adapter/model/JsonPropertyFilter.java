package com.google.adapter.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.adapter.constants.AdapterConstants;
import java.util.ArrayList;
import java.util.List;

public class JsonPropertyFilter extends SimpleBeanPropertyFilter {

  /**
   * Setting fields that need to be serialized
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
    List<String> fields = new ArrayList<>();
    fields.add(AdapterConstants.CHILDREN);
    fields.add(AdapterConstants.COMPLEXTYPES);
    if (include(writer)) {
      addWriter(fields, writer, pojo, jgen, provider);
    } else if (!jgen.canOmitFields()) { // since 2.3
      writer.serializeAsOmittedField(pojo, jgen, provider);
    }
  }

  /**
   * @param fields   List
   * @param writer   PropertyWriter
   * @param pojo     Object
   * @param jgen     JsonGenerator
   * @param provider SerializerProvider
   * @throws Exception Exception
   */
  private void addWriter(List<String> fields,
      PropertyWriter writer, Object pojo,
      JsonGenerator jgen, SerializerProvider provider) throws Exception {
    if (!fields.contains(writer.getName())) {
      writer.serializeAsField(pojo, jgen, provider);
    } else if (pojo instanceof RFCMethod) {
      RFCMethod method = (RFCMethod) pojo;
      setIfSchema(method, writer, pojo, jgen, provider);
    }
  }

  /**
   * Set Value if it's Schema
   *
   * @param writer   PropertyWriter
   * @param pojo     Object
   * @param jgen     JsonGenerator
   * @param provider SerializerProvider
   * @throws Exception Exception
   */
  private void setIfSchema(RFCMethod method, PropertyWriter writer, Object pojo,
      JsonGenerator jgen, SerializerProvider provider) throws Exception {
    if (method.callType.equals(AdapterConstants.SCHEMA)) {
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
