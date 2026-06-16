package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {

    List<ExpenseItem> findByExpenseId(String expenseId);

    List<ExpenseItem> findByExpenseIdIn(List<String> expenseIds);

    void deleteByExpenseId(String expenseId);
}
