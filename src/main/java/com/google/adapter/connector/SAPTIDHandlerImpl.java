package com.google.adapter.connector;

import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerTIDHandler;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 */
public class SAPTIDHandlerImpl implements JCoServerTIDHandler {
    public enum TIDState {
        CREATED, EXECUTED, COMMITTED, ROLLED_BACK
    }

    private Map<String, TIDState> availableTIDs = new Hashtable<>();
    private static final SAPTIDHandlerImpl instance = new SAPTIDHandlerImpl();

    private SAPTIDHandlerImpl() {
        // create a TID handler...
    }

    public static SAPTIDHandlerImpl getInstance() {
        return instance;
    }

    public boolean checkTID(JCoServerContext serverCtx, String tid) {
        // This example uses a Hashtable to store status information. But usually
        // you would use a database. If the DB is down, throw a RuntimeException at
        // this point. JCo will then abort the tRFC and the R/3 backend will try
        // again later.
        TIDState state = this.getTID(tid);
        if (state == null) {
            this.putTID(tid, TIDState.CREATED);
            return true;
        }
        return (state == TIDState.CREATED || state == TIDState.ROLLED_BACK);
        // "true" means that JCo will now execute the transaction, "false" means
        // that we have already executed this transaction previously, so JCo will
        // skip the handleRequest() step and will immediately return an OK code to R/3.
    }

    public void commit(JCoServerContext serverCtx, String tid) {
        // react on commit e.g. commit on the database
        // if necessary throw a RuntimeException, if the commit was not
        // possible
        this.putTID(tid, TIDState.COMMITTED);
    }

    public void rollback(JCoServerContext serverCtx, String tid) {
        this.putTID(tid, TIDState.ROLLED_BACK);
        // react on rollback e.g. rollback on the database
    }

    public void confirmTID(JCoServerContext serverCtx, String tid) {

        this.removeTID(tid);
    }

    public void execute(JCoServerContext serverCtx) {
        String tid = serverCtx.getTID();
        if (tid != null) {
            this.putTID(tid, TIDState.EXECUTED);
        }
    }

    // Synchornizing multi-thread acess of TID table
    public TIDState getTID(String tid) {
        return availableTIDs.get(tid);
    }

    public void putTID(String tid, TIDState state) {
        availableTIDs.put(tid, state);
    }

    public void removeTID(String tid) {
        availableTIDs.remove(tid);
    }
}
