package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.IDocType;
import com.google.adapter.model.JcoElement;
import com.google.adapter.model.RFCParameter;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ReadErrorPropertyFile;
import com.sap.conn.jco.*;
import com.sap.conn.jco.util.Codecs;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dhiraj.kumar
 */
public class SAPDataConverter {


    private static final ThreadLocal<SimpleDateFormat> threadedDateFormatter =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocal<SimpleDateFormat> threadedTimeFormatter =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));
    private static Map<String, Map<String, JcoElement>> funCache = new HashMap<>();
    private final SAPConnector sapConn;
    private static Map<String, IDocType> idocStore = new HashMap<>();

    private final ErrorCapture errorCapture;
    /**
     * Public constructor for SAPDataConverter
     *
     * @param sapConn      SAPConnector
     * @param errorCapture ErrorCapture
     */
    public SAPDataConverter(SAPConnector sapConn, ErrorCapture errorCapture) {
        this.sapConn = sapConn;
        this.errorCapture = errorCapture;
    }

    /**
     * Handle byte arrays
     *
     * @param string String
     * @return byte array
     */
    public byte[] decodeBytes(String string) {
        return new String(Codecs.Base64.decode(string)).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get internal rfc name from BAPI
     *
     * @param bapiName Bapi Name
     * @return RFC Name
     * @throws JCoException JCoException
     */
    public String getRfcForBapi(String bapiName) throws JCoException {
        String boType = getBoType(bapiName.substring(0, bapiName.indexOf('.')));
        if (boType != null) {
            return getBoVerb(boType, bapiName.substring(bapiName.indexOf('.') + 1));
        }
        return null;
    }

    /**
     * Get Business name from BAPI
     *
     * @param boName Business Name
     * @return Type Name
     * @throws JCoException JCoException
     */
    public String getBoType(String boName) throws JCoException {
        JCoFunction function = sapConn.getRepository().getFunction(AdapterConstants.SWO_QUERY_API_OBJTYPES);
        if (function == null) {
            throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
                    .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
        }
        function.getImportParameterList().setValue(AdapterConstants.OBJECT_NAME, boName);
        function.execute(sapConn.getDestination());
        JCoTable vt = function.getTableParameterList().getTable(AdapterConstants.OBJTYPES);
        return retBoType(vt);
    }

    /**
     * Checks whether Jco Table is empty or not
     *
     * @param vt JCoTable
     * @return String
     */
    public String retBoType(JCoTable vt) {
        if (vt.getNumRows() > 0) {
            return vt.getString(
                    AdapterConstants.OBJTYPE);
        } else {
            return null;
        }
    }

    /**
     * Get Verb from BAPI name
     *
     * @param boType Business Name
     * @param verb   Business Verb from BAPI Name
     * @return Verb
     * @throws JCoException JCoException
     */
    public String getBoVerb(String boType, String verb) throws JCoException {
        JCoFunction function = sapConn.getRepository().getFunction(AdapterConstants.SWO_QUERY_OBJTYPE_INFO);
        if (function == null) {
            throw SystemException.throwException(ReadErrorPropertyFile.getInstance().getErrorDetails(Integer.toString(AdapterConstants.JCO_NOT_FOUND))
                    .getSecondaryMessage(), new AdapterException(Integer.toString(AdapterConstants.JCO_NOT_FOUND)));
        }
        function.getImportParameterList().setValue(AdapterConstants.OBJTYPE, boType);
        function.execute(sapConn.getDestination());
        JCoTable vt = function.getTableParameterList().getTable(AdapterConstants.VERBS);
        for (int i = 0; i < vt.getNumRows(); i++) {
            vt.setRow(i);
            String edit = vt.getString(AdapterConstants.EDITELEM);
            if ((edit == null) || (edit.equalsIgnoreCase(verb))) {
                return vt.getString(AdapterConstants.ABAPNAME);
            }
        }
        return null;
    }

    /**
     * Set JCo request from Json
     * @param function JCoFunction
     * @param payload JsonNode
     * @return JCoFunction
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     * @throws JCoException JCoException
     */
    public JCoFunction setJCoRequestFromJson(JCoFunction function, JsonNode payload) throws UnsupportedEncodingException, JCoException {
        Map<String, JcoElement> map = getFunctionMap(function);
        JCoParameterList[] list = new JCoParameterList[4];
        list[0] = function.getImportParameterList();
        list[1] = function.getChangingParameterList();
        list[2] = function.getTableParameterList();
        list[3] = function.getExportParameterList();
        Iterator<Map.Entry<String, JsonNode>> entryItr = payload.fields();
        while (entryItr.hasNext()) {
            Map.Entry<String, JsonNode> entry = entryItr.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            RFCParameter elm = (RFCParameter) map.get(key);
            if (elm != null) {
                String pType = elm.getTypeName();
                String pTypeName = elm.getRecordType();
                int idx = getParmListIndex(elm.getParmType());
                switch (pType) {
                    case AdapterConstants.STYPE_STRUCTURE:
                        JCoStructure struct = list[idx].getStructure(key);
                        procStructureList(struct, value, key, pTypeName);
                        break;
                    case AdapterConstants.STYPE_TABLE:
                        JCoTable table = list[idx].getTable(key);
                        procTableList(table, value, key, pTypeName);
                        break;
                    case AdapterConstants.STYPE_BYTE:
                        list[idx].setValue(key, decodeBinaryBytes(value.asText()));
                        break;
                    case AdapterConstants.STYPE_XSTRING:
                        list[idx].setValue(key, decodeBytes(value.asText()));
                        break;
                    default:
                        list[idx].setValue(key, value.asText());
                        break;
                }
            }
        }
        return function;
    }

    /**
     * Get Function Map
     * @param function JCoFunction
     * @return Map
     */
    private Map<String, JcoElement> getFunctionMap(JCoFunction function) {
        String name = function.getName();
        Map<String, JcoElement> map = funCache.get(name);
        if (map == null) {
            map = new HashMap<>();
            procMeatdataList(function.getImportParameterList(), map, AdapterConstants.I, 0);
            procMeatdataList(function.getChangingParameterList(), map, AdapterConstants.C, 0);
            procMeatdataList(function.getTableParameterList(), map, AdapterConstants.T, 0);
            procMeatdataList(function.getExportParameterList(), map, AdapterConstants.E, 0);
            funCache.put(name, map);
        }
        return map;
    }

    /**
     * MetaData list
     * @param list JCoParameterList
     * @param map Map
     * @param parmType Parameter Type
     * @param sapType SAP Type
     */
    private void procMeatdataList(JCoParameterList list, Map<String, JcoElement> map,
                                  String parmType, int sapType) {
        if (list != null) {
            JCoListMetaData mList = list.getListMetaData();
            int size = (mList != null) ? mList.getFieldCount() : 0;
            for (int i = 0; i < size; i++) {
                RFCParameter elm = new RFCParameter(mList, i, parmType, sapType);
                map.put(elm.getName(), elm);
            }
        }
    }

    /**
     * Decode byte String
     * @param v String
     * @return byte array
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    private byte[] decodeBinaryBytes(String v) throws UnsupportedEncodingException {
        String originalStr = new String(Codecs.Base64.decode(v));
        return originalStr.getBytes(AdapterConstants.UTF_8);
    }

    /**
     * Structure List
     * @param struct JCoStructure
     * @param parent JsonNode
     * @param pName String
     * @param typeName String
     * @throws JCoException JCoException
     */
    private void procStructureList(JCoStructure struct, JsonNode parent, String pName,
                                   String typeName) throws JCoException {
        JCoRecordMetaData mList = sapConn.getRepository().getRecordMetaData(typeName);
        Iterator<Map.Entry<String, JsonNode>> entryItr = parent.fields();
        while (entryItr.hasNext()) {
            Map.Entry<String, JsonNode> entry = entryItr.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            convertNode(struct, mList, value, key);
        }
    }


    /**
     * Convert XML node into a SAP structure parameter
     * @param struct JCoStructure
     * @param mList JCoRecordMetaData
     * @param node JsonNode
     * @param name Name
     */
    private void convertNode(JCoStructure struct, JCoRecordMetaData mList, JsonNode node,
                             String name) throws JCoException {
        convertToSAP(struct, mList.getTypeAsString(name), mList.getRecordTypeName(name), node,
                name);
    }

    /**
     * Convert to SAP
     * @param input JCoRecord
     * @param pType Type
     * @param pTypeName Type Name
     * @param node JsonNode
     * @param name Name
     * @throws JCoException JCoException
     */
    private void convertToSAP(JCoRecord input, String pType, String pTypeName, JsonNode node,
                              String name) throws JCoException {
        switch (pType) {
            case AdapterConstants.STYPE_STRUCTURE: // a nested structure...
                JCoStructure struct2 = input.getStructure(name);
                procStructureList(struct2, node, name, pTypeName);
                break;
            case AdapterConstants.STYPE_TABLE:
                JCoTable table2 = input.getTable(name);
                procTableList(table2, node, name, pTypeName);
                break;
            case AdapterConstants.STYPE_BYTE:
            case AdapterConstants.STYPE_XSTRING:
                input.setValue(name, decodeBytes(node.asText()));
                break;
            default:
                input.setValue(name, node.asText());
                break;
        }
    }

    /**
     * Table List
     * @param table JCoTable
     * @param parent JsonNode
     * @param pName Name
     * @param typeName Type Name
     */
    private void procTableList(JCoTable table, JsonNode parent, String pName, String typeName) throws JCoException {
        for (int i = 0; i < parent.size(); i++) {
            table.appendRow();
            JsonNode object = parent.get(i);
            JCoRecordMetaData mList = sapConn.getRepository().getRecordMetaData(typeName);
            if (object.getNodeType().equals(JsonNodeType.OBJECT)) {
                Iterator<Map.Entry<String, JsonNode>> entryItr = object.fields();
                while (entryItr.hasNext()) {
                    Map.Entry<String, JsonNode> entry = entryItr.next();
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();
                    convertTableNode(table, mList, value, key);
                }
            } else {
                table.setValue("", object.asText());
            }
        }
    }


    /**
     * Convert XML node into a SAP structure parameter
     * @param table JCoTable
     * @param mList JCoRecordMetaData
     * @param node JsonNode
     * @param name Name
     * @throws JCoException JCoException
     */
    private void convertTableNode(JCoTable table, JCoRecordMetaData mList, JsonNode node,
                                  String name) throws JCoException {
        convertToSAP(table, mList.getTypeAsString(name), mList.getRecordTypeName(name), node, name);
    }

    /**
     * Parameter List Index
     * @param pType Type
     * @return integer
     */
    private int getParmListIndex(String pType) {
        int type = 0;
        if (pType.equalsIgnoreCase(AdapterConstants.I))
            type = 0;
        else if (pType.equalsIgnoreCase(AdapterConstants.C))
            type = 1;
        else if (pType.equalsIgnoreCase(AdapterConstants.T))
            type = 2;
        else if (pType.equalsIgnoreCase(AdapterConstants.E))
            type = 3;
        return type;
    }


    /**
     * This method will create json from SAP response
     * @param function JCoFunction
     * @param name Name
     * @param mapper ObjectMapper
     * @param response JsonNode
     * @return JsonNode
     */
    public JsonNode getJsonReplyFromJCo(JCoFunction function, String name, ObjectMapper mapper,
                                        JsonNode response) {
        getOutParameterList(function.getExportParameterList(), mapper, response);

        JCoParameterList tList = function.getTableParameterList();
        if (tList != null) {
            for (JCoParameterFieldIterator itr = tList.getParameterFieldIterator();
                 itr.hasNextField();) {
                JCoField fld = itr.nextField();
                if (fld.getName().equalsIgnoreCase(AdapterConstants.EXTENSION_IN)) {
                    System.out.println();//TODO:Need to check
                } else {
                    genOutTableField(fld.getName(), fld.getTable(), mapper, response);
                }
            }
        }
        getOutParameterList(function.getChangingParameterList(), mapper, response);
        return response;
    }

    public JsonNode getJsonReplyFromJCoForInbound(JCoFunction function, String name, ObjectMapper mapper,
                                                  JsonNode response) {
        getOutParameterList(function.getImportParameterList(), mapper, response);

        JCoParameterList tList = function.getTableParameterList();
        if (tList != null) {
            for (JCoParameterFieldIterator itr = tList.getParameterFieldIterator();
                 itr.hasNextField();) {
                JCoField fld = itr.nextField();
                if (fld.getName().equalsIgnoreCase(AdapterConstants.EXTENSION_IN)) {
                    System.out.println();//TODO:Need to check
                } else {
                    genOutTableField(fld.getName(), fld.getTable(), mapper, response);
                }
            }
        }
        getOutParameterList(function.getChangingParameterList(), mapper, response);
        return response;
    }

    /**
     * Get Output Parameter List
     * @param list JCoParameterList
     * @param mapper ObjectMapper
     * @param response JsonNode
     */
    private void getOutParameterList(JCoParameterList list, ObjectMapper mapper, JsonNode response)
    {
        if (list != null) {
            for (JCoParameterFieldIterator itr = list.getParameterFieldIterator(); itr
                    .hasNextField();) {
                genOutField(itr.nextField(), mapper, response);
            }
        }
    }

    /**
     * Generate Output Field
     * @param fld JCoField
     * @param mapper ObjectMapper
     * @param response JsonNode
     */
    private void genOutField(JCoField fld, ObjectMapper mapper, JsonNode response)
    {
        String fName = fld.getName();
        if (fld.isStructure()) {
            genOutStructField(fName, fld.getStructure(), mapper, response);
        } else if (fld.isTable()) { // table should not be here...
            genOutTableField(fName, fld.getTable(), mapper, response);
        } else {
            genOutElement(fName, fld, response);
        }
    }

    /**
     * Generate Output Structure Field
     * @param name Name
     * @param str JCoStructure
     * @param mapper ObjectMapper
     * @param node JsonNode
     */
    private void genOutStructField(String name, JCoStructure str, ObjectMapper mapper,
                                   JsonNode node){
        JsonNode strNode = mapper.createObjectNode();
        for (JCoFieldIterator itr = str.getFieldIterator();
             itr.hasNextField();) {
            genOutField(itr.nextField(), mapper, strNode);
        }
        setJsoNode(node, strNode, name);
    }

    /**
     * Set JSON Node
     * @param parent JsonNode
     * @param child JsonNode
     * @param name Name
     */
    private void setJsoNode(JsonNode parent, JsonNode child, String name) {
        if (parent.getNodeType().equals(JsonNodeType.OBJECT)) {
            ((ObjectNode) parent).set(name, child);
        } else if (parent.getNodeType().equals(JsonNodeType.ARRAY)) {
            ((ArrayNode) parent).add(child);
        }
    }

    /**
     * Generate Output Table Field
     * @param name Name
     * @param tbl JCoTable
     * @param mapper ObjectMapper
     * @param node JsonNode
     */
    private void genOutTableField(String name, JCoTable tbl, ObjectMapper mapper, JsonNode node)
    {
        if (tbl.getNumRows() > 0) {
            ArrayNode arrayNode = mapper.createArrayNode();
            setJsoNode(node, arrayNode, name);
            for (int i = 0; i < tbl.getNumRows(); i++) {
                tbl.setRow(i);
                ObjectNode objNode = mapper.createObjectNode();
                for (JCoFieldIterator e = tbl.getFieldIterator();
                     e.hasNextField();) {
                    JCoField f = e.nextField();
                    if (f.isStructure()) {
                        genOutStructField(f.getName(), f.getStructure(), mapper,
                                mapper.createObjectNode());
                    } else if (f.isTable()) {
                        genOutTableField(f.getName(), f.getTable(), mapper, objNode);
                    } else {
                        genOutElement(f.getName(), f, objNode);
                    }
                }
                arrayNode.add(objNode);
            }
        }
    }

    /**
     * Generate output Element
     * @param name Name
     * @param fld JCoField
     * @param node JsonNode
     */
    private void genOutElement(String name, JCoField fld, JsonNode node) {
        String val = getFieldValue(fld);
        if ((val != null) && (!name.equals(""))) {
            ((ObjectNode) node).put(name, val);
        }
    }

    /**
     * Get Field Value
     * @param f JCoField
     * @return String
     */
    private String getFieldValue(JCoField f) {
        switch (f.getType()) {
            case 1: // TYPE_DATE Date (YYYYYMMDD)
                try {
                    Date date = f.getDate();
                    if (date != null) {
                        /*
                         * Using ThreadLocal instead of class level date formatter, to avoid dates
                         * from multiple rows from getting mixed, due to multithreading
                         */
                        SimpleDateFormat formatter = threadedDateFormatter.get();
                        return formatter.format(f.getDate());
                    }
                    return "";
                } catch (Exception ex) { // bad date input or null
                    return f.getString();
                }
                // case 2: // TYPE_BCD Packed BCD number, 1 to 16 bytes
            case 3: // TYPE_TIME Time (HHMMSS)
                try {
                    Date time = f.getTime();
                    if (time != null) {
                        /*
                         * Using ThreadLocal instead of class level date formatter, to avoid time
                         * from multiple rows from getting mixed, due to multithreading
                         */
                        SimpleDateFormat formatter = threadedTimeFormatter.get();
                        return formatter.format(f.getTime());
                    } else {
                        return "";
                    }
                } catch (Exception ex) { // bad time input or null
                    return f.getString();
                }
            case 4: // TYPE_BYTE Raw data, binary, fixed length, zero padde
            case 30: // TYPE_XSTRING Byte array of variable length
                try {
                    return getHexValue(f.getByteArray());
                } catch (Exception ex) { // bad input or null
                    return f.getString();
                }
        }
        return getTextValue(f.getString());
    }

    /**
     * Get Hex Value
     * @param bs byte[]
     * @return String
     */
    private String getHexValue(byte[] bs) {
        return (bs != null) ? Codecs.Base64.encode(bs) : null;
    }

    /**
     * Get Text Value
     * @param val Value
     * @return String
     */
    private String getTextValue(String val) {
        char[] strChars = {};
        if (val != null) {
            strChars = new char[val.length()];
            for (int i = 0; i < val.length(); i++) {
                strChars[i] = (val.charAt(i) < ' ') ? ' ' : val.charAt(i);
            }
        }
        return new String(strChars);
    }

    public IDocType getIdocTypeFromFunction(JCoFunction function, String release, String appRelease) throws JCoException {
        JCoTable dc = function.getTableParameterList().getTable("IDOC_CONTROL_REC_40");
        if (dc.getNumRows() < 1) {
            return null; // no DC...
        }
        String type = dc.getString("IDOCTYP");
        String mesType = dc.getString("MESTYP");
        String ext = dc.getString("CIMTYP");
        String key = type + ":" + mesType + ":" + ext + ":" + release;
        IDocType idoc = idocStore.get(key);
        if (idoc != null) {
            return idoc;
        }
        idoc = new IDocType(type, "", mesType, release, ext);
        idoc.buildSegments(sapConn.getIDocRepsitory(), release, appRelease);
        idocStore.put(key, idoc);
        return idoc;
    }


    /**
     * Method to convert Json Payload to sap native jco structure
     * @param function JCoFunction
     * @param payload Json payload
     * @throws JCoException JCoException
     */
    public void conIdocJsonToJCO(JCoFunction function, JsonNode payload) throws JCoException {
        IDocType type = getIDOCType(payload);
        type.setIDocJsonToJCO(function, payload, false);
    }

    /**
     * Method to get IDOC info from json
     */
    private IDocType getIDOCType(JsonNode payload) throws JCoException {
        IDocType type = null;
        if (payload != null && payload.get("IDOC") != null) {
            JsonNode idoc = payload.get("IDOC");
            JsonNode dc40 = idoc.get("EDI_DC40");
            String idocName = dc40.get("IDOCTYP").textValue();
            String idocType = dc40.get("MESTYP").textValue();
            String ext = "";
            if (dc40.has("CIMTYP")) {
                ext = dc40.get("CIMTYP").textValue();
            }
            String rel = "";
            if (dc40.has("DOCREL")) {
                rel = dc40.get("DOCREL").textValue();
            }
            String idocKey = idocName + ":" + idocType + ":" + ext + ":" + rel;
            type = idocStore.get(idocKey);
            if (type != null) {
                return type;
            }
            type = new IDocType(idocName, "", idocType, rel, ext);
            type.buildSegments(sapConn.getIDocRepsitory(), rel, "");
            idocStore.put(idocKey, type);
        }
        return type;
    }
}
