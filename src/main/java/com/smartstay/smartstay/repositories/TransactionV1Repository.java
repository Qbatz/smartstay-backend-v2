package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;
import com.smartstay.smartstay.dto.transaction.Receipts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionV1Repository extends JpaRepository<TransactionV1, String> {

    List<TransactionV1> findByCustomerId(String customerId);

    List<TransactionV1> findByInvoiceId(String invoiceId);

    List<TransactionV1> findByInvoiceIdIn(List<String> invoiceId);
    List<TransactionV1> findByHostelIdAndInvoiceId(String hostelId, String invoiceId);
    TransactionV1 findByHostelIdAndTransactionId(String hostelId, String transactionId);
    TransactionV1 findByTransactionId(String transactionId);

    @Query("SELECT COALESCE(SUM(t.paidAmount), 0) FROM TransactionV1 t WHERE t.invoiceId = :invoiceId")
    Double getTotalPaidAmountByInvoiceId(@Param("invoiceId") String invoiceId);

    @Query(value = """
            SELECT invc.invoice_id as invoiceId, invc.invoice_number as invoiceNumber,
            invc.invoice_mode as invoiceMode, invc.invoice_type as invoiceType, transaction.status as paymentStatus,
            transaction.type as transactionType, transaction.transaction_reference_id as transactionNo,
            transaction.paid_amount as paidAmount, transaction.payment_date as paidAt,
            transaction.transaction_id as transactionId, customers.customer_id as customerId, customers.first_name as firstName, customers.last_name as lastName,
            transaction.reference_number as referenceNumber, transaction.bank_id as bankId, bank.bank_name as bankName,
            bank.account_holder_name as holderName, bank.account_type as accountType, customers.profile_pic as profilePic FROM transactionv1 transaction
            LEFT OUTER JOIN invoicesv1 invc on invc.invoice_id = transaction.invoice_id LEFT OUTER JOIN
            customers customers on customers.customer_id=transaction.customer_id LEFT OUTER JOIN
            bankingv1 bank on bank.bank_id=transaction.bank_id WHERE transaction.hostel_id=:hostelId
            """, nativeQuery = true)
    List<Receipts> findAllByHostelId(@Param("hostelId") String hostelId);

    List<TransactionV1> findByHostelId(String hostelId);

    boolean existsByTransactionReferenceId(String transactionReferenceId);

    @Query(value = """
                SELECT
                    reference_number AS referenceNumber,
                    paid_amount AS amount,
                    DATE_FORMAT(paid_at, '%d/%m/%Y') AS paidDate,
                    transaction_reference_id as transactionReferenceId
                FROM
                    transactionv1
                WHERE
                    invoice_id = :invoiceId
                ORDER BY
                    paid_at DESC
            """, nativeQuery = true)
    List<PaymentHistoryProjection> getPaymentHistoryByInvoiceId(@Param("invoiceId") String invoiceId);

    @Query("SELECT COUNT(t) FROM TransactionV1 t WHERE t.hostelId = :hostelId AND DATE(t.paidAt) >= DATE(:startDate) AND DATE(t.paidAt) <= DATE(:endDate)")
    int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COALESCE(SUM(t.paidAmount), 0) FROM TransactionV1 t WHERE t.hostelId = :hostelId AND DATE(t.paidAt) >= DATE(:startDate) AND DATE(t.paidAt) <= DATE(:endDate)")
    Double sumPaidAmountByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT DISTINCT t.createdBy FROM TransactionV1 t WHERE t.hostelId = :hostelId")
    List<String> findDistinctCreatedByByHostelId(@Param("hostelId") String hostelId);

    @Query(value = "SELECT t FROM TransactionV1 t WHERE t.hostelId = :hostelId " +
            "AND (:startDate IS NULL OR DATE(t.paymentDate) >= DATE(:startDate)) AND (:endDate IS NULL OR DATE(t.paymentDate) <= DATE(:endDate)) " +
            "AND (:bankIds IS NULL OR t.bankId IN :bankIds) AND (:userIds IS NULL OR t.createdBy IN :userIds) " +
            "AND (:invoiceIds IS NULL OR t.invoiceId IN :invoiceIds)")
    List<TransactionV1> findTransactionsByFiltersNew(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("bankIds") List<String> bankIds, @Param("userIds") List<String> userIds, @Param("invoiceIds") List<String> invoiceIds, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(t) FROM TransactionV1 t WHERE t.hostelId = :hostelId " + "AND (:startDate IS NULL OR t.paidAt >= :startDate) " + "AND (:endDate IS NULL OR t.paidAt <= :endDate) " + "AND (:bankIds IS NULL OR t.bankId IN :bankIds) " + "AND (:userIds IS NULL OR t.createdBy IN :userIds) " + "AND (:invoiceIds IS NULL OR t.invoiceId IN :invoiceIds)")
    long countTransactionsByFiltersNew(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("bankIds") List<String> bankIds, @Param("userIds") List<String> userIds, @Param("invoiceIds") List<String> invoiceIds);

    @Query(value = """
    SELECT t FROM TransactionV1 t WHERE t.hostelId = :hostelId AND (:startDate IS NULL OR DATE(t.paymentDate) >= DATE(:startDate)) AND (:endDate IS NULL OR DATE(t.paymentDate) <= DATE(:endDate))  
    AND (:bankIds IS NULL OR t.bankId IN :bankIds) AND (:userIds IS NULL OR t.createdBy IN :userIds) AND (:invoiceIds IS NULL OR t.invoiceId IN :invoiceIds)""")
    List<TransactionV1> sumPaidAmountByFiltersNew(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("bankIds") List<String> bankIds, @Param("userIds") List<String> userIds, @Param("invoiceIds") List<String> invoiceIds);

    @Query(value = """
            SELECT * FROM transactionv1 transactionv1 where transactionv1.transaction_id = (SELECT trv1.transaction_id FROM transactionv1 trv1 
                  where trv1.invoice_id=transactionv1.invoice_id ORDER BY trv1.payment_date DESC LIMIT 1) and transactionv1.hostel_id=:hostelId and transactionv1.invoice_id in (:invoiceIds)
            """, nativeQuery = true)
    List<TransactionV1> findLatestTransactionByInvoicesId(@Param("hostelId") String hostelId, @Param("invoiceIds") List<String> invoiceIds);

    @Query(value = """
            SELECT trns FROM TransactionV1 trns WHERE trns.hostelId=:hostelId AND 
            ((:customerIds IS NULL AND :invoiceIds IS NULL) OR (:customerIds IS NOT NULL AND trns.customerId IN :customerIds) OR (:invoiceIds IS NOT NULL AND trns.invoiceId IN :invoiceIds)) AND 
            (:bankIds IS NULL OR trns.bankId IN :bankIds) AND (:startDate IS NULL OR DATE(trns.paymentDate) >= DATE(:startDate)) AND 
            (:endDate IS NULL OR DATE(trns.paymentDate) <= DATE(:endDate)) AND 
            (:collectedBy IS NULL OR trns.createdBy IN :collectedBy) AND (:minAmount IS NULL OR trns.paidAmount >= :minAmount) AND 
            (:maxAmount IS NULL OR trns.paidAmount <= :maxAmount)
            """,
    countQuery = """
            SELECT COUNT(trns) FROM TransactionV1 trns WHERE trns.hostelId=:hostelId AND 
            ((:customerIds IS NULL AND :invoiceIds IS NULL) OR (:customerIds IS NOT NULL AND trns.customerId IN :customerIds) OR (:invoiceIds IS NOT NULL AND trns.invoiceId IN :invoiceIds)) AND 
            (:bankIds IS NULL OR trns.bankId IN :bankIds) AND (:startDate IS NULL OR DATE(trns.paymentDate) >= DATE(:startDate)) AND 
            (:endDate IS NULL OR DATE(trns.paymentDate) <= DATE(:endDate)) AND 
            (:collectedBy IS NULL OR trns.createdBy IN :collectedBy) AND (:minAmount IS NULL OR trns.paidAmount >= :minAmount) AND 
            (:maxAmount IS NULL OR trns.paidAmount <= :maxAmount)
            """)
    Page<TransactionV1> findPagebleTransactions(String hostelId, List<String> customerIds, List<String> invoiceIds, List<String> bankIds, List<String> collectedBy, Date startDate, Date endDate, Integer minAmount, Integer maxAmount, Pageable pageable);

    @Query("""
            SELECT trns FROM TransactionV1 trns WHERE trns.hostelId=:hostelId AND 
            ((:customerIds IS NULL AND :invoiceIds IS NULL) OR (:customerIds IS NOT NULL AND trns.customerId IN :customerIds) OR (:invoiceIds IS NOT NULL AND trns.invoiceId IN :invoiceIds)) AND 
            (:bankIds IS NULL OR trns.bankId IN :bankIds) AND (:startDate IS NULL OR DATE(trns.paymentDate) >= DATE(:startDate)) AND 
            (:endDate IS NULL OR DATE(trns.paymentDate) <= DATE(:endDate)) AND 
            (:collectedBy IS NULL OR trns.createdBy IN :collectedBy) AND (:minAmount IS NULL OR trns.paidAmount >= :minAmount) AND 
            (:maxAmount IS NULL OR trns.paidAmount <= :maxAmount)
            """)
    List<TransactionV1> findTransactionsByHostelId(String hostelId, List<String> customerIds, List<String> invoiceIds, List<String> bankIds, List<String> collectedBy, Date startDate, Date endDate, Integer minAmount, Integer maxAmount);
}
