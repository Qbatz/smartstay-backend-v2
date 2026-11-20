package com.smartstay.smartstay.responses.pagination;

public record PaginationDataRes(int currentPage,
                                int pageSize,
                                long totalElements,
                                int totalPages) {
}
