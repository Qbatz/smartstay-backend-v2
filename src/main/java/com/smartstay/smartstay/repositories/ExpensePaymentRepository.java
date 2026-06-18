package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {

    List<ExpensePayment> findByExpenseId(String expenseId);

    List<ExpensePayment> findByExpenseIdIn(List<String> expenseIds);

    void deleteByExpenseId(String expenseId);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM ExpensePayment p WHERE p.expenseId = :expenseId")
    Double sumPaidAmountByExpenseId(@Param("expenseId") String expenseId);

    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM ExpensePayment p " +
            "WHERE p.vendorId = :vendorId " +
            "AND (:startDate IS NULL OR DATE(p.paymentDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(p.paymentDate) <= DATE(:endDate))")
    Double sumVendorPaid(@Param("vendorId") String vendorId,
                         @Param("startDate") Date startDate,
                         @Param("endDate") Date endDate);

    @Query("SELECT COUNT(p) FROM ExpensePayment p " +
            "WHERE p.vendorId = :vendorId " +
            "AND (:startDate IS NULL OR DATE(p.paymentDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(p.paymentDate) <= DATE(:endDate))")
    long countVendorPayments(@Param("vendorId") String vendorId,
                             @Param("startDate") Date startDate,
                             @Param("endDate") Date endDate);
}
