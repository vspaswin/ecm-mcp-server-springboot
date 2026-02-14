package com.jpmc.ecm.mcp.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * Core interface for all MCP tool executors.
 * Each tool must implement this interface to be registered in the tool registry.
 * 
 * @author ECM MCP Team
 */
public interface ToolExecutor {
    
    /**
     * Get the unique name of this tool.
     * This name is used by MCP clients to invoke the tool.
     * 
     * @return tool name (e.g., "list_available_apis")
     */
    String getToolName();
    
    /**
     * Get a human-readable description of what this tool does.
     * This description is shown to AI models and developers.
     * 
     * @return tool description
     */
    String getDescription();
    
    /**
     * Get the JSON schema defining the tool's input parameters.
     * Use JsonSchemaBuilder to construct the schema.
     * 
     * @return JSON schema for input validation
     */
    JsonNode getInputSchema();
    
    /**
     * Execute the tool with the provided arguments.
     * This method should be idempotent and thread-safe.
     * 
     * @param arguments map of parameter name to value
     * @return execution result
     */
    ToolResult execute(Map<String, Object> arguments);
    
    /**
     * Get tags for categorizing this tool.
     * Default implementation returns empty array.
     * 
     * @return array of tags (e.g., ["documentation", "api"])
     */
    default String[] getTags() {
        return new String[0];
    }
}
