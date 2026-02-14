package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a code sample in a specific programming language.
 */
@Data
@Builder
public class CodeSample {
    
    /**
     * Programming language (java, python, curl, javascript, etc.)
     */
    private String language;
    
    /**
     * The actual code
     */
    private String code;
    
    /**
     * Description of what this code does
     */
    private String description;
    
    /**
     * Dependencies required to run this code
     */
    private String dependencies;
    
    /**
     * How to run this code
     */
    private String runInstructions;
    
    /**
     * Create a simple code sample
     */
    public static CodeSample of(String language, String code) {
        return CodeSample.builder()
                .language(language)
                .code(code)
                .build();
    }
}
