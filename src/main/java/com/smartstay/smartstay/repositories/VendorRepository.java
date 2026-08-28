package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.vendor.VendorLookupProjection;
import com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary;
import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;


public interface VendorRepository extends JpaRepository<VendorV1, String> {

    List<VendorV1> findAllByHostelId(String hostelId);

    /**
     * Ids of vendors assigned to a vendor category. Used to determine whether a category is in use by
     * any expense (a category is referenced indirectly: category -> vendors -> their expenses). Backed
     * by the vendor_category index. Returns ids regardless of vendor active state, since a soft-deleted
     * vendor's expenses still reference the category.
     */
    @Query("SELECT v.vendorId FROM VendorV1 v WHERE v.vendorCategory = :categoryId")
    List<Integer> findVendorIdsByVendorCategory(@Param("categoryId") Integer categoryId);

    /**
     * Efficient existence check for active vendors mapped to a category — used to block disabling a
     * category that is still in use. Spring Data issues a lightweight EXISTS/LIMIT 1 query (no entities
     * loaded), backed by the vendor_category index.
     */
    boolean existsByVendorCategoryAndIsActiveTrue(Integer vendorCategory);

    /**
     * Lightweight active-vendor lookup for a hostel: selects only id, name and business name (no full
     * entity, no joins), ordered by business name. Backed by the (hostel_id, business_name) index.
     */
    @Query("SELECT v.vendorId AS vendorId, v.firstName AS firstName, v.lastName AS lastName, " +
            "v.businessName AS businessName " +
            "FROM VendorV1 v WHERE v.hostelId = :hostelId AND v.isActive = true " +
            "ORDER BY v.businessName ASC")
    List<VendorLookupProjection> findActiveVendorLookupByHostelId(@Param("hostelId") String hostelId);

    String VENDOR_FILTERS =
            "WHERE v.hostelId = :hostelId AND v.isActive = true " +
            "AND (:name IS NULL OR LOWER(CONCAT(v.firstName, ' ', COALESCE(v.lastName, ''))) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR v.vendorCategory = :categoryId) " +
            "AND (:paymentStatuses IS NULL OR v.paymentStatus IN :paymentStatuses) " +
            "AND (:createdBy IS NULL OR v.createdBy IN :createdBy) " +
            "AND (:createdFrom IS NULL OR v.createdAt >= :createdFrom) " +
            "AND (:createdTo IS NULL OR v.createdAt <= :createdTo) " +
            "AND (:minBalance IS NULL OR COALESCE(v.balance, 0) >= :minBalance) " +
            "AND (:maxBalance IS NULL OR COALESCE(v.balance, 0) <= :maxBalance) " +
            "AND (:subCategoryId IS NULL OR EXISTS (SELECT e FROM ExpensesV1 e " +
            "     WHERE e.vendorId = CAST(v.vendorId AS String) AND e.hostelId = v.hostelId " +
            "       AND e.subCategoryId = :subCategoryId AND e.isActive = true)) ";

    @Query("SELECT v FROM VendorV1 v " + VENDOR_FILTERS + "ORDER BY v.vendorId DESC")
    Page<VendorV1> listVendors(@Param("hostelId") String hostelId,
                               @Param("name") String name,
                               @Param("categoryId") Integer categoryId,
                               @Param("paymentStatuses") List<VendorPaymentStatus> paymentStatuses,
                               @Param("createdBy") List<String> createdBy,
                               @Param("createdFrom") Date createdFrom,
                               @Param("createdTo") Date createdTo,
                               @Param("subCategoryId") Long subCategoryId,
                               @Param("minBalance") Double minBalance,
                               @Param("maxBalance") Double maxBalance,
                               Pageable pageable);

    @Query("SELECT new com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary(" +
            "COALESCE(SUM(v.totalExpense), 0), COALESCE(SUM(v.totalPaid), 0)) " +
            "FROM VendorV1 v " + VENDOR_FILTERS)
    VendorPurchaseSummary summarizeVendors(@Param("hostelId") String hostelId,
                                           @Param("name") String name,
                                           @Param("categoryId") Integer categoryId,
                                           @Param("paymentStatuses") List<VendorPaymentStatus> paymentStatuses,
                                           @Param("createdBy") List<String> createdBy,
                                           @Param("createdFrom") Date createdFrom,
                                           @Param("createdTo") Date createdTo,
                                           @Param("subCategoryId") Long subCategoryId,
                                           @Param("minBalance") Double minBalance,
                                           @Param("maxBalance") Double maxBalance);

    VendorV1 findByVendorId(int vendorId);

    List<VendorV1> findByVendorIdIn(List<Integer> vendorIds);

    List<VendorV1> findByHostelIdAndIsActiveTrueOrderByVendorIdDesc(String hostelId);

    VendorV1 findByVendorIdAndHostelId(int vendorId,String hostelId);

    boolean existsByEmailId(String emailId);
    boolean existsByEmailIdIgnoreCase(String emailId);
    boolean existsByMobile(String mobileNumber);

    // Uniqueness is scoped to a hostel: the same email/mobile may legitimately exist in other hostels.
    boolean existsByEmailIdIgnoreCaseAndHostelId(String emailId, String hostelId);
    boolean existsByMobileAndHostelId(String mobileNumber, String hostelId);

    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorResponse(" +
            "v.vendorId, v.firstName, v.lastName, TRIM(CONCAT(COALESCE(v.firstName, ''), ' ', COALESCE(v.lastName, ''))), " +
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
            "v.vendorId, v.firstName, v.lastName, TRIM(CONCAT(COALESCE(v.firstName, ''), ' ', COALESCE(v.lastName, ''))), " +
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
