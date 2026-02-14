# ECM MCP Server Architecture

## Overview

This document describes the architecture of the Enterprise Content Management (ECM) Model Context Protocol (MCP) server built with Spring Boot 4. The server provides a standardized interface for AI agents to interact with enterprise content management systems.

## Design Principles

### 1. **Extensibility First**
- New MCP tools can be added without modifying core framework
- Support for multiple ECM backends through pluggable client architecture
- Configuration-driven approach for different environments

### 2. **Enterprise-Ready**
- Built on Spring Boot 4 with Java 21
- Reactive programming with WebFlux for high concurrency
- Resilience patterns (circuit breaker, retry, timeout)
- Production-grade observability (metrics, tracing, health checks)

### 3. **Protocol Compliance**
- Strict adherence to MCP specification
- JSON-RPC 2.0 message format
- Standardized tool schema definitions
- Error handling per MCP specification

### 4. **Security & Compliance**
- JWT-based authentication (planned)
- Role-based access control (planned)
- Audit logging for all operations
- Encryption for sensitive data in transit

## System Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         External Systems                                │
├─────────────────────────────────────────────────────────────────────────┤
│  • Claude Desktop      • VSCode Copilot     • Custom MCP Clients       │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ HTTP/HTTPS (JSON-RPC)
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         API Gateway Layer                               │
├─────────────────────────────────────────────────────────────────────────┤
│  • Spring Web Controller                                                │
│  • Request Validation                                                   │
│  • Response Formatting                                                  │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       MCP Protocol Layer                                │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐    │
│  │  Protocol        │  │  Tool            │  │  Tool             │    │
│  │  Handler         │  │  Registry        │  │  Executor         │    │
│  │                  │  │                  │  │                   │    │
│  │ • Parses MCP     │  │ • Manages tool   │  │ • Executes tool   │    │
│  │   requests       │  │   definitions    │  │   logic           │    │
│  │ • Routes to      │  │ • Returns tool   │  │ • Parameter       │    │
│  │   handlers       │  │   list           │  │   validation      │    │
│  │ • Formats        │  │ • Tool lookup    │  │ • Error handling  │    │
│  │   responses      │  │                  │  │                   │    │
│  └──────────────────┘  └──────────────────┘  └───────────────────┘    │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Business Service Layer                           │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │  Document    │  │  Search      │  │  Workflow    │  │  Security  │ │
│  │  Service     │  │  Service     │  │  Service     │  │  Service   │ │
│  │              │  │              │  │              │  │            │ │
│  │ • CRUD ops   │  │ • Query      │  │ • Start WF   │  │ • AuthZ    │ │
│  │ • Metadata   │  │ • Filter     │  │ • Status     │  │ • Perms    │ │
│  │ • Versions   │  │ • Facets     │  │ • Tasks      │  │ • Audit    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────────┘ │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         ECM Client Layer                                │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐    │
│  │  EcmClient       │  │  FileNetClient   │  │  SharePoint       │    │
│  │  Interface       │  │                  │  │  Client           │    │
│  │                  │  │ • REST calls     │  │                   │    │
│  │ • Common         │  │ • Auth           │  │ • Graph API       │    │
│  │   contract       │  │ • Error mapping  │  │ • OAuth2          │    │
│  │ • Reactive API   │  │ • Retry logic    │  │ • Rate limiting   │    │
│  └──────────────────┘  └──────────────────┘  └───────────────────┘    │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                                 │
├─────────────────────────────────────────────────────────────────────────┤
│  • WebClient (Reactive HTTP)       • Resilience4j (Circuit Breaker)    │
│  • Jackson (JSON Serialization)    • Micrometer (Metrics)              │
│  • Logback (Logging)               • Spring Actuator (Health Checks)   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. MCP Protocol Layer

#### McpController
**Responsibility**: HTTP endpoint for MCP protocol

```java
@RestController
@RequestMapping("/mcp")
public class McpController {
    // Handles JSON-RPC requests
    // Routes to appropriate handlers
    // Formats MCP responses
}
```

**Key Methods**:
- `handleMcpRequest(McpRequest)` - Main entry point
- Validates JSON-RPC format
- Routes based on `method` field
- Returns properly formatted `McpResponse`

#### McpProtocolHandler
**Responsibility**: MCP protocol operations

```java
@Component
public class McpProtocolHandler {
    // Implements MCP protocol methods:
    // - initialize
    // - tools/list
    // - tools/call
}
```

**Supported Methods**:
1. `initialize` - Handshake and capability negotiation
2. `tools/list` - Returns available tools
3. `tools/call` - Executes a specific tool

#### McpToolRegistry
**Responsibility**: Tool registration and discovery

