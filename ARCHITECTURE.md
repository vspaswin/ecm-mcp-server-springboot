# ECM MCP Server - Architecture Documentation

## System Overview

The ECM MCP Server acts as a bridge between AI assistants (like Claude Desktop) and Enterprise Content Management systems, exposing ECM operations as MCP tools that AI can invoke.

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    AI Assistant                         │
│              (Claude Desktop, etc.)                     │
│                                                         │
│  - Natural language understanding                       │
│  - Tool selection and parameter extraction              │
│  - Response interpretation                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ MCP Protocol (JSON-RPC over STDIO)
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              ECM MCP Server (This Application)          │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │         McpServerApplication                      │ │
│  │  - JSON-RPC request/response handling             │ │
│  │  - Tool registration and routing                  │ │
│  │  - Protocol compliance (initialize, tools/*, etc) │ │
│  └────────────────────┬──────────────────────────────┘ │
│                       │                                 │
│  ┌────────────────────▼──────────────────────────────┐ │
│  │           Handler Layer                           │ │
│  │                                                   │ │
│  │  ┌──────────────┐  ┌──────────────┐             │ │
│  │  │ Document     │  │ Folder       │             │ │
│  │  │ Handler      │  │ Handler      │ ...         │ │
│  │  └──────┬───────┘  └──────┬───────┘             │ │
│  └─────────┼──────────────────┼─────────────────────┘ │
│            │                  │                        │
│  ┌─────────▼──────────────────▼─────────────────────┐ │
│  │           Service Layer                           │ │
│  │                                                   │ │
│  │  ┌──────────────┐  ┌──────────────┐             │ │
│  │  │ Document     │  │ Folder       │             │ │
│  │  │ Service      │  │ Service      │ ...         │ │
│  │  └──────┬───────┘  └──────┬───────┘             │ │
│  └─────────┼──────────────────┼─────────────────────┘ │
│            │                  │                        │
│  ┌─────────▼──────────────────▼─────────────────────┐ │
│  │           Client Layer                            │ │
│  │                                                   │ │
│  │          ┌──────────────────┐                    │ │
│  │          │  EcmApiClient    │                    │ │
│  │          │  (WebClient)     │                    │ │
│  │          └────────┬─────────┘                    │ │
│  └───────────────────┼───────────────────────────────┘ │
└────────────────────────┼────────────────────────────────┘
                        │
                        │ REST API (HTTP)
                        │ - Authentication (Basic Auth)
                        │ - JSON payloads
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                ECM Backend System                       │
│                                                         │
│  - Document storage and management                      │
│  - Access control and permissions                       │
│  - Workflow engine                                      │
│  - Search and indexing                                  │
│  - Version control                                      │
└─────────────────────────────────────────────────────────┘
```

---

## Component Architecture

### 1. MCP Protocol Layer

**Purpose**: Implements the Model Context Protocol specification for communication with AI assistants.

**Components**:
- `McpServerApplication` - Main entry point, handles STDIO communication

**Responsibilities**:
- Read JSON-RPC messages from STDIN
- Parse and validate MCP protocol messages
- Route requests to appropriate handlers
- Format and send JSON-RPC responses to STDOUT
- Handle MCP lifecycle (initialize, initialized, shutdown)

**Protocol Flow**:
```
Client                           Server
  │                                │
  ├──initialize──────────────────► │
  │                                │
  │ ◄──────────────────initialize──┤
  │                                │
  ├──initialized─────────────────► │
  │                                │
  ├──tools/list──────────────────► │
  │                                │
  │ ◄──────────────────tools/list──┤
  │                                │
  ├──tools/call──────────────────► │
  │  (documentId: "doc123")        │
  │                                │
  │ ◄──────────────────tools/call──┤
  │  {document: {...}}             │
  │                                │
```

---

### 2. Handler Layer

**Purpose**: Bridge between MCP protocol and business logic, handling tool invocations.

**Design Pattern**: Command Pattern
- Each handler is responsible for a domain area
- Handlers translate MCP tool calls into service calls
- Handlers format service responses for MCP protocol

**Handler Structure**:
```java
@Component
public class DocumentHandler {
    private final DocumentService service;
    
    // Tool routing
    public Map<String, Object> handleToolCall(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "document_get" -> handleGet(arguments);
            case "document_create" -> handleCreate(arguments);
            // ...
        };
    }
    
    // Parameter extraction and validation
    private Map<String, Object> handleGet(Map<String, Object> arguments) {
        String documentId = extractRequired(arguments, "documentId");
        Document doc = service.getDocument(documentId).block();
        return formatSuccess(doc);
    }
}
```

**Available Handlers**:
- `DocumentHandler` - Document CRUD operations
- `FolderHandler` - Folder management
- `SearchHandler` - Document search
- `SecurityHandler` - Permissions management
- `WorkflowHandler` - Workflow operations
- `MetadataHandler` - Schema and validation

---

### 3. Service Layer

**Purpose**: Business logic and orchestration between handlers and API client.

**Design Pattern**: Service Layer Pattern
- Services contain domain-specific business logic
- Services coordinate multiple API calls if needed
- Services handle validation and error handling
- Services are the unit of reusability

**Service Characteristics**:
- **Stateless** - No instance variables for request data
- **Reactive** - Returns Mono/Flux for non-blocking operations
- **Transactional** - Coordinates multiple operations
- **Validated** - Enforces business rules

**Service Example**:
```java
@Service
public class DocumentService {
    private final EcmApiClient ecmApiClient;
    
    public Mono<Document> createDocument(Document document) {
        // Validation
        validateDocument(document);
        
        // Business logic
        enrichMetadata(document);
        
        // API call
        return ecmApiClient.createDocument(document)
            .doOnSuccess(doc -> log.info("Document created: {}", doc.getId()))
            .doOnError(e -> log.error("Failed to create document", e));
    }
    
    private void validateDocument(Document document) {
        if (document.getName() == null || document.getName().isBlank()) {
            throw new IllegalArgumentException("Document name is required");
        }
        if (document.getFolderId() == null) {
            throw new IllegalArgumentException("Folder ID is required");
        }
    }
}
```

---

### 4. Client Layer

**Purpose**: HTTP communication with ECM backend API.

**Design Pattern**: API Client Pattern
- Encapsulates all HTTP communication
- Handles authentication
- Manages request/response transformation
- Implements error handling and retry logic

**Client Architecture**:
```
┌───────────────────────────────────────────┐
│          EcmApiClient                     │
├───────────────────────────────────────────┤
│                                           │
│  WebClient (Spring WebFlux)               │
│    │                                      │
│    ├─ Base URL configuration              │
│    ├─ Default headers (Auth, Content)     │
│    ├─ Timeout configuration               │
│    └─ Error handling                      │
│                                           │
│  Methods:                                 │
│    - getDocument(id)                      │
│    - createDocument(doc)                  │
│    - updateDocument(id, doc)              │
│    - deleteDocument(id)                   │
│    - searchDocuments(query)               │
│    - ...                                  │
└───────────────────────────────────────────┘
```

**WebClient Configuration**:
```java
@Bean
public WebClient ecmWebClient(
        @Value("${ecm.api.base-url}") String baseUrl,
        @Value("${ecm.api.username}") String username,
        @Value("${ecm.api.password}") String password) {
    
    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, createBasicAuth(username, password))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
}
```

---

## Data Flow

### Request Flow (Tool Invocation)

```
1. AI Assistant                "Get document doc123"
   │
   ▼
2. MCP Client                  Generate tool call:
   │                           { method: "tools/call",
   │                             params: {
   │                               name: "document_get",
   │                               arguments: { documentId: "doc123" }
   │                             }
   │                           }
   ▼
3. McpServerApplication        Parse JSON-RPC request
   │                           Identify tool: "document_get"
   │                           Route to DocumentHandler
   ▼
4. DocumentHandler             Extract parameter: documentId
   │                           Validate parameters
   │                           Call service.getDocument("doc123")
   ▼
5. DocumentService             Business validation
   │                           Call ecmApiClient.getDocument("doc123")
   ▼
6. EcmApiClient                Build HTTP request:
   │                           GET /api/documents/doc123
   │                           Headers: Authorization, Content-Type
   ▼
7. ECM Backend                 Process request
   │                           Retrieve document from storage
   │                           Return JSON response
   ▼
8. EcmApiClient                Parse response to Document object
   │                           Handle errors if any
   │                           Return Mono<Document>
   ▼
9. DocumentService             Apply business logic if needed
   │                           Return Document
   ▼
10. DocumentHandler            Format as MCP response:
    │                          { success: true,
    │                            document: {...} }
    ▼
11. McpServerApplication       Wrap in JSON-RPC response
    │                          Send to STDOUT
    ▼
12. MCP Client                 Parse response
    │                          Extract document data
    ▼
13. AI Assistant               "Here's the document information: ..."
```

---

## Technology Stack

### Core Framework
- **Spring Boot 3.4.2** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Project Reactor** - Reactive programming library

### Build & Dependencies
- **Maven 3.9+** - Build tool
- **Java 21** - Programming language

### Key Dependencies
```xml
<!-- Reactive Web -->
org.springframework.boot:spring-boot-starter-webflux

<!-- JSON Processing -->
com.fasterxml.jackson.core:jackson-databind

<!-- Logging -->
ch.qos.logback:logback-classic

<!-- Testing -->
org.springframework.boot:spring-boot-starter-test
com.squareup.okhttp3:mockwebserver (for integration tests)
```

---

## Design Patterns

### 1. Layered Architecture
**Why**: Separation of concerns, maintainability, testability

- **Handler Layer**: Protocol translation
- **Service Layer**: Business logic
- **Client Layer**: External communication

### 2. Dependency Injection
**Why**: Loose coupling, testability, configurability

```java
@Component
public class DocumentHandler {
    private final DocumentService documentService;
    
    // Constructor injection - Spring autowires
    public DocumentHandler(DocumentService documentService) {
        this.documentService = documentService;
    }
}
```

### 3. Repository Pattern
**Why**: Abstraction over data access

`EcmApiClient` acts as a repository for ECM data, abstracting HTTP details.

### 4. Command Pattern
**Why**: Encapsulate requests as objects

Each MCP tool is a command with parameters, routed to appropriate handler.

### 5. Factory Pattern
**Why**: Object creation abstraction

`WebClientConfig` creates configured WebClient instances.

---

## Reactive Programming Model

### Why Reactive?

1. **Non-blocking I/O** - Handle many concurrent requests efficiently
2. **Backpressure** - Handle slow consumers gracefully
3. **Composability** - Chain operations declaratively
4. **Resource Efficiency** - Fewer threads, better scaling

### Reactive Types

**Mono<T>** - 0 or 1 element
```java
public Mono<Document> getDocument(String id) {
    return webClient.get()
        .uri("/documents/{id}", id)
        .retrieve()
        .bodyToMono(Document.class);
}
```

**Flux<T>** - 0 to N elements
```java
public Flux<Document> searchDocuments(String query) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/documents/search")
            .queryParam("q", query)
            .build())
        .retrieve()
        .bodyToFlux(Document.class);
}
```

### Reactive Operators

```java
// Transformation
mono.map(doc -> doc.getName())

// Error handling
mono.onErrorResume(e -> Mono.just(fallbackDocument))

// Side effects
mono.doOnSuccess(doc -> log.info("Retrieved: {}", doc.getId()))

// Composition
mono.flatMap(doc -> getPermissions(doc.getId()))

// Blocking (only in handlers)
Document doc = mono.block();
```

---

## Configuration Management

### Configuration Hierarchy

```
1. application.yml (defaults)
   ↓
2. application-{profile}.yml (profile-specific)
   ↓
3. Environment variables (runtime overrides)
   ↓
4. Command-line arguments (highest priority)
```

### Configuration Properties

```yaml
# application.yml
ecm:
  api:
    base-url: ${ECM_BASE_URL:http://localhost:8081/api}
    username: ${ECM_USERNAME:admin}
    password: ${ECM_PASSWORD:admin}
    timeout: 30
    max-retries: 3

spring:
  application:
    name: ecm-mcp-server

logging:
  level:
    com.jpmc.ecm: DEBUG
    org.springframework.web.reactive: INFO
```

### Environment-Specific Profiles

```yaml
# application-dev.yml
ecm:
  api:
    base-url: http://localhost:8081/api
    
logging:
  level:
    com.jpmc.ecm: DEBUG

# application-prod.yml
ecm:
  api:
    base-url: https://ecm.jpmc.com/api
    
logging:
  level:
    com.jpmc.ecm: INFO
```

---

## Security Considerations

### Authentication

**Current**: Basic Authentication
```java
String basicAuth = Base64.getEncoder()
    .encodeToString((username + ":" + password).getBytes());
    
webClient.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
```

**Future Enhancements**:
- OAuth 2.0 / JWT tokens
- Certificate-based authentication
- Kerberos/SPNEGO

### Credential Management

**Current**: Environment variables
- `ECM_USERNAME`
- `ECM_PASSWORD`

**Best Practices**:
- Never commit credentials to version control
- Use secure secret management (Vault, AWS Secrets Manager)
- Rotate credentials regularly
- Use least-privilege principles

### Authorization

The server acts as a proxy - authorization is enforced by ECM backend:
- User permissions checked by ECM
- MCP server passes through user context
- No authorization logic in MCP server itself

---

## Error Handling Strategy

### Error Flow

```
ECM Backend Error
   ↓
EcmApiClient catches WebClientResponseException
   ↓
Transforms to domain exception
   ↓
Service layer handles/propagates
   ↓
Handler catches and formats
   ↓
MCP error response to client
```

### Error Response Format

```json
{
  "success": false,
  "error": "Document not found",
  "errorCode": "DOCUMENT_NOT_FOUND",
  "details": {
    "documentId": "doc123"
  }
}
```

### Exception Hierarchy

```
RuntimeException
├── EcmException (base)
│   ├── DocumentNotFoundException
│   ├── FolderNotFoundException
│   ├── PermissionDeniedException
│   ├── InvalidMetadataException
│   └── WorkflowException
└── WebClientResponseException (Spring)
```

---

## Testing Strategy

### Test Pyramid

```
        ┌─────────────┐
        │     E2E     │  (Few) - Full MCP protocol tests
        └─────────────┘
       ┌───────────────┐
       │  Integration  │  (Some) - API client with MockWebServer
       └───────────────┘
      ┌─────────────────┐
      │      Unit       │  (Many) - Service and handler logic
      └─────────────────┘
```

### Unit Tests
**Target**: Services and handlers  
**Tools**: JUnit 5, Mockito  
**Coverage**: Business logic, validation, error handling

```java
@SpringBootTest
class DocumentServiceTest {
    @MockBean
    private EcmApiClient ecmApiClient;
    
    @Autowired
    private DocumentService documentService;
    
    @Test
    void testGetDocument() {
        // Arrange
        when(ecmApiClient.getDocument("doc123"))
            .thenReturn(Mono.just(expectedDocument));
        
        // Act
        Document result = documentService.getDocument("doc123").block();
        
        // Assert
        assertEquals(expectedDocument, result);
    }
}
```

### Integration Tests
**Target**: API client  
**Tools**: MockWebServer  
**Coverage**: HTTP communication, serialization

```java
class EcmApiClientTest {
    private MockWebServer mockWebServer;
    private EcmApiClient client;
    
    @Test
    void testGetDocument() {
        mockWebServer.enqueue(new MockResponse()
            .setBody(documentJson)
            .setResponseCode(200));
        
        Document doc = client.getDocument("doc123").block();
        
        assertNotNull(doc);
        assertEquals("doc123", doc.getId());
    }
}
```

---

## Performance Considerations

### Reactive Non-Blocking I/O

**Benefit**: Handle many concurrent requests with few threads

```
Traditional (Blocking):
1 thread per request
1000 requests = 1000 threads

Reactive (Non-blocking):
Few threads (typically CPU cores × 2)
1000 requests = ~8 threads
```

### Connection Pooling

WebClient automatically manages connection pool:
- Reuses HTTP connections
- Reduces connection overhead
- Configurable max connections

### Timeout Configuration

```yaml
ecm:
  api:
    timeout: 30  # seconds
```

Applied to:
- Connection timeout
- Read timeout
- Response timeout

### Backpressure

Reactive streams handle slow consumers:
- Producer (ECM API) won't overwhelm consumer (MCP client)
- Automatic flow control
- Prevents memory overflow

---

## Extensibility

### Adding New Domain Areas

The architecture is designed for easy extension:

1. **Create Model** - Define domain objects
2. **Create Service** - Implement business logic
3. **Extend Client** - Add API methods
4. **Create Handler** - Implement tool handlers
5. **Register Tools** - Add to McpServerApplication

### Plugin Architecture (Future)

Potential enhancement for dynamic tool loading:

```java
public interface ToolPlugin {
    String getName();
    List<ToolDefinition> getTools();
    Map<String, Object> executeTool(String toolName, Map<String, Object> args);
}

@Configuration
public class PluginLoader {
    public List<ToolPlugin> loadPlugins() {
        // Discover and load plugins from classpath
    }
}
```

---

## Deployment Architecture

### Local Development

```
Developer Machine
├── IDE (IntelliJ IDEA / VS Code)
├── Java 21 JDK
├── Maven 3.9+
└── Claude Desktop (MCP Client)
```

### Enterprise Deployment

```
┌─────────────────────────────────────┐
│      AI Workstations                │
│   (Claude Desktop instances)        │
└────────────┬────────────────────────┘
             │
             │ STDIO/Local execution
             │
┌────────────▼────────────────────────┐
│   ECM MCP Server (JAR)              │
│   - Deployed on workstation         │
│   - Environment-specific config     │
│   - Credentials from env vars       │
└────────────┬────────────────────────┘
             │
             │ HTTPS/REST
             │
┌────────────▼────────────────────────┐
│   ECM Backend Servers               │
│   - Load balanced                   │
│   - High availability               │
│   - Authentication & authorization  │
└─────────────────────────────────────┘
```

---

## Monitoring & Observability

### Logging

**Framework**: SLF4J with Logback

**Log Levels**:
- `ERROR` - Errors requiring immediate attention
- `WARN` - Potential issues, degraded functionality
- `INFO` - Significant events (startup, config, tool calls)
- `DEBUG` - Detailed diagnostic information
- `TRACE` - Very detailed diagnostic information

**Log Format**:
```
2026-02-13 20:28:33.123 INFO  [main] c.j.e.m.McpServerApplication : Starting ECM MCP Server
2026-02-13 20:28:35.456 DEBUG [main] c.j.e.h.DocumentHandler : Handling document_get for doc123
2026-02-13 20:28:35.789 ERROR [main] c.j.e.c.EcmApiClient : Failed to get document: 404 Not Found
```

### Metrics (Future Enhancement)

Potential metrics to track:
- Tool call counts by tool name
- Response times by operation
- Error rates by error type
- ECM API response times
- Concurrent request counts

### Tracing (Future Enhancement)

Distributed tracing integration:
- Spring Cloud Sleuth
- OpenTelemetry
- Zipkin/Jaeger

---

## Future Enhancements

### 1. Async Tool Execution
**Problem**: Long-running operations block MCP client  
**Solution**: Implement async tools with progress reporting

### 2. Caching Layer
**Problem**: Repeated API calls for same data  
**Solution**: Redis/Caffeine cache for frequently accessed data

### 3. Rate Limiting
**Problem**: Overwhelming ECM backend with requests  
**Solution**: Implement token bucket or sliding window rate limiter

### 4. Circuit Breaker
**Problem**: Cascading failures when ECM backend is down  
**Solution**: Resilience4j circuit breaker pattern

### 5. Batch Operations
**Problem**: Multiple individual API calls inefficient  
**Solution**: Batch API endpoints for bulk operations

### 6. Event Streaming
**Problem**: Polling for changes is inefficient  
**Solution**: WebSocket or SSE for real-time updates

### 7. Multi-tenancy
**Problem**: Different users/teams need different ECM backends  
**Solution**: Tenant resolution and configuration routing

---

## References

- [Model Context Protocol Specification](https://modelcontextprotocol.io)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Reactive Programming](https://www.reactivemanifesto.org/)

---

**Document Version**: 1.0.0  
**Last Updated**: February 13, 2026  
**Author**: ECM MCP Team