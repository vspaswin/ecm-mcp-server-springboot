package com.jpmc.ecm.mcp.registry;

import com.jpmc.ecm.mcp.core.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all MCP tools.
 * Provides dynamic tool discovery and registration.
 */
@Slf4j
@Component
public class ToolRegistry {
    
    private final Map<String, ToolExecutor> tools = new ConcurrentHashMap<>();
    
    /**
     * Register a tool
     */
    public void register(ToolExecutor tool) {
        String toolName = tool.getToolName();
        if (tools.containsKey(toolName)) {
            log.warn("Tool '{}' is already registered, overwriting", toolName);
        }
        tools.put(toolName, tool);
        log.info("Registered MCP tool: {} - {}", toolName, tool.getDescription());
    }
    
    /**
     * Register multiple tools
     */
    public void registerAll(Collection<ToolExecutor> executors) {
        executors.forEach(this::register);
    }
    
    /**
     * Get a tool by name
     */
    public Optional<ToolExecutor> getTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }
    
    /**
     * Get all registered tools
     */
    public Collection<ToolExecutor> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }
    
    /**
     * Get tool names
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }
    
    /**
     * Check if a tool exists
     */
    public boolean hasTo ol(String toolName) {
        return tools.containsKey(toolName);
    }
    
    /**
     * Get tools by tag
     */
    public List<ToolExecutor> getToolsByTag(String tag) {
        return tools.values().stream()
                .filter(tool -> Arrays.asList(tool.getTags()).contains(tag))
                .toList();
    }
    
    /**
     * Get count of registered tools
     */
    public int getToolCount() {
        return tools.size();
    }
}
