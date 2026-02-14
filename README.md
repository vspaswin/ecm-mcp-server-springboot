# ECM MCP Server - Spring Boot 4

A production-ready **Model Context Protocol (MCP) server** built with **Spring Boot 4** and **Java 21** for seamless integration with Enterprise Content Management (ECM) REST APIs. This server enables AI assistants and MCP clients to manage documents, search content, organize folders, handle metadata, control versions, and manage workflows within enterprise content management systems.

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
- **Java 21** - Modern Java with virtual threads and pattern matching
- **Spring WebFlux** - Reactive programming for non-blocking I/O
- **Resilience4j** - Circuit breaker, retry, and rate limiting
- **Micrometer** - Observability with Prometheus integration
- **Lombok** - Reduce boilerplate code

### Project Structure

```
ecm-mcp-server-springboot/
├── src/main/java/com/jpmc/ecm/
│   ├── EcmMcpServerApplication.java    # Main application class
│   ├── client/
│   │   └── EcmApiClient.java            # ECM REST API client
│   ├── config/
│   │   ├── EcmApiProperties.java        # ECM API configuration
│   │   ├── WebClientConfig.java         # WebClient setup
│   │   └── McpProtocolConfig.java       # MCP protocol config
│   ├── controller/
│   │   ├── McpController.java           # MCP HTTP endpoints
│   │   └── HealthController.java        # Health check endpoint
│   ├── dto/                             # Data Transfer Objects
│   │   ├── DocumentDto.java
│   │   ├── FolderDto.java
│   │   ├── SearchRequestDto.java
│   │   ├── SearchResultDto.java
│   │   ├── VersionDto.java
│   │   └── WorkflowDto.java
│   ├── exception/                       # Exception handling
│   │   ├── EcmApiException.java
│   │   └── GlobalExceptionHandler.java
│   └── mcp/                            # MCP Protocol layer
│       ├── McpProtocolHandler.java      # Main protocol handler
│       ├── model/                       # MCP models
│       │   ├── McpRequest.java
│       │   ├── McpResponse.java
│       │   ├── McpError.java
│       │   └── ToolInfo.java
│       └── tools/                       # MCP Tool implementations
│           ├── DocumentTools.java
│           ├── SearchTools.java
│           ├── FolderTools.java
│           ├── MetadataTools.java
│           ├── VersionTools.java
│           └── WorkflowTools.java
├── src/main/resources/
│   ├── application.yml                  # Main configuration
│   ├── application-dev.yml              # Development profile
│   └── application-prod.yml             # Production profile
└── pom.xml                              # Maven dependencies
```

## 📦 Installation

### Prerequisites
- **Java 21** or higher
- **Maven 3.9+**
- Access to ECM REST API
- API credentials (username/password or API key)

### Build from Source

```bash
# Clone the repository
git clone https://github.com/vspaswin/ecm-mcp-server-springboot.git
cd ecm-mcp-server-springboot

# Build with Maven
mvn clean package

# Run the application
java -jar target/ecm-mcp-server-1.0.0.jar
```

### Configuration

Create `application-local.yml` or set environment variables:

```yaml
ecm:
  api:
    base-url: https://your-ecm-api.com/api/v1
    username: your_username
    password: your_password
    # OR use API key:
    # api-key: your_api_key
```

Or use environment variables:

```bash
export ECM_API_URL=https://your-ecm-api.com/api/v1
export ECM_USERNAME=your_username
export ECM_PASSWORD=your_password
```

## 🚀 Usage

### Running the Server

**Development mode:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production mode:**
```bash
java -jar target/ecm-mcp-server-1.0.0.jar --spring.profiles.active=prod
```

**With custom configuration:**
```bash
java -jar target/ecm-mcp-server-1.0.0.jar \
  --ecm.api.base-url=https://ecm-api.example.com \
  --ecm.api.username=myuser \
  --ecm.api.password=mypass
```

