package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.invoices.UnpaidInvoicesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedRoomFloor;
import com.smartstay.smartstay.dto.customer.CustomerData;
import com.smartstay.smartstay.dto.customer.CustomersBookingDetails;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.beds.ChangeBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.responses.customer.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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

    @Autowired
    private InvoiceV1Service invoiceService;

    @Autowired
    private HostelService hostelService;

    @Autowired
    private BankingService bankingService;
    @Autowired
    private CustomersBedHistoryService bedHistory;

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
        HashMap<String, String> filterOption = new HashMap<>();
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
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
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
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
                currentStatus = "Cancelled";
            }
            else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                currentStatus = "Settlement Generated";
            }

            if (!filterOption.containsKey(currentStatus)) {
                filterOption.put(currentStatus, currentStatus);
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
                    Utils.dateToString(item.getExpectedJoiningDate()),
                    Utils.dateToString(item.getActualJoiningDate()),
                    item.getCountryCode(),
                    Utils.dateToString(item.getCreatedAt()),
                    item.getBedName(),
                    item.getRoomName(),
                    item.getFloorName());
        }).collect(Collectors.toList());

//        CustomersList response = new CustomersList(listCustomers, null);
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

        if (!floorsService.checkFloorExistForHostel(payloads.floorId(), hostelId)) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), hostelId)) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
        }
        Date dt = Utils.stringToDate(payloads.bookingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (bedsService.isBedAvailable(payloads.bedId(), user.getParentId(), joiningDate)) {
            Customers customers = customersRepository.findById(payloads.customerId()).orElse(null);
            if (customers != null) {
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                    return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }
                customers.setKycStatus(KycStatus.PENDING.name());
                customers.setCurrentStatus(CustomerStatus.BOOKED.name());
                customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
                customers.setCountry(1L);
                customers.setCreatedBy(user.getUserId());
                customers.setCreatedAt(new Date());
                customers.setHostelId(hostelId);

                customers.setExpJoiningDate(joiningDate);

                String invoiceId = invoiceService.addBookingInvoice(customers.getCustomerId(), payloads.bookingAmount(), InvoiceType.BOOKING.name(), hostelId, customers.getMobile(), customers.getEmailId(), payloads.bankId(), payloads.referenceNumber());
//                List<TransactionV1> transactions = transactionService.addBookingAmount(customers, payloads.bookingAmount());
//                customers.setTransactions(transactions);
                customersRepository.save(customers);

                bookingsService.addBooking(hostelId, payloads);

                AddPayment addPayment = new AddPayment(payloads.bankId(), payloads.bookingDate(), payloads.referenceNumber(), payloads.bookingAmount());
                transactionService.recordPaymentForBooking(hostelId, invoiceId, addPayment);
                return bedsService.assignCustomer(payloads.bedId(), payloads.joiningDate().replace("/", "-"));
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
     *  for customers who are not booked
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
        HostelV1 hostelV1 = hostelService.getHostelInfo(payloads.hostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
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

            Advance advance = customers.getAdvance();
            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            }
            else {
                listDeductions = advance.getDeductions();
            }
            listDeductions.addAll(payloads.deductions()
                    .stream()
                    .map(item -> new Deductions(item.type(), item.amount()))
                    .toList());
            advance.setAdvanceAmount(payloads.advanceAmount());
            advance.setCustomers(customers);
            advance.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            advance.setCreatedBy(userId);
            advance.setCreatedAt(new Date());
            advance.setDeductions(listDeductions);
            advance.setInvoiceDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            advance.setUpdatedAt(new Date());

            customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            customers.setAdvance(advance);
            Customers savedCustomer = customersRepository.save(customers);

            bedsService.addUserToBed(payloads.bedId(), payloads.joiningDate().replace("/", "-"));

            bookingsService.addCheckin(customerId, payloads);

            Calendar calendar = Calendar.getInstance();
            int dueDate = calendar.get(Calendar.DAY_OF_MONTH) + 5;
            int day = 1;
            if (hostelV1.getElectricityConfig() != null) {
                day = hostelV1.getElectricityConfig().getBillDate();
            }

            invoiceService.addInvoice(customerId, payloads.advanceAmount(), InvoiceType.ADVANCE.name(), payloads.hostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), day);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, day);

            Date startateOfCurrentCycle = cal.getTime();
            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            //checking joining date is fall under todays date
            if (Utils.compareWithTwoDates(joiningDate, startateOfCurrentCycle) < 0) {
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }
            calculateRentAndCreateRentalInvoice(customers, payloads);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

        }else {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }



    }

    public ResponseEntity<?> checkinBookedCustomer(String customerId, CheckInBookedCustomer checkinRequest) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        Customers customers = customersRepository.findById(customerId).orElse(null);
        BookingsV1 booking = bookingsService.findByBookingId(checkinRequest.bookingId());
        if (booking == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
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

        if (!floorsService.checkFloorExistForHostel(booking.getFloorId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        if (!roomsService.checkRoomExistForFloor(booking.getFloorId(), booking.getRoomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
        }

        if (!bedsService.checkBedExistForRoom(booking.getBedId(), booking.getRoomId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
        }

        String date = checkinRequest.joiningDate().replace("/", "-");

        if (bedsService.checkAvailabilityForCheckIn(booking.getBedId(), Utils.stringToDate(checkinRequest.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT)) != null) {

            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());

            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));

            Advance advance = customers.getAdvance();

            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            }
            else {
                listDeductions = advance.getDeductions();
            }
            listDeductions.addAll(checkinRequest.deductions()
                    .stream()
                    .map(item -> new Deductions(item.type(), item.amount()))
                    .toList());

            advance.setAdvanceAmount(checkinRequest.advanceAmount());
            advance.setStatus(AdvanceStatus.INVOICE_GENERATED.name());
            advance.setCreatedBy(userId);
            advance.setCreatedAt(new Date());
            advance.setDeductions(listDeductions);
            advance.setInvoiceDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            advance.setUpdatedAt(new Date());
            advance.setCustomers(customers);

            customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));
            customers.setAdvance(advance);

            Customers savedCustomer = customersRepository.save(customers);

            bedsService.addUserToBed(booking.getBedId(), date);

            CheckInRequest request = new CheckInRequest(
                    booking.getHostelId(),
                    booking.getFloorId(),
                    booking.getBedId(),
                    booking.getRoomId(),
                    checkinRequest.joiningDate(),
                    checkinRequest.advanceAmount(),
                    checkinRequest.rentalAmount(),
                    checkinRequest.stayType(),
                    checkinRequest.deductions()
            );

            bookingsService.checkInBookedCustomer(customerId, request);

            Calendar calendar = Calendar.getInstance();
            int dueDate = calendar.get(Calendar.DAY_OF_MONTH) + 5;

            int day = 1;
            if (hostelV1.getElectricityConfig() != null) {
                day = hostelV1.getElectricityConfig().getBillDate();
            }

            invoiceService.addInvoice(customerId, checkinRequest.advanceAmount(), InvoiceType.ADVANCE.name(), booking.getHostelId(), customers.getMobile(), customers.getEmailId(), date, day);

            bedsService.addUserToBed(booking.getBedId(), date);


            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));

            Date currentCycleStartDate = cal.getTime();
            Date joiningDate = Utils.stringToDate(checkinRequest.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            //check joining date is in this current cycle.
            if (Utils.compareWithTwoDates(joiningDate, currentCycleStartDate) < 0) {
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
            }

            calculateRentAndCreateRentalInvoice(customers, request);

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

            if (customerInfo.emailId() !=null && !customerInfo.emailId().isEmpty() && customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(customerInfo.emailId(),hostelId, List.of("VACATED"))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }

            if (customerInfo.mobileNumber() !=null && !customerInfo.mobileNumber().isEmpty() && customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(customerInfo.mobileNumber(),hostelId, List.of("VACATED"))) {
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

        if (customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(customerInfo.mobile(), hostelId, List.of("VACATED"))) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
//            return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(customerInfo.emailId())) {
            if (customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(customerInfo.emailId(), hostelId, List.of("VACATED"))) {
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

    public ResponseEntity<?> requestNotice(String hostelId, CheckoutNotice checkoutNotice) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (checkoutNotice.customerId() == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }


        Customers customers = customersRepository.findById(checkoutNotice.customerId()).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ON_NOTICE, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 booking = bookingsService.getBookingsByCustomerId(checkoutNotice.customerId());
        if (booking == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }
        Date joiningDate = booking.getJoiningDate();
        Date requestDate = Utils.stringToDate(checkoutNotice.requestDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date checkoutDate = Utils.stringToDate(checkoutNotice.checkoutDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (Utils.compareWithTwoDates(requestDate, joiningDate) <= 0) {
            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(checkoutDate, joiningDate) <= 0) {
            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (Utils.compareWithTwoDates(requestDate, billingDates.currentBillStartDate()) < 0) {
            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_BILLING_START_DATE + Utils.dateToString(billingDates.currentBillStartDate()), HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(checkoutDate, requestDate) <= 0) {
            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_REQUEST_DATE, HttpStatus.BAD_REQUEST);
        }

        customers.setCurrentStatus(CustomerStatus.NOTICE.name());


        bedsService.updateBedToNotice(bookingsService.getBedIdFromBooking(customers.getCustomerId(), hostelId), checkoutNotice.checkoutDate());
        bookingsService.moveToNotice(customers.getCustomerId(), checkoutNotice.checkoutDate(), checkoutNotice.requestDate(), checkoutNotice.reason());
        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }


    public static AdvanceInfo toAdvanceInfoResponse(Advance advance, InvoiceResponse invoicesV1) {
        if (advance == null) return null;
        double maintenanceAmount = 0.0;
        double otherDeductionsAmount = 0.0;

        if (advance.getDeductions() != null && !advance.getDeductions().isEmpty()) {
            for (Deductions d : advance.getDeductions()) {
                if (d.getType() == null || d.getAmount() == null) continue;

                String type = d.getType().trim().toLowerCase();
                if (type.equals("maintenance")) {
                    maintenanceAmount += d.getAmount();
                } else {
                    otherDeductionsAmount += d.getAmount();
                }
            }
        }

        double dueAmount = (advance.getAdvanceAmount() != 0)
                ? advance.getAdvanceAmount() - invoicesV1.paidAmount()
                : 0.0;

        return new AdvanceInfo(
                advance.getInvoiceDate() != null ? Utils.dateToString(advance.getInvoiceDate()) : null,
                invoicesV1.dueDate(),
                dueAmount,
                advance.getAdvanceAmount(),
                invoicesV1.paymentStatus(),
                maintenanceAmount,
                otherDeductionsAmount,
                invoicesV1.paidAmount()
        );
    }

    public Customers getCustomerInformation(String customerId) {
        return customersRepository.findById(customerId).orElse(null);
    }

    public Customers markCustomerInactive(Customers customers) {
        customers.setCurrentStatus(CustomerStatus.CANCELLED_BOOKING.name());
        customers.setLastUpdatedAt(new Date());
        customers.setUpdatedBy(authentication.getName());

        return customersRepository.save(customers);
    }

    public ResponseEntity<?> getCustomerDetails(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = userService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (customerId == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        StringBuilder initials = new StringBuilder();
        initials.append(customers.getFirstName().toUpperCase().charAt(0));
        if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
            initials.append(customers.getLastName().toUpperCase().charAt(0));
        }
        else {
            initials.append(customers.getFirstName().toUpperCase().charAt(1));
        }
        String fullName = customers.getFirstName() + " " + customers.getLastName();

        CustomersBookingDetails bookingDetails = bookingsService.getCustomerBookingDetails(customers.getCustomerId());
        HostelInformation hostelInformation = null;
        Advance advance = customers.getAdvance();
        List<Deductions> listDeduction = null;
        List<Deductions> otherDeductionBreakup = null;
        double maintenance = 0;
        double otherDeductions = 0;
        double advanceAmount = 0;
        if (advance != null) {
            advanceAmount = advance.getAdvanceAmount();
            listDeduction = advance.getDeductions();
            if (listDeduction != null) {
                maintenance = listDeduction
                        .stream()
                        .filter(item -> item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(Deductions::getAmount)
                        .sum();
                otherDeductions = listDeduction
                        .stream()
                        .filter(item -> !item.getType().equalsIgnoreCase("maintenance"))
                        .mapToDouble(Deductions::getAmount)
                        .sum();
                otherDeductionBreakup = listDeduction
                        .stream()
                        .filter(item -> !item.getType().equalsIgnoreCase("maintenance"))
                        .collect(Collectors.toList());

            }

        }
        if (bookingDetails != null) {
            hostelInformation = new HostelInformation(bookingDetails.getRoomName(),
                    bookingDetails.getRoomId(),
                    bookingDetails.getFloorName(),
                    bookingDetails.getFloorId(),
                    bookingDetails.getBedName(),
                    bookingDetails.getBedId(),
                    Utils.dateToString(bookingDetails.getJoiningDate()),
                    bookingDetails.getCurrentStatus(),
                    advanceAmount,
                    otherDeductions,
                    maintenance,
                    bookingDetails.getRentAmount(),
                    otherDeductionBreakup);
        }

        CustomerAddress address = new CustomerAddress(customers.getStreet(),
                customers.getHouseNo(),
                customers.getLandmark(),
                customers.getPincode(),
                customers.getCity(),
                customers.getState());
        KycDetails kycDetails = customers.getKycDetails();
        KycInformations kycInfo = null;
        if (kycDetails == null) {
            kycInfo = new KycInformations(customers.getKycStatus(),
                    null,
                    null,
                    null);
        }
        else {
            kycInfo = new KycInformations(kycDetails.getCurrentStatus(),
                    null,
                    null,
                    null);
        }


        List<InvoiceResponse> invoiceResponseList = invoiceService.getInvoiceResponseList(customers.getCustomerId());
        InvoiceResponse advanceInvoice = invoiceResponseList.stream()
                .filter(inv -> "ADVANCE".equalsIgnoreCase(inv.invoiceType()))
                .limit(1)
                .findFirst()
                .orElse(null);
        AdvanceInfo advanceInfo = toAdvanceInfoResponse(advance, advanceInvoice);
        List<BedHistory> listBeds = bedHistory.getCustomersBedHistory(customers.getCustomerId());

        CustomerDetails details = new CustomerDetails(customers.getCustomerId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName,
                customers.getEmailId(),
                customers.getMobile(),
                "91",
                initials.toString(),
                customers.getProfilePic(),
                address,
                hostelInformation,
                kycInfo,
                advanceInfo,
                invoiceResponseList,
                listBeds);

        return new ResponseEntity<>(details, HttpStatus.OK);
    }


    public void calculateRentAndCreateRentalInvoice(Customers customers,  CheckInRequest payloads) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(payloads.hostelId());
        if (hostelV1 != null) {

            int lastRulingBillDate = 1;
            if (!hostelV1.getBillingRulesList().isEmpty()) {
                lastRulingBillDate  = hostelV1.getBillingRulesList().get(0).getBillingStartDate();
            }

            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

            Calendar cal = Calendar.getInstance();
            cal.setTime(joiningDate);
            cal.set(Calendar.DAY_OF_MONTH, lastRulingBillDate);
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));

            Date lastDate = Utils.findLastDate(lastRulingBillDate, cal.getTime());

            Calendar c = Calendar.getInstance();
            c.setTime(joiningDate);


            long noOfDaysInCurrentMonth = Utils.findNumberOfDays(cal.getTime(), lastDate);
            long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), lastDate);
            double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
            double finalRent  = calculateRentPerDay * noOfDaysLeftInCurrentMonth;
                if (finalRent > payloads.rentalAmount()) {
                    finalRent = payloads.rentalAmount();
                }
                int day = 1;
                if (hostelV1.getElectricityConfig() != null) {
                    day = hostelV1.getElectricityConfig().getBillDate();
                }

            invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), payloads.hostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), day);

        }


    }

    public ResponseEntity<?> getInformationForFinalSettlement(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        BookingsV1 bookingDetails = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingDetails == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        double bookingAmount = 0.0;
        if (bookingDetails.getBookingAmount() != null) {
            bookingAmount = bookingDetails.getBookingAmount();
        }

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        boolean isAdvancePaid = false;

        Double advancePaidAmount = 0.0;
        double currentMonthPayableRent = 0.0;
        long noOfDaySatayed = 0l;
        double currentRentPaid = 0.0;
        Double currentMonthRent = 0.0;

        double unpaidInvoiceAmount = 0.0;
        double partialPaidAmount = 0.0;
        double totalAmountToBePaid = 0.0;
        Double totalDeductions = 0.0;
        boolean isRefundable = false;
        boolean isCurrentRentPaid = false;

        if (customers.getFirstName() != null) {
            fullName.append(customers.getFirstName());
            initials.append(customers.getFirstName().toUpperCase().charAt(0));
        }
        if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(customers.getLastName());
            initials.append(customers.getLastName().toUpperCase().charAt(0));
        }
        else {
            if (customers.getFirstName().length() > 1) {
                initials.append(customers.getFirstName().toUpperCase().charAt(1));
            }
        }

        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());

        if (customers.getAdvance() != null) {
            totalDeductions = customers.getAdvance()
                    .getDeductions()
                    .stream()
                    .mapToDouble(Deductions::getAmount)
                    .sum();
        }

        StayInfo stayInfo = new StayInfo(Utils.dateToString(bookingDetails.getBookingDate()),
                Utils.dateToString(bookingDetails.getNoticeDate()),
                Utils.dateToString(bookingDetails.getLeavingDate()));

        List<InvoicesV1> listUnpaidInvoices = invoiceService.listAllUnpaidInvoices(customerId, customers.getHostelId());

        List<InvoicesV1> listUnpaidRentalInvoices = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0)
                .toList();

        List<InvoicesV1> currentMonthInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) >= 0)
                .toList();

        Calendar calStartDate = Calendar.getInstance();
        calStartDate.setTime(billDate.currentBillStartDate());

        Date billStartDate = null;
        Calendar calBillStartDate = Calendar.getInstance();
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) < 0) {
            calBillStartDate.setTime(billDate.currentBillStartDate());
        }
        else {
            calBillStartDate.setTime(bookingDetails.getJoiningDate());
        }

        billStartDate = calBillStartDate.getTime();




        Calendar calEndDate = Calendar.getInstance();
        calEndDate.setTime(billDate.currentBillEndDate());

        Long findNoOfDaysInCurrentMonth = Utils.findNumberOfDays(calStartDate.getTime(), calEndDate.getTime());

        noOfDaySatayed = Utils.findNumberOfDays(billStartDate, new Date());

        //taken from unpaid invoices. So current month invoice is empty for paid
        if (!currentMonthInvoice.isEmpty()) {
            InvoicesV1 currentInvoice = currentMonthInvoice.get(0);
            currentMonthRent = currentInvoice.getTotalAmount();

            List<String> currentMonthInfo = new ArrayList<>();
            currentMonthInfo.add(currentInvoice.getInvoiceId());

            currentRentPaid = transactionService.getTransactionInfo(currentMonthInfo)
                    .stream()
                    .mapToDouble(PartialPaidInvoiceInfo::paidAmount)
                    .sum();
        }
        else {
            //current month invoice is paid
            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthInvoice(customerId);
            if (invoicesV1 != null) {
                currentMonthRent = invoicesV1.getTotalAmount();
                currentRentPaid = invoicesV1.getTotalAmount();
                isCurrentRentPaid = true;
            }
        }


        double rentPerDay = ((bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth)* 100.0)/100.0;
