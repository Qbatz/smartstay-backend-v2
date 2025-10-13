package com.smartstay.smartstay.services;

import com.smartstay.smartstay.repositories.ExpenseSubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseSubCategoryService {

    @Autowired
    private ExpenseSubCategoryRepository expenseSubCategoryRepository;

    public boolean checkSubCategoryExist(String hostelId, String subcategoryName) {
        return expenseSubCategoryRepository.existsBySubCategoryNameIgnoreCaseAndHostelId(subcategoryName, hostelId);
    }
}
