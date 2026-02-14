package com.jpmc.ecm.mcp.tools;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.mcp.model.ToolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * MCP tools for version control operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VersionTools {

    private final EcmApiClient ecmApiClient;

    public List<ToolInfo> getToolDefinitions() {
        return List.of(getVersionsToolInfo());
    }

    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_get_versions" -> getVersions(params);
            default -> Mono.error(new IllegalArgumentException("Unknown tool: " + toolName));
        };
    }

    private Mono<Object> getVersions(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("documentId is required"));
        }

        log.info("Executing ecm_get_versions: {}", documentId);
        return ecmApiClient.getVersions(documentId).cast(Object.class);
    }

    private ToolInfo getVersionsToolInfo() {
        return ToolInfo.builder()
            .name("ecm_get_versions")
            .description("Get version history for a document")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "documentId", Map.of(
                        "type", "string",
                        "description", "Document ID"
                    )
                ),
                "required", List.of("documentId")
            ))
            .build();
    }
}
