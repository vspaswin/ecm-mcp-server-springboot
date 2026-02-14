# GitHub Copilot Guide - ECM MCP Server

## Overview

This guide helps GitHub Copilot understand the codebase structure and assist you in extending the ECM MCP server effectively.

## Project Context

**Purpose**: Enterprise Content Management (ECM) MCP Server that helps API consumers understand, integrate, and troubleshoot ECM REST APIs without creating support tickets.

**Technology Stack**:
- Java 21 (with preview features)
- Spring Boot 4.0
- Maven
- Model Context Protocol (MCP)
- Resilience4j (Circuit Breaker, Retry)
- Micrometer (Observability)

**Architecture Pattern**: Entity-Component-System (ECS) for extensibility

## Key Design Principles

1. **Extensibility First**: New MCP tools should be addable without modifying existing code
2. **Self-Documenting APIs**: API consumers should get instant answers to integration questions
3. **Error-Guided Development**: Error responses should include solutions, not just descriptions
4. **Enterprise-Ready**: Security, observability, and resilience built-in
5. **Test-Driven**: Every component must be testable in isolation

## Code Patterns

### Pattern 1: Creating a New MCP Tool

**When to use**: Adding functionality that AI clients can invoke

**Structure**:
```java
// Step 1: Define the tool capability
@Component
public class MyToolExecutor implements ToolExecutor {
    
    private final ApiKnowledgeBase knowledgeBase;
    private final ErrorAnalyzer errorAnalyzer;
    
    @Override
    public String getToolName() {
        return "my_tool_name";
    }
    
    @Override
    public String getDescription() {
        return "Clear description of what this tool does";
    }
    
    @Override
    public JsonNode getInputSchema() {
        return JsonSchemaBuilder.object()
            .property("param1", JsonSchemaBuilder.string()
                .description("Description of param1")
                .required(true))
            .property("param2", JsonSchemaBuilder.number()
                .description("Description of param2"))
            .build();
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // 1. Extract and validate arguments
            String param1 = (String) arguments.get("param1");
            
            // 2. Execute tool logic
            Object result = performToolLogic(param1);
            
            // 3. Return structured result
            return ToolResult.success(result);
            
        } catch (Exception e) {
            return ToolResult.error(e.getMessage());
        }
    }
}

// Step 2: Register the tool (auto-discovered via @Component)
```

### Pattern 2: Accessing API Knowledge Base

**When to use**: Need to retrieve API documentation, schemas, or examples

```java
@Service
public class MyService {
    
    private final ApiKnowledgeBase knowledgeBase;
    
    public APIDocumentation getAPIInfo(String endpoint, HttpMethod method) {
        return knowledgeBase.findEndpoint(endpoint, method)
            .orElseThrow(() -> new APINotFoundException(endpoint));
    }
    
    public List<Parameter> getMandatoryFields(String endpoint) {
        return knowledgeBase.getMandatoryParameters(endpoint);
    }
    
    public List<ErrorSolution> getCommonErrors(String endpoint) {
        return knowledgeBase.getCommonErrors(endpoint);
    }
}
```

### Pattern 3: Error Analysis

**When to use**: Analyzing API error responses to help developers

```java
@Service
public class ErrorAnalysisService {
    
    public ErrorAnalysisResult analyze(
            HttpStatus statusCode,
            String responseBody,
            Map<String, String> requestHeaders,
            String requestBody) {
        
        return ErrorAnalysisResult.builder()
            .errorCode(statusCode.value())
            .diagnosis(identifyRootCause(statusCode, responseBody))
            .likelyCauses(analyzeLikelyCauses(statusCode, requestHeaders, requestBody))
            .solution(generateSolution(statusCode, responseBody))
            .codeExample(generateFixedExample(requestBody))
            .build();
    }
    
    private String identifyRootCause(HttpStatus status, String body) {
        // Pattern matching for common error scenarios
        if (status == HttpStatus.UNAUTHORIZED) {
            return "Authentication token is missing or invalid";
        } else if (status == HttpStatus.BAD_REQUEST && body.contains("required field")) {
            return "One or more mandatory fields are missing";
        }
        // ... more patterns
    }
}
```

