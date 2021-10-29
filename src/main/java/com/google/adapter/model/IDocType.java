/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package com.google.adapter.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.util.StrUtil;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.conn.idoc.IDocRepository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.conn.idoc.IDocRecordMetaData;
import com.sap.conn.idoc.IDocSegmentMetaData;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

/**
 * IDOCType Message type model
 */
public class IDocType
// extends CloudDataObjectNodeImpl
{
    private String name; // 0: CHAR, EDI_IDOCTP, Basic type,
    private String description; // 1: CHAR, EDI_TEXT60, Short description of object
    private String devclass; // 2: CHAR, DEVCLASS, Package
    private String releaseIn; // 3: CHAR, EDI_TYPREL, Released in
    private String ext = null; // extensions
    @JsonIgnore
    private String dName;
    @JsonIgnore
    private String xName; // extend IDOC XML element name
    @JsonIgnore
    private String segRelease;
    @JsonIgnore
    private String appRelease;
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(IDocType.class.getName());
    public static final Map<String, String[]> IDOCStatus = new HashMap<>();
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            IDOCStatus.putAll(objectMapper.readValue(new File("src/main/resources/idocType.json"), HashMap.class));
        } catch (IOException e) {
            SystemException.throwException(e.getMessage(), e);
        }
    }

    List<IDocSegment> sList = new ArrayList(); // top-level child segments
    Map<String, IDocSegment> sMap = new HashMap(); // type (E1STATS) to segment
    Map<String, String> defMap = new HashMap(); // def (E2STATS001) to type (E1STATS)e
    private Document document = null;
    private XPathFactory xPathFactory = null;
    XPath xPath = null;
    private boolean generic = false;

    public IDocType(String name, String description, String devclass, String releaseIn) {
        this(name, description, devclass, releaseIn, null);
    }

    public IDocType(String name, String description, String devclass, String releaseIn,
        String ext) {
        this.name = name;
        this.description = description;
        this.releaseIn = releaseIn;
        this.devclass = devclass;
        this.segRelease = "";
        this.appRelease = "";
        if ((ext != null) && (!ext.trim().isEmpty())) {
            this.ext = ext;
        }
        dName = (this.ext == null) ? name : (name + "." + ext);
        xName = (this.ext == null) ? name : (name + "_" + ext);
        if (xPathFactory == null) {
            xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
        }
    }

    @JsonProperty("idoc")
    public String getName() {
        return dName;
    }

    @JsonIgnore
    public String getBaseName() {
        return name;
    }

    @JsonIgnore
    public String getDisplayName() {
        return dName;
    }

    @JsonIgnore
    public String getXmlElementName() {
        return xName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("release")
    public String getReleaseIn() {
        return releaseIn;
    }

    public void setReleaseIn(String releaseIn) {
        this.releaseIn = releaseIn;
    }

    @JsonProperty("idocType")
    public String getDevclass() {
        return devclass;
    }

    @JsonIgnore
    public String getSegRelease() {
        return segRelease;
    }

    public void setSegRelease(String segRelease) {
        this.segRelease = segRelease;
    }

    @JsonIgnore
    public String getAppRelease() {
        return appRelease;
    }

    public void setAppRelease(String appRelease) {
        this.appRelease = appRelease;
    }

    @JsonIgnore
    public boolean isLeaf() {
        return (sList.size() > 0);
    }

    public void addSegment(IDocSegment child) {
        sList.add(child);
    }

    @JsonIgnore
    public List<IDocSegment> getSegments() {
        return sList;
    }

    @JsonIgnore
    public Set<String> getSegmentTypes() {
        return sMap.keySet();
    }

    @JsonIgnore
    public Set<String> getSegmentDefs() {
        return defMap.keySet();
    }

    public IDocSegment getSegmentByType(String type) {
        return sMap.get(type);
    }

    public IDocSegment getSegmentByDef(String def) {
        return sMap.get(defMap.get(def));
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @JsonIgnore
    public boolean isExt() {
        return (ext != null);
    }

    public String toString() {
        return "IDocType [name=" + getName() + ", description=" + getDescription() + ", releaseIn="
            + getReleaseIn() + "instance :" + hashCode() + "]";
    }
    // Generate IDOC xsd

    /**
     * @return the generic
     */
    @JsonIgnore
    public boolean isGeneric() {
        return generic;
    }

    /**
     * @param generic the generic to set
     */
    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    private void createSegment(IDocSegmentMetaData rs) {
        IDocSegmentMetaData[] cs = rs.getChildren();
        for (int i = 0; i < cs.length; i++) {
            IDocSegment seg = new IDocSegment(cs[i]);
            if ((seg.PARSEG == null) || (seg.PARSEG.equals("ROOT"))) { // seg may be at 2 or higer
                // level
                this.addSegment(seg);
            } else {
                IDocSegment p = sMap.get(seg.PARSEG);
                if (p != null) {
                    p.addSegment(seg);
                }
            }
            sMap.put(seg.SEGMENTTYP, seg);
            // Create Fields
            IDocRecordMetaData rm = cs[i].getRecordMetaData();
            for (int r = 0; r < rm.getNumFields(); r++) {
                IDocField fld = new IDocField(rm, r);
                if (seg != null) {
                    seg.addField(fld);
                }
            }
            createSegment(cs[i]);
        }
    }

    public void buildSegments(IDocRepository idocRepository, String segRelease, String appRelease) {

        if (idocRepository == null) {
            return;
        }
        try {
            IDocSegmentMetaData rs = idocRepository.getRootSegmentMetaData(this.getBaseName(),
                    this.getExt(), this.getSegRelease(), this.getAppRelease());
            // Create Segments
            createSegment(rs);
            // populate def maps
            Iterator<String> is = sMap.keySet().iterator();
            while (is.hasNext()) {
                String key = is.next();
                IDocSegment seg = getSegmentByType(key);
                defMap.put(seg.SEGMENTDEF, key);
            }
        } catch (Exception exception) {
            throw SystemException.throwException(null, new AdapterException(String.valueOf(AdapterConstants.JCO_IDOC_ERROR)));
        }
    }

    public String getIDocJsonFromJCONew(JCoFunction function, String tid, boolean isGeneric,
        boolean isWrapped, boolean doMigration) throws JsonProcessingException, AbapException {
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.createObjectNode();
        Map<String, String> segnumMap = null;
        String result = null;
        Map<String, String> segToPathMap = null;
        JsonNode idoc = mapper.createObjectNode();
        StringBuilder s = new StringBuilder();

        IDocSegmentDC40 dc40 = IDocSegmentDC40.getEDI_DC40();
        String idocName = null;
        String segIdocNumber = null;
        String controlIdocNumber = null;;
        for (int i = 0; i < dc.getNumRows(); i++) {
            dc.setRow(i);
            idocName = dc.getString("IDOCTYP");
            idocName = StrUtil.encodeName(idocName);
            controlIdocNumber = dc.getString("DOCNUM");
            String CIMTYP = dc.getString("CIMTYP");
            if ((CIMTYP != null) && (CIMTYP.length() > 0)) {
                if (!doMigration) {
                    idocName += "_" + CIMTYP;
                }
            }
            dc40.setDC40JsonFromJCO(dc, false, idoc, mapper); // EDI_DC40
            ((ObjectNode) root).set("EDI_DC40", idoc.get("EDI_DC40"));
            int segs = dd.getNumRows();
            for (int j = 0; j < segs; j++) {
                dd.setRow(j);
                ObjectNode currentNode = null;
                ArrayNode currentArrayNode = null;
                JsonNode currentParent = null;
                segIdocNumber = dd.getString("DOCNUM");
                if (controlIdocNumber.equals(segIdocNumber)) {
                    String sName = dd.getString("SEGNAM");
                    IDocSegment seg = getSegmentByDef(sName);
                    int sn = Integer.parseInt(dd.getString("SEGNUM"));
                    long pn = dd.getInt("PSGNUM");
                    // OK, start a new tag
                    if (seg == null) {
                        throw new AbapException("IDOC Segement Issue", "Segement " + sName
                            + " metadata for idoc type " + idocName + "\n"
                            + " does not match with release version configured in adapter endpoint or\n"
                            + " Provided segement release is Invalid. Please verify again");
                    }
                    /**
                     * find the current parent of the new segment in the JSON tree based on parent
                     * segment number
                     */
                    currentParent = (ObjectNode) getParentNode((JsonNode) root, pn);
                    // start a Node
                    // If Parent available , get the existing same json node from parent
                    if (currentParent != null) {
                        currentArrayNode = (ArrayNode) currentParent.get(seg.SEGMENTTYP);
                    }
                    if (Integer.parseInt(seg.OCCMAX) > 1 && currentArrayNode == null) {
                        /**
                         * Find the current json node in root level.
                         */
                        currentArrayNode = (ArrayNode) root.get(seg.SEGMENTTYP);
                        if (currentArrayNode == null)
                            currentArrayNode = mapper.createArrayNode();
                    }
                    currentNode = mapper.createObjectNode();
                    seg.getSegmentJsonFromJCO(dd, false, currentNode);
                    currentNode.put("SEGNUM", sn);
                    if (currentParent != null) {
                        if (currentArrayNode != null) {
                            currentArrayNode.add(currentNode);
                            ((ObjectNode) currentParent).set(seg.SEGMENTTYP, currentArrayNode);
                        } else {
                            ((ObjectNode) currentParent).set(seg.SEGMENTTYP, currentNode);
                        }
                    } else {
                        if (currentArrayNode != null) {
                            currentArrayNode.add(currentNode);
                            ((ObjectNode) root).set(seg.SEGMENTTYP, currentArrayNode);
                        } else {
                            ((ObjectNode) root).set(seg.SEGMENTTYP, currentNode);
                        }
                    }
                }
            }
            JsonNode idocJson = mapper.createObjectNode();
            JsonNode meta = mapper.createObjectNode();
            ((ObjectNode) meta).put("tid", tid);
            ((ObjectNode) idocJson).set("meta", meta);
            ((ObjectNode) idocJson).set("IDOC", root);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(idocJson);
        }
        return result;
    }

    public JsonNode getParentNode(JsonNode root, long parentNumber) {
        ObjectNode node = null;
        if (root instanceof ObjectNode) {
            return parseJsonObject((ObjectNode) root, parentNumber);
        }
        return null;
    }

    public ObjectNode parseJsonArray(ArrayNode arrNode, long parentNumber) {
        ObjectNode resNode = null;
        for (int i = 0; i < arrNode.size(); i++) {
            JsonNode node = arrNode.get(i);
            if (node.isObject()) {
                resNode = parseJsonObject((ObjectNode) node, parentNumber);
                if (resNode != null) {
                    return resNode;
                }
            } else {
                resNode = parseJsonArray((ArrayNode) node, parentNumber);
                if (resNode != null) {
                    return resNode;
                }
            }
        }
        return resNode;
    }

    public ObjectNode parseJsonObject(ObjectNode objectNode, long parentNumber) {
        ObjectNode resNode = null;
        for (Iterator<String> it = objectNode.fieldNames(); it.hasNext();) {
            String field = it.next();
            JsonNode child = objectNode.get(field);
            if (child instanceof ArrayNode) {
                resNode = parseJsonArray((ArrayNode) child, parentNumber);
                if (resNode != null) {
                    return resNode;
                }
            } else if (child instanceof ObjectNode) {
                resNode = parseJsonObject((ObjectNode) child, parentNumber);
                if (resNode != null) {
                    return resNode;
                }
            } else if (field.equals("SEGNUM")) {
                if (parentNumber == child.intValue()) {
                    return objectNode;
                }
            }
        }
        return resNode;
    }

    public String getIDocFlatFromJCO(JCoFunction function) {
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < dc.getNumRows(); i++) {
            dc.setRow(i);
            s.append(IDocSegmentDC40.getDC40FlatFromJCO(dc));
            s.append("\n");
        }
        for (int i = 0; i < dd.getNumRows(); i++) {
            dd.setRow(i);
            String segDef = dd.getString("SEGNAM");
            String idocType = defMap.get(segDef);
            IDocSegment seg = sMap.get(idocType);
            s.append(IDocSegment.getSegmentFlatFromJCO(dd, seg));
            s.append("\n");
        }
        return s.toString();
    }

    public String getIDocFlatXMLFromJCO(JCoFunction function, String tid, boolean isGeneric,
        boolean isWrapped, String targetNameSpace) throws AbapException {
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
        // Get IDOC name
        String idocName = null;
        if (dc.getNumRows() > 0) {
            idocName = dc.getString("IDOCTYP");
            String CIMTYP = dc.getString("CIMTYP");
            if ((CIMTYP != null) && (CIMTYP.length() > 0)) {
                idocName += "_" + CIMTYP;
            }
        }
        String tidTag = ((tid != null) ? " tid=\"" + tid + "\"" : "");
        String nameTag = "";
        if (isGeneric) {
            nameTag = "<GENERIC_IDOC xmlns=\"" + targetNameSpace + "\">" + "<" + idocName + tidTag
                + ">\n<IDOC-ENCODED>";
        } else if (isWrapped) {
            nameTag = "<parameters><" + idocName + tidTag + " xmlns=\"" + targetNameSpace
                + "\">\n<IDOC-ENCODED>";
        } else {
            nameTag =
                "<" + idocName + tidTag + " xmlns=\"" + targetNameSpace + "\">\n<IDOC-ENCODED>";
        }
        String endTag = "";
        if (isGeneric) {
            endTag = "</IDOC-ENCODED>\n</" + idocName + ">\n</GENERIC_IDOC>\n";
        } else if (isWrapped) {
            endTag = "</IDOC-ENCODED>\n</" + idocName + "></parameters>\n";
        } else {
            endTag = "</IDOC-ENCODED>\n</" + idocName + ">\n";
        }
        String segIdocNumber = null;
        String controlIdocNumber = null;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < dc.getNumRows(); i++) {
            dc.setRow(i);
            controlIdocNumber = dc.getString("DOCNUM");
            s.append(IDocSegmentDC40.getDC40FlatFromJCO(dc));
            s.append("\n");
            for (int j = 0; j < dd.getNumRows(); j++) {
                dd.setRow(j);
                segIdocNumber = dd.getString("DOCNUM");
                if (controlIdocNumber.equals(segIdocNumber)) {
                    String segDef = dd.getString("SEGNAM");
                    String idocType = defMap.get(segDef);
                    IDocSegment seg = sMap.get(idocType);
                    if (seg == null) {
                        throw new AbapException("IDOC Segement Issue", "Segement " + segDef
                            + " metadata for idoc type " + idocName + "\n"
                            + " does not match with release version configured in adapter endpoint or\n"
                            + " Provided segement release is Invalid. Please verify again");
                    }
                    s.append(IDocSegment.getSegmentFlatFromJCO(dd, seg));
                    s.append("\n");
                }
            }
        }
        String encoded = DatatypeConverter.printBase64Binary(s.toString().getBytes());
        return (nameTag + encoded + endTag);
    }

    int docnumInt = 1;

    private void setXmlToDC40(JCoTable dc, Node sn) {
        dc.appendRow();
        NodeList ns = sn.getChildNodes();
        for (int k = 0, n = ns.getLength(); k < n; k++) {
            Node node = ns.item(k);
            if (node instanceof Element) {
                String v = node.getTextContent();
                if ((v != null) && (v.length() > 0)) {
                    dc.setValue(node.getLocalName() /* .getNodeName() */, v);
                } else {
                    if (node.getLocalName().equals("DOCNUM") && v != null) {
                        while (v.isEmpty()) {
                            if (!idocNumbers.contains(String.valueOf(docnumInt))) {
                                v = String.valueOf(docnumInt);
                                idocNumbers.add(v);
                                dc.setValue(node.getLocalName() /* .getNodeName() */, v);
                                docnumInt = docnumInt + 1;
                            } else {
                                docnumInt = docnumInt + 1;
                            }
                        }
                    }
                }
            }
        }
    }

    private int setJsonToDD40(JCoTable dd, String segmentName, JsonNode segObj, String mandt,
        String docnum, int segnum, int psgnum) throws AbapException {
        // add a new row...
        dd.appendRow();
        if (segmentName.equals("BEGIN"))
            return segnum;
        int nxSegnum = segnum + 1;
        IDocSegment seg = sMap.get(segmentName);
        if (seg == null) {
            throw new AbapException("IDOC Segement Issue", "Segement " + segmentName
                + " metadata for current idoc type "
                + " does not match with release version configured in adapter endpoint or\n"
                + " Provided segement release is Invalid. Please check and verify again");
        }
        seg.initSData();
        dd.setValue("SEGNAM", seg.SEGMENTDEF);
        dd.setValue("MANDT", mandt);
        dd.setValue("DOCNUM", docnum);
        dd.setValue("SEGNUM", String.format("%06d%n", segnum));
        dd.setValue("PSGNUM", psgnum); // String.format("%06d%n", psgnum));
        dd.setValue("HLEVEL", seg.HLEVEL);
        // add segment fields and child segments
        boolean writeData = true;
        Iterator<Map.Entry<String, JsonNode>> entryItr = segObj.fields();
        while (entryItr.hasNext()) {
            Map.Entry<String, JsonNode> entry = entryItr.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            IDocSegment s = sMap.get(name);
            if (s == null) {
                if (value.getNodeType().equals(JsonNodeType.STRING)) {
                    if (!name.equals("SEGMENT"))
                        seg.fillSData(name, value.textValue());
                }
            }
        }
        if (writeData) { // set sdata once...
            writeData = false;
            dd.setValue("SDATA", seg.getSData());
        }
        List<IDocSegment> segments = seg.getSegments();
        List<String> segSeq = getSegSeguence(segments, segObj);
        for (String segType : segSeq) {
            if (!segObj.has(segType)) {
                continue;
            }
            JsonNode value = segObj.get(segType);
            IDocSegment s = sMap.get(segType);
            if (s != null) {
                if (value.getNodeType().equals(JsonNodeType.ARRAY)) {
                    Iterator<JsonNode> arrItr = value.iterator();
                    while (arrItr.hasNext()) {
                        JsonNode object = arrItr.next();
                        nxSegnum =
                            setJsonToDD40(dd, segType, object, mandt, docnum, nxSegnum, segnum);
                    }
                }
                if (value.getNodeType().equals(JsonNodeType.OBJECT)) {
                    nxSegnum = setJsonToDD40(dd, segType, value, mandt, docnum, nxSegnum, segnum);
                }
            }
        }
        if (writeData) { // set sdata
            writeData = false;
            dd.setValue("SDATA", seg.getSData());
        }
        return nxSegnum;
    }

    public List<String> getSegSeguence(List<IDocSegment> segments, JsonNode jsonSegment) {
        List<String> segSeq = new ArrayList<String>();
        if (segments != null) {
            for (IDocSegment seg : segments) {
                segSeq.add(seg.SEGMENTTYP);
            }
        }
        return segSeq;
    }

    private int setXmlToDD40(JCoTable dd, Node sn, String mandt, String docnum, int segnum,
        int psgnum) throws AbapException {
        // add a new row...
        dd.appendRow();
        int nxSegnum = segnum + 1;
        String sName = sn.getLocalName(); // .getNodeName();
        IDocSegment seg = sMap.get(sName);
        if (seg == null) {
            throw new AbapException("IDOC Segement Issue", "Segement " + sName
                + " metadata for current idoc type "
                + " does not match with release version configured in adapter endpoint or\n"
                + " Provided segement release is Invalid. Please check and verify again");
        }
        seg.initSData();
        dd.setValue("SEGNAM", seg.SEGMENTDEF);
        dd.setValue("MANDT", mandt);
        dd.setValue("DOCNUM", docnum);
        dd.setValue("SEGNUM", String.format("%06d%n", segnum));
        dd.setValue("PSGNUM", psgnum); // String.format("%06d%n", psgnum));
        dd.setValue("HLEVEL", seg.HLEVEL);
        // add segment fields and child segments
        NodeList ns = sn.getChildNodes();
        boolean writeData = true;
        for (int k = 0, n = ns.getLength(); k < n; k++) {
            Node node = ns.item(k);
            if (node instanceof Element) {
                String name = node.getLocalName(); // .getNodeName();
                IDocSegment s = sMap.get(name);
                if (s == null) { // a field, encode into SDATA...
                    // convert xml value into field value
                    seg.fillSData(name, node.getTextContent());
                } else { // a segment..
                    if (writeData) { // set sdata once...
                        writeData = false;
                        dd.setValue("SDATA", seg.getSData());
                    }
                    setXmlToDD40(dd, node, mandt, docnum, nxSegnum, segnum);
                    nxSegnum++;
                }
            }
        }
        if (writeData) { // set sdata
            writeData = false;
            dd.setValue("SDATA", seg.getSData());
        }
        return nxSegnum;
    }

    List<String> idocNumbers = null;

    public synchronized void setIDocXMLToJCO(JCoFunction function, Element root, boolean migration)
        throws AbapException {
        // Function: IDOC_INBOUND_ASYNCHRONOUS
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
/*        if (migration) {
            // process attributes for iWay IDOC...
        }*/
        NodeList ns = root.getElementsByTagNameNS("*", "IDOC"); // list of IDOCs...
        for (int i = 0, nl = ns.getLength(); i < nl; i++) {
            Node in = ns.item(i); // an IOC..
            NodeList ss = in.getChildNodes();
            String mandt = "";
            String docnum = "";
            int segnum = 1;
            int psgnum = 0;
            for (int j = 0, sl = ss.getLength(); j < sl; j++) {
                Node sn = ss.item(j); // a segment..
                if (sn instanceof Element) {
                    String sName = sn.getLocalName(); // .getNodeName();
                    if (sName.equals("EDI_DC40")) { // DC40
                        setXmlToDC40(dc, sn);
                        mandt = dc.getString("MANDT");
                        docnum = dc.getString("DOCNUM");
                    } else { // data segment
                        segnum = setXmlToDD40(dd, sn, mandt, docnum, segnum, psgnum);
                    }
                }
            }
        }
    }

    public synchronized void setIDocJsonToJCO(JCoFunction function, JsonNode root,
        boolean migration) throws AbapException {
        // Function: IDOC_INBOUND_ASYNCHRONOUS
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
        if (root.get("IDOC") != null) {
            String mandt = null;
            String docnum = null;
            int segnum = 1;
            int psgnum = 0;
            JsonNode idoc = (JsonNode) root.get("IDOC");
            JsonNode edi_dc40 = idoc.get("EDI_DC40");
            /**
             * Setting Control record IN IDOC
             */
            Iterator<Map.Entry<String, JsonNode>> entryItr = edi_dc40.fields();
            dc.appendRow();
            if (edi_dc40.has("DOCNUM"))
                docnum = edi_dc40.get("DOCNUM").textValue();
            while (entryItr.hasNext()) {
                Map.Entry<String, JsonNode> entry = entryItr.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                // Control Record properties iteration
                if (value.getNodeType().equals(JsonNodeType.STRING)) {
                    if (!key.equals("SEGMENT"))
                        dc.setValue(key, value.textValue());
                }
                mandt = edi_dc40.get("MANDT").textValue();
            }
            /**
             * Setting Data record IN IDOC
             */
            entryItr = idoc.fields();
            List<String> segSeq = getSegSeguence(sList, idoc);
            for (String segType : segSeq) {
                if (!idoc.has(segType)) {
                    continue;
                }
                JsonNode jsonSegment = idoc.get(segType);

                if (!jsonSegment.equals("EDI_DC40")
                    && jsonSegment.getNodeType().equals(JsonNodeType.OBJECT)) {
                    // JsonNode jsonSegment = (JsonNode) value;
                    segnum = setJsonToDD40(dd, segType, jsonSegment, mandt, docnum, segnum, psgnum);

                }
                if (!jsonSegment.equals("EDI_DC40")
                    && jsonSegment.getNodeType().equals(JsonNodeType.ARRAY)) {
                    Iterator<JsonNode> arrItr = jsonSegment.iterator();
                    while (arrItr.hasNext()) {
                        JsonNode object = arrItr.next();
                        segnum = setJsonToDD40(dd, segType, object, mandt, docnum, segnum, psgnum);

                    }
                }
            }
        }
    }

    private void getAllIdocNum(Element root) {
        idocNumbers = new ArrayList<>();

        NodeList nodeList = root.getElementsByTagNameNS(root.getNamespaceURI(), "DOCNUM");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            idocNumbers.add(node.getTextContent());
        }

    }

    public void setIDocFlatToJCO(JCoFunction function, Element root, boolean migration) {
        JCoTable dc = function.getTableParameterList().getTable((String) "IDOC_CONTROL_REC_40");
        JCoTable dd = function.getTableParameterList().getTable((String) "IDOC_DATA_REC_40");
        // handle iWay migration XML..
/*        if (migration) {
            // process attributes for iWay IDOC...
        }*/
        NodeList ns = root.getElementsByTagNameNS("*", "IDOC-ENCODED"); // FF encoded IDOCs...
        for (int i = 0, nl = ns.getLength(); i < nl; i++) {
            Node in = ns.item(i); // a FF IDOC..
            byte[] ff = DatatypeConverter.parseBase64Binary(in.getTextContent());
            // }
            BufferedReader br =
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ff)));
            String line;
            List<byte[]> ss = new ArrayList(); // segments..
            try {
                while ((line = br.readLine()) != null) {
                    ss.add(line.getBytes());
                }
            } catch (IOException ex) {
                //
            }
            idocNumbers = getAllIdocNumbers(ss);
            mLog.info("idoc list======" + idocNumbers);
            String mandt = "";
            String docnum = "";
            int segnum = 1;
            int psgnum = 0;
            for (byte[] bs : ss) {
                String sName = (new String(Arrays.copyOfRange(bs, 0, 30))).trim();

                mLog.log(Level.INFO, "line={0}", new String(bs));
                mLog.log(Level.INFO, "sName={0}", sName);
                if (sName.startsWith("EDI_DC40")) {// DC40
                    IDocSegmentDC40 dc40 = IDocSegmentDC40.getEDI_DC40();
                    dc.appendRow();
                    idocNumbers = dc40.setDC40FlatToJCO(dc, bs, idocNumbers);
                    mLog.log(Level.INFO, "idocNumbers={0}", idocNumbers);
                    mandt = dc.getString("MANDT");
                    docnum = dc.getString("DOCNUM");
                } else { // data segment
                    dd.appendRow();
                    String def = defMap.get(sName);
                    IDocSegment seg = (def != null) ? sMap.get(def) : null;
                    if (seg != null) {
                        dd.setValue("SEGNAM", seg.SEGMENTDEF);
                        dd.setValue("MANDT", mandt);
                        dd.setValue("DOCNUM", docnum);
                        dd.setValue("SEGNUM", String.format("%06d%n", segnum));
                        dd.setValue("PSGNUM", psgnum); // String.format("%06d%n", psgnum));
                        dd.setValue("HLEVEL", seg.HLEVEL);
                        dd.setValue("SDATA", new String(Arrays.copyOfRange(bs, 63, bs.length)));
                    }
                }
            }
        }
    }

    public static List<String> getAllIdocNumbers(List<byte[]> ss) {
        List<String> idocNumbers = new ArrayList<>();
        String v = "";
        for (byte[] bs : ss) {
            String sName = (new String(Arrays.copyOfRange(bs, 0, 30))).trim();
            if (sName.startsWith("EDI_DC40")) {
                int pos = 0;
                for (IDocField f : IDocSegmentDC40.getEDI_DC40().fList) {
                    byte[] b = Arrays.copyOfRange(bs, pos, pos + f.EXTLEN);
                    v = (new String(b)).trim();
                    if (f.FIELDNAME.equalsIgnoreCase("DOCNUM")) {
                        if (v.length() > 0) {
                            idocNumbers.add(v);
                            break;
                        }
                    }
                    pos += f.EXTLEN;
                }
            }
        }
        return idocNumbers;
    }

}
