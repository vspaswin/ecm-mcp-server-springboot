package com.jpmc.ecm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;

/**
 * Configuration properties for ECM API integration.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "ecm.api")
public class EcmApiProperties {

    /**
     * Base URL of the ECM REST API
     */
    @NotBlank(message = "ECM API base URL is required")
    private String baseUrl;

    /**
     * Username for basic authentication
     */
    private String username;

    /**
     * Password for basic authentication
     */
    private String password;

    /**
     * API key for token-based authentication
     */
    private String apiKey;

    /**
     * Timeout configuration
     */
    private TimeoutConfig timeout = new TimeoutConfig();

    /**
     * Maximum number of retry attempts
     */
    private int maxRetries = 3;

    /**
     * Backoff duration between retries
     */
    private Duration retryBackoff = Duration.ofSeconds(1);

    @Data
    public static class TimeoutConfig {
        /**
         * Connection timeout
         */
        private Duration connect = Duration.ofSeconds(10);

        /**
         * Read timeout
         */
        private Duration read = Duration.ofSeconds(30);
    }
}
