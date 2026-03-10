package com.smartstay.smartstay.payloads.expense;

public record UpdateExpense(Long categoryId,
                            Long subCategoryId,
                            String purchaseDate,
                            Integer count,
                            Double totalAmount,
                            String description) {
}
