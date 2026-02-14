package com.jpmc.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search Result Data Transfer Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResultDto {

    private List<DocumentDto> documents;
    private Integer totalCount;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private Boolean hasMore;
}
