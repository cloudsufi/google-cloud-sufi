/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.adapter.util.StrUtil;
import com.sap.conn.idoc.IDocSegmentMetaData;
import com.sap.conn.jco.JCoTable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * IDOC Segment structure
 */
@JsonIgnoreProperties({"NR", "SEGMENTDEF", "QUALIFIER", "SEGLEN", "PARPNO", "MUSTFL", "GRP_OCCMIN",
        "GRP_MUSTFL", "GRP_OCCMAX", "OCCMAX", "REFSEGTYP", "userInput", "PARSEG", "PARFLG"})
@JsonPropertyOrder({"name", "type", "description", "isLeaf", "required", "controlSeg",
        "properties"})
@JsonInclude(Include.NON_NULL)
public class IDocSegment {
    public int NR; // Sequential Number of Segment in IDoc Type
    @JsonProperty("name")
    public String SEGMENTTYP; // Segment type in 30-character format
    public String SEGMENTDEF; // IDoc Development: Segment definition
    public String QUALIFIER; // Flag: Qualified segment in IDoc
    public int SEGLEN; // Length of Field (Number of Characters)
    public String PARSEG; // Segment type in 30-character format
    public int PARPNO; // Sequential number of parent segment
    public String PARFLG; // Flag for parent segment: Segment is start of segment group
    public String controlSeg;
    public String MUSTFL; // Flag: Mandatory entry
    @JsonIgnore
    public int OCCMIN; // Minimum number of segments in sequence
    public boolean required;
    public String OCCMAX; // Maximum number of segments in sequence
    @JsonProperty("type")
    public String jsonType;
    @JsonIgnore
    public int HLEVEL; // Hierarchy level of IDoc type segment
    @JsonProperty("description")
    public String DESCRP; // Short description of object
    public String GRP_MUSTFL; // Flag for groups: Mandatory
    public int GRP_OCCMIN; // Minimum number of groups in sequence
    public String GRP_OCCMAX; // Maximum number of groups in sequence
    public String REFSEGTYP; // Segment type in 30-character format
    public List<Object> fields = new ArrayList<Object>();
    int max = 0;
    String userInput = "";
    List<IDocField> fList = new ArrayList();
    List<IDocSegment> sList = null; // child segments
    Map<String, IDocField> fMap = new HashMap(); // FIELDNAME (AGENCYNUM) to field
    char[] SDATA; // segment's sdata decode string
    byte[] SVALUE; // encoded value, e.g., new String(SVALUE, charset);

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(IDocSegment.class.getName());

    public IDocSegment() {
        // create an empty IDoc segment..
    }

