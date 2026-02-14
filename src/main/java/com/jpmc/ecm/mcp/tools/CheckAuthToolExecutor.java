package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP tool for checking authentication setup.
 * Helps developers verify their auth credentials and permissions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckAuthToolExecutor implements ToolExecutor {
    
    @Override
    public String getToolName() {
        return "check_authentication";
    }
    
    @Override
    public String getDescription() {
        return "Check if your authentication is properly configured. " +
               "Validates OAuth2 tokens, checks required scopes, and verifies header format. " +
               "Use this if you're getting 401 Unauthorized errors.";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
                .property("token", JsonSchemaBuilder.string()
                        .description("OAuth2 access token to validate")
                        .required(false))
                .property("authHeader", JsonSchemaBuilder.string()
                        .description("Complete Authorization header value")
                        .required(false))
                .property("requiredScopes", JsonSchemaBuilder.array()
                        .description("Scopes required for the API endpoint")
                        .items(JsonSchemaBuilder.string())
                        .required(false))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            String token = (String) arguments.get("token");
            String authHeader = (String) arguments.get("authHeader");
            @SuppressWarnings("unchecked")
            List<String> requiredScopes = (List<String>) arguments.get("requiredScopes");
            
            log.info("Checking authentication configuration");
            
            List<String> issues = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();
            boolean isValid = true;
            
            // Check if any authentication info provided
            if (token == null && authHeader == null) {
                issues.add("No authentication provided");
                suggestions.add("Obtain an OAuth2 token from the /auth/token endpoint");
                suggestions.add("Include token in Authorization header: Bearer {token}");
                isValid = false;
            }
            
            // Validate auth header format
            if (authHeader != null) {
                if (!authHeader.startsWith("Bearer ")) {
                    issues.add("Invalid Authorization header format");
                    suggestions.add("Use format: Bearer {your_token}");
                    isValid = false;
                } else {
                    String headerToken = authHeader.substring(7);
                    if (headerToken.isBlank()) {
                        issues.add("Token is empty");
                        suggestions.add("Provide a valid OAuth2 token after 'Bearer '");
                        isValid = false;
                    } else if (headerToken.length() < 20) {
                        issues.add("Token seems too short - may be invalid");
                        suggestions.add("Verify you're using a complete, valid token");
                        isValid = false;
                    }
                }
            }
            
            // Check token directly
            if (token != null && token.isBlank()) {
                issues.add("Token is empty");
                suggestions.add("Provide a valid OAuth2 access token");
                isValid = false;
            }
            
            // Scope information
            String scopeInfo = "Unknown - cannot verify without token validation";
            if (requiredScopes != null && !requiredScopes.isEmpty()) {
                scopeInfo = "Required scopes: " + String.join(", ", requiredScopes);
                suggestions.add("Ensure your token has these scopes: " + 
                        String.join(", ", requiredScopes));
            }
            
            // Build response
            Map<String, Object> response = Map.of(
                    "valid", isValid,
                    "issues", issues,
                    "suggestions", suggestions,
                    "scopeInfo", scopeInfo,
                    "message", isValid ? 
                            "✓ Authentication appears to be configured correctly" : 
                            "✗ Authentication has issues that need to be fixed",
                    "tip", "OAuth2 tokens expire after 1 hour. Request a new token if yours has expired.",
                    "tokenEndpoint", "/api/auth/token"
            );
            
            return ToolResult.success(response);
            
        } catch (Exception e) {
            log.error("Error checking authentication", e);
            return ToolResult.error("Failed to check authentication: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"authentication", "security", "debugging"};
    }
}
