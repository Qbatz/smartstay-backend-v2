package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensePayment;
import com.smartstay.smartstay.dto.vendor.VendorLastPayment;
import com.smartstay.smartstay.dto.vendor.VendorLastPaymentAmount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT new com.smartstay.smartstay.dto.vendor.VendorLastPayment(p.vendorId, MAX(p.paymentDate)) " +
            "FROM ExpensePayment p WHERE p.vendorId IN :vendorIds GROUP BY p.vendorId")
    List<VendorLastPayment> findLatestPaymentDates(@Param("vendorIds") List<String> vendorIds);

    @Query("SELECT new com.smartstay.smartstay.dto.vendor.VendorLastPaymentAmount(p.vendorId, p.paidAmount) " +
            "FROM ExpensePayment p WHERE p.vendorId IN :vendorIds " +
            "AND p.paymentDate = (SELECT MAX(p2.paymentDate) FROM ExpensePayment p2 WHERE p2.vendorId = p.vendorId)")
    List<VendorLastPaymentAmount> findLatestPaymentAmounts(@Param("vendorIds") List<String> vendorIds);

    @Query("SELECT p FROM ExpensePayment p WHERE p.vendorId = :vendorId " +
            "AND (:startDate IS NULL OR DATE(p.paymentDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(p.paymentDate) <= DATE(:endDate)) " +
            "ORDER BY p.paymentDate DESC")
    Page<ExpensePayment> findVendorPayments(@Param("vendorId") String vendorId,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            Pageable pageable);
}
