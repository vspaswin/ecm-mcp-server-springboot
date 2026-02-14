package com.jpmc.ecm.config;

import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.registry.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for MCP tool registry.
 * Automatically discovers and registers all ToolExecutor beans.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ToolRegistryConfig {
    
    /**
     * Register all ToolExecutor beans found in the application context.
     * This enables auto-discovery of tools - just create a @Component
     * that implements ToolExecutor and it will be automatically registered.
     */
    @Bean
    public ToolRegistryInitializer toolRegistryInitializer(
            ToolRegistry registry,
            List<ToolExecutor> toolExecutors) {
        
        log.info("Initializing tool registry with {} tools", toolExecutors.size());
        
        // Register all discovered tools
        registry.registerAll(toolExecutors);
        
        log.info("Tool registry initialized successfully");
        log.info("Available tools: {}", registry.getToolNames());
        
        return new ToolRegistryInitializer();
    }
    
    /**
     * Marker class to ensure initialization happens
     */
    public static class ToolRegistryInitializer {
        // Empty marker class
    }
}
