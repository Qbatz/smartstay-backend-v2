package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorCategories;
import com.smartstay.smartstay.responses.vendor.VendorCategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorCategoriesRepository extends JpaRepository<VendorCategories, Integer> {

    VendorCategories findByCategoryId(int categoryId);

    VendorCategories findByCategoryIdAndHostelId(int categoryId, String hostelId);

    VendorCategories findByCategoryNameIgnoreCaseAndHostelId(String categoryName, String hostelId);

    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorCategoryResponse(" +
            "c.categoryId, c.categoryName) " +
            "FROM VendorCategories c " +
            "WHERE c.isEnabled = true AND c.hostelId = :hostelId " +
            "ORDER BY c.categoryId DESC")
    List<VendorCategoryResponse> findAllEnabledCategoriesByHostelId(@Param("hostelId") String hostelId);
}
