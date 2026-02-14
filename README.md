# ECM MCP Server - Spring Boot 4

A production-ready **Model Context Protocol (MCP) server** built with **Spring Boot 4** and **Java 21** for seamless integration with Enterprise Content Management (ECM) REST APIs. This server enables AI assistants and MCP clients to manage documents, search content, organize folders, handle metadata, control versions, and manage workflows within enterprise content management systems.

> 🚀 **Ready to build your own MCP server?** This repository is designed to be **extensible and reusable** for JPMorgan Chase and other enterprises. See [docs/COPILOT_GUIDE.md](docs/COPILOT_GUIDE.md) for detailed instructions.

## 📚 Documentation

- **[Quick Start Guide](QUICKSTART.md)** - Get up and running in minutes
- **[Architecture Documentation](docs/ARCHITECTURE.md)** - System design and patterns
- **[Copilot Guide](docs/COPILOT_GUIDE.md)** - Comprehensive guide for extending the server

## ⚡ Quick Start

```bash
# 1. Pull latest changes
git pull origin main

# 2. Build the project (Java 21 required)
mvn clean install

# 3. Run the server
java -jar target/ecm-mcp-server-1.0.0.jar

# 4. Test the server
curl http://localhost:8080/actuator/health
```

**Note**: The recent compilation error with `--enable-preview` has been fixed. If you encounter build issues, pull the latest changes from main branch.

## 🚀 Features

### Document Management
- Get document information by ID
- Delete documents
- Upload documents (multipart support)
- Download document content

### Search Capabilities
- Full-text search across documents
- Advanced search with filters:
  - Document type filtering
  - Date range queries
  - Folder-scoped search
  - Tag-based filtering
  - Custom metadata search

### Folder Organization
- Create hierarchical folder structures
- List folder contents (documents and subfolders)
- Move documents between folders
- Get folder tree visualization
- Delete folders (recursive support)

### Metadata Management
- Retrieve document metadata
- Update metadata fields
- View metadata schemas by document type

### Version Control
- View complete version history
- Create new versions (major/minor)
- Restore previous versions
- Version comments and tracking

### Workflow Management
- Start workflows on documents
- Monitor workflow status
- Approve workflow steps
- Reject workflows with reasons

## 🏗️ Architecture

### Technology Stack
- **Spring Boot 4.0.0** - Latest Spring Boot framework
- **Java 21** - Modern Java with latest features
- **Spring WebFlux** - Reactive programming for non-blocking I/O
- **Resilience4j** - Circuit breaker, retry, and rate limiting
- **Micrometer** - Observability with Prometheus integration
- **Lombok** - Reduce boilerplate code

### Extensible Design

This server is built with extensibility in mind:

✅ **Easy to add new MCP tools** - Simple annotation-based registration
✅ **Pluggable ECM backends** - Support multiple ECM systems (FileNet, SharePoint, Documentum)
✅ **Configuration-driven** - Environment-specific settings
✅ **Enterprise-ready** - Built-in resilience, monitoring, and security
✅ **Well-documented** - Comprehensive guides for extending functionality

See [Architecture Documentation](docs/ARCHITECTURE.md) for details.

### Project Structure

```
ecm-mcp-server-springboot/
├── docs/
│   ├── ARCHITECTURE.md           # System design and patterns
│   └── COPILOT_GUIDE.md          # Comprehensive extension guide
├── src/main/java/com/jpmc/ecm/
│   ├── EcmMcpServerApplication.java    # Main application class
│   ├── client/                         # ECM backend clients
│   │   ├── EcmClient.java              # Common interface
│   │   ├── FileNetClient.java          # FileNet implementation
│   │   └── SharePointClient.java       # SharePoint implementation
│   ├── config/                         # Configuration classes
│   │   ├── EcmProperties.java          # ECM configuration
│   │   ├── WebClientConfig.java        # HTTP client setup
│   │   └── ResilienceConfig.java       # Resilience patterns
│   ├── controller/                     # REST controllers
│   │   └── McpController.java          # MCP HTTP endpoints
│   ├── service/                        # Business services
│   │   ├── DocumentService.java        # Document operations
│   │   ├── SearchService.java          # Search operations
│   │   ├── WorkflowService.java        # Workflow operations
│   │   └── SecurityService.java        # Security operations
│   ├── dto/                            # Data Transfer Objects
│   ├── exception/                      # Exception handling
│   └── mcp/                            # MCP Protocol layer
│       ├── model/                      # MCP models
│       │   ├── McpRequest.java
│       │   ├── McpResponse.java
│       │   ├── McpTool.java
│       │   └── McpToolParameter.java
│       ├── service/                    # MCP services
│       │   ├── McpToolRegistry.java    # Tool registration
│       │   └── McpToolExecutor.java    # Tool execution
│       └── handler/                    # Protocol handlers
│           └── McpProtocolHandler.java
├── src/main/resources/
│   ├── application.yml                 # Main configuration
│   ├── application-dev.yml             # Development profile
│   └── application-prod.yml            # Production profile
├── QUICKSTART.md                       # Quick start guide
└── pom.xml                             # Maven dependencies
```