```java
@Service
public class McpToolRegistry {
    private Map<String, McpTool> tools;
    private Map<String, Function<Map, Object>> executors;
    
    // Register new tools
    public void registerTool(McpTool tool, Function executor);
    
    // List all tools
    public List<McpTool> getAllTools();
    
    // Execute a tool
    public Object executeTool(String name, Map params);
}
```

**Registration Flow**:
```
Service @PostConstruct
    ↓
Create McpTool with schema
    ↓
Define executor function
    ↓
Register with McpToolRegistry
    ↓
Tool available to MCP clients
```

### 2. Business Service Layer

#### Service Pattern
Each service:
1. Registers tools in `@PostConstruct`
2. Implements business logic
3. Delegates to ECM clients
4. Handles errors gracefully

#### Example: DocumentService

```java
@Service
public class DocumentService {
    private final McpToolRegistry registry;
    private final EcmClient client;
    
    @PostConstruct
    public void registerTools() {
        // Tool 1: Get document
        registry.registerTool(
            createGetDocumentTool(),
            params -> getDocument(params)
        );
        
        // Tool 2: Search documents
        registry.registerTool(
            createSearchTool(),
            params -> searchDocuments(params)
        );
        
        // Tool 3: Create document
        registry.registerTool(
            createDocumentTool(),
            params -> createDocument(params)
        );
    }
    
    private Object getDocument(Map<String, Object> params) {
        String id = (String) params.get("documentId");
        return client.getDocument(id).block();
    }
}
```

### 3. ECM Client Layer

#### EcmClient Interface
Defines contract for all ECM integrations:

```java
public interface EcmClient {
    // Document operations
    Mono<Map<String, Object>> getDocument(String id);
    Mono<Map<String, Object>> createDocument(Map doc);
    Mono<Map<String, Object>> updateDocument(String id, Map updates);
    Mono<Void> deleteDocument(String id);
    
    // Search operations
    Mono<Map<String, Object>> searchDocuments(Map criteria);
    
    // Metadata
    String getBaseUrl();
    String getClientName();
}
```

#### Implementation Strategy

**FileNet Client Example**:
```java
@Component
public class FileNetClient implements EcmClient {
    private final WebClient webClient;
    private final EcmProperties properties;
    
    @Override
    public Mono<Map<String, Object>> getDocument(String id) {
        return webClient.get()
            .uri(baseUrl + "/documents/{id}", id)
            .retrieve()
            .bodyToMono(Map.class)
            .retryWhen(retrySpec())
            .timeout(Duration.ofSeconds(30));
    }
}
```

**Key Features**:
- Reactive WebClient for non-blocking I/O
- Automatic retry with exponential backoff
- Circuit breaker for fault tolerance
- Timeout protection
- Detailed error mapping

## Data Flow

### Tool Execution Flow

```
1. MCP Client (Claude)
   |
   | POST /mcp
   | {
   |   "method": "tools/call",
   |   "params": {
   |     "name": "ecm_get_document",
   |     "arguments": {"documentId": "DOC-123"}
   |   }
   | }
   ↓
2. McpController
   |
   | Validates JSON-RPC format
   | Parses request
   ↓
3. McpProtocolHandler
   |
   | Routes to tools/call handler
   | Extracts tool name and arguments
   ↓
4. McpToolRegistry
   |
   | Looks up tool executor
   | Validates parameters against schema
   ↓
5. DocumentService (executor)
   |
   | Processes request
   | Calls ECM client
   ↓
6. FileNetClient
   |
   | Makes HTTP call to FileNet
   | Handles response/errors
   ↓
7. Response flows back up
   |
   | Wrapped in McpResponse
   | Returned to client
   ↓
8. MCP Client receives result
```

### Error Handling Flow

```
Exception thrown at any layer
   ↓
Caught by appropriate handler
   ↓
Mapped to McpError
   ↓
Logged with context
   ↓
Returned in McpResponse.error
   ↓
Client receives structured error
```

## Extensibility Patterns

### Pattern 1: Adding New Tools

**Steps**:
1. Create or use existing service class
2. Define tool schema
3. Implement executor logic
4. Register in `@PostConstruct`

**Example - Adding a "Get Metadata" tool**:

```java
@Service
public class MetadataService {
    private final McpToolRegistry registry;
    private final EcmClient client;
    
    @PostConstruct
    public void registerTools() {
        // Define schema
        Map<String, McpToolParameter> schema = Map.of(
            "documentId", McpToolParameter.builder()
                .type("string")
                .description("Document ID")
                .required(true)
                .build()
        );
        
        // Create tool
        McpTool tool = McpTool.builder()
            .name("ecm_get_metadata")
            .description("Retrieve document metadata")
            .inputSchema(schema)
            .build();
        
        // Register with executor
        registry.registerTool(tool, params -> {
            String id = (String) params.get("documentId");
            return client.getDocument(id)
                .map(doc -> doc.get("metadata"))
                .block();
        });
    }
}
```

