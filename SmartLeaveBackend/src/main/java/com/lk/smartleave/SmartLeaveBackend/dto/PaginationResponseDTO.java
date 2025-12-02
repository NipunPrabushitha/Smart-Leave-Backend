package com.lk.smartleave.SmartLeaveBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // Helper method to create response from Spring Page
    public static <T> PaginationResponseDTO<T> of(
            List<T> content,
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last) {
        return new PaginationResponseDTO<>(
                content, currentPage, pageSize,
                totalElements, totalPages, first, last
        );
    }
}