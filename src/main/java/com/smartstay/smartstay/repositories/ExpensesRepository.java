package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.dto.expenses.ExpenseList;
import com.smartstay.smartstay.dto.expenses.ExpenseSummaryProjection;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ExpensesRepository extends JpaRepository<ExpensesV1, String> {
    ExpensesV1 findByExpenseNumberAndHostelId(String expenseNumber, String hostelId);

    @Query("SELECT COALESCE(SUM(e.totalPrice), 0) FROM ExpensesV1 e " +
            "WHERE e.vendorId = :vendorId AND e.isActive = true " +
            "AND (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate))")
    Double sumVendorExpense(@Param("vendorId") String vendorId,
                            @Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    /**
     * Active, outstanding expenses for a vendor — i.e. only those still eligible for settlement.
     * Excludes fully-settled expenses (paymentStatus = Full) and those with a zero balance,
     * leaving Pending / Partial / Overdue. Filtering is applied at the query level.
     */
    @Query("SELECT e FROM ExpensesV1 e " +
            "WHERE e.vendorId = :vendorId AND e.isActive = true " +
            "AND COALESCE(e.balanceAmount, 0) <> 0 " +
            "AND (e.paymentStatus IS NULL OR e.paymentStatus <> :fullStatus) " +
            "ORDER BY e.transactionDate DESC")
    List<ExpensesV1> findOutstandingExpensesByVendorId(@Param("vendorId") String vendorId,
                                                       @Param("fullStatus") ExpensePaymentStatus fullStatus);

    @Query("SELECT COUNT(e) FROM ExpensesV1 e " +
            "WHERE e.vendorId = :vendorId AND e.isActive = true " +
            "AND (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate))")
    long countVendorExpense(@Param("vendorId") String vendorId,
                            @Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    /**
     * Efficient existence check used to guard vendor deletion. Spring Data issues a lightweight
     * EXISTS/LIMIT 1 query (no rows loaded) and the leading column of the (vendor_id, transaction_date)
     * index serves the vendor_id predicate. Matches any expense row for the vendor, active or not.
     */
    boolean existsByVendorId(String vendorId);

    /**
     * Efficient existence check across a set of vendor ids (e.g. all vendors in a category). Spring Data
     * issues a single EXISTS/LIMIT 1 query backed by the (vendor_id, ...) index — no rows are loaded and
     * there is no N+1. Matches any expense row, active or not. Call only with a non-empty collection.
     */
    boolean existsByVendorIdIn(java.util.Collection<String> vendorIds);

    /**
     * Month-wise expense aggregate for a vendor within a date range. Counts use mutually exclusive,
     * exhaustive buckets (they sum to totalExpenseCount):
     *   fullPaid = paymentStatus 'Full' OR balance 0
     *   partial  = paymentStatus 'Partial' AND balance <> 0
     *   unpaid   = everything else (Pending / NULL / Overdue with a balance)
     * Amounts:
     *   totalFullPaidAmount    = SUM(paid_amount)   over the fullPaid bucket
     *   totalPartialPaidAmount = SUM(paid_amount)   over the partial bucket
     *   totalUnpaidAmount      = SUM(total_price)   over the unpaid bucket
     *   totalAmount/balanceAmount/paidAmount = SUM(total_price/balance_amount/paid_amount) over all rows
     * Returns one row per year+month that has expenses; missing months are zero-filled in the service.
     */
    @Query(value = """
            SELECT YEAR(e.transaction_date)  AS expenseYear,
                   MONTH(e.transaction_date) AS expenseMonth,
                   COUNT(*)                  AS totalExpenseCount,
                   SUM(CASE WHEN COALESCE(e.payment_status,'') = 'Full' OR COALESCE(e.balance_amount,0) = 0
                            THEN 1 ELSE 0 END) AS totalFullPaidCount,
                   SUM(CASE WHEN COALESCE(e.payment_status,'') = 'Partial' AND COALESCE(e.balance_amount,0) <> 0
                            THEN 1 ELSE 0 END) AS totalPartialPaidCount,
                   SUM(CASE WHEN NOT (COALESCE(e.payment_status,'') = 'Full' OR COALESCE(e.balance_amount,0) = 0)
                             AND NOT (COALESCE(e.payment_status,'') = 'Partial' AND COALESCE(e.balance_amount,0) <> 0)
                            THEN 1 ELSE 0 END) AS totalUnpaidCount,
                   COALESCE(SUM(CASE WHEN COALESCE(e.payment_status,'') = 'Full' OR COALESCE(e.balance_amount,0) = 0
                                     THEN COALESCE(e.paid_amount,0) ELSE 0 END), 0) AS totalFullPaidAmount,
                   COALESCE(SUM(CASE WHEN COALESCE(e.payment_status,'') = 'Partial' AND COALESCE(e.balance_amount,0) <> 0
                                     THEN COALESCE(e.paid_amount,0) ELSE 0 END), 0) AS totalPartialPaidAmount,
                   COALESCE(SUM(CASE WHEN NOT (COALESCE(e.payment_status,'') = 'Full' OR COALESCE(e.balance_amount,0) = 0)
                                      AND NOT (COALESCE(e.payment_status,'') = 'Partial' AND COALESCE(e.balance_amount,0) <> 0)
                                     THEN COALESCE(e.total_price,0) ELSE 0 END), 0) AS totalUnpaidAmount,
                   COALESCE(SUM(COALESCE(e.total_price,0)),    0) AS totalAmount,
                   COALESCE(SUM(COALESCE(e.balance_amount,0)), 0) AS balanceAmount,
                   COALESCE(SUM(COALESCE(e.paid_amount,0)),    0) AS paidAmount
            FROM expensesv1 e
            WHERE e.vendor_id = :vendorId AND e.is_active = true
              AND DATE(e.transaction_date) >= DATE(:startDate)
              AND DATE(e.transaction_date) <= DATE(:endDate)
            GROUP BY YEAR(e.transaction_date), MONTH(e.transaction_date)
            """, nativeQuery = true)
    List<com.smartstay.smartstay.dto.vendor.VendorMonthSummaryProjection> findVendorMonthlyExpenseSummary(
            @Param("vendorId") String vendorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query(value = """
            SELECT exp.expense_id as expenseId, exp.unit_count as noOfItems, exp.category_id as categoryId,
            exp.description, exp.hostel_id as hostelId, exp.bank_id as bankId, exp.sub_category_id as subCategoryId,
            exp.total_price as totalAmount, exp.actual_total_price as actualTotalPrice,
            exp.transaction_amount as transactionAmount, exp.transaction_date as transactionDate,
            exp.unit_price as unitPrice, exp.vendor_id as vendorId, exp.expense_number as referenceNumber,
            exp.title as title, exp.is_vendor_expense as isVendorExpense, exp.payment_status as paymentStatus,
            exp.paid_amount as paidAmount, exp.balance_amount as balanceAmount, exp.payment_method as paymentMethod, exp.note as note,
            exp.created_at as createdAt, exp.credit_period as creditPeriod,
            banking.account_holder_name as holderName, banking.account_type as accountType, banking.bank_name as bankName, expCat.category_name as categoryName,
            expSub.sub_category_name as subCategoryName  FROM expensesv1 exp LEFT OUTER JOIN
            bankingv1 banking on banking.bank_id=exp.bank_id LEFT OUTER JOIN expense_category expCat on expCat.category_id=exp.category_id
            LEFT OUTER JOIN expense_sub_category expSub on expSub.sub_category_id=exp.sub_category_id WHERE exp.hostel_id=:hostelId and exp.is_active=true;
            """, nativeQuery = true)
    List<ExpenseList> findAllExpensesByHostelId(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT exp.expense_id as expenseId, exp.unit_count as noOfItems, exp.category_id as categoryId,
            exp.description, exp.hostel_id as hostelId, exp.bank_id as bankId, exp.sub_category_id as subCategoryId,
            exp.total_price as totalAmount, exp.actual_total_price as actualTotalPrice,
            exp.transaction_amount as transactionAmount, exp.transaction_date as transactionDate,
            exp.unit_price as unitPrice, exp.vendor_id as vendorId, exp.expense_number as referenceNumber,
            exp.title as title, exp.is_vendor_expense as isVendorExpense, exp.payment_status as paymentStatus,
            exp.paid_amount as paidAmount, exp.balance_amount as balanceAmount, exp.payment_method as paymentMethod, exp.note as note,
            exp.created_at as createdAt, exp.credit_period as creditPeriod,
            banking.account_holder_name as holderName, banking.account_type as accountType, banking.bank_name as bankName, expCat.category_name as categoryName,
            expSub.sub_category_name as subCategoryName FROM expensesv1 exp
            LEFT OUTER JOIN bankingv1 banking on banking.bank_id=exp.bank_id
            LEFT OUTER JOIN expense_category expCat on expCat.category_id=exp.category_id
            LEFT OUTER JOIN expense_sub_category expSub on expSub.sub_category_id=exp.sub_category_id
            WHERE exp.vendor_id=:vendorId AND exp.is_active=true
            AND (:search IS NULL OR exp.expense_number LIKE CONCAT('%', :search, '%'))
            AND (:startDate IS NULL OR DATE(exp.transaction_date) >= DATE(:startDate))
            AND (:endDate IS NULL OR DATE(exp.transaction_date) <= DATE(:endDate))
            ORDER BY exp.transaction_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM expensesv1 exp
            WHERE exp.vendor_id=:vendorId AND exp.is_active=true
            AND (:search IS NULL OR exp.expense_number LIKE CONCAT('%', :search, '%'))
            AND (:startDate IS NULL OR DATE(exp.transaction_date) >= DATE(:startDate))
            AND (:endDate IS NULL OR DATE(exp.transaction_date) <= DATE(:endDate))
            """,
            nativeQuery = true)
    org.springframework.data.domain.Page<ExpenseList> findVendorExpenses(@Param("vendorId") String vendorId,
                                                                          @Param("search") String search,
                                                                          @Param("startDate") Date startDate,
                                                                          @Param("endDate") Date endDate,
                                                                          Pageable pageable);

    @Query(value = """
            SELECT exp.expense_id as expenseId, exp.unit_count as noOfItems, exp.category_id as categoryId,
            exp.description, exp.hostel_id as hostelId, exp.bank_id as bankId, exp.sub_category_id as subCategoryId,
            exp.total_price as totalAmount, exp.actual_total_price as actualTotalPrice,
            exp.transaction_amount as transactionAmount, exp.transaction_date as transactionDate,
            exp.unit_price as unitPrice, exp.vendor_id as vendorId, exp.expense_number as referenceNumber,
            exp.title as title, exp.is_vendor_expense as isVendorExpense, exp.payment_status as paymentStatus,
            exp.paid_amount as paidAmount, exp.balance_amount as balanceAmount, exp.payment_method as paymentMethod, exp.note as note,
            exp.created_at as createdAt, exp.credit_period as creditPeriod,
            banking.account_holder_name as holderName, banking.account_type as accountType, banking.bank_name as bankName, expCat.category_name as categoryName,
            expSub.sub_category_name as subCategoryName FROM expensesv1 exp
            LEFT OUTER JOIN bankingv1 banking on banking.bank_id=exp.bank_id
            LEFT OUTER JOIN expense_category expCat on expCat.category_id=exp.category_id
            LEFT OUTER JOIN expense_sub_category expSub on expSub.sub_category_id=exp.sub_category_id
            WHERE exp.hostel_id=:hostelId AND exp.is_active=true
            AND (:name IS NULL OR exp.title LIKE CONCAT('%', :name, '%') OR exp.expense_number LIKE CONCAT('%', :name, '%') OR exp.expense_id LIKE CONCAT('%', :name, '%'))
            AND (:categoryId IS NULL OR exp.category_id=:categoryId)
            AND (:paymentStatus IS NULL OR exp.payment_status = :paymentStatus)
            AND (:paymentDate IS NULL OR DATE(exp.created_at) = DATE(:paymentDate))
            ORDER BY exp.transaction_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM expensesv1 exp
            WHERE exp.hostel_id=:hostelId AND exp.is_active=true
            AND (:name IS NULL OR exp.title LIKE CONCAT('%', :name, '%') OR exp.expense_number LIKE CONCAT('%', :name, '%') OR exp.expense_id LIKE CONCAT('%', :name, '%'))
            AND (:categoryId IS NULL OR exp.category_id=:categoryId)
            AND (:paymentStatus IS NULL OR exp.payment_status = :paymentStatus)
            AND (:paymentDate IS NULL OR DATE(exp.created_at) = DATE(:paymentDate))
            """,
            nativeQuery = true)
    org.springframework.data.domain.Page<ExpenseList> findExpensesForHostel(@Param("hostelId") String hostelId,
                                                                             @Param("name") String name,
                                                                             @Param("categoryId") Long categoryId,
                                                                             @Param("paymentStatus") String paymentStatus,
                                                                             @Param("paymentDate") Date paymentDate,
                                                                             Pageable pageable);

    @Query(value = """
            SELECT
              COALESCE(SUM(exp.total_price), 0) as totalExpenseAmount,
              COALESCE(SUM(CASE WHEN exp.payment_status = 'Full' THEN exp.total_price ELSE 0 END), 0) as totalPaidAmount,
              COALESCE(SUM(COALESCE(exp.balance_amount, 0)), 0) as totalUnPaidAmount,
              COALESCE(SUM(CASE WHEN exp.payment_status = 'Partial' THEN COALESCE(exp.paid_amount, 0) ELSE 0 END), 0) as totalPartialPaidAmount
            FROM expensesv1 exp
            WHERE exp.hostel_id=:hostelId AND exp.is_active=true
            AND (:name IS NULL OR exp.title LIKE CONCAT('%', :name, '%') OR exp.expense_number LIKE CONCAT('%', :name, '%') OR exp.expense_id LIKE CONCAT('%', :name, '%'))
            AND (:categoryId IS NULL OR exp.category_id=:categoryId)
            AND (:paymentStatus IS NULL OR exp.payment_status = :paymentStatus)
            AND (:paymentDate IS NULL OR DATE(exp.created_at) = DATE(:paymentDate))
            """, nativeQuery = true)
    com.smartstay.smartstay.dto.expenses.ExpenseSummaryView getExpenseListSummary(@Param("hostelId") String hostelId,
                                                                                  @Param("name") String name,
                                                                                  @Param("categoryId") Long categoryId,
                                                                                  @Param("paymentStatus") String paymentStatus,
                                                                                  @Param("paymentDate") Date paymentDate);

    @Modifying
    @Query("UPDATE ExpensesV1 e SET e.paymentStatus = :status WHERE e.expenseId IN :expenseIds")
    void updatePaymentStatus(@Param("expenseIds") List<String> expenseIds, @Param("status") ExpensePaymentStatus status);
    @Query("SELECT COUNT(e) FROM ExpensesV1 e WHERE e.hostelId = :hostelId AND e.isActive = true AND DATE(e.transactionDate) >= DATE(:startDate) AND DATE(e.transactionDate) <= DATE(:endDate)")
    int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

        @Query("SELECT COALESCE(SUM(e.totalPrice), 0) FROM ExpensesV1 e WHERE e.hostelId = :hostelId AND e.isActive = true AND DATE(e.transactionDate) >= DATE(:startDate) AND DATE(e.transactionDate) <= DATE(:endDate)")
        Double sumAmountByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

        @Query("SELECT e FROM ExpensesV1 e " +
                "WHERE e.hostelId = :hostelId " +
                "AND e.isActive = true " +
                "AND (:categoryIds IS NULL OR e.categoryId IN :categoryIds) " +
                "AND (:subCategoryIds IS NULL OR e.subCategoryId IN :subCategoryIds) " +
                "AND (:bankIds IS NULL OR e.bankId IN :bankIds) " +
                "AND (:vendorIds IS NULL OR e.vendorId IN :vendorIds) " +
                "AND (:createdByList IS NULL OR e.createdBy IN :createdByList) " +
                "AND (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) " +
                "AND (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate)) " +
                "ORDER BY e.transactionDate DESC")
        List<ExpensesV1> findExpensesWithFiltersV2(
                @Param("hostelId") String hostelId,
                @Param("categoryIds") List<Long> categoryIds,
                @Param("subCategoryIds") List<Long> subCategoryIds,
                @Param("bankIds") List<String> bankIds,
                @Param("vendorIds") List<String> vendorIds,
                @Param("createdByList") List<String> createdByList,
                @Param("startDate") Date startDate,
                @Param("endDate") Date endDate,
                Pageable pageable);

        @Query("SELECT COUNT(e) as totalRecords, COALESCE(SUM(e.transactionAmount), 0) as totalAmount " +
                "FROM ExpensesV1 e " +
                "WHERE e.hostelId = :hostelId " +
                "AND e.isActive = true " +
                "AND (:categoryIds IS NULL OR e.categoryId IN :categoryIds) " +
                "AND (:subCategoryIds IS NULL OR e.subCategoryId IN :subCategoryIds) " +
                "AND (:bankIds IS NULL OR e.bankId IN :bankIds) " +
                "AND (:vendorIds IS NULL OR e.vendorId IN :vendorIds) " +
                "AND (:createdByList IS NULL OR e.createdBy IN :createdByList) " +
                "AND (:startDate IS NULL OR DATE(e.transactionDate) >= DATE(:startDate)) " +
                "AND (:endDate IS NULL OR DATE(e.transactionDate) <= DATE(:endDate))")
        ExpenseSummaryProjection getExpenseSummary(
                @Param("hostelId") String hostelId,
                @Param("categoryIds") List<Long> categoryIds,
                @Param("subCategoryIds") List<Long> subCategoryIds,
                @Param("bankIds") List<String> bankIds,
                @Param("vendorIds") List<String> vendorIds,
                @Param("createdByList") List<String> createdByList,
                @Param("startDate") Date startDate,
                @Param("endDate") Date endDate);

        List<ExpensesV1> findByHostelIdAndIsActiveTrue(String hostelId);

    @Query("SELECT e FROM ExpensesV1 e WHERE e.hostelId = :hostelId AND e.isActive = true AND DATE(e.transactionDate) >= DATE(:startDate) AND DATE(e.transactionDate) <= DATE(:endDate)")
    List<ExpensesV1> findByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
