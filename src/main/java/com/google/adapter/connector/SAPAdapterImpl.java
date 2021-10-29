package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.outbound.SapSubscriber;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.JSONReader;
import com.google.adapter.util.ReadPropertyFile;
import com.google.adapter.util.ValidationParameter;
import com.sap.conn.jco.JCoException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Implementation class of SAPAdapter. This is the wrapper class of all the classes and all the call
 * will begin from this class only
 *
 * @author dhiraj.kumar
 */
public class SAPAdapterImpl implements SAPAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SAPAdapterImpl.class);
    private static Map<String, JCoListenerHandler> regJcoListeners = new HashMap<>();
    // Removed final modifier for mockito test support
    private SAPConnector sapConn;
    private ErrorCapture errorCapture;
    private SAPSchema sapSchema;
    private SAPProperties sapProps;

    /**
     * Constructor for SAPAdapterImpl
     *
     * @param errorCapture Initialize ErrorCapture
     * @param properties   Get parameters from Properties file
     * @throws JCoException In case of missing parameters
     */
    public SAPAdapterImpl(ErrorCapture errorCapture, Properties properties)
            throws JCoException {
        this.errorCapture = errorCapture;
        sapProps = SAPProperties.getDefault(properties);
        this.sapConn = new SAPConnector(this.errorCapture, properties);
        this.sapSchema = new SAPSchema(this.sapConn, this.errorCapture);
    }


    /**
     * This method checks the connectivity with SAP system
     *
     * @return returning the boolean value of the connection
     */
    @Override
    public boolean ping() {
        try {
            sapConn.ping();
            logger.info("SAPAdapterImpl Ping Method");
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return true;
    }

    /**
     * This method checks the connectivity with SAP system
     *
     * @return returning the boolean value of the connection
     */
    public String generateSSO() {
        String ssoTicket;
        try {
            ssoTicket = sapConn.generateSSO();
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return ssoTicket;
    }

    /**
     * Get List of RFC and store into Map
     *
     * @param refreshRequired Is caching required or not
     * @param filterText      String based on which list will be populated
     * @return Map where key is String and value is RFCMethod
     */
    @Override
    public JsonNode getRFCList(String filterText, boolean refreshRequired) {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getRFCList");
            node = sapConn.getRFCList(filterText, refreshRequired);
            logger.info("SAPAdapterImpl getRFCList called, returned node size: " + node.size());
        } catch (InterruptedException | ExecutionException | IOException | ParseException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }


    /**
     * Get List of RFC and store into Map
     *
     * @param rfcName String
     * @return JsonNode where key is String and value is RFCMethod
     */
    @Override
    public JsonNode getRFCSchema(String rfcName) {
        JsonNode node = null;
        try {
            logger.info("SAPAdapterImpl method : " + "getRFCSchema");
            node = sapSchema.getRFCSchema(rfcName);
            logger.info("SAPAdapterImpl getRFCSchema called, returned node size: " + node.size());
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Get List of BAPI and store into Map
     *
     * @param bapiName String
     * @return JsonNode where key is String and value is RFCMethod
     */
    @Override
    public JsonNode getBAPISchema(String bapiName) {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getBAPISchema");
            node = sapSchema.getBAPISchema(bapiName);
            logger.info("SAPAdapterImpl getBAPISchema called, returned node size: " + node.size());
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * @param bapiName       BAPI Name
     * @param operationProps Properties which are needed BAPI Execution
     * @return JSON node
     */
    @Override
    public JsonNode executeBAPI(String bapiName, Map<String, String> operationProps, String tID, String queueName) {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "executeBAPI");
            node = sapConn.executeBAPI(bapiName, operationProps, tID, queueName);
            logger.info("SAPAdapterImpl executeBAPI called, returned node size: " + node.size());
        } catch (JCoException | IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Get List of BAPI in JSON Node
     *
     * @return List of BAPI
     */
    @Override
    public JsonNode getBAPIList() {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getBAPIList");
            node = sapConn.getBAPIList();
            logger.info("SAPAdapterImpl getBAPIList called, returned node size: " + node.size());
        } catch (JCoException | IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * @param payload        RFC Name
     * @param operationProps Properties which are needed RFC Execution
     * @return JSON node
     */
    @Override
    public JsonNode executeRFC(String payload, Map<String, String> operationProps, String tID, String queueName) {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "executeRFC");
            node = sapConn.executeRFC(payload, operationProps, tID, queueName);
            logger.info("SAPAdapterImpl executeRFC called, returned node size: " + node.size());
        } catch (JCoException | IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Get list of IDOC
     *
     * @return Json Node
     */
    @Override
    public JsonNode getIDOCList() {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getIDOCList");
            node = sapConn.getIDOCList();
            logger.info("SAPAdapterImpl getIDOCList called, returned node size: " + node.size());
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Get IDOC Schema based on the name provided
     *
     * @param idocType  Idoc Type
     * @param idocName  Idoc Name
     * @param revision  Idoc Revision
     * @param extension Idoc Extension
     * @return Idoc Schema in Json Node type
     */
    @Override
    public JsonNode getIdocSchema(String idocType, String idocName, String revision, String extension) {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getIdocSchema");
            node = sapSchema.getIdocSchema(idocType, idocName, revision, extension);
            logger.info("SAPAdapterImpl getIdocSchema called, returned node size: " + node.size());
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Activate Listener to create a bridge to communicate to SAP
     *
     * @param operationProps properties map
     * @param subscriber     subscriber
     * @param errorCapture   errorCapture
     * @param properties     properties
     * @return true if lister is activated
     */
    @Override
    public boolean subscribe(Map<String, String> operationProps, SapSubscriber subscriber,
                             ErrorCapture errorCapture, Properties properties) {
        logger.info("sapProps : " + sapConn);
        logger.info("operationProps : " + operationProps);
        JsonNode programIdJson = fetchProgramId();
        if (ValidationParameter.validateProgramID(programIdJson, properties)) {
            try {
                logger.info("server key " + sapProps.getServerName());
                logger.info("server key 1 " + regJcoListeners.get(sapProps.getServerName()));

                JCoListenerHandler lisHandler = regJcoListeners.computeIfAbsent(
                        sapProps.getServerName(), k -> new JCoListenerHandler(errorCapture, properties));
                lisHandler.addEndpoint(subscriber, operationProps);
                logger.info("lisHandler.isServerRunning() " + lisHandler.isServerRunning());
                if (!lisHandler.isServerRunning()) {
                    lisHandler.startServer();
                }
                logger.info("Registered JCo Listener" + regJcoListeners);
            } catch (Exception e) {
                throw SystemException.throwException("Exception Occured while subscribing the PROGRAM ID registration with SAP.",
                        new AdapterException(AdapterConstants.DESTINATION_EXCEPTION_120 + ""));
            }
        } else {
            throw SystemException.throwException("Invalid program ID", new AdapterException(
                    AdapterConstants.DESTINATION_EXCEPTION_120 + ""));
        }
        return true;
    }

    /**
     * Deactivate the listener
     *
     * @param operationProps Properties map
     * @return true if deactivated
     */
    @Override
    public boolean unSubscribe(Map<String, String> operationProps) {
        boolean isStarted = false;
        try {
            logger.info("SAPAdapterImpl method : " + "getIdocSchema");
            JCoListenerHandler lisHandler = regJcoListeners.get(sapProps.getServerName());
            lisHandler.removeEndpoint(operationProps);
            if (!lisHandler.isServerRunning())
                regJcoListeners.remove(sapProps.getServerName());
        } catch (Exception e) {
            throw SystemException.throwException("Failed while un-subscribing the IDoc outbound endpoint.",
                    new AdapterException(AdapterConstants.DESTINATION_EXCEPTION_120 + ""));
        }

        logger.info("Registered JCo Listener" + regJcoListeners);
        return isStarted;
    }

    /**
     * Get Outbound IDOC list
     *
     * @return Json Node
     */
    @Override
    public JsonNode getOutboundIdocList() {
        JsonNode node;
        try {
            logger.info("SAPAdapterImpl method : " + "getOutboundIdocList");
            node = sapConn.getOutboundIdocList();
            logger.info("SAPAdapterImpl getOutboundIdocList called, returned node size: " + node.size());
        } catch (JCoException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        return node;
    }

    /**
     * Get Outbound IDOC list
     *
     * @return Json Node
     */
    @Override
    public JsonNode postIDOC(String payload, String idocType, String idoc, String uniqueIdentifier,
                             String release) {
        logger.info("SAPAdapterImpl method : " + "postIDOC");
        JsonNode node = null;
        ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
        Properties properties = readPropertyFile.readPropertyFile();
        JsonNode programIdJson = fetchProgramId();
        if (ValidationParameter.validateProgramID(programIdJson, properties)) {
            try {
                node = sapConn.postIDOC(payload, idocType, idoc, uniqueIdentifier, release);
            } catch (JCoException | SystemException | IOException e) {
                throw SystemException.throwException(e.getMessage(), e);
            }
        }
        logger.info("SAPAdapterImpl postIDOC called, returned node size: " + node.size());
        return node;
    }

    public JsonNode fetchProgramId() {
        logger.info("SAPAdapterImpl method : " + "fetchProgramId");
        JsonNode node = null;
        Map<String, String> opProps = new HashMap<>();
        opProps.put("RFC", "RFC_READ_TABLE");
        try {
            JsonNode json = JSONReader.readJson("src/main/resources/RFC_READ_PRGM_ID.json");
            node = executeRFC(json.toString(), opProps, "", "");
        } catch (IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
        logger.info("SAPAdapterImpl postIDOC called, returned node size: " + node.size());
        return node;
    }

    public JsonNode getIdocStatus(String uniqueKey) {
        logger.info("SAPAdapterImpl method : " + "getIdocStatus");
        IDOCStatus idocStatus = new IDOCStatus();
        JsonNode node = idocStatus.getIdocStatus(uniqueKey, sapConn);
        logger.info("SAPAdapterImpl getIdocStatus called, returned node size: " + node.size());
        return node;
    }



 /* public static void main(String[] args) throws JCoException {

    String fileName = "Google_SAP_Connection.properties";
    ReadPropertyFile readPropertyFile = new ReadPropertyFile(fileName);
    Properties connection = readPropertyFile.readPropertyFile();
    ExceptionUtils exceptionUtils = new ExceptionUtils();
    ErrorCapture errorCapture = new ErrorCapture(exceptionUtils);
    SAPAdapterImpl sapAdapterImpl = new SAPAdapterImpl(errorCapture,connection);
//                  sapAdapterImpl.ping();
//                      JsonNode node = sapAdapterImpl.getBAPIList();
//    System.out.println(node.toString());
//        sapAdapterImpl.getBAPISchema("BusinessArea.GetDetail");
//            sapAdapterImpl.executeBAPI();

    Map<String, List<RFCMethod>> list = sapAdapterImpl.getRFCList(null,false);
    System.out.println(list.size());
//            Iterator itr = list.entrySet().iterator();
//            while(itr.hasNext()){
//                System.out.println(itr.next());
//            }
//            String rfcName = "dhi";
//            JsonNode node  = sapAdapterImpl.getRFCSchema(rfcName);
//            node.size();
  }*/
}