### Pattern 2: Adding New ECM Backend

**Steps**:
1. Implement `EcmClient` interface
2. Add configuration properties
3. Configure WebClient bean
4. Add authentication logic
5. Map ECM-specific errors

**Example - Adding Documentum**:

```java
@Component
@ConditionalOnProperty("ecm.documentum.enabled")
public class DocumentumClient implements EcmClient {
    private final WebClient webClient;
    private final DocumentumProperties properties;
    
    @Override
    public Mono<Map<String, Object>> getDocument(String id) {
        return webClient.get()
            .uri(properties.getBaseUrl() + "/dql", uriBuilder -> 
                uriBuilder.queryParam("dql", "SELECT * FROM dm_document WHERE r_object_id = '" + id + "'")
                          .build())
            .headers(h -> h.setBearerAuth(getToken()))
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::mapDocumentumResponse);
    }
    
    private String getToken() {
        // Implement Documentum authentication
        return "...";
    }
    
    private Map<String, Object> mapDocumentumResponse(Map<String, Object> raw) {
        // Transform Documentum-specific format to standard format
        return ...;
    }
}
```

### Pattern 3: Adding Cross-Cutting Concerns

#### Aspect-Oriented Programming for Logging

```java
@Aspect
@Component
public class McpLoggingAspect {
    
    @Around("@annotation(com.jpmc.ecm.mcp.annotation.McpTool)")
    public Object logToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("Executing tool: {} with args: {}", toolName, args);
        long start = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Tool {} completed in {}ms", toolName, duration);
            return result;
        } catch (Exception e) {
            log.error("Tool {} failed: {}", toolName, e.getMessage());
            throw e;
        }
    }
}
```

## Configuration Strategy

### Layered Configuration

```
application.yml (defaults)
   ↓
application-{profile}.yml (environment-specific)
   ↓
Environment variables (runtime overrides)
   ↓
JVM properties (system overrides)
```

### Configuration Structure

```yaml
# Core application settings
server:
  port: 8080
  
spring:
  application:
    name: ecm-mcp-server

# MCP-specific configuration
mcp:
  protocol:
    version: "2024-11-05"
  server:
    name: "ECM MCP Server"
    version: "1.0.0"

# ECM backends
ecm:
  # FileNet configuration
  filenet:
    enabled: true
    base-url: ${FILENET_BASE_URL:http://localhost:9080/fncmis}
    username: ${FILENET_USERNAME:admin}
    password: ${FILENET_PASSWORD:password}
    timeout: 30000
    
  # SharePoint configuration
  sharepoint:
    enabled: false
    base-url: ${SHAREPOINT_BASE_URL}
    client-id: ${SHAREPOINT_CLIENT_ID}
    client-secret: ${SHAREPOINT_CLIENT_SECRET}
    timeout: 30000

# Resilience configuration
resilience4j:
  circuitbreaker:
    instances:
      ecmClient:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        
  retry:
    instances:
      ecmClient:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2

# Observability
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## Security Architecture

### Authentication Flow (Future)

```
1. Client authenticates with Identity Provider
   ↓
2. Receives JWT token
   ↓
3. Includes token in MCP requests
   ↓
4. Server validates token
   ↓
5. Extracts user identity
   ↓
6. Checks permissions
   ↓
7. Allows/denies operation
```

### Authorization Model (Future)

```java
@Component
public class SecurityService {
    
    public boolean hasPermission(String userId, String resource, String action) {
        // Check role-based permissions
        // Check resource-level ACLs
        // Audit the check
        return ...
    }
    
    public void auditAccess(String userId, String resource, String action, boolean granted) {
        // Log to audit system
    }
}
```

## Observability

### Metrics

**Collected Metrics**:
- Tool execution count (counter)
- Tool execution duration (histogram)
- Error rate by tool (counter)
- ECM client call duration (histogram)
- Circuit breaker state (gauge)

**Example Custom Metrics**:
```java
@Component
public class MetricsService {
    private final Counter toolExecutionCounter;
    private final Timer toolExecutionTimer;
    
    public MetricsService(MeterRegistry registry) {
        this.toolExecutionCounter = Counter.builder("mcp.tool.executions")
            .tag("type", "tool")
            .register(registry);
            
        this.toolExecutionTimer = Timer.builder("mcp.tool.duration")
            .register(registry);
    }
    
