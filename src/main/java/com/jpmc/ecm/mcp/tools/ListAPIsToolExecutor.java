package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.dto.api.APIDocumentation;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.service.ApiKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tool for listing all available ECM APIs.
 * Helps developers discover what endpoints are available.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListAPIsToolExecutor implements ToolExecutor {
    
    private final ApiKnowledgeBaseService knowledgeBase;
    
    @Override
    public String getToolName() {
        return "list_available_apis";
    }
    
    @Override
    public String getDescription() {
        return "List all available ECM REST APIs. Returns endpoint paths, HTTP methods, and brief descriptions. " +
               "Use this to discover what operations you can perform with the ECM system.";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
                .property("category", JsonSchemaBuilder.string()
                        .description("Optional: Filter by category (documents, search, workflow, metadata)")
                        .required(false))
                .property("method", JsonSchemaBuilder.string()
                        .description("Optional: Filter by HTTP method (GET, POST, PUT, DELETE)")
                        .enumValues("GET", "POST", "PUT", "DELETE", "PATCH")
                        .required(false))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            log.info("Listing available APIs with filters: {}", arguments);
            
            String category = (String) arguments.get("category");
            String method = (String) arguments.get("method");
            
            // Get all APIs from knowledge base
            List<APIDocumentation> apis = knowledgeBase.getAllAPIs();
            
            // Apply filters
            if (category != null && !category.isBlank()) {
                apis = apis.stream()
                        .filter(api -> api.getTags() != null && 
                                api.getTags().contains(category.toLowerCase()))
                        .toList();
            }
            
            if (method != null && !method.isBlank()) {
                String methodUpper = method.toUpperCase();
                apis = apis.stream()
                        .filter(api -> api.getMethod().name().equals(methodUpper))
                        .toList();
            }
            
            // Convert to simple list format
            List<Map<String, Object>> result = apis.stream()
                    .map(api -> Map.of(
                            "path", api.getPath(),
                            "method", api.getMethod().name(),
                            "description", api.getDescription(),
                            "tags", api.getTags() != null ? api.getTags() : List.of(),
                            "authRequired", api.getAuthentication() != null && 
                                    api.getAuthentication().isMandatory()
                    ))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "totalCount", result.size(),
                    "apis", result,
                    "tip", "Use 'get_api_documentation' tool to get detailed information about any endpoint"
            );
            
            return ToolResult.success(response);
            
        } catch (Exception e) {
            log.error("Error listing APIs", e);
            return ToolResult.error("Failed to list APIs: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"discovery", "api", "documentation"};
    }
}
