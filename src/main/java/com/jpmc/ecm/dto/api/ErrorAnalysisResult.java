package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Result of error analysis for an API call.
 */
@Data
@Builder
public class ErrorAnalysisResult {
    
    /**
     * HTTP status code of the error
     */
    private int errorCode;
    
    /**
     * Root cause diagnosis
     */
    private String diagnosis;
    
    /**
     * Likely causes of this error
     */
    private List<String> likelyCauses;
    
    /**
     * Step-by-step solution
     */
    private Solution solution;
    
    /**
     * Code example showing the fix
     */
    private String codeExample;
    
    /**
     * Related documentation
     */
    private List<String> relatedDocs;
    
    /**
     * Additional context about the error
     */
    private Map<String, Object> context;
    
    @Data
    @Builder
    public static class Solution {
        private List<String> steps;
        private String summary;
        private Map<String, String> codeExamples;
    }
}
