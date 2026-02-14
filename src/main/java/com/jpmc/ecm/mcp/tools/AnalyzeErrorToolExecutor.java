package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.dto.api.ErrorAnalysisResult;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.service.ErrorAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tool for analyzing API error responses.
 * Helps developers understand what went wrong and how to fix it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeErrorToolExecutor implements ToolExecutor {
    
    private final ErrorAnalysisService errorAnalysisService;
    
    @Override
    public String getToolName() {
        return "analyze_error_response";
    }
    
    @Override
    public String getDescription() {
        return "Analyze an API error response to understand what went wrong and how to fix it. " +
               "Provides root cause analysis, likely causes, step-by-step solutions, and code examples. " +
               "Use this when you get an error from the API and need help troubleshooting.";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
                .property("statusCode", JsonSchemaBuilder.integer()
                        .description("HTTP status code of the error (e.g., 401, 400, 500)")
                        .required(true)
                        .minimum(400)
                        .maximum(599))
                .property("responseBody", JsonSchemaBuilder.string()
                        .description("Error response body from the API")
                        .required(false))
                .property("requestHeaders", JsonSchemaBuilder.object()
                        .description("Headers sent with the request")
                        .required(false))
                .property("requestBody", JsonSchemaBuilder.string()
                        .description("Body/payload sent with the request")
                        .required(false))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            Integer statusCode = (Integer) arguments.get("statusCode");
            String responseBody = (String) arguments.get("responseBody");
            @SuppressWarnings("unchecked")
            Map<String, String> requestHeaders = (Map<String, String>) arguments.get("requestHeaders");
            String requestBody = (String) arguments.get("requestBody");
            
            if (statusCode == null) {
                return ToolResult.error("'statusCode' is required");
            }
            
            log.info("Analyzing error: HTTP {}", statusCode);
            
            HttpStatus status = HttpStatus.valueOf(statusCode);
            
            // Perform error analysis
            ErrorAnalysisResult analysis = errorAnalysisService.analyze(
                    status, responseBody, requestHeaders, requestBody);
            
            // Build response
            Map<String, Object> response = Map.of(
                    "errorCode", analysis.getErrorCode(),
                    "diagnosis", analysis.getDiagnosis(),
                    "likelyCauses", analysis.getLikelyCauses(),
                    "solution", analysis.getSolution(),
                    "codeExample", analysis.getCodeExample() != null ? 
                            analysis.getCodeExample() : "No code example available",
                    "relatedDocs", analysis.getRelatedDocs() != null ? 
                            analysis.getRelatedDocs() : List.of(),
                    "tip", "Follow the solution steps carefully to resolve the issue"
            );
            
            return ToolResult.success(response);
            
        } catch (Exception e) {
            log.error("Error analyzing error response", e);
            return ToolResult.error("Failed to analyze error: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"debugging", "troubleshooting", "support"};
    }
}
