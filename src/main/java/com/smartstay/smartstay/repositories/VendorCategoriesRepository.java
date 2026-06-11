package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.VendorCategories;
import com.smartstay.smartstay.responses.vendor.VendorCategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VendorCategoriesRepository extends JpaRepository<VendorCategories, Integer> {

    VendorCategories findByCategoryId(int categoryId);

    VendorCategories findByCategoryNameIgnoreCase(String categoryName);

    @Query("SELECT new com.smartstay.smartstay.responses.vendor.VendorCategoryResponse(" +
            "c.categoryId, c.categoryName) " +
            "FROM VendorCategories c " +
            "WHERE c.isEnabled = true " +
            "ORDER BY c.categoryId DESC")
    List<VendorCategoryResponse> findAllEnabledCategories();
}
