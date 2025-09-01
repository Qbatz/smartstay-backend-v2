package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.CustomerData;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.responses.customer.AddCustomerError;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_READ)) {
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
            String currentStatus = null;
            if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
                currentStatus = "Booked";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                currentStatus = "Vacated";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ON_NOTICE.name())) {
                currentStatus = "Notice Period";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
                currentStatus = "Checked In";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
                currentStatus = "Inactive";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ACTIVE.name())) {
                currentStatus = "Active";
            }
            return new com.smartstay.smartstay.responses.customer.CustomerData(item.getFirstName(),
                    item.getCity(),
                    item.getState(),
                    item.getCountry(),
                    item.getMobile(),
                    currentStatus,
                    item.getEmailId(),
                    item.getProfilePic(),
                    item.getBedId(),
                    item.getFloorId(),
                    item.getRoomId(),
                    item.getCustomerId(),
                    initials.toString(),
                    Utils.dateToString(item.getJoiningDate()),
                    Utils.dateToString(item.getActualJoiningDate()),
                    item.getCountryCode(),
                    Utils.dateToString(item.getCreatedAt()),
                    item.getBedName(),
                    item.getRoomName(),
                    item.getFloorName());
        }).collect(Collectors.toList());
        return new ResponseEntity<>(listCustomers, HttpStatus.OK);
    }

    /**
     * For Booking flow.
     *
     * Do not use anywhere else
     * @param payloads
     * @param hostelId
     * @return
     */

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

    /**
     *
     *  for check in the customers
     *
     *
     */

    public ResponseEntity<?> addCheckIn(String customerId, CheckInRequest payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!Utils.checkNullOrEmpty(customerId)) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
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

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
        }

        String date = payloads.joiningDate().replace("/", "-");
        if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
            return new ResponseEntity<>(Utils.CHECK_IN_FUTURE_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (bedsService.isBedAvailable(payloads.bedId(), user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {

            customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            Customers savedCustomer = customersRepository.save(customers);

            bedsService.addUserToBed(payloads.bedId(), payloads.joiningDate().replace("/", "-"));

            BookingsV1 bookingsV1 = bookingsService.findBookingsByCustomerIdAndHostelId(customerId, payloads.hostelId());
            if (bookingsV1 != null) {
                bookingsV1.setUpdatedAt(new Date());
                bookingsV1.setLeavingDate(null);
                if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
                    bookingsV1.setCurrentStatus(BedStatus.BOOKED.name());
                }
                else {
                    bookingsV1.setCurrentStatus(BedStatus.OCCUPIED.name());
                }
                bookingsV1.setRoomId(payloads.roomId());
                String rawDateStr = payloads.joiningDate().replace("-", "/");

                Date joiningDate = Utils.convertStringToDate(rawDateStr);
                bookingsV1.setJoiningDate(joiningDate);
                bookingsService.saveBooking(bookingsV1);
            }else {
                bookingsService.checkinCustomer(payloads, customerId);
            }

            transactionService.addAdvanceAmount(customers, payloads.advanceAmount());


            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }else {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }



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

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
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

        String date = checkinRequest.joiningDate().replace("/", "-");

        if (bedsService.isBedAvailable(checkinRequest.bedId(), user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {

            if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
                //future booking
                customers.setCurrentStatus(CustomerStatus.BOOKED.name());
            }
            else {
                customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            }

            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));

            Advance advance = customers.getAdvance();

            if (advance == null) {
                Advance ad = new Advance();
                ad.setAdvanceAmount(checkinRequest.advanceAmount());
                customers.setAdvance(ad);
            }

            transactionService.addAdvanceAmount(customers, checkinRequest.advanceAmount());

            BookingsV1 bookingsV1 = bookingsService.getBookingsByCustomerId(checkinRequest.customerId());
            if (bookingsV1 == null) {
                bookingsV1 = new BookingsV1();
            }
            bookingsV1.setCustomerId(customers.getCustomerId());
            bookingsV1.setBedId(checkinRequest.bedId());
            bookingsV1.setHostelId(customers.getHostelId());
            bookingsV1.setFloorId(checkinRequest.floorId());
            bookingsV1.setRoomId(checkinRequest.roomId());
            bookingsV1.setRentAmount(checkinRequest.rentAmount());
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setUpdatedBy(authentication.getName());
            if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
                bookingsV1.setCurrentStatus(CustomerStatus.BOOKED.name());
            }
            else {
                bookingsV1.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            }

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

            String mobileStatus = "";
            String emailStatus = "";

            if (customerInfo.emailId() !=null && !customerInfo.emailId().isEmpty() && customersRepository.existsByEmailId(customerInfo.emailId())) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }

            if (customerInfo.mobileNumber() !=null && !customerInfo.mobileNumber().isEmpty() && customersRepository.existsByMobile(customerInfo.mobileNumber())) {
                mobileStatus = Utils.MOBILE_NO_EXISTS;
            }

            if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
                Map<String, String> map = Map.of(
                        "mobileStatus", mobileStatus,
                        "emailStatus", emailStatus,
                        "message", "Validation failed"
                );
                return new ResponseEntity<>(
                        map,
                        HttpStatus.BAD_REQUEST
                );
            }

            String profileImage = null;
            if (profilePic != null) {
                profileImage = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(profilePic), "users/profile");
            }

            Customers customers = new Customers();
            customers.setFirstName(customerInfo.firstName());
            customers.setLastName(customerInfo.lastName());
            customers.setCountry(1L);
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
                if (Utils.checkNullOrEmpty(customerInfo.address().houseNo())) {
                    customers.setHouseNo(customerInfo.address().houseNo());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().street())) {
                    customers.setStreet(customerInfo.address().street());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().landmark())) {
                    customers.setLandmark(customerInfo.address().landmark());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().pincode())) {
                    customers.setPincode(customerInfo.address().pincode());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().state())) {
                    customers.setState(customerInfo.address().state());
                }
                if (Utils.checkNullOrEmpty(customerInfo.address().city())) {
                    customers.setCity(customerInfo.address().city());
                }
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
        String mobileStatus = "";
        String emailStatus = "";

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (customersRepository.existsByMobile(customerInfo.mobile())) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
//            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(customerInfo.emailId())) {
            if (customersRepository.existsByEmailId(customerInfo.emailId())) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }
        }

        if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
            AddCustomerError error = new AddCustomerError(mobileStatus, emailStatus);

            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    public ResponseEntity<?> requestNotice(String customerId, String hostelId, CheckoutNotice checkoutNotice) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (customerId == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }


        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ON_NOTICE.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ON_NOTICE, HttpStatus.BAD_REQUEST);
        }

        customers.setCurrentStatus(CustomerStatus.ON_NOTICE.name());


        bedsService.updateBedToNotice(bookingsService.getBedIdFromBooking(customerId, hostelId), checkoutNotice.checkoutDate());
        bookingsService.moveToNotice(customerId, checkoutNotice.checkoutDate(), checkoutNotice.requestDate(), checkoutNotice.reason());
        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }


}
