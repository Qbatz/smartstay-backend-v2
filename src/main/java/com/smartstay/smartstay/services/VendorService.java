package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorCategories;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.AddVendorCategory;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.VendorCategoriesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.vendor.VendorCategoryResponse;
import com.smartstay.smartstay.responses.vendor.VendorResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

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

    public ResponseEntity<?> getAllVendors(String hostelId) {
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
        List<VendorResponse> vendorV1List = vendorRepository.findAllVendorsByHostelId(hostelId);
        return new ResponseEntity<>(vendorV1List, HttpStatus.OK);
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
