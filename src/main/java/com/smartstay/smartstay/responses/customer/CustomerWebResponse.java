package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dto.customer.Summary;
import com.smartstay.smartstay.filterOptions.customers.FilterOptions;

import java.util.List;

public record CustomerWebResponse(int totalCustomers,
                                  int currentPage,
                                  int totalPages,
                                  int itemPerPage,
                                  Summary tenantSummary,
                                  FilterOptions filterOptions,
                                  List<String> tableHeaders,
                                  List<ColumnFilters> columnList,
                                  List<List<Object>> tenants) {
}
