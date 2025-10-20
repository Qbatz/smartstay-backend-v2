package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.amenity.AmenityMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.amenity.AmenityRequest;
import com.smartstay.smartstay.payloads.amenity.AssignRequest;
import com.smartstay.smartstay.payloads.amenity.UnAssignRequest;
import com.smartstay.smartstay.payloads.amenity.AssignAmenity;
import com.smartstay.smartstay.repositories.AmentityRepository;
import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import com.smartstay.smartstay.responses.amenitity.AmenityResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AmenitiesService {
    @Autowired
    AmentityRepository amentityRepository;
    @Autowired
    CustomerAmenityRepository customerAmenityRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UserHostelService userHostelService;

    public ResponseEntity<?> getAllAmenities(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        List<AmenityInfoProjection> amenitiesV1List = amentityRepository.findAmenityInfoByHostelId(hostelId, user.getParentId());
        if (amenitiesV1List != null) {
            List<AmenityResponse> amenityResponses = amenitiesV1List.stream()
                    .map(item -> new AmenityMapper(customerAmenityRepository, hostelId).apply(item))
                    .toList();
            return new ResponseEntity<>(amenityResponses, HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.NO_RECORDS_FOUND, HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<?> getAmenitiesById(String hostelId, String amenitiesId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        AmenityInfoProjection amenitiesV1 = amentityRepository.findAmenityInfoByHostelIdByAmenityId(hostelId, user.getParentId(), amenitiesId);
        if (amenitiesV1 != null) {
            return new ResponseEntity<>(new AmenityMapper(customerAmenityRepository, hostelId).apply(amenitiesV1), HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.NO_RECORDS_FOUND, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> addAmenity(AmenityRequest request, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        boolean exists = amentityRepository.existsByAmenityNameAndHostelIdAndIsActiveTrueAndIsDeletedFalse(request.amenityName(), hostelId);
        if (exists) {
            return new ResponseEntity<>(Utils.AMENITY_ALREADY_EXIST, HttpStatus.FORBIDDEN);
        }
        AmenitiesV1 amenitiesV1 = new AmenitiesV1();
        amenitiesV1.setCreatedBy(user.getUserId());
        amenitiesV1.setCreatedAt(new java.util.Date());
        amenitiesV1.setIsActive(true);
        amenitiesV1.setIsDeleted(false);
        amenitiesV1.setIsProRate(false);
        amenitiesV1.setHostelId(hostelId);
        amenitiesV1.setAmenityName(request.amenityName());
        amenitiesV1.setAmenityAmount(request.amount());
        amenitiesV1.setParentId(user.getParentId());
        amentityRepository.save(amenitiesV1);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }


    public ResponseEntity<?> updateAmenity(AmenityRequest request, String amenityId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        AmenitiesV1 amenitiesV1 = amentityRepository.findByAmenityIdAndHostelIdAndParentIdAndIsDeletedFalse(amenityId, hostelId, user.getParentId());
        if (amenitiesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.NOT_FOUND);
        }
        if (request.amenityName()!=null){
            amenitiesV1.setAmenityName(request.amenityName());
        }
        if (request.amount()!=null){
            amenitiesV1.setAmenityAmount(request.amount());
        }
        amenitiesV1.setUpdatedAt(new java.util.Date());
        if (request.proRate() != null) {
            amenitiesV1.setIsProRate(request.proRate());
            List<CustomersAmenity> customerAmenities = customerAmenityRepository.findLatestByAmenityId(amenityId);
            if (request.proRate()){
                for (CustomersAmenity customerAmenity : customerAmenities) {
                    customerAmenity.setUpdatedAt(new Date());
                    customerAmenityRepository.save(customerAmenity);
                }
            } else {
                for (CustomersAmenity customerAmenity : customerAmenities) {
                    customerAmenity.setUpdatedAt(new Date());
                    customerAmenityRepository.save(customerAmenity);
                }
            }
        }
        amentityRepository.save(amenitiesV1);

        return new ResponseEntity<>(
                Utils.UPDATED,
                HttpStatus.OK
        );
    }

    public ResponseEntity<?> deleteAmenityById(String amenityId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean exists = amentityRepository.existsByAmenityIdAndHostelIdAndParentIdAndIsDeletedTrue(amenityId, hostelId, users.getParentId());
        if (exists) {
            return new ResponseEntity<>(Utils.AMENITY_ALREADY_DELETED, HttpStatus.BAD_REQUEST);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(users.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        AmenitiesV1 existingAmenity = amentityRepository.findByAmenityIdAndHostelIdAndParentIdAndIsDeletedFalse(amenityId, hostelId, users.getParentId());
        if (existingAmenity != null) {
            existingAmenity.setIsDeleted(true);
            existingAmenity.setUpdatedAt(new Date());
            amentityRepository.save(existingAmenity);
            return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.BAD_REQUEST);

    }

    public ResponseEntity<?> assign(AssignRequest request, String amenityId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        AmenitiesV1 amenitiesV1 = amentityRepository.findByAmenityIdAndHostelIdAndParentIdAndIsDeletedFalse(amenityId, hostelId, user.getParentId());
        if (amenitiesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.NOT_FOUND);
        }

        if (request.assignedCustomers() != null) {
            for (String customerId : request.assignedCustomers()) {
                CustomersAmenity customerAmenity = new CustomersAmenity();
                customerAmenity.setAmenityId(amenityId);
                customerAmenity.setCustomerId(customerId);
                customerAmenity.setCreatedAt(new Date());
                customerAmenity.setEndDate(null);
                customerAmenity.setStartDate(new Date());
                customerAmenity.setUpdatedBy(user.getUserId());
                customerAmenityRepository.save(customerAmenity);
            }
        }


        return new ResponseEntity<>(
                Utils.UPDATED,
                HttpStatus.OK
        );
    }

    public ResponseEntity<?> unAssign(UnAssignRequest request, String amenityId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        AmenitiesV1 amenitiesV1 = amentityRepository.findByAmenityIdAndHostelIdAndParentIdAndIsDeletedFalse(amenityId, hostelId, user.getParentId());
        if (amenitiesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.NOT_FOUND);
        }

        if (request.unassignedCustomers() != null) {
            for (String customerId : request.unassignedCustomers()) {
                CustomersAmenity exists = customerAmenityRepository.findTopByAmenityIdAndCustomerIdAndEndDateIsNullOrderByCreatedAtDesc(amenityId, customerId);
                if (exists != null) {
                    exists.setEndDate(new Date());
                    exists.setUpdatedAt(new Date());
                    customerAmenityRepository.save(exists);
                }
            }
        }

        return new ResponseEntity<>(
                Utils.UPDATED,
                HttpStatus.OK
        );
    }


}
