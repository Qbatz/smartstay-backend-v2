package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface VendorRepository extends JpaRepository<VendorV1, String> {

    List<VendorV1> findAllByHostelId(String hostelId);

    VendorV1 findByVendorId(int vendorId);

    VendorV1 findByVendorIdAndHostelId(int vendorId,String hostelId);

    boolean existsByEmailId(String emailId);
    boolean existsByMobile(String mobileNumber);

    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorResponse(" +
            "v.vendorId, v.firstName, v.lastName, CONCAT(v.firstName, ' ', v.lastName), " +
            "v.businessName, v.mobile, v.emailId, v.profilePic, " +
            "v.houseNo, v.area, v.landMark, v.city, v.pinCode, v.state, v.countryCode, c.countryName, c.countryId, " +
            "vc.categoryId, vc.categoryName, v.contactPerson, v.contactPersonMobile, v.description, v.vendorCode, v.gst, v.pan, " +
            "v.allowCredit, v.creditLimit, v.creditPeriod) " +
            "FROM VendorV1 v JOIN Countries c ON v.country = c.countryId " +
            "LEFT JOIN VendorCategories vc ON v.vendorCategory = vc.categoryId " +
            "WHERE v.hostelId = :hostelId " +
            "ORDER BY v.vendorId Desc")
    List<VendorResponse> findAllVendorsByHostelId(@Param("hostelId") String hostelId);


    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorResponse(" +
            "v.vendorId, v.firstName, v.lastName, CONCAT(v.firstName, ' ', v.lastName), " +
            "v.businessName, v.mobile, v.emailId, v.profilePic, " +
            "v.houseNo, v.area, v.landMark, v.city, v.pinCode, v.state, v.countryCode, c.countryName, c.countryId, " +
            "vc.categoryId, vc.categoryName, v.contactPerson, v.contactPersonMobile, v.description, v.vendorCode, v.gst, v.pan, " +
            "v.allowCredit, v.creditLimit, v.creditPeriod) " +
            "FROM VendorV1 v JOIN Countries c ON v.country = c.countryId " +
            "LEFT JOIN VendorCategories vc ON v.vendorCategory = vc.categoryId " +
            "WHERE v.vendorId = :vendorId")
    VendorResponse getVendor(@Param("vendorId") int vendorId);

        @Query("SELECT COUNT(v) FROM VendorV1 v WHERE v.hostelId = :hostelId")
        int countByHostelId(@Param("hostelId") String hostelId);



}
