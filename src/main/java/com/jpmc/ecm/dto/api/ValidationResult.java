package com.jpmc.ecm.dto.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of request validation.
 */
@Data
public class ValidationResult {
    
    /**
     * Whether the request is valid
     */
    private boolean valid = true;
    
    /**
     * List of validation errors
     */
    private List<ValidationError> errors = new ArrayList<>();
    
    /**
     * List of validation warnings
     */
    private List<ValidationWarning> warnings = new ArrayList<>();
    
    /**
     * Add a validation error
     */
    public void addError(String field, String message) {
        this.valid = false;
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .build());
    }
    
    /**
     * Add a validation error with details
     */
    public void addError(String field, String message, String details) {
        this.valid = false;
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .details(details)
                .build());
    }
    
    /**
     * Add a validation warning
     */
    public void addWarning(String field, String message) {
        this.warnings.add(ValidationWarning.builder()
                .field(field)
                .message(message)
                .build());
    }
    
    @Data
    @lombok.Builder
    public static class ValidationError {
        private String field;
        private String message;
        private String details;
    }
    
    @Data
    @lombok.Builder
    public static class ValidationWarning {
        private String field;
        private String message;
    }
}
