package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRedemptionRepository extends JpaRepository<InvoiceRedemption, Long> {

    @Query(value = """
            SELECT * FROM invoice_redemption  WHERE hostel_id=:hostelId order by created_at desc limit 1
            """, nativeQuery = true)
    InvoiceRedemption findLatestInvoice(@Param("hostelId") String hostelId);
}
