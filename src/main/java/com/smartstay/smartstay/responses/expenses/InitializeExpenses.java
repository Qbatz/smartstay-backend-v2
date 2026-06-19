package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.responses.vendor.VendorInitializeResponse;

import java.util.List;

public record InitializeExpenses(String hostelId,
                                 List<ExpensesCategory> listExpenses,
                                 List<DebitsBank> banks,
                                 List<VendorInitializeResponse> vendor) {
}
