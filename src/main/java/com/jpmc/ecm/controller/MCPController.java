package com.jpmc.ecm.controller;

import com.jpmc.ecm.dto.mcp.MCPRequest;
import com.jpmc.ecm.dto.mcp.MCPResponse;
import com.jpmc.ecm.dto.mcp.ToolInfo;
import com.jpmc.ecm.mcp.core.ToolExecutor;
import com.jpmc.ecm.mcp.core.ToolResult;
import com.jpmc.ecm.mcp.registry.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for MCP protocol endpoints.
 * Handles tool discovery and execution.
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class MCPController {
    
    private final ToolRegistry toolRegistry;
    
    /**
     * List all available MCP tools
     */
    @GetMapping("/tools")
    public ResponseEntity<List<ToolInfo>> listTools() {
        log.debug("Listing all MCP tools");
        
        List<ToolInfo> tools = toolRegistry.getAllTools().stream()
                .map(tool -> ToolInfo.builder()
                        .name(tool.getToolName())
                        .description(tool.getDescription())
                        .inputSchema(tool.getInputSchema())
                        .tags(tool.getTags())
                        .build())
                .toList();
        
        return ResponseEntity.ok(tools);
    }
    
    /**
     * Get information about a specific tool
     */
    @GetMapping("/tools/{toolName}")
    public ResponseEntity<ToolInfo> getTool(@PathVariable String toolName) {
        log.debug("Getting tool info: {}", toolName);
        
        return toolRegistry.getTool(toolName)
                .map(tool -> ToolInfo.builder()
                        .name(tool.getToolName())
                        .description(tool.getDescription())
                        .inputSchema(tool.getInputSchema())
                        .tags(tool.getTags())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Execute an MCP tool
     */
    @PostMapping("/execute")
    public ResponseEntity<MCPResponse> executeTool(@RequestBody MCPRequest request) {
        log.info("Executing MCP tool: {}", request.getParams().getName());
        
        try {
            String toolName = request.getParams().getName();
            Map<String, Object> arguments = request.getParams().getArguments();
            
            // Find the tool
            ToolExecutor tool = toolRegistry.getTool(toolName)
                    .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolName));
            
            // Execute the tool
            ToolResult result = tool.execute(arguments);
            
            // Build response
            if (result.isSuccess()) {
                return ResponseEntity.ok(
                        MCPResponse.success(request.getId(), result.getData())
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(MCPResponse.error(
                                request.getId(),
                                -32603,
                                result.getError(),
                                result.getErrorDetails()
                        ));
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Tool not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MCPResponse.error(
                            request.getId(),
                            -32601,
                            e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error executing tool", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MCPResponse.error(
                            request.getId(),
                            -32603,
                            "Internal error: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "toolCount", toolRegistry.getToolCount(),
                "tools", toolRegistry.getToolNames()
        ));
    }
}
