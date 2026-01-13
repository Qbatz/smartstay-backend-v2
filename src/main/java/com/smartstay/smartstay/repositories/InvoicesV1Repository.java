package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.InvoiceCustomer;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.responses.invoices.InvoiceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String> {

    @Query(value = """
            select invc.invoice_id as invoiceId, invc.base_price as basePrice, invc.total_amount as totalAmount, invc.gst, invc.cgst, invc.sgst,
            invc.created_at as createdAt, invc.created_by as createdBy, invc.customer_id as customerId, 
            invc.hostel_id as hostelId, invc.invoice_generated_date as invoiceGeneratedAt, invc.invoice_start_date as invoiceStartDate,
            invc.invoice_due_date as invoiceDueDate, invc.invoice_type as invoiceType, 
            invc.payment_status as paymentStatus, invc.updated_at as updatedAt, 
            invc.invoice_number as invoiceNumber, customers.first_name as firstName, customers.last_name as lastName,
            customers.profile_pic as profilePic, 
            advance.advance_amount as advanceAmount, advance.deductions as deductions,
            invc.is_cancelled as cancelled, invc.paid_amount as paidAmount, invc.invoice_mode as invoiceMode   
            from invoicesv1 invc inner join customers customers on customers.customer_id=invc.customer_id 
            left outer join advance advance on advance.customer_id=invc.customer_id where invc.hostel_id=:hostelId AND invc.invoice_type not in('BOOKING') 
            order by invc.invoice_start_date desc
            """, nativeQuery = true)
    List<Invoices> findByHostelId(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT invc.invoice_id as invoiceId, invc.invoice_number as invoiceNumber, invc.invoice_mode as invoiceMode, invc.invoice_type as invoiceType, 
            invc.payment_status as paymentStatus, cus.profile_pic as profilePic,
            transaction.paid_amount as paidAmount, transaction.paid_at as paidAt, invc.customer_id as customerId, transaction.transaction_id as transactionId, cus.first_name as firstName, cus.last_name as lastName, 
            transaction.reference_number as referenceNumber,
            transaction.bank_id as bankId, bank.bank_name as bankName, bank.account_holder_name as holderName 
            FROM invoicesv1 invc 
            inner join transactionv1 transaction on transaction.invoice_id=invc.invoice_id 
            inner join customers cus on cus.customer_id=invc.customer_id 
            inner join bankingv1 bank on bank.bank_id=transaction.bank_id 
            where invc.hostel_id=:hostelId and invc.payment_status in ('PARTIAL_PAYMENT', 'PAID') and invc.is_cancelled=false
            """, nativeQuery = true)
    List<Receipts> findReceipts(@Param("hostelId") String hostelId);

    @Query("""
            SELECT i FROM InvoicesV1 i WHERE hostelId=:hostelId 
            AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate)) 
            AND (:endDate IS NULL OR DATE(i.invoiceEndDate) <= DATE(:endDate)) 
            AND i.invoiceType in (:types) AND (:createdBy IS NULL OR i.createdBy in (:createdBy)) 
            AND (:mode IS NULL OR i.invoiceMode in (:mode)) 
            AND (:paymentStatus IS NULL OR i.paymentStatus in (:paymentStatus)) 
            AND (:userId IS NULL OR i.customerId IN (:userId)) ORDER BY i.invoiceStartDate DESC
            """)
    List<InvoicesV1> findAllInvoicesByHostelId(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate, @Param("types") List<String> types,
                                               @Param("createdBy") List<String> createdBy, @Param("mode") List<String> mode,
                                               @Param("paymentStatus") List<String> paymentStatus, @Param("userId") List<String> userId);

    @Query(value = """
    SELECT * FROM invoicesv1
    WHERE hostel_id=:hostelId AND invoice_number LIKE CONCAT(:prefix, '%')
    ORDER BY CAST(SUBSTRING(invoice_number, LENGTH(:prefix) + 2) AS UNSIGNED) DESC
    LIMIT 1
