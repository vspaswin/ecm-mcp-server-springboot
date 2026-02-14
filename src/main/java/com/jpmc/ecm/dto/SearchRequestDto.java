package com.jpmc.ecm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Search Request Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    private String query;
    private String folderId;
    private String documentType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> tags;
    private Map<String, Object> metadata;
    private Integer maxResults;
    private Integer offset;
    private String sortBy;
    private String sortOrder;
}
