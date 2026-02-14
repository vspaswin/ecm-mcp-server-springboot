package com.jpmc.ecm.controller;

import com.jpmc.ecm.mcp.McpProtocolHandler;
import com.jpmc.ecm.mcp.model.McpRequest;
import com.jpmc.ecm.mcp.model.McpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for MCP protocol communication.
 * 
 * This controller provides HTTP endpoints for MCP clients that don't use stdio transport.
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpProtocolHandler mcpProtocolHandler;

    /**
     * Handle MCP requests via HTTP POST
     */
    @PostMapping(value = "/message", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<McpResponse> handleMessage(@RequestBody McpRequest request) {
        log.debug("Received MCP request via HTTP: method={}", request.getMethod());
        return mcpProtocolHandler.handleRequest(request);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("MCP Server is running");
    }
}
