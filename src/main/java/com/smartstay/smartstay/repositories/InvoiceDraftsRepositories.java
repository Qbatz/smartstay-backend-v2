package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceDrafts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDraftsRepositories extends JpaRepository<InvoiceDrafts, Long> {
}
