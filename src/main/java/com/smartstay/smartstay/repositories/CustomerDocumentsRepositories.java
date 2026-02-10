package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDocumentsRepositories extends JpaRepository<CustomerDocuments, Long> {
    List<CustomerDocuments> findByCustomerIdAndIsDeletedFalse(String customerId);

    CustomerDocuments findByDocumentIdAndCustomerId(Long documentId, String customerId);
}
