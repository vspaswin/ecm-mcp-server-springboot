package com.jpmc.ecm.mcp;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.config.McpProtocolConfig;
import com.jpmc.ecm.mcp.model.McpRequest;
import com.jpmc.ecm.mcp.model.McpResponse;
import com.jpmc.ecm.mcp.model.ToolInfo;
import com.jpmc.ecm.mcp.tools.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP Protocol handler for processing MCP requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpProtocolHandler {

    private final McpProtocolConfig mcpConfig;
    private final EcmApiClient ecmApiClient;
    private final DocumentTools documentTools;
    private final SearchTools searchTools;
    private final FolderTools folderTools;
    private final MetadataTools metadataTools;
    private final VersionTools versionTools;
    private final WorkflowTools workflowTools;

    /**
     * Process an MCP request
     */
    public Mono<McpResponse> handleRequest(McpRequest request) {
        log.debug("Processing MCP request: method={}, id={}", 
            request.getMethod(), request.getId());

        return switch (request.getMethod()) {
            case "initialize" -> handleInitialize(request);
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolCall(request);
            case "health" -> handleHealth(request);
            default -> Mono.just(McpResponse.error(
                request.getId(), 
                -32601, 
                "Method not found: " + request.getMethod()
            ));
        };
    }

    /**
     * Handle initialize request
     */
    private Mono<McpResponse> handleInitialize(McpRequest request) {
        log.info("Initializing MCP server");

        Map<String, Object> result = Map.of(
            "protocolVersion", mcpConfig.getVersion(),
            "capabilities", Map.of(
                "tools", mcpConfig.getCapabilities().isTools(),
                "prompts", mcpConfig.getCapabilities().isPrompts(),
                "resources", mcpConfig.getCapabilities().isResources()
            ),
            "serverInfo", Map.of(
                "name", "ecm-mcp-server",
                "version", "1.0.0"
            )
        );

        return Mono.just(McpResponse.success(request.getId(), result));
    }

    /**
     * Handle tools list request
     */
    private Mono<McpResponse> handleToolsList(McpRequest request) {
        log.debug("Listing available tools");

        List<ToolInfo> tools = new ArrayList<>();
        tools.addAll(documentTools.getToolDefinitions());
        tools.addAll(searchTools.getToolDefinitions());
        tools.addAll(folderTools.getToolDefinitions());
        tools.addAll(metadataTools.getToolDefinitions());
        tools.addAll(versionTools.getToolDefinitions());
        tools.addAll(workflowTools.getToolDefinitions());

        Map<String, Object> result = Map.of("tools", tools);
        return Mono.just(McpResponse.success(request.getId(), result));
    }

    /**
     * Handle tool call request
     */
    @SuppressWarnings("unchecked")
    private Mono<McpResponse> handleToolCall(McpRequest request) {
        Map<String, Object> params = request.getParams();
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        log.info("Calling tool: {}", toolName);

        return executeTool(toolName, arguments)
            .map(result -> McpResponse.success(request.getId(), result))
            .onErrorResume(error -> {
                log.error("Tool execution failed: {}", toolName, error);
                return Mono.just(McpResponse.error(
                    request.getId(),
                    -32000,
                    "Tool execution failed: " + error.getMessage()
                ));
            });
    }

    /**
     * Handle health check request
     */
    private Mono<McpResponse> handleHealth(McpRequest request) {
        log.debug("Health check requested");

        return ecmApiClient.healthCheck()
            .map(healthStatus -> {
                Map<String, Object> result = Map.of(
                    "status", "healthy",
                    "ecmApi", healthStatus
                );
                return McpResponse.success(request.getId(), result);
            })
            .onErrorResume(error -> {
                Map<String, Object> result = Map.of(
                    "status", "unhealthy",
                    "error", error.getMessage()
                );
                return Mono.just(McpResponse.success(request.getId(), result));
            });
    }

    /**
     * Execute a tool based on its name
     */
    private Mono<Object> executeTool(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case String name when name.startsWith("ecm_get_document") || 
                                   name.startsWith("ecm_delete_document") ->
                documentTools.executeTool(toolName, arguments);
            
            case String name when name.startsWith("ecm_search") ->
                searchTools.executeTool(toolName, arguments);
            
            case String name when name.startsWith("ecm_create_folder") || 
                                   name.startsWith("ecm_list_folder") ->
                folderTools.executeTool(toolName, arguments);
            
            case String name when name.contains("_metadata") ->
                metadataTools.executeTool(toolName, arguments);
            
            case String name when name.contains("_version") ->
                versionTools.executeTool(toolName, arguments);
            
            case String name when name.contains("_workflow") ->
                workflowTools.executeTool(toolName, arguments);
            
            default -> Mono.error(new IllegalArgumentException(
                "Unknown tool: " + toolName));
        };
    }
}
