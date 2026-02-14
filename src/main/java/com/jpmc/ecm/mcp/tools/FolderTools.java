package com.jpmc.ecm.mcp.tools;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.dto.FolderDto;
import com.jpmc.ecm.mcp.model.ToolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * MCP tools for folder operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FolderTools {

    private final EcmApiClient ecmApiClient;

    /**
     * Get list of available folder tools
     */
    public List<ToolInfo> getToolDefinitions() {
        return List.of(
            createFolderToolInfo(),
            listFolderContentsToolInfo()
        );
    }

    /**
     * Execute folder tool
     */
    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_create_folder" -> createFolder(params);
            case "ecm_list_folder_contents" -> listFolderContents(params);
            default -> Mono.error(new IllegalArgumentException(
                "Unknown tool: " + toolName));
        };
    }

    /**
     * Create folder tool
     */
    private Mono<Object> createFolder(Map<String, Object> params) {
        String name = (String) params.get("name");
        String parentId = (String) params.get("parentId");
        String description = (String) params.get("description");

        if (name == null || name.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                "name is required"));
        }

        log.info("Executing ecm_create_folder: name='{}', parentId='{}'", 
            name, parentId);

        return ecmApiClient.createFolder(name, parentId, description)
                .cast(Object.class)
                .doOnSuccess(folder -> {
                    FolderDto dto = (FolderDto) folder;
                    log.info("Folder created: {} (ID: {})", name, dto.getId());
                })
                .doOnError(error -> log.error("Failed to create folder: {}", name, error));
    }

    /**
     * List folder contents tool
     */
    private Mono<Object> listFolderContents(Map<String, Object> params) {
        String folderId = (String) params.get("folderId");
        Boolean includeDocuments = params.get("includeDocuments") != null ?
            (Boolean) params.get("includeDocuments") : true;
        Boolean includeSubfolders = params.get("includeSubfolders") != null ?
            (Boolean) params.get("includeSubfolders") : true;

        if (folderId == null || folderId.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                "folderId is required"));
        }

        log.info("Executing ecm_list_folder_contents: folderId='{}'", folderId);

        return ecmApiClient.getFolderContents(folderId, includeDocuments, includeSubfolders)
                .cast(Object.class)
                .doOnSuccess(folder -> log.info("Folder contents retrieved: {}", folderId))
                .doOnError(error -> log.error("Failed to list folder contents: {}", 
                    folderId, error));
    }

    /**
     * Tool info for create folder
     */
    private ToolInfo createFolderToolInfo() {
        return ToolInfo.builder()
                .name("ecm_create_folder")
                .description("Create a new folder in the ECM system")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "name", Map.of(
                            "type", "string",
                            "description", "Name of the folder to create"
                        ),
                        "parentId", Map.of(
                            "type", "string",
                            "description", "ID of the parent folder (optional, omit for root)"
                        ),
                        "description", Map.of(
                            "type", "string",
                            "description", "Description of the folder (optional)"
                        )
                    ),
                    "required", List.of("name")
                ))
                .build();
    }

    /**
     * Tool info for list folder contents
     */
    private ToolInfo listFolderContentsToolInfo() {
        return ToolInfo.builder()
                .name("ecm_list_folder_contents")
                .description("List the contents of a folder including documents and subfolders")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "folderId", Map.of(
                            "type", "string",
                            "description", "ID of the folder to list"
                        ),
                        "includeDocuments", Map.of(
                            "type", "boolean",
                            "description", "Include documents in the results (default: true)",
                            "default", true
                        ),
                        "includeSubfolders", Map.of(
                            "type", "boolean",
                            "description", "Include subfolders in the results (default: true)",
                            "default", true
                        )
                    ),
                    "required", List.of("folderId")
                ))
                .build();
    }
}
