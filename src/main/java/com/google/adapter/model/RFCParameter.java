/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sap.conn.jco.JCoListMetaData;

/**
 * JCO RFC Parameter Model
 */
@JsonFilter("ReqResFilter")
@JsonPropertyOrder({"name", "displayName", "defaultValue", "required", "length", "decimals",
        "description", "type", "properties"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RFCParameter extends JcoElement {
    public static final int BAPI_PARAMETER = 0;
    public static final int RFC_PARAMETER = 1;
    public static final int IDOC_PARAMETER = 2;
    public static final int IN_PARAMETER = 0;
    public static final int OUT_PARAMETER = 1;
    public static final int INOUT_PARAMETER = 2;

    @JsonProperty("required")
    private boolean optional;
    private String defaultValue;
    @JsonIgnore
    private String parmType; // imports, change, exports, tables
    @JsonIgnore
    private String elemType; // in, out,
    @JsonIgnore
    private int sapType; // BAPI, RFC, IDOC?
    @JsonIgnore
    private int iconType; // 0-IN, 1-Out, 2-INout

    public RFCParameter(JCoListMetaData data, int idx, String parmType, int sapType) {
        super(data, idx);
        initializer(data, idx, parmType, sapType);
    }

    public RFCParameter(JCoListMetaData data, int idx, String parmType, int sapType,
                        java.util.Map map, String parent) {
        super(data, idx, map, parent);
        initializer(data, idx, parmType, sapType);
    }

    /**
     * Initialize common values for both constructors
     *
     * @param data
     * @param idx
     * @param parmType
     * @param sapType
     */
    private void initializer(JCoListMetaData data, int idx, String parmType, int sapType) {
        this.parmType = parmType;
        this.sapType = sapType;
        this.defaultValue = data.getDefault(idx);
        this.optional = data.isOptional(idx);
        setIconType(parmType);
    }

    /**
     * Set the element type
     * @param etype Element Type
     */
    public void setElemType(String etype) {
        elemType = etype;
        setIconType(getElemType());
    }

    /**
     * This method sets value for iconType as per provided input data
     * @param type param type
     * @return value for iconType
     */
    private int setIconType(String type) {
        if (type != null) {
            if (type.equalsIgnoreCase("I")) {
                iconType = 0;
            } else if (type.equalsIgnoreCase("E")) {
                iconType = 1;
            } else {
                iconType = 2;
            }
        }
        return iconType;
    }

    /**
     * @return the parameter icon type: 0 - input 1 - output 2 - inout
     */
    public int getIconType() {
        return iconType;
    }

    /**
     * @return the parmType
     */
    public String getParmType() {
        return parmType;
    }

    /**
     * @return the elemType
     */
    public String getElemType() {
        return elemType;
    }

    /**
     * @return the sapType
     */
    public int getSapType() {
        return sapType;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }
}
