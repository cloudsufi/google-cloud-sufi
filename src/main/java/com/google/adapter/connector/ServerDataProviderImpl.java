package com.google.adapter.connector;

import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SAP server data provider to keep track server definitions associated with the SAP adapter
 * 
 */
public class ServerDataProviderImpl implements ServerDataProvider {
    private static final ServerDataProviderImpl instance = new ServerDataProviderImpl();
    private Map<String, SAPProperties> defs = new HashMap<>();

    protected ServerDataProviderImpl() {
        // Exists only to define instantiation.
    }

    public static ServerDataProviderImpl getInstance() {
        return instance;
    }

    public void clearDefinition() {
        if (defs != null) {
            defs.clear();
        }
    }

    public void setDefinition(SAPProperties def) {
        synchronized (this) {
            if (defs.isEmpty() && !Environment.isServerDataProviderRegistered()) { // first use...
                Environment.registerServerDataProvider(this);
            }
            defs.put(def.getDestinationName(), def);
        }
    }

    @Override
    public Properties getServerProperties(String name) {
        SAPProperties def = defs.get(name);
        return def.getServerProperties();
    }

    @Override
    public void setServerDataEventListener(ServerDataEventListener eventListener) {}

    @Override
    public boolean supportsEvents() {
        return false;
    }
}
