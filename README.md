# ECM MCP Server - Spring Boot

A production-ready **Model Context Protocol (MCP) server** built with **Spring Boot 3.4.2** and **Java 21** for seamless integration with Enterprise Content Management (ECM) REST APIs. This server enables AI assistants like Claude Desktop to interact with ECM systems through a clean, extensible architecture.

> 🚀 **Ready to build your own MCP server at JPMorgan Chase?** This repository is designed to be **extensible and reusable**. See [COPILOT.md](COPILOT.md) for comprehensive documentation that GitHub Copilot can use to help you build features.

---

## 📚 Documentation

- **[COPILOT.md](COPILOT.md)** - Comprehensive guide for GitHub Copilot and developers
  - Architecture overview with diagrams
  - Complete component documentation
  - Step-by-step guide to adding new tools
  - Code patterns and examples
  - Testing strategies
  - Best practices

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed system design documentation
  - High-level architecture diagrams
  - Data flow visualizations
  - Component interactions
  - Design patterns
  - Technology stack details
  - Future enhancements

---

## ⚡ Quick Start

### Prerequisites
- **Java 21** ([Download OpenJDK 21](https://adoptium.net/temurin/releases/?version=21))
- **Maven 3.9+** ([Download Maven](https://maven.apache.org/download.cgi))
- Access to ECM REST API

### Build & Run

```bash
# 1. Clone the repository
git clone https://github.com/vspaswin/ecm-mcp-server-springboot.git
cd ecm-mcp-server-springboot

# 2. Build (Java 21 required)
mvn clean install

# 3. Run with environment variables
ECM_BASE_URL=http://localhost:8081/api \
ECM_USERNAME=admin \
ECM_PASSWORD=admin \
java -jar target/ecm-mcp-server-1.0.0.jar
```

**✅ Build Status**: Recent compilation errors fixed. If you encounter issues, pull latest from main.

---

## 🏗️ Architecture Overview

```
┌──────────────────────────────────────────┐
│   AI Assistant (Claude Desktop, etc.)    │
│  - Natural language understanding        │
│  - Tool selection & execution            │
└─────────────────┬────────────────────────┘
                  │ MCP Protocol (JSON-RPC over STDIO)
┌─────────────────▼────────────────────────┐
│      ECM MCP Server (This Application)   │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │   McpServerApplication             │ │
│  │   - JSON-RPC handler               │ │
│  │   - Tool routing                   │ │
│  └──────────────┬─────────────────────┘ │
│                 │                        │
│  ┌──────────────▼─────────────────────┐ │
│  │        Handler Layer               │ │
│  │  DocumentHandler | FolderHandler   │ │
│  │  SearchHandler | SecurityHandler   │ │
│  └──────────────┬─────────────────────┘ │
│                 │                        │
│  ┌──────────────▼─────────────────────┐ │
│  │        Service Layer               │ │
│  │  DocumentService | SearchService   │ │
│  └──────────────┬─────────────────────┘ │
│                 │                        │
│  ┌──────────────▼─────────────────────┐ │
│  │        EcmApiClient                │ │
│  │  (WebClient - reactive HTTP)       │ │
│  └──────────────┬─────────────────────┘ │
└─────────────────┼────────────────────────┘
                  │ REST API (HTTP)
┌─────────────────▼────────────────────────┐
│       ECM Backend System                 │
│  - Document storage & management         │
│  - Search & workflow                     │
└──────────────────────────────────────────┘
```

**Key Features**:
- ✅ **Layered architecture** - Handler → Service → Client separation
- ✅ **Reactive programming** - Non-blocking I/O with Spring WebFlux
- ✅ **Extensible design** - Easy to add new tools and ECM backends
- ✅ **Production-ready** - Error handling, validation, logging

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed diagrams and design patterns.

---

## 🛠️ Available MCP Tools

### Document Management
- `document_get` - Retrieve document by ID
- `document_create` - Create new document
- `document_update` - Update document metadata
- `document_delete` - Delete document
- `document_get_content` - Get document binary content
- `document_get_versions` - List all versions
- `document_checkout` - Check out for editing
- `document_checkin` - Check in after editing
- `document_cancel_checkout` - Cancel checkout

### Folder Management
- `folder_get` - Get folder information
- `folder_create` - Create new folder
- `folder_update` - Update folder metadata
- `folder_delete` - Delete folder
- `folder_list_children` - List folder contents
- `folder_move` - Move folder to new parent

### Search
- `search_documents` - Search documents with query
- `search_advanced` - Advanced search with filters

### Security
- `security_get_permissions` - Get object permissions
- `security_set_permissions` - Set permissions
- `security_add_user` - Add user permission
- `security_remove_user` - Remove user permission

### Workflow
- `workflow_start` - Start workflow on object
- `workflow_get_status` - Get workflow status
- `workflow_approve` - Approve workflow task
- `workflow_reject` - Reject workflow task

### Metadata
- `metadata_get_schema` - Get object type schema
- `metadata_validate` - Validate metadata values

---

## 🔌 Integration with Claude Desktop

**macOS**: Edit `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows**: Edit `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "ecm": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/ecm-mcp-server-1.0.0.jar"
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

**Restart Claude Desktop** and look for the 🔨 tools icon to see available ECM tools.

---

## 📦 Project Structure

```
ecm-mcp-server-springboot/
├── src/main/java/com/jpmc/ecm/
│   ├── mcp/
│   │   └── McpServerApplication.java    # Main entry point
│   ├── handler/                         # MCP tool handlers
│   │   ├── DocumentHandler.java
│   │   ├── FolderHandler.java
│   │   ├── SearchHandler.java
│   │   ├── SecurityHandler.java
│   │   ├── WorkflowHandler.java
│   │   └── MetadataHandler.java
│   ├── service/                         # Business logic
│   │   ├── DocumentService.java
│   │   ├── FolderService.java
│   │   ├── SearchService.java
│   │   ├── SecurityService.java
│   │   ├── WorkflowService.java
│   │   └── MetadataService.java
│   ├── client/                          # ECM API client
│   │   └── EcmApiClient.java
│   ├── config/                          # Configuration
│   │   └── WebClientConfig.java
│   └── model/                           # Domain models
│       ├── Document.java
│       ├── Folder.java
│       ├── SearchResult.java
│       ├── Permissions.java
│       └── WorkflowInstance.java
├── src/main/resources/
│   └── application.yml                  # Configuration
├── COPILOT.md                          # Comprehensive dev guide
├── ARCHITECTURE.md                     # System design docs
├── README.md                           # This file
└── pom.xml                             # Maven dependencies
```

---

## 🚀 Extending the Server

### Adding a New Tool (Quick Example)

See [COPILOT.md](COPILOT.md) for detailed step-by-step instructions. Here's the overview:

**1. Add Handler Method**
```java
// In DocumentHandler.java
private Map<String, Object> handleCopy(Map<String, Object> arguments) {
    String sourceId = (String) arguments.get("sourceId");
    String targetFolder = (String) arguments.get("targetFolder");
    
    Document copied = documentService.copyDocument(sourceId, targetFolder).block();
    return Map.of("success", true, "document", copied);
}
```

**2. Add Service Method**
```java
// In DocumentService.java
public Mono<Document> copyDocument(String sourceId, String targetFolder) {
    return ecmApiClient.copyDocument(sourceId, targetFolder);
}
```

**3. Add Client Method**
```java
// In EcmApiClient.java
public Mono<Document> copyDocument(String sourceId, String targetFolder) {
    return webClient.post()
        .uri("/documents/copy")
        .bodyValue(Map.of("sourceId", sourceId, "targetFolder", targetFolder))
        .retrieve()
        .bodyToMono(Document.class);
}
```

**4. Register Tool** in `McpServerApplication.handleToolsList()`

Full examples with error handling, validation, and testing in [COPILOT.md](COPILOT.md).

---

## 🧪 Testing

### Run Tests
```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=DocumentServiceTest

# With coverage
mvn verify
```

### Test Examples

**Unit Test** (with mocked dependencies):
```java
@SpringBootTest
class DocumentServiceTest {
    @MockBean
    private EcmApiClient ecmApiClient;
    
    @Autowired
    private DocumentService documentService;
    
    @Test
    void testGetDocument() {
        when(ecmApiClient.getDocument("doc123"))
            .thenReturn(Mono.just(expectedDoc));
            
        Document result = documentService.getDocument("doc123").block();
        assertEquals("doc123", result.getId());
    }
}
```

**Integration Test** (with MockWebServer):
```java
class EcmApiClientTest {
    private MockWebServer mockWebServer;
    private EcmApiClient client;
    
    @Test
    void testGetDocument() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"id\":\"doc123\"}")
            .setResponseCode(200));
            
        Document doc = client.getDocument("doc123").block();
        assertEquals("doc123", doc.getId());
    }
}
```

See [COPILOT.md - Testing Section](COPILOT.md#testing) for comprehensive testing strategies.

---

## ⚙️ Configuration

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

logging:
  level:
    com.jpmc.ecm: DEBUG
```

### Environment Variables

- `ECM_BASE_URL` - ECM API endpoint
- `ECM_USERNAME` - API username
- `ECM_PASSWORD` - API password

---

## 🔐 Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for sensitive data
3. **Secure secrets** with Vault or AWS Secrets Manager in production
4. **HTTPS only** for ECM API connections
5. **Validate SSL certificates** in production
6. **Rotate credentials** regularly
7. **Use least-privilege** API accounts

---

## 📊 Technology Stack

- **Spring Boot 3.4.2** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Project Reactor** - Reactive programming
- **Java 21** - Modern Java features
- **Maven 3.9+** - Build tool
- **Jackson** - JSON processing
- **SLF4J + Logback** - Logging
- **JUnit 5 + Mockito** - Testing
- **OkHttp MockWebServer** - Integration testing

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Make changes
4. Write tests (see [COPILOT.md](COPILOT.md) for testing patterns)
5. Build and test (`mvn clean verify`)
6. Commit (`git commit -m 'Add amazing feature'`)
7. Push (`git push origin feature/amazing-feature`)
8. Open Pull Request

---

## 🐛 Troubleshooting

### Build Errors

**Error**: `invalid source release 21 with --enable-preview`

**Solution**:
```bash
git pull origin main  # Get latest pom.xml
mvn clean install
```

### Connection Errors

**Error**: Connection refused to ECM API

**Solution**:
1. Verify `ECM_BASE_URL` is correct
2. Check network connectivity
3. Verify ECM API is running
4. Check firewall/proxy settings

### Authentication Errors

**Error**: 401 Unauthorized

**Solution**:
1. Verify `ECM_USERNAME` and `ECM_PASSWORD`
2. Check account is not locked
3. Verify credentials have API access
4. Test with curl/Postman first

---

## 📖 Additional Resources

- **[Model Context Protocol Spec](https://modelcontextprotocol.io)** - MCP protocol details
- **[Spring WebFlux Guide](https://docs.spring.io/spring-framework/reference/web/webflux.html)** - Reactive programming
- **[Project Reactor Docs](https://projectreactor.io/docs)** - Reactive types (Mono/Flux)
- **[Java 21 Features](https://openjdk.org/projects/jdk/21/)** - Modern Java capabilities

---

## 🗺️ Roadmap

- [x] Core MCP server implementation
- [x] Document management tools
- [x] Search functionality
- [x] Workflow operations
- [x] Security/permissions management
- [x] Comprehensive documentation (COPILOT.md, ARCHITECTURE.md)
- [ ] OAuth 2.0 authentication support
- [ ] Batch operations
- [ ] Caching layer (Redis)
- [ ] Circuit breaker pattern (Resilience4j)
- [ ] Rate limiting
- [ ] Event streaming (WebSocket)
- [ ] Multi-tenancy support

---

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

---

## 🙏 Acknowledgments

- [Model Context Protocol](https://modelcontextprotocol.io/) for the MCP specification
- [Spring Boot](https://spring.io/projects/spring-boot) for the application framework
- [Project Reactor](https://projectreactor.io/) for reactive programming support

---

## 📞 Support

- **Documentation**: [COPILOT.md](COPILOT.md) | [ARCHITECTURE.md](ARCHITECTURE.md)
- **Issues**: [GitHub Issues](https://github.com/vspaswin/ecm-mcp-server-springboot/issues)
- **Questions**: Open a discussion in GitHub

---

**Built with ❤️ for JPMorgan Chase and Enterprise Applications**

**Ready to extend this server?** Start with [COPILOT.md](COPILOT.md) - it has everything GitHub Copilot needs to help you build new features!