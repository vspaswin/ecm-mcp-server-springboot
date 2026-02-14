package com.jpmc.ecm.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Information model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("inputSchema")
    private Map<String, Object> inputSchema;
}
