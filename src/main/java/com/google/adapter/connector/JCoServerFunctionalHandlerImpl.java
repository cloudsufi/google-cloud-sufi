package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.IDocType;
import com.google.adapter.util.JsonUtil;
import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 */
public class JCoServerFunctionalHandlerImpl implements JCoServerFunctionHandler {
    public JCoListenerHandler listenerHandler = null;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JCoServerFunctionalHandlerImpl.class);

    public JCoServerFunctionalHandlerImpl(JCoListenerHandler listenerHandler) {
        this.listenerHandler = listenerHandler;
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {
        log.info("----------------------------------------------------------------");
        log.info("call              : " + function.getName());
        log.info("ConnectionId      : " + serverCtx.getConnectionID());
        log.info("SessionId         : " + serverCtx.getSessionID());
        log.info("TID               : " + serverCtx.getTID());
        log.info("repository name   : " + serverCtx.getRepository().getName());
        log.info("is in transaction : " + serverCtx.isInTransaction());
        log.info("is stateful       : " + serverCtx.isStatefulSession());
        log.info("----------------------------------------------------------------");
        log.info("gwhost: " + serverCtx.getServer().getGatewayHost());
        log.info("gwserv: " + serverCtx.getServer().getGatewayService());
        log.info("progid: " + serverCtx.getServer().getProgramID());
        log.info("----------------------------------------------------------------");
        log.info("attributes  : ");
        log.info(serverCtx.getConnectionAttributes().toString());
        log.info("----------------------------------------------------------------");
        log.info("CPIC conversation ID: "
                + serverCtx.getConnectionAttributes().getCPICConversationID());
        log.info("----------------------------------------------------------------");
        IDocType idocT = null;
        String fName = function.getName();
        JCoListenerHandler.MessageConf mc = null;
        String idocJson = null;
        String idocType = null;
        try {
            if (fName.equals("IDOC_INBOUND_ASYNCHRONOUS")) { // get IDOC template

                //JsonUtil.writeUsingFiles(fName.concat("_").concat(new Date(System.currentTimeMillis())
                        //.toString().replace(" ", "_").replace(":","_")).concat(".xml"), function.toXML());

                fName = function.getTableParameterList().getTable("IDOC_CONTROL_REC_40")
                        .getString("IDOCTYP");
                idocType = function.getTableParameterList().getTable("IDOC_CONTROL_REC_40")
                        .getString("MESTYP");
                String extension = function.getTableParameterList().getTable("IDOC_CONTROL_REC_40")
                        .getString("CIMTYP");
                String release = function.getTableParameterList().getTable("IDOC_CONTROL_REC_40")
                        .getString("DOCREL");
                fName = idocType + "." + fName + "." + extension + "." + release;
                mc = listenerHandler.getMessageConf(fName);
                if (mc == null) {
                    throw SystemException.throwException("Upcoming message " + fName
                            + " is not registered with endpoint.", new AdapterException(String.valueOf(AdapterConstants.JCO_RUNTIME_EXCEPTION)));
                }
                SAPDataConverter converter = listenerHandler.getDataConverter();
                String host = serverCtx.getConnectionAttributes().getPartnerHost();
                String sysNumber = serverCtx.getConnectionAttributes().getSystemNumber();
                System.out.println("==dest==" + host);
                System.out.println("==sysNumber==" + sysNumber);
//
                idocT = converter.getIdocTypeFromFunction(function, release, "");
                if (idocT != null) {
                    fName = idocT.getName();
                    log.info("Got IDOC in: " + fName);
                } else { // error in
                    throw SystemException.throwException("IDOC is null", new AdapterException(AdapterConstants.ADAPTER_EXCEPTION_1000 + ""));
                    // throw exception here...
                }

                if (idocT != null) {
                    idocJson = idocT.getIDocJsonFromJCONew(function, serverCtx.getTID(), false,
                            false, false);
                    JsonUtil.writeUsingFiles(fName.concat("_").concat(System.currentTimeMillis()+"").concat(".json"),idocJson);
                }
            } else {
                writeContentToFile(function, fName);
            }
        } catch (Exception e) {
            log.info("Error while receving IDOC : " + e.getMessage(), e);
            throw new AbapClassException(e);
        }
    }

    private void writeContentToFile(JCoFunction function, String type) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();
        SAPDataConverter converter = listenerHandler.getDataConverter();
        node = converter.getJsonReplyFromJCoForInbound(function, "", mapper, node);
        if (Objects.nonNull(node)) {
            JsonUtil.writeUsingFiles(function.getName().concat("_").concat(System.currentTimeMillis()+"").concat(".json"), node.toString());
        }
    }
}
