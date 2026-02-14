package com.jpmc.ecm.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpmc.ecm.dto.api.CodeSample;
import com.jpmc.ecm.mcp.core.JsonSchemaBuilder;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.service.SampleGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tool for generating code samples in various languages.
 * Helps developers quickly get working code to call APIs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateSampleToolExecutor implements ToolExecutor {
    
    private final SampleGenerationService sampleGenerationService;
    
    @Override
    public String getToolName() {
        return "generate_sample_request";
    }
    
    @Override
    public String getDescription() {
        return "Generate a working code sample for calling an API endpoint. " +
               "Supports multiple programming languages including Java, Python, JavaScript, and curl. " +
               "Includes authentication, proper headers, and example payloads.";
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
                .property("language", JsonSchemaBuilder.string()
                        .description("Programming language for the sample")
                        .enumValues("java", "python", "javascript", "curl", "typescript")
                        .defaultValue("java")
                        .required(false))
                .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            String endpoint = (String) arguments.get("endpoint");
            String methodStr = (String) arguments.get("method");
            String language = (String) arguments.getOrDefault("language", "java");
            
            if (endpoint == null || methodStr == null) {
                return ToolResult.error("Both 'endpoint' and 'method' are required");
            }
            
            log.info("Generating {} sample for: {} {}", language, methodStr, endpoint);
            
            HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
            
            // Generate code sample
            CodeSample sample = sampleGenerationService.generateSample(
                    endpoint, method, language.toLowerCase());
            
            // Build response
            Map<String, Object> response = Map.of(
                    "language", sample.getLanguage(),
                    "code", sample.getCode(),
                    "description", sample.getDescription() != null ? 
                            sample.getDescription() : "Code sample for " + methodStr + " " + endpoint,
                    "dependencies", sample.getDependencies() != null ? 
                            sample.getDependencies() : "No additional dependencies",
                    "runInstructions", sample.getRunInstructions() != null ? 
                            sample.getRunInstructions() : "Run the code in your development environment",
                    "tip", "Replace placeholder values (token, file paths) with your actual values"
            );
            
            return ToolResult.success(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid arguments: {}", e.getMessage());
            return ToolResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error generating sample code", e);
            return ToolResult.error("Failed to generate sample: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"code-generation", "examples", "documentation"};
    }
}
