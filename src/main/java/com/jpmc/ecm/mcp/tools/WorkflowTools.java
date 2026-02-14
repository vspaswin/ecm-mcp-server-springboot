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
 * MCP tools for workflow operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowTools {

    private final EcmApiClient ecmApiClient;

    public List<ToolInfo> getToolDefinitions() {
        return List.of(
            startWorkflowToolInfo(),
            getWorkflowStatusToolInfo()
        );
    }

    public Mono<Object> executeTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "ecm_start_workflow" -> startWorkflow(params);
            case "ecm_get_workflow_status" -> getWorkflowStatus(params);
            default -> Mono.error(new IllegalArgumentException("Unknown tool: " + toolName));
        };
    }

    @SuppressWarnings("unchecked")
    private Mono<Object> startWorkflow(Map<String, Object> params) {
        String documentId = (String) params.get("documentId");
        String workflowName = (String) params.get("workflowName");
        Map<String, Object> parameters = (Map<String, Object>) params.get("parameters");
        
        if (documentId == null || documentId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("documentId is required"));
        }
        if (workflowName == null || workflowName.isEmpty()) {
            return Mono.error(new IllegalArgumentException("workflowName is required"));
        }

        log.info("Executing ecm_start_workflow: workflow='{}', documentId='{}'", 
            workflowName, documentId);
        return ecmApiClient.startWorkflow(documentId, workflowName, parameters).cast(Object.class);
    }

    private Mono<Object> getWorkflowStatus(Map<String, Object> params) {
        String workflowId = (String) params.get("workflowId");
        
        if (workflowId == null || workflowId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("workflowId is required"));
        }

        log.info("Executing ecm_get_workflow_status: {}", workflowId);
        return ecmApiClient.getWorkflowStatus(workflowId).cast(Object.class);
    }

    private ToolInfo startWorkflowToolInfo() {
        return ToolInfo.builder()
            .name("ecm_start_workflow")
            .description("Start a workflow on a document")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "documentId", Map.of(
                        "type", "string",
                        "description", "Document ID"
                    ),
                    "workflowName", Map.of(
                        "type", "string",
                        "description", "Name of the workflow to start"
                    ),
                    "parameters", Map.of(
                        "type", "object",
                        "description", "Optional workflow parameters"
                    )
                ),
                "required", List.of("documentId", "workflowName")
            ))
            .build();
    }

    private ToolInfo getWorkflowStatusToolInfo() {
        return ToolInfo.builder()
            .name("ecm_get_workflow_status")
            .description("Get the status of a workflow instance")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "workflowId", Map.of(
                        "type", "string",
                        "description", "Workflow instance ID"
                    )
                ),
                "required", List.of("workflowId")
            ))
            .build();
    }
}
