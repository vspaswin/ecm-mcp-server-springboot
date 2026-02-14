# Getting Started with ECM MCP Server

This guide will help you get up and running with the ECM MCP Server in under 10 minutes.

---

## Prerequisites

Before you begin, ensure you have:

- [ ] **Java 21** installed ([Download](https://adoptium.net/temurin/releases/?version=21))
- [ ] **Maven 3.9+** installed ([Download](https://maven.apache.org/download.cgi))
- [ ] **Git** installed
- [ ] Access to an ECM REST API endpoint
- [ ] API credentials (username/password)

### Verify Installation

```bash
# Check Java version
java -version
# Should show: openjdk version "21.x.x" or similar

# Check Maven version
mvn --version
# Should show: Apache Maven 3.9.x or higher

# Check Git
git --version
```

---

## Step 1: Clone the Repository

```bash
# Clone from GitHub
git clone https://github.com/vspaswin/ecm-mcp-server-springboot.git

# Navigate to project directory
cd ecm-mcp-server-springboot

# Verify you're on main branch
git branch
# Should show: * main
```

---

## Step 2: Build the Project

```bash
# Clean build (first time or after pulling changes)
mvn clean install

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: ~40 seconds
```

**If you see compilation errors**, pull the latest changes:

```bash
git pull origin main
mvn clean install
```

---

## Step 3: Configure Environment

### Option A: Environment Variables (Recommended)

**macOS/Linux**:
```bash
export ECM_BASE_URL="http://localhost:8081/api"
export ECM_USERNAME="admin"
export ECM_PASSWORD="admin"
```

**Windows (PowerShell)**:
```powershell
$env:ECM_BASE_URL="http://localhost:8081/api"
$env:ECM_USERNAME="admin"
$env:ECM_PASSWORD="admin"
```

**Windows (Command Prompt)**:
```cmd
set ECM_BASE_URL=http://localhost:8081/api
set ECM_USERNAME=admin
set ECM_PASSWORD=admin
```

### Option B: Configuration File

Edit `src/main/resources/application.yml`:

```yaml
ecm:
  api:
    base-url: http://localhost:8081/api
    username: admin
    password: admin
```

⚠️ **Security Note**: Never commit real credentials to Git. Use environment variables in production.

---

## Step 4: Run the Server

### Local Testing (Development Mode)

```bash
# Run with Maven (development)
mvn spring-boot:run

# Expected output:
# Started McpServerApplication in X.XXX seconds
```

### Production Mode

```bash
# Run JAR directly
java -jar target/ecm-mcp-server-1.0.0.jar

# Or with environment variables inline
ECM_BASE_URL=http://localhost:8081/api \
ECM_USERNAME=admin \
ECM_PASSWORD=admin \
java -jar target/ecm-mcp-server-1.0.0.jar
```

The server will start reading from STDIN and writing to STDOUT (MCP protocol).

---

## Step 5: Integrate with Claude Desktop

### macOS Configuration

1. Open: `~/Library/Application Support/Claude/claude_desktop_config.json`

2. Add ECM server configuration:

```json
{
  "mcpServers": {
    "ecm": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/YOUR_USERNAME/develop/projects/ecm-mcp-server-springboot/target/ecm-mcp-server-1.0.0.jar"
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

**Important**: Replace `/Users/YOUR_USERNAME/...` with your actual absolute path.

### Windows Configuration

1. Open: `%APPDATA%\Claude\claude_desktop_config.json`

2. Add ECM server configuration:

```json
{
  "mcpServers": {
    "ecm": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\Users\\YOUR_USERNAME\\projects\\ecm-mcp-server-springboot\\target\\ecm-mcp-server-1.0.0.jar"
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

**Important**: Use double backslashes `\\` in Windows paths.

### Restart Claude Desktop

1. Quit Claude Desktop completely
2. Restart Claude Desktop
3. Look for 🔨 tools icon in the interface
4. You should see ECM tools available

---

## Step 6: Test the Integration

Open Claude Desktop and try these prompts:

### Test 1: List Tools

```
What ECM tools do you have available?
```

Expected response: Claude should list all available ECM tools (document_get, folder_create, search_documents, etc.)

### Test 2: Simple Operation

```
Get information about document with ID "doc123"
```

Expected: Claude should call the `document_get` tool and show document information.

### Test 3: Complex Workflow

```
Search for documents containing "quarterly report" and show me the most recent 5 results
```

Expected: Claude should call `search_documents` and format results.

---

## Common Issues & Solutions

### Issue 1: Build Fails with "invalid source release 21"

**Solution**:
```bash
git pull origin main
mvn clean install
```

This error was fixed in recent commits.

### Issue 2: Claude Desktop doesn't show tools

**Checklist**:
- [ ] Is the absolute path in `claude_desktop_config.json` correct?
- [ ] Did you restart Claude Desktop after editing config?
- [ ] Are environment variables set correctly?
- [ ] Can you run the JAR manually in terminal?

**Debug**:
```bash
# Test running the JAR manually
cd /path/to/ecm-mcp-server-springboot
java -jar target/ecm-mcp-server-1.0.0.jar

# If it starts without errors, the JAR is fine
# Press Ctrl+C to stop
```

### Issue 3: Connection to ECM Backend Fails

**Symptoms**: Tools work in Claude but return errors

**Solution**:

1. Verify ECM API is running:
```bash
curl http://localhost:8081/api/health
```

2. Test authentication:
```bash
curl -u admin:admin http://localhost:8081/api/documents
```

3. Check logs in Claude Desktop:
   - macOS: `~/Library/Logs/Claude/`
   - Windows: `%APPDATA%\Claude\Logs\`

### Issue 4: Java Not Found

**Error**: `java: command not found`

**Solution**:

1. Install Java 21 from [Adoptium](https://adoptium.net/temurin/releases/?version=21)

2. Verify installation:
```bash
java -version
```

3. If still not found, add to PATH:

**macOS/Linux** (add to `~/.bashrc` or `~/.zshrc`):
```bash
export JAVA_HOME=/path/to/java-21
export PATH=$JAVA_HOME/bin:$PATH
```

**Windows** (System Properties → Environment Variables):
- Add `JAVA_HOME` → `C:\Program Files\Java\jdk-21`
- Edit `PATH` → Add `%JAVA_HOME%\bin`

---

## Next Steps

### For Users

1. **Explore Tools** - Try different ECM operations in Claude Desktop
2. **Read Documentation** - Check [README.md](README.md) for all available tools
3. **Workflow Examples** - See how Claude can automate ECM tasks

### For Developers

1. **Read [COPILOT.md](COPILOT.md)** - Comprehensive development guide
   - Architecture overview
   - Code patterns
   - How to add new tools
   - Testing strategies

2. **Read [ARCHITECTURE.md](ARCHITECTURE.md)** - System design
   - Component diagrams
   - Data flow
   - Design patterns
   - Technology stack

3. **Explore Codebase**:
```bash
# Main application entry point
src/main/java/com/jpmc/ecm/mcp/McpServerApplication.java

# Handler layer (tool implementations)
src/main/java/com/jpmc/ecm/handler/

# Service layer (business logic)
src/main/java/com/jpmc/ecm/service/

# API client (ECM communication)
src/main/java/com/jpmc/ecm/client/EcmApiClient.java
```

4. **Run Tests**:
```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=DocumentServiceTest

# With coverage
mvn verify
```

5. **Add Your First Tool**:
   - Follow the step-by-step guide in [COPILOT.md - Adding New Tools](COPILOT.md#adding-new-tools)
   - See code examples and patterns
   - Write tests for your new tool

---

## Quick Reference Commands

```bash
# Build
mvn clean install

# Run (development)
mvn spring-boot:run

# Run (production)
java -jar target/ecm-mcp-server-1.0.0.jar

# Test
mvn test

# Clean
mvn clean

# Package only (skip tests)
mvn package -DskipTests

# Update dependencies
mvn versions:display-dependency-updates

# Pull latest changes
git pull origin main
```

---

## Environment Variables Reference

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `ECM_BASE_URL` | ECM API endpoint | `http://localhost:8081/api` | Yes |
| `ECM_USERNAME` | API username | `admin` | Yes |
| `ECM_PASSWORD` | API password | `admin` | Yes |
| `SERVER_PORT` | Server port (if using HTTP endpoint) | `8080` | No |
| `LOGGING_LEVEL` | Log level | `DEBUG` | No |

---

## Useful Links

- **Main Documentation**: [README.md](README.md)
- **Developer Guide**: [COPILOT.md](COPILOT.md)
- **Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **MCP Protocol**: [https://modelcontextprotocol.io](https://modelcontextprotocol.io)
- **Spring WebFlux**: [https://docs.spring.io/spring-framework/reference/web/webflux.html](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- **Issues**: [GitHub Issues](https://github.com/vspaswin/ecm-mcp-server-springboot/issues)

---

## Getting Help

1. **Check documentation first**:
   - [README.md](README.md) for general info
   - [COPILOT.md](COPILOT.md) for development
   - [ARCHITECTURE.md](ARCHITECTURE.md) for design

2. **Search existing issues**: [GitHub Issues](https://github.com/vspaswin/ecm-mcp-server-springboot/issues)

3. **Common issues section** in this document

4. **Create new issue** if problem persists:
   - Include error messages
   - Include Java/Maven versions
   - Include steps to reproduce
   - Include relevant logs

---

## Success Checklist

You've successfully set up the ECM MCP Server when:

- [ ] Build completes without errors (`mvn clean install`)
- [ ] JAR file exists at `target/ecm-mcp-server-1.0.0.jar`
- [ ] Claude Desktop config file is updated with correct paths
- [ ] Claude Desktop shows 🔨 tools icon after restart
- [ ] ECM tools appear in Claude's tool list
- [ ] Test query (e.g., "What ECM tools do you have?") works
- [ ] Document retrieval test works

---

**Ready to build features?** Head to [COPILOT.md](COPILOT.md) for comprehensive development guidance!

**Questions about architecture?** Check out [ARCHITECTURE.md](ARCHITECTURE.md) for detailed system design.

**Want to see all available tools?** See [README.md - Available MCP Tools](README.md#available-mcp-tools).