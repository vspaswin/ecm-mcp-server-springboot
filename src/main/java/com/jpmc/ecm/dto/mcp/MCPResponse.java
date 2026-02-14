package com.jpmc.ecm.dto.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * MCP protocol response.
 * Follows the Model Context Protocol specification.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MCPResponse {
    
    /**
     * JSON-RPC version (always "2.0")
     */
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    /**
     * Request ID (echoed from request)
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Result (present if successful)
     */
    @JsonProperty("result")
    private Object result;
    
    /**
     * Error (present if failed)
     */
    @JsonProperty("error")
    private Error error;
    
    /**
     * Create success response
     */
    public static MCPResponse success(String id, Object result) {
        return MCPResponse.builder()
                .id(id)
                .result(result)
                .build();
    }
    
    /**
     * Create error response
     */
    public static MCPResponse error(String id, int code, String message) {
        return MCPResponse.builder()
                .id(id)
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
    
    /**
     * Create error response with details
     */
    public static MCPResponse error(String id, int code, String message, Object data) {
        return MCPResponse.builder()
                .id(id)
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .data(data)
                        .build())
                .build();
    }
    
    @Data
    @Builder
    public static class Error {
        
        /**
         * Error code
         */
        @JsonProperty("code")
        private int code;
        
        /**
         * Error message
         */
        @JsonProperty("message")
        private String message;
        
        /**
         * Additional error data
         */
        @JsonProperty("data")
        private Object data;
    }
}
