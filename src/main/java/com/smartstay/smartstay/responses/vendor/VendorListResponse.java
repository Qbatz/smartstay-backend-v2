package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public record VendorListResponse(int totalVendors,
                                 int currentPage,
                                 int totalPages,
                                 int itemPerPage,
                                 VendorSummary vendorSummary,
                                 VendorFilterOptions filterOptions,
                                 List<String> tableHeaders,
                                 List<ColumnFilters> columnList,
                                 List<List<Object>> vendors) {
}
