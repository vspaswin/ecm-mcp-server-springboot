# Extensible ECS Architecture for MCP Server

## Overview

This MCP server implements an **Entity-Component-System (ECS)** architecture pattern, making it highly extensible and maintainable for enterprise use at JPMorgan Chase.

## Why ECS Architecture?

The ECS pattern separates **data** (Components), **logic** (Systems), and **identity** (Entities), providing:

- ✅ **Extensibility** - Add new MCP tools without modifying existing code
- ✅ **Maintainability** - Clear separation of concerns
- ✅ **Testability** - Each component can be tested independently
- ✅ **Scalability** - Systems can be parallelized and optimized
- ✅ **Reusability** - Components can be shared across different entities

## Architecture Layers

### 1. Entity Layer (Identity)

**Entities** represent distinct concepts in the system:

```
├── MCPTool (Abstract Entity)
│   ├── APIDocumentationTool
│   ├── RequestValidatorTool
│   ├── ErrorAnalyzerTool
│   ├── SampleGeneratorTool
│   └── IntegrationGuideTool
│
├── APIEndpoint (Data Entity)
│   ├── DocumentManagementAPI
│   ├── SearchAPI
│   ├── WorkflowAPI
│   └── MetadataAPI
│
└── ErrorContext (Analysis Entity)
    ├── HTTPError
    ├── ValidationError
    └── AuthenticationError
```

### 2. Component Layer (Data)

**Components** hold pure data without logic:

```java
// Example: API Endpoint Component
@Component
public class APIEndpointComponent {
    private String path;
    private HttpMethod method;
    private List<ParameterSchema> parameters;
    private RequestSchema requestSchema;
    private ResponseSchema responseSchema;
    private List<ErrorCode> possibleErrors;
    private List<String> requiredPermissions;
}

// Example: Tool Capability Component
@Component
public class ToolCapabilityComponent {
    private String toolName;
    private String description;
    private JsonSchema inputSchema;
    private ToolExecutor executor;
    private List<String> tags;
}
```

### 3. System Layer (Logic)

**Systems** process entities with specific components:

```java
// Example: API Documentation System
@System
public class APIDocumentationSystem {
    public void execute(Entity entity, APIEndpointComponent api) {
        // Process API documentation
    }
}

// Example: Error Analysis System
@System
public class ErrorAnalysisSystem {
    public void execute(Entity entity, 
                       ErrorContext error, 
                       APIEndpointComponent api) {
        // Analyze error and suggest fixes
    }
}
```

## Package Structure

```
com.jpmc.ecm/
├── mcp/
│   ├── core/              # Core MCP protocol implementation
│   │   ├── MCPServer.java
│   │   ├── MCPTransport.java
│   │   └── MCPMessageHandler.java
│   │
│   ├── entity/            # Entity definitions
│   │   ├── MCPTool.java
│   │   ├── APIEndpoint.java
│   │   └── ErrorContext.java
│   │
│   ├── component/         # Component (data) definitions
│   │   ├── APIEndpointComponent.java
│   │   ├── ToolCapabilityComponent.java
│   │   ├── SchemaComponent.java
│   │   └── ErrorAnalysisComponent.java
│   │
│   ├── system/            # System (logic) implementations
│   │   ├── APIDocumentationSystem.java
│   │   ├── RequestValidationSystem.java
│   │   ├── ErrorAnalysisSystem.java
│   │   ├── SampleGenerationSystem.java
│   │   └── IntegrationGuideSystem.java
│   │
│   ├── registry/          # Dynamic tool registry
│   │   ├── ToolRegistry.java
│   │   ├── ToolRegistrar.java
│   │   └── ToolDiscovery.java
│   │
│   └── tools/             # Concrete MCP tool implementations
│       ├── ListAPIsToolExecutor.java
│       ├── GetAPIDocToolExecutor.java
│       ├── ValidateRequestToolExecutor.java
│       ├── AnalyzeErrorToolExecutor.java
│       ├── GenerateSampleToolExecutor.java
│       └── IntegrationGuideToolExecutor.java
│
├── client/                # ECM REST API client
│   └── ECMRestClient.java
│
├── config/                # Configuration
│   ├── MCPServerConfig.java
│   ├── ToolRegistryConfig.java
│   └── SecurityConfig.java
│
├── controller/            # REST controllers
│   └── MCPController.java
│
├── dto/                   # Data transfer objects
│   ├── mcp/
│   │   ├── MCPRequest.java
│   │   ├── MCPResponse.java
│   │   └── ToolResult.java
│   └── api/
│       ├── APIDocumentation.java
│       ├── ValidationResult.java
│       └── ErrorAnalysis.java
│
└── exception/             # Exception handling
    ├── MCPException.java
    ├── ToolExecutionException.java
    └── ValidationException.java
```

## How to Add New MCP Tools

### Step 1: Define Tool Entity

```java
public class MyNewTool implements MCPTool {
    private final ToolCapabilityComponent capability;
    
    public MyNewTool() {
        this.capability = ToolCapabilityComponent.builder()
            .toolName("my_new_tool")
            .description("Description of what this tool does")
            .inputSchema(createInputSchema())
            .executor(new MyNewToolExecutor())
            .build();
    }
}
```

### Step 2: Create Tool Executor

```java
@Component
public class MyNewToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        // Implement tool logic here
        return ToolResult.success(result);
    }
}
```

### Step 3: Register Tool

```java
@Configuration
public class ToolRegistryConfig {
    
    @Bean
    public ToolRegistrar toolRegistrar(ToolRegistry registry) {
        return new ToolRegistrar(registry)
            .register(new MyNewTool())
            .register(new AnotherTool());
    }
}
```

