package com.jpmc.ecm.mcp.tools;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.dto.DocumentDto;
import com.jpmc.ecm.mcp.model.ToolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * MCP tools for document operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTools {

    private final EcmApiClient ecmApiClient;

    /**
     * Get list of available document tools
     */
    public List<ToolInfo> getToolDefinitions() {
        return List.of(
            getDocumentToolInfo(),
            deleteDocumentToolInfo()
        );
    }

    /**
     * Execute document tool
     */
    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_get_document" -> getDocument(params);
            case "ecm_delete_document" -> deleteDocument(params);
            default -> Mono.error(new IllegalArgumentException(
                "Unknown tool: " + toolName));
        };
    }

    /**
     * Get document tool
     */
    private Mono<Object> getDocument(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                "documentId is required"));
        }

        log.info("Executing ecm_get_document: {}", documentId);
        
        return ecmApiClient.getDocument(documentId)
                .cast(Object.class)
                .doOnSuccess(doc -> log.info("Document retrieved: {}", documentId))
                .doOnError(error -> log.error("Failed to get document: {}", 
                    documentId, error));
    }

    /**
     * Delete document tool
     */
    private Mono<Object> deleteDocument(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                "documentId is required"));
        }

        log.info("Executing ecm_delete_document: {}", documentId);
        
        return ecmApiClient.deleteDocument(documentId)
                .thenReturn(Map.of(
                    "success", true,
                    "message", "Document deleted successfully",
                    "documentId", documentId
                ))
                .cast(Object.class)
                .doOnSuccess(result -> log.info("Document deleted: {}", documentId))
                .doOnError(error -> log.error("Failed to delete document: {}", 
                    documentId, error));
    }

    /**
     * Tool info for get document
     */
    private ToolInfo getDocumentToolInfo() {
        return ToolInfo.builder()
                .name("ecm_get_document")
                .description("Get detailed information about a document by its ID")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "documentId", Map.of(
                            "type", "string",
                            "description", "The unique identifier of the document"
                        )
                    ),
                    "required", List.of("documentId")
                ))
                .build();
    }

    /**
     * Tool info for delete document
     */
    private ToolInfo deleteDocumentToolInfo() {
        return ToolInfo.builder()
                .name("ecm_delete_document")
                .description("Delete a document from the ECM system")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "documentId", Map.of(
                            "type", "string",
                            "description", "The unique identifier of the document to delete"
                        )
                    ),
                    "required", List.of("documentId")
                ))
                .build();
    }
}
