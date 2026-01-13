package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.amenity.AmenityMapper;
import com.smartstay.smartstay.Wrappers.amenity.CustomerAmenityMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.BedInformations;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.payloads.amenity.*;
import com.smartstay.smartstay.payloads.amenity.AmenityRequest;
import com.smartstay.smartstay.repositories.AmentityRepository;
import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import com.smartstay.smartstay.responses.amenitity.AmenityList;
import com.smartstay.smartstay.responses.amenitity.AmenityResponse;
import com.smartstay.smartstay.responses.customer.Amenities;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private BedsService bedsService;

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

        AmenityList amenityList = new AmenityList(hostelId, amenitiesV1List);
        if (amenitiesV1List != null) {
            return new ResponseEntity<>(amenityList, HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.NO_RECORDS_FOUND, HttpStatus.BAD_REQUEST);
    }


    public ResponseEntity<?> getAmenitiesById(String hostelId, String amenitiesId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
        List<BookingsV1> stayingCustomersList = bookingsService.getAllCheckedInCustomer(hostelId);
        List<String> customerIds = stayingCustomersList
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();
        List<Integer> bedIds = stayingCustomersList
                .stream()
                .map(BookingsV1::getBedId)
                .toList();
        List<BedDetails> bedDetails = bedsService.getBedDetails(bedIds);
        List<Customers> listCustomer = customersService.getCustomerDetails(customerIds);
        HashMap<String, Integer> bedCustomerMapper = new HashMap<>();
        stayingCustomersList.forEach(i -> {
            bedCustomerMapper.put(i.getCustomerId(), i.getBedId());
        });
        List<CustomersAmenity> listCustomersAmenity = customerAmenityRepository.findLatestByAmenityId(amenitiesV1.getAmenityId());
        return new ResponseEntity<>(new AmenityMapper(listCustomer, listCustomersAmenity, bedDetails, bedCustomerMapper).apply(amenitiesV1), HttpStatus.OK);
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
        if (request.description() != null && !request.description().trim().equalsIgnoreCase("")) {
            amenitiesV1.setDescription(request.description());
        }
        if (request.termsOfUsage() != null && !request.termsOfUsage().trim().equalsIgnoreCase("")) {
            amenitiesV1.setTermsAndCondition(request.termsOfUsage());
        }
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
            List<AmenitiesV1> listAmenitiesByName = amentityRepository.findByAmenityNameAndHostelId(hostelId, amenityId, request.amenityName());
            if (listAmenitiesByName != null && !listAmenitiesByName.isEmpty()) {
                return new ResponseEntity<>(Utils.AMENITY_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
            }
            amenitiesV1.setAmenityName(request.amenityName());
        }
        if (request.amount()!=null){
            amenitiesV1.setAmenityAmount(request.amount());
        }
        if (request.description() != null && !request.description().trim().equalsIgnoreCase("")) {
            amenitiesV1.setDescription(request.description());
        }
        if (request.termsOfUsage() != null && !request.termsOfUsage().trim().equalsIgnoreCase("")) {
            amenitiesV1.setTermsAndCondition(request.termsOfUsage());
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
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
            //check is amenity is currently assigned
            List<CustomersAmenity> listAmenityAssignedAndActive = customerAmenityRepository.checkAmenityAssignedAndActive(amenityId, new Date());
            if (listAmenityAssignedAndActive != null && !listAmenityAssignedAndActive.isEmpty()) {
                return new ResponseEntity<>(Utils.CANNOT_DELETE_ASSIGNED_AMENITIES, HttpStatus.BAD_REQUEST);
            }
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

        if (request.customers() != null) {
            for (String customerId : request.customers()) {
                CustomersAmenity customerAmenity = new CustomersAmenity();
                customerAmenity.setAmenityId(amenityId);
                customerAmenity.setCustomerId(customerId);
                customerAmenity.setCreatedAt(new Date());
                customerAmenity.setAmenityPrice(amenitiesV1.getAmenityAmount());
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

        if (request.customers() != null) {
            Date amenityEndDate = null;
            if (amenitiesV1.getIsProRate()) {
                amenityEndDate = new Date();
            }
            else {
                BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, new Date());
                if (billingDates != null) {
                    amenityEndDate = billingDates.currentBillEndDate();
                }
            }

            for (String customerId : request.customers()) {
                CustomersAmenity exists = customerAmenityRepository.findTopByAmenityIdAndCustomerIdAndEndDateIsNullOrderByCreatedAtDesc(amenityId, customerId);
                if (exists != null) {
                    exists.setEndDate(amenityEndDate);
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


    public List<CustomersAmenity> getAllAmenitiesByCustomerId(String customerId) {
        return customerAmenityRepository.findByCustomerId(customerId);
    }

    public List<Amenities> getAmenitiesByCustomerId(String customerId) {
        List<CustomersAmenity> listCustomerAmenities = customerAmenityRepository.findByCustomerId(customerId);
        List<String> amenityIds = listCustomerAmenities
                .stream()
                .map(CustomersAmenity::getAmenityId)
                .toList();
        List<AmenitiesV1> amenities = amentityRepository.findAllById(amenityIds);

        return listCustomerAmenities
                .stream()
                .map(i -> new CustomerAmenityMapper(amenities).apply(i))
                .toList();
    }

    public ResponseEntity<?> assignToCustomer(String hostelId, AssignCustomer assignCustomer) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_AMENITIES, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!Utils.checkNullOrEmpty(assignCustomer.customerId())) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersService.getCustomerInformation(assignCustomer.customerId());
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equals(CustomerStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.AMENITY_CNNOT_ADD_BOOKED_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.AMENITY_CANNOT_ADD_SETTLEMENT_GENERATED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        if (assignCustomer.newAmenities() == null) {
            return new ResponseEntity<>(Utils.ATLEAST_ONE_AMENITY_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (assignCustomer.newAmenities().isEmpty()) {
            return new ResponseEntity<>(Utils.ATLEAST_ONE_AMENITY_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        List<AmenitiesV1> listAmenities = amentityRepository.findByHostelIdAndAmenityIdInAndIsDeletedFalse(hostelId, assignCustomer.newAmenities());
        if (listAmenities == null) {
            return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.BAD_REQUEST);
        }
        if (listAmenities.isEmpty()) {
            return new ResponseEntity<>(Utils.INVALID_AMENITY, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        List<String> newAmenityIds = listAmenities.stream()
                .map(AmenitiesV1::getAmenityId)
                .toList();

        List<CustomersAmenity> listCustomerAmenities = customerAmenityRepository.checkAmenitiesAlreadyAssigned(assignCustomer.customerId(), newAmenityIds);
        List<String> unassignedAmenities = new ArrayList<>();
        List<String> assignedAmenities;

        if (listCustomerAmenities != null && !listCustomerAmenities.isEmpty()) {
            assignedAmenities = listCustomerAmenities
                    .stream()
                    .map(CustomersAmenity::getAmenityId)
                    .toList();
        } else {
            assignedAmenities = new ArrayList<>();
        }

        if (!assignedAmenities.isEmpty()) {
            unassignedAmenities = newAmenityIds.stream()
                    .filter(id -> !assignedAmenities.contains(id))
                    .toList();
        }
        else {
            unassignedAmenities = newAmenityIds;

        }

        if (!unassignedAmenities.isEmpty()) {
            List<CustomersAmenity> listNewAmenities = unassignedAmenities
                    .stream()
                    .map(i -> {
                        CustomersAmenity customersAmenity = new CustomersAmenity();
                        List<AmenitiesV1> listNewAmenity = listAmenities.stream()
                                        .filter(itm -> itm.getAmenityId().equals(i))
                                        .toList();
                        AmenitiesV1 amenityV1 = null;
                        if (!listNewAmenity.isEmpty()) {
                            amenityV1 = listNewAmenity.get(0);
                        }
                        if (amenityV1 != null) {
                            customersAmenity.setAmenityPrice(amenityV1.getAmenityAmount());
                            customersAmenity.setAmenityId(amenityV1.getAmenityId());
                            customersAmenity.setCustomerId(assignCustomer.customerId());
                            customersAmenity.setCreatedAt(new Date());
                            customersAmenity.setCreatedBy(authentication.getName());
                            if (amenityV1.getIsProRate()) {
                                customersAmenity.setStartDate(new Date());
                            }
                            else {
                                customersAmenity.setStartDate(billingDates.currentBillStartDate());
                            }


                        }

                        return customersAmenity;
                    })
                    .toList();

            customerAmenityRepository.saveAll(listNewAmenities);
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public List<AmenitiesV1> findByAmenityIds(List<String> requestedAmenityIds) {
        return amentityRepository.findAllById(requestedAmenityIds);
    }

    public List<CustomersAmenity> getAllCustomerAmenitiesForRecurring(String customerId, Date billingDate) {
        List<CustomersAmenity> customersAmenities = customerAmenityRepository.getAllCustomersAmenityByCustomerIdAndEndDate(customerId, billingDate);
        if (customersAmenities == null) {
            customersAmenities = new ArrayList<>();
        }
        return customersAmenities;
    }
}
