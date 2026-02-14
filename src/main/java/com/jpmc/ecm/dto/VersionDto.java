package com.jpmc.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Version Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionDto {

    private String versionId;
    private String documentId;
    private String versionNumber;
    private String comment;
    private Boolean isMajor;
    private String createdBy;
    private LocalDateTime createdDate;
    private Long size;
    private String checksum;
    private Boolean isCurrent;
}