//        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billStartDate) < 0) {
//            rentPerDay = currentMonthRent / findNoOfDaysInCurrentMonth;
//        }
//        else {
//
//            noOfDaySatayed =  Utils.findNumberOfDays(bookingDetails.getJoiningDate(), new Date());
////            long noOfDaysLeft = Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), new Date());
//
//            rentPerDay = currentMonthRent / noOfDaySatayed;
//        }

        currentMonthPayableRent = Math.round(noOfDaySatayed * rentPerDay)*100.0/100.0;

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                .toList();

        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            }
            else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            }
            else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }
        }
        else {
            InvoicesV1 invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
            Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
            if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = paidAmount;
            }
        }

        advancePaidAmount = advancePaidAmount + bookingAmount;


        List<String> partialPaymentInvoices = listUnpaidRentalInvoices
                .stream()
                .filter(invoicesV1 -> invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                .map(InvoicesV1::getInvoiceId)
                .toList();

        List<PartialPaidInvoiceInfo> lisPartialPayments = transactionService.getTransactionInfo(partialPaymentInvoices);

        partialPaidAmount = lisPartialPayments
                .stream()
                .mapToDouble(PartialPaidInvoiceInfo::paidAmount)
                .sum();
        unpaidInvoiceAmount = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0)
                .mapToDouble(InvoicesV1::getTotalAmount)
                .sum();

        double invoiceBalance = unpaidInvoiceAmount - partialPaidAmount;

        totalAmountToBePaid =  invoiceBalance - advancePaidAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthPayableRent - currentRentPaid);
        }
        else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthPayableRent;
        }

        totalAmountToBePaid = totalAmountToBePaid + totalDeductions;

