package com.google.adapter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.connector.SAPAdapterImpl;
import com.google.adapter.connector.SAPConnector;
import com.google.adapter.connector.SAPProperties;
import com.google.adapter.connector.SAPSchema;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CommonTest {

    @Mock
    protected ErrorCapture errorCapture;
    @Mock
    protected JCoDestination jcoClient;
    @Mock
    protected SAPProperties sapProps;
    @Mock
    protected Properties properties;
    @Mock
    protected JsonNode jsonNode;
    @Mock
    protected ObjectMapper mapper;

    @InjectMocks
    protected SAPConnector sapConn;
    @InjectMocks
    protected SAPSchema sapSchema;
    @InjectMocks
    protected SAPAdapterImpl sapAdapter;

    public void init() throws Exception {
        errorCapture = mock(ErrorCapture.class);
        jcoClient = mock(JCoDestination.class);
        jsonNode = mock(JsonNode.class);
        mapper = mock(ObjectMapper.class);
        //validationParameter = mock(ValidationParameter.class);

        sapProps = spy(SAPProperties.getDefault(prepareConnectionProperties()));
        properties = spy(prepareConnectionProperties());
        sapConn = spy(new SAPConnector(errorCapture, sapProps));
        sapSchema = spy(new SAPSchema(sapConn, errorCapture));
        sapAdapter = spy(new SAPAdapterImpl(errorCapture, sapProps));

        when(properties.containsKey(anyString())).thenReturn(true);
        doReturn("").when(sapProps).getDestinationName();

        MockitoAnnotations.initMocks(this);
    }

    public Properties prepareConnectionProperties() {
        Properties properties = new SAPProperties();
        properties.put(DestinationDataProvider.JCO_CLIENT, "100");
        properties.put(DestinationDataProvider.JCO_ASHOST, "10.10.10.10");
        properties.put(DestinationDataProvider.JCO_LANG, "EN");
        properties.put(DestinationDataProvider.JCO_SYSNR, "00");
        properties.put(DestinationDataProvider.JCO_PEAK_LIMIT, "100");
        properties.put(DestinationDataProvider.JCO_POOL_CAPACITY, "3");
        properties.put(DestinationDataProvider.JCO_USER, "user");
        properties.put(DestinationDataProvider.JCO_PASSWD, "password");

        return properties;
    }
}
