/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoRecordMetaData;

/**
 * Basic data element in SAP JCO
 *
 * @author prashant.singh
 */
public class JcoElement {
    private String name;
    private String description;
    private String typeName;
    private String recordType;
    private String lineType;
    private String lineDataTypeAsString;
    private String jsonPattern = null;
    private String multipleOf = null;
    private String jsontype = null;

    private int byteLength;
    private int byteOffset; // for record element
    private int decimals;
    private int length;
    private int type;
    private int lineDataType;
    private int lineTypeLength;
    private int unicodeByteLength;

    private boolean isNestedType1Structure;
    private boolean isStructure;
    private boolean isTable;
    private boolean optional;
    private boolean isLineType = false;

    private Logger mLog = Logger.getLogger(getClass().getName());
    private List<JcoElement> fields; // structure/table elements

    public JcoElement(JCoMetaData data, int idx) {
        if (data == null) return;

        name = data.getName(idx);
        if (name == null) {
            lineType = "lineType";
            lineDataType = data.getType(idx);
        }

        commonInitializer(data, idx);

        if (data instanceof com.sap.conn.jco.JCoListMetaData) {
            optional = ((com.sap.conn.jco.JCoListMetaData) data).isOptional(idx);
        }

        if (isStructure || isTable) { // fill fields...
            fields = new ArrayList<>();
            JCoRecordMetaData list = data.getRecordMetaData(idx);
            int size = (list != null) ? list.getFieldCount() : 0;
            lineType = list.getLineType();
            for (int i = 0; i < size; i++) {
                if (lineType != null) {
                    lineDataType = list.getType(i);
                } else {
                    JcoElement field = new JcoElement(list, i);
                    fields.add(field);
                }
            }
        } else {
            if (data instanceof JCoRecordMetaData) {
                lineType = ((JCoRecordMetaData) data).getLineType();
            }
        }
    }

    public JcoElement(JCoMetaData data, int idx, java.util.Map map, String parent) {
        if (data == null) {
            return;
        }
        name = data.getName(idx);
        if (name != null && name.isEmpty()) {
            lineType = "lineType";
            lineDataType = data.getType(idx);
        }
        commonInitializer(data, idx);

        if (data instanceof com.sap.conn.jco.JCoListMetaData) {
            optional = ((com.sap.conn.jco.JCoListMetaData) data).isOptional(idx);
        } else if (parent != null && map.containsKey(parent + "|" + name)) {
            optional = !((String) map.get(parent + "|" + name)).trim().equalsIgnoreCase("X");
        }

        if (isStructure || isTable) { // fill fields...
            fields = new ArrayList<>();
            JCoRecordMetaData list = data.getRecordMetaData(idx);
            int size = (list != null) ? list.getFieldCount() : 0;
            for (int i = 0; i < size; i++) {
                JcoElement field = null;
                // prashant
                if (list.getName(i).isEmpty()) {
                    lineType = "lineType";
                    lineDataType = list.getType(i);
                    lineDataTypeAsString = list.getTypeAsString(i);
                    lineTypeLength = list.getLength(i);
                    field = new JcoElement(list, i, map, recordType);
                } else {
                    field = new JcoElement(list, i, map, recordType);
                }
                fields.add(field);
            }
        }
    }

    /**
     * Initialize common data in between overridden constructors
     * @param data
     * @param index
     */
    private void commonInitializer(JCoMetaData data, int index) {
        byteLength = data.getLength(index);
        decimals = data.getDecimals(index);
        description = data.getDescription(index);
        length = data.getLength(index);
        typeName = data.getTypeAsString(index);
        type = data.getType(index);
        recordType = data.getRecordTypeName(index);
        unicodeByteLength = data.getUnicodeByteLength(index);
        isNestedType1Structure = data.isNestedType1Structure(index);
        isStructure = data.isStructure(index);
        isTable = data.isTable(index);
        calculateJsonType();
        byteOffset = (data instanceof JCoRecordMetaData) ?
            ((JCoRecordMetaData) data).getByteOffset(index) : 0;
    }

    @JsonProperty("required")
    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @JsonIgnore
    public String getLineType() {
        return lineType;
    }

    @JsonIgnore
    public int getLineDataType() {
        return lineDataType;
    }