//        totalAmountToBePaid = unpaidInvoiceAmount - partialPaidAmount;
//        if (!isAdvancePaid) {
//            totalAmountToBePaid = totalAmountToBePaid + totalDeductions;
//        }
//        else {
//            totalAmountToBePaid = totalAmountToBePaid - (advancePaidAmount - totalDeductions);
//        }



        List<UnpaidInvoices> unpaidInvoices = listUnpaidRentalInvoices
                .stream()
                .map(item -> new UnpaidInvoicesMapper(lisPartialPayments).apply(item))
                .toList();

        CustomerInformations customerInformations = new CustomerInformations(customers.getCustomerId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName.toString(),
                customers.getProfilePic(),
                initials.toString(),
                Utils.dateToString(bookingDetails.getJoiningDate()),
                customers.getAdvance().getAdvanceAmount(),
                bookingDetails.getRentAmount(),
                isAdvancePaid,
                advancePaidAmount,
                bookingAmount,
                customers.getAdvance().getDeductions());

        if (Double.isInfinite(rentPerDay)) {
            rentPerDay = 0;
        }

        RentInfo rentInfo = new RentInfo(currentMonthPayableRent,
                currentRentPaid,
                (int) noOfDaySatayed,
                currentMonthRent,
                rentPerDay,
                Utils.dateToString(calStartDate.getTime()),
                Utils.dateToString(calEndDate.getTime()));

        if (totalAmountToBePaid < 0) {
            isRefundable = true;
        }

        SettlementInfo settlementInfo = new SettlementInfo(totalAmountToBePaid,
                totalDeductions,
                unpaidInvoiceAmount,
                isRefundable);



        FinalSettlement finalSettlement = new FinalSettlement(customerInformations, stayInfo, unpaidInvoices, rentInfo, settlementInfo);

        return new ResponseEntity<>(finalSettlement, HttpStatus.OK);
    }

    public ResponseEntity<?> generateFinalSettlement(String customerId, List<Settlement> deductions) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        BookingsV1 bookingDetails = bookingsService.getBookingsByCustomerId(customerId);
        if (bookingDetails == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        }
        else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        boolean isCurrentRentPaid = false;
        boolean isAdvancePaid = false;

        double bookingAmount = 0.0;
        double totalDeductions = 0.0;
        long noOfDaySatayed = 1;
        double currentMonthRent = 0.0;
        double currentRentPaid = 0.0;
        double currentMonthPayableRent = 0.0;
        double advancePaidAmount = 0.0;
        double unpaidInvoiceAmount = 0.0;
        double partialPaidAmount = 0.0;
        double totalAmountToBePaid = 0.0;


        if (bookingDetails.getBookingAmount() != null) {
            bookingAmount = bookingDetails.getBookingAmount();
        }

        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());

        if (customers.getAdvance() != null) {
            totalDeductions = customers.getAdvance()
                    .getDeductions()
                    .stream()
                    .mapToDouble(Deductions::getAmount)
                    .sum();
        }

        List<InvoicesV1> listUnpaidInvoices = invoiceService.listAllUnpaidInvoices(customerId, customers.getHostelId());

        List<InvoicesV1> listUnpaidRentalInvoices = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0)
                .toList();

        List<InvoicesV1> currentMonthInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) >= 0)
                .toList();

        Calendar calStartDate = Calendar.getInstance();
        calStartDate.setTime(billDate.currentBillStartDate());

        Calendar calEndDate = Calendar.getInstance();
        calEndDate.setTime(billDate.currentBillEndDate());

        Date dateStartDate = calStartDate.getTime();

        if (Utils.findNumberOfDays(billDate.currentBillStartDate(), bookingDetails.getJoiningDate()) >= 0) {
            dateStartDate =bookingDetails.getJoiningDate();
        }
        else {
            dateStartDate = billDate.currentBillStartDate();
        }

        long findNoOfDaysInCurrentMonth = Utils.findNumberOfDays(calStartDate.getTime(), calEndDate.getTime());

        noOfDaySatayed = Utils.findNumberOfDays(dateStartDate, new Date());

        //taken from unpaid invoices. So current month invoice is empty for paid
        if (!currentMonthInvoice.isEmpty()) {
            InvoicesV1 currentInvoice = currentMonthInvoice.get(0);
            currentMonthRent = currentInvoice.getTotalAmount();

            List<String> currentMonthInfo = new ArrayList<>();
            currentMonthInfo.add(currentInvoice.getInvoiceId());

            currentRentPaid = transactionService.getTransactionInfo(currentMonthInfo)
                    .stream()
                    .mapToDouble(PartialPaidInvoiceInfo::paidAmount)
                    .sum();
        }
        else {
            //current month invoice is paid
            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthInvoice(customerId);
            if (invoicesV1 != null) {
                currentMonthRent = invoicesV1.getTotalAmount();
                currentRentPaid = invoicesV1.getTotalAmount();
                isCurrentRentPaid = true;
            }
        }

        double rentPerDay = currentMonthRent / findNoOfDaysInCurrentMonth;
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) >= 0) {
            rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
        }
        currentMonthPayableRent = Math.round(noOfDaySatayed * rentPerDay)*100.0/100.0;

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                .toList();

        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            }
            else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            }
            else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }
        }
        else {
            InvoicesV1 invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
            Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
            if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = paidAmount;
            }
        }

        advancePaidAmount = advancePaidAmount + bookingAmount;


        List<String> partialPaymentInvoices = listUnpaidRentalInvoices
                .stream()
                .filter(invoicesV1 -> invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                .map(InvoicesV1::getInvoiceId)
                .toList();

        List<PartialPaidInvoiceInfo> lisPartialPayments = transactionService.getTransactionInfo(partialPaymentInvoices);

        partialPaidAmount = lisPartialPayments
                .stream()
                .mapToDouble(PartialPaidInvoiceInfo::paidAmount)
                .sum();
        unpaidInvoiceAmount = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) < 0)
                .mapToDouble(InvoicesV1::getTotalAmount)
                .sum();

        double invoiceBalance = unpaidInvoiceAmount - partialPaidAmount;

        totalAmountToBePaid =  invoiceBalance - advancePaidAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthPayableRent - currentRentPaid);
        }
        else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthPayableRent;
        }

        totalAmountToBePaid = totalAmountToBePaid + totalDeductions;

        if (deductions != null && !deductions.isEmpty()) {
            double finalDeductions = deductions
                    .stream()
                    .mapToDouble(Settlement::amount)
                    .sum();
            totalAmountToBePaid = totalAmountToBePaid - finalDeductions;
        }

        List<InvoicesV1> unpaidUpdated = listUnpaidRentalInvoices
                .stream()
                .peek(item -> item.setCancelled(true))
                .toList();

        totalAmountToBePaid = (totalAmountToBePaid * 100.0) / 100.0;

        invoiceService.cancelActiveInvoice(unpaidUpdated);
        invoiceService.createSettlementInvoice(customers, customers.getHostelId(), totalAmountToBePaid, unpaidUpdated);

        customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
        customersRepository.save(customers);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public boolean customerExist(String hostelId) {
        return customersRepository.existsByHostelIdAndCurrentStatusIn(hostelId, List.of(CustomerStatus.NOTICE.name(),CustomerStatus.CHECK_IN.name(),CustomerStatus.BOOKED.name()));
    }

    public List<CustomerBedsList> getCustomersFromBedHistory(String hostelId, Date billStartDate, Date billEndDate) {
        return bedHistory.getAllCustomerFromBedsHistory(hostelId, billStartDate, billEndDate);
    }

    public ResponseEntity<?> changeBed(String hostelId, String customerId, ChangeBed request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!bedsService.checkIsBedExsits(request.bedId(), user.getParentId(), hostelId)) {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingsV1 = bookingsService.findBookingsByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (bookingsV1.getBedId() == request.bedId()) {
            return new ResponseEntity<>(Utils.CHANGE_BED_SAME_BED_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (!bedsService.isBedAvailableForReassign(request.bedId(), request.joiningDate())) {
            return new ResponseEntity<>(Utils.BED_UNAVAILABLE_DATE, HttpStatus.BAD_REQUEST);
        }

        //check is it after or equal current billing cycle
        Date joiningDate = Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        double balanceAmount = invoiceService.calculateAndCreateInvoiceForReassign(customers, request.joiningDate(), request.rentAmount());

        bedsService.unassignBed(bookingsV1.getBedId());
        bedsService.reassignBed(customerId, request.bedId());

        BedRoomFloor bedRoomFloor = bedsService.findRoomAndFloorByBedIdAndHostelId(
                request.bedId(), hostelId
        );

        bookingsService.reassignBed(bedRoomFloor, bookingsV1, request);


        CustomerWallet wallet = customers.getWallet();
        if (wallet != null &&  wallet.getAmount() != null) {
            wallet.setAmount(wallet.getAmount() - balanceAmount);
            wallet.setTransactionDate(joiningDate);
        }
        else {
            if (wallet == null) {
                wallet = new CustomerWallet();
            }
            wallet.setAmount(balanceAmount);
            wallet.setTransactionDate(joiningDate);
        }
        wallet.setCustomers(customers);

        customers.setWallet(wallet);

        customersRepository.save(customers);


        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public void markCustomerCheckedOut(Customers customers) {
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.VACATED.name());
        customersRepository.save(customers);
    }
}
