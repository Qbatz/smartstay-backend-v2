package com.smartstay.smartstay.responses.expenses;

public record ExpenseList(String expenseId,
                          int itemsCount,
                          long categoryId,
                          long subCategoryId,
                          String description,
                          String hostelId,
                          String bankId,
                          Double totalAmount,
                          String transactionDate,
                          Double unitPrice,
                          String vendorId,
                          String referenceNumber,
                          String accountHolderName,
                          String bankName,
                          String categoryName,
                          String subCategoryName) {
}
