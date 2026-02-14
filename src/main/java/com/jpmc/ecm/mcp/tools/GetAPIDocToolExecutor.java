package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.dto.api.APIDocumentation;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.service.ApiKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tool for getting detailed API documentation.
 * Provides complete information about a specific endpoint including parameters, examples, and errors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetAPIDocToolExecutor implements ToolExecutor {
    
    private final ApiKnowledgeBaseService knowledgeBase;
    
    @Override
    public String getToolName() {
        return "get_api_documentation";
    }
    
    @Override
    public String getDescription() {
        return "Get comprehensive documentation for a specific API endpoint. " +
               "Returns mandatory fields, optional parameters, authentication requirements, " +
               "common errors, and code examples. Use this to understand how to call an API correctly.";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
                .property("endpoint", JsonSchemaBuilder.string()
                        .description("API endpoint path (e.g., '/api/v1/documents')")
                        .required(true))
                .property("method", JsonSchemaBuilder.string()
                        .description("HTTP method")
                        .enumValues("GET", "POST", "PUT", "DELETE", "PATCH")
                        .required(true))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            String endpoint = (String) arguments.get("endpoint");
            String methodStr = (String) arguments.get("method");
            
            if (endpoint == null || methodStr == null) {
                return ToolResult.error("Both 'endpoint' and 'method' are required");
            }
            
            log.info("Getting API documentation for: {} {}", methodStr, endpoint);
            
            HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
            
            // Get API from knowledge base
            APIDocumentation api = knowledgeBase.findEndpoint(endpoint, method)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "API endpoint not found: " + methodStr + " " + endpoint));
            
            // Build comprehensive response
            Map<String, Object> response = new HashMap<>();
            response.put("endpoint", api.getPath());
            response.put("method", api.getMethod().name());
            response.put("description", api.getDescription());
            response.put("baseUrl", api.getBaseUrl());
            
            // Mandatory fields
            if (api.getMandatoryFields() != null && !api.getMandatoryFields().isEmpty()) {
                response.put("mandatoryFields", api.getMandatoryFields().stream()
                        .map(param -> Map.of(
                                "name", param.getName(),
                                "type", param.getType(),
                                "description", param.getDescription(),
                                "example", param.getExample() != null ? param.getExample() : "N/A",
                                "allowedValues", param.getAllowedValues() != null ? 
                                        param.getAllowedValues() : List.of()
                        ))
                        .collect(Collectors.toList()));
            }
            
            // Optional fields
            if (api.getOptionalFields() != null && !api.getOptionalFields().isEmpty()) {
                response.put("optionalFields", api.getOptionalFields().stream()
                        .map(param -> Map.of(
                                "name", param.getName(),
                                "type", param.getType(),
                                "description", param.getDescription(),
                                "example", param.getExample() != null ? param.getExample() : "N/A"
                        ))
                        .collect(Collectors.toList()));
            }
            
            // Authentication
            if (api.getAuthentication() != null) {
                response.put("authentication", Map.of(
                        "type", api.getAuthentication().getType(),
                        "mandatory", api.getAuthentication().isMandatory(),
                        "headerName", api.getAuthentication().getHeaderName() != null ? 
                                api.getAuthentication().getHeaderName() : "Authorization",
                        "format", api.getAuthentication().getFormat() != null ? 
                                api.getAuthentication().getFormat() : "Bearer {token}",
                        "scopes", api.getAuthentication().getScopes() != null ? 
                                api.getAuthentication().getScopes() : List.of()
                ));
            }
            
            // Common errors
            if (api.getCommonErrors() != null && !api.getCommonErrors().isEmpty()) {
                response.put("commonErrors", api.getCommonErrors().stream()
                        .map(error -> Map.of(
                                "code", error.getCode(),
                                "reason", error.getReason(),
                                "solution", error.getSolution()
                        ))
                        .collect(Collectors.toList()));
            }
            
            // Code examples
            if (api.getCodeExamples() != null && !api.getCodeExamples().isEmpty()) {
                response.put("codeExamples", api.getCodeExamples());
            }
            
            response.put("tip", "Use 'validate_request' before calling the API to check if your request is valid");
            
            return ToolResult.success(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("API not found: {}", e.getMessage());
            return ToolResult.error(e.getMessage(), 
                    "Use 'list_available_apis' to see all available endpoints");
        } catch (Exception e) {
            log.error("Error getting API documentation", e);
            return ToolResult.error("Failed to get API documentation: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"documentation", "api", "reference"};
    }
}
