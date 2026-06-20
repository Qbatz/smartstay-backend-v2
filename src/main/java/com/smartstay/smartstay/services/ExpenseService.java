package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.expenses.ExpenseListMapper;
import com.smartstay.smartstay.Wrappers.expenses.ExpenseTableMapper;
import com.smartstay.smartstay.Wrappers.expenses.VendorExpenseSummaryMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.ExpenseItem;
import com.smartstay.smartstay.dao.ExpensePayment;
import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.dao.Units;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.expenses.ExpensesCategory;
import com.smartstay.smartstay.dto.expenses.ExpenseSummaryView;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.expense.AddUnit;
import com.smartstay.smartstay.payloads.expense.Expense;
import com.smartstay.smartstay.payloads.expense.ExpenseItemPayload;
import com.smartstay.smartstay.payloads.expense.RecordExpensePayment;
import com.smartstay.smartstay.payloads.expense.SettleExpensePayment;
import com.smartstay.smartstay.payloads.expense.SettleVendorExpense;
import com.smartstay.smartstay.payloads.expense.SettleVendorPayment;
import com.smartstay.smartstay.payloads.expense.UpdateExpense;
import com.smartstay.smartstay.repositories.ExpenseItemRepository;
import com.smartstay.smartstay.repositories.ExpensePaymentRepository;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.repositories.UnitsRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.expenses.ExpenseDetailItem;
import com.smartstay.smartstay.responses.expenses.ExpenseDetailPayment;
import com.smartstay.smartstay.responses.expenses.ExpenseDetailResponse;
import com.smartstay.smartstay.responses.expenses.ExpenseFilterOptions;
import com.smartstay.smartstay.responses.expenses.ExpenseItemResponse;
import com.smartstay.smartstay.responses.expenses.ExpensePaymentResponse;
import com.smartstay.smartstay.responses.expenses.ExpenseSummary;
import com.smartstay.smartstay.responses.expenses.ExpensesMobileResponse;
import com.smartstay.smartstay.responses.expenses.ExpensesWebResponse;
import com.smartstay.smartstay.responses.expenses.UnitResponse;
import com.smartstay.smartstay.responses.vendor.VendorExpenseSummary;
import com.smartstay.smartstay.responses.vendor.VendorInitialize;
import com.smartstay.smartstay.responses.vendor.VendorInitializeResponse;
import com.smartstay.smartstay.responses.Reports.TenantRegisterResponse;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.responses.expenses.ExpenseList;
import com.smartstay.smartstay.responses.expenses.InitializeExpenses;
import com.smartstay.smartstay.responses.expenseForReport.ExpenseReportResponse;
import com.smartstay.smartstay.dto.expenses.ExpenseSummaryProjection;
import com.smartstay.smartstay.dao.ExpenseCategory;
import com.smartstay.smartstay.dao.ExpenseSubCategory;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UnitsRepository unitsRepository;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private ExpensePaymentRepository expensePaymentRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorFinancialService vendorFinancialService;

    @Autowired
    private TableColumnService columnService;

    @Value("${expense.payment.max-images:5}")
    private int maxPaymentImages;

    @Autowired
    private UploadFileToS3 uploadToS3;

    public ResponseEntity<?> addUnit(AddUnit payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String hostelId = payloads.hostelId();
        String unitName = payloads.unitName().trim();
        Units existingUnit = unitsRepository.findByUnitNameIgnoreCaseAndHostelId(unitName, hostelId);
        if (existingUnit != null) {
            if (existingUnit.isEnabled()) {
                return new ResponseEntity<>(Utils.UNIT_ALREADY_ADDED, HttpStatus.BAD_REQUEST);
            }
            existingUnit.setEnabled(true);
            existingUnit.setModifiedAt(new Date());
            existingUnit.setModifiedBy(userId);
            unitsRepository.save(existingUnit);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }

        Units units = new Units();
        units.setUnitName(unitName);
        units.setHostelId(hostelId);
        units.setEnabled(true);
        units.setAddedBy(userId);
        units.setCreatedAt(new Date());
        units.setModifiedAt(new Date());
        units.setModifiedBy(userId);
        unitsRepository.save(units);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateUnit(int unitId, AddUnit payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String hostelId = payloads.hostelId();
        Units existingUnit = unitsRepository.findByUnitIdAndHostelId(unitId, hostelId);
        if (existingUnit == null || !existingUnit.isEnabled()) {
            return new ResponseEntity<>(Utils.INVALID_UNIT, HttpStatus.BAD_REQUEST);
        }

        String unitName = payloads.unitName().trim();
        Units duplicateUnit = unitsRepository.findByUnitNameIgnoreCaseAndHostelId(unitName, hostelId);
        if (duplicateUnit != null && duplicateUnit.isEnabled() && !duplicateUnit.getUnitId().equals(unitId)) {
            return new ResponseEntity<>(Utils.UNIT_ALREADY_ADDED, HttpStatus.BAD_REQUEST);
        }

        existingUnit.setUnitName(unitName);
        existingUnit.setModifiedAt(new Date());
        existingUnit.setModifiedBy(userId);
        unitsRepository.save(existingUnit);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllUnits(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<UnitResponse> units = unitsRepository.findAllEnabledUnitsByHostelId(hostelId);
        return new ResponseEntity<>(units, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteUnit(int unitId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Units existingUnit = unitsRepository.findByUnitIdAndHostelId(unitId, hostelId);
        if (existingUnit == null || !existingUnit.isEnabled()) {
            return new ResponseEntity<>(Utils.INVALID_UNIT, HttpStatus.BAD_REQUEST);
        }

        existingUnit.setEnabled(false);
        existingUnit.setModifiedAt(new Date());
        existingUnit.setModifiedBy(userId);
        unitsRepository.save(existingUnit);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

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

        // Vendor financials are denormalized on the vendor row, so this is a single read with no
        // per-vendor aggregation (no N+1). Only the fields needed by the picker are exposed.
        List<VendorInitializeResponse> listVendors = vendorRepository
                .findByHostelIdAndIsActiveTrueOrderByVendorIdDesc(hostelId).stream()
                .map(v -> new VendorInitializeResponse(
                        v.getVendorId(),
                        v.getBusinessName(),
                        v.getPaymentStatus() != null ? v.getPaymentStatus().name() : null,
                        nullSafe(v.getTotalExpense()),
                        nullSafe(v.getTotalPaid()),
                        nullSafe(v.getBalance())))
                .toList();

        InitializeExpenses initializeExpenses = new InitializeExpenses(hostelId, listExpensesCategory, listBanks, listVendors);

        return new ResponseEntity<>(initializeExpenses, HttpStatus.OK);

    }

    /**
     * Initialize data for settling a vendor's expenses: the hostel's banks (same source as
     * {@link #initializeToAddExpense}) plus a lightweight summary of every expense raised against
     * the vendor. The expense summaries are produced by a single projection query (no N+1).
     */
    public ResponseEntity<?> initializeVendorSettlement(String hostelId, int vendorId) {
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

        // Vendor must exist and belong to the supplied hostel.
        VendorV1 vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null || !hostelId.equalsIgnoreCase(vendor.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.BAD_REQUEST);
        }

        List<DebitsBank> listBanks = bankingService.getAllBankForReturn(hostelId);

        // Map entities -> summary via a dedicated mapper (no JPQL constructor projection).
        VendorExpenseSummaryMapper expenseSummaryMapper = new VendorExpenseSummaryMapper();
        List<VendorExpenseSummary> expenses = expensesRepository
                .findByVendorIdAndIsActiveTrueOrderByTransactionDateDesc(String.valueOf(vendorId))
                .stream()
                .map(expenseSummaryMapper)
                .toList();

        VendorInitialize response = new VendorInitialize(hostelId, String.valueOf(vendorId), listBanks, expenses);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addExpense(String hostelId, MultipartFile[] images, Expense expense) {
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
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        // Reject more than the configured number of images before doing any work / uploads.
        if (exceedsImageLimit(images)) {
            return new ResponseEntity<>(Utils.MAX_IMAGES_EXCEEDED + ". Allowed: " + maxPaymentImages, HttpStatus.BAD_REQUEST);
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
        expensesV1.setTitle(expense.title());
        expensesV1.setIsVendorExpense(expense.isVendorExpense());
        String vendorId = expense.vendorId() == null ? null : String.valueOf(expense.vendorId());
        expensesV1.setVendorId(vendorId);
        expensesV1.setPaymentStatus(ExpensePaymentStatus.fromString(expense.paymentStatus()));
        if (Boolean.TRUE.equals(expense.isVendorExpense()) && expense.vendorId() != null) {
            VendorV1 vendor = vendorRepository.findByVendorId(expense.vendorId());
            if (vendor != null) {
                expensesV1.setCreditPeriod(vendor.getCreditPeriod());
            }
        }
        expensesV1.setPaidAmount(expense.paidAmount());
        expensesV1.setBalanceAmount(expense.balanceAmount());
        expensesV1.setPaymentMethod(expense.paymentMethod());
        expensesV1.setNote(expense.note());
        expensesV1.setTransactionId(expense.transactionId());
        expensesV1.setTax(expense.tax());
        expensesV1.setDiscount(expense.discount());
        // Upload receipt images to S3; a failure aborts the (transactional) expense creation.
        expensesV1.setImages(uploadImages(images, "Expense/Images"));

        TransactionDto transactionDto = new TransactionDto(expense.bankId(),
                expensesV1.getExpenseNumber(),
                expense.totalAmount(),
                BankTransactionType.DEBIT.name(),
                BankSource.EXPENSE.name(),
                hostelId,
                expense.purchaseDate(),
                expenseNumber);

        ExpensesV1 expV1 = expensesRepository.save(expensesV1);

        if (expense.expenseItems() != null && !expense.expenseItems().isEmpty()) {
            String auditUser = authentication.getName();
            Date auditNow = new Date();
            List<ExpenseItem> expenseItems = new ArrayList<>();
            for (ExpenseItemPayload itemPayload : expense.expenseItems()) {
                ExpenseItem expenseItem = new ExpenseItem();
                expenseItem.setExpenseId(expV1.getExpenseId());
                expenseItem.setHostelId(hostelId);
                expenseItem.setVendorId(vendorId);
                expenseItem.setItem(itemPayload.item());
                expenseItem.setQuantity(itemPayload.quantity());
                expenseItem.setUnit(itemPayload.unit());
                expenseItem.setUnitPrice(itemPayload.unitPrice());
                expenseItem.setTotalAmount(itemPayload.totalAmount());
                stampCreate(expenseItem, auditUser, auditNow);
                expenseItems.add(expenseItem);
            }
            // Seed each item's payment details from the parent expense, then persist in one batch.
            initializeItemPayments(expenseItems, expensesV1.getPaymentStatus(), expense.paidAmount());
            expenseItemRepository.saveAll(expenseItems);
        }

        if (expense.paidAmount() != null && expense.paidAmount() > 0) {
            ExpensePayment expensePayment = new ExpensePayment();
            expensePayment.setExpenseId(expV1.getExpenseId());
            expensePayment.setHostelId(hostelId);
            expensePayment.setVendorId(vendorId);
            expensePayment.setPaidAmount(expense.paidAmount());
            expensePayment.setPaymentMethod(expense.paymentMethod());
            expensePayment.setBankId(expense.bankId());
            expensePayment.setPaymentDate(expensesV1.getTransactionDate());
            expensePayment.setNotes(expense.note());
            stampCreate(expensePayment, authentication.getName(), new Date());
            expensePaymentRepository.save(expensePayment);
        }

        // Keep the vendor's denormalized financial summary in sync.
        if (vendorId != null) {
            vendorFinancialService.recalculate(vendorId);
        }

        usersService.addUserLog(hostelId, expV1.getExpenseId(), ActivitySource.EXPENSE, ActivitySourceType.CREATE, users);
        if (bankTransactionService.addExpenseTransaction(transactionDto, expV1.getExpenseId())) {

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(Utils.INSUFFICIENT_FUND_ERROR, HttpStatus.BAD_REQUEST);
        }

    }

    @Transactional
    public ResponseEntity<?> recordExpensePayment(MultipartFile[] images, RecordExpensePayment payload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ExpensesV1 expensesV1 = expensesRepository.findById(payload.expenseId()).orElse(null);
        if (expensesV1 == null || !expensesV1.isActive()) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), expensesV1.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(expensesV1.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        // Reject more than the configured number of images before uploading anything to S3.
        if (exceedsImageLimit(images)) {
            return new ResponseEntity<>(Utils.MAX_IMAGES_EXCEEDED + ". Allowed: " + maxPaymentImages, HttpStatus.BAD_REQUEST);
        }

        List<String> imageUrls = uploadPaymentImages(images);

        String auditUser = authentication.getName();
        Date auditNow = new Date();
        Date paymentDate = resolvePaymentDate(payload.paymentDate());

        // Every payment is recorded as a new row in expense_payments (full payment history),
        // with all uploaded receipt URLs stored in image_urls.
        ExpensePayment expensePayment = buildPayment(expensesV1.getExpenseId(), null, expensesV1.getHostelId(),
                expensesV1.getVendorId(), payload.amount(), payload.paymentMethod(), payload.bankId(),
                paymentDate, payload.transactionId(), payload.notes(), imageUrls, auditUser, auditNow);
        expensePaymentRepository.save(expensePayment);

        Double totalPaid = expensePaymentRepository.sumPaidAmountByExpenseId(expensesV1.getExpenseId());
        if (totalPaid == null) {
            totalPaid = 0.0;
        }
        double totalAmount = expensesV1.getTotalPrice() == null ? 0.0 : expensesV1.getTotalPrice();

        ExpensePaymentStatus status;
        if (totalPaid <= 0) {
            status = ExpensePaymentStatus.Pending;
        } else if (totalPaid >= totalAmount) {
            status = ExpensePaymentStatus.Full;
        } else {
            status = ExpensePaymentStatus.Partial;
        }

        expensesV1.setPaymentStatus(status);
        expensesV1.setPaidAmount(totalPaid);
        expensesV1.setBalanceAmount(totalAmount - totalPaid);
        expensesV1.setUpdatedAt(new Date());
        expensesV1.setUpdatedBy(authentication.getName());
        expensesRepository.save(expensesV1);

        // Keep the vendor's denormalized financial summary in sync with the new payment.
        if (expensesV1.getVendorId() != null) {
            vendorFinancialService.recalculate(expensesV1.getVendorId());
        }

        usersService.addUserLog(expensesV1.getHostelId(), expensesV1.getExpenseId(), ActivitySource.EXPENSE, ActivitySourceType.UPDATE, users);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    /**
     * Settles a single expense. The incoming paid amount is added cumulatively to the expense's
     * existing paid amount, the balance and payment status are recomputed on {@code expensesv1}, and
     * a payment-history row (with the uploaded receipt images) is recorded. {@code expense_items} are
     * intentionally left untouched. Shares its settlement logic with
     * {@link #settleVendorExpenses(String, MultipartFile[], SettleVendorPayment)}. All writes happen
     * in one transaction so a failure rolls everything back.
     */
    @Transactional
    public ResponseEntity<?> settleExpense(String expenseId, MultipartFile[] images, SettleExpensePayment payload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ExpensesV1 expense = expensesRepository.findById(expenseId).orElse(null);
        if (expense == null || !expense.isActive()) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), expense.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(expense.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        String methodError = validatePaymentMethod(payload.paymentMethod(), payload.transactionId());
        if (methodError != null) {
            return new ResponseEntity<>(methodError, HttpStatus.BAD_REQUEST);
        }

        double requestPaid = payload.paidAmount() != null ? payload.paidAmount() : 0.0;
        String settleError = validateExpenseSettlement(expense, requestPaid);
        if (settleError != null) {
            return new ResponseEntity<>(settleError, HttpStatus.BAD_REQUEST);
        }

        if (exceedsImageLimit(images)) {
            return new ResponseEntity<>(Utils.MAX_IMAGES_EXCEEDED + ". Allowed: " + maxPaymentImages, HttpStatus.BAD_REQUEST);
        }

        // All validations passed — upload receipts, then persist within the transaction.
        List<String> imageUrls = uploadPaymentImages(images);

        String auditUser = authentication.getName();
        Date auditNow = new Date();
        Date paymentDate = resolvePaymentDate(payload.paymentDate());

        // Update only the parent expense; expense_items are not modified.
        applyExpenseSettlement(expense, requestPaid, auditUser, auditNow);
        expensesRepository.save(expense);

        ExpensePayment payment = buildPayment(expense.getExpenseId(), null, expense.getHostelId(),
                expense.getVendorId(), requestPaid, payload.paymentMethod(), payload.bankId(),
                paymentDate, payload.transactionId(), payload.notes(), imageUrls, auditUser, auditNow);
        expensePaymentRepository.save(payment);

        // Keep the vendor's denormalized financial summary in sync.
        if (expense.getVendorId() != null) {
            vendorFinancialService.recalculate(expense.getVendorId());
        }

        usersService.addUserLog(expense.getHostelId(), expense.getExpenseId(), ActivitySource.EXPENSE, ActivitySourceType.UPDATE, users);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    /**
     * Settles one or more of a vendor's expenses in a single request: for each expense the incoming
     * paid amount is added cumulatively to the existing paid amount, the balance and payment status
     * are recomputed, and a payment-history row (with the uploaded receipt images) is recorded.
     * {@code expense_items} are intentionally left untouched. All writes happen in one transaction
     * so a failure rolls everything back.
     */
    @Transactional
    public ResponseEntity<?> settleVendorExpenses(String vendorId, MultipartFile[] images, SettleVendorPayment payload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        // Vendor must exist.
        Integer vendorKey = parseVendorId(vendorId);
        if (vendorKey == null || vendorRepository.findByVendorId(vendorKey) == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.BAD_REQUEST);
        }
        String vendorIdStr = String.valueOf(vendorKey);

        // Payment-method based validation.
        String methodError = validatePaymentMethod(payload.paymentMethod(), payload.transactionId());
        if (methodError != null) {
            return new ResponseEntity<>(methodError, HttpStatus.BAD_REQUEST);
        }

        // Reject blank / duplicate expense ids.
        List<SettleVendorExpense> requestedExpenses = payload.expenses();
        List<String> requestedIds = requestedExpenses.stream().map(SettleVendorExpense::expenseId).toList();
        if (requestedIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }
        if (requestedIds.stream().distinct().count() != requestedIds.size()) {
            return new ResponseEntity<>(Utils.DUPLICATE_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }

        // Bulk-load all requested expenses in one query (no N+1).
        Map<String, ExpensesV1> expensesById = expensesRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(ExpensesV1::getExpenseId, expense -> expense));

        // Phase 1 — validate every expense without mutating, so a later failure never leaves a
        // partial settlement behind.
        List<ExpensesV1> expensesToUpdate = new ArrayList<>();
        for (SettleVendorExpense requested : requestedExpenses) {
            ExpensesV1 expense = expensesById.get(requested.expenseId());
            if (expense == null || !expense.isActive()) {
                return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
            }
            // Must belong to the supplied vendor.
            if (expense.getVendorId() == null || !expense.getVendorId().equals(vendorIdStr)) {
                return new ResponseEntity<>(Utils.VENDOR_EXPENSE_MISMATCH, HttpStatus.BAD_REQUEST);
            }
            // A vendor's expenses could span hostels; guard access per expense.
            if (!userHostelService.checkHostelAccess(users.getUserId(), expense.getHostelId())) {
                return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
            }
            String settleError = validateExpenseSettlement(expense, requested.paidAmount() != null ? requested.paidAmount() : 0.0);
            if (settleError != null) {
                return new ResponseEntity<>(settleError, HttpStatus.BAD_REQUEST);
            }
            expensesToUpdate.add(expense);
        }

        // Validate image count before uploading anything to S3.
        if (exceedsImageLimit(images)) {
            return new ResponseEntity<>(Utils.MAX_IMAGES_EXCEEDED + ". Allowed: " + maxPaymentImages, HttpStatus.BAD_REQUEST);
        }

        // All validations passed — upload receipts, then persist everything transactionally.
        List<String> imageUrls = uploadPaymentImages(images);

        String auditUser = authentication.getName();
        Date auditNow = new Date();
        Date paymentDate = resolvePaymentDate(payload.paymentDate());

        // Phase 2 — apply the (validated) settlements.
        for (SettleVendorExpense requested : requestedExpenses) {
            applyExpenseSettlement(expensesById.get(requested.expenseId()),
                    requested.paidAmount() != null ? requested.paidAmount() : 0.0, auditUser, auditNow);
        }
        expensesRepository.saveAll(expensesToUpdate);

        // One payment-history row per expense in the request (no expense item association).
        List<ExpensePayment> payments = new ArrayList<>();
        for (SettleVendorExpense requested : requestedExpenses) {
            ExpensesV1 expense = expensesById.get(requested.expenseId());
            payments.add(buildPayment(expense.getExpenseId(), null, expense.getHostelId(), vendorIdStr,
                    requested.paidAmount() != null ? requested.paidAmount() : 0.0,
                    payload.paymentMethod(), payload.bankId(), paymentDate, payload.transactionId(),
                    payload.notes(), imageUrls, auditUser, auditNow));
        }
        expensePaymentRepository.saveAll(payments);

        for (ExpensesV1 expense : expensesToUpdate) {
            usersService.addUserLog(expense.getHostelId(), expense.getExpenseId(), ActivitySource.EXPENSE, ActivitySourceType.UPDATE, users);
        }

        // Keep the vendor's denormalized financial summary in sync.
        vendorFinancialService.recalculate(vendorKey);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    private ExpensePaymentStatus deriveExpenseStatus(double paid, double total, ExpensesV1 expense) {
        if (total > 0 && paid >= total - 0.0001) {
            return ExpensePaymentStatus.Full;
        }
        // Not fully paid: a vendor expense past its credit period is overdue.
        if (Boolean.TRUE.equals(expense.getIsVendorExpense())
                && expense.getCreditPeriod() != null && expense.getCreditPeriod() > 0
                && expense.getCreatedAt() != null
                && daysSince(expense.getCreatedAt()) > expense.getCreditPeriod()) {
            return ExpensePaymentStatus.Overdue;
        }
        if (paid <= 0) {
            return ExpensePaymentStatus.Pending;
        }
        return ExpensePaymentStatus.Partial;
    }

    private List<String> uploadPaymentImages(MultipartFile[] images) {
        return uploadImages(images, "Expense/Payments");
    }

    /**
     * Uploads the non-empty multipart images to the given S3 folder and returns their URLs.
     * Empty/null files are ignored. Shared by the settlement and add-expense flows.
     */
    private List<String> uploadImages(MultipartFile[] images, String folder) {
        List<String> urls = new ArrayList<>();
        if (images == null) {
            return urls;
        }
        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty()) {
                urls.add(uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), folder));
            }
        }
        return urls;
    }

    // ---- Settlement helpers shared by the expense-level and vendor-level settlement APIs ----

    /**
     * Validates the payment method. Returns an error message, or {@code null} when valid. A payment
     * method is mandatory, and a transaction id is required for any non-cash method.
     */
    private String validatePaymentMethod(String paymentMethod, String transactionId) {
        if (!Utils.checkNullOrEmpty(paymentMethod)) {
            return Utils.PAYMENT_METHOD_REQUIRED;
        }
        if (!"CASH".equalsIgnoreCase(paymentMethod.trim()) && !Utils.checkNullOrEmpty(transactionId)) {
            return Utils.TRANSACTION_ID_REQUIRED;
        }
        return null;
    }

    private boolean exceedsImageLimit(MultipartFile[] images) {
        long imageCount = images == null ? 0 :
                Arrays.stream(images).filter(file -> file != null && !file.isEmpty()).count();
        return imageCount > maxPaymentImages;
    }

    private Date resolvePaymentDate(String paymentDate) {
        return Utils.checkNullOrEmpty(paymentDate)
                ? Utils.stringToDate(paymentDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT)
                : new Date();
    }

    private ExpensePayment buildPayment(String expenseId, Long expenseItemId, String hostelId, String vendorId,
                                        double paidAmount, String paymentMethod, String bankId, Date paymentDate,
                                        String transactionId, String notes, List<String> imageUrls,
                                        String auditUser, Date auditNow) {
        ExpensePayment payment = new ExpensePayment();
        payment.setExpenseId(expenseId);
        payment.setExpenseItemId(expenseItemId);
        payment.setHostelId(hostelId);
        payment.setVendorId(vendorId);
        payment.setPaidAmount(paidAmount);
        payment.setPaymentMethod(paymentMethod);
        payment.setBankId(bankId);
        payment.setPaymentDate(paymentDate);
        payment.setTransactionId(transactionId);
        payment.setNotes(notes);
        payment.setImageUrls(imageUrls);
        stampCreate(payment, auditUser, auditNow);
        return payment;
    }

    /**
     * Validates a cumulative settlement payment against a single expense <em>without</em> mutating
     * it. Returns an error message, or {@code null} when the payment can be applied. Shared by the
     * single-expense and vendor (multi-expense) settlement flows.
     */
    private String validateExpenseSettlement(ExpensesV1 expense, double requestPaid) {
        if (requestPaid < 0) {
            return Utils.PAID_AMOUNT_NEGATIVE;
        }
        double existingPaid = expense.getPaidAmount() != null ? expense.getPaidAmount() : 0.0;
        double total = expense.getTotalPrice() != null ? expense.getTotalPrice() : 0.0;
        // Already fully settled expenses cannot take further payment.
        if (requestPaid > 0 && total > 0 && existingPaid - total >= -0.0001) {
            return Utils.EXPENSE_ALREADY_SETTLED;
        }
        // Cumulative paid must never exceed the expense total.
        if (existingPaid + requestPaid - total > 0.0001) {
            return Utils.EXPENSE_OVERPAID;
        }
        return null;
    }

    /**
     * Applies a (already-validated) cumulative settlement payment to the expense: bumps the paid
     * amount, recomputes the balance and payment status, and stamps the update audit fields.
     */
    private void applyExpenseSettlement(ExpensesV1 expense, double requestPaid, String auditUser, Date auditNow) {
        double existingPaid = expense.getPaidAmount() != null ? expense.getPaidAmount() : 0.0;
        double total = expense.getTotalPrice() != null ? expense.getTotalPrice() : 0.0;
        double newPaid = existingPaid + requestPaid;
        expense.setPaidAmount(newPaid);
        expense.setBalanceAmount(total - newPaid);
        expense.setPaymentStatus(deriveExpenseStatus(newPaid, total, expense));
        expense.setUpdatedAt(auditNow);
        expense.setUpdatedBy(auditUser);
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

    @Transactional
    public ResponseEntity<?> getAllExpenses(String hostelId, String name, Integer categoryId, Integer page, Integer size) {
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

        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        Long categoryFilter = categoryId != null ? categoryId.longValue() : null;
        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        // Pagination, the filtered page, and the summary are identical for web and mobile.
        Page<com.smartstay.smartstay.dto.expenses.ExpenseList> expensePage =
                expensesRepository.findExpensesForHostel(hostelId, searchName, categoryFilter, pageable);
        List<com.smartstay.smartstay.dto.expenses.ExpenseList> projections = expensePage.getContent();
        ExpenseSummary expenseSummary = buildExpenseSummary(hostelId, searchName, categoryFilter);

        int currentPage = expensePage.getPageable().getPageNumber() + 1;
        int totalPages = expensePage.getTotalPages();
        int totalExpenses = (int) expensePage.getTotalElements();

        if ("web".equalsIgnoreCase(authentication.getSource())) {
            return buildExpenseWebResponse(hostelId, projections, expenseSummary, totalExpenses, currentPage, totalPages, pageSize);
        }
        return buildExpenseMobileResponse(projections, expenseSummary, totalExpenses, currentPage, totalPages, pageSize);
    }

    private ExpenseSummary buildExpenseSummary(String hostelId, String name, Long categoryId) {
        ExpenseSummaryView view = expensesRepository.getExpenseListSummary(hostelId, name, categoryId);
        if (view == null) {
            return new ExpenseSummary(0.0, 0.0, 0.0, 0.0);
        }
        return new ExpenseSummary(
                nullSafe(view.getTotalExpenseAmount()),
                nullSafe(view.getTotalPaidAmount()),
                nullSafe(view.getTotalUnPaidAmount()),
                nullSafe(view.getTotalPartialPaidAmount()));
    }

    private double nullSafe(Double value) {
        return value != null ? value : 0.0;
    }

    private ResponseEntity<?> buildExpenseWebResponse(String hostelId,
                                                      List<com.smartstay.smartstay.dto.expenses.ExpenseList> projections,
                                                      ExpenseSummary expenseSummary, int totalExpenses, int currentPage,
                                                      int totalPages, int pageSize) {
        // Resolve the user's configured columns for this hostel; only enabled columns are rendered.
        List<ColumnFilters> listColumns = columnService.getExpenseColumns(hostelId, FilterOptionsModule.MODULE_EXPENSE.name());
        List<String> tableColumns = listColumns.stream()
                .filter(ColumnFilters::isSelected)
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .map(ColumnFilters::getFieldName)
                .toList();

        Map<String, String> vendorNamesById = resolveVendorNames(projections);

        ExpenseTableMapper mapper = new ExpenseTableMapper(tableColumns, vendorNamesById);
        List<List<Object>> rows = projections.stream().map(mapper).collect(Collectors.toList());

        ExpenseFilterOptions filterOptions = buildExpenseFilterOptions(hostelId);
        ExpensesWebResponse response = new ExpensesWebResponse(totalExpenses, currentPage, totalPages, pageSize,
                expenseSummary, filterOptions, tableColumns, listColumns, rows);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<?> buildExpenseMobileResponse(List<com.smartstay.smartstay.dto.expenses.ExpenseList> projections,
                                                         ExpenseSummary expenseSummary, int totalExpenses, int currentPage,
                                                         int totalPages, int pageSize) {
        // Bulk-load items and payments for the page in two queries (no N+1), grouped by expense id.
        List<String> expenseIds = projections.stream()
                .map(com.smartstay.smartstay.dto.expenses.ExpenseList::getExpenseId)
                .toList();
        Map<String, List<ExpenseItemResponse>> itemsByExpense = new HashMap<>();
        Map<String, List<ExpensePaymentResponse>> paymentsByExpense = new HashMap<>();
        if (!expenseIds.isEmpty()) {
            itemsByExpense = expenseItemRepository.findByExpenseIdIn(expenseIds).stream()
                    .collect(Collectors.groupingBy(ExpenseItem::getExpenseId,
                            Collectors.mapping(item -> new ExpenseItemResponse(
                                    item.getId(),
                                    item.getItem(),
                                    item.getQuantity(),
                                    item.getUnitId(),
                                    item.getUnit(),
                                    item.getUnitPrice(),
                                    item.getTotalAmount(),
                                    item.getPaymentStatus(),
                                    item.getPaidAmount()), Collectors.toList())));

            paymentsByExpense = expensePaymentRepository.findByExpenseIdIn(expenseIds).stream()
                    .collect(Collectors.groupingBy(ExpensePayment::getExpenseId,
                            Collectors.mapping(payment -> new ExpensePaymentResponse(
                                    payment.getId(),
                                    payment.getPaidAmount(),
                                    payment.getPaymentMethod(),
                                    payment.getBankId(),
                                    Utils.dateToString(payment.getPaymentDate()),
                                    payment.getTransactionId(),
                                    payment.getNotes(),
                                    payment.getImageUrl()), Collectors.toList())));
        }

        ExpenseListMapper mapper = new ExpenseListMapper();
        Map<String, List<ExpenseItemResponse>> finalItemsByExpense = itemsByExpense;
        Map<String, List<ExpensePaymentResponse>> finalPaymentsByExpense = paymentsByExpense;
        List<ExpenseList> expenses = projections.stream()
                .map(item -> mapper.apply(item,
                        finalItemsByExpense.getOrDefault(item.getExpenseId(), List.of()),
                        finalPaymentsByExpense.getOrDefault(item.getExpenseId(), List.of())))
                .toList();

        // filterOptions / tableHeaders / columnList are intentionally null for mobile.
        ExpensesMobileResponse response = new ExpensesMobileResponse(totalExpenses, currentPage, totalPages, pageSize,
                expenseSummary, null, null, null, expenses);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Map<String, String> resolveVendorNames(List<com.smartstay.smartstay.dto.expenses.ExpenseList> projections) {
        List<Integer> vendorIds = projections.stream()
                .map(com.smartstay.smartstay.dto.expenses.ExpenseList::getVendorId)
                .map(this::parseVendorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> vendorNamesById = new HashMap<>();
        if (!vendorIds.isEmpty()) {
            vendorRepository.findByVendorIdIn(vendorIds).forEach(v ->
                    vendorNamesById.put(String.valueOf(v.getVendorId()), NameUtils.getFullName(v.getFirstName(), v.getLastName())));
        }
        return vendorNamesById;
    }

    private Integer parseVendorId(String vendorId) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(vendorId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ExpenseFilterOptions buildExpenseFilterOptions(String hostelId) {
        List<ExpensesCategory> categories = expenseCategoryService.getAllActiveCategories(hostelId);
        List<ExpenseFilterOptions.FilterItems> categoryItems = categories.stream()
                .map(c -> new ExpenseFilterOptions.FilterItems(c.categoryName(), String.valueOf(c.categoryId())))
                .collect(Collectors.toList());
        return new ExpenseFilterOptions(categoryItems);
    }

    /**
     * Full detail of a single expense: the expense record plus its items and complete payment
     * history. Bank, category and creator names are resolved through a handful of bulk lookups
     * (one per related table) so there are no per-row queries.
     */
    public ResponseEntity<?> getExpenseDetails(String hostelId, String expenseId) {
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

        ExpensesV1 expense = expensesRepository.findById(expenseId).orElse(null);
        if (expense == null || !expense.isActive() || !hostelId.equalsIgnoreCase(expense.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }

        List<ExpenseItem> items = expenseItemRepository.findByExpenseId(expenseId);
        List<ExpensePayment> payments = expensePaymentRepository.findByExpenseId(expenseId);

        // Bulk-resolve banks referenced by the expense and its payments.
        Set<String> bankIds = new HashSet<>();
        if (expense.getBankId() != null) {
            bankIds.add(expense.getBankId());
        }
        payments.stream().map(ExpensePayment::getBankId).filter(Objects::nonNull).forEach(bankIds::add);
        Map<String, BankingV1> bankMap = new HashMap<>();
        if (!bankIds.isEmpty()) {
            bankingService.findAllBanksById(bankIds).forEach(b -> bankMap.put(b.getBankId(), b));
        }

        // Resolve category + sub-category names.
        ExpenseCategory category = expense.getCategoryId() != null
                ? expenseCategoryService.getExpenseCategoryById(expense.getCategoryId()) : null;
        String categoryName = category != null ? category.getCategoryName() : null;
        String subCategoryName = null;
        if (category != null && expense.getSubCategoryId() != null && category.getListSubCategories() != null) {
            subCategoryName = category.getListSubCategories().stream()
                    .filter(s -> expense.getSubCategoryId().equals(s.getSubCategoryId()))
                    .map(ExpenseSubCategory::getSubCategoryName)
                    .findFirst().orElse(null);
        }

        // Bulk-resolve creator names across the expense, items and payments.
        Set<String> creatorIds = new HashSet<>();
        if (expense.getCreatedBy() != null) {
            creatorIds.add(expense.getCreatedBy());
        }
        items.stream().map(ExpenseItem::getCreatedBy).filter(Objects::nonNull).forEach(creatorIds::add);
        payments.stream().map(ExpensePayment::getCreatedBy).filter(Objects::nonNull).forEach(creatorIds::add);
        Map<String, String> creatorNamesById = new HashMap<>();
        if (!creatorIds.isEmpty()) {
            usersService.findAllUsersFromUserId(new ArrayList<>(creatorIds)).forEach(u ->
                    creatorNamesById.put(u.getUserId(), NameUtils.getFullName(u.getFirstName(), u.getLastName())));
        }

        BankingV1 expenseBank = bankMap.get(expense.getBankId());

        List<ExpenseDetailItem> itemDetails = items.stream()
                .map(i -> new ExpenseDetailItem(
                        i.getId(), i.getItem(), i.getQuantity(), i.getUnit(), i.getUnitPrice(), i.getTotalAmount(),
                        i.getCreatedAt() != null ? Utils.dateToString(i.getCreatedAt()) : null,
                        resolveCreatorName(i.getCreatedBy(), creatorNamesById)))
                .toList();

        List<ExpenseDetailPayment> paymentDetails = payments.stream()
                .map(p -> {
                    BankingV1 paymentBank = bankMap.get(p.getBankId());
                    return new ExpenseDetailPayment(
                            p.getId(), p.getPaidAmount(), p.getPaymentMethod(), p.getBankId(),
                            paymentBank != null ? paymentBank.getBankName() : null,
                            p.getPaymentDate() != null ? Utils.dateToString(p.getPaymentDate()) : null,
                            p.getTransactionId(), p.getNotes(), p.getImageUrl(), p.getImageUrls(),
                            p.getCreatedAt() != null ? Utils.dateToString(p.getCreatedAt()) : null,
                            resolveCreatorName(p.getCreatedBy(), creatorNamesById));
                })
                .toList();

        double totalExpenseAmount = items.stream()
                .map(ExpenseItem::getTotalAmount).filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        double totalExpensePaidAmount = payments.stream()
                .map(ExpensePayment::getPaidAmount).filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();

        ExpenseDetailResponse response = new ExpenseDetailResponse(
                expense.getExpenseId(),
                expense.getUnitCount(),
                expense.getCategoryId(),
                expense.getSubCategoryId() != null ? expense.getSubCategoryId() : 0L,
                expense.getDescription(),
                expense.getHostelId(),
                expense.getBankId(),
                expenseBank != null ? expenseBank.getBankName() : null,
                expense.getTotalPrice(),
                expense.getTransactionDate() != null ? Utils.dateToString(expense.getTransactionDate()) : null,
                expense.getUnitPrice(),
                expense.getVendorId(),
                expense.getExpenseNumber(),
                expenseBank != null ? expenseBank.getAccountHolderName() : null,
                categoryName,
                subCategoryName,
                expense.getTitle(),
                expense.getIsVendorExpense(),
                expense.getPaymentStatus(),
                expense.getPaidAmount(),
                expense.getBalanceAmount(),
                expense.getPaymentMethod(),
                expense.getNote(),
                totalExpenseAmount,
                totalExpensePaidAmount,
                expense.getCreditPeriod(),
                expense.getDiscount(),
                expense.getTax(),
                expense.getTransactionId(),
                expense.getCreatedAt() != null ? Utils.dateToString(expense.getCreatedAt()) : null,
                resolveCreatorName(expense.getCreatedBy(), creatorNamesById),
                expense.getImages() != null ? expense.getImages() : List.of(),
                itemDetails,
                paymentDetails);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String resolveCreatorName(String createdBy, Map<String, String> creatorNamesById) {
        if (createdBy == null) {
            return null;
        }
        return creatorNamesById.getOrDefault(createdBy, createdBy);
    }

    private long daysSince(Date date) {
        long diffMillis = System.currentTimeMillis() - date.getTime();
        return diffMillis / (1000L * 60 * 60 * 24);
    }

    private void stampCreate(ExpenseItem item, String userId, Date now) {
        item.setCreatedBy(userId);
        item.setUpdatedBy(userId);
        item.setCreatedAt(now);
        item.setModifiedAt(now);
    }

    private void stampCreate(ExpensePayment payment, String userId, Date now) {
        payment.setCreatedBy(userId);
        payment.setUpdatedBy(userId);
        payment.setCreatedAt(now);
        payment.setModifiedAt(now);
    }

    /**
     * Initializes each expense item's payment details from the parent expense:
     * <ul>
     *   <li>{@code paymentStatus} mirrors the parent expense status.</li>
     *   <li>{@code Full} → paid amount equals the item's own total.</li>
     *   <li>{@code Pending}/{@code Overdue} → paid amount is 0.</li>
     *   <li>{@code Partial} → the parent's paid amount is split proportionally across items by their
     *       individual totals (see {@link #distributePartialPaidAmount}).</li>
     * </ul>
     */
    private void initializeItemPayments(List<ExpenseItem> items, ExpensePaymentStatus parentStatus, Double parentPaidAmount) {
        if (items == null || items.isEmpty()) {
            return;
        }
        ExpensePaymentStatus status = parentStatus != null ? parentStatus : ExpensePaymentStatus.Full;
        items.forEach(item -> item.setPaymentStatus(status));

        switch (status) {
            case Full -> items.forEach(item ->
                    item.setPaidAmount(item.getTotalAmount() != null ? item.getTotalAmount() : 0.0));
            case Partial -> distributePartialPaidAmount(items, parentPaidAmount);
            case Pending, Overdue -> items.forEach(item -> item.setPaidAmount(0.0));
        }
    }

    /**
     * Splits the parent expense's paid amount across its items proportionally to each item's total.
     * Rounding is absorbed by the last item so the item paid amounts sum exactly to the parent's
     * paid amount.
     */
    private void distributePartialPaidAmount(List<ExpenseItem> items, Double parentPaidAmount) {
        double totalPaid = parentPaidAmount != null ? parentPaidAmount : 0.0;
        double totalItemsAmount = items.stream()
                .map(ExpenseItem::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalItemsAmount <= 0 || totalPaid <= 0) {
            items.forEach(item -> item.setPaidAmount(0.0));
            return;
        }

        double allocated = 0.0;
        for (int i = 0; i < items.size(); i++) {
            ExpenseItem item = items.get(i);
            if (i == items.size() - 1) {
                item.setPaidAmount(Utils.roundOffWithTwoDigit(totalPaid - allocated));
            } else {
                double itemAmount = item.getTotalAmount() != null ? item.getTotalAmount() : 0.0;
                double share = Utils.roundOffWithTwoDigit((itemAmount / totalItemsAmount) * totalPaid);
                item.setPaidAmount(share);
                allocated += share;
            }
        }
    }

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return expensesRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public Double sumAmountByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return expensesRepository.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public List<ExpensesV1> findByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return expensesRepository.findByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public List<ExpensesV1> findByHostelIdAndIsActiveTrue(String hostelId) {
        return expensesRepository.findByHostelIdAndIsActiveTrue(hostelId);
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
            BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
            cal.setTime(billingDates.currentBillStartDate());

            if ("THIS_MONTH".equalsIgnoreCase(period)) {
                startDate = billingDates.currentBillStartDate();
                endDate = billingDates.currentBillEndDate();
            }
            else if ("LAST_MONTH".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -1);
                BillingDates lastMonthBillingDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
                startDate = lastMonthBillingDates.currentBillStartDate();
                endDate = lastMonthBillingDates.currentBillEndDate();
            }
            else if ("LAST_3_MONTHS".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -3);
                BillingDates lastMonthBillingDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
                startDate = lastMonthBillingDates.currentBillStartDate();
                endDate =  billingDates.currentBillEndDate();
            } else if ("LAST_6_MONTHS".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -6);
                BillingDates lastMonthBillingDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
                startDate = lastMonthBillingDates.currentBillStartDate();
                endDate = billingDates.currentBillEndDate();
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
        periodList.add(new ExpenseReportResponse.FilterItem("LAST_MONTH", "Last Month"));
        periodList.add(new ExpenseReportResponse.FilterItem("LAST_3_MONTHS", "Last 3 Months"));
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

    @Transactional
    public ResponseEntity<?> updateExpense(String hostelId, String expenseId, UpdateExpense updateExpense) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        ExpensesV1 expensesV1 = expensesRepository.findById(expenseId).orElse(null);
        if (expensesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }

        double priceDifference = 0.0;
        boolean totalAmountModified = false;

        if (!expensesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!expensesV1.isActive()) {
            return new ResponseEntity<>(Utils.EXPENSE_ALREADY_DELETED, HttpStatus.BAD_REQUEST);
        }
        if (updateExpense == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (updateExpense.categoryId() != null && updateExpense.categoryId() != 0) {

            boolean isValidCategory = expenseCategoryService.checkCategoryIdValid(hostelId, updateExpense.categoryId());
            if (!isValidCategory) {
                return new ResponseEntity<>(Utils.INVALID_CATEGORY_ID, HttpStatus.BAD_REQUEST);
            }

            boolean hasSubCategory = expenseCategoryService.checkCategoryHavingSubCategory(hostelId, updateExpense.categoryId());
            if (hasSubCategory) {
                if (!Utils.checkNullOrEmpty(updateExpense.subCategoryId())) {
                    return new ResponseEntity<>(Utils.SUB_CATEGORY_ID_REQUIRED, HttpStatus.BAD_REQUEST);
                }
            }
            else {
                if (expensesV1.getCategoryId().equals(updateExpense.categoryId())) {
                    if (expensesV1.getSubCategoryId() != null) {
                        expensesV1.setSubCategoryId(null);
                    }
                }
            }

            if (updateExpense.subCategoryId() != null && updateExpense.subCategoryId() != 0) {
               boolean isValidSubCategory = expenseCategoryService.checkSubCategoryValid(hostelId, updateExpense.categoryId(), updateExpense.subCategoryId());
               if (!isValidSubCategory) {
                   return new ResponseEntity<>(Utils.INVALID_SUB_CATEGORY_ID, HttpStatus.BAD_REQUEST);
               }
               expensesV1.setSubCategoryId(updateExpense.subCategoryId());
            }
            expensesV1.setUpdatedBy(authentication.getName());
            expensesV1.setUpdatedAt(new Date());
            expensesV1.setCategoryId(updateExpense.categoryId());
        }
        else {
            if (updateExpense.subCategoryId() != null && updateExpense.subCategoryId() != 0) {
                boolean isValidSubCategory = expenseCategoryService.checkSubCategoryValid(hostelId, expensesV1.getCategoryId(), updateExpense.subCategoryId());
                if (!isValidSubCategory) {
                    return new ResponseEntity<>(Utils.INVALID_SUB_CATEGORY_ID, HttpStatus.BAD_REQUEST);
                }
                expensesV1.setUpdatedBy(authentication.getName());
                expensesV1.setUpdatedAt(new Date());
                expensesV1.setSubCategoryId(updateExpense.subCategoryId());
            }
        }

        if (updateExpense.purchaseDate() != null && !updateExpense.purchaseDate().trim().isBlank()) {
            Date purchaseDate = Utils.stringToDate(updateExpense.purchaseDate().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            expensesV1.setTransactionDate(purchaseDate);
            expensesV1.setUpdatedBy(authentication.getName());
            expensesV1.setUpdatedAt(new Date());
        }
        if (updateExpense.count() != null && updateExpense.count() != 0) {
            expensesV1.setUnitCount(updateExpense.count());
            expensesV1.setUpdatedBy(authentication.getName());
            expensesV1.setUpdatedAt(new Date());
            if (updateExpense.totalAmount() != null && updateExpense.totalAmount() != 0) {
                double unitPrice = updateExpense.totalAmount()/ updateExpense.count();
                expensesV1.setUnitPrice(unitPrice);
            }
            else {
                double unitPrice = expensesV1.getTotalPrice()/ updateExpense.count();
                expensesV1.setUnitPrice(unitPrice);
            }
        }

        if (updateExpense.totalAmount() != null && updateExpense.totalAmount() != 0) {
            totalAmountModified = true;
            priceDifference = expensesV1.getTotalPrice() - updateExpense.totalAmount();
            double unitPrice = updateExpense.totalAmount()/ expensesV1.getUnitCount();
            expensesV1.setUnitPrice(unitPrice);
            expensesV1.setTotalPrice(updateExpense.totalAmount());
            expensesV1.setUpdatedBy(authentication.getName());
            expensesV1.setUpdatedAt(new Date());

        }
        if (updateExpense.description() != null) {
            expensesV1.setDescription(updateExpense.description());
            expensesV1.setUpdatedBy(authentication.getName());
            expensesV1.setUpdatedAt(new Date());
        }

        if (totalAmountModified) {
            bankTransactionService.updateExpenseTransactions(hostelId, expenseId, updateExpense.totalAmount(), priceDifference, updateExpense.purchaseDate());
        }

        // Keep the vendor's denormalized financial summary in sync after an amount change.
        if (expensesV1.getVendorId() != null) {
            vendorFinancialService.recalculate(expensesV1.getVendorId());
        }

        usersService.addUserLog(hostelId, expenseId, ActivitySource.EXPENSE, ActivitySourceType.UPDATE, users);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<?> deleteExpense(String hostelId, String expenseId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_EXPENSE, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        ExpensesV1 expensesV1 = expensesRepository.findById(expenseId).orElse(null);
        if (expensesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_EXPENSE_ID, HttpStatus.BAD_REQUEST);
        }

        if (!expensesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        String vendorId = expensesV1.getVendorId();
        if (bankTransactionService.deleteExpnese(hostelId, expenseId)) {
            expenseItemRepository.deleteByExpenseId(expenseId);
            expensePaymentRepository.deleteByExpenseId(expenseId);
            expensesRepository.delete(expensesV1);

            // Recompute the vendor summary now that this expense and its payments are gone.
            if (vendorId != null) {
                vendorFinancialService.recalculate(vendorId);
            }

            usersService.addUserLog(hostelId, expenseId, ActivitySource.EXPENSE, ActivitySourceType.DELETE, users);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

    }
}
