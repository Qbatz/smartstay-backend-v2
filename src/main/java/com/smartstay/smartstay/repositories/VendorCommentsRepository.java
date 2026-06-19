package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorComments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorCommentsRepository extends JpaRepository<VendorComments, Long> {

    Page<VendorComments> findByVendorIdAndIsActiveTrue(Integer vendorId, Pageable pageable);

    VendorComments findByIdAndIsActiveTrue(Long id);
}
