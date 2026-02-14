package com.jpmc.ecm.dto.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

/**
 * Information about an MCP tool for discovery.
 */
@Data
@Builder
public class ToolInfo {
    
    /**
     * Tool name
     */
    private String name;
    
    /**
     * Tool description
     */
    private String description;
    
    /**
     * Input schema
     */
    private JsonNode inputSchema;
    
    /**
     * Tags for categorization
     */
    private String[] tags;
}
