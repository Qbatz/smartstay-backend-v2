package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.ExpenseCategory;
import com.smartstay.smartstay.responses.expenses.ExpensesCategories;
import com.smartstay.smartstay.responses.expenses.ExpensesSubCategories;

import java.util.List;
import java.util.function.Function;

public class ExpensesCategoryMapper implements Function<ExpenseCategory, ExpensesCategories> {
    private List<ExpensesSubCategories> listSubcategories = null;
    public ExpensesCategoryMapper(List<ExpensesSubCategories> listSubcategories) {
        this.listSubcategories = listSubcategories;
    }

    @Override
    public ExpensesCategories apply(ExpenseCategory category) {
        return new ExpensesCategories(category.getCategoryName(),
                category.getCategoryId(),
                listSubcategories);
    }
}
