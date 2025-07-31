package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.Advance;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.BookingRequest;
import com.smartstay.smartstay.payloads.customer.CheckInRequest;
import com.smartstay.smartstay.repositories.BookingsRepository;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class CustomersService {

    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private BookingsService bookingsService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private UsersService userService;

    @Autowired
    private FloorsService floorsService;

    @Autowired
    private RoomsService roomsService;

    @Autowired
    private BedsService bedsService;




    @Autowired
    private Authentication authentication;

    @Autowired
    private UserHostelService userHostelService;

    public ResponseEntity<?> createCustomer(MultipartFile file, AddCustomer payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
         profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.mailId());
        customers.setHouseNo(payloads.houseNo());
        customers.setStreet(payloads.street());
        customers.setLandmark(payloads.landmark());
        customers.setPincode(payloads.pincode());
        customers.setCity(payloads.city());
        customers.setState(payloads.state());
        customers.setCountry(customers.getCountry());
        customers.setProfilePic(profileImage);
        customers.setKycStatus(KycStatus.PENDING.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCountry(1L);
        customers.setCreatedBy(user.getUserId());
        customers.setCreatedAt(new Date());

        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public ResponseEntity<?> assignBed(AssignBed assignBed) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);
        if (rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            Customers customers = customersRepository.findById(assignBed.customerId()).orElse(null);

            if (customers == null) {
                return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
            }

            customers.setCurrentStatus(CustomerStatus.ACTIVE.name());
            Advance advanceAmount = new Advance();
            advanceAmount.setCustomers(customers);
            advanceAmount.setAdvanceAmount(assignBed.advanceAmount());
            advanceAmount.setCreatedBy(userId);
            advanceAmount.setCreatedAt(new Date());
            advanceAmount.setAdvanceAmount(assignBed.advanceAmount());
            if (Utils.compareWithTodayDate(Utils.stringToDate(assignBed.invoiceDate()))) {
                advanceAmount.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            }
            else {
                advanceAmount.setStatus(AdvanceStatus.PENDING.name());
            }
            advanceAmount.setInvoiceDate(Utils.stringToDate(assignBed.invoiceDate()));
            advanceAmount.setDueDate(Utils.stringToDate(assignBed.dueDate()));


            customersRepository.save(customers);

            bookingsService.assignBedToCustomer(assignBed);

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

        }
        else {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

    }

    public ResponseEntity<?> getAllCheckInCustomers(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

//        customersRepository.

        return null;
    }

    public ResponseEntity<?> createBooking(MultipartFile file, BookingRequest payloads, String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        System.out.println("text---->>>"+hostelId);
        if (!userHostelService.checkHostelAccess(user.getUserId(),hostelId)){
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.mailId());
        customers.setHouseNo(payloads.houseNo());
        customers.setStreet(payloads.street());
        customers.setLandmark(payloads.landmark());
        customers.setPincode(payloads.pincode());
        customers.setCity(payloads.city());
        customers.setState(payloads.state());
        customers.setCountry(customers.getCountry());
        customers.setProfilePic(profileImage);
        customers.setKycStatus(KycStatus.PENDING.name());
        customers.setCurrentStatus(CustomerStatus.BOOKED.name());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCountry(1L);
        customers.setCreatedBy(user.getUserId());
        customers.setCreatedAt(new Date());
        customers.setExpJoiningDate(payloads.bookingDate());

        Advance advance = new Advance();
        advance.setCreatedAt(new Date());
        advance.setCreatedBy(user.getCreatedBy());
        advance.setAdvanceAmount(payloads.bookingAmount());
        customers.setAdvance(advance);
        Customers savedCustomer = customersRepository.save(customers);

        BookingsV1 bookingsV1 = new BookingsV1();
        bookingsV1.setHostelId(hostelId);
        bookingsV1.setCustomerId(savedCustomer.getCustomerId());
        bookingsV1.setCreatedAt(new Date());
        bookingsV1.setUpdatedAt(new Date());
        bookingsV1.setLeavingDate(null);
        bookingsV1.setCurrentStatus(BedStatus.BOOKED.name());
        bookingsService.saveBooking(bookingsV1);


        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public ResponseEntity<?> addCheckIn(MultipartFile file, CheckInRequest payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(payloads.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), payloads.hostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!floorsService.checkFloorExistForHostel(payloads.floorId(), payloads.hostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.UNAUTHORIZED);
        }

        if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.UNAUTHORIZED);
        }

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(),payloads.hostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.UNAUTHORIZED);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.mailId());
        customers.setHouseNo(payloads.houseNo());
        customers.setStreet(payloads.street());
        customers.setLandmark(payloads.landmark());
        customers.setPincode(payloads.pincode());
        customers.setCity(payloads.city());
        customers.setState(payloads.state());
        customers.setCountry(customers.getCountry());
        customers.setProfilePic(profileImage);
        customers.setKycStatus(KycStatus.PENDING.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCountry(1L);
        customers.setCreatedBy(user.getUserId());
        customers.setCreatedAt(new Date());
        Customers savedCustomer = customersRepository.save(customers);

        BookingsV1 bookingsV1 = new BookingsV1();
        bookingsV1.setHostelId(payloads.hostelId());
        bookingsV1.setBedId(payloads.bedId());
        bookingsV1.setFloorId(payloads.floorId());
        bookingsV1.setCustomerId(savedCustomer.getCustomerId());
        bookingsV1.setCreatedAt(new Date());
        bookingsV1.setUpdatedAt(new Date());
        bookingsV1.setLeavingDate(null);
        bookingsV1.setCurrentStatus(BedStatus.OCCUPIED.name());
        bookingsV1.setRoomId(payloads.roomId());
        String rawDateStr = payloads.joiningDate();
        if (rawDateStr != null) {
            rawDateStr = rawDateStr.replace("-", "/");
        }
        Date joiningDate = Utils.convertStringToDate(rawDateStr);
        bookingsV1.setJoiningDate(joiningDate);
        bookingsService.saveBooking(bookingsV1);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }
}
