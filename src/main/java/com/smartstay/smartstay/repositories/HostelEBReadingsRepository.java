package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelEBReadingsRepository extends JpaRepository<HostelReadings, Long> {
    @Query(value = """
            SELECT * FROM hostel_readings WHERE hostel_id=:hostelId ORDER BY entry_date DESC LIMIT 1
            """, nativeQuery = true)
    HostelReadings lastReading(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT * FROM hostel_readings WHERE hostel_id=:hostelId AND id !=:id ORDER BY entry_date DESC LIMIT 1
            """, nativeQuery = true)
    HostelReadings previousEntry(@Param("hostelId") String hostelId, @Param("id") Long id);

    @Query("""
            SELECT hr from HostelReadings hr WHERE hr.hostelId=:hostelId AND hr.billStatus='INVOICE_NOT_GENERATED'
            """)
    List<HostelReadings> findAllInvoiceNotGeneratedReadings(String hostelId);
    List<HostelReadings> findByHostelId(String hostelId);
}
