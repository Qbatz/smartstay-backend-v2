package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceDrafts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDraftsRepositories extends JpaRepository<InvoiceDrafts, Long> {
    List<InvoiceDrafts> findByHostelId(String hostelId);
    @Query("""
            SELECT ir FROM  InvoiceDrafts ir WHERE ir.hostelId =:hostelId AND ir.draftId IN (:draftIds)
            """)
    List<InvoiceDrafts> findByHostelIdAndDraftIdIn(String hostelId, List<Long> draftIds);
}
