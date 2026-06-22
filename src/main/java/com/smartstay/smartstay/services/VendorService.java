package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.Wrappers.expenses.ExpenseListMapper;
import com.smartstay.smartstay.Wrappers.vendor.VendorTableMapper;
import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.ExpenseItem;
import com.smartstay.smartstay.dao.ExpensePayment;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorCategories;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.vendor.VendorMonthSummaryProjection;
import com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.AddVendorCategory;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.repositories.CountriesRepository;
import com.smartstay.smartstay.repositories.ExpenseItemRepository;
import com.smartstay.smartstay.repositories.ExpensePaymentRepository;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.VendorCategoriesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.expenses.ExpenseItemResponse;
import com.smartstay.smartstay.responses.expenses.ExpensePaymentResponse;
import com.smartstay.smartstay.responses.vendor.VendorCategoryResponse;
import com.smartstay.smartstay.responses.vendor.VendorDetailsFilterOptions;
import com.smartstay.smartstay.responses.vendor.VendorDetailsResponse;
import com.smartstay.smartstay.responses.vendor.VendorExpensePaymentResponse;
import com.smartstay.smartstay.responses.vendor.VendorExpensePaymentsResponse;
import com.smartstay.smartstay.responses.vendor.VendorExpensesResponse;
import com.smartstay.smartstay.responses.vendor.VendorFilterOptions;
import com.smartstay.smartstay.responses.vendor.VendorFinancialSummary;
import com.smartstay.smartstay.responses.vendor.VendorListResponse;
import com.smartstay.smartstay.responses.vendor.VendorMonthSummary;
import com.smartstay.smartstay.responses.vendor.VendorMobileListResponse;
import com.smartstay.smartstay.responses.vendor.VendorMobileResponse;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import com.smartstay.smartstay.responses.vendor.VendorSummary;
import com.smartstay.smartstay.util.FilterKeywords;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    VendorCategoriesRepository vendorCategoriesRepository;
    @Autowired
    RolesRepository rolesRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private TableColumnService columnService;

    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private ExpensePaymentRepository expensePaymentRepository;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private CountriesRepository countriesRepository;

    @Autowired
    private BankingService bankingService;

    private String normalizeMobile(String countryCode, String mobile) {
        if (mobile == null) {
            return null;
        }
        String cleanedMobile = mobile.replaceAll("\\s+", "");
        if (countryCode == null || countryCode.isBlank()) {
            return cleanedMobile;
        }
        String cleanedCountryCode = countryCode.replace("+", "").replaceAll("\\s+", "");
        if (cleanedMobile.startsWith(cleanedCountryCode) && cleanedMobile.length() > cleanedCountryCode.length()) {
            return cleanedMobile.substring(cleanedCountryCode.length());
        }
        return cleanedMobile;
    }

    /**
     * Builds a unique vendor code in the format {@code VND} + 8-digit identifier
     * (e.g. {@code VND00000123}). The numeric part is the database-generated
     * vendorId, which is guaranteed unique by the identity column, so the
     * resulting code can never collide. IDs beyond 8 digits are not truncated.
     */
    private String generateVendorCode(Integer vendorId) {
        return String.format("VEN%08d", vendorId);
    }

    public ResponseEntity<?> getAllVendors(String hostelId, String name, Integer categoryId, String paymentStatus,
                                           Integer page, Integer size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        // Null => no status filter (covers both omitted and "ALL").
        VendorPaymentStatus statusFilter = VendorPaymentStatus.fromFilter(paymentStatus);
        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        // Pagination, the filtered page, and the summary are identical for web and mobile.
        Page<VendorV1> vendorPage = vendorRepository.listVendors(hostelId, searchName, categoryId, statusFilter, pageable);
        List<VendorV1> vendors = vendorPage.getContent();
        VendorSummary vendorSummary = buildVendorSummary(hostelId, searchName, categoryId, statusFilter, vendorPage.getTotalElements());
        Map<Integer, String> categoryNamesById = resolveCategoryNames(vendors);

        int currentPage = vendorPage.getPageable().getPageNumber() + 1;
        int totalPages = vendorPage.getTotalPages();
        int totalVendors = (int) vendorPage.getTotalElements();

        if ("web".equalsIgnoreCase(authentication.getSource())) {
            return buildVendorWebResponse(hostelId, vendors, categoryNamesById, vendorSummary,
                    totalVendors, currentPage, totalPages, pageSize);
        }
        return buildVendorMobileResponse(vendors, categoryNamesById, vendorSummary,
                totalVendors, currentPage, totalPages, pageSize);
    }

    private VendorSummary buildVendorSummary(String hostelId, String searchName, Integer categoryId,
                                             VendorPaymentStatus statusFilter, long totalVendors) {
        // Aggregated from the stored vendor columns over the full filtered result set.
        double totalPurchase = 0.0;
        double totalPaid = 0.0;
        VendorPurchaseSummary purchaseSummary = vendorRepository.summarizeVendors(hostelId, searchName, categoryId, statusFilter);
        if (purchaseSummary != null) {
            totalPurchase = purchaseSummary.totalPurchase() != null ? purchaseSummary.totalPurchase() : 0.0;
            totalPaid = purchaseSummary.totalPaid() != null ? purchaseSummary.totalPaid() : 0.0;
        }
        return new VendorSummary(totalVendors, totalPurchase, totalPaid, totalPurchase - totalPaid);
    }

    private Map<Integer, String> resolveCategoryNames(List<VendorV1> vendors) {
        Set<Integer> categoryIds = vendors.stream().map(VendorV1::getVendorCategory).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> categoryNamesById = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            vendorCategoriesRepository.findAllById(categoryIds)
                    .forEach(c -> categoryNamesById.put(c.getCategoryId(), c.getCategoryName()));
        }
        return categoryNamesById;
    }

    private ResponseEntity<?> buildVendorWebResponse(String hostelId, List<VendorV1> vendors,
                                                     Map<Integer, String> categoryNamesById, VendorSummary vendorSummary,
                                                     int totalVendors, int currentPage, int totalPages, int pageSize) {
        // Resolve the user's configured columns for this hostel; only enabled columns are rendered.
        List<ColumnFilters> listColumns = columnService.getVendorColumns(hostelId, FilterOptionsModule.MODULE_VENDOR.name());
        List<String> tableColumns = listColumns.stream()
                .filter(ColumnFilters::isSelected)
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .map(ColumnFilters::getFieldName)
                .toList();

        // Latest payment date per vendor (Last Transaction) for the current page in one bulk query (no N+1).
        List<String> pageVendorIds = vendors.stream().map(v -> String.valueOf(v.getVendorId())).toList();
        Map<String, Date> lastPaymentByVendorId = new HashMap<>();
        if (!pageVendorIds.isEmpty()) {
            expensePaymentRepository.findLatestPaymentDates(pageVendorIds)
                    .forEach(p -> lastPaymentByVendorId.put(p.vendorId(), p.lastPaymentDate()));
        }

        VendorTableMapper mapper = new VendorTableMapper(tableColumns, categoryNamesById, lastPaymentByVendorId);
        List<List<Object>> listVendorRows = vendors.stream().map(mapper).collect(Collectors.toList());

        VendorFilterOptions filterOptions = buildVendorFilterOptions(hostelId);
        VendorListResponse response = new VendorListResponse(totalVendors, currentPage, totalPages, pageSize,
                vendorSummary, filterOptions, tableColumns, listColumns, listVendorRows);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<?> buildVendorMobileResponse(List<VendorV1> vendors, Map<Integer, String> categoryNamesById,
                                                        VendorSummary vendorSummary, int totalVendors, int currentPage,
                                                        int totalPages, int pageSize) {
        // Resolve country names for the current page in one bulk lookup (no N+1).
        Set<Long> countryIds = vendors.stream().map(VendorV1::getCountry).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> countryNamesById = new HashMap<>();
        if (!countryIds.isEmpty()) {
            countriesRepository.findAllById(countryIds)
                    .forEach(c -> countryNamesById.put(c.getCountryId(), c.getCountryName()));
        }

        List<VendorMobileResponse> mobileVendors = vendors.stream()
                .map(v -> toMobileResponse(v, categoryNamesById, countryNamesById))
                .toList();

        // filterOptions / tableHeaders / columnList are intentionally null for mobile.
        VendorMobileListResponse response = new VendorMobileListResponse(totalVendors, currentPage, totalPages, pageSize,
                vendorSummary, null, null, null, mobileVendors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private VendorMobileResponse toMobileResponse(VendorV1 vendor, Map<Integer, String> categoryNamesById,
                                                  Map<Long, String> countryNamesById) {
        Integer categoryId = vendor.getVendorCategory();
        String categoryName = categoryId != null ? categoryNamesById.get(categoryId) : null;
        String countryName = vendor.getCountry() != null ? countryNamesById.get(vendor.getCountry()) : null;
        String paymentStatus = vendor.getPaymentStatus() != null ? vendor.getPaymentStatus().name() : null;

        return new VendorMobileResponse(
                vendor.getVendorId(),
                vendor.getFirstName(),
                vendor.getLastName(),
                NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName()),
                vendor.getBusinessName(),
                vendor.getMobile(),
                vendor.getEmailId(),
                vendor.getProfilePic(),
                vendor.getHouseNo(),
                vendor.getArea(),
                vendor.getLandMark(),
                vendor.getCity(),
                vendor.getPinCode(),
                vendor.getState(),
                vendor.getCountryCode(),
                countryName,
                vendor.getCountry(),
                categoryId,
                categoryName,
                vendor.getContactPerson(),
                vendor.getContactPersonMobile(),
                vendor.getDescription(),
                vendor.getVendorCode(),
                vendor.getGst(),
                vendor.getPan(),
                vendor.getAllowCredit(),
                vendor.getCreditLimit(),
                vendor.getCreditPeriod(),
                toIsoDateTime(vendor.getCreatedAt()),
                paymentStatus,
                nullSafe(vendor.getTotalExpense()),
                nullSafe(vendor.getTotalPaid()),
                nullSafe(vendor.getBalance()),
                vendor.getBusinessMobileCode(),
                vendor.getContactPersonMobileCode());
    }

    private VendorFilterOptions buildVendorFilterOptions(String hostelId) {
        List<VendorCategoryResponse> categories = vendorCategoriesRepository.findAllEnabledCategoriesByHostelId(hostelId);
        List<VendorFilterOptions.FilterItems> categoryItems = categories.stream()
                .map(c -> new VendorFilterOptions.FilterItems(c.categoryName(), String.valueOf(c.id())))
                .collect(Collectors.toList());

        List<String> paymentStatusOptions = new java.util.ArrayList<>();
        paymentStatusOptions.add("All");
        for (VendorPaymentStatus status : VendorPaymentStatus.values()) {
            paymentStatusOptions.add(status.getDisplayName());
        }
        return new VendorFilterOptions(categoryItems, paymentStatusOptions);
    }

    public ResponseEntity<?> getVendorById(Integer id, String period) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        VendorResponse vendorResponse = vendorRepository.getVendor(id);
        if (vendorResponse == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }

        VendorV1 vendor = vendorRepository.findByVendorId(id);

        // Null range => complete transaction history.
        Date[] range = resolvePeriodRange(period);
        Date startDate = range != null ? range[0] : null;
        Date endDate = range != null ? range[1] : null;

        String vendorId = String.valueOf(id);
        double totalExpense = nullSafe(expensesRepository.sumVendorExpense(vendorId, startDate, endDate));
        double totalPaid = nullSafe(expensePaymentRepository.sumVendorPaid(vendorId, startDate, endDate));
        long expenseCount = expensesRepository.countVendorExpense(vendorId, startDate, endDate);
        long paymentsCounts = expensePaymentRepository.countVendorPayments(vendorId, startDate, endDate);
        VendorFinancialSummary summary = new VendorFinancialSummary(totalExpense, totalPaid,
                totalExpense - totalPaid, expenseCount, paymentsCounts);

        String createdAt = vendor != null ? toIsoDateTime(vendor.getCreatedAt()) : null;

        // Month-wise breakdown for the selected range; defaults to the last 6 months when no
        // (or an unrecognised) filter is supplied.
        Date[] monthRange = range != null ? range : new Date[]{startOfMonth(-5), endOfMonth(0)};
        List<VendorMonthSummary> monthSummary = buildMonthSummary(vendorId, monthRange[0], monthRange[1]);

        VendorDetailsResponse response = new VendorDetailsResponse(vendorResponse, createdAt,
                buildPeriodFilterOptions(), summary, monthSummary);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Builds one {@link VendorMonthSummary} per calendar month in [startDate, endDate], using a
     * single grouped aggregate query. Months with no expenses are still returned, zero-filled, so the
     * response structure is consistent across the whole range.
     */
    private List<VendorMonthSummary> buildMonthSummary(String vendorId, Date startDate, Date endDate) {
        Map<Integer, VendorMonthSummaryProjection> byYearMonth = new HashMap<>();
        for (VendorMonthSummaryProjection row : expensesRepository.findVendorMonthlyExpenseSummary(vendorId, startDate, endDate)) {
            byYearMonth.put(yearMonthKey(row.getExpenseYear(), row.getExpenseMonth()), row);
        }

        List<VendorMonthSummary> monthSummaries = new ArrayList<>();
        SimpleDateFormat monthNameFormat = new SimpleDateFormat("MMMM");
        Calendar cursor = Calendar.getInstance();
        cursor.setTime(startDate);
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        while (cursor.get(Calendar.YEAR) < end.get(Calendar.YEAR)
                || (cursor.get(Calendar.YEAR) == end.get(Calendar.YEAR)
                && cursor.get(Calendar.MONTH) <= end.get(Calendar.MONTH))) {
            String monthName = monthNameFormat.format(cursor.getTime());
            VendorMonthSummaryProjection row = byYearMonth.get(
                    yearMonthKey(cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH) + 1));
            if (row != null) {
                monthSummaries.add(new VendorMonthSummary(monthName,
                        nullSafeLong(row.getTotalExpenseCount()), nullSafeLong(row.getTotalPaidCount()),
                        nullSafeLong(row.getTotalUnpaidCount()), nullSafeLong(row.getTotalPartialCount()),
                        nullSafe(row.getTotalPaidAmount()), nullSafe(row.getTotalUnpaidAmount()),
                        nullSafe(row.getTotalPartialAmount())));
            } else {
                monthSummaries.add(new VendorMonthSummary(monthName, 0, 0, 0, 0, 0.0, 0.0, 0.0));
            }
            cursor.add(Calendar.MONTH, 1);
        }
        return monthSummaries;
    }

    private int yearMonthKey(int year, int month) {
        return year * 100 + month;
    }

    private long nullSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    public ResponseEntity<?> getVendorExpenses(Integer vendorId, String search, String startDate, String endDate,
                                               Integer page, Integer size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorV1 vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.NO_CONTENT);
        }

        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Date start = (startDate != null && !startDate.trim().isEmpty())
                ? Utils.stringToDate(startDate.trim(), Utils.DATE_FORMAT_ZOHO) : null;
        Date end = (endDate != null && !endDate.trim().isEmpty())
                ? Utils.stringToDate(endDate.trim(), Utils.DATE_FORMAT_ZOHO) : null;

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        Page<com.smartstay.smartstay.dto.expenses.ExpenseList> expensePage =
                expensesRepository.findVendorExpenses(String.valueOf(vendorId), searchTerm, start, end, pageable);
        List<com.smartstay.smartstay.dto.expenses.ExpenseList> projections = expensePage.getContent();

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
        List<com.smartstay.smartstay.responses.expenses.ExpenseList> expenses = projections.stream()
                .map(item -> mapper.apply(item,
                        finalItemsByExpense.getOrDefault(item.getExpenseId(), List.of()),
                        finalPaymentsByExpense.getOrDefault(item.getExpenseId(), List.of())))
                .toList();

        VendorExpensesResponse response = new VendorExpensesResponse(expensePage.getTotalElements(), pageNumber,
                expensePage.getTotalPages(), pageSize, expenses);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getVendorExpensePayments(Integer vendorId, String startDate, String endDate,
                                                      Integer page, Integer size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorV1 vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.NO_CONTENT);
        }

        Date start = (startDate != null && !startDate.trim().isEmpty())
                ? Utils.stringToDate(startDate.trim(), Utils.DATE_FORMAT_ZOHO) : null;
        Date end = (endDate != null && !endDate.trim().isEmpty())
                ? Utils.stringToDate(endDate.trim(), Utils.DATE_FORMAT_ZOHO) : null;

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        Page<ExpensePayment> paymentPage =
                expensePaymentRepository.findVendorPayments(String.valueOf(vendorId), start, end, pageable);
        List<ExpensePayment> pagePayments = paymentPage.getContent();

        // Resolve banks once for the page: paymentMethod may hold a bank id (-> account type) and
        // bankId resolves to the bank name. Both come from a single bulk lookup (no N+1).
        Set<String> bankLookupIds = new HashSet<>();
        pagePayments.forEach(p -> {
            if (p.getPaymentMethod() != null && !p.getPaymentMethod().trim().isEmpty()) {
                bankLookupIds.add(p.getPaymentMethod());
            }
            if (p.getBankId() != null && !p.getBankId().trim().isEmpty()) {
                bankLookupIds.add(p.getBankId());
            }
        });
        Map<String, String> accountTypeById = new HashMap<>();
        Map<String, String> bankNameById = new HashMap<>();
        if (!bankLookupIds.isEmpty()) {
            bankingService.findAllBanksById(bankLookupIds).forEach(b -> {
                accountTypeById.put(b.getBankId(), b.getAccountType());
                bankNameById.put(b.getBankId(), b.getBankName());
            });
        }

        List<VendorExpensePaymentResponse> payments = pagePayments.stream()
                .map(p -> new VendorExpensePaymentResponse(
                        p.getId(),
                        p.getPaidAmount(),
                        resolvePaymentMethodName(p.getPaymentMethod(), accountTypeById),
                        p.getExpenseId(),
                        p.getBankId(),
                        resolveBankName(p.getBankId(), bankNameById),
                        p.getHostelId(),
                        Utils.dateToString(p.getPaymentDate()),
                        p.getTransactionId(),
                        p.getNotes(),
                        p.getImageUrl()))
                .toList();

        VendorExpensePaymentsResponse response = new VendorExpensePaymentsResponse(paymentPage.getTotalElements(),
                pageNumber, paymentPage.getTotalPages(), pageSize, payments);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private double nullSafe(Double value) {
        return value != null ? value : 0.0;
    }

    /**
     * Resolves a stored {@code paymentMethod} value (which may be a bank id) to its payment method
     * name. Falls back to the original value when it doesn't map to a bank.
     */
    private String resolvePaymentMethodName(String paymentMethod, Map<String, String> paymentMethodNames) {
        if (paymentMethod == null) {
            return null;
        }
        String resolved = paymentMethodNames.get(paymentMethod);
        return (resolved != null && !resolved.trim().isEmpty()) ? resolved : paymentMethod;
    }

    /**
     * Resolves a bank id to its bank name. Returns an empty string when the id is blank or no
     * matching bank is found.
     */
    private String resolveBankName(String bankId, Map<String, String> bankNameById) {
        if (bankId == null || bankId.trim().isEmpty()) {
            return "";
        }
        String name = bankNameById.get(bankId);
        return (name != null && !name.trim().isEmpty()) ? name : "";
    }

    private String toIsoDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
    }

    private VendorDetailsFilterOptions buildPeriodFilterOptions() {
        return new VendorDetailsFilterOptions(List.of(
                new VendorDetailsFilterOptions.PeriodFilter("This Month", FilterKeywords.THIS_MONTH),
                new VendorDetailsFilterOptions.PeriodFilter("Last Month", FilterKeywords.LAST_MONTH),
                new VendorDetailsFilterOptions.PeriodFilter("Last 3 Months", FilterKeywords.LAST_3_MONTH),
                new VendorDetailsFilterOptions.PeriodFilter("Last 6 Months", FilterKeywords.LAST_6_MONTH)));
    }

    /**
     * Resolves the selected period into an inclusive [start, end] range over whole calendar months,
     * so a record's timestamp component never excludes it (the repository compares on DATE()).
     * Returns {@code null} when no (or an unrecognised) period is supplied, signalling that the full
     * transaction history should be considered.
     */
    private Date[] resolvePeriodRange(String period) {
        if (period == null || period.trim().isEmpty()) {
            return null;
        }
        int monthsBack;
        if (FilterKeywords.THIS_MONTH.equalsIgnoreCase(period)) {
            monthsBack = 0;
        } else if (FilterKeywords.LAST_MONTH.equalsIgnoreCase(period)) {
            // Previous calendar month only.
            Date start = startOfMonth(-1);
            Date end = endOfMonth(-1);
            return new Date[]{start, end};
        } else if (FilterKeywords.LAST_3_MONTH.equalsIgnoreCase(period)) {
            monthsBack = 2;
        } else if (FilterKeywords.LAST_6_MONTH.equalsIgnoreCase(period)) {
            monthsBack = 5;
        } else {
            return null;
        }
        // Last N calendar months, inclusive of the current month.
        return new Date[]{startOfMonth(-monthsBack), endOfMonth(0)};
    }

    private Date startOfMonth(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date endOfMonth(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public ResponseEntity<?> updateVendorById(int vendorId, UpdateVendor updateVendor, MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        VendorV1 existingVendor = vendorRepository.findByVendorId(vendorId);
        if (existingVendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.NO_CONTENT);
        }

        if (!subscriptionService.validateSubscription(existingVendor.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "Vendor/Logo");
        }

        if (updateVendor.firstName() != null && !updateVendor.firstName().isEmpty()) {
            existingVendor.setFirstName(updateVendor.firstName());
        }
        if (updateVendor.lastName() != null && !updateVendor.lastName().isEmpty()) {
            existingVendor.setLastName(updateVendor.lastName());
        }
        if (updateVendor.mobile() != null && !updateVendor.mobile().isEmpty()) {
            existingVendor.setMobile(normalizeMobile(updateVendor.countryCode(), updateVendor.mobile()));
        }
        if (updateVendor.countryCode() != null && !updateVendor.countryCode().isEmpty()) {
            existingVendor.setCountryCode(updateVendor.countryCode().replace("+", "").trim());
        }
        if (updateVendor.mailId() != null && !updateVendor.mailId().isEmpty()) {
            existingVendor.setEmailId(updateVendor.mailId());
        }
        if (updateVendor.houseNo() != null && !updateVendor.houseNo().isEmpty()) {
            existingVendor.setHouseNo(updateVendor.houseNo());
        }
        if (updateVendor.landmark() != null && !updateVendor.landmark().isEmpty()) {
            existingVendor.setLandMark(updateVendor.landmark());
        }
        if (updateVendor.area() != null && !updateVendor.area().isEmpty()) {
            existingVendor.setArea(updateVendor.area());
        }
        if (updateVendor.pinCode() != null) {
            existingVendor.setPinCode(updateVendor.pinCode());
        }
        if (updateVendor.city() != null && !updateVendor.city().isEmpty()) {
            existingVendor.setCity(updateVendor.city());
        }
        if (updateVendor.state() != null && !updateVendor.state().isEmpty()) {
            existingVendor.setState(updateVendor.state());
        }
        if (updateVendor.businessName() != null && !updateVendor.businessName().isEmpty()) {
            existingVendor.setBusinessName(updateVendor.businessName());
        }
        if (updateVendor.country() != null ) {
            existingVendor.setCountry(updateVendor.country());
        }
        if (profileImage != null ) {
            existingVendor.setProfilePic(profileImage);
        }
        if (updateVendor.vendorCategory() != null) {
            existingVendor.setVendorCategory(updateVendor.vendorCategory());
        }
        if (updateVendor.contactPerson() != null) {
            existingVendor.setContactPerson(updateVendor.contactPerson());
        }
        if (updateVendor.description() != null) {
            existingVendor.setDescription(updateVendor.description());
        }
        if (updateVendor.vendorCode() != null) {
            existingVendor.setVendorCode(updateVendor.vendorCode());
        }
        if (updateVendor.gst() != null) {
            existingVendor.setGst(updateVendor.gst());
        }
        if (updateVendor.pan() != null) {
            existingVendor.setPan(updateVendor.pan());
        }
        if (updateVendor.allowCredit() != null) {
            existingVendor.setAllowCredit(updateVendor.allowCredit());
        }
        if (updateVendor.creditLimit() != null) {
            existingVendor.setCreditLimit(updateVendor.creditLimit());
        }
        if (updateVendor.creditPeriod() != null) {
            existingVendor.setCreditPeriod(updateVendor.creditPeriod());
        }

        existingVendor.setUpdatedAt(new Date());
        vendorRepository.save(existingVendor);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addVendor(MultipartFile file, AddVendor payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String normalizedMobile = normalizeMobile(payloads.countryCode(), payloads.mobile());
        if (vendorRepository.existsByMobile(normalizedMobile)) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        // Email must be unique across vendors (case-insensitive). Blank emails are stored as null so
        // multiple vendors without an email remain allowed.
        String email = (payloads.mailId() != null && !payloads.mailId().trim().isEmpty())
                ? payloads.mailId().trim() : null;
        if (email != null && vendorRepository.existsByEmailIdIgnoreCase(email)) {
            return new ResponseEntity<>(Utils.VENDOR_EMAIL_EXISTS, HttpStatus.BAD_REQUEST);
        }

        if (!subscriptionService.validateSubscription(payloads.hostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "Vendor/Logo");
        }

        VendorV1 vendorV1 = new VendorV1();
        vendorV1.setFirstName(payloads.firstName());
        vendorV1.setLastName(payloads.lastName());
        vendorV1.setCountryCode(payloads.countryCode() == null ? null : payloads.countryCode().replace("+", "").trim());
        vendorV1.setBusinessMobileCode(payloads.businessMobileCode() != null ? payloads.businessMobileCode().trim() : null);
        vendorV1.setMobile(normalizedMobile);
        vendorV1.setEmailId(email);
        vendorV1.setHouseNo(payloads.houseNo());
        vendorV1.setLandMark(payloads.landmark());
        vendorV1.setPinCode(payloads.pinCode());
        vendorV1.setCity(payloads.city());
        vendorV1.setState(payloads.state());
        vendorV1.setProfilePic(profileImage);
        vendorV1.setBusinessName(payloads.businessName());
        vendorV1.setArea(payloads.area());
        vendorV1.setCountry(1L);
        vendorV1.setCreatedAt(new Date());
        vendorV1.setUpdatedAt(new Date());
        vendorV1.setCreatedBy(userId);
        vendorV1.setBusinessName(payloads.businessName());
        vendorV1.setArea(payloads.area());
        vendorV1.setHostelId(payloads.hostelId());
        vendorV1.setVendorCategory(payloads.vendorCategory());
        vendorV1.setContactPerson(payloads.contactPerson());
        vendorV1.setContactPersonMobile(payloads.contactPersonMobile());
        // Optional: persist as-is, or null when not provided.
        vendorV1.setContactPersonMobileCode(
                (payloads.contactPersonMobileCode() != null && !payloads.contactPersonMobileCode().trim().isEmpty())
                        ? payloads.contactPersonMobileCode().trim() : null);
        vendorV1.setDescription(payloads.description());
        vendorV1.setGst(payloads.gst());
        vendorV1.setPan(payloads.pan());
        vendorV1.setAllowCredit(payloads.allowCredit());
        vendorV1.setCreditLimit(payloads.creditLimit());
        vendorV1.setCreditPeriod(payloads.creditPeriod());
        vendorV1.setActive(true);
        // A freshly created vendor has no expenses yet.
        vendorV1.setTotalExpense(0.0);
        vendorV1.setTotalPaid(0.0);
        vendorV1.setBalance(0.0);
        vendorV1.setPaymentStatus(VendorPaymentStatus.NO_TRANSACTION);

        // Persist first so the database assigns the unique, auto-incremented vendorId,
        // then derive the vendor code from it. Using the identity column guarantees
        // uniqueness and avoids the collisions possible with random generation.
        // The DB-level unique email index is the final guard against concurrent duplicate inserts.
        try {
            vendorV1 = vendorRepository.save(vendorV1);
            vendorV1.setVendorCode(generateVendorCode(vendorV1.getVendorId()));
            vendorRepository.save(vendorV1);
        } catch (DataIntegrityViolationException ex) {
            return new ResponseEntity<>(Utils.VENDOR_EMAIL_EXISTS, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public ResponseEntity<?> deleteVendorById(int vendorId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        VendorV1 existingVendor = vendorRepository.findByVendorId(vendorId);
        if (existingVendor != null) {
            if (!subscriptionService.validateSubscription(existingVendor.getHostelId())) {
                return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
            }
            vendorRepository.delete(existingVendor);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Vendor found", HttpStatus.BAD_REQUEST);

    }

    public int countByHostelId(String hostelId) {
        return vendorRepository.countByHostelId(hostelId);
    }

    public com.smartstay.smartstay.dao.VendorV1 getVendorObjectById(int id) {
        return vendorRepository.findByVendorId(id);
    }

    public ResponseEntity<?> addVendorCategory(AddVendorCategory payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String hostelId = payloads.hostelId();
        String categoryName = payloads.categoryName().trim();
        VendorCategories existingCategory = vendorCategoriesRepository.findByCategoryNameIgnoreCaseAndHostelId(categoryName, hostelId);
        if (existingCategory != null) {
            if (existingCategory.isEnabled()) {
                return new ResponseEntity<>(Utils.CATEGORY_ALREADY_ADDED, HttpStatus.BAD_REQUEST);
            }
            existingCategory.setEnabled(true);
            existingCategory.setModifiedAt(new Date());
            existingCategory.setModifiedBy(userId);
            vendorCategoriesRepository.save(existingCategory);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }

        VendorCategories vendorCategories = new VendorCategories();
        vendorCategories.setCategoryName(categoryName);
        vendorCategories.setHostelId(hostelId);
        vendorCategories.setEnabled(true);
        vendorCategories.setAddedBy(userId);
        vendorCategories.setCreatedAt(new Date());
        vendorCategories.setModifiedAt(new Date());
        vendorCategories.setModifiedBy(userId);
        vendorCategoriesRepository.save(vendorCategories);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteVendorCategory(int categoryId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorCategories existingCategory = vendorCategoriesRepository.findByCategoryIdAndHostelId(categoryId, hostelId);
        if (existingCategory == null || !existingCategory.isEnabled()) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }

        existingCategory.setEnabled(false);
        existingCategory.setModifiedAt(new Date());
        existingCategory.setModifiedBy(userId);
        vendorCategoriesRepository.save(existingCategory);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllVendorCategories(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<VendorCategoryResponse> categories = vendorCategoriesRepository.findAllEnabledCategoriesByHostelId(hostelId);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