    @JsonIgnore
    public int getLineTypeLength() {
        return lineTypeLength;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the byteLength
     */
    @JsonIgnore
    public int getByteLength() {
        return byteLength;
    }

    /**
     * @return the decimals
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @return the typeName
     */
    @JsonIgnore
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return the recordType
     */
    @JsonIgnore
    public String getRecordType() {
        return recordType;
    }

    /**
     * @return the unicodeByteLength
     */
    @JsonIgnore
    public int getUnicodeByteLength() {
        return unicodeByteLength;
    }

    /**
     * @return the isNestedType1Structure
     */
    @JsonIgnore
    public boolean isNestedType1Structure() {
        return isNestedType1Structure;
    }

    /**
     * @return the isStructure
     */
    @JsonIgnore
    public boolean isStructure() {
        return isStructure;
    }

    /**
     * @return the isTable
     */
    @JsonIgnore
    public boolean isTable() {
        return isTable;
    }

    @JsonProperty("type")
    public String getJsonType() {
        return jsontype;
    }

    public String getJsonPattern() {
        return jsonPattern;
    }

    public String getMultipleOf() {
        return multipleOf;
    }

    public void calculateJsonType() {
        int type = this.getType();
        /*
         * x TYPE_CHAR 0 string .. x TYPE_DATE 1 date .. x TYPE_BCD 2 decimal .. x TYPE_TIME 3 time
         * .. x TYPE_BYTE 4 base64Binary.. TYPE_ITAB 5 x TYPE_NUM 6 string .. x TYPE_FLOAT 7 double
         * x TYPE_INT 8 int x TYPE_INT2 9 short x TYPE_INT1 10 byte x TYPE_STRUCTURE 17 TYPE_DECF16
         * 23 TYPE_DECF34 24 x TYPE_STRING 29 string x TYPE_XSTRING 30 base64Binary ..
         * TYPE_EXCEPTION 98 TYPE_TABLE 99 TYPE_INVALID -1 UNINITIALIZED -1
         */
        switch (type) {
            case JCoMetaData.TYPE_CHAR: // 0
                jsontype = "string";
                break;
            case JCoMetaData.TYPE_DATE: // 1
                jsontype = "date_time";
                break;
            case JCoMetaData.TYPE_BCD: // 2
                jsontype = "number";
                break;
            case JCoMetaData.TYPE_TIME: // 3
                jsontype = "date_time";
                break;
            case JCoMetaData.TYPE_BYTE: // 4
                jsontype = "base64string";
                break;
            case JCoMetaData.TYPE_NUM: // 6
                jsontype = "number";
                // jsonPattern = "^[0-9]$";
                break;
            case JCoMetaData.TYPE_FLOAT: // 7
                jsontype = "number";
                // multipleOf="0.01";
                break;
            case JCoMetaData.TYPE_INT: // 8
                jsontype = "integer";
                break;
            case JCoMetaData.TYPE_INT2: // 9
                jsontype = "integer";
                break;
            case JCoMetaData.TYPE_INT1: // 10
                // Fix for Bug : 16976912 INT1(1-BYTE INTEGER) DATA TYPE ISSUE by Sachin Starts
                // str = "<xsd:simpleType><xsd:restriction base=\"xsd:byte\">"
                jsontype = "integer";
                break;
            case JCoMetaData.TYPE_STRUCTURE: // 17
                jsontype = "object";
                break;
            case JCoMetaData.TYPE_TABLE: // 17
                jsontype = "array";
                break;
            case JCoMetaData.TYPE_STRING: // 29
                jsontype = "string";
                break;
            case JCoMetaData.TYPE_XSTRING: // 30
                jsontype = "base64string";
                break;
            // Fix for Bug : 16082273 : XSD CONTAINS ERRORS FOR DECFLOAT16 AND DECFLOAT 32
            // DATA TYPES by Sachin Starts
            case JCoMetaData.TYPE_DECF16: // 23
                jsontype = "string";
                jsonPattern = "[01]+$";
                break;
            case JCoMetaData.TYPE_DECF34: // 24
                jsontype = "string";
                jsonPattern = "[01]+$";
                break;
            // Fix for Bug : 16082273 : XSD CONTAINS ERRORS FOR DECFLOAT16 AND DECFLOAT 32
            // DATA TYPES by Sachin ends
            default:
                jsontype = "string";
                break;
        }
    }

    // Method FOR Bug 16977030 - PACKED (ABAP TYPE P) DATA TYPE ISSUE
    // geting the maxInclusive for a given decimal and fraction
    // ### Prashant
    // BCD DAT TYPE Supports Decimal number. with 0 decimal places
    // Decimal number. with >0 decimal places
    public String calMaxInclusive(JcoElement elem, boolean isMax) {
        int i = ((elem.getByteLength() * 2 - 1) - elem.getDecimals());
        int k = elem.getDecimals();
        String nines = "9999999999999999999999999999999";
        String negativeNines = "-9999999999999999999999999999999";
        String maxInclusive = nines.substring(0, i) + "." + nines.substring(0, k);
        String minInclusive = "-" + maxInclusive;
        if (isMax) {
            return maxInclusive;
        }
        return minInclusive;
    }

    /**
     * @return the fields
     */
    @JsonProperty("properties")
    public List<JcoElement> getFields() {
        return fields;
    }

    /**
     * @return the display Name
     */
    public String getDisplayName() {
        return name;
    }

    public String toString() {
        return "JCoElement : " + name + " | lineType : " + lineType + "  | RecordType | "
            + recordType + " lineDataTypeAsString : " + lineDataTypeAsString + "Size : "
            + fields;
    }
}
