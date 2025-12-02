package com.lk.smartleave.SmartLeaveBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequestDTO {
    private int page = 0;           // Default page (0-indexed)
    private int size = 5;          // Default page size
    private String sortBy = "id";   // Default sort field
    private String sortDir = "desc"; // Default sort direction

    public boolean isAscending() {
        return "asc".equalsIgnoreCase(sortDir);
    }
}