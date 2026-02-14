package com.jpmc.ecm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for MCP Protocol.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "mcp.protocol")
public class McpProtocolConfig {

    /**
     * MCP protocol version
     */
    private String version = "2024-11-05";

    /**
     * Transport mechanism (stdio, http, websocket)
     */
    private String transport = "stdio";

    /**
     * Server capabilities
     */
    private Capabilities capabilities = new Capabilities();

    @Data
    public static class Capabilities {
        /**
         * Whether tools are supported
         */
        private boolean tools = true;

        /**
         * Whether prompts are supported
         */
        private boolean prompts = false;

        /**
         * Whether resources are supported
         */
        private boolean resources = false;
    }
}
