package com.jpmc.ecm.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * MCP protocol request.
 * Follows the Model Context Protocol specification.
 */
@Data
public class MCPRequest {
    
    /**
     * JSON-RPC version (always "2.0")
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    /**
     * Request ID for correlation
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Method name (e.g., "tools/call")
     */
    @JsonProperty("method")
    private String method;
    
    /**
     * Method parameters
     */
    @JsonProperty("params")
    private Params params;
    
    @Data
    public static class Params {
        
        /**
         * Tool name to execute
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * Tool arguments
         */
        @JsonProperty("arguments")
        private Map<String, Object> arguments;
    }
}
