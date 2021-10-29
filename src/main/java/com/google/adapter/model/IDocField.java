/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package com.google.adapter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.adapter.constants.AdapterConstants;
import com.sap.conn.idoc.IDocRecordMetaData;

/**
 * Pojo class to store IDOC field information
 */
@JsonIgnoreProperties({"SEGMENTTYP", "INTLEN", "FIELD_POS", "BYTE_FIRST", "BYTE_LAST", "DATATYPE",
        "jcoType", "testValue"})
@JsonInclude(Include.NON_NULL)
public class IDocField {

    private static final String STRING = "string";

    public String SEGMENTTYP;
    @JsonProperty("name")
    public String FIELDNAME;
    public int INTLEN;
    public String minLength = "0";
    @JsonProperty("maxLength")
    public int EXTLEN;
    public int FIELD_POS;
    public int BYTE_FIRST;
    public int BYTE_LAST;
    public String DATATYPE;
    public String DESCRP;
    public String hint;
    public boolean optional;
    public int jcoType;

    @JsonProperty("type")
    public String jsonType = STRING;
    private String testValue;
    public IDocField(){}


    public IDocField(IDocRecordMetaData fldTable, int i) {
        FIELDNAME = fldTable.getName(i);
        INTLEN = fldTable.getInternalLength(i);
        EXTLEN = fldTable.getLength(i);
        FIELD_POS = i;
        BYTE_FIRST = fldTable.getOffset(i);
        DATATYPE = fldTable.getTypeAsString(i);
        DESCRP = fldTable.getDescription(i);
        jcoType = getType();
        optional = true;
        hint = DESCRP;
    }
    
    public IDocField(String FIELDNAME, int EXTLEN, String DATATYPE, String DESCRP) {
        this.FIELDNAME = FIELDNAME;
        this.EXTLEN = EXTLEN;
        this.DATATYPE = DATATYPE;
        this.DESCRP = DESCRP;
        this.hint = DESCRP;
        jcoType = getType();
        optional = true;
    }

    private int getType() {
        if (DATATYPE == null) {
            jsonType = STRING;
            return 0;
        }
        if (DATATYPE.equals(STRING)) {
            jsonType = AdapterConstants.CHAR;
            return 0;
        }
        if (DATATYPE.equalsIgnoreCase(AdapterConstants.DATE)) {
            jsonType = AdapterConstants.DATE;
            return 1;
        }
        if (DATATYPE.equals(AdapterConstants.TIME)) {
            jsonType = AdapterConstants.DATETIME;
            return 3;
        }
        return 0;
    }
}