### Pattern 4: Request Validation

**When to use**: Validating requests before they're sent to the API

```java
@Service
public class RequestValidationService {
    
    public ValidationResult validate(
            String endpoint,
            HttpMethod method,
            Map<String, Object> requestBody,
            Map<String, String> headers) {
        
        ValidationResult result = new ValidationResult();
        
        // 1. Load API schema
        APIDocumentation api = knowledgeBase.findEndpoint(endpoint, method)
            .orElseThrow();
        
        // 2. Validate structure
        validateStructure(api.getRequestSchema(), requestBody, result);
        
        // 3. Validate mandatory fields
        validateMandatoryFields(api.getMandatoryFields(), requestBody, result);
        
        // 4. Validate data types
        validateDataTypes(api.getRequestSchema(), requestBody, result);
        
        // 5. Validate authentication
        validateAuthentication(api.getAuthRequirements(), headers, result);
        
        return result;
    }
}
```

### Pattern 5: Sample Code Generation

**When to use**: Generating working code examples in various languages

```java
@Service
public class SampleGenerationService {
    
    public CodeSample generateSample(
            String endpoint,
            HttpMethod method,
            String language) {
        
        APIDocumentation api = knowledgeBase.findEndpoint(endpoint, method)
            .orElseThrow();
        
        return switch (language.toLowerCase()) {
            case "java" -> generateJavaSample(api);
            case "python" -> generatePythonSample(api);
            case "curl" -> generateCurlSample(api);
            case "javascript" -> generateJavaScriptSample(api);
            default -> throw new UnsupportedLanguageException(language);
        };
    }
    
    private CodeSample generateJavaSample(APIDocumentation api) {
        String code = String.format("""
            WebClient client = WebClient.builder()
                .baseUrl("%s")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
            
            %s response = client.%s()
                .uri("%s")
                .bodyValue(%s)
                .retrieve()
                .bodyToMono(%s.class)
                .block();
            """,
            api.getBaseUrl(),
            api.getResponseType(),
            api.getMethod().name().toLowerCase(),
            api.getPath(),
            generateSampleRequest(api),
            api.getResponseType()
        );
        
        return CodeSample.of("java", code);
    }
}
```

## Component Responsibilities

### MCP Layer (`com.jpmc.ecm.mcp`)

**Purpose**: Implement Model Context Protocol and tool execution

**Key Classes**:
- `MCPServer`: Core MCP protocol handler
- `MCPController`: REST endpoint for MCP requests
- `ToolRegistry`: Dynamic tool discovery and registration
- `ToolExecutor`: Interface for all MCP tools

**Design Rules**:
- Tool executors must be stateless
- Each tool must have clear input/output schemas
- Tools should fail gracefully with helpful error messages
- Tool execution must be async-capable

### Client Layer (`com.jpmc.ecm.client`)

**Purpose**: Communicate with ECM backend REST APIs

**Key Classes**:
- `ECMRestClient`: HTTP client with resilience patterns
- `CircuitBreakerConfig`: Failure handling
- `RetryConfig`: Retry logic

**Design Rules**:
- All external calls must have timeouts
- Circuit breaker for fault tolerance
- Retry with exponential backoff
- Connection pooling for performance

### Knowledge Base Layer (`com.jpmc.ecm.knowledge`)

**Purpose**: Store and retrieve API documentation and schemas

**Key Classes**:
- `ApiKnowledgeBase`: Central repository of API information
- `APIDocumentation`: Model for API metadata
- `SchemaValidator`: JSON schema validation

**Design Rules**:
- Knowledge base should be loaded at startup
- Support hot-reload for updates
- Cache frequently accessed data
- Version API documentation

