package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a parameter (field) in an API request.
 */
@Data
@Builder
public class Parameter {
    
    /**
     * Parameter name
     */
    private String name;
    
    /**
     * Data type (string, integer, boolean, object, array)
     */
    private String type;
    
    /**
     * Human-readable description
     */
    private String description;
    
    /**
     * Whether this parameter is required
     */
    private boolean required;
    
    /**
     * Example value
     */
    private String example;
    
    /**
     * Allowed values (for enums)
     */
    private List<String> allowedValues;
    
    /**
     * Minimum value (for numbers)
     */
    private Number minimum;
    
    /**
     * Maximum value (for numbers)
     */
    private Number maximum;
    
    /**
     * Pattern for validation (regex)
     */
    private String pattern;
    
    /**
     * Default value if not provided
     */
    private String defaultValue;
    
    /**
     * For object types, nested properties
     */
    private List<Parameter> properties;
}
