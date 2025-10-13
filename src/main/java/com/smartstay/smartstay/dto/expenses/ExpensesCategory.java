package com.smartstay.smartstay.dto.expenses;

import java.util.List;

public record ExpensesCategory(Long categoryId,
                               String categoryName,
                               List<ExpensesSubCategory> subCategories) {
}
