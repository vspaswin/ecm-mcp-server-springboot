# GitHub Copilot Guide: Building Extensible MCP Servers

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Core Components](#core-components)
3. [Adding New MCP Tools](#adding-new-mcp-tools)
4. [Adding New ECM Integrations](#adding-new-ecm-integrations)
5. [Configuration Management](#configuration-management)
6. [Testing Strategy](#testing-strategy)
7. [Best Practices](#best-practices)
8. [Examples](#examples)

---

## Architecture Overview

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    MCP Client (Claude/VSCode)               │
└────────────────────────┬────────────────────────────────────┘
                         │ MCP Protocol (JSON-RPC)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot MCP Server Application             │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │   MCP       │  │              │  │                 │   │
│  │ Controllers │──│   Services   │──│  ECM Clients    │   │
│  │             │  │              │  │                 │   │
│  └─────────────┘  └──────────────┘  └─────────────────┘   │
│         │                 │                    │            │
│         ▼                 ▼                    ▼            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Configuration & Properties                │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ REST APIs
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              External ECM Systems (FileNet, etc.)           │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.jpmc.ecm/
├── EcmMcpServerApplication.java      # Spring Boot main application
├── mcp/                               # MCP Protocol layer
│   ├── model/                         # MCP request/response models
│   │   ├── McpRequest.java
│   │   ├── McpResponse.java
│   │   ├── McpTool.java
│   │   └── McpToolParameter.java
│   ├── service/                       # MCP business logic
│   │   ├── McpToolRegistry.java       # Tool registration
│   │   └── McpToolExecutor.java       # Tool execution
│   └── handler/                       # Protocol handlers
│       └── McpProtocolHandler.java
├── controller/                        # REST controllers
│   └── McpController.java             # Main MCP endpoint
├── service/                           # Business services
│   ├── DocumentService.java           # Document operations
│   ├── SearchService.java             # Search operations
│   ├── WorkflowService.java           # Workflow operations
│   └── SecurityService.java           # Security operations
├── client/                            # External API clients
│   ├── EcmClient.java                 # Base ECM client
│   ├── FileNetClient.java             # FileNet implementation
│   └── SharePointClient.java          # SharePoint implementation
├── config/                            # Configuration classes
│   ├── WebClientConfig.java           # WebClient setup
│   ├── SecurityConfig.java            # Security configuration
│   └── EcmProperties.java             # ECM properties
├── dto/                               # Data Transfer Objects
│   ├── DocumentDto.java
│   ├── SearchRequestDto.java
│   └── WorkflowDto.java
└── exception/                         # Exception handling
    ├── EcmException.java
    ├── McpException.java
    └── GlobalExceptionHandler.java
```

---

## Core Components

### 1. MCP Protocol Models

#### McpRequest.java
```java
package com.jpmc.ecm.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class McpRequest {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    private String id;
    private String method;
    private Map<String, Object> params;
}
```

#### McpResponse.java
```java
package com.jpmc.ecm.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    private String id;
    private Object result;
    private McpError error;
    
    @Data
    public static class McpError {
        private int code;
        private String message;
        private Object data;
    }
}
```

#### McpTool.java
```java
package com.jpmc.ecm.mcp.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class McpTool {
    private String name;
    private String description;
    private Map<String, McpToolParameter> inputSchema;
}
```

### 2. MCP Tool Registry

```java
package com.jpmc.ecm.mcp.service;

import com.jpmc.ecm.mcp.model.McpTool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class McpToolRegistry {
    
    private final Map<String, McpTool> tools = new HashMap<>();
    private final Map<String, Function<Map<String, Object>, Object>> executors = new HashMap<>();
    
    /**
     * Register a new MCP tool
     * @param tool Tool metadata
     * @param executor Function to execute the tool
     */
    public void registerTool(McpTool tool, Function<Map<String, Object>, Object> executor) {
        tools.put(tool.getName(), tool);
        executors.put(tool.getName(), executor);
    }
    
    /**
     * Get all registered tools
     */
    public List<McpTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * Execute a tool by name
     */
    public Object executeTool(String toolName, Map<String, Object> params) {
        Function<Map<String, Object>, Object> executor = executors.get(toolName);
        if (executor == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        return executor.apply(params);
    }
    
    /**
     * Check if tool exists
     */
    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
}
```

### 3. Base ECM Client

```java
package com.jpmc.ecm.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface EcmClient {
    
    /**
     * Get document by ID
     */
    Mono<Map<String, Object>> getDocument(String documentId);
    
    /**
     * Search documents
     */
    Mono<Map<String, Object>> searchDocuments(Map<String, Object> criteria);
    
    /**
     * Create document
     */
    Mono<Map<String, Object>> createDocument(Map<String, Object> document);
    
    /**
     * Update document
     */
    Mono<Map<String, Object>> updateDocument(String documentId, Map<String, Object> updates);
    
    /**
     * Delete document
     */
    Mono<Void> deleteDocument(String documentId);
    
    /**
     * Get base URL for this ECM system
     */
    String getBaseUrl();
    
    /**
     * Get client name
     */
    String getClientName();
}
```

---

## Adding New MCP Tools

### Step 1: Define the Tool

Create a service that implements your tool logic:

```java
package com.jpmc.ecm.service;

import com.jpmc.ecm.client.EcmClient;
import com.jpmc.ecm.mcp.model.McpTool;
import com.jpmc.ecm.mcp.model.McpToolParameter;
import com.jpmc.ecm.mcp.service.McpToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final McpToolRegistry toolRegistry;
    private final EcmClient ecmClient;
    
    @PostConstruct
    public void registerTools() {
        registerGetDocumentTool();
        registerSearchDocumentsTool();
        registerCreateDocumentTool();
    }
    
    private void registerGetDocumentTool() {
        // Define tool metadata
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("documentId", McpToolParameter.builder()
            .type("string")
            .description("The unique identifier of the document")
            .required(true)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("ecm_get_document")
            .description("Retrieve a document from ECM system by its ID")
            .inputSchema(schema)
            .build();
        
        // Register tool with executor
        toolRegistry.registerTool(tool, params -> {
            String documentId = (String) params.get("documentId");
            log.info("Getting document: {}", documentId);
            return ecmClient.getDocument(documentId).block();
        });
    }
    
    private void registerSearchDocumentsTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("query", McpToolParameter.builder()
            .type("string")
            .description("Search query string")
            .required(true)
            .build());
        schema.put("maxResults", McpToolParameter.builder()
            .type("number")
            .description("Maximum number of results to return")
            .required(false)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("ecm_search_documents")
            .description("Search for documents in ECM system")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Searching documents with params: {}", params);
            return ecmClient.searchDocuments(params).block();
        });
    }
    
    private void registerCreateDocumentTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("title", McpToolParameter.builder()
            .type("string")
            .description("Document title")
            .required(true)
            .build());
        schema.put("content", McpToolParameter.builder()
            .type("string")
            .description("Document content")
            .required(true)
            .build());
        schema.put("metadata", McpToolParameter.builder()
            .type("object")
            .description("Additional metadata")
            .required(false)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("ecm_create_document")
            .description("Create a new document in ECM system")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Creating document with params: {}", params);
            return ecmClient.createDocument(params).block();
        });
    }
}
```

### Step 2: Tool Registration Pattern

**Key Points:**
1. Use `@PostConstruct` to register tools after bean initialization
2. Define clear input schemas with types and descriptions
3. Mark required vs optional parameters
4. Implement executor as lambda or method reference
5. Use descriptive tool names (prefix with domain, e.g., `ecm_`, `workflow_`)

### Step 3: Test Your Tool

```java
package com.jpmc.ecm.service;

import com.jpmc.ecm.mcp.service.McpToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocumentServiceTest {
    
    @Autowired
    private McpToolRegistry toolRegistry;
    
    @Test
    void testGetDocumentToolRegistered() {
        assertThat(toolRegistry.hasTool("ecm_get_document")).isTrue();
    }
    
    @Test
    void testSearchDocumentsToolRegistered() {
        assertThat(toolRegistry.hasTool("ecm_search_documents")).isTrue();
    }
    
    @Test
    void testExecuteGetDocument() {
        Map<String, Object> params = new HashMap<>();
        params.put("documentId", "DOC-123");
        
        Object result = toolRegistry.executeTool("ecm_get_document", params);
        assertThat(result).isNotNull();
    }
}
```

---

## Adding New ECM Integrations

### Step 1: Implement EcmClient Interface

```java
package com.jpmc.ecm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileNetClient implements EcmClient {
    
    private final WebClient webClient;
    private final EcmProperties ecmProperties;
    
    @Override
    public Mono<Map<String, Object>> getDocument(String documentId) {
        return webClient.get()
            .uri(getBaseUrl() + "/documents/{id}", documentId)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnSuccess(doc -> log.info("Retrieved document: {}", documentId))
            .doOnError(error -> log.error("Error retrieving document: {}", error.getMessage()));
    }
    
    @Override
    public Mono<Map<String, Object>> searchDocuments(Map<String, Object> criteria) {
        return webClient.post()
            .uri(getBaseUrl() + "/search")
            .bodyValue(criteria)
            .retrieve()
            .bodyToMono(Map.class);
    }
    
    @Override
    public Mono<Map<String, Object>> createDocument(Map<String, Object> document) {
        return webClient.post()
            .uri(getBaseUrl() + "/documents")
            .bodyValue(document)
            .retrieve()
            .bodyToMono(Map.class);
    }
    
    @Override
    public Mono<Map<String, Object>> updateDocument(String documentId, Map<String, Object> updates) {
        return webClient.put()
            .uri(getBaseUrl() + "/documents/{id}", documentId)
            .bodyValue(updates)
            .retrieve()
            .bodyToMono(Map.class);
    }
    
    @Override
    public Mono<Void> deleteDocument(String documentId) {
        return webClient.delete()
            .uri(getBaseUrl() + "/documents/{id}", documentId)
            .retrieve()
            .bodyToMono(Void.class);
    }
    
    @Override
    public String getBaseUrl() {
        return ecmProperties.getFilenet().getBaseUrl();
    }
    
    @Override
    public String getClientName() {
        return "FileNet";
    }
}
```

### Step 2: Add Configuration Properties

```java
package com.jpmc.ecm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ecm")
public class EcmProperties {
    
    private FileNetConfig filenet = new FileNetConfig();
    private SharePointConfig sharepoint = new SharePointConfig();
    
    @Data
    public static class FileNetConfig {
        private String baseUrl;
        private String username;
        private String password;
        private int timeout = 30000;
    }
    
    @Data
    public static class SharePointConfig {
        private String baseUrl;
        private String clientId;
        private String clientSecret;
        private int timeout = 30000;
    }
}
```

### Step 3: Update application.yml

```yaml
ecm:
  filenet:
    base-url: ${FILENET_BASE_URL:http://localhost:8080/filenet}
    username: ${FILENET_USERNAME:admin}
    password: ${FILENET_PASSWORD:password}
    timeout: 30000
  sharepoint:
    base-url: ${SHAREPOINT_BASE_URL:http://localhost:8080/sharepoint}
    client-id: ${SHAREPOINT_CLIENT_ID:client-id}
    client-secret: ${SHAREPOINT_CLIENT_SECRET:secret}
    timeout: 30000
```

---

## Configuration Management

### WebClient Configuration

```java
package com.jpmc.ecm.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final EcmProperties ecmProperties;
    
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .responseTimeout(Duration.ofMillis(30000))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)));
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
```

### Resilience Configuration

```java
package com.jpmc.ecm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            .build();
    }
    
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .build();
    }
}
```

---

## Testing Strategy

### Unit Tests

```java
package com.jpmc.ecm.service;

