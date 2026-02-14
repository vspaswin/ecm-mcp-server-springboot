# GitHub Copilot Guide for ECM MCP Server

## Project Overview

This is a **Spring Boot 3.4.2** application implementing the **Model Context Protocol (MCP)** to expose Enterprise Content Management (ECM) operations as tools that can be used by AI assistants like Claude Desktop.

### Key Technologies
- **Java 21** (no preview features)
- **Spring Boot 3.4.2** with WebFlux (reactive programming)
- **Maven** build system
- **MCP (Model Context Protocol)** for AI tool integration
- **RESTful API client** for ECM backend communication

---

## Architecture Overview

### Layered Architecture

```
┌─────────────────────────────────────────┐
│   MCP Client (Claude Desktop, etc.)     │
└─────────────────┬───────────────────────┘
                  │ STDIO (JSON-RPC)
┌─────────────────▼───────────────────────┐
│      McpServerApplication               │
│  - Handles JSON-RPC requests/responses  │
│  - Routes to appropriate handlers       │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Tool Handlers Layer               │
│  - DocumentHandler                      │
│  - FolderHandler                        │
│  - SearchHandler                        │
│  - SecurityHandler                      │
│  - WorkflowHandler                      │
│  - MetadataHandler                      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Service Layer                     │
│  - DocumentService                      │
│  - FolderService                        │
│  - SearchService                        │
│  - SecurityService                      │
│  - WorkflowService                      │
│  - MetadataService                      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Client Layer                      │
│  - EcmApiClient (WebClient-based)       │
│  - Handles HTTP communication           │
│  - Authentication & error handling      │
└─────────────────┬───────────────────────┘
                  │ REST API
┌─────────────────▼───────────────────────┐
│       ECM Backend System                │
└─────────────────────────────────────────┘
```

---

## Core Components

### 1. MCP Server Application (`McpServerApplication.java`)

**Location**: `com.jpmc.ecm.mcp.McpServerApplication`

**Responsibilities**:
- Reads JSON-RPC requests from STDIN
- Parses MCP protocol messages
- Routes tool calls to appropriate handlers
- Sends JSON-RPC responses to STDOUT
- Manages server lifecycle

**Key Methods**:
```java
public void start() // Main server loop
private void processRequest(Map<String, Object> request) // Request router
private void handleInitialize(Map<String, Object> request) // MCP handshake
private void handleToolsList(Map<String, Object> request) // List available tools
private void handleToolsCall(Map<String, Object> request) // Execute tool
```

**MCP Protocol Flow**:
1. Client sends `initialize` request
2. Server responds with capabilities
3. Client sends `initialized` notification
4. Client can now call `tools/list` and `tools/call`

---

### 2. Tool Handlers

Each handler corresponds to a domain area and exposes MCP tools.

#### DocumentHandler (`com.jpmc.ecm.handler.DocumentHandler`)

**Tools Exposed**:
- `document_get` - Retrieve document by ID
- `document_create` - Create new document
- `document_update` - Update existing document
- `document_delete` - Delete document
- `document_get_content` - Get document content/binary
- `document_get_versions` - List document versions
- `document_checkout` - Check out document for editing
- `document_checkin` - Check in document after editing
- `document_cancel_checkout` - Cancel checkout

**Pattern**:
```java
@Component
public class DocumentHandler {
    private final DocumentService documentService;
    
    public DocumentHandler(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    public Map<String, Object> handleToolCall(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "document_get" -> handleGet(arguments);
            case "document_create" -> handleCreate(arguments);
            // ... other cases
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }
}
```

#### FolderHandler (`com.jpmc.ecm.handler.FolderHandler`)

**Tools Exposed**:
- `folder_get` - Retrieve folder by ID
- `folder_create` - Create new folder
- `folder_update` - Update folder metadata
- `folder_delete` - Delete folder
- `folder_list_children` - List folder contents
- `folder_move` - Move folder to new parent

#### SearchHandler (`com.jpmc.ecm.handler.SearchHandler`)

