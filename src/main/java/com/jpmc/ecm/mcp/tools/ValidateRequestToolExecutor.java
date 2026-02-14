package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.dto.api.ValidationResult;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.service.RequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tool for validating API requests before sending them.
 * Helps developers catch errors before making actual API calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateRequestToolExecutor implements ToolExecutor {
    
    private final RequestValidationService validationService;
    
    @Override
    public String getToolName() {
        return "validate_request";
    }
    
    @Override
    public String getDescription() {
        return "Validate an API request before sending it. Checks for missing mandatory fields, " +
               "invalid data types, authentication headers, and business rule compliance. " +
               "Use this to avoid errors and ensure your request will succeed.";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
                .property("endpoint", JsonSchemaBuilder.string()
                        .description("API endpoint path")
                        .required(true))
                .property("method", JsonSchemaBuilder.string()
                        .description("HTTP method")
                        .enumValues("GET", "POST", "PUT", "DELETE", "PATCH")
                        .required(true))
                .property("requestBody", JsonSchemaBuilder.object()
                        .description("Request body/payload")
                        .required(false))
                .property("headers", JsonSchemaBuilder.object()
                        .description("HTTP headers including authentication")
                        .required(false))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            String endpoint = (String) arguments.get("endpoint");
            String methodStr = (String) arguments.get("method");
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = (Map<String, Object>) arguments.get("requestBody");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) arguments.get("headers");
            
            if (endpoint == null || methodStr == null) {
                return ToolResult.error("Both 'endpoint' and 'method' are required");
            }
            
            log.info("Validating request: {} {}", methodStr, endpoint);
            
            HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
            
            // Perform validation
            ValidationResult result = validationService.validate(
                    endpoint, method, requestBody, headers);
            
            // Build response
            Map<String, Object> response = Map.of(
                    "valid", result.isValid(),
                    "errors", result.getErrors(),
                    "warnings", result.getWarnings(),
                    "message", result.isValid() ? 
                            "✓ Request is valid and ready to send" : 
                            "✗ Request has validation errors that must be fixed",
                    "tip", result.isValid() ? 
                            "You can now make the API call with confidence" : 
                            "Fix the errors listed above and validate again"
            );
            
            return ToolResult.success(response);
            
        } catch (Exception e) {
            log.error("Error validating request", e);
            return ToolResult.error("Failed to validate request: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"validation", "testing", "debugging"};
    }
}
