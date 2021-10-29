package com.google.adapter.connector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Schema {
    private JsonNode REQUEST_DETAIL;
    private JsonNode RESPONSE_DETAIL;

    @JsonProperty("REQUEST_DETAIL")
    public JsonNode getREQUEST_DETAIL() {
        return REQUEST_DETAIL;
    }

    public void setREQUEST_DETAIL(JsonNode REQUEST_DETAIL) {
        this.REQUEST_DETAIL = REQUEST_DETAIL;
    }

    @JsonProperty("RESPONSE_DETAIL")
    public JsonNode getRESPONSE_DETAIL() {
        return RESPONSE_DETAIL;
    }

    public void setRESPONSE_DETAIL(JsonNode RESPONSE_DETAIL) {
        this.RESPONSE_DETAIL = RESPONSE_DETAIL;
    }
}
