package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

/**
 * Mobile variant of the expense listing. Keeps the same pagination envelope and summary as the web
 * response, but {@code filterOptions}, {@code tableHeaders} and {@code columnList} are always
 * {@code null} and expenses are returned as flat key-value objects ({@link ExpenseList}) instead of
 * dynamic column rows.
 */
public record ExpensesMobileResponse(int totalExpenses,
                                     int currentPage,
                                     int totalPages,
                                     int itemPerPage,
                                     ExpenseSummary expenseSummary,
                                     ExpenseFilterOptions filterOptions,
                                     List<String> tableHeaders,
                                     List<ColumnFilters> columnList,
                                     List<ExpenseList> expenses) {
}