**Tools Exposed**:
- `search_documents` - Search documents by criteria
- `search_advanced` - Advanced search with complex queries

#### SecurityHandler (`com.jpmc.ecm.handler.SecurityHandler`)

**Tools Exposed**:
- `security_get_permissions` - Get object permissions
- `security_set_permissions` - Set object permissions
- `security_add_user` - Add user permission
- `security_remove_user` - Remove user permission

#### WorkflowHandler (`com.jpmc.ecm.handler.WorkflowHandler`)

**Tools Exposed**:
- `workflow_start` - Start workflow on object
- `workflow_get_status` - Get workflow status
- `workflow_approve` - Approve workflow task
- `workflow_reject` - Reject workflow task

#### MetadataHandler (`com.jpmc.ecm.handler.MetadataHandler`)

**Tools Exposed**:
- `metadata_get_schema` - Get object type schema
- `metadata_validate` - Validate metadata values

---

### 3. Service Layer

Services contain business logic and orchestrate calls to the ECM API client.

#### Common Service Pattern

```java
@Service
public class DocumentService {
    private final EcmApiClient ecmApiClient;
    
    public DocumentService(EcmApiClient ecmApiClient) {
        this.ecmApiClient = ecmApiClient;
    }
    
    public Mono<Document> getDocument(String documentId) {
        return ecmApiClient.getDocument(documentId);
    }
    
    public Mono<Document> createDocument(Document document) {
        validateDocument(document);
        return ecmApiClient.createDocument(document);
    }
    
    private void validateDocument(Document document) {
        // Business validation logic
    }
}
```

**Services Available**:
- `DocumentService` - Document operations
- `FolderService` - Folder operations
- `SearchService` - Search operations
- `SecurityService` - Security/permissions
- `WorkflowService` - Workflow operations
- `MetadataService` - Metadata validation

---

### 4. ECM API Client (`EcmApiClient.java`)

**Location**: `com.jpmc.ecm.client.EcmApiClient`

**Responsibilities**:
- HTTP communication with ECM backend
- Authentication header injection
- Error handling and logging
- Request/response transformation

**Configuration**:
```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient ecmWebClient(
            @Value("${ecm.api.base-url}") String baseUrl,
            @Value("${ecm.api.username}") String username,
            @Value("${ecm.api.password}") String password) {
        
        String basicAuth = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes());
        
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

**Key Methods**:
```java
// Document operations
public Mono<Document> getDocument(String documentId)
public Mono<Document> createDocument(Document document)
public Mono<Document> updateDocument(String documentId, Document document)
public Mono<Void> deleteDocument(String documentId)
public Mono<byte[]> getDocumentContent(String documentId)

// Folder operations
public Mono<Folder> getFolder(String folderId)
public Mono<Folder> createFolder(Folder folder)
public Mono<List<EcmObject>> listFolderChildren(String folderId)

// Search operations
public Mono<SearchResult> searchDocuments(String query, int maxResults)

// Security operations
public Mono<Permissions> getPermissions(String objectId)
public Mono<Permissions> setPermissions(String objectId, Permissions permissions)

// Workflow operations
public Mono<WorkflowInstance> startWorkflow(String objectId, String workflowName)
public Mono<WorkflowStatus> getWorkflowStatus(String workflowId)
```

---

## Domain Models

### Document (`com.jpmc.ecm.model.Document`)

```java
public class Document {
    private String id;
    private String name;
    private String objectType;
    private String folderId;
    private Map<String, Object> properties;
    private String contentType;
    private Long contentSize;
    private String versionLabel;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    // getters/setters
}
```

### Folder (`com.jpmc.ecm.model.Folder`)

```java
public class Folder {
    private String id;
    private String name;
    private String parentId;
    private String path;
    private Map<String, Object> properties;
    private String createdBy;
    private LocalDateTime createdDate;
    // getters/setters
}
```

### SearchResult (`com.jpmc.ecm.model.SearchResult`)

```java
public class SearchResult {
    private List<Document> documents;
    private int totalCount;
    private boolean hasMore;
    // getters/setters
}
```

### Permissions (`com.jpmc.ecm.model.Permissions`)

```java
public class Permissions {
    private String objectId;
    private List<AccessControlEntry> entries;
    // getters/setters
}

