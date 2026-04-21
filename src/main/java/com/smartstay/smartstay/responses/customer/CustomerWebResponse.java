package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public record CustomerWebResponse(int totalCustomers,
                                  int currentPage,
                                  int totalPages,
                                  int itemPerPage,
                                  List<String> tableHeaders,
                                  List<ColumnFilters> columnList,
                                  List<List<Object>> tenants) {
}
