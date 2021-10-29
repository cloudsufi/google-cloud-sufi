package com.google.adapter.connector;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;
import org.slf4j.LoggerFactory;


public class JcoServerStateChangeListenerImpl implements JCoServerStateChangedListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JcoServerStateChangeListenerImpl.class);
    public void serverStateChangeOccurred(JCoServer server, JCoServerState oldState,
                                          JCoServerState newState) {
       log.info("Server state changed from " + oldState.toString() + " to "
                + newState.toString() + " on server with program id " + server.getProgramID());
    }
}