### Integration with Claude Desktop

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
        "ECM_API_URL": "https://your-ecm-api.com/api/v1",
        "ECM_USERNAME": "your_username",
        "ECM_PASSWORD": "your_password"
      }
    }
  }
}
```

### Available MCP Tools

#### Documents
- `ecm_get_document` - Get document information
- `ecm_delete_document` - Delete a document

#### Search
- `ecm_search_documents` - Simple text search
- `ecm_advanced_search` - Search with filters

#### Folders
- `ecm_create_folder` - Create new folder
- `ecm_list_folder_contents` - List folder contents

#### Metadata
- `ecm_get_metadata` - Get document metadata
- `ecm_update_metadata` - Update metadata fields

#### Versions
- `ecm_get_versions` - Get version history

#### Workflows
- `ecm_start_workflow` - Start workflow on document
- `ecm_get_workflow_status` - Get workflow status

### API Endpoints

**MCP Protocol (HTTP):**
```bash
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'
```

**Health Check:**
```bash
curl http://localhost:8080/health
```

**Actuator Endpoints:**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

## 🔧 Configuration

### Key Configuration Properties

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `ecm.api.base-url` | ECM API base URL | - | Yes |
| `ecm.api.username` | Basic auth username | - | Yes* |
| `ecm.api.password` | Basic auth password | - | Yes* |
| `ecm.api.api-key` | API key auth | - | Yes* |
| `ecm.api.timeout.connect` | Connection timeout | 10s | No |
| `ecm.api.timeout.read` | Read timeout | 30s | No |
| `ecm.api.max-retries` | Max retry attempts | 3 | No |
| `server.port` | Server port | 8080 | No |

*Either username/password OR api-key is required

### Resilience Configuration

**Circuit Breaker:**
- Failure threshold: 50%
- Sliding window: 10 calls
- Wait duration: 10 seconds

**Retry:**
- Max attempts: 3
- Wait duration: 1 second
- Exponential backoff: 2x multiplier

## 📊 Monitoring

### Prometheus Metrics

Metrics are exposed at `/actuator/prometheus`:

- HTTP request metrics
- ECM API call metrics
- Circuit breaker stats
- Retry statistics
- JVM metrics (heap, threads, GC)

### Health Checks

Comprehensive health checks at `/actuator/health`:

- Application status
- ECM API connectivity
- Circuit breaker status
- Disk space
- Database (if applicable)

## 🧪 Testing

**Run all tests:**
```bash
mvn test
```

**Run with coverage:**
```bash
mvn test jacoco:report
```

**Integration tests:**
```bash
mvn verify
```

## 🐳 Docker Deployment

**Build Docker image:**
```bash
docker build -t ecm-mcp-server:latest .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e ECM_API_URL=https://ecm-api.example.com \
  -e ECM_USERNAME=myuser \
  -e ECM_PASSWORD=mypass \
  --name ecm-mcp-server \
  ecm-mcp-server:latest
```

## 🔐 Security

### Best Practices

1. **Secure Credentials**
   - Use environment variables for sensitive data
   - Never commit credentials to version control
   - Consider using HashiCorp Vault or AWS Secrets Manager

2. **HTTPS Only**
   - Always use HTTPS for ECM API connections
   - Validate SSL certificates
   - Use TLS 1.2 or higher

3. **Authentication**
   - Prefer API key authentication over basic auth
   - Rotate credentials regularly
   - Implement rate limiting

4. **Network Security**
   - Run behind firewall or VPN
   - Limit access to trusted networks
   - Use Spring Security for HTTP endpoints

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Model Context Protocol](https://modelcontextprotocol.io/) - MCP specification
- [Resilience4j](https://resilience4j.readme.io/) - Resilience patterns
- [Project Reactor](https://projectreactor.io/) - Reactive programming

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/vspaswin/ecm-mcp-server-springboot/issues)
- **Discussions**: [GitHub Discussions](https://github.com/vspaswin/ecm-mcp-server-springboot/discussions)
- **Email**: support@example.com

## 🗺️ Roadmap

- [ ] OAuth 2.0 authentication support
- [ ] Bulk document operations
- [ ] Advanced permission management
- [ ] Document template support
- [ ] Audit log retrieval
- [ ] WebSocket transport for MCP
- [ ] GraphQL API support
- [ ] Multi-tenancy support
- [ ] AI-powered document classification
- [ ] Advanced workflow automation

---

**Built with ❤️ using Spring Boot 4 and Java 21**
