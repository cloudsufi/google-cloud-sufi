package com.google.adapter.connector;

import com.google.adapter.outbound.SapSubscriber;
import com.google.adapter.util.ErrorCapture;
import com.sap.conn.jco.JCoException;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author dhiraj.kumar
 */
public interface SAPAdapter {

  /**
   * This method will help in testing the sap credentials by ping to sap
   * @return true if connection is successful
   * @throws JCoException JCoException
   */
  boolean ping() throws JCoException;

  /**
   * This method will help in testing the sap credentials by ping to sap
   * and return SSO Token for future connectivity.
   * @return SSO Token if connection is successful
   * @throws JCoException JCoException
   */
  String generateSSO() throws JCoException;


  /**

   * @param refreshRequired Is caching required or not
   * @param filterText      String based on which list will be populated
   * @return JsonNode where key is String and value is RFCMethod
   */
  /**
   * Get List of RFC and store into Map
   * @param filterText String based on which list will be populated
   * @param refreshRequired Is caching required or not
   * @return RFCList in the form of JsonNode
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @throws IOException IOException
   * @throws ParseException ParseException
   */
  JsonNode getRFCList(String filterText, boolean refreshRequired)
          throws InterruptedException, ExecutionException, IOException, ParseException;

  /**
   * Get List of RFC and store into Map
   * @param rfcName RFC Name
   * @return JsonNode where key is String and value is RFCMethod
   * @throws JCoException JCoException
   */
  JsonNode getRFCSchema(String rfcName) throws JCoException;

  /**
   * Get List of BAPI and store into Map
   * @param bapiName String
   * @return JsonNode where key is String and value is RFCMethod
   * @throws JCoException JCoException
   */
  JsonNode getBAPISchema(String bapiName) throws JCoException;

  /**
   *
   * @param bapiName BAPI Name
   * @param operationProps Properties which are needed BAPI Execution
   * @param tID Transaction ID
   * @param queueName Queue Name
   * @return Json Node
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  JsonNode executeBAPI(String bapiName, Map<String, String> operationProps,String tID, String queueName)
          throws JCoException, IOException;

  /**
   * Get List of BAPI in JSON Node
   * @return List of BAPI
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  JsonNode getBAPIList() throws JCoException, IOException;

  /**
   *
   * @param payload RFC Name
   * @param operationProps Properties which are needed RFC Execution
   * @param tID Transactional ID
   * @param queueName Queue Name
   * @return JSON node
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  JsonNode executeRFC(String payload, Map<String, String> operationProps,String tID, String queueName)
          throws JCoException, IOException;

  /**
   * Get list of IDOC
   * @return Json Node
   * @throws JCoException JCoException
   */
  JsonNode getIDOCList() throws JCoException;

  /**
   *
   * Get IDOC Schema based on the name provided
   * @param idocType Idoc Type
   * @param idocName Idoc Name
   * @param revision Idoc Revision
   * @param extension Idoc Extension
   * @return Idoc Schema in Json Node type
   * @throws JCoException JCoException
   */
  JsonNode getIdocSchema(String idocType, String idocName, String revision, String extension)
          throws JCoException;

  /**
   *
   * Activate Listener to create a bridge to communicate to SAP
   * @param operationProps properties map
   * @param subscriber subscriber
   * @param errorCapture errorCapture
   * @param properties properties
   * @return true if lister is activated
   * @throws Exception Exception
   */
  boolean subscribe(Map<String, String> operationProps, SapSubscriber subscriber,
                    ErrorCapture errorCapture, Properties properties) throws Exception;

  /**
   * Deactivate the listener
   * @param operationProps Properties map
   * @return true if deactivated
   * @throws Exception Exception
   */
  boolean unSubscribe(Map<String, String> operationProps) throws Exception;

  /**
   * Get Outbound IDOC list
   * @return Json Node
   * @throws JCoException JCoException
   */
  JsonNode getOutboundIdocList() throws JCoException;


  /**
   * Get Outbound IDOC list
   * @param payload Payload
   * @param idocType Type of IDOC
   * @param idoc Idoc Name
   * @param uniqueIdentifier Unique Identifier
   * @param release Release number
   * @return Json Node
   * @throws JCoException JCoException
   * @throws IOException IOException
   */
  JsonNode postIDOC(String payload, String idocType, String idoc, String uniqueIdentifier, String release)
          throws JCoException, IOException;
}