    public IDocSegment(JCoTable segTable, int i) {
        segTable.setRow(i);
        NR = segTable.getInt("NR");
        SEGMENTTYP = segTable.getString("SEGMENTTYP");
        SEGMENTDEF = segTable.getString("SEGMENTDEF");
        QUALIFIER = segTable.getString("QUALIFIER");
        SEGLEN = segTable.getInt("SEGLEN");
        PARSEG = segTable.getString("PARSEG");
        PARPNO = segTable.getInt("PARPNO");
        PARFLG = segTable.getString("PARFLG");
        MUSTFL = segTable.getString("MUSTFL");
        OCCMIN = segTable.getInt("OCCMIN");
        required = !MUSTFL.isEmpty();
        OCCMAX = segTable.getString("OCCMAX"); // can be 9999999999
        if (Integer.parseInt(OCCMAX) > 1) {
            jsonType = "array";
        } else {
            jsonType = "object";
        }
        HLEVEL = segTable.getInt("HLEVEL");
        DESCRP = segTable.getString("DESCRP");
        GRP_MUSTFL = segTable.getString("GRP_MUSTFL");
        GRP_OCCMIN = segTable.getInt("GRP_OCCMIN");
        GRP_OCCMAX = segTable.getString("GRP_OCCMAX"); // can be 9999999999
        REFSEGTYP = segTable.getString("REFSEGTYP");
        try {
            max = Integer.parseInt(OCCMAX);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Value passed " + OCCMAX
                    + " is  incorrect. setting max occurrence to 0 for IDOC Segment  ", ex);
            max = 0;
        }
    }

    public IDocSegment(IDocSegmentMetaData segTable) {
        SEGMENTTYP = segTable.getType();
        SEGMENTDEF = segTable.getDefinition();
        QUALIFIER = segTable.isQualified() ? "X" : "";
        SEGLEN = segTable.getRecordMetaData().getRecordLength();
        PARSEG = (segTable.getParent() != null) ? segTable.getParent().getType() : "";

        PARFLG = segTable.isParent() ? "X" : "";
        MUSTFL = segTable.isMandatory() ? "X" : "";
        OCCMIN = (int) segTable.getMinOccurrence();
        max = (int) segTable.getMaxOccurrence(); // can be 9999999999
        OCCMAX = String.valueOf(max);
        required = !MUSTFL.isEmpty();
        if (Integer.parseInt(OCCMAX) > 1) {
            jsonType = "array";
        } else {
            jsonType = "object";
        }
        HLEVEL = segTable.getHierarchyLevel();
        DESCRP = segTable.getDescription();

        try {
            max = Integer.parseInt(OCCMAX);
        } catch (Exception ex) {
            max = 0;
            log.log(Level.WARNING, "Value passed " + OCCMAX
                    + " is  incorrect. setting max occurrence to 0 for IDOC Segment  ", ex);
        }
    }

    /**
     * @return the optional
     */
    @JsonIgnore
    public boolean isOptional() {
        return ((MUSTFL == null) || (MUSTFL.trim().length() == 0));
    }

    /**
     * @return the table
     */
    @JsonIgnore
    public boolean isTable() {
        return (max > 1);
    }

    @JsonProperty("isLeaf")
    public boolean isLeaf() {
        // only valid after parsing the segment table
        return (fList.size() > 0);
    }

    public void addSegment(IDocSegment child) {
        if (sList == null) {
            sList = new ArrayList<>();
        }
        sList.add(child);
    }

    @JsonIgnore
    public List<IDocSegment> getSegments() {
        return sList;
    }

    public void addField(IDocField child) {
        fList.add(child);
        fMap.put(child.FIELDNAME, child);
    }

    @JsonIgnore
    public List<IDocField> getFields() {
        return fList;
    }

    public IDocField getField(String name) {
        return fMap.get(name);
    }

    @JsonIgnore
    public String getMaxValue(String s) {
        try {
            int val = Integer.valueOf(s);
            if (val < 5000) { // xsd limit
                return "" + val; // trim 0's
            }
            return "unbounded";
        } catch (Exception ex) {
            // not in range...
        }
        if (s.endsWith("999")) {
            return "unbounded";
        }
        return s;
    }

    @JsonProperty("properties")
    public List<Object> getAllJSonProperties() {
        List<Object> properties = new ArrayList<>();
        for (IDocField field : getFields()) {
            properties.add(field);
        }
        if (getSegments() != null) {
            for (IDocSegment seg : getSegments()) {
                properties.add(seg);
            }
        }
        return properties;
    }

    public static String getSegmentFlatFromJCO(JCoTable table, IDocSegment seg) {
        StringBuilder s = new StringBuilder();
        int segLegth = seg.SEGLEN;
        s.append(String.format("%-30s", table.getString("SEGNAM"))); // , CHAR, EDI4SEGNAM, 30,
        s.append(String.format("%-3s", table.getString("MANDT"))); // , CHAR, EDI4MANDT, 3, Client,
        s.append(String.format("%-16s", table.getString("DOCNUM"))); // , CHAR, EDI4DOCNUC, 16, IDoc
        s.append(String.format("%-6s", table.getString("SEGNUM"))); // , CHAR, EDI4SEGNUC, 6,
        s.append(String.format("%-6s", table.getString("PSGNUM"))); // , NUM, EDI4PSGNUC, 6, Number
        s.append(String.format("%-2s", table.getString("HLEVEL"))); // , CHAR, EDI4HLEVEC, 2,
        s.append(String.format("%-" + segLegth + "s", table.getString("SDATA"))); // , CHAR,
        return s.toString();
    }

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

    /**

     */
    /**
     * Generate a segment XML from the SDATA element
     * @param table JCo Table
     * @param okEmpty Boolean value
     * @param currentNode Object node value
     * @return  segment in the form of Object
     */
    public Object getSegmentJsonFromJCO(JCoTable table, boolean okEmpty, ObjectNode currentNode) {

        JsonNode segNode = null;
        // get segment data
        String sdata = table.getString("SDATA");
        int size = sdata.length();
        for (IDocField fl : fList) {

            String v;
            int fTo = 0;

            fTo = fl.BYTE_FIRST + fl.EXTLEN;
            if (fTo > size) {
                fTo = size;
            }
            if (fl.BYTE_FIRST >= size) {
                break;
            }
            String t = sdata.substring(fl.BYTE_FIRST, fTo);
            if (fl.jcoType == 1) { // date
                try {
                    v = df.format(t);
                } catch (Exception ex) { // bad date input or null
                    v = t.trim();
                }
            } else if (fl.jcoType == 3) { // time
                try {
                    v = tf.format(t);
                } catch (Exception ex) { // bad date input or null
                    v = t.trim();
                }
            } else {
                v = t.trim();
            }

            if (okEmpty || (v.length() > 0)) {
                currentNode.put(fl.FIELDNAME, v);
            }
        }
        return segNode;
    }

    public void initSData() {
        SDATA = new char[1000];
        Arrays.fill(SDATA, ' ');
    }

    @JsonIgnore
    public String getSData() {
        return new String(SDATA);
    }

    public void fillSData(String name, String value) {

        IDocField f = getField(name);

        if (value != null) {
            int len = value.length();
            if ((len > 0) && f != null) {
                StringBuilder sb = StrUtil.removeEncodeInvalidControlChars(value, false);
                if (sb != null)
                    value = sb.toString();
                if (f.EXTLEN < len) { // len is too big..
                    len = f.EXTLEN;
                }
                for (int i = 0; i < len; i++) {
                    SDATA[f.BYTE_FIRST + i] = value.charAt(i);
                }

            }
        }

    }
}
