/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adapter.exceptions.SystemException;
import com.sap.conn.jco.JCoTable;

/**
 *
 * IDOC control segment DC40 data definition
 */
@JsonInclude(Include.NON_NULL)
public class IDocSegmentDC40 extends IDocSegment {
    private static IDocSegmentDC40 DC40 = null;
    public static final String SEGMENT_TYPE = "EDI_DC40";
    private int idocNumber = 0;
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(IDocSegmentDC40.class.getName());

    private IDocSegmentDC40() {}

    private void init() {
        // create fields..
        DC40.SEGMENTTYP = SEGMENT_TYPE;
        DC40.MUSTFL = "X";
        DC40.OCCMAX = "1";
        ObjectMapper objectMapper = new ObjectMapper();
        List<IDocField> list;
        try {
            list = Arrays.asList(objectMapper.readValue(new File("src/main/resources/idoc.json"), IDocField[].class));
            list.stream().forEach(iDocField -> DC40.addField(iDocField));
        } catch (IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
    }

    private void initIdocCounter() {
        idocNumber = 1;
    }

    public static IDocSegmentDC40 getEDI_DC40() {
        if (DC40 == null) {
            DC40 = new IDocSegmentDC40();
            DC40.controlSeg = "true";
            DC40.init();
        }
        DC40.initIdocCounter();
        return DC40;
    }

    private static final String CHARACTER_LENGTH_10 = "%-10s";
    private static final String CHARACTER_LENGTH_14 = "%-14s";
    private static final String CHARACTER_LENGTH_30 = "%-30s";
    private static final String CHARACTER_LENGTH_70 = "%-70s";
    private static final String FIELD_CREDAT = "CREDAT";
    private static final String FIELD_CRETIM = "CRETIM";
    private static final String FIELD_DOCNUM = "DOCNUM";


    public static String getDC40FlatFromJCO(JCoTable table) {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        StringBuilder s = new StringBuilder();
        s.append(String.format(CHARACTER_LENGTH_10, table.getString("TABNAM"))); // , CHAR,
        // Name of Table
        // Structure,
        s.append(String.format("%-3s", table.getString("MANDT"))); // , CHAR, EDI4MANDT, 3, Client,
        s.append(String.format("%-16s", table.getString(FIELD_DOCNUM))); // , CHAR, EDI4DOCNUC, 16,
        // IDoc number,
        s.append(String.format("%-4s", table.getString("DOCREL"))); // , CHAR, EDI4DOCREL, 4, SAP
        // Release for IDoc,
        s.append(String.format("%-2s", table.getString("STATUS"))); // , CHAR, EDI4STATUS, 2, Status
        // of IDoc,
        s.append(String.format("%1s", table.getString("DIRECT"))); // , CHAR, EDI4DIRECT, 1,
        // Direction,
        s.append(String.format("%1s", table.getString("OUTMOD"))); // , CHAR, EDI4OUTMOD, 1, Output
        // mode,
        s.append(String.format("%1s", table.getString("EXPRSS"))); // , CHAR, EDI4EXPRSS, 1,
        // Overriding in inbound
        // processing,
        s.append(String.format("%1s", table.getString("TEST"))); // , CHAR, EDI4TEST, 1, Test flag,
        s.append(String.format(CHARACTER_LENGTH_30, table.getString("IDOCTYP"))); // , CHAR,
        // Name of basic
        // type,
        s.append(String.format(CHARACTER_LENGTH_30, table.getString("CIMTYP"))); // , CHAR,
        // Extension
        // (defined by
        // customer),
        s.append(String.format(CHARACTER_LENGTH_30, table.getString("MESTYP"))); // , CHAR,
        // Message type,
        s.append(String.format("%-3s", table.getString("MESCOD"))); // , CHAR, EDI4MESCOD, 3,
        // Message code,
        s.append(String.format("%-3s", table.getString("MESFCT"))); // , CHAR, EDI4MESFCT, 3,
        // Message Function,
        s.append(String.format("%1s", table.getString("STD"))); // , CHAR, EDI4STD, 1, EDI standard,
        // flag,
        s.append(String.format("%-6s", table.getString("STDVRS"))); // , CHAR, EDI4STDVRS, 6, EDI
        // standard, version and
        // release,
        s.append(String.format("%-6s", table.getString("STDMES"))); // , CHAR, EDI4STDMES, 6, EDI
        // message type,
        s.append(String.format(CHARACTER_LENGTH_10, table.getString("SNDPOR"))); // , CHAR,
        // Sender port (SAP
        // System,
        // external subsystem),
        s.append(String.format("%-2s", table.getString("SNDPRT"))); // , CHAR, EDI4SNDPRT, 2,
        // Partner type of sender,
        s.append(String.format("%-2s", table.getString("SNDPFC"))); // , CHAR, EDI4SNDPFC, 2,
        // Partner Function of
        // Sender,
        s.append(String.format(CHARACTER_LENGTH_10, table.getString("SNDPRN"))); // , CHAR,
        // Partner Number
        // of
        // Sender,
        s.append(String.format("%-21s", table.getString("SNDSAD"))); // , CHAR, EDI4SNDSAD, 21,
        // Sender address (SADR),
        s.append(String.format(CHARACTER_LENGTH_70, table.getString("SNDLAD"))); // , CHAR,
        // Logical address
        // of
        // sender,
        s.append(String.format(CHARACTER_LENGTH_10, table.getString("RCVPOR"))); // , CHAR,
        // Receiver port,
        s.append(String.format("%-2s", table.getString("RCVPRT"))); // , CHAR, EDI4RCVPRT, 2,
        // Partner Type of Receiver,
        s.append(String.format("%-2s", table.getString("RCVPFC"))); // , CHAR, EDI4RCVPFC, 2,
        // Partner function of
        // recipient,
        s.append(String.format(CHARACTER_LENGTH_10, table.getString("RCVPRN"))); // , CHAR,
        // EDI4RCVPRN, 10,
        // Partner Number
        // of
        // Receiver,
        s.append(String.format("%-21s", table.getString("RCVSAD"))); // , CHAR, EDI4RCVSAD, 21,
        // Recipient address
        // (SADR),
        s.append(String.format(CHARACTER_LENGTH_70, table.getString("RCVLAD"))); // , CHAR,
        // EDI4RCVLAD, 70,
        // Logical address
        // of
        // recipient,
        String d = "";
        try {
            d = df.format(table.getDate(FIELD_CREDAT));
        } catch (Exception ex) { // bad date input or null

            d = table.getString(FIELD_CREDAT);
            log.log(Level.WARNING, "Value passed " + table.getDate(FIELD_CREDAT)
                    + " for CREDAT field is incorrect. setting date as String " + d);
        }
        s.append(String.format("%8s", d)); // , DATE, EDI4CREDAT, 8, Created on,
        String t = "";
        try {
            t = tf.format(table.getTime(FIELD_CRETIM));
        } catch (Exception ex) { // bad date input or null
            log.log(Level.WARNING, "Value passed " + table.getDate(FIELD_CRETIM)
                    + " for CRETIM field is incorrect. setting Time as String");
            t = table.getString(FIELD_CRETIM);
        }
        s.append(String.format("%6s", t)); // , TIME, EDI4CRETIM, 6, Created at,
        s.append(String.format(CHARACTER_LENGTH_14, table.getString("REFINT"))); // , CHAR,
        // EDI4REFINT, 14,
        // Transmission
        // file (EDI
        // Interchange),
        s.append(String.format(CHARACTER_LENGTH_14, table.getString("REFGRP"))); // , CHAR,
        // EDI4REFGRP, 14,
        // Message group
        // (EDI
        // Message Group),
        s.append(String.format(CHARACTER_LENGTH_14, table.getString("REFMES"))); // , CHAR,
        // EDI4REFMES, 14,
        // Message (EDI
        // Message),
        s.append(String.format(CHARACTER_LENGTH_70, table.getString("ARCKEY"))); // , CHAR,
        // EDI4ARCKEY, 70,
        // Key for external
        // message
        // archive,
        s.append(String.format("%-20s", table.getString("SERIAL"))); // , CHAR, EDI4SERIAL, 20,
        // Serialization,
        return s.toString();
    }

    /**
     * Generate DC40 XML segment from RFC table entries
     * @param table JCo Table
     * @param okEmpty boolean type
     * @param idoc Idoc in the form of JsonNode
     * @param mapper ObjectMapper
     */
    public void setDC40JsonFromJCO(JCoTable table, boolean okEmpty, JsonNode idoc,
                                   ObjectMapper mapper) {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        JsonNode edidc40 = mapper.createObjectNode();

        for (IDocField f : DC40.fList) {
            String v;
            String t = table.getString(f.FIELDNAME);

            if (f.jcoType == 0) {
                v = t.trim();
            } else if (f.jcoType == 1) { // date
                try {
                    v = df.format(table.getDate(f.FIELDNAME));
                } catch (Exception ex) { // bad date input or null
                    v = t.trim();
                }
            } else if (f.jcoType == 3) { // time
                try {
                    v = tf.format(table.getTime(f.FIELDNAME));
                } catch (Exception ex) { // bad date input or null
                    v = t.trim();
                }
            } else {
                v = t.trim();
            }
            if (okEmpty || (v.length() > 0)) {
                ((ObjectNode) edidc40).put(f.FIELDNAME, v);
            }
        }

        ((ObjectNode) idoc).set(SEGMENT_TYPE, edidc40);
    }

    /**

     */
    /**
     * EDI_DC40 8000000000001758531700 3014 MATMAS01 MATMAS SAPEQ6
     * @param table JCo table
     * @param flat Flat type
     * @param idocNumbers List of Idoc Numbers
     * @return List of String
     */
    public List<String> setDC40FlatToJCO(JCoTable table, byte[] flat, List<String> idocNumbers) {
        int pos = 0;
        for (IDocField f : DC40.fList) {
            byte[] b = Arrays.copyOfRange(flat, pos, pos + f.EXTLEN);
            pos += f.EXTLEN;
            String v = (new String(b)).trim();
            if (f.FIELDNAME.equalsIgnoreCase(FIELD_DOCNUM)) {
                while (v.isEmpty()) {
                    if (!idocNumbers.contains(String.valueOf(idocNumber))) {
                        v = String.valueOf(idocNumber);
                        idocNumbers.add(v);
                        log.log(Level.INFO, "final value {0}", v);
                        break;
                    } else {
                        idocNumber = idocNumber + 1;
                    }
                }
            }
            if (v.length() > 0) {
                table.setValue(f.FIELDNAME, v);
            }
        }
        return idocNumbers;
    }
}
