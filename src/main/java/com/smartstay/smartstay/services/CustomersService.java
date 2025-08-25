package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.dto.customer.CustomerData;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private TransactionService transactionService;

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
            if (Utils.compareWithTodayDate(Utils.stringToDate(assignBed.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT))) {
                advanceAmount.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            } else {
                advanceAmount.setStatus(AdvanceStatus.PENDING.name());
            }
            advanceAmount.setInvoiceDate(Utils.stringToDate(assignBed.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT));
            advanceAmount.setDueDate(Utils.stringToDate(assignBed.dueDate(), Utils.USER_INPUT_DATE_FORMAT));


            customersRepository.save(customers);

            bookingsService.assignBedToCustomer(assignBed);

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

        } else {
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

        return bookingsService.getAllCheckInCustomers(hostelId);
    }

    public List<CustomerData> searchAndGetCustomers(String hostelId, String name, String type) {
        return customersRepository.getCustomerData(
                hostelId,
                name != null && !name.isBlank() ? name : null,
                type != null && !type.isBlank() ? type : null
        );
    }


    public ResponseEntity<?> getAllCustomersForHostel(String hostelId, String name, String type) {
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

        List<CustomerData> customerData = searchAndGetCustomers(hostelId, name, type);
        List<com.smartstay.smartstay.responses.customer.CustomerData> listCustomers = customerData.stream().map(item -> {
            StringBuilder initials = new StringBuilder();
            String[] nameArray = item.getFirstName().split(" ");
            initials.append(nameArray[0].toUpperCase().charAt(0));
            if (nameArray.length > 1) {
                initials.append(nameArray[nameArray.length - 1].toUpperCase().charAt(0));
            } else {
                initials.append(nameArray[0].toUpperCase().charAt(1));
            }
            return new com.smartstay.smartstay.responses.customer.CustomerData(item.getFirstName(),
                    item.getCity(),
                    item.getState(),
                    item.getCountry(),
                    item.getMobile(),
                    item.getCurrentStatus(),
                    item.getEmailId(),
                    item.getProfilePic(),
                    item.getBedId(),
                    item.getFloorId(),
                    item.getRoomId(),
                    item.getCustomerId(),
                    initials.toString(),
                    Utils.dateToString(item.getJoiningDate()),
                    Utils.dateToString(item.getActualJoiningDate()),
                    Utils.dateToString(item.getCreatedAt()));
        }).collect(Collectors.toList());
        return new ResponseEntity<>(listCustomers, HttpStatus.OK);
    }

    public ResponseEntity<?> createBooking(BookingRequest payloads, String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }
        Date dt = Utils.stringToDate(payloads.bookingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);


        if (bedsService.isBedAvailable(payloads.bedId(), user.getParentId(), dt)) {
            Customers customers = customersRepository.findById(payloads.customerId()).orElse(null);
            if (customers != null) {
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ON_NOTICE.name())) {
                    return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }
                customers.setKycStatus(KycStatus.PENDING.name());
                customers.setCurrentStatus(CustomerStatus.BOOKED.name());
                customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
                customers.setCountry(1L);
                customers.setCreatedBy(user.getUserId());
                customers.setCreatedAt(new Date());
                customers.setHostelId(hostelId);

                customers.setExpJoiningDate(dt);
                List<TransactionV1> transactions = transactionService.addBookingAmount(customers, payloads.bookingAmount());
                customers.setTransactions(transactions);
                customersRepository.save(customers);

                BookingsV1 bookingsV1 = new BookingsV1();
                bookingsV1.setHostelId(hostelId);
                bookingsV1.setFloorId(payloads.floorId());
                bookingsV1.setRoomId(payloads.roomId());
                bookingsV1.setBedId(payloads.bedId());
                bookingsV1.setCustomerId(customers.getCustomerId());
                bookingsV1.setCreatedAt(new Date());
                bookingsV1.setUpdatedAt(new Date());
                bookingsV1.setUpdatedBy(userId);
                bookingsV1.setLeavingDate(null);
                bookingsV1.setCurrentStatus(BedStatus.BOOKED.name());
                bookingsService.saveBooking(bookingsV1);



                return bedsService.addUserToBed(payloads.bedId(), payloads.joiningDate());
            } else {
                return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
            }
        }
        else {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }




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

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), payloads.hostelId())) {
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
        customers.setHostelId(payloads.hostelId());
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

    public ResponseEntity<?> checkinBookedCustomer(CheckinCustomer checkinRequest) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        Customers customers = customersRepository.findById(checkinRequest.customerId()).orElse(null);

        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!floorsService.checkFloorExistForHostel(checkinRequest.floorId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        if (!roomsService.checkRoomExistForFloor(checkinRequest.floorId(), checkinRequest.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.checkBedExistForRoom(checkinRequest.bedId(), checkinRequest.roomId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
        }

        String date = Utils.stringToDateFormat(checkinRequest.joiningDate().replace("/", "-"));

        if (bedsService.isBedAvailable(checkinRequest.bedId(), user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {


            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));

            Advance advance = customers.getAdvance();

            if (advance == null) {
                Advance ad = new Advance();
                ad.setAdvanceAmount(checkinRequest.advanceAmount());
                customers.setAdvance(ad);
            }

            transactionService.addAdvanceAmount(customers, checkinRequest.advanceAmount());

            BookingsV1 bookingsV1 = bookingsService.getBookingsByCustomerId(checkinRequest.customerId());
            bookingsV1.setBedId(checkinRequest.bedId());
            bookingsV1.setHostelId(customers.getHostelId());
            bookingsV1.setFloorId(checkinRequest.floorId());
            bookingsV1.setRoomId(checkinRequest.roomId());
            bookingsV1.setRentAmount(checkinRequest.rentAmount());
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setUpdatedBy(authentication.getName());
            bookingsV1.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            bookingsService.saveBooking(bookingsV1);

            bedsService.addUserToBed(checkinRequest.bedId(), date);

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }


    }

    public ResponseEntity<?> addCustomer(String hostelId, MultipartFile profilePic, com.smartstay.smartstay.payloads.customer.AddCustomer customerInfo) {
        if (authentication.isAuthenticated()) {
            String loginId = authentication.getName();
            Users user = userService.findUserByUserId(loginId);

            if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
                return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
            }

            if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
                return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
            }

            if (customersRepository.existsByMobile(customerInfo.mobileNumber())) {
                return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
            }

            String profileImage = null;
            if (profilePic != null) {
                profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic), "users/profile");
            }

            Customers customers = new Customers();
            customers.setFirstName(customerInfo.firstName());
            customers.setLastName(customerInfo.lastName());
            customers.setCountry(1l);
            customers.setMobile(customerInfo.mobileNumber());
            customers.setEmailId(customerInfo.emailId());
            customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.INACTIVE.name());
            customers.setHostelId(hostelId);
            customers.setCreatedBy(loginId);
            customers.setCreatedAt(new Date());
            customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
            customers.setProfilePic(profileImage);

            if (customerInfo.address() != null) {
                customers.setHouseNo(customerInfo.address().houseNo());
                customers.setStreet(customerInfo.address().street());
                customers.setLandmark(customerInfo.address().landmark());
                customers.setPincode(customerInfo.address().pincode());
                customers.setState(customerInfo.address().state());
                customers.setCity(customerInfo.address().city());
            }

            customersRepository.save(customers);

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> addCustomerPartialInfo(String hostelId, AddCustomerPartialInfo customerInfo, MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(customerInfo.mobile())) {
            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String profileImage = null;
        if (file != null) {
            profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
        }

        Customers customers = new Customers();
        customers.setFirstName(customerInfo.firstName());
        customers.setLastName(customerInfo.lastName());
        customers.setCountry(1l);
        customers.setMobile(customerInfo.mobile());
        customers.setEmailId(customerInfo.emailId());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.INACTIVE.name());
        customers.setHostelId(hostelId);
        customers.setCreatedBy(loginId);
        customers.setCreatedAt(new Date());
        customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
        customers.setProfilePic(profileImage);

        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateCustomerInfo(String customerId, UpdateCustomerInfo updateInfo, MultipartFile file) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!userHostelService.checkHostelAccess(loginId, customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (updateInfo != null) {
            if (updateInfo.mobile() != null && !updateInfo.mobile().equalsIgnoreCase("")) {
                if (customersRepository.findCustomersByMobile(customers.getCustomerId(), updateInfo.mobile()) > 0) {
                    return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
                }
                customers.setMobile(updateInfo.mobile());
            }

            String profileImage = null;
            if (file != null) {
                profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/profile");
                customers.setProfilePic(profileImage);
            }

            if (updateInfo.firstName() != null && !updateInfo.firstName().equalsIgnoreCase("")) {
                customers.setFirstName(updateInfo.firstName());
            }
            if (updateInfo.lastName() != null && !updateInfo.lastName().equalsIgnoreCase("")) {
                customers.setLastName(updateInfo.lastName());
            }
            if (updateInfo.mailId() != null && !updateInfo.mailId().equalsIgnoreCase("")) {
                customers.setEmailId(updateInfo.mailId());
            }
            if (updateInfo.houseNo() != null && !updateInfo.houseNo().equalsIgnoreCase("")) {
                customers.setHouseNo(updateInfo.houseNo());
            }
            if (updateInfo.street() != null && !updateInfo.street().equalsIgnoreCase("")) {
                customers.setStreet(updateInfo.street());
            }
            if (updateInfo.landmark() != null && !updateInfo.landmark().equalsIgnoreCase("")) {
                customers.setLandmark(updateInfo.landmark());
            }
            if (updateInfo.pincode() != null) {
                customers.setPincode(updateInfo.pincode());
            }
            if (updateInfo.city() != null && !updateInfo.city().equalsIgnoreCase("")) {
                customers.setCity(updateInfo.city());
            }
            if (updateInfo.state() != null && !updateInfo.state().equalsIgnoreCase("")) {
                customers.setState(updateInfo.state());
            }

            customersRepository.save(customers);

            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

        }
        else {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }






    }

//    public ResponseEntity<?> requestCheckout(CheckoutRequest checkoutRequest) {
//
//    }
}
