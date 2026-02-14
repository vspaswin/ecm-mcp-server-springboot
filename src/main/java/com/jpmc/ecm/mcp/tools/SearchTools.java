package com.jpmc.ecm.mcp.tools;

import com.jpmc.ecm.client.EcmApiClient;
import com.jpmc.ecm.dto.SearchRequestDto;
import com.jpmc.ecm.dto.SearchResultDto;
import com.jpmc.ecm.mcp.model.ToolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * MCP tools for search operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTools {

    private final EcmApiClient ecmApiClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Get list of available search tools
     */
    public List<ToolInfo> getToolDefinitions() {
        return List.of(
            searchDocumentsToolInfo(),
            advancedSearchToolInfo()
        );
    }

    /**
     * Execute search tool
     */
    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_search_documents" -> searchDocuments(params);
            case "ecm_advanced_search" -> advancedSearch(params);
            default -> Mono.error(new IllegalArgumentException(
                "Unknown tool: " + toolName));
        };
    }

    /**
     * Simple search tool
     */
    private Mono<Object> searchDocuments(Map<String, Object> params) {
        String query = (String) params.get("query");
        Integer maxResults = params.get("maxResults") != null ? 
            ((Number) params.get("maxResults")).intValue() : 50;

        log.info("Executing ecm_search_documents: query='{}', maxResults={}", 
            query, maxResults);

        SearchRequestDto searchRequest = SearchRequestDto.builder()
                .query(query)
                .maxResults(maxResults)
                .build();

        return ecmApiClient.searchDocuments(searchRequest)
                .cast(Object.class)
                .doOnSuccess(result -> {
                    SearchResultDto dto = (SearchResultDto) result;
                    log.info("Search completed: {} results", dto.getTotalCount());
                })
                .doOnError(error -> log.error("Search failed", error));
    }

    /**
     * Advanced search tool
     */
    @SuppressWarnings("unchecked")
    private Mono<Object> advancedSearch(Map<String, Object> params) {
        String query = (String) params.get("query");
        String folderId = (String) params.get("folderId");
        String documentType = (String) params.get("documentType");
        String dateFromStr = (String) params.get("dateFrom");
        String dateToStr = (String) params.get("dateTo");
        List<String> tags = (List<String>) params.get("tags");
        Integer maxResults = params.get("maxResults") != null ? 
            ((Number) params.get("maxResults")).intValue() : 50;

        log.info("Executing ecm_advanced_search: query='{}'", query);

        LocalDate dateFrom = dateFromStr != null ? 
            LocalDate.parse(dateFromStr, DATE_FORMATTER) : null;
        LocalDate dateTo = dateToStr != null ? 
            LocalDate.parse(dateToStr, DATE_FORMATTER) : null;

        SearchRequestDto searchRequest = SearchRequestDto.builder()
                .query(query)
                .folderId(folderId)
                .documentType(documentType)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .tags(tags)
                .maxResults(maxResults)
                .build();

        return ecmApiClient.searchDocuments(searchRequest)
                .cast(Object.class)
                .doOnSuccess(result -> {
                    SearchResultDto dto = (SearchResultDto) result;
                    log.info("Advanced search completed: {} results", dto.getTotalCount());
                })
                .doOnError(error -> log.error("Advanced search failed", error));
    }

    /**
     * Tool info for simple search
     */
    private ToolInfo searchDocumentsToolInfo() {
        return ToolInfo.builder()
                .name("ecm_search_documents")
                .description("Search for documents using a text query")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "query", Map.of(
                            "type", "string",
                            "description", "Search query text"
                        ),
                        "maxResults", Map.of(
                            "type", "integer",
                            "description", "Maximum number of results to return (default: 50)",
                            "default", 50
                        )
                    ),
                    "required", List.of("query")
                ))
                .build();
    }

    /**
     * Tool info for advanced search
     */
    private ToolInfo advancedSearchToolInfo() {
        return ToolInfo.builder()
                .name("ecm_advanced_search")
                .description("Perform advanced search with filters for document type, date range, folder, and tags")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "query", Map.of(
                            "type", "string",
                            "description", "Search query text"
                        ),
                        "folderId", Map.of(
                            "type", "string",
                            "description", "Filter by folder ID"
                        ),
                        "documentType", Map.of(
                            "type", "string",
                            "description", "Filter by document type (e.g., 'pdf', 'contract', 'invoice')"
                        ),
                        "dateFrom", Map.of(
                            "type", "string",
                            "description", "Start date for filtering (ISO format: YYYY-MM-DD)",
                            "format", "date"
                        ),
                        "dateTo", Map.of(
                            "type", "string",
                            "description", "End date for filtering (ISO format: YYYY-MM-DD)",
                            "format", "date"
                        ),
                        "tags", Map.of(
                            "type", "array",
                            "items", Map.of("type", "string"),
                            "description", "Filter by tags"
                        ),
                        "maxResults", Map.of(
                            "type", "integer",
                            "description", "Maximum number of results to return (default: 50)",
                            "default", 50
                        )
                    )
                ))
                .build();
    }
}