public class AccessControlEntry {
    private String principal; // username or group
    private List<String> permissions; // READ, WRITE, DELETE, etc.
    // getters/setters
}
```

### WorkflowInstance (`com.jpmc.ecm.model.WorkflowInstance`)

```java
public class WorkflowInstance {
    private String workflowId;
    private String objectId;
    private String workflowName;
    private String status; // ACTIVE, COMPLETED, ABORTED
    private List<WorkflowTask> tasks;
    // getters/setters
}
```

---

## Configuration

### application.yml

```yaml
server:
  port: 8080

ecm:
  api:
    base-url: ${ECM_BASE_URL:http://localhost:8081/api}
    username: ${ECM_USERNAME:admin}
    password: ${ECM_PASSWORD:admin}
    timeout: 30

spring:
  application:
    name: ecm-mcp-server
  
logging:
  level:
    com.jpmc.ecm: DEBUG
    org.springframework.web.reactive: DEBUG
```

### Environment Variables

- `ECM_BASE_URL` - ECM backend API URL
- `ECM_USERNAME` - ECM API username
- `ECM_PASSWORD` - ECM API password

---

## Adding New Tools

### Step 1: Define the Tool in Handler

Add a new case to the appropriate handler's `handleToolCall` method:

```java
// In DocumentHandler.java
public Map<String, Object> handleToolCall(String toolName, Map<String, Object> arguments) {
    return switch (toolName) {
        // ... existing tools
        case "document_copy" -> handleCopy(arguments);
        default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
    };
}

private Map<String, Object> handleCopy(Map<String, Object> arguments) {
    String sourceId = (String) arguments.get("sourceId");
    String targetFolderId = (String) arguments.get("targetFolderId");
    String newName = (String) arguments.get("newName");
    
    Document copied = documentService.copyDocument(sourceId, targetFolderId, newName)
        .block();
    
    return Map.of(
        "success", true,
        "document", copied
    );
}
```

### Step 2: Add Service Method

```java
// In DocumentService.java
public Mono<Document> copyDocument(String sourceId, String targetFolderId, String newName) {
    return ecmApiClient.copyDocument(sourceId, targetFolderId, newName);
}
```

### Step 3: Add Client Method

```java
// In EcmApiClient.java
public Mono<Document> copyDocument(String sourceId, String targetFolderId, String newName) {
    Map<String, Object> request = Map.of(
        "sourceId", sourceId,
        "targetFolderId", targetFolderId,
        "name", newName
    );
    
    return webClient.post()
        .uri("/documents/copy")
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, response -> 
            response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("Copy failed: " + body))))
        .bodyToMono(Document.class);
}
```

### Step 4: Register Tool in McpServerApplication

```java
// In McpServerApplication.java - handleToolsList method
private void handleToolsList(Map<String, Object> request) {
    List<Map<String, Object>> tools = new ArrayList<>();
    
    // ... existing tools
    
    tools.add(Map.of(
        "name", "document_copy",
        "description", "Copy a document to a different folder",
        "inputSchema", Map.of(
            "type", "object",
            "properties", Map.of(
                "sourceId", Map.of(
                    "type", "string",
                    "description", "ID of document to copy"
                ),
                "targetFolderId", Map.of(
                    "type", "string",
                    "description", "ID of target folder"
                ),
                "newName", Map.of(
                    "type", "string",
                    "description", "Name for the copied document"
                )
            ),
            "required", List.of("sourceId", "targetFolderId")
        )
    ));
    
    // ... rest of method
}
```

---

## Testing

### Unit Test Example

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
        String docId = "doc123";
        Document expectedDoc = new Document();
        expectedDoc.setId(docId);
        expectedDoc.setName("test.pdf");
        
        when(ecmApiClient.getDocument(docId))
            .thenReturn(Mono.just(expectedDoc));
        
        // Act
        Document result = documentService.getDocument(docId).block();
        
        // Assert
        assertNotNull(result);
        assertEquals(docId, result.getId());
        assertEquals("test.pdf", result.getName());
    }
}
```

