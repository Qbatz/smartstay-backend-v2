package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {

    List<ExpensePayment> findByExpenseId(String expenseId);

    List<ExpensePayment> findByExpenseIdIn(List<String> expenseIds);

    void deleteByExpenseId(String expenseId);
}