""", nativeQuery = true)
    InvoicesV1 findLatestInvoiceByPrefix(@Param("prefix") String prefix, @Param("hostelId") String hostelId);
    @Query(value = "SELECT * FROM invoicesv1 WHERE customer_id = :customerId ORDER BY invoice_start_date DESC LIMIT 1",
            nativeQuery = true)
    InvoicesV1 findLatestInvoiceByCustomerId(@Param("customerId") String customerId);

    @Query(value = "SELECT * FROM invoicesv1 WHERE customer_id = :customerId and invoice_type='RENT' ORDER BY invoice_start_date DESC LIMIT 1",
            nativeQuery = true)
    InvoicesV1 findLatestRentInvoiceByCustomerId(@Param("customerId") String customerId);

    @Query(value = """
            SELECT * FROM invoicesv1 WHERE customer_id = :customerId and invoice_type in ('RENT', 'REASSIGN_RENT') ORDER BY invoice_start_date
            """, nativeQuery = true)
    List<InvoicesV1> findAllRentInvoicesByCustomerId(@Param("customerId") String customerId);

    InvoicesV1 findByCustomerIdAndHostelIdAndInvoiceType(String customerId, String hostelId, String invoiceType);
    List<InvoicesV1> findByHostelIdAndCustomerIdAndPaymentStatusNotIgnoreCaseAndIsCancelledFalse(String hostelId, String customerId, String paymentStatus);

    @Query(value = """
            SELECT * FROM invoicesv1 invc WHERE invc.invoice_start_date <= DATE(:endDate) and invc.invoice_end_date >=DATE(:startDate) and invc.customer_id=:customerId and invc.invoice_type='RENT' and is_cancelled=false
            """, nativeQuery = true)
    List<InvoicesV1> findInvoiceByCustomerIdAndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    InvoicesV1 findByInvoiceNumberAndHostelId(String invoiceNumber, String hostelId);

    List<InvoicesV1> findByCustomerId(String customerId);

    @Query(value = """
    SELECT COALESCE(SUM(t.paid_amount), 0)
    FROM transactionv1 t
    WHERE t.invoice_id = :invoiceId
    """, nativeQuery = true)
    Double findTotalPaidAmountByInvoiceId(@Param("invoiceId") String invoiceId);


    @Query(
            value = """
        SELECT 
            i.invoice_number AS invoiceNumber,
            i.total_amount AS totalAmount,
            DATE_FORMAT(i.invoice_start_date, '%d/%m/%Y') AS invoiceStartDate,
            i.invoice_type AS invoiceType
        FROM invoicesv1 i
        WHERE i.hostel_id = :hostelId
        AND i.invoice_id IN (:invoiceId)
        """,
            nativeQuery = true
    )
    List<InvoiceSummary> findInvoiceSummariesByHostelId(
            @Param("hostelId") String hostelId,
            @Param("invoiceId") List<String> invoiceId
    );

    List<InvoicesV1> findByCustomerIdAndInvoiceType(String customerId, String type);

    @Query("""
            SELECT inv.customerId, inv.invoiceId FROM InvoicesV1 inv where (inv.paidAmount IS NULL OR inv.paidAmount<inv.totalAmount)
             and inv.invoiceDueDate<DATE(:todaysDate) and inv.customerId in (:customerIds) and inv.invoiceType='RENT'
            """)
    List<InvoiceCustomer> findByCustomerIdAndBedIdsForDue(List<String> customerIds, Date todaysDate);

    @Query("""
            SELECT inv FROM InvoicesV1 inv where inv.hostelId=:hostelId AND inv.invoiceType='RENT' AND inv.paymentStatus in 
            ('PARTIAL_PAYMENT', 'PENDING')
            """)
    List<InvoicesV1> findPendingInvoices(String hostelId);

    @Query(value = """
            SELECT * FROM invoicesv1 invc WHERE invc.customer_id=:customerId and DATE(invc.invoice_start_date) >= DATE(:startDate) AND invoice_type in ('RENT', 'REASSIGN_RENT') ORDER BY invc.invoice_start_date DESC LIMIT 1;
            """, nativeQuery = true)
    InvoicesV1 findCurrentRunningInvoice(@Param("customerId") String customerId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM `invoicesv1` WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) < DATE(:startDate) 
            AND payment_status in ('PENDING', 'PARTIAL_PAYMENT') AND (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
            """, nativeQuery = true)
    List<InvoicesV1> findOldRentalPendingInvoicesExcludeCurrentMonth(@Param("customerId") String customerId, @Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM `invoicesv1` WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) >= DATE(:startDate) 
             AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
            """, nativeQuery = true)
    List<InvoicesV1> findAllCurrentMonthInvoices(@Param("customerId") String customerId, @Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM invoicesv1 WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) >= DATE(:startDate) 
             AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
            """, nativeQuery = true)
    List<InvoicesV1> findAllInvoicesFromDate(@Param("customerId") String customerId, @Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    List<InvoicesV1> findByInvoiceIdIn(List<String> invoiceId);

    @Query(value = """
            SELECT * FROM invoicesv1 WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) < DATE(:currentMonthStartDate) AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
            """, nativeQuery = true)
    List<InvoicesV1> findAllRentalInvoicesExceptCurrentMonth(String customerId, String hostelId, Date currentMonthStartDate);

    @Query(value = """
            SELECT * FROM invoicesv1 i WHERE (i.customer_id, i.invoice_start_date) IN (SELECT customer_id, MAX(invoice_start_date)
                    FROM invoicesv1 WHERE customer_id IN :customerIds GROUP BY customer_id)
            """, nativeQuery = true)
    List<InvoicesV1> findLatestInvoicesByCustomerIds(@Param("customerIds") List<String> customerIds);

    @Query("""
            SELECT inv FROM InvoicesV1 inv WHERE inv.invoiceType='SETTLEMENT'
            """)
    List<InvoicesV1> findAllSettlementInvoices();

}
