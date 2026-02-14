package com.jpmc.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Document Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDto {

    private String id;
    private String title;
    private String fileName;
    private String mimeType;
    private Long size;
    private String folderId;
    private String folderPath;
    private String author;
    private String documentType;
    private String status;
    private Map<String, Object> metadata;
    private String version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private String checkoutStatus;
    private String checkoutBy;
}
