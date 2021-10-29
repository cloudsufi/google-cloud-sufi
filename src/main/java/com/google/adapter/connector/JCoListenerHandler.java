package com.google.adapter.connector;

import com.google.adapter.outbound.SapSubscriber;
import com.google.adapter.util.ErrorCapture;
import org.slf4j.LoggerFactory;

import java.util.*;


public class JCoListenerHandler {
    private static Map<String, List<Integer>> endpoints = new HashMap<>();
    private static Map<String, MessageConf> operations = new HashMap<>();
    private SAPJCoServer jcoServer = null;
    private SAPDataConverter converter;
    private boolean isServerRunning = false;
    private Properties properties;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SAPAdapterImpl.class);

    public JCoListenerHandler(ErrorCapture errorCapture, Properties properties) {
        this.properties = properties;
        converter = new SAPDataConverter(new SAPConnector(errorCapture, properties), errorCapture);
    }

    public SAPDataConverter getDataConverter() {
        return converter;
    }

    public Properties getConnectionProps() {
        return properties;
    }


    public void addEndpoint(SapSubscriber Subscriber, Map<String, String> operationProps) {
        String functionType = operationProps.get("type");
        if (functionType.equals("IDOC")) {
            String idocType = operationProps.get("idocType");
            String idoc = operationProps.get("idoc");
            String ext = "";
            String idocName = "";
            if (idoc.contains(".")) {
                String[] idocInfo = idoc.split("\\.");
                ext = idocInfo[1];
                idocName = idocInfo[0];
            } else {
                idocName = idoc;
            }
            String release = operationProps.getOrDefault("releaseNumber", "");

            idoc = idocType + "." + idocName + "." + ext + "." + release;
            registerEndpoints(idoc, Subscriber, operationProps);
        } else {
            String rfcName = operationProps.get("rfc");
            registerEndpoints(rfcName, Subscriber, operationProps);
        }

        log.info( "Registered JCo message endpoint" + operations);
    }

    /**
     * Common method to register IDOC, RFC and BAPI Inbound endpoints
     * @param name
     * @param sapSubscriber
     * @param operationProps
     */
    private void registerEndpoints(String name, SapSubscriber sapSubscriber, Map<String, String> operationProps) {
        operations.put(name, new MessageConf(sapSubscriber, operationProps));
        List<Integer> confs = endpoints.computeIfAbsent(name, k -> new ArrayList<>());
        if (confs.isEmpty()) {
            confs = new ArrayList<>();
            endpoints.put(name, confs);
        }

        if (confs.isEmpty()) {
            confs.add(1);
        } else {
            confs.add(confs.get(confs.size() - 1) + 1);
        }
    }


    public MessageConf getMessageConf(String function) {
        log.info("Registered message endpoint : " + operations);
        MessageConf mc = null;
        if (operations != null) {
            mc = operations.get(function);
        }
        return mc;
    }

    public void removeEndpoint(Map<String, String> operationProps) throws Exception {
        String functionType = operationProps.get("type");
        if (functionType.equals("IDOC")) {
            String idocType = operationProps.get("idocType");
            String idoc = operationProps.get("idoc");
            String ext = "";
            String idocName;
            if (idoc.contains(".")) {
                String[] idocInfo = idoc.split("\\.");
                ext = idocInfo[1];
                idocName = idocInfo[0];
            } else {
                idocName = idoc;
            }
            String release = operationProps.getOrDefault("releaseNumber", "");
            idoc = idocType + "." + idocName + "." + ext + "." + release;
            List messConfs = endpoints.computeIfAbsent(idoc, k -> new ArrayList<>());

            if (!messConfs.isEmpty()) {
                messConfs.remove(messConfs.get(messConfs.size() - 1));
            }
            if (messConfs.isEmpty()) {
                operations.remove(idoc);
                endpoints.remove(idoc);
            }
            if (endpoints.isEmpty()) {
                stopServer();
            }
        }
        log.info("Registered message endpoint : " + operations);
    }

    public void startServer() throws Exception {
        jcoServer = new SAPJCoServer(this);
        jcoServer.startServer();
        isServerRunning = true;
    }

    public void stopServer() throws Exception {
        if (jcoServer != null) {
            jcoServer.stopServer();
            isServerRunning = false;
        }
    }

    public boolean isServerRunning() {
        return isServerRunning;
    }

    class MessageConf {
        final static String RFC = "rfc";
        final static String BAPI = "bapi";
        final static String IDOC = "idoc";
        SapSubscriber messageListener;
        Map<String, String> operationProps;

        MessageConf(SapSubscriber messageListener,
                    Map<String, String> operationProps) {
            this.messageListener = messageListener;
            this.operationProps = operationProps;
        }
    }
}
