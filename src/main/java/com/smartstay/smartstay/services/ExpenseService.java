package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.expenses.ExpenseListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.ennum.BankSource;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.ExpenseSource;
import com.smartstay.smartstay.payloads.expense.Expense;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.responses.expenses.ExpenseList;
import com.smartstay.smartstay.responses.expenses.InitializeExpenses;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private BankTransactionService bankTransactionService;

    public ResponseEntity<?> initializeToAddExpense(String hostelId) {
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

        List<ExpensesCategory> listExpensesCategory = expenseCategoryService.getAllActiveCategories(hostelId);
        List<DebitsBank> listBanks = bankingService.getAllBankForReturn(hostelId);

        InitializeExpenses initializeExpenses = new InitializeExpenses(listExpensesCategory, listBanks);

        return new ResponseEntity<>(initializeExpenses, HttpStatus.OK);

    }

    public ResponseEntity<?> addExpense(String hostelId, Expense expense) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!bankingService.checkBankExist(expense.bankId())) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }

        boolean hasSubCategory = expenseCategoryService.checkCategoryHavingSubCategory(hostelId, expense.categoryId());
        if (hasSubCategory) {
            if (!Utils.checkNullOrEmpty(expense.subCategory())) {
                return new ResponseEntity<>(Utils.SUB_CATEGORY_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }
        }

        double unitPrice = 0.0;
        if (expense.totalAmount() != null) {
            unitPrice = expense.totalAmount() / expense.count();
        }

        ExpensesV1 expensesV1 = new ExpensesV1();
        expensesV1.setCategoryId(expense.categoryId());
        expensesV1.setSubCategoryId(expense.subCategory());
        expensesV1.setParentId(users.getParentId());
        expensesV1.setHostelId(hostelId);
        expensesV1.setBankId(expense.bankId());
        expensesV1.setUnitPrice(unitPrice);
        expensesV1.setUnitCount(expense.count());
        expensesV1.setTotalPrice(expense.totalAmount());
        expensesV1.setExpenseNumber(generateExpenseNumber(hostelId));

        expensesV1.setTransactionAmount(expense.totalAmount());
        expensesV1.setSource(ExpenseSource.EXPENSE.name());
        expensesV1.setTransactionDate(Utils.stringToDate(expense.purchaseDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        expensesV1.setCreatedAt(new Date());
        expensesV1.setCreatedBy(authentication.getName());
        expensesV1.setActive(true);
        expensesV1.setDescription(expense.description());

        TransactionDto transactionDto = new TransactionDto(expense.bankId(),
                expensesV1.getExpenseNumber(),
                expense.totalAmount(),
                BankTransactionType.DEBIT.name(),
                BankSource.EXPENSE.name(),
                hostelId,
                expense.purchaseDate());

        if (bankTransactionService.addExpenseTransaction(transactionDto)) {
            expensesRepository.save(expensesV1);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(Utils.INSUFFICIENT_FUND_ERROR, HttpStatus.BAD_REQUEST);
        }

    }

    public String generateExpenseNumber(String hostelId) {
        int randomNumber = Utils.generateExpenseNumber();
        StringBuilder randomRefNumber = new StringBuilder();
        randomRefNumber.append("#REF-");
        randomRefNumber.append(randomNumber);

        if (!checkRandomNumberExistsOrNot(randomRefNumber.toString(), hostelId)) {
            return generateExpenseNumber(hostelId);
        }
        return randomRefNumber.toString();

    }

    private boolean checkRandomNumberExistsOrNot(String randomNumber, String hostelId) {
        if (expensesRepository.findByExpenseNumberAndHostelId(randomNumber, hostelId) == null) {
            return true;
        }
        return false;
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

        List<ExpenseList> listExpenses = expensesRepository.findAllExpensesByHostelId(hostelId)
                .stream()
                .map(item -> new ExpenseListMapper().apply(item))
                .toList();


        return new ResponseEntity<>(listExpenses, HttpStatus.OK);
    }
}
