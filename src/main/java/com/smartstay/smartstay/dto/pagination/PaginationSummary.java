package com.smartstay.smartstay.dto.pagination;

public record PaginationSummary(Integer totalPages,
                                Integer totalItems,
                                Integer currentPage,
                                Integer pageSize) {
}
