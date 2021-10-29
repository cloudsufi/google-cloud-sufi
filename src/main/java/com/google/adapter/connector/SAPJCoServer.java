package com.google.adapter.connector;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.server.*;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 */
public class SAPJCoServer {
    private Properties properties;
    private SAPProperties sapProps = null;
    private JCoServer server = null;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SAPJCoServer.class);
    private SAPThrowableListener sapThrowableListener = null;
    private JcoServerStateChangeListenerImpl stateChangeListener = null;
    private JCoListenerHandler listenerHandler = null;

    public SAPJCoServer(JCoListenerHandler listenerHandler) {
        this.listenerHandler = listenerHandler;
        this.properties = listenerHandler.getConnectionProps();
        sapProps = SAPProperties.getDefault(properties);
    }

    /**
     * Start SAP Server
     * @throws Exception Exception
     */
    public void startServer() throws Exception {
        log.info("" + properties);
        sapProps.setSERVER_JCO_REP_DEST(sapProps.getDestinationName());
        ServerDataProviderImpl.getInstance().setDefinition(sapProps);
        try {
            server = JCoServerFactory.getServer(sapProps.getDestinationName());
        } catch (JCoException ex) {
            throw new RuntimeException("Unable to create the server" + sapProps.getServerName()
                    + ", because of " + ex.getMessage(), ex);
        }
        JCoServerFunctionHandler functionHandler =
                new JCoServerFunctionalHandlerImpl(listenerHandler);
        DefaultServerHandlerFactory.FunctionHandlerFactory factory =
                new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerHandler("GENERIC_HANDLER", functionHandler);
        server.setCallHandlerFactory(factory);
        SAPTIDHandlerImpl tidHandler = SAPTIDHandlerImpl.getInstance();
        server.setTIDHandler(tidHandler);
        sapThrowableListener = new SAPThrowableListener();
        server.addServerExceptionListener(sapThrowableListener);
        stateChangeListener = new JcoServerStateChangeListenerImpl();
        server.addServerStateChangedListener(stateChangeListener);
        if (server.getState() == JCoServerState.STOPPING) {
            int retry = 0;
            while (server.getState() != JCoServerState.STOPPED) {
                if (retry++ > 20) {
                    log.info("Server thread still not stopped after 1 min !");
                    // Remove the client pool which was created
                    // JCO.removeClientPool(clientPoolName);
                    server.stop();
                    server.release();
                    log.error("startServer(): [ " + // ((JCoServerFunctionHandler)this.mServer)
                            "thread still not stop after 1 min! ]");
                    throw new Exception("startServer(): [ " + // this.mServer.getID() +
                            "thread still not stop after 1 min! ]");
                }
                try {
                    log.info("Sleep for 3 seconds...waiting server to stop");
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    log.error("Thread sleep interrupted...");
                }
            }
        }
        if (server.getState() == JCoServerState.STOPPED) {
            this.server.start();
            log.info("Starting RFC server... " + server.getState());
        } else {
            log.info("RFC server running state..." + server.getState());
        }
        // Allow the server thread to stabilize first
        int ntries = 0;
        while (server.getState() != JCoServerState.ALIVE) {
            if (ntries++ > 20) {
                log.info("Server thread still not alive after 1 min !");

                server.stop();
                server.release();
                log.info("startServer(): [ " + // ((JCoServerFunctionHandler)this.mServer)
                        "thread still not alive after 1 min! ]");
                throw new Exception("startServer(): [ " + // this.mServer.getID() +
                        "thread still not alive after 1 min! ]");
            }
            try {
                log.info("Sleep for 3 seconds...");
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                log.info("Thread sleep interrupted...");
            }
        }
        if (server.getState() == JCoServerState.ALIVE) {
            log.info("SAPBAPIServer: thread started.");
        }
    }

    public void stopServer() throws Exception {
        log.info("SAPBAPIServer: STOP server..");
        try {
            if (server != null) {
                if (server.getState() == JCoServerState.ALIVE) {
                    server.stop();
                }
                server.removeServerExceptionListener(sapThrowableListener);
                server.removeServerStateChangedListener(stateChangeListener);
                server.release();
                log.info("SAPBAPIServer: Release server..");
                server = null;
            }
        } catch (Exception ex) {
            log.info("Exception encountered in stopServer()");
            throw new Exception(ex);
        }
    }
}
