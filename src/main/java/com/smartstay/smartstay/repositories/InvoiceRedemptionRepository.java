package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRedemptionRepository extends JpaRepository<InvoiceRedemption, Long> {

    @Query(value = """
            SELECT * FROM invoice_redemption  WHERE hostel_id=:hostelId order by created_at desc limit 1
            """, nativeQuery = true)
    InvoiceRedemption findLatestInvoice(@Param("hostelId") String hostelId);

    @Query("""
            SELECT ir FROM InvoiceRedemption ir WHERE ir.hostelId=:hostelId AND ir.targetInvoiceId=:targetInvoiceId 
            AND ir.isActive=true
            """)
    List<InvoiceRedemption> findByHostelIdAndTargetInvoiceId(String hostelId, String targetInvoiceId);

    @Query("""
            SELECT ir FROM InvoiceRedemption ir WHERE ir.hostelId=:hostelId AND ir.targetInvoiceId IN (:targetInvoiceId) 
            AND ir.isActive=true
            """)
    List<InvoiceRedemption> findByHostelIdAndTargetInvoiceId(String hostelId, List<String> targetInvoiceId);

    @Query("""
            SELECT ir FROM InvoiceRedemption ir WHERE ir.hostelId=:hostelId AND ir.sourceInvoiceId=:invoiceId AND 
            ir.isActive = true
            """)
    List<InvoiceRedemption> findByHostelIdAndSourceId(String hostelId, String invoiceId);

    @Query("""
            SELECT ir FROM InvoiceRedemption ir WHERE ir.hostelId=:hostelId AND ir.targetInvoiceId=:invoiceId AND 
            ir.isActive=true
            """)
    List<InvoiceRedemption> findByHostelIdAndTargetId(String hostelId, String invoiceId);

}