    public void recordToolExecution(String toolName, Duration duration) {
        toolExecutionCounter.increment();
        toolExecutionTimer.record(duration);
    }
}
```

### Distributed Tracing

**Trace Context Propagation**:
```
MCP Request
  └─ McpController [span: http.server]
      └─ McpProtocolHandler [span: mcp.protocol]
          └─ McpToolRegistry [span: mcp.tool]
              └─ DocumentService [span: service.document]
                  └─ FileNetClient [span: http.client]
```

### Health Checks

```java
@Component
public class EcmHealthIndicator implements HealthIndicator {
    
    private final EcmClient ecmClient;
    
    @Override
    public Health health() {
        try {
            // Ping ECM system
            ecmClient.getDocument("health-check-doc").block();
            return Health.up()
                .withDetail("ecm", "FileNet")
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Testing Strategy

### Test Pyramid

```
        /\
       /  \  E2E Tests (5%)
      /────\  
     /      \  Integration Tests (20%)
    /────────\  
   /          \  Unit Tests (75%)
  /────────────\
```

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    
    @Mock
    private McpToolRegistry registry;
    
    @Mock
    private EcmClient client;
    
    @InjectMocks
    private DocumentService service;
    
    @Test
    void shouldRegisterGetDocumentTool() {
        // Given
        service.registerTools();
        
        // Then
        verify(registry, times(1)).registerTool(
            argThat(tool -> tool.getName().equals("ecm_get_document")),
            any(Function.class)
        );
    }
}
```

### Integration Test Example

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class McpControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldListTools() {
        // Given
        McpRequest request = new McpRequest();
        request.setMethod("tools/list");
        request.setId("test-1");
        
        // When
        ResponseEntity<McpResponse> response = 
            restTemplate.postForEntity("/mcp", request, McpResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResult()).isNotNull();
    }
}
```

## Deployment

### Container Deployment

**Dockerfile**:
```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/ecm-mcp-server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose**:
```yaml
version: '3.8'

services:
  ecm-mcp-server:
    build: .
    ports:
      - "8080:8080"
    environment:
      - FILENET_BASE_URL=http://filenet:9080/fncmis
      - FILENET_USERNAME=admin
      - FILENET_PASSWORD=password
    depends_on:
      - filenet
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ecm-mcp-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ecm-mcp-server
  template:
    metadata:
      labels:
        app: ecm-mcp-server
    spec:
      containers:
      - name: ecm-mcp-server
        image: ecm-mcp-server:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: FILENET_BASE_URL
          valueFrom:
            configMapKeyRef:
              name: ecm-config
              key: filenet-url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
```

## Performance Considerations

### Reactive Programming Benefits

1. **Non-blocking I/O**: Handles thousands of concurrent requests
2. **Backpressure**: Prevents overwhelming downstream systems
3. **Efficient resource usage**: Fewer threads needed
4. **Composition**: Chain operations declaratively

### Optimization Strategies

1. **Connection Pooling**:
```java
HttpClient httpClient = HttpClient.create()
    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
    .responseTimeout(Duration.ofSeconds(30))
    .option(ChannelOption.SO_KEEPALIVE, true);
```

2. **Caching** (for frequently accessed data):
```java
@Cacheable("documents")
public Mono<Document> getDocument(String id) {
    return client.getDocument(id);
}
```

3. **Parallel Execution**:
```java
public Mono<List<Document>> getMultipleDocuments(List<String> ids) {
    return Flux.fromIterable(ids)
        .flatMap(id -> client.getDocument(id))
        .parallel()
        .runOn(Schedulers.parallel())
        .sequential()
        .collectList();
}
```

## Future Enhancements

### Planned Features

1. **Multi-tenancy Support**
   - Tenant isolation
   - Per-tenant configuration
   - Tenant-specific tools

2. **Advanced Search**
   - Full-text search
   - Faceted search
   - Saved searches

3. **Workflow Engine Integration**
   - Camunda/Activiti integration
   - Process monitoring
   - Task management

4. **Event-Driven Architecture**
   - Document change events
   - Workflow events
   - Integration with Kafka/RabbitMQ

5. **GraphQL API**
   - Alternative to JSON-RPC
   - Better for complex queries
   - Real-time subscriptions

## Conclusion

This architecture provides a solid foundation for building extensible MCP servers at JPMorgan Chase. The key strengths are:

✅ **Extensibility**: Easy to add new tools and backends
✅ **Enterprise-Ready**: Production-grade resilience and observability
✅ **Performance**: Reactive programming for high concurrency
✅ **Maintainability**: Clear separation of concerns
✅ **Testability**: Comprehensive testing strategy

For questions or contributions, please refer to the COPILOT_GUIDE.md for detailed implementation instructions.
