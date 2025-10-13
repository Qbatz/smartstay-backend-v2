package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpenseSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseSubCategoryRepository extends JpaRepository<ExpenseSubCategory, Long> {
    boolean existsBySubCategoryNameIgnoreCaseAndHostelId(String subCategoryName, String hostelId);
}
