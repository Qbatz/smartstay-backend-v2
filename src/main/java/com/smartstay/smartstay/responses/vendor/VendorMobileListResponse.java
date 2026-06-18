package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

/**
 * Mobile variant of the vendor listing. Keeps the same pagination envelope and summary as the web
 * response, but {@code filterOptions}, {@code tableHeaders} and {@code columnList} are always
 * {@code null} and vendors are returned as flat key-value objects instead of dynamic column rows.
 */
public record VendorMobileListResponse(int totalVendors,
                                       int currentPage,
                                       int totalPages,
                                       int itemPerPage,
                                       VendorSummary vendorSummary,
                                       VendorFilterOptions filterOptions,
                                       List<String> tableHeaders,
                                       List<ColumnFilters> columnList,
                                       List<VendorMobileResponse> vendors) {
}
