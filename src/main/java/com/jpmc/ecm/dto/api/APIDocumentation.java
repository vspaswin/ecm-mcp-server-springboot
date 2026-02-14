package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

/**
 * Complete documentation for an API endpoint.
 * This is loaded from the API knowledge base.
 */
@Data
@Builder
public class APIDocumentation {
    
    /**
     * API endpoint path (e.g., "/api/v1/documents")
     */
    private String path;
    
    /**
     * HTTP method
     */
    private HttpMethod method;
    
    /**
     * Human-readable description of the endpoint
     */
    private String description;
    
    /**
     * Base URL for the API
     */
    private String baseUrl;
    
    /**
     * List of mandatory parameters/fields
     */
    private List<Parameter> mandatoryFields;
    
    /**
     * List of optional parameters/fields
     */
    private List<Parameter> optionalFields;
    
    /**
     * Authentication requirements
     */
    private AuthenticationRequirement authentication;
    
    /**
     * Request body schema (for POST/PUT)
     */
    private Map<String, Object> requestSchema;
    
    /**
     * Response body schema
     */
    private Map<String, Object> responseSchema;
    
    /**
     * Expected response type class name
     */
    private String responseType;
    
    /**
     * Common errors for this endpoint
     */
    private List<ErrorPattern> commonErrors;
    
    /**
     * Code examples in different languages
     */
    private Map<String, String> codeExamples;
    
    /**
     * Tags for categorization
     */
    private List<String> tags;
}
