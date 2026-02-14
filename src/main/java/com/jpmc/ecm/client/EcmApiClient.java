package com.jpmc.ecm.client;

import com.jpmc.ecm.dto.*;
import com.jpmc.ecm.exception.EcmApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Client service for interacting with ECM REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EcmApiClient {

    private final WebClient ecmWebClient;

    private static final String CIRCUIT_BREAKER = "ecmApi";
    private static final String RETRY = "ecmApi";

    /**
     * Health check for ECM API
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<Map<String, Object>> healthCheck() {
        log.debug("Checking ECM API health");
        
        return ecmWebClient.get()
                .uri("/health")
                .retrieve()
                .onStatus(HttpStatus::isError, response -> 
                    handleErrorResponse(response.statusCode(), "Health check failed"))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(result -> log.info("ECM API health check successful"))
                .doOnError(error -> log.error("ECM API health check failed", error));
    }

    /**
     * Upload a document
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<DocumentDto> uploadDocument(
            FilePart filePart,
            String title,
            String folderId,
            Map<String, Object> metadata) {
        
        log.debug("Uploading document: {}", title);

        return ecmWebClient.post()
                .uri("/documents")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", filePart)
                        .with("title", title)
                        .with("folderId", folderId != null ? folderId : "")
                        .with("metadata", metadata != null ? metadata : Map.of()))
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to upload document"))
                .bodyToMono(DocumentDto.class)
                .doOnSuccess(doc -> log.info("Document uploaded successfully: {}", doc.getId()))
                .doOnError(error -> log.error("Failed to upload document", error));
    }

    /**
     * Get document by ID
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<DocumentDto> getDocument(String documentId) {
        log.debug("Getting document: {}", documentId);

        return ecmWebClient.get()
                .uri("/documents/{id}", documentId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), 
                        "Failed to get document: " + documentId))
                .bodyToMono(DocumentDto.class)
                .doOnSuccess(doc -> log.debug("Retrieved document: {}", documentId))
                .doOnError(error -> log.error("Failed to get document: {}", documentId, error));
    }

    /**
     * Download document content
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Flux<DataBuffer> downloadDocument(String documentId) {
        log.debug("Downloading document: {}", documentId);

        return ecmWebClient.get()
                .uri("/documents/{id}/content", documentId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), 
                        "Failed to download document: " + documentId))
                .bodyToFlux(DataBuffer.class)
                .doOnComplete(() -> log.info("Document downloaded: {}", documentId))
                .doOnError(error -> log.error("Failed to download document: {}", documentId, error));
    }

    /**
     * Delete document
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<Void> deleteDocument(String documentId) {
        log.debug("Deleting document: {}", documentId);

        return ecmWebClient.delete()
                .uri("/documents/{id}", documentId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), 
                        "Failed to delete document: " + documentId))
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Document deleted: {}", documentId))
                .doOnError(error -> log.error("Failed to delete document: {}", documentId, error));
    }

    /**
     * Search documents
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<SearchResultDto> searchDocuments(SearchRequestDto searchRequest) {
        log.debug("Searching documents with query: {}", searchRequest.getQuery());

        return ecmWebClient.post()
                .uri("/documents/search")
                .bodyValue(searchRequest)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Search failed"))
                .bodyToMono(SearchResultDto.class)
                .doOnSuccess(result -> log.info("Search returned {} results", 
                    result.getTotalCount()))
                .doOnError(error -> log.error("Search failed", error));
    }

    /**
     * Create folder
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<FolderDto> createFolder(
            String name,
            String parentId,
            String description) {
        
        log.debug("Creating folder: {}", name);

        Map<String, Object> payload = Map.of(
            "name", name,
            "parentId", parentId != null ? parentId : "",
            "description", description != null ? description : ""
        );

        return ecmWebClient.post()
                .uri("/folders")
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to create folder"))
                .bodyToMono(FolderDto.class)
                .doOnSuccess(folder -> log.info("Folder created: {}", folder.getId()))
                .doOnError(error -> log.error("Failed to create folder", error));
    }

    /**
     * Get folder contents
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<FolderDto> getFolderContents(
            String folderId,
            boolean includeDocuments,
            boolean includeSubfolders) {
        
        log.debug("Getting folder contents: {}", folderId);

        return ecmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/folders/{id}/contents")
                        .queryParam("includeDocuments", includeDocuments)
                        .queryParam("includeSubfolders", includeSubfolders)
                        .build(folderId))
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), 
                        "Failed to get folder contents: " + folderId))
                .bodyToMono(FolderDto.class)
                .doOnSuccess(folder -> log.debug("Retrieved folder contents: {}", folderId))
                .doOnError(error -> log.error("Failed to get folder contents: {}", 
                    folderId, error));
    }

    /**
     * Get document metadata
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<Map<String, Object>> getMetadata(String documentId) {
        log.debug("Getting metadata for document: {}", documentId);

        return ecmWebClient.get()
                .uri("/documents/{id}/metadata", documentId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to get metadata"))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(metadata -> log.debug("Retrieved metadata for: {}", documentId))
                .doOnError(error -> log.error("Failed to get metadata: {}", documentId, error));
    }

    /**
     * Update document metadata
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<Map<String, Object>> updateMetadata(
            String documentId,
            Map<String, Object> metadata) {
        
        log.debug("Updating metadata for document: {}", documentId);

        return ecmWebClient.patch()
                .uri("/documents/{id}/metadata", documentId)
                .bodyValue(metadata)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to update metadata"))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(m -> log.info("Metadata updated for: {}", documentId))
                .doOnError(error -> log.error("Failed to update metadata: {}", 
                    documentId, error));
    }

    /**
     * Get version history
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<List<VersionDto>> getVersions(String documentId) {
        log.debug("Getting versions for document: {}", documentId);

        return ecmWebClient.get()
                .uri("/documents/{id}/versions", documentId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to get versions"))
                .bodyToMono(new ParameterizedTypeReference<List<VersionDto>>() {})
                .doOnSuccess(versions -> log.debug("Retrieved {} versions", versions.size()))
                .doOnError(error -> log.error("Failed to get versions: {}", documentId, error));
    }

    /**
     * Start workflow
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<WorkflowDto> startWorkflow(
            String documentId,
            String workflowName,
            Map<String, Object> parameters) {
        
        log.debug("Starting workflow '{}' on document: {}", workflowName, documentId);

        Map<String, Object> payload = Map.of(
            "workflowName", workflowName,
            "documentId", documentId,
            "parameters", parameters != null ? parameters : Map.of()
        );

        return ecmWebClient.post()
                .uri("/workflows")
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), "Failed to start workflow"))
                .bodyToMono(WorkflowDto.class)
                .doOnSuccess(workflow -> log.info("Workflow started: {}", workflow.getWorkflowId()))
                .doOnError(error -> log.error("Failed to start workflow", error));
    }

    /**
     * Get workflow status
     */
    @Retry(name = RETRY)
    @CircuitBreaker(name = CIRCUIT_BREAKER)
    public Mono<WorkflowDto> getWorkflowStatus(String workflowId) {
        log.debug("Getting workflow status: {}", workflowId);

        return ecmWebClient.get()
                .uri("/workflows/{id}", workflowId)
                .retrieve()
                .onStatus(HttpStatus::isError, response ->
                    handleErrorResponse(response.statusCode(), 
                        "Failed to get workflow status: " + workflowId))
                .bodyToMono(WorkflowDto.class)
                .doOnSuccess(workflow -> log.debug("Retrieved workflow status: {}", workflowId))
                .doOnError(error -> log.error("Failed to get workflow status: {}", 
                    workflowId, error));
    }

    /**
     * Handle error responses
     */
    private Mono<? extends Throwable> handleErrorResponse(HttpStatus status, String message) {
        return Mono.error(new EcmApiException(
            status.value(),
            message + ": " + status.getReasonPhrase()
        ));
    }
}
