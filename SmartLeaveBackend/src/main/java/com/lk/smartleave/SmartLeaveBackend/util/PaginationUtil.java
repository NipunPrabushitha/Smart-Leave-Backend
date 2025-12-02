package com.lk.smartleave.SmartLeaveBackend.util;

import com.lk.smartleave.SmartLeaveBackend.dto.LeavePaginationResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaginationUtil {

    public static <T, R> LeavePaginationResponseDTO createPaginationResponse(
            Page<T> page,
            Function<T, R> mapper,
            Class<R> responseClass) {

        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new LeavePaginationResponseDTO(
                (List<LeaveRequestDTO>) content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public static LeavePaginationResponseDTO createLeavePaginationResponse(
            Page<LeaveRequestDTO> page) {

        return new LeavePaginationResponseDTO(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}