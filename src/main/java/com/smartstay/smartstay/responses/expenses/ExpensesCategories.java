package com.smartstay.smartstay.responses.expenses;

import java.util.List;

public record ExpensesCategories(String categoryName,
                                 Long categoryId,
                                 List<ExpensesSubCategories> listSubcategories) {
}
