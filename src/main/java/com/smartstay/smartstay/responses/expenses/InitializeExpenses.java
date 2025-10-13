package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.List;

public record InitializeExpenses(List<ExpensesCategory> listExpenses,
                                 List<DebitsBank> banks) {
}
