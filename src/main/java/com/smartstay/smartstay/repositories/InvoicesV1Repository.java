package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto;
import com.smartstay.smartstay.dto.invoices.InvoiceCustomer;
import com.smartstay.smartstay.responses.invoices.InvoiceSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String> {
        @Query("""
                        SELECT i FROM InvoicesV1 i WHERE hostelId=:hostelId
                        AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
                        AND (:endDate IS NULL OR DATE(i.invoiceEndDate) <= DATE(:endDate))
                        AND i.invoiceType in (:types) AND (:createdBy IS NULL OR i.createdBy in (:createdBy))
                        AND (:mode IS NULL OR i.invoiceMode in (:mode))
                        AND (:paymentStatus IS NULL OR i.paymentStatus in (:paymentStatus))
                        AND (:userId IS NULL OR i.customerId IN (:userId)) ORDER BY i.invoiceStartDate DESC
                        """)
        List<InvoicesV1> findAllInvoicesByHostelId(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate,
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

        @Query(value = "SELECT * FROM invoicesv1 WHERE customer_id = :customerId ORDER BY invoice_start_date DESC LIMIT 1", nativeQuery = true)
        InvoicesV1 findLatestInvoiceByCustomerId(@Param("customerId") String customerId);

        @Query(value = "SELECT * FROM invoicesv1 WHERE customer_id = :customerId and invoice_type='RENT' ORDER BY invoice_start_date DESC LIMIT 1", nativeQuery = true)
        InvoicesV1 findLatestRentInvoiceByCustomerId(@Param("customerId") String customerId);

        @Query(value = """
                        SELECT * FROM invoicesv1 WHERE customer_id = :customerId and invoice_type in ('RENT', 'REASSIGN_RENT') ORDER BY invoice_start_date
                        """, nativeQuery = true)
        List<InvoicesV1> findAllRentInvoicesByCustomerId(@Param("customerId") String customerId);

        InvoicesV1 findByCustomerIdAndHostelIdAndInvoiceType(String customerId, String hostelId, String invoiceType);

        List<InvoicesV1> findByHostelIdAndCustomerIdAndPaymentStatusNotIgnoreCaseAndIsCancelledFalse(String hostelId,
                        String customerId, String paymentStatus);

        @Query(value = """
                        SELECT * FROM invoicesv1 invc WHERE invc.invoice_start_date <= DATE(:endDate) and invc.invoice_end_date >=DATE(:startDate) and invc.customer_id=:customerId and invc.invoice_type='RENT' and is_cancelled=false
                        """, nativeQuery = true)
        List<InvoicesV1> findInvoiceByCustomerIdAndDate(@Param("customerId") String customerId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate);

        InvoicesV1 findByInvoiceNumberAndHostelId(String invoiceNumber, String hostelId);

        List<InvoicesV1> findByCustomerId(String customerId);

        @Query(value = """
                        SELECT COALESCE(SUM(t.paid_amount), 0)
                        FROM transactionv1 t
                        WHERE t.invoice_id = :invoiceId
                        """, nativeQuery = true)
        Double findTotalPaidAmountByInvoiceId(@Param("invoiceId") String invoiceId);

        @Query(value = """
                        SELECT
                            i.invoice_number AS invoiceNumber,
                            i.total_amount AS totalAmount,
                            DATE_FORMAT(i.invoice_start_date, '%d/%m/%Y') AS invoiceStartDate,
                            i.invoice_type AS invoiceType
                        FROM invoicesv1 i
                        WHERE i.hostel_id = :hostelId
                        AND i.invoice_id IN (:invoiceId)
                        """, nativeQuery = true)
        List<InvoiceSummary> findInvoiceSummariesByHostelId(@Param("hostelId") String hostelId,
                        @Param("invoiceId") List<String> invoiceId);

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
        InvoicesV1 findCurrentRunningInvoice(@Param("customerId") String customerId,
                        @Param("startDate") Date startDate);

        @Query(value = """
                        SELECT * FROM `invoicesv1` WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) < DATE(:startDate)
                        AND payment_status in ('PENDING', 'PARTIAL_PAYMENT') AND (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
                        """, nativeQuery = true)
        List<InvoicesV1> findOldRentalPendingInvoicesExcludeCurrentMonth(@Param("customerId") String customerId,
                        @Param("hostelId") String hostelId, @Param("startDate") Date startDate);

        @Query(value = """
                        SELECT * FROM `invoicesv1` WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) >= DATE(:startDate)
                         AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
                        """, nativeQuery = true)
        List<InvoicesV1> findAllCurrentMonthInvoices(@Param("customerId") String customerId,
                        @Param("hostelId") String hostelId, @Param("startDate") Date startDate);

        @Query(value = """
                        SELECT * FROM invoicesv1 WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) >= DATE(:startDate)
                         AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
                        """, nativeQuery = true)
        List<InvoicesV1> findAllInvoicesFromDate(@Param("customerId") String customerId,
                        @Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate);

        List<InvoicesV1> findByInvoiceIdIn(List<String> invoiceId);

        @Query(value = """
                        SELECT * FROM invoicesv1 WHERE customer_id=:customerId AND hostel_id=:hostelId AND DATE(invoice_start_date) < DATE(:currentMonthStartDate) AND  (invoice_type='RENT' OR invoice_type='REASSIGN_RENT')
                        """, nativeQuery = true)
        List<InvoicesV1> findAllRentalInvoicesExceptCurrentMonth(String customerId, String hostelId,
                        Date currentMonthStartDate);

        @Query(value = """
                        SELECT * FROM invoicesv1 i WHERE (i.customer_id, i.invoice_start_date) IN (SELECT customer_id, MAX(invoice_start_date)
                                FROM invoicesv1 WHERE customer_id IN :customerIds GROUP BY customer_id)
                        """, nativeQuery = true)
        List<InvoicesV1> findLatestInvoicesByCustomerIds(@Param("customerIds") List<String> customerIds);

        @Query("""
                        SELECT inv FROM InvoicesV1 inv WHERE inv.invoiceType='BOOKING'
                        """)
        List<InvoicesV1> findAllBookingInvoices();

        @Query("SELECT COUNT(i) FROM InvoicesV1 i WHERE i.hostelId = :hostelId")
        int countByHostelId(@Param("hostelId") String hostelId);

        @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InvoicesV1 i WHERE i.hostelId = :hostelId")
        Double sumTotalAmountByHostelId(@Param("hostelId") String hostelId);

        @Query(value = """
                        SELECT invc.* FROM invoicesv1 invc
                        JOIN customers c ON invc.customer_id = c.customer_id
                        WHERE invc.hostel_id = :hostelId
                        AND invc.invoice_type != 'SETTLEMENT'
                        AND (:startDate IS NULL OR DATE(invc.invoice_start_date) >= DATE(:startDate))
                        AND (:endDate IS NULL OR DATE(invc.invoice_start_date) <= DATE(:endDate))
                        AND (:search IS NULL OR (LOWER(c.first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.last_name) LIKE LOWER(CONCAT('%', :search, '%'))))
                        AND (:paymentStatus IS NULL OR invc.payment_status IN (:paymentStatus))
                        AND (:invoiceModes IS NULL OR invc.invoice_mode IN (:invoiceModes))
                        AND (:invoiceTypes IS NULL OR invc.invoice_type IN (:invoiceTypes))
                        AND (:createdBy IS NULL OR invc.created_by IN (:createdBy))
                        ORDER BY invc.invoice_start_date DESC
                        """, nativeQuery = true)
        List<InvoicesV1> findInvoicesByFilters(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate, @Param("search") String search,
                        @Param("paymentStatus") List<String> paymentStatus,
                        @Param("invoiceModes") List<String> invoiceModes,
                        @Param("invoiceTypes") List<String> invoiceTypes, @Param("createdBy") List<String> createdBy,
                                               @Param("isCancelled") boolean isCancelled, Pageable pageable);

        @Query("SELECT COUNT(i) FROM InvoicesV1 i WHERE i.hostelId = :hostelId AND DATE(i.invoiceStartDate) >= DATE(:startDate) AND DATE(i.invoiceStartDate) <= DATE(:endDate)")
        int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM InvoicesV1 i WHERE i.hostelId = :hostelId AND i.invoiceType != :invoiceType AND DATE(i.invoiceStartDate) >= DATE(:startDate) AND DATE(i.invoiceStartDate) <= DATE(:endDate)")
        Double sumTotalAmountByHostelIdAndDateRangeExcludingSettlement(@Param("hostelId") String hostelId,
                        @Param("invoiceType") String invoiceType, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM InvoicesV1 i WHERE i.hostelId = :hostelId AND i.invoiceType != :invoiceType AND DATE(i.invoiceStartDate) >= DATE(:startDate) AND DATE(i.invoiceStartDate) <= DATE(:endDate)")
        Double sumPaidAmountByHostelIdAndDateRangeExcludingSettlement(@Param("hostelId") String hostelId,
                        @Param("invoiceType") String invoiceType, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("""
                        SELECT i FROM InvoicesV1 i
                        WHERE i.hostelId = :hostelId 
                        AND i.invoiceType != 'SETTLEMENT' 
                        AND i.isCancelled in :isCancelled 
                        AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
                        AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
                        AND (:paymentStatus IS NULL OR i.paymentStatus IN :paymentStatus)
                        AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes)
                        AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes)
                        AND (:createdBy IS NULL OR i.createdBy IN :createdBy)
                        AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount)
                        AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount)
                        AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount)
                        AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount)
                        ORDER BY i.invoiceStartDate DESC
                        """)
        List<InvoicesV1> findInvoicesByFilters(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate, @Param("paymentStatus") List<String> paymentStatus,
                        @Param("invoiceModes") List<String> invoiceModes,
                        @Param("invoiceTypes") List<String> invoiceTypes,
                        @Param("createdBy") List<String> createdBy, @Param("minPaidAmount") Double minPaidAmount,
                        @Param("maxPaidAmount") Double maxPaidAmount,
                        @Param("minOutstandingAmount") Double minOutstandingAmount,
                        @Param("maxOutstandingAmount") Double maxOutstandingAmount, List<Boolean> isCancelled, Pageable pageable);

        @Query("""
                        SELECT i FROM InvoicesV1 i
                        WHERE i.hostelId = :hostelId 
                        AND  i.invoiceType != 'SETTLEMENT'  
                        AND i.isCancelled in :isCancelled 
                        AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
                        AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
                        AND i.customerId IN :customerIds
                        AND (:paymentStatus IS NULL OR i.paymentStatus IN :paymentStatus)
                        AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes)
                        AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes)
                        AND (:createdBy IS NULL OR i.createdBy IN :createdBy)
                        AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount)
                        AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount)
                        AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount)
                        AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount)
                        ORDER BY i.invoiceStartDate DESC
                        """)
        List<InvoicesV1> findInvoicesByFiltersWithCustomers(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                        @Param("customerIds") List<String> customerIds,
                        @Param("paymentStatus") List<String> paymentStatus,
                        @Param("invoiceModes") List<String> invoiceModes,
                        @Param("invoiceTypes") List<String> invoiceTypes,
                        @Param("createdBy") List<String> createdBy, @Param("minPaidAmount") Double minPaidAmount,
                        @Param("maxPaidAmount") Double maxPaidAmount,
                        @Param("minOutstandingAmount") Double minOutstandingAmount,
                        @Param("maxOutstandingAmount") Double maxOutstandingAmount, List<Boolean> isCancelled, Pageable pageable);

//        @Query("""
//                        SELECT new com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto(
//                            COUNT(CASE WHEN i.invoiceType != 'REFUND' THEN 1 ELSE NULL END),
//                            SUM(CASE WHEN i.invoiceType != 'REFUND' THEN i.totalAmount ELSE 0.0 END),
//                            SUM(i.paidAmount),
//                            SUM(CASE WHEN i.invoiceType = 'REFUND' THEN i.totalAmount ELSE 0.0 END)
//                        )
//                        FROM InvoicesV1 i
//                        WHERE i.hostelId = :hostelId
//                        AND i.invoiceType != 'SETTLEMENT'
//                        AND i.isCancelled = false
//                        AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
//                        AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
//                        AND (:paymentStatus IS NULL OR i.paymentStatus IN :paymentStatus)
//                        AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes)
//                        AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes)
//                        AND (:createdBy IS NULL OR i.createdBy IN :createdBy)
//                        AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount)
//                        AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount)
//                        AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount)
//                        AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount)
//                        """)
//        InvoiceAggregateDto findInvoiceAggregatesByFilters(@Param("hostelId") String hostelId,
//                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
//                        @Param("paymentStatus") List<String> paymentStatus,
//                        @Param("invoiceModes") List<String> invoiceModes,
//                        @Param("invoiceTypes") List<String> invoiceTypes, @Param("createdBy") List<String> createdBy,
//                        @Param("minPaidAmount") Double minPaidAmount, @Param("maxPaidAmount") Double maxPaidAmount,
//                        @Param("minOutstandingAmount") Double minOutstandingAmount,
//                        @Param("maxOutstandingAmount") Double maxOutstandingAmount);

//        @Query("""
//                        SELECT new com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto(
//                            COUNT(CASE WHEN i.invoiceType != 'REFUND' THEN 1 ELSE NULL END),
//                            SUM(CASE WHEN i.invoiceType != 'REFUND' THEN i.totalAmount ELSE 0.0 END),
//                            SUM(i.paidAmount),
//                            SUM(CASE WHEN i.invoiceType = 'REFUND' THEN i.totalAmount ELSE 0.0 END)
//                        )
//                        FROM InvoicesV1 i
//                        WHERE i.hostelId = :hostelId
//                        AND i.invoiceType != 'SETTLEMENT'
//                        AND i.isCancelled = false
//                        AND (:startDate IS NULL OR DATE(i.invoiceStartDate) >= DATE(:startDate))
//                        AND (:endDate IS NULL OR DATE(i.invoiceStartDate) <= DATE(:endDate))
//                        AND i.customerId IN :customerIds
//                        AND (:paymentStatus IS NULL OR i.paymentStatus IN :paymentStatus)
//                        AND (:invoiceModes IS NULL OR i.invoiceMode IN :invoiceModes)
//                        AND (:invoiceTypes IS NULL OR i.invoiceType IN :invoiceTypes)
//                        AND (:createdBy IS NULL OR i.createdBy IN :createdBy)
//                        AND (:minPaidAmount IS NULL OR i.paidAmount >= :minPaidAmount)
//                        AND (:maxPaidAmount IS NULL OR i.paidAmount <= :maxPaidAmount)
//                        AND (:minOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) >= :minOutstandingAmount)
//                        AND (:maxOutstandingAmount IS NULL OR (i.totalAmount - i.paidAmount) <= :maxOutstandingAmount)
//                        """)
//        InvoiceAggregateDto findInvoiceAggregatesByFiltersWithCustomers(@Param("hostelId") String hostelId,
//                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
//                        @Param("customerIds") List<String> customerIds,
//                        @Param("paymentStatus") List<String> paymentStatus,
//                        @Param("invoiceModes") List<String> invoiceModes,
//                        @Param("invoiceTypes") List<String> invoiceTypes,
//                        @Param("createdBy") List<String> createdBy, @Param("minPaidAmount") Double minPaidAmount,
//                        @Param("maxPaidAmount") Double maxPaidAmount,
//                        @Param("minOutstandingAmount") Double minOutstandingAmount,
//                        @Param("maxOutstandingAmount") Double maxOutstandingAmount);

        @Query(value = """
                        SELECT DISTINCT created_by
                        FROM invoicesv1
                        WHERE hostel_id = :hostelId
                        AND invoice_type != 'SETTLEMENT'
                        """, nativeQuery = true)
        List<String> findDistinctCreatedBy(@Param("hostelId") String hostelId);

        @Query("""
                        SELECT inv FROM InvoicesV1 inv WHERE inv.invoiceType='SETTLEMENT' AND inv.hostelId=:hostelId AND
                        DATE(inv.invoiceStartDate) >=DATE(:startDate) AND DATE(inv.invoiceEndDate)<=DATE(:endDate)
                        """)
        List<InvoicesV1> findSettlementByHostelIdAndStartDateAndEndDate(String hostelId, Date startDate, Date endDate);

        @Query("""
                        SELECT inv FROM InvoicesV1 inv WHERE inv.hostelId=:hostelId AND
                        DATE(inv.invoiceStartDate) >=DATE(:startDate) AND DATE(inv.invoiceEndDate)<=DATE(:endDate) AND
                        inv.invoiceType IN (:invoiceTypes) AND inv.isCancelled = false
                        """)
        List<InvoicesV1> findInvoiceByHostelIdAndStartDateAndEndDate(String hostelId, Date startDate, Date endDate,
                        List<String> invoiceTypes);

        @Query(value = """
                        SELECT * FROM invoicesv1 where invoice_type='BOOKING' and paid_amount>total_amount;
                        """, nativeQuery = true)
        List<InvoicesV1> findBookingAmountGreaterThanPaidAmount();

        @Query("SELECT i.invoiceId FROM InvoicesV1 i WHERE i.hostelId = :hostelId AND i.invoiceType IN :invoiceTypes")
        List<String> findInvoiceIdsByHostelIdAndTypeIn(@Param("hostelId") String hostelId,
                        @Param("invoiceTypes") List<String> invoiceTypes);

        @Query("""
                SELECT invc FROM InvoicesV1 invc WHERE invc.invoiceType='BOOKING' AND invc.paymentStatus='CANCELLED'
                """)
        List<InvoicesV1> findBookingInvoiceWithCancelledPayment();
}