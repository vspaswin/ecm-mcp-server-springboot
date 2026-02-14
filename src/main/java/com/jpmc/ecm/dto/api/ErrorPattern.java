package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a common error pattern for an API endpoint.
 */
@Data
@Builder
public class ErrorPattern {
    
    /**
     * HTTP status code
     */
    private int code;
    
    /**
     * Error reason/message
     */
    private String reason;
    
    /**
     * Detailed explanation
     */
    private String explanation;
    
    /**
     * How to fix this error
     */
    private String solution;
    
    /**
     * Likely causes of this error
     */
    private List<String> likelyCauses;
    
    /**
     * Code example showing the fix
     */
    private String fixExample;
    
    /**
     * Related documentation links
     */
    private List<String> relatedDocs;
}