import com.jpmc.ecm.client.EcmClient;
import com.jpmc.ecm.mcp.service.McpToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DocumentServiceTest {
    
    @Mock
    private EcmClient ecmClient;
    
    @Mock
    private McpToolRegistry toolRegistry;
    
    private DocumentService documentService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentService = new DocumentService(toolRegistry, ecmClient);
    }
    
    @Test
    void testGetDocument() {
        Map<String, Object> expectedDoc = new HashMap<>();
        expectedDoc.put("id", "DOC-123");
        expectedDoc.put("title", "Test Document");
        
        when(ecmClient.getDocument(anyString()))
            .thenReturn(Mono.just(expectedDoc));
        
        Map<String, Object> result = ecmClient.getDocument("DOC-123").block();
        
        assertThat(result).isNotNull();
        assertThat(result.get("id")).isEqualTo("DOC-123");
    }
}
```

### Integration Tests

```java
package com.jpmc.ecm.controller;

import com.jpmc.ecm.mcp.model.McpRequest;
import com.jpmc.ecm.mcp.model.McpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testListTools() {
        McpRequest request = new McpRequest();
        request.setMethod("tools/list");
        request.setId("test-1");
        
        ResponseEntity<McpResponse> response = restTemplate
            .postForEntity("/mcp", request, McpResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResult()).isNotNull();
    }
    
    @Test
    void testCallTool() {
        Map<String, Object> params = new HashMap<>();
        params.put("documentId", "DOC-123");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "ecm_get_document");
        arguments.put("arguments", params);
        
        McpRequest request = new McpRequest();
        request.setMethod("tools/call");
        request.setId("test-2");
        request.setParams(arguments);
        
        ResponseEntity<McpResponse> response = restTemplate
            .postForEntity("/mcp", request, McpResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

---

## Best Practices

### 1. Tool Naming Convention

- Use domain prefix: `ecm_`, `workflow_`, `security_`
- Use snake_case: `ecm_get_document`, not `ecmGetDocument`
- Be descriptive: `ecm_search_documents_by_metadata` vs `ecm_search`
- Keep consistent: All ECM tools start with `ecm_`

### 2. Error Handling

```java
public Object executeTool(String toolName, Map<String, Object> params) {
    try {
        return ecmClient.getDocument((String) params.get("documentId"))
            .doOnError(error -> log.error("Error getting document: {}", error.getMessage()))
            .onErrorMap(error -> new McpException("Failed to retrieve document", error))
            .block();
    } catch (Exception e) {
        throw new McpException("Tool execution failed: " + toolName, e);
    }
}
```

### 3. Logging

```java
@Slf4j
public class DocumentService {
    
    public void registerGetDocumentTool() {
        log.info("Registering tool: ecm_get_document");
        
        toolRegistry.registerTool(tool, params -> {
            String documentId = (String) params.get("documentId");
            log.debug("Executing ecm_get_document with documentId: {}", documentId);
            
            return ecmClient.getDocument(documentId)
                .doOnSuccess(doc -> log.info("Successfully retrieved document: {}", documentId))
                .doOnError(error -> log.error("Failed to retrieve document: {}", documentId, error))
                .block();
        });
    }
}
```

### 4. Configuration Management

- Use environment variables for sensitive data
- Provide sensible defaults
- Document all configuration options
- Use Spring Boot configuration properties

### 5. Async Operations

```java
public Mono<Object> executeToolAsync(String toolName, Map<String, Object> params) {
    return Mono.fromCallable(() -> executeTool(toolName, params))
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(30))
        .retry(3);
}
```

---

## Examples

### Example 1: Adding a Workflow Tool

```java
package com.jpmc.ecm.service;

import com.jpmc.ecm.mcp.model.McpTool;
import com.jpmc.ecm.mcp.model.McpToolParameter;
import com.jpmc.ecm.mcp.service.McpToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    
    private final McpToolRegistry toolRegistry;
    
    @PostConstruct
    public void registerTools() {
        registerStartWorkflowTool();
        registerGetWorkflowStatusTool();
    }
    
    private void registerStartWorkflowTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("workflowType", McpToolParameter.builder()
            .type("string")
            .description("Type of workflow to start (e.g., 'document_approval', 'review')")
            .required(true)
            .build());
        schema.put("documentId", McpToolParameter.builder()
            .type("string")
            .description("ID of document to process")
            .required(true)
            .build());
        schema.put("assignees", McpToolParameter.builder()
            .type("array")
            .description("List of user IDs to assign tasks to")
            .required(false)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("workflow_start")
            .description("Start a new workflow instance for a document")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Starting workflow: {}", params);
            // Implementation here
            return Map.of(
                "workflowId", "WF-" + System.currentTimeMillis(),
                "status", "STARTED",
                "documentId", params.get("documentId")
            );
        });
    }
    
    private void registerGetWorkflowStatusTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("workflowId", McpToolParameter.builder()
            .type("string")
            .description("Workflow instance ID")
            .required(true)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("workflow_get_status")
            .description("Get current status of a workflow instance")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Getting workflow status: {}", params);
            return Map.of(
                "workflowId", params.get("workflowId"),
                "status", "IN_PROGRESS",
                "currentStep", "approval",
                "assignee", "user123"
            );
        });
    }
}
```

### Example 2: Adding Security/Permission Tool

```java
package com.jpmc.ecm.service;

import com.jpmc.ecm.mcp.model.McpTool;
import com.jpmc.ecm.mcp.model.McpToolParameter;
import com.jpmc.ecm.mcp.service.McpToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final McpToolRegistry toolRegistry;
    
    @PostConstruct
    public void registerTools() {
        registerCheckPermissionTool();
        registerGrantPermissionTool();
    }
    
    private void registerCheckPermissionTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("documentId", McpToolParameter.builder()
            .type("string")
            .description("Document ID to check permissions for")
            .required(true)
            .build());
        schema.put("userId", McpToolParameter.builder()
            .type("string")
            .description("User ID to check permissions for")
            .required(true)
            .build());
        schema.put("permission", McpToolParameter.builder()
            .type("string")
            .description("Permission to check (e.g., 'read', 'write', 'delete')")
            .required(true)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("security_check_permission")
            .description("Check if user has specific permission on a document")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Checking permission: {}", params);
            return Map.of(
                "hasPermission", true,
                "permission", params.get("permission"),
                "userId", params.get("userId"),
                "documentId", params.get("documentId")
            );
        });
    }
    
    private void registerGrantPermissionTool() {
        Map<String, McpToolParameter> schema = new HashMap<>();
        schema.put("documentId", McpToolParameter.builder()
            .type("string")
            .description("Document ID to grant permissions on")
            .required(true)
            .build());
        schema.put("userId", McpToolParameter.builder()
            .type("string")
            .description("User ID to grant permissions to")
            .required(true)
            .build());
        schema.put("permissions", McpToolParameter.builder()
            .type("array")
            .description("List of permissions to grant (e.g., ['read', 'write'])")
            .required(true)
            .build());
        
        McpTool tool = McpTool.builder()
            .name("security_grant_permission")
            .description("Grant permissions to a user on a document")
            .inputSchema(schema)
            .build();
        
        toolRegistry.registerTool(tool, params -> {
            log.info("Granting permissions: {}", params);
            return Map.of(
                "success", true,
                "userId", params.get("userId"),
                "documentId", params.get("documentId"),
                "grantedPermissions", params.get("permissions")
            );
        });
    }
}
```

### Example 3: Testing with Claude Desktop

**claude_desktop_config.json** (macOS):

```json
{
  "mcpServers": {
    "ecm-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/ecm-mcp-server-springboot/target/ecm-mcp-server-1.0.0.jar"
      ],
      "env": {
        "FILENET_BASE_URL": "http://your-filenet-server:9080/fncmis",
        "FILENET_USERNAME": "admin",
        "FILENET_PASSWORD": "password"
      }
    }
  }
}
```

**Testing in Claude:**

1. List available tools:
   ```
   Can you list all available ECM tools?
   ```

2. Get a document:
   ```
   Can you get document with ID DOC-12345 from the ECM system?
   ```

3. Search documents:
   ```
   Search for all documents with title containing "invoice" from the last 30 days
   ```

4. Start a workflow:
   ```
   Start a document approval workflow for document DOC-12345 and assign it to user john.doe
   ```

---

## Quick Start Checklist

When building a new MCP server based on this template:

- [ ] Clone the repository
- [ ] Update `pom.xml` with your project details
- [ ] Configure `application.yml` with your ECM endpoints
- [ ] Implement your `EcmClient` implementation
- [ ] Create service classes with `@PostConstruct` for tool registration
- [ ] Define clear tool schemas with proper types and descriptions
- [ ] Write unit tests for each tool
- [ ] Write integration tests for the MCP controller
- [ ] Build the project: `mvn clean install`
- [ ] Test with Claude Desktop or your MCP client
- [ ] Document your tools in README.md

---

## Additional Resources

- [MCP Specification](https://modelcontextprotocol.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring WebFlux Guide](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

## Support

For issues or questions:
1. Check existing GitHub issues
2. Review this guide thoroughly
3. Check Spring Boot logs for detailed error messages
4. Create a new issue with:
   - Error message
   - Steps to reproduce
   - Environment details (Java version, OS, etc.)
