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
 * MCP tools for metadata operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataTools {

    private final EcmApiClient ecmApiClient;

    public List<ToolInfo> getToolDefinitions() {
        return List.of(
            getMetadataToolInfo(),
            updateMetadataToolInfo()
        );
    }

    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_get_metadata" -> getMetadata(params);
            case "ecm_update_metadata" -> updateMetadata(params);
            default -> Mono.error(new IllegalArgumentException("Unknown tool: " + toolName));
        };
    }

    @SuppressWarnings("unchecked")
    private Mono<Object> getMetadata(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("documentId is required"));
        }

        log.info("Executing ecm_get_metadata: {}", documentId);
        return ecmApiClient.getMetadata(documentId).cast(Object.class);
    }

    @SuppressWarnings("unchecked")
    private Mono<Object> updateMetadata(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        Map<String, Object> metadata = (Map<String, Object>) params.get("metadata");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("documentId is required"));
        }
        if (metadata == null || metadata.isEmpty()) {
            return Mono.error(new IllegalArgumentException("metadata is required"));
        }

        log.info("Executing ecm_update_metadata: {}", documentId);
        return ecmApiClient.updateMetadata(documentId, metadata).cast(Object.class);
    }

    private ToolInfo getMetadataToolInfo() {
        return ToolInfo.builder()
            .name("ecm_get_metadata")
            .description("Get metadata for a document")
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

    private ToolInfo updateMetadataToolInfo() {
        return ToolInfo.builder()
            .name("ecm_update_metadata")
            .description("Update metadata for a document")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "documentId", Map.of(
                        "type", "string",
                        "description", "Document ID"
                    ),
                    "metadata", Map.of(
                        "type", "object",
                        "description", "Metadata key-value pairs to update"
                    )
                ),
                "required", List.of("documentId", "metadata")
            ))
            .build();
    }
}
