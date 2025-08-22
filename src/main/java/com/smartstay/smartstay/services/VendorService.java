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
        List<VendorV1> vendorV1List = vendorRepository.findAllByHostelId(hostelId);
        List<VendorResponse> vendorResponses = vendorV1List.stream().map(item -> new VendorMapper().apply(item)).toList();
        return new ResponseEntity<>(vendorResponses, HttpStatus.OK);
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

    public ResponseEntity<?> updateVendorById(int vendorId, UpdateVendor updateVendor) {
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
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
//        if (updateVendor.roomName() != null && !updateVendor.roomName().isEmpty()) {
//            existingVendor.setRoomName(updateVendor.roomName());
//        }
//        if (updateVendor.isActive() != null) {
//            existingVendor.setIsActive(updateVendor.isActive());
//        }
//        existingVendor.setUpdatedAt(new Date());
//        roomRepository.save(existingVendor);
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

        if (vendorRepository.existsByMobileNumber(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        VendorV1 vendorV1 = new VendorV1();
        vendorV1.setFirstName(payloads.firstName());
        vendorV1.setLastName(payloads.lastName());
        vendorV1.setMobileNumber(payloads.mobile());
        vendorV1.setEmailId(payloads.mailId());
        vendorV1.setHouseNo(payloads.houseNo());
        vendorV1.setLandMark(payloads.landmark());
        vendorV1.setPinCode(payloads.pinCode());
        vendorV1.setCity(payloads.city());
        vendorV1.setState(payloads.state());
        vendorV1.setProfilePic(profileImage);
        vendorV1.setCountry("");
        vendorV1.setCity(user.getUserId());
        vendorV1.setCreatedAt(new Date());
        vendorV1.setUpdatedAt(new Date());
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
