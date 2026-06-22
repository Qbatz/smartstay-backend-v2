package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary;
import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface VendorRepository extends JpaRepository<VendorV1, String> {

    List<VendorV1> findAllByHostelId(String hostelId);

    @Query("SELECT v FROM VendorV1 v " +
            "WHERE v.hostelId = :hostelId AND v.isActive = true " +
            "AND (:name IS NULL OR LOWER(CONCAT(v.firstName, ' ', COALESCE(v.lastName, ''))) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR v.vendorCategory = :categoryId) " +
            "AND (:paymentStatus IS NULL OR v.paymentStatus = :paymentStatus) " +
            "ORDER BY v.vendorId DESC")
    Page<VendorV1> listVendors(@Param("hostelId") String hostelId,
                               @Param("name") String name,
                               @Param("categoryId") Integer categoryId,
                               @Param("paymentStatus") VendorPaymentStatus paymentStatus,
                               Pageable pageable);

    @Query("SELECT new com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary(" +
            "COALESCE(SUM(v.totalExpense), 0), COALESCE(SUM(v.totalPaid), 0)) " +
            "FROM VendorV1 v " +
            "WHERE v.hostelId = :hostelId AND v.isActive = true " +
            "AND (:name IS NULL OR LOWER(CONCAT(v.firstName, ' ', COALESCE(v.lastName, ''))) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR v.vendorCategory = :categoryId) " +
            "AND (:paymentStatus IS NULL OR v.paymentStatus = :paymentStatus)")
    VendorPurchaseSummary summarizeVendors(@Param("hostelId") String hostelId,
                                           @Param("name") String name,
                                           @Param("categoryId") Integer categoryId,
                                           @Param("paymentStatus") VendorPaymentStatus paymentStatus);

    VendorV1 findByVendorId(int vendorId);

    List<VendorV1> findByVendorIdIn(List<Integer> vendorIds);

    List<VendorV1> findByHostelIdAndIsActiveTrueOrderByVendorIdDesc(String hostelId);

    VendorV1 findByVendorIdAndHostelId(int vendorId,String hostelId);

    boolean existsByEmailId(String emailId);
    boolean existsByEmailIdIgnoreCase(String emailId);
    boolean existsByMobile(String mobileNumber);

    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorResponse(" +
            "v.vendorId, v.firstName, v.lastName, CONCAT(v.firstName, ' ', v.lastName), " +
            "v.businessName, v.mobile, v.emailId, v.profilePic, " +
            "v.houseNo, v.area, v.landMark, v.city, v.pinCode, v.state, v.countryCode, c.countryName, c.countryId, " +
            "vc.categoryId, vc.categoryName, v.contactPerson, v.contactPersonMobile, v.description, v.vendorCode, v.gst, v.pan, " +
            "v.allowCredit, v.creditLimit, v.creditPeriod, v.businessMobileCode, v.contactPersonMobileCode) " +
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
            "v.allowCredit, v.creditLimit, v.creditPeriod, v.businessMobileCode, v.contactPersonMobileCode) " +
            "FROM VendorV1 v JOIN Countries c ON v.country = c.countryId " +
            "LEFT JOIN VendorCategories vc ON v.vendorCategory = vc.categoryId " +
            "WHERE v.vendorId = :vendorId")
    VendorResponse getVendor(@Param("vendorId") int vendorId);

        @Query("SELECT COUNT(v) FROM VendorV1 v WHERE v.hostelId = :hostelId")
        int countByHostelId(@Param("hostelId") String hostelId);



}
