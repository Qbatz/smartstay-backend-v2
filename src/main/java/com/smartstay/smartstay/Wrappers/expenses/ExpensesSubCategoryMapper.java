package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.ExpenseSubCategory;
import com.smartstay.smartstay.responses.expenses.ExpensesSubCategories;

import java.util.function.Function;

public class ExpensesSubCategoryMapper implements Function<ExpenseSubCategory, ExpensesSubCategories>  {
    @Override
    public ExpensesSubCategories apply(ExpenseSubCategory expenseSubCategory) {
        return new ExpensesSubCategories(expenseSubCategory.getSubCategoryName(),
                expenseSubCategory.getSubCategoryId());
    }
}
