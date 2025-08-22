package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorV1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface VendorRepository extends JpaRepository<VendorV1, String> {

    List<VendorV1> findAllByHostelId(String hostelId);

    VendorV1 findByVendorId(int vendorId);

    boolean existsByEmailId(String emailId);
    boolean existsByMobile(String mobileNumber);

}
