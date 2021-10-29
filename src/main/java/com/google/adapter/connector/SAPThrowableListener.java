package com.google.adapter.connector;

import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SAPThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SAPThrowableListener.class);

    @Override
    public void serverExceptionOccurred(JCoServer jcoServer, String connectionID,
                                        JCoServerContextInfo serverContext, Exception error) {
        log.info("Exception while receiving idoc from SAP", error);
    }

    @Override
    public void serverErrorOccurred(JCoServer jcoServer, String connectionID,
                                    JCoServerContextInfo serverContext, Error error) {
        log.info("Error while receiving idoc from SAP", error);
    }
}
