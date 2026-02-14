package com.jpmc.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Workflow Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDto {

    private String workflowId;
    private String documentId;
    private String workflowName;
    private String status;
    private String currentStep;
    private LocalDateTime startedDate;
    private LocalDateTime completedDate;
    private String initiatedBy;
    private Map<String, Object> parameters;
    private List<WorkflowStepDto> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkflowStepDto {
        private String stepId;
        private String stepName;
        private String status;
        private String assignee;
        private String action;
        private String comment;
        private LocalDateTime completedDate;
    }
}
