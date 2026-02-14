package com.jpmc.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Folder Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderDto {

    private String id;
    private String name;
    private String path;
    private String parentId;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private Integer documentCount;
    private Integer subfolderCount;
    private List<FolderDto> subfolders;
    private List<DocumentDto> documents;
}
