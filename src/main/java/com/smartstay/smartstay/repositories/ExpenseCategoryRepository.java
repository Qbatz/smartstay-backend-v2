package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ExpenseCategory;
import com.smartstay.smartstay.responses.expenses.ExpensesCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    boolean existsByCategoryNameIgnoreCaseAndHostelId(String categoryName, String hostelId);
    List<ExpenseCategory> findByHostelId(String hostelId);
    @Query("""
        SELECT DISTINCT c FROM ExpenseCategory c
        LEFT JOIN FETCH c.listSubCategories s
        WHERE c.hostelId = :hostelId
          AND c.isActive = true
          AND (s.isActive = true OR s IS NULL)
    """)
    List<ExpenseCategory> findAllByHostelIdAndIsActiveTrue(@Param("hostelId") String hostelId);
    ExpenseCategory findByCategoryId(Long id);

    @Query("""
            SELECT ec FROM ExpenseCategory ec WHERE LOWER(ec.categoryName)=LOWER(:categoryName) AND ec.hostelId=:hostelId AND ec.categoryId != :catId
            """)
    List<ExpenseCategory> findByCategoryName(String hostelId, String categoryName, Long catId);

}
