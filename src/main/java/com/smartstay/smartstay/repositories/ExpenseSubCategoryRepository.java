package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpenseSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSubCategoryRepository extends JpaRepository<ExpenseSubCategory, Long> {
    boolean existsBySubCategoryNameIgnoreCaseAndHostelId(String subCategoryName, String hostelId);

    @Query("""
            SELECT esc FROM ExpenseSubCategory esc WHERE LOWER(esc.subCategoryName)=LOWER(:subCateName) AND
            esc.subCategoryId !=:id
            """)
    List<ExpenseSubCategory> findBySUbCatNameAndId(String subCateName, Long id);
}
