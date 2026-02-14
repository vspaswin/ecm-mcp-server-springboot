package com.jpmc.ecm.mcp.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized result object for MCP tool executions.
 * Encapsulates success/failure state, data, and metadata.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolResult {
    
    /**
     * Whether the tool execution was successful
     */
    private boolean success;
    
    /**
     * The result data (only present if success=true)
     */
    private Object data;
    
    /**
     * Error message (only present if success=false)
     */
    private String error;
    
    /**
     * Additional error details for debugging
     */
    private String errorDetails;
    
    /**
     * Execution timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Execution duration in milliseconds
     */
    private Long durationMs;
    
    /**
     * Additional metadata about the execution
     */
    private Map<String, Object> metadata;
    
    /**
     * Create a successful result with data
     */
    public static ToolResult success(Object data) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Create a successful result with data and metadata
     */
    public static ToolResult success(Object data, Map<String, Object> metadata) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .build();
    }
    
    /**
     * Create an error result with message
     */
    public static ToolResult error(String errorMessage) {
        return ToolResult.builder()
                .success(false)
                .error(errorMessage)
                .build();
    }
    
    /**
     * Create an error result with message and details
     */
    public static ToolResult error(String errorMessage, String errorDetails) {
        return ToolResult.builder()
                .success(false)
                .error(errorMessage)
                .errorDetails(errorDetails)
                .build();
    }
    
    /**
     * Create an error result from an exception
     */
    public static ToolResult error(Exception e) {
        return ToolResult.builder()
                .success(false)
                .error(e.getMessage())
                .errorDetails(e.getClass().getName())
                .build();
    }
}
