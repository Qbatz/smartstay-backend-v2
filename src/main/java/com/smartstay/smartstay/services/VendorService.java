package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.VendorMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
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
    RolesRepository rolesRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UploadFileToS3 uploadToS3;

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
        VendorV1 vendorV1 = vendorRepository.findByVendorId(id);
        if (vendorV1 != null) {
            VendorResponse vendorResponse = new VendorMapper().apply(vendorV1);
            return new ResponseEntity<>(vendorResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> updateVendorById(int vendorId, UpdateVendor updateVendor,MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        VendorV1 existingVendor = vendorRepository.findByVendorId(vendorId);
        if (existingVendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.NO_CONTENT);
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
            existingVendor.setMobile(updateVendor.mobile());
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

        if (vendorRepository.existsByMobile(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "Vendor/Logo");
        }

        VendorV1 vendorV1 = new VendorV1();
        vendorV1.setFirstName(payloads.firstName());
        vendorV1.setLastName(payloads.lastName());
        vendorV1.setMobile(payloads.mobile());
        vendorV1.setEmailId(payloads.mailId());
        vendorV1.setHouseNo(payloads.houseNo());
        vendorV1.setLandMark(payloads.landmark());
        vendorV1.setPinCode(payloads.pinCode());
        vendorV1.setCity(payloads.city());
        vendorV1.setState(payloads.state());
        vendorV1.setProfilePic(profileImage);
        vendorV1.setCountry(1L);
        vendorV1.setCreatedAt(new Date());
        vendorV1.setUpdatedAt(new Date());
        vendorV1.setCreatedBy(userId);
        vendorV1.setHostelId(payloads.hostelId());
        vendorV1.setActive(true);
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
            vendorRepository.delete(existingVendor);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Vendor found", HttpStatus.BAD_REQUEST);

    }

}
