package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.Wrappers.vendor.VendorTableMapper;
import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorCategories;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.vendor.VendorExpenseAggregate;
import com.smartstay.smartstay.dto.vendor.VendorPurchaseSummary;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.AddVendorCategory;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.VendorCategoriesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.vendor.VendorCategoryResponse;
import com.smartstay.smartstay.responses.vendor.VendorFilterOptions;
import com.smartstay.smartstay.responses.vendor.VendorListResponse;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import com.smartstay.smartstay.responses.vendor.VendorSummary;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

    public ResponseEntity<?> getAllVendors(String hostelId, String name, Integer categoryId, Integer page, Integer size) {
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
        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        Page<VendorV1> vendorPage = vendorRepository.listVendors(hostelId, searchName, categoryId, pageable);
        List<VendorV1> vendors = vendorPage.getContent();

        // Resolve the user's configured columns for this hostel; only enabled columns are rendered.
        List<ColumnFilters> listColumns = columnService.getVendorColumns(hostelId, FilterOptionsModule.MODULE_VENDOR.name());
        List<String> tableColumns = listColumns.stream()
                .filter(ColumnFilters::isSelected)
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .map(ColumnFilters::getFieldName)
                .toList();

        // Per-vendor purchase/paid/last-transaction roll-up for the current page in a single query (no N+1).
        List<String> pageVendorIds = vendors.stream().map(v -> String.valueOf(v.getVendorId())).toList();
        Map<String, VendorExpenseAggregate> aggregatesByVendorId = new HashMap<>();
        if (!pageVendorIds.isEmpty()) {
            aggregatesByVendorId = expensesRepository.findVendorExpenseAggregates(hostelId, pageVendorIds).stream()
                    .collect(Collectors.toMap(VendorExpenseAggregate::vendorId, Function.identity(), (a, b) -> a));
        }

        // Resolve category names for the current page in one bulk lookup.
        Set<Integer> categoryIds = vendors.stream().map(VendorV1::getVendorCategory).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> categoryNamesById = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            vendorCategoriesRepository.findAllById(categoryIds)
                    .forEach(c -> categoryNamesById.put(c.getCategoryId(), c.getCategoryName()));
        }

        VendorTableMapper mapper = new VendorTableMapper(tableColumns, categoryNamesById, aggregatesByVendorId);
        List<List<Object>> listVendorRows = vendors.stream().map(mapper).collect(Collectors.toList());

        // Summary reflects the full result set for the current search/filter, not just the page.
        List<Integer> matchingVendorIds = vendorRepository.findVendorIdsByFilters(hostelId, searchName, categoryId);
        double totalPurchase = 0.0;
        double totalPaid = 0.0;
        if (!matchingVendorIds.isEmpty()) {
            List<String> matchingVendorIdStrings = matchingVendorIds.stream().map(String::valueOf).toList();
            VendorPurchaseSummary purchaseSummary = expensesRepository.findVendorPurchaseSummary(hostelId, matchingVendorIdStrings);
            if (purchaseSummary != null) {
                totalPurchase = purchaseSummary.totalPurchase() != null ? purchaseSummary.totalPurchase() : 0.0;
                totalPaid = purchaseSummary.totalPaid() != null ? purchaseSummary.totalPaid() : 0.0;
            }
        }

        long totalVendors = vendorPage.getTotalElements();
        VendorSummary vendorSummary = new VendorSummary(totalVendors, totalPurchase, totalPaid, totalPurchase - totalPaid);
        VendorFilterOptions filterOptions = buildVendorFilterOptions(hostelId);

        int currentPage = vendorPage.getPageable().getPageNumber() + 1;
        int totalPages = vendorPage.getTotalPages();

        VendorListResponse response = new VendorListResponse((int) totalVendors, currentPage, totalPages, pageSize,
                vendorSummary, filterOptions, tableColumns, listColumns, listVendorRows);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private VendorFilterOptions buildVendorFilterOptions(String hostelId) {
        List<VendorCategoryResponse> categories = vendorCategoriesRepository.findAllEnabledCategoriesByHostelId(hostelId);
        List<VendorFilterOptions.FilterItems> categoryItems = categories.stream()
                .map(c -> new VendorFilterOptions.FilterItems(c.categoryName(), String.valueOf(c.id())))
                .collect(Collectors.toList());
        return new VendorFilterOptions(categoryItems);
    }

    public ResponseEntity<?> getVendorById(Integer id) {
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
        if (vendorResponse != null) {
            return new ResponseEntity<>(vendorResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

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
        vendorV1.setMobile(normalizedMobile);
        vendorV1.setEmailId(payloads.mailId());
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
        vendorV1.setDescription(payloads.description());
        vendorV1.setGst(payloads.gst());
        vendorV1.setPan(payloads.pan());
        vendorV1.setAllowCredit(payloads.allowCredit());
        vendorV1.setCreditLimit(payloads.creditLimit());
        vendorV1.setCreditPeriod(payloads.creditPeriod());
        vendorV1.setActive(true);

        // Persist first so the database assigns the unique, auto-incremented vendorId,
        // then derive the vendor code from it. Using the identity column guarantees
        // uniqueness and avoids the collisions possible with random generation.
        vendorV1 = vendorRepository.save(vendorV1);
        vendorV1.setVendorCode(generateVendorCode(vendorV1.getVendorId()));
        vendorRepository.save(vendorV1);

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