### Integration Test with MockWebServer

```java
@SpringBootTest
class EcmApiClientTest {
    
    private MockWebServer mockWebServer;
    private EcmApiClient ecmApiClient;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
        
        ecmApiClient = new EcmApiClient(webClient);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void testGetDocument() {
        // Arrange
        String docId = "doc123";
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"id\":\"doc123\",\"name\":\"test.pdf\"}")
            .addHeader("Content-Type", "application/json"));
        
        // Act
        Document result = ecmApiClient.getDocument(docId).block();
        
        // Assert
        assertNotNull(result);
        assertEquals(docId, result.getId());
    }
}
```

---

## Error Handling

### Standard Error Response

All handlers should return errors in this format:

```java
return Map.of(
    "success", false,
    "error", errorMessage,
    "errorCode", "ERROR_CODE"
);
```

### Exception Handling in Services

```java
public Mono<Document> getDocument(String documentId) {
    return ecmApiClient.getDocument(documentId)
        .onErrorResume(WebClientResponseException.class, e -> {
            log.error("Failed to get document {}: {}", documentId, e.getMessage());
            return Mono.error(new DocumentNotFoundException(
                "Document not found: " + documentId));
        });
}
```

---

## Code Style & Conventions

### Naming Conventions
- **Classes**: PascalCase (e.g., `DocumentService`)
- **Methods**: camelCase (e.g., `getDocument`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.jpmc.ecm.service`)

### Package Structure
```
com.jpmc.ecm
├── client          # External API clients
├── config          # Spring configuration classes
├── handler         # MCP tool handlers
├── mcp             # MCP server application
├── model           # Domain models (POJOs)
└── service         # Business logic services
```

### Reactive Programming
- Use `Mono<T>` for single value responses
- Use `Flux<T>` for multiple value streams
- Never call `.block()` in production service code
- Use `.block()` only in handlers when returning to MCP client

### Dependency Injection
- Use constructor injection (preferred)
- Mark beans with `@Component`, `@Service`, or `@Configuration`
- Avoid field injection

---

## Building & Running

### Build
```bash
mvn clean install
```

### Run
```bash
java -jar target/ecm-mcp-server-1.0.0.jar
```

### Run with Custom Config
```bash
ECM_BASE_URL=https://ecm.jpmc.com/api \
ECM_USERNAME=myuser \
ECM_PASSWORD=mypass \
java -jar target/ecm-mcp-server-1.0.0.jar
```

---

## MCP Client Configuration (Claude Desktop)

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "ecm": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/ecm-mcp-server-1.0.0.jar"
      ],
      "env": {
        "ECM_BASE_URL": "http://localhost:8081/api",
        "ECM_USERNAME": "admin",
        "ECM_PASSWORD": "admin"
      }
    }
  }
}
```

---

## Extending the Server

### Adding a New Domain Area

1. **Create Model Classes** in `com.jpmc.ecm.model`
2. **Create Service** in `com.jpmc.ecm.service`
3. **Add API Methods** to `EcmApiClient`
4. **Create Handler** in `com.jpmc.ecm.handler`
5. **Register Tools** in `McpServerApplication.handleToolsList()`
6. **Route Calls** in `McpServerApplication.handleToolsCall()`

### Example: Adding Audit Functionality

```java
// 1. Model
public class AuditEntry {
    private String objectId;
    private String action;
    private String user;
    private LocalDateTime timestamp;
    // getters/setters
}

// 2. Service
@Service
public class AuditService {
    private final EcmApiClient ecmApiClient;
    
    public Mono<List<AuditEntry>> getAuditTrail(String objectId) {
        return ecmApiClient.getAuditTrail(objectId);
    }
}

// 3. Client method
public Mono<List<AuditEntry>> getAuditTrail(String objectId) {
    return webClient.get()
        .uri("/audit/{objectId}", objectId)
        .retrieve()
        .bodyToFlux(AuditEntry.class)
        .collectList();
}

// 4. Handler
@Component
public class AuditHandler {
    private final AuditService auditService;
    
    public Map<String, Object> handleToolCall(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "audit_get_trail" -> handleGetTrail(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }
    
    private Map<String, Object> handleGetTrail(Map<String, Object> arguments) {
        String objectId = (String) arguments.get("objectId");
        List<AuditEntry> trail = auditService.getAuditTrail(objectId).block();
        return Map.of("success", true, "trail", trail);
    }
}
```

---

## Common Patterns

### Pattern 1: Optional Parameters

```java
private Map<String, Object> handleSearch(Map<String, Object> arguments) {
    String query = (String) arguments.get("query");
    Integer maxResults = arguments.containsKey("maxResults") 
        ? ((Number) arguments.get("maxResults")).intValue() 
        : 100; // default
    
    SearchResult result = searchService.search(query, maxResults).block();
    return Map.of("success", true, "result", result);
}
```

### Pattern 2: List Parameters

```java
private Map<String, Object> handleBatchDelete(Map<String, Object> arguments) {
    @SuppressWarnings("unchecked")
    List<String> documentIds = (List<String>) arguments.get("documentIds");
    
    List<String> deleted = new ArrayList<>();
    for (String id : documentIds) {
        documentService.deleteDocument(id).block();
        deleted.add(id);
    }
    
    return Map.of("success", true, "deletedIds", deleted);
}
```

### Pattern 3: Nested Objects

```java
private Map<String, Object> handleCreateWithMetadata(Map<String, Object> arguments) {
    String name = (String) arguments.get("name");
    String folderId = (String) arguments.get("folderId");
    
    @SuppressWarnings("unchecked")
    Map<String, Object> properties = (Map<String, Object>) arguments.get("properties");
    
    Document doc = new Document();
    doc.setName(name);
    doc.setFolderId(folderId);
    doc.setProperties(properties);
    
    Document created = documentService.createDocument(doc).block();
    return Map.of("success", true, "document", created);
}
```

---

## Troubleshooting

### Common Issues

1. **STDIO Communication Issues**
   - Ensure no `System.out.println()` except for JSON-RPC responses
   - Use logging framework for debug output
   - Check MCP client configuration

2. **Authentication Failures**
   - Verify ECM_USERNAME and ECM_PASSWORD environment variables
   - Check Basic Auth header encoding
   - Test with Postman/curl first

3. **Timeout Issues**
   - Increase `ecm.api.timeout` in application.yml
   - Check network connectivity to ECM backend
   - Review ECM backend logs

4. **Serialization Errors**
   - Ensure models have proper getters/setters
   - Check JSON property names match ECM API
   - Use `@JsonProperty` for name mapping if needed

---

## Best Practices

1. **Always validate input parameters** in handlers before passing to services
2. **Use Mono/Flux** throughout service and client layers for non-blocking IO
3. **Log all errors** with context (operation, parameters, error message)
4. **Return consistent response format** from all tool handlers
5. **Write unit tests** for business logic in services
6. **Write integration tests** for API client methods
7. **Use meaningful variable names** that describe the data
8. **Keep handlers thin** - delegate business logic to services
9. **Document complex algorithms** with inline comments
10. **Follow Spring Boot conventions** for configuration and component scanning

---

## Quick Reference

### Add New Tool Checklist
- [ ] Define tool schema in `handleToolsList()`
- [ ] Route tool call in `handleToolsCall()`
- [ ] Implement handler method
- [ ] Add service method if needed
- [ ] Add client API method if needed
- [ ] Create/update model classes if needed
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update this documentation
- [ ] Test end-to-end with MCP client

### Common Commands
```bash
# Build
mvn clean install

# Run tests
mvn test

# Run specific test
mvn test -Dtest=DocumentServiceTest

# Package
mvn package

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Additional Resources

- [MCP Protocol Specification](https://modelcontextprotocol.io)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

**Last Updated**: February 13, 2026  
**Version**: 1.0.0  
**Maintainer**: ECM MCP Team