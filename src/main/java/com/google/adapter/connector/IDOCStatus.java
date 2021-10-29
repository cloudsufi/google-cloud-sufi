package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adapter.model.IDocType;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDOCStatus {
    private static final Logger log = Logger.getLogger(IDOCStatus.class.getName());


    /**
     * This method will return all the Idoc types configured with a RCVPRN value from sender
     * configuration
     * @param uniqueKey key based on which status will be returning
     * @param sapConn SAPConnector instance
     * @return status in form of JsonNode
     */
    public JsonNode getIdocStatus(String uniqueKey,SAPConnector sapConn) {

        try {
            JCoFunction function = sapConn.getRepository().getFunction("RFC_READ_TABLE");
            function.getImportParameterList().setValue("QUERY_TABLE", "EDIDC");
            function.getImportParameterList().setValue("DELIMITER", ";");
            JCoTable optionsTable = function.getTableParameterList().getTable("OPTIONS");
            optionsTable.appendRow();
            optionsTable.setValue("TEXT", "ARCKEY EQ '" + uniqueKey + "'");
            JCoTable fieldsTable = function.getTableParameterList().getTable("FIELDS");
            fieldsTable.appendRow();
            fieldsTable.setValue("FIELDNAME", "DOCNUM");
            fieldsTable.appendRow();
            fieldsTable.setValue("FIELDNAME", "STATUS");
            function.execute(sapConn.getDestination());
            JCoTable dataTable = function.getTableParameterList().getTable("DATA");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode idocNode = mapper.createObjectNode();
            for (int i = 0; i < dataTable.getNumRows(); i++) {
                dataTable.setRow(i);
                String rowData = dataTable.getString("WA");
                idocNode.put("idocNumber", rowData.split(";")[0]);
                String statusCode = rowData.split(";")[1];
                String[] statusDetail = mapper.convertValue(IDocType.IDOCStatus.get(statusCode), String[].class);
                idocNode.put("statusCode", statusCode);
                idocNode.put("status", statusDetail[0]);
                idocNode.put("description", statusDetail[1]);
            }
            return idocNode;
        } catch (Exception e) {
            log.log(
                    Level.SEVERE,
                    "Exception in getting idoc status: ",
                    "Error while fetching IDoc list for SAP outbound. Root cause: ");
        }
        return null;
    }


}
