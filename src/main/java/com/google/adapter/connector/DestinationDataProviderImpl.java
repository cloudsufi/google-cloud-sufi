package com.google.adapter.connector;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation class of Destination Data Provider
 *
 * @author dhiraj.kumar
 */
public class DestinationDataProviderImpl implements DestinationDataProvider {

  private static final DestinationDataProviderImpl instance = new DestinationDataProviderImpl();
  private Map<String, SAPProperties> defs = new HashMap<>();
  private DestinationDataEventListener eventListener;

  /**
   * Protected constructor
   */
  protected DestinationDataProviderImpl() {
  }

  /**
   * Returns the instance of this class
   *
   * @return Instance of Destination Data Provider
   */
  public static DestinationDataProviderImpl getInstance() {
    return instance;
  }

  /**
   * Returns Destination properties if it matches destination key
   *
   * @param key Destination key
   * @return destination properties
   */
  @Override
  public Properties getDestinationProperties(String key) {
    if (defs.containsKey(key)) {
      return defs.get(key);
    }
    return null;
  }

  /**
   * It will create destination based on the key for the first time and return the same destination
   * again if passed with same properties
   *
   * @param properties SAP Connecting properties
   */
  public void setProperties(SAPProperties properties) {
    if (defs.isEmpty() && !Environment.isDestinationDataProviderRegistered()) // first use..
    {
      Environment.registerDestinationDataProvider(instance);
    }
    SAPProperties definitionProvider = defs.put(properties.getDestinationName(), properties);
    if (definitionProvider != null) {
      this.eventListener.updated(definitionProvider.getDestinationName());
    }

  }

  /**
   * Enabling Destination data event listener
   *
   * @param eventListener event listener to check which destination object need to be returned
   */
  @Override
  public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
    this.eventListener = eventListener;
  }

  /**
   * Return event listener object
   *
   * @return the eventListener on destination data object
   */
  public DestinationDataEventListener getEventListener() {
    return eventListener;
  }

  /**
   * Returns if it supports event or not
   *
   * @return true if event listener is supported.
   */
  @Override
  public boolean supportsEvents() {
    return true;
  }
}
