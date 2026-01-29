package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ExpenseSubCategory;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.payloads.expense.UpdateSubCategory;
import com.smartstay.smartstay.repositories.ExpenseSubCategoryRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ExpenseSubCategoryService {

    @Autowired
    private ExpenseSubCategoryRepository expenseSubCategoryRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

    public boolean checkSubCategoryExist(String hostelId, String subcategoryName) {
        return expenseSubCategoryRepository.existsBySubCategoryNameIgnoreCaseAndHostelId(subcategoryName, hostelId);
    }

    public ResponseEntity<?> updateSubCategry(String hostelId, String subCategoryId, UpdateSubCategory subCategory) {
        Long subCatId = 0l;
        try {
            subCatId = Long.valueOf(subCategoryId);
        }
        catch (Exception e) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        ExpenseSubCategory expSub = expenseSubCategoryRepository.getReferenceById(subCatId);

        if (expSub == null) {
            return new ResponseEntity<>(Utils.INVALID_SUB_CATEGORY_ID, HttpStatus.BAD_REQUEST);
        }
        if (!hostelId.equalsIgnoreCase(expSub.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }


        if (subCategory.newSubCategoryName() == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        expSub.setSubCategoryName(subCategory.newSubCategoryName());
        expenseSubCategoryRepository.save(expSub);

        Users users = usersService.findUserByUserId(authentication.getName());


        usersService.addUserLog(hostelId, String.valueOf(expSub.getSubCategoryId()), ActivitySource.EXPENSE_SUB_CATEGORY, ActivitySourceType.UPDATE, users);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }
}
