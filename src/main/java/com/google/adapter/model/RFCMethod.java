/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JCO RFC Method model
 *
 * @author dhiraj.kumar
 */

@JsonPropertyOrder({"name", "group", "description", "application"/*, "keyRFC"*/})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RFCMethod {

  @JsonIgnore
  public String callType = "";
  @JsonIgnore
  public Map<String, JcoElement> complexTypes;
  private List<RFCParameter> children;
  private String function;
  private String group;
  private String shortText = "";
  @JsonIgnore
  private String devClass;
  private Character application;
//  private String keyRFC = "";

  /**
   * Public default constructor of RFC Method
   */
  public RFCMethod(){}

  /**
   * Public constructor of RFC Method to set value initially
   *
   * @param functionName Function Name
   * @param shortText    Identifier of that Function Name
   * @param callType     Type of RFC
   */
  public RFCMethod(String functionName, String shortText, String callType) {
    this(functionName, "", shortText, "", "");
    this.callType = callType;
  }


  /**
   * Public constructor of RFC Method to set value initially
   * @param functionName Function Name
   * @param groupName    Group name of RFC
   * @param shortText    Identifier of that Function Name
   * @param devClass     Class of RFC
   * @param keyRFC RFC name as key
   */
  public RFCMethod(String functionName, String groupName, String shortText, String devClass, String keyRFC) {
    this.function = functionName;
    this.group = groupName;
    this.shortText = shortText;
    this.devClass = devClass;
//    this.keyRFC = keyRFC;
  }

  /**
   * @return function
   */
  @JsonProperty("name")
  public String getFunction() {
    return function;
  }

  /**
   * @return application
   */
  public Character getApplication() {
    return application;
  }

  /**
   * Set Application
   *
   * @param application application
   */
  public void setApplication(Character application) {
    this.application = application;
  }

  /**
   * @return group
   */
  @JsonProperty("group")
  public String getGroup() {
    return group;
  }

  /**
   * @return shortText
   */
  @JsonProperty("description")
  public String getShortText() {
    return shortText;
  }

  /**
   * @return devClass
   */
  public String getDevClass() {
    return devClass;
  }

  /**
   * String object of RFCMethod
   *
   * @return toString object of RFCMethod
   */
  @Override
  public String toString() {
    return "RFC ["
            + "functionName="
            + getFunction()
            + ", groupName="
            + getGroup()
            + ", shortText="
            + getShortText()
            + ", devClass="
            + getDevClass();
  }



  /**
   * @return children
   */
  @JsonProperty("properties")
  public List<RFCParameter> getChildren() {
    return children;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RFCMethod rfcMethod = (RFCMethod) o;
    return function.equals(rfcMethod.function) &&
            group.equals(rfcMethod.group);
  }

  @Override
  public int hashCode() {
    return Objects.hash(function, group);
  }

  /**
   * Create hierarchy of RFC
   *
   * @param childObject Child object of RFCParameter
   */
  public void addChild(RFCParameter childObject) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(childObject);
  }

  /**
   * Get Type of RFC
   *
   * @param name Name of RFC
   * @param type Type of RFC
   */
  public void addType(String name, JcoElement type) {
    if (complexTypes == null) {
      complexTypes = new HashMap<>();
    }
    addExistingType(name, type);
  }

  /**
   * Add if already present
   *
   * @param name Name of RFC
   * @param type Type of RFC
   */
  private void addExistingType(String name, JcoElement type) {
    if (!complexTypes.containsKey(name)) {
      complexTypes.put(name, type);
    }
  }

  /**
   * Method to return name
   *
   * @return name
   */
  public String getName() {
    return function;
  }


  @JsonIgnore
  private boolean inOnly = false;
  /**
   * @return the inOnly
   */
  public boolean isInOnly() {
    return inOnly;
  }

  /**
   * @param inOnly the inOnly to set
   */
  public void setInOnly(boolean inOnly) {
    this.inOnly = inOnly;
  }
  /*
   * Method to return keyRFC
   *
   * @return keyRFC
   */
/*  @JsonProperty("keyRFC")
  public String getKeyRFC() {
    return keyRFC;
  }*/
}
