package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.expense.Expense;
import com.smartstay.smartstay.payloads.expense.ExpenseCategory;
import com.smartstay.smartstay.services.ExpenseCategoryService;
import com.smartstay.smartstay.services.ExpenseService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/expense/")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ExpenseController {
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/category/{hostelId}")
    public ResponseEntity<?> addExpenseCategory(@PathVariable("hostelId") String hostelId, @RequestBody(required = false) ExpenseCategory expenseCategory) {
        return expenseCategoryService.createExpenseCategory(expenseCategory, hostelId);
    }

    @GetMapping("/category/{hostelId}")
    public ResponseEntity<?> getExpenseCategory(@PathVariable("hostelId") String hostelId) {
        return expenseCategoryService.getAllExpenses(hostelId);
    }

    @GetMapping("/initialize/{hostelId}")
    public ResponseEntity<?> initializeToAddExpense(@PathVariable("hostelId") String hostelId) {
        return expenseService.initializeToAddExpense(hostelId);
    }

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addExpenses(@PathVariable("hostelId") String hostelId, @RequestBody @Valid Expense expense) {
        return expenseService.addExpense(hostelId, expense);
    }
}
