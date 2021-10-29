package com.google.adapter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.IDocType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author dhiraj.kumar
 */
public class JsonUtil {

  /**
   * To remove empty array nodes
   *
   * @param inputNode JsonNode
   */
  public static void removeEmptyNodes(JsonNode inputNode) {
    Iterator<Map.Entry<String, JsonNode>> fieldIterator = inputNode.fields();
    while (fieldIterator.hasNext()) {
      Map.Entry<String, JsonNode> entry = fieldIterator.next();
      if (entry.getValue().isArray()) {
        ArrayNode arrayNode = (ArrayNode) entry.getValue();
        for (int i = arrayNode.size() - 1; i >= 0; i--) {
          JsonNode child = arrayNode.get(i);
          removeIfObject(child, arrayNode, i);
        }
      } else if (entry.getValue().isObject()) {
        runForObject(entry, inputNode);
      }
    }
  }

  /**
   * Remove node if Object
   *
   * @param child     JsonNode
   * @param arrayNode ArrayNode
   * @param i         int
   */
  private static void removeIfObject(JsonNode child, ArrayNode arrayNode, int i) {
    if (child.isObject() && child.size() == 0) {
      arrayNode.remove(i);
    } else {
      removeEmptyNodes(child);
    }
  }


  /**
   * Set value of if Object
   *
   * @param entry     Map.Entry
   * @param inputNode JsonNode
   */
  private static void runForObject(Map.Entry<String, JsonNode> entry, JsonNode inputNode) {
    if (entry.getValue().isNull()) {
      ((ObjectNode) inputNode).remove(entry.getKey());
    } else {
      removeEmptyNodes(entry.getValue());
    }
  }


  /**
   * Method to create JSON payload for sap IDOC list
   * @param idocTypesList Idoc Type list
   * @return Json node
   */
  public static JsonNode getJson(List<IDocType> idocTypesList) {
    JsonNode json = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      json = mapper.valueToTree(idocTypesList);
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
    return json;
  }


  public static void writeUsingFiles(String fileName, String data) {
    try {
      Files.write(Paths.get(AdapterConstants.FILE_PATH_IDOC.concat("\\").concat(fileName)), data.getBytes());
    } catch (IOException e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

}