That's it! The tool is now available to all MCP clients.

## API Knowledge Base Integration

The system maintains a comprehensive knowledge base of all ECM APIs:

```yaml
apiEndpoints:
  - path: /api/v1/documents
    method: POST
    description: Upload a new document
    mandatoryFields:
      - name: file
        type: binary
        description: Document file to upload
      - name: metadata
        type: object
        description: Document metadata
        properties:
          documentType:
            type: string
            required: true
            enum: [CONTRACT, INVOICE, REPORT]
          classification:
            type: string
            required: true
            enum: [PUBLIC, CONFIDENTIAL, RESTRICTED]
    authentication:
      type: OAuth2
      scopes: [document:write]
    commonErrors:
      - code: 401
        reason: Invalid or expired token
        solution: Refresh your OAuth2 token
      - code: 400
        reason: Missing required field
        solution: Ensure all mandatory fields are provided
```

## Error Analysis Flow

When a developer encounters an error:

```
Developer calls API → Gets Error Response
    ↓
MCP Tool: analyze_error_response
    ↓
ErrorAnalysisSystem processes:
    1. Parse error response
    2. Match against known error patterns
    3. Identify missing/invalid fields
    4. Check authentication/authorization
    5. Analyze request payload
    ↓
Return detailed analysis:
    - What went wrong
    - Why it happened
    - How to fix it
    - Code examples
```

## Request Validation Flow

Before calling API, developers can validate:

```
Developer prepares request → MCP Tool: validate_request
    ↓
RequestValidationSystem:
    1. Load API schema from knowledge base
    2. Validate request structure
    3. Check mandatory fields
    4. Validate data types
    5. Check authentication headers
    6. Validate business rules
    ↓
Return validation result:
    ✅ Valid - proceed with confidence
    ❌ Invalid - get specific error messages
```

## Benefits for API Consumers

### Before MCP Server:
```
Developer → Tries API → Gets Error → Creates Support Ticket → Waits → Gets Response
Time: Hours to Days
```

### With MCP Server:
```
Developer → Asks MCP Tool → Gets Instant Answer → Fixes Issue → Success
Time: Seconds to Minutes
```

### Concrete Examples:

**Q: "Why am I getting 401 Unauthorized?"**
```json
{
  "error_code": 401,
  "diagnosis": "Authentication token is missing or invalid",
  "likely_causes": [
    "Token has expired (tokens valid for 1 hour)",
    "Token not included in Authorization header",
    "Invalid token format"
  ],
  "solution": {
    "steps": [
      "1. Request new token from /auth/token endpoint",
      "2. Include token in header: Authorization: Bearer {token}",
      "3. Ensure token hasn't expired"
    ],
    "code_example": "curl -H 'Authorization: Bearer YOUR_TOKEN' ..."
  }
}
```

**Q: "What are the mandatory fields for document upload?"**
```json
{
  "endpoint": "/api/v1/documents",
  "method": "POST",
  "mandatory_fields": [
    {
      "name": "file",
      "type": "binary",
      "description": "Document file (PDF, DOCX, etc.)",
      "max_size": "10MB"
    },
    {
      "name": "metadata.documentType",
      "type": "string",
      "description": "Type of document",
      "allowed_values": ["CONTRACT", "INVOICE", "REPORT"],
      "example": "CONTRACT"
    },
    {
      "name": "metadata.classification",
      "type": "string",
      "description": "Security classification",
      "allowed_values": ["PUBLIC", "CONFIDENTIAL", "RESTRICTED"],
      "example": "CONFIDENTIAL"
    }
  ],
  "complete_example": {
    "curl": "curl -X POST ... -F file=@doc.pdf -F metadata=...",
    "java": "MultipartBodyBuilder builder = ...",
    "python": "files = {'file': open('doc.pdf', 'rb')}"
  }
}
```

## Performance Considerations

- **Caching**: API schemas cached in memory for fast lookups
- **Async Processing**: Tool execution happens asynchronously
- **Connection Pooling**: Reuse HTTP connections to ECM backend
- **Circuit Breaker**: Resilience4j protects against cascading failures
- **Metrics**: Micrometer tracks tool execution times

## Security

- **Authentication**: OAuth2 token validation
- **Authorization**: Role-based access control
- **Rate Limiting**: Prevent abuse
- **Audit Logging**: Track all MCP tool invocations
- **Secret Management**: Externalized configuration

## Testing Strategy

```
├── Unit Tests
│   ├── Component Tests (data validation)
│   ├── System Tests (logic verification)
│   └── Tool Executor Tests
│
├── Integration Tests
│   ├── MCP Protocol Tests
│   ├── Tool Registry Tests
│   └── End-to-End Tool Execution
│
└── Contract Tests
    └── ECM API Client Tests
```

## Monitoring & Observability

- **Metrics**: Tool execution count, success rate, latency
- **Tracing**: Distributed tracing with Micrometer
- **Logging**: Structured logging with context
- **Health Checks**: /actuator/health endpoint
- **Prometheus**: Metrics export for monitoring

## Future Extensibility

Easy to add:
- ✅ New ECM API endpoints
- ✅ Additional MCP tools
- ✅ Support for more error types
- ✅ Multi-language code generation
- ✅ Interactive tutorials
- ✅ API versioning support
- ✅ Mock API responses for testing

## References

- [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/)
- [ECS Architecture Pattern](https://madrona-engine.github.io/)
- [Spring Boot Best Practices](https://spring.io/guides)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
