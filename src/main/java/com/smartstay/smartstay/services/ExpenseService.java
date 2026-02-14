package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.expenses.ExpenseListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.expense.Expense;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.responses.Reports.TenantRegisterResponse;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.responses.expenses.ExpenseList;
import com.smartstay.smartstay.responses.expenses.InitializeExpenses;
import com.smartstay.smartstay.responses.expenseForReport.ExpenseReportResponse;
import com.smartstay.smartstay.dto.expenses.ExpenseSummaryProjection;
import com.smartstay.smartstay.dao.ExpenseCategory;
import com.smartstay.smartstay.dao.ExpenseSubCategory;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    private HostelService hostelService;

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

        if (expense.count() == null) {
            return new ResponseEntity<>(Utils.EXPENSE_COUNT_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        try {
            int count = Integer.parseInt(expense.count().toString());
            if (count == 0) {
                return new ResponseEntity<>(Utils.INVALID_COUNT, HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            return new ResponseEntity<>(Utils.INVALID_COUNT, HttpStatus.BAD_REQUEST);
        }

        boolean hasSubCategory = expenseCategoryService.checkCategoryHavingSubCategory(hostelId, expense.categoryId());
        if (hasSubCategory) {
            if (!Utils.checkNullOrEmpty(expense.subCategory())) {
                return new ResponseEntity<>(Utils.SUB_CATEGORY_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }
        }

        Integer count = 1;
        if (Utils.checkNullOrEmpty(expense.count())) {
            count = expense.count();
        }
        double unitPrice = 0.0;
        if (expense.totalAmount() != null) {
            unitPrice = expense.totalAmount() / count;
        }

        String expenseNumber = generateExpenseNumber(hostelId);
        ExpensesV1 expensesV1 = new ExpensesV1();
        expensesV1.setCategoryId(expense.categoryId());
        expensesV1.setSubCategoryId(expense.subCategory());
        expensesV1.setParentId(users.getParentId());
        expensesV1.setHostelId(hostelId);
        expensesV1.setBankId(expense.bankId());
        expensesV1.setUnitPrice(unitPrice);
        expensesV1.setUnitCount(count);
        expensesV1.setTotalPrice(expense.totalAmount());
        expensesV1.setExpenseNumber(expenseNumber);

        expensesV1.setTransactionAmount(expense.totalAmount());
        expensesV1.setSource(ExpenseSource.EXPENSE.name());
        expensesV1.setTransactionDate(
                Utils.stringToDate(expense.purchaseDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
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
                expense.purchaseDate(),
                expenseNumber);

        ExpensesV1 expV1 = expensesRepository.save(expensesV1);

        usersService.addUserLog(hostelId, expV1.getExpenseId(), ActivitySource.EXPENSE, ActivitySourceType.CREATE, users);
        if (bankTransactionService.addExpenseTransaction(transactionDto, expV1.getExpenseId())) {

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

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return expensesRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public Double sumAmountByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return expensesRepository.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public ExpenseReportResponse getExpenseReportDetails(String hostelId, String period, String customStartDate,
            String customEndDate, List<Long> categoryIds, List<Long> subCategoryIds,
            List<String> paymentModes, List<String> paidTo, List<String> createdBy,
            int page, int size) {

        Date startDate = null;
        Date endDate = null;

        if (customStartDate != null && customEndDate != null) {
            startDate = Utils.stringToDate(customStartDate, Utils.USER_INPUT_DATE_FORMAT);
            endDate = Utils.stringToDate(customEndDate, Utils.USER_INPUT_DATE_FORMAT);
        } else {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            if ("THIS_MONTH".equalsIgnoreCase(period)) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
            } else if ("LAST_3_MONTHS".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -3);
                startDate = cal.getTime();
            } else if ("LAST_6_MONTHS".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -6);
                startDate = cal.getTime();
            } else {
                BillingDates bd = hostelService.getCurrentBillStartAndEndDates(hostelId);
                startDate = bd.currentBillStartDate();
                endDate = bd.currentBillEndDate();
            }
        }

        List<String> bankIdsFromModes = null;
        if (paymentModes != null && !paymentModes.isEmpty()) {
            List<String> normalizedModes = paymentModes.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
            bankIdsFromModes = bankingService.findBankIdsByAccountTypes(hostelId, normalizedModes);
            if (bankIdsFromModes.isEmpty()) {
                return buildEmptyResponse(hostelId, startDate, endDate, page, size);
            }
        }

        List<String> bankIdsFromPaidTo = null;
        if (paidTo != null && !paidTo.isEmpty()) {
            bankIdsFromPaidTo = bankingService.findBankIdsByAccountHolderNames(hostelId, paidTo);
            if (bankIdsFromPaidTo.isEmpty()) {
                return buildEmptyResponse(hostelId, startDate, endDate, page, size);
            }
        }

        List<String> finalBankIds = null;
        if (bankIdsFromModes != null && bankIdsFromPaidTo != null) {
            finalBankIds = bankIdsFromModes.stream().filter(bankIdsFromPaidTo::contains).collect(Collectors.toList());
            if (finalBankIds.isEmpty()) {
                return buildEmptyResponse(hostelId, startDate, endDate, page, size);
            }
        } else if (bankIdsFromModes != null) {
            finalBankIds = bankIdsFromModes;
        } else if (bankIdsFromPaidTo != null) {
            finalBankIds = bankIdsFromPaidTo;
        }

        ExpenseSummaryProjection summaryProj = expensesRepository.getExpenseSummary(hostelId, categoryIds,
                subCategoryIds, finalBankIds, null, createdBy, startDate, endDate);
        long totalRecords = (summaryProj != null) ? summaryProj.getTotalRecords() : 0;
        Double totalAmount = (summaryProj != null) ? summaryProj.getTotalAmount() : 0.0;

        Pageable pageable = PageRequest.of(page, size);
        List<ExpensesV1> expenses = expensesRepository.findExpensesWithFiltersV2(hostelId, categoryIds,
                subCategoryIds, finalBankIds, null, createdBy, startDate, endDate, pageable);

        Set<Long> catIds = expenses.stream().map(ExpensesV1::getCategoryId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> bIds = expenses.stream().map(ExpensesV1::getBankId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> uIds = expenses.stream().map(ExpensesV1::getCreatedBy).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ExpenseCategory> categoryMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            catIds.forEach(id -> {
                ExpenseCategory cat = expenseCategoryService.getExpenseCategoryById(id);
                if (cat != null)
                    categoryMap.put(id, cat);
            });
        }

        Map<String, BankingV1> bankMap = new HashMap<>();
        if (!bIds.isEmpty()) {
            bankingService.findAllBanksById(bIds).forEach(b -> bankMap.put(b.getBankId(), b));
        }

        Map<String, Users> userMap = new HashMap<>();
        if (!uIds.isEmpty()) {
            usersService.findAllUsersFromUserId(uIds.stream().toList()).forEach(u -> userMap.put(u.getUserId(), u));
        }

        List<ExpenseReportResponse.ExpenseDetail> details = expenses.stream().map(e -> {
            ExpenseCategory cat = categoryMap.get(e.getCategoryId());
            String catName = (cat != null) ? cat.getCategoryName() : null;
            String subCatName = null;
            if (cat != null && e.getSubCategoryId() != null && cat.getListSubCategories() != null) {
                subCatName = cat.getListSubCategories().stream()
                        .filter(s -> s.getSubCategoryId().equals(e.getSubCategoryId()))
                        .map(ExpenseSubCategory::getSubCategoryName).findFirst().orElse(null);
            }

            BankingV1 b = bankMap.get(e.getBankId());
            String pMode = (b != null) ? Utils.capitalize(b.getAccountType()) : null;
            String account = (b != null) ? (b.getAccountHolderName() + "-" + Utils.capitalize(b.getAccountType()))
                    : null;

            Users u = userMap.get(e.getCreatedBy());
            String creatorName = (u != null)
                    ? (u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : ""))
                    : null;

            return ExpenseReportResponse.ExpenseDetail.builder()
                    .expenseId(e.getExpenseId())
                    .date(Utils.dateToString(e.getTransactionDate()))
                    .expenseCategory(catName)
                    .expenseSubCategory(subCatName)
                    .description(e.getDescription())
                    .counts(e.getUnitCount() != null ? e.getUnitCount() : 0)
                    .assetName(null)
                    .vendorName(null)
                    .paymentMode(pMode)
                    .account(account)
                    .amount(e.getTransactionAmount())
                    .createdBy(creatorName != null ? creatorName.trim() : null)
                    .build();
        }).collect(Collectors.toList());

        ExpenseReportResponse.FiltersData filtersData = buildFiltersData(hostelId);

        int totalPages = (int) Math.ceil((double) totalRecords / size);

        return ExpenseReportResponse.builder()
                .hostelId(hostelId)
                .filtersData(filtersData)
                .summary(ExpenseReportResponse.Summary.builder()
                        .totalExpenses(totalRecords)
                        .totalAmount(totalAmount)
                        .startDate(Utils.dateToString(startDate))
                        .endDate(Utils.dateToString(endDate))
                        .build())
                .pagination(ExpenseReportResponse.Pagination.builder()
                        .currentPage(page)
                        .pageSize(size)
                        .totalPages(totalPages)
                        .totalRecords(totalRecords)
                        .hasNext(page < totalPages - 1)
                        .hasPrevious(page > 0)
                        .build())
                .expenseLists(details)
                .build();
    }

    private ExpenseReportResponse.FiltersData buildFiltersData(String hostelId) {

        List<ExpenseReportResponse.FilterItem> periodList = new ArrayList<>();
        periodList.add(new ExpenseReportResponse.FilterItem("THIS_MONTH", "This Month"));
        periodList.add(new ExpenseReportResponse.FilterItem("LAST_3_MONTHS", "Last Month"));
        periodList.add(new ExpenseReportResponse.FilterItem("LAST_6_MONTHS", "Last 6 Months"));


        List<com.smartstay.smartstay.dto.expenses.ExpensesCategory> allCategories = expenseCategoryService
                .getAllActiveCategories(hostelId);

        List<ExpenseReportResponse.CategoryFilter> catFilters = allCategories.stream()
                .map(c -> new ExpenseReportResponse.CategoryFilter(c.categoryId(), c.categoryName()))
                .collect(Collectors.toList());

        List<ExpenseReportResponse.SubCategoryFilter> subCatFilters = allCategories.stream()
                .flatMap(c -> c.subCategories().stream())
                .map(s -> new ExpenseReportResponse.SubCategoryFilter(s.subCategoryId(), s.subCategoryName()))
                .distinct()
                .collect(Collectors.toList());

        List<ExpenseReportResponse.UserFilter> creators = usersService.findAllUsersByHostelId(hostelId).stream()
                .map(u -> new ExpenseReportResponse.UserFilter(u.getUserId(),
                        u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "")))
                .collect(Collectors.toList());

        List<String> paymentModes = Arrays.stream(com.smartstay.smartstay.ennum.BankAccountType.values())
                .map(e -> Utils.capitalize(e.name()))
                .collect(Collectors.toList());

        return ExpenseReportResponse.FiltersData.builder()
                .period(periodList)
                .category(catFilters)
                .subCategory(subCatFilters)
                .createdBy(creators)
                .paymentMode(paymentModes)
                .build();
    }

    private ExpenseReportResponse buildEmptyResponse(String hostelId, Date startDate, Date endDate, int page,
            int size) {
        return ExpenseReportResponse.builder()
                .hostelId(hostelId)
                .filtersData(buildFiltersData(hostelId))
                .summary(ExpenseReportResponse.Summary.builder()
                        .totalExpenses(0)
                        .totalAmount(0.0)
                        .startDate(Utils.dateToString(startDate))
                        .endDate(Utils.dateToString(endDate))
                        .build())
                .pagination(ExpenseReportResponse.Pagination.builder()
                        .currentPage(page)
                        .pageSize(size)
                        .totalPages(0)
                        .totalRecords(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .expenseLists(new ArrayList<>())
                .build();
    }
}
