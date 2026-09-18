package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.responses.expenses.ExpenseFilterOptions;

import java.util.List;

public record VendorExpenseFilterOptions(List<ExpenseFilterOptions.FilterItems> status,
                                         List<ExpenseFilterOptions.FilterItems> category) {
}
