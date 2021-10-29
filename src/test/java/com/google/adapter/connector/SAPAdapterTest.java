package com.google.adapter.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.connector.model.Schema;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.util.CommonTest;
import com.sap.conn.jco.*;
import com.sap.conn.jco.rt.AbapFunctionTemplate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SAPAdapterTest extends CommonTest {

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testPing() {
        boolean isSuccessful = sapAdapter.ping();

        Assertions.assertTrue(isSuccessful);
        verify(sapAdapter, times(1)).ping();
    }

    public ObjectInputStream readFile(String fileName){
        ObjectInputStream objectInputStream = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            objectInputStream = new ObjectInputStream(fin);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return objectInputStream;
    }

    @Test
    public void testBAPIList_FlowTest() throws JCoException {
        JCoRepository repository = mock(JCoRepository.class);
        JCoFunction function = mock(JCoFunction.class);
        JCoParameterList parameterList = mock(JCoParameterList.class);
        JCoTable table = mock(JCoTable.class);

        when(jcoClient.getRepository()).thenReturn(repository);
        when(repository.getFunction(anyString())).thenReturn(function);
        when(function.getTableParameterList()).thenReturn(parameterList);
        when(parameterList.getTable(anyString())).thenReturn(table);

        sapAdapter.getBAPIList();

        verify(sapAdapter, times(1)).getBAPIList();
    }

    @Test
    public void testBAPISchema_FromFile() throws JCoException, IOException, ClassNotFoundException {
        SAPDataConverter converter = mock(SAPDataConverter.class);
        JCoRepository repository = mock(JCoRepository.class);
        JCoFunction function = mock(JCoFunction.class);
        JCoParameterList parameterList = mock(JCoParameterList.class);
        JCoTable table = mock(JCoTable.class);
        AbapFunctionTemplate functionTemplate = null;
        ObjectInputStream objectInputStream = readFile("src/test/resources/bapi_function_template_v3_1_2.obj");
        functionTemplate = spy((AbapFunctionTemplate) objectInputStream.readObject());

        when(jcoClient.getRepository()).thenReturn(repository);
        when(repository.getFunction(anyString())).thenReturn(function);
        when(function.getImportParameterList()).thenReturn(parameterList);
        when(function.getTableParameterList()).thenReturn(parameterList);
        when(function.getExportParameterList()).thenReturn(parameterList);
        when(parameterList.getTable(anyString())).thenReturn(table);
        when(table.getNumRows()).thenReturn(1);

        when(table.getString(AdapterConstants.OBJTYPE)).thenReturn("");
        when(table.getString(AdapterConstants.TYPEKIND)).thenReturn("");
        when(sapConn.getRepository()).thenReturn(repository);

        when(repository.getFunctionTemplate("BAPI_ALM_ORDER_MAINTAIN")).thenReturn(functionTemplate);
        when(converter.getBoType(anyString())).thenReturn("BAPI_ALM_ORDER_MAINTAIN");
        when(table.getString(AdapterConstants.ABAPNAME)).thenReturn("BAPI_ALM_ORDER_MAINTAIN");

        JsonNode actualNode = sapAdapter.getBAPISchema("bapi.name");
        JsonNode expectedNode = mapper.readTree(new File("src/test/resources/BAPI_Schema.json"));

        Assertions.assertEquals(expectedNode.size(), actualNode.size());
        verify(sapAdapter, times(1)).getBAPISchema(anyString());
    }

    @Test
    public void testRFCSchema_FromFile() throws JCoException, IOException, ClassNotFoundException {
        SAPDataConverter converter = mock(SAPDataConverter.class);
        JCoRepository repository = mock(JCoRepository.class);
        JCoFunction function = mock(JCoFunction.class);
        JCoParameterList parameterList = mock(JCoParameterList.class);
        JCoTable table = mock(JCoTable.class);
        AbapFunctionTemplate functionTemplate = null;

        ObjectInputStream objectInputStream = readFile("src/test/resources/rfc_function_template_v3_1_2.obj");
        functionTemplate = spy((AbapFunctionTemplate) objectInputStream.readObject());


        when(jcoClient.getRepository()).thenReturn(repository);
        when(repository.getFunction(anyString())).thenReturn(function);
        when(function.getImportParameterList()).thenReturn(parameterList);
        when(function.getTableParameterList()).thenReturn(parameterList);
        when(function.getExportParameterList()).thenReturn(parameterList);
        when(parameterList.getTable(anyString())).thenReturn(table);
        when(table.getNumRows()).thenReturn(1);

        when(table.getString(AdapterConstants.OBJTYPE)).thenReturn("");
        when(table.getString(AdapterConstants.TYPEKIND)).thenReturn("");
        when(sapConn.getRepository()).thenReturn(repository);

        when(repository.getFunctionTemplate("RFC_READ_TABLE")).thenReturn(functionTemplate);
        when(converter.getBoType(anyString())).thenReturn("RFC_READ_TABLE");
        when(table.getString(AdapterConstants.ABAPNAME)).thenReturn("RFC_READ_TABLE");

        JsonNode actualNode = sapAdapter.getRFCSchema("RFC_READ_TABLE");
        JsonNode expectedNode = mapper.readTree(new File("src/test/resources/RFC_Schema.json"));

        Assertions.assertEquals(expectedNode.size(), actualNode.size());
        verify(sapAdapter, times(1)).getRFCSchema(anyString());
    }

    @Test
    public void testRFCSchema_FlowTest() throws JCoException {
        JCoRepository repository = mock(JCoRepository.class);
        JCoFunction function = mock(JCoFunction.class);
        JCoParameterList parameterList = mock(JCoParameterList.class);
        JCoTable table = mock(JCoTable.class);
        JCoFunctionTemplate functionTemplate = mock(JCoFunctionTemplate.class);
        JCoListMetaData jCoListMetaData = mock(JCoListMetaData.class);

        when(jcoClient.getRepository()).thenReturn(repository);
        when(repository.getFunction(anyString())).thenReturn(function);
        when(function.getImportParameterList()).thenReturn(parameterList);
        when(function.getTableParameterList()).thenReturn(parameterList);
        when(parameterList.getTable(anyString())).thenReturn(table);
        when(table.getNumRows()).thenReturn(1);

        when(table.getString(AdapterConstants.OBJTYPE)).thenReturn("");
        when(sapConn.getRepository()).thenReturn(repository);

        when(repository.getFunctionTemplate(anyString())).thenReturn(functionTemplate);
        when(functionTemplate.getChangingParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getImportParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getExportParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getTableParameterList()).thenReturn(jCoListMetaData);

        JsonNode actualNode = sapAdapter.getRFCSchema("RFC_NAME");
        JsonNode expectedNode = mapper.convertValue(new Schema(), JsonNode.class);

        Assertions.assertEquals(expectedNode, actualNode);
        verify(sapAdapter, times(1)).getRFCSchema(anyString());
    }

    @Test
    public void testBAPISchema_FlowTest() throws JCoException {
        SAPDataConverter converter = mock(SAPDataConverter.class);
        JCoRepository repository = mock(JCoRepository.class);
        JCoFunction function = mock(JCoFunction.class);
        JCoParameterList parameterList = mock(JCoParameterList.class);
        JCoTable table = mock(JCoTable.class);
        JCoFunctionTemplate functionTemplate = mock(JCoFunctionTemplate.class);
        JCoListMetaData jCoListMetaData = mock(JCoListMetaData.class);

        when(jcoClient.getRepository()).thenReturn(repository);
        when(repository.getFunction(anyString())).thenReturn(function);
        when(function.getImportParameterList()).thenReturn(parameterList);
        when(function.getTableParameterList()).thenReturn(parameterList);
        when(parameterList.getTable(anyString())).thenReturn(table);
        when(table.getNumRows()).thenReturn(1);

        when(table.getString(AdapterConstants.OBJTYPE)).thenReturn("");
        when(sapConn.getRepository()).thenReturn(repository);

        when(repository.getFunctionTemplate(anyString())).thenReturn(functionTemplate);
        when(functionTemplate.getChangingParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getImportParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getExportParameterList()).thenReturn(jCoListMetaData);
        when(functionTemplate.getTableParameterList()).thenReturn(jCoListMetaData);
        when(converter.getBoType(anyString())).thenReturn("RFC_NAME");
        when(table.getString(AdapterConstants.ABAPNAME)).thenReturn("RFC_NAME");

        JsonNode actualNode = sapAdapter.getBAPISchema("bapi.name");
        JsonNode expectedNode = mapper.convertValue(new Schema(), JsonNode.class);

        Assertions.assertEquals(expectedNode, actualNode);
        verify(sapAdapter, times(1)).getBAPISchema(anyString());
    }

    @Test
    public void testSystemException() throws JCoException {

        doThrow(JCoException.class).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.getBAPISchema("bapi.name");
        });

        Assertions.assertEquals(exception.getClass(), SystemException.class);
    }

    @Test
    public void testPingErrorCode101() throws JCoException {
        JCoException jException = new JCoException(101,"Exception caused due to Missing/Invalid parameters");
        doThrow(jException).when(jcoClient).ping();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.ping();
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("650"));
    }

    @Test
    public void testPingConnectivity() throws JCoException {
        JCoException jException = new JCoException(102,"Connectivity Problem");
        doThrow(jException).when(jcoClient).ping();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.ping();
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("650"));
    }

    @Test
    public void testPingAuthorization() throws JCoException {
        JCoException jException = new JCoException(103,"Authorization Problem");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.getRFCSchema("RFC_READ_TABLE");
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("651"));
    }

    @Test
    public void testExecuteRFCErrorCode104() throws JCoException {
        HashMap<String, String> opProps = new HashMap<>();
        opProps.put("type", "RFC");
        opProps.put("BAPI", "RFC_READ_TABLE");
        opProps.put("autoCommit", "false");

        JCoException jException = new JCoException(104,"Authorization Problem");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.executeRFC("{\"payload\": \"test\"}", opProps, "", "");;
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("653"));
    }

    @Test
    public void testRFCListErrorCode106() throws JCoException {
        JCoException jException = new JCoException(106,"Resource Unavailable");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.getRFCList(null,true);
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("650"));
    }

    @Test
    public void testBAPIListErrorCode111() throws JCoException {
        JCoException jException = new JCoException(111,"Resource Unavailable");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.getBAPIList();
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("651"));
    }

    @Test
    public void testExecuteBAPIErrorCode122() throws JCoException {
        HashMap<String, String> opProps = new HashMap<>();
        opProps.put("type", "BAPI");
        opProps.put("BAPI", "PurchaseOrderFRE.CreateFromData1");
        opProps.put("autoCommit", "false");

        JCoException jException = new JCoException(122,"Invalid Payload");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.executeRFC("{\"payload\": \"test\"}", opProps, "", "");;
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("651"));
    }

    @Test
    public void testBAPISchemaErrorCode1001() throws JCoException {
        JCoException jException = new JCoException(1001,"Schema Not Found");
        doThrow(jException).when(jcoClient).getRepository();

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            sapAdapter.getBAPISchema("MaintenanceOrderBAPI.Order");
        });
        MatcherAssert
                .assertThat(((SystemException) exception).getErrorDetails().getPrimaryCode(),
                        Matchers.is("652"));
    }
}
