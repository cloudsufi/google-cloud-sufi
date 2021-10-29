package com.google.adapter.connector;

import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.AdapterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.util.ValidationParameter;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;

import java.util.Map;
import java.util.Properties;

/**
 * This class reads connection properties from properties file and sends it to JCo.
 *
 * @author dhiraj.kumar
 */
public class SAPProperties extends Properties {

  private static final long serialVersionUID;
  private Map<String, String> idocProfile = null;
  private Properties serverprops = new Properties();

  static {
    serialVersionUID = 1L;
  }

  /**
   * Public Constructor for SAPProperties
   */
  public SAPProperties() {
    super();
  }


  /**
   * Create Default Connection object for SAP Destination
   *
   * @param properties with connection details
   * @return Connection after reading connection parameter from connection file
   */
  public static SAPProperties getDefault(Properties properties) {
    ValidationParameter validationParameter = new ValidationParameter();
    SAPProperties connection = setSapProperties(properties);
    if (!validateParameters(properties, validationParameter)) {
      throw SystemException.throwException(validationParameter.getErrorMessage(),
              new AdapterException(Integer.toString(AdapterConstants.JCO_INVALID_PARAMETER_ERROR_CODE)));
    } else {
      return connection;
    }
  }


  /**
   * Set value into SAPProperties from properties file
   *
   * @param properties Properties
   * @return SAPProperties
   */
  private static SAPProperties setSapProperties(Properties properties) {
    SAPProperties connection = null;
    if (properties instanceof SAPProperties) {
      connection = (SAPProperties) properties;
    }
    return connection;
  }


  /**
   * Check Different Connection Parameters
   * @param properties Connection Properties
   * @param validationParameter ValidationParameter class object
   * @return true if all check passed
   */
  public static boolean validateParameters(Properties properties, ValidationParameter validationParameter) {
    return validationParameter.notNullConstraint(properties) && validationParameter.validate(properties) &&
            validationParameter.checkLang(properties) && validationParameter.checkPeakLimit(properties)
            &&
            validationParameter.checkPoolCapacity(properties) && validationParameter
            .checkClient(properties) && validationParameter.checkSNC(properties)
            && validationParameter.validateLoadBalancer(properties);
  }

  /**
   * Creates Destination name based on the connection properties provided
   *
   * @return Destination Name
   */
  public String getDestinationName() {
    return ((this.getProperty(DestinationDataProvider.JCO_ASHOST) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_ASHOST) + ":")
            + ((this.getProperty(DestinationDataProvider.JCO_MSHOST) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_MSHOST) + ":")
            + ((this.getProperty(DestinationDataProvider.JCO_MSSERV) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_MSSERV) + ":")
            + ((this.getProperty(DestinationDataProvider.JCO_CLIENT) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_CLIENT) + ":")
            + ((this.getProperty(DestinationDataProvider.JCO_USER) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_USER) + ":")
            + ((this.getProperty(DestinationDataProvider.JCO_LANG) == null) ? ""
            : this.getProperty(DestinationDataProvider.JCO_LANG) + ":");
  }

  public void setIdocprofiles(Map<String, String> profileParams) {
    this.idocProfile = profileParams;
  }

  public Map<String, String> getIdocprofiles() {
    return this.idocProfile;
  }

  /**
   * @param sERVER_JCO_REP_DEST the sERVER_JCO_REP_DEST to set
   */
  public void setSERVER_JCO_REP_DEST(String sERVER_JCO_REP_DEST) {
    put(ServerDataProvider.JCO_REP_DEST, sERVER_JCO_REP_DEST);
  }

  public Properties getServerProperties() {
    for (Object k : this.keySet()) {
      String key = (String) k;
      if (key.startsWith("ServerDataProvider")) {
        serverprops.put(k, get(k));
      } else if (key.startsWith("jco.server")) {
        serverprops.put(k, get(k));
      }
    }
    return serverprops;
  }

  public String getServerName() {
    return ((this.getProperty(ServerDataProvider.JCO_GWHOST) == null) ? ""
            : this.getProperty(ServerDataProvider.JCO_GWHOST) + ":")
            + ((this.getProperty(ServerDataProvider.JCO_GWSERV) == null) ? ""
            : this.getProperty(ServerDataProvider.JCO_GWSERV) + ":")
            + ((this.getProperty(ServerDataProvider.JCO_PROGID) == null) ? ""
            : this.getProperty(ServerDataProvider.JCO_PROGID) + ":");
  }
  /**
   * @return the destination_JCO_CLIENT
   */
  public String getDestination_JCO_CLIENT() {
    return (String) get(DestinationDataProvider.JCO_CLIENT);
  }
}
