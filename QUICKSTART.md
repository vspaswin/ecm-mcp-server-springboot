# Quick Start Guide

## Prerequisites

- Java 21 (JDK 21)
- Maven 3.8+
- Git

## Getting Started

### Step 1: Pull Latest Changes

```bash
git pull origin main
```

### Step 2: Clean Previous Build

```bash
mvn clean
```

### Step 3: Build the Project

```bash
mvn clean install
```

This will:
1. Clean previous builds
2. Compile source code
3. Run tests
4. Package into JAR file

### Step 4: Run the Application

```bash
java -jar target/ecm-mcp-server-1.0.0.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

## Verifying the Build

### Check Java Version

```bash
java -version
```

Expected output:
```
openjdk version "21.0.x" ...
```

### Check Maven Version

```bash
mvn -version
```

Expected output:
```
Apache Maven 3.8.x or higher
Java version: 21.0.x
```

## Build Stages

The build process has been fixed and optimized for stage-by-stage compilation:

### Stage 1: Compile Core Models
```bash
mvn compile -pl . -am
```

Compiles:
- MCP protocol models (McpRequest, McpResponse, McpTool)
- DTOs (DocumentDto, SearchRequestDto, etc.)
- Exception classes

### Stage 2: Compile Services
```bash
mvn compile
```

Compiles:
- Business services (DocumentService, SearchService, etc.)
- MCP tool registry and executor
- ECM clients

### Stage 3: Run Tests
```bash
mvn test
```

Runs:
- Unit tests for all components
- Integration tests for MCP controller
- Client tests

### Stage 4: Package
```bash
mvn package
```

Creates:
- Executable JAR: `target/ecm-mcp-server-1.0.0.jar`
- Test JARs
- Build artifacts

## Configuration

### Environment Variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` with your ECM system details:

```bash
# FileNet Configuration
FILENET_BASE_URL=http://your-filenet-server:9080/fncmis
FILENET_USERNAME=admin
FILENET_PASSWORD=your-password

# SharePoint Configuration (optional)
SHAREPOINT_BASE_URL=https://your-tenant.sharepoint.com
SHAREPOINT_CLIENT_ID=your-client-id
SHAREPOINT_CLIENT_SECRET=your-secret

# Server Configuration
SERVER_PORT=8080
```

### Running with Environment Variables

```bash
# Load environment variables
source .env

# Run application
java -jar target/ecm-mcp-server-1.0.0.jar
```

Or pass directly:

```bash
java -jar target/ecm-mcp-server-1.0.0.jar \
  --ecm.filenet.base-url=http://filenet:9080/fncmis \
  --ecm.filenet.username=admin \
  --ecm.filenet.password=password
```

## Testing the Server

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### List MCP Tools

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

Expected response:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "tools": [
      {
        "name": "ecm_get_document",
        "description": "Retrieve a document from ECM system by its ID",
        "inputSchema": {...}
      },
      ...
    ]
  }
}
```

### Call an MCP Tool

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "ecm_get_document",
      "arguments": {
        "documentId": "DOC-123"
      }
    }
  }'
```

## Docker Deployment

### Build Docker Image

```bash
docker build -t ecm-mcp-server:1.0.0 .
```

### Run with Docker

```bash
docker run -d \
  -p 8080:8080 \
  -e FILENET_BASE_URL=http://filenet:9080/fncmis \
  -e FILENET_USERNAME=admin \
  -e FILENET_PASSWORD=password \
  --name ecm-mcp-server \
  ecm-mcp-server:1.0.0
```

### Using Docker Compose

```bash
docker-compose up -d
```

## Using with Claude Desktop

### macOS Configuration

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

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

### Windows Configuration

Edit `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "ecm-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\path\\to\\ecm-mcp-server-springboot\\target\\ecm-mcp-server-1.0.0.jar"
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

### Restart Claude Desktop

After configuration:
1. Quit Claude Desktop completely
2. Restart Claude Desktop
3. Look for the hammer icon (🔨) in the chat interface
4. Click to see available MCP tools

## Using with VSCode Copilot

### Install MCP Extension

1. Open VSCode
2. Install "MCP Servers" extension
3. Reload VSCode

### Configure MCP Server

Create `.mcp.json` in your workspace or user directory:

```json
{
  "servers": {
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

## Troubleshooting

### Issue: "invalid source release 21 with --enable-preview"

**Solution**: Pull the latest code from GitHub:

```bash
git pull origin main
mvn clean
mvn install
```

The `--enable-preview` flag has been removed from the pom.xml.

### Issue: "Java version mismatch"

**Solution**: Ensure you're using Java 21:

```bash
# Check current version
java -version

# If using jenv
jenv local 21

# If using SDKMAN
sdk use java 21.0.x-tem

# If using Homebrew (macOS)
brew install openjdk@21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
```

### Issue: "Connection refused to ECM system"

**Solution**: Verify ECM system URL and credentials:

```bash
# Test FileNet connection
curl -u admin:password http://filenet-server:9080/fncmis/resources

# Check network connectivity
ping filenet-server
telnet filenet-server 9080
```

### Issue: "Port 8080 already in use"

**Solution**: Change the port:

```bash
java -jar target/ecm-mcp-server-1.0.0.jar --server.port=8081
```

Or update `application.yml`:

```yaml
server:
  port: 8081
```

### Issue: Tests failing

**Solution**: Skip tests during build:

```bash
mvn clean install -DskipTests
```

Or fix individual test issues:

```bash
# Run specific test
mvn test -Dtest=DocumentServiceTest

# Run with debug logging
mvn test -X
```

## Development Workflow

### Making Changes

```bash
# 1. Create a feature branch
git checkout -b feature/add-new-tool

# 2. Make changes
# Edit files...

# 3. Build and test
mvn clean verify

# 4. Commit changes
git add .
git commit -m "feat: Add new ECM tool for metadata retrieval"

# 5. Push to GitHub
git push origin feature/add-new-tool
```

### Hot Reload During Development

Use Spring Boot DevTools (already included):

```bash
mvn spring-boot:run
```

Changes to Java files will trigger automatic restart.

### Building Without Tests

For faster builds during development:

```bash
mvn clean install -DskipTests
```

## Monitoring

### Actuator Endpoints

Available endpoints:

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`
- Prometheus: `http://localhost:8080/actuator/prometheus`

### View Metrics

```bash
# All metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Tool execution metrics
curl http://localhost:8080/actuator/metrics/mcp.tool.executions
```

## Next Steps

1. **Read the Architecture**: See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for system design
2. **Learn to Extend**: See [docs/COPILOT_GUIDE.md](docs/COPILOT_GUIDE.md) for adding new tools
3. **Configure for Production**: Update environment-specific settings
4. **Add Your ECM System**: Implement custom `EcmClient`
5. **Create Custom Tools**: Add business-specific MCP tools

## Support

For issues:
1. Check this guide
2. Review logs: `tail -f logs/application.log`
3. Check GitHub issues
4. Create new issue with:
   - Error message
   - Steps to reproduce
   - Environment details (Java version, OS, etc.)

## Additional Resources

- [MCP Specification](https://modelcontextprotocol.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Project Documentation](docs/)
