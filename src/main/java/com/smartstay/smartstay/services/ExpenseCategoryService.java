package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.expenses.ExpensesCategoryMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ExpenseSubCategory;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.dto.expenses.ExpensesSubCategory;
import com.smartstay.smartstay.payloads.expense.ExpenseCategory;
import com.smartstay.smartstay.repositories.ExpenseCategoryRepository;
import com.smartstay.smartstay.responses.expenses.ExpensesCategories;
import com.smartstay.smartstay.responses.expenses.ExpensesSubCategories;
import com.smartstay.smartstay.util.Utils;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExpenseCategoryService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private ExpenseCategoryRepository expensesCategoryRepository;
    @Autowired
    private ExpenseSubCategoryService expenseSubCategory;

    public ResponseEntity<?> createExpenseCategory(ExpenseCategory category, String hostelId) {

        if (category == null) {
            return new ResponseEntity<>(Utils.CATEGORY_NAME_CATEGORY_ID_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!Utils.checkNullOrEmpty(category.categoryId()) && !Utils.checkNullOrEmpty(category.categoryName())) {
            return new ResponseEntity<>(Utils.CATEGORY_NAME_CATEGORY_ID_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(category.categoryName()) && !Utils.checkNullOrEmpty(category.categoryId())) {
            if (checkCategoryNameExists(category.categoryName(), hostelId)) {
                return new ResponseEntity<>(Utils.CATEGORY_NAME_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            else {
                com.smartstay.smartstay.dao.ExpenseCategory expenseCategory = new com.smartstay.smartstay.dao.ExpenseCategory();
                expenseCategory.setCategoryName(category.categoryName());
                expenseCategory.setHostelId(hostelId);
                expenseCategory.setCreatedBy(authentication.getName());
                expenseCategory.setCreatedAt(new Date());
                expenseCategory.setActive(true);

                if (Utils.checkNullOrEmpty(category.subCategory())) {
                    List<ExpenseSubCategory> listSubCategory = new ArrayList<>();
                    ExpenseSubCategory subCategory = new ExpenseSubCategory();
                    subCategory.setSubCategoryName(category.subCategory());
                    subCategory.setHostelId(hostelId);
                    subCategory.setCreatedBy(authentication.getName());
                    subCategory.setActive(true);
                    subCategory.setCreatedAt(new Date());

                    subCategory.setExpenseCategory(expenseCategory);

                    listSubCategory.add(subCategory);
                    expenseCategory.setListSubCategories(listSubCategory);
                }
                expensesCategoryRepository.save(expenseCategory);

                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }
        }
        else if (Utils.checkNullOrEmpty(category.categoryId()) && !Utils.checkNullOrEmpty(category.categoryName())) {
            if (!Utils.checkNullOrEmpty(category.subCategory())) {
                return new ResponseEntity<>(Utils.SUB_CATEGORY_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            if (expenseSubCategory.checkSubCategoryExist(hostelId, category.subCategory())) {
                return new ResponseEntity<>(Utils.SUB_CATEGORY_NAME_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            else {
                com.smartstay.smartstay.dao.ExpenseCategory expenseCategory = expensesCategoryRepository.findById(category.categoryId()).orElse(null);
                if (expenseCategory == null) {
                    return new ResponseEntity<>(Utils.INVALID_CATEGORY_ID, HttpStatus.BAD_REQUEST);
                }
                List<ExpenseSubCategory> listSubCategories = expenseCategory.getListSubCategories();
                long subCategoryWithName = listSubCategories
                        .stream()
                        .filter(item -> item.getSubCategoryName() != null && item.getSubCategoryName().equalsIgnoreCase(category.subCategory()))
                        .count();
                if (subCategoryWithName == 0) {
                    ExpenseSubCategory subCat = new ExpenseSubCategory();
                    subCat.setSubCategoryName(category.subCategory());
                    subCat.setActive(true);
                    subCat.setCreatedAt(new Date());
                    subCat.setCreatedBy(authentication.getName());
                    subCat.setHostelId(hostelId);
                    subCat.setExpenseCategory(expenseCategory);

                    listSubCategories.add(subCat);
                    expenseCategory.setListSubCategories(listSubCategories);

                    expensesCategoryRepository.save(expenseCategory);
                    return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

                }
                else {
                    return new ResponseEntity<>(Utils.SUB_CATEGORY_NAME_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
                }
             }
        }
        else {
            return new ResponseEntity<>(Utils.CATEGORY_NAME_CATEGORY_ID_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    public boolean checkCategoryNameExists(String categoryName, String hostelId) {
        return expensesCategoryRepository.existsByCategoryNameIgnoreCaseAndHostelId(categoryName, hostelId);
    }

    public ResponseEntity<?> getAllExpenses(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<ExpensesCategories> listCategories = expensesCategoryRepository.findByHostelId(hostelId)
                .stream()
                .map(item -> {
                    List<ExpensesSubCategories> listSubCategories = item.getListSubCategories()
                            .stream()
                            .map(i -> new ExpensesSubCategories(i.getSubCategoryName(), i.getSubCategoryId()))
                            .toList();
                    return new ExpensesCategoryMapper(listSubCategories).apply(item);
                })
                .toList();

        return new ResponseEntity<>(listCategories, HttpStatus.OK);
    }

    public List<ExpensesCategory> getAllActiveCategories(String hostelId) {
        return expensesCategoryRepository.findAllByHostelIdAndIsActiveTrue(hostelId)
                .stream()
                .map(item -> {
                    List<ExpensesSubCategory> listSubCategories= item.getListSubCategories()
                            .stream()
                            .map(i -> {
                                return new ExpensesSubCategory(i.getSubCategoryId(), i.getSubCategoryName());
                            })
                            .toList();
                    return new ExpensesCategory(item.getCategoryId(),
                            item.getCategoryName(),
                            listSubCategories);
                }).toList();
    }

    public boolean checkCategoryHavingSubCategory(String hostelId, Long categoryId) {
        com.smartstay.smartstay.dao.ExpenseCategory category = expensesCategoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return false;
        }
        long counts = category.getListSubCategories()
                .stream()
                .filter(ExpenseSubCategory::isActive)
                .count();

        return counts > 0;
    }
}
