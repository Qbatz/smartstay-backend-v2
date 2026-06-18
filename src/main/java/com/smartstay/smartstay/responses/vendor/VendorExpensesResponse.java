package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.responses.expenses.ExpenseList;

import java.util.List;

public record VendorExpensesResponse(
        long totalExpenses,
        int currentPage,
        int totalPages,
        int itemPerPage,
        List<ExpenseList> expenses) {
}
