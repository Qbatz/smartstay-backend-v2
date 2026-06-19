package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public record ExpensesWebResponse(int totalExpenses,
                                  int currentPage,
                                  int totalPages,
                                  int itemPerPage,
                                  ExpenseSummary expenseSummary,
                                  ExpenseFilterOptions filterOptions,
                                  List<String> tableHeaders,
                                  List<ColumnFilters> columnList,
                                  List<List<Object>> expenses) {
}