### DTO Layer (`com.jpmc.ecm.dto`)

**Purpose**: Data transfer between layers

**Design Rules**:
- Immutable when possible (use records)
- Include validation annotations
- Separate request/response DTOs
- Use builder pattern for complex objects

## Common Tasks

### Task: Add a New API Endpoint to Knowledge Base

1. Open `src/main/resources/api-knowledge-base.yml`
2. Add endpoint definition:
```yaml
- path: /api/v1/new-endpoint
  method: POST
  description: Clear description
  mandatoryFields:
    - name: fieldName
      type: string
      description: Field purpose
      example: "example value"
  authentication:
    type: OAuth2
    scopes: [scope:name]
  commonErrors:
    - code: 400
      reason: Missing field
      solution: Include all mandatory fields
```

### Task: Add a New MCP Tool

1. Create executor class in `com.jpmc.ecm.mcp.tools`
2. Implement `ToolExecutor` interface
3. Annotate with `@Component`
4. Define tool name, description, schema
5. Implement execute() method
6. Write unit tests
7. Update API documentation

### Task: Add Error Pattern

1. Open `ErrorAnalysisService`
2. Add pattern matching in `identifyRootCause()`
3. Add solution in `generateSolution()`
4. Add test case
5. Document in knowledge base

### Task: Add Code Generation Language

1. Open `SampleGenerationService`
2. Add case in language switch
3. Implement `generate{Language}Sample()` method
4. Add code template
5. Write test with expected output

## Testing Guidelines

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class MyToolExecutorTest {
    
    @Mock
    private ApiKnowledgeBase knowledgeBase;
    
    @InjectMocks
    private MyToolExecutor executor;
    
    @Test
    void shouldExecuteSuccessfully() {
        // Given
        Map<String, Object> args = Map.of("param1", "value1");
        
        // When
        ToolResult result = executor.execute(args);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class MCPIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldExecuteToolViaMCP() throws Exception {
        mockMvc.perform(post("/mcp/tools/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mcpRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").exists());
    }
}
```

## Configuration

### Application Properties
```yaml
# src/main/resources/application.yml
ecm:
  api:
    base-url: ${ECM_API_BASE_URL}
    timeout: 30s
  mcp:
    enabled: true
    tools-package: com.jpmc.ecm.mcp.tools
  
resilience4j:
  circuitbreaker:
    instances:
      ecmApi:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

## Common Pitfalls

❌ **Don't**: Create tools with side effects (modifications)
✅ **Do**: Keep tools read-only and idempotent

❌ **Don't**: Return raw exceptions to MCP clients
✅ **Do**: Convert to user-friendly error messages

❌ **Don't**: Hard-code API documentation
✅ **Do**: Load from external configuration

❌ **Don't**: Block threads in tool execution
✅ **Do**: Use reactive/async patterns

## IDE Integration

### IntelliJ IDEA Settings
- Enable annotation processing (Lombok)
- Set Java language level to 21
- Enable preview features
- Install Spring Boot plugin

### VS Code Settings
```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/path/to/jdk-21"
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic"
}
```

## Building & Running

```bash
# Build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Build Docker image
docker build -t ecm-mcp-server .

# Run with Docker Compose
docker-compose up
```

## Debugging Tips

1. **Enable debug logging**: `logging.level.com.jpmc.ecm=DEBUG`
2. **Use Actuator**: `/actuator/health`, `/actuator/metrics`
3. **Check MCP requests**: Log incoming MCP messages
4. **Validate schemas**: Use JSON schema validator
5. **Test tools independently**: Unit test each executor

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [Resilience4j Guide](https://resilience4j.readme.io/docs/getting-started)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)

## Questions?

For questions or issues:
1. Check ARCHITECTURE.md for design patterns
2. Review API_KNOWLEDGE_BASE.md for API details
3. See DEVELOPER_GUIDE.md for quick reference
4. Create an issue in the repository
