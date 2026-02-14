package com.jpmc.ecm.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP Protocol Response model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {

    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private Object result;

    @JsonProperty("error")
    private McpError error;

    /**
     * Create a success response
     */
    public static McpResponse success(String id, Object result) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    /**
     * Create an error response
     */
    public static McpResponse error(String id, int code, String message) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(new McpError(code, message, null))
                .build();
    }
}
