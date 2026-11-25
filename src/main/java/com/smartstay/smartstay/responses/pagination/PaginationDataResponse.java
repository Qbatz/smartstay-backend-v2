package com.smartstay.smartstay.responses.pagination;

public record PaginationDataResponse(int currentPage,
                                     int pageSize,
                                     long totalElements,
                                     int totalPages) {
}
