package com.jpmc.ecm.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Authentication requirements for an API endpoint.
 */
@Data
@Builder
public class AuthenticationRequirement {
    
    /**
     * Authentication type (OAuth2, ApiKey, Basic, None)
     */
    private String type;
    
    /**
     * Required OAuth2 scopes
     */
    private List<String> scopes;
    
    /**
     * Header name for authentication (e.g., "Authorization")
     */
    private String headerName;
    
    /**
     * Format of the authentication value (e.g., "Bearer {token}")
     */
    private String format;
    
    /**
     * Whether authentication is mandatory
     */
    private boolean mandatory;
    
    /**
     * Instructions for obtaining credentials
     */
    private String instructions;
}
