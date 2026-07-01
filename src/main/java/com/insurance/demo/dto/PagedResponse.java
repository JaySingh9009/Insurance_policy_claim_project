package com.insurance.demo.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> records;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean isLastPage;
}
