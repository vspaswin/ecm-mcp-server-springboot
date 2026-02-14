package com.jpmc.ecm.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * MCP Protocol Request model.
 */
@Data
public class McpRequest {

    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    @JsonProperty("id")
    private String id;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private Map<String, Object> params;
}