## 📦 Installation

### Prerequisites
- **Java 21** or higher ([Download](https://adoptium.net/temurin/releases/?version=21))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- Access to ECM REST API
- API credentials

### Build from Source

```bash
# Clone the repository
git clone https://github.com/vspaswin/ecm-mcp-server-springboot.git
cd ecm-mcp-server-springboot

# Verify Java version
java -version  # Should show Java 21

# Build with Maven
mvn clean install

# Run the application
java -jar target/ecm-mcp-server-1.0.0.jar
```

**Troubleshooting Build Issues**:

If you see `"invalid source release 21 with --enable-preview"` error:
```bash
git pull origin main  # Get the fixed pom.xml
mvn clean
mvn install
```

See [QUICKSTART.md](QUICKSTART.md) for detailed build instructions and troubleshooting.

### Configuration

Create `.env` file from example:

```bash
cp .env.example .env
```

Edit `.env` with your settings:

```bash
# FileNet Configuration
FILENET_BASE_URL=http://your-filenet-server:9080/fncmis
FILENET_USERNAME=admin
FILENET_PASSWORD=your-password

# Server Configuration
SERVER_PORT=8080
```

Or use environment variables directly:

```bash
export FILENET_BASE_URL=https://your-filenet-server:9080/fncmis
export FILENET_USERNAME=your_username
export FILENET_PASSWORD=your_password
```

## 🚀 Usage

### Running the Server

**Development mode:**
```bash
mvn spring-boot:run
```

**Production mode:**
```bash
java -jar target/ecm-mcp-server-1.0.0.jar
```

**With custom configuration:**
```bash
java -jar target/ecm-mcp-server-1.0.0.jar \
  --ecm.filenet.base-url=https://filenet.example.com \
  --ecm.filenet.username=myuser \
  --ecm.filenet.password=mypass
```

### Integration with Claude Desktop

**macOS**: Edit `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows**: Edit `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "ecm-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/ecm-mcp-server-springboot/target/ecm-mcp-server-1.0.0.jar"
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

Restart Claude Desktop and look for the hammer icon (🔨) to see available tools.

### Integration with VSCode Copilot

Create `.mcp.json` in your workspace or `%USERPROFILE%` (Windows) / `~/` (macOS/Linux):

```json
{
  "servers": {
    "ecm-mcp-server": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/target/ecm-mcp-server-1.0.0.jar"],
      "env": {
        "FILENET_BASE_URL": "http://your-filenet-server:9080/fncmis"
      }
    }
  }
}
```

### Available MCP Tools

#### Documents
- `ecm_get_document` - Get document information by ID
- `ecm_create_document` - Create new document
- `ecm_update_document` - Update document
- `ecm_delete_document` - Delete document

#### Search
- `ecm_search_documents` - Search documents with query

#### Metadata
- `ecm_get_metadata` - Get document metadata
- `ecm_update_metadata` - Update metadata fields

#### Workflows (Extensible)
- `workflow_start` - Start workflow instance
- `workflow_get_status` - Get workflow status

#### Security (Extensible)
- `security_check_permission` - Check user permissions
- `security_grant_permission` - Grant permissions

### Testing the Server

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

**List MCP Tools:**
```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'
```

**Call a Tool:**
```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "ecm_get_document",
      "arguments": {"documentId": "DOC-123"}
    }
  }'
```

## 🔧 Extending the Server

### Adding New MCP Tools

1. Create a service class
2. Use `@PostConstruct` to register tools
3. Define tool schema with parameters
4. Implement executor logic

**Example:**

```java
@Service
@RequiredArgsConstructor
public class CustomService {
    private final McpToolRegistry registry;
    
    @PostConstruct
    public void registerTools() {
        McpTool tool = McpTool.builder()
            .name("custom_tool")
            .description("My custom tool")
            .inputSchema(Map.of(
                "param1", McpToolParameter.builder()
                    .type("string")
                    .description("Parameter description")
                    .required(true)
                    .build()
            ))
            .build();
            
        registry.registerTool(tool, params -> {
            // Your logic here
            return result;
        });
    }
}
```

See [docs/COPILOT_GUIDE.md](docs/COPILOT_GUIDE.md) for comprehensive examples and patterns.

### Adding New ECM Backends

1. Implement `EcmClient` interface
2. Add configuration properties
3. Configure WebClient
4. Add authentication logic

**Example:**

```java
@Component
public class DocumentumClient implements EcmClient {
    @Override
    public Mono<Map<String, Object>> getDocument(String id) {
        // Implement Documentum-specific logic
        return webClient.get()
            .uri(baseUrl + "/documents/{id}", id)
            .retrieve()
            .bodyToMono(Map.class);
    }
}
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture patterns.

## 📊 Monitoring

### Actuator Endpoints

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Info**: `http://localhost:8080/actuator/info`

### Prometheus Metrics

Metrics include:
- HTTP request metrics
- ECM API call metrics
- Circuit breaker statistics
- Retry statistics
- JVM metrics (heap, threads, GC)
- Custom MCP tool metrics

### Health Checks

Comprehensive health checks:
- Application status
- ECM API connectivity
- Circuit breaker status
- Disk space
- Custom health indicators

## 🧪 Testing

**Run all tests:**
```bash
mvn test
```

**Run specific test:**
```bash
mvn test -Dtest=DocumentServiceTest
```

**Run integration tests:**
```bash
mvn verify
```

**Skip tests (faster builds):**
```bash
mvn clean install -DskipTests
```

## 🐳 Docker Deployment

**Build Docker image:**
```bash
docker build -t ecm-mcp-server:1.0.0 .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e FILENET_BASE_URL=https://filenet.example.com:9080/fncmis \
  -e FILENET_USERNAME=admin \
  -e FILENET_PASSWORD=password \
  --name ecm-mcp-server \
  ecm-mcp-server:1.0.0
```

**Using Docker Compose:**
```bash
docker-compose up -d
```

## 🔐 Security

### Best Practices

1. **Secure Credentials**
   - Use environment variables
   - Never commit credentials
   - Use secrets management (Vault, AWS Secrets Manager)

2. **HTTPS Only**
   - Always use HTTPS for ECM APIs
   - Validate SSL certificates
   - TLS 1.2 or higher

3. **Authentication**
   - Prefer API key over basic auth
   - Rotate credentials regularly
   - Implement rate limiting

4. **Network Security**
   - Run behind firewall/VPN
   - Limit access to trusted networks
   - Use Spring Security for endpoints

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write tests
5. Build and test (`mvn clean verify`)
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Model Context Protocol](https://modelcontextprotocol.io/) - MCP specification
- [Resilience4j](https://resilience4j.readme.io/) - Resilience patterns
- [Project Reactor](https://projectreactor.io/) - Reactive programming
- [Micrometer](https://micrometer.io/) - Metrics and observability

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/vspaswin/ecm-mcp-server-springboot/issues)
- **Documentation**: [docs/](docs/)
- **Quick Start**: [QUICKSTART.md](QUICKSTART.md)
- **Architecture**: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **Extension Guide**: [docs/COPILOT_GUIDE.md](docs/COPILOT_GUIDE.md)

## 🗺️ Roadmap

- [x] Spring Boot 4 & Java 21 support
- [x] Reactive WebFlux architecture
- [x] Resilience patterns (circuit breaker, retry)
- [x] Comprehensive documentation
- [x] Extensible tool registration
- [ ] OAuth 2.0 authentication support
- [ ] Bulk document operations
- [ ] Advanced permission management
- [ ] WebSocket transport for MCP
- [ ] GraphQL API support
- [ ] Multi-tenancy support
- [ ] AI-powered document classification

---

**Built with ❤️ using Spring Boot 4 and Java 21 for JPMorgan Chase and Enterprise Applications**
