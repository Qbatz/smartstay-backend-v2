package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.customers.FinalSettlementMapper;
import com.smartstay.smartstay.Wrappers.customers.TransctionsForCustomerDetails;
import com.smartstay.smartstay.Wrappers.invoices.UnpaidInvoicesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.BedRoomFloor;
import com.smartstay.smartstay.dto.customer.*;
import com.smartstay.smartstay.dto.customer.CustomerData;
import com.smartstay.smartstay.dto.customer.TransactionDto;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.ennum.InvoiceItems;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.beds.CancelCheckout;
import com.smartstay.smartstay.payloads.beds.ChangeBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.responses.customer.*;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.responses.customer.CheckoutCustomers;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private ReasonService reasonService;
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
    @Autowired
    private CustomerCredentialsService ccs;
    @Autowired
    private CustomersConfigService customersConfigService;

    private AmenityRequestService amenityRequestService;
    private AmenitiesService amenitiesService;
    @Autowired
    public void setAmenitiesService(@Lazy AmenitiesService amenitiesService) {
        this.amenitiesService = amenitiesService;
    }

    @Autowired
    public void setAmenityRequestService(@Lazy AmenityRequestService amenityRequestService) {
        this.amenityRequestService = amenityRequestService;
    }

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
        List<String> typeArray = new ArrayList<>();
        if (type == null || (type != null && type.trim().equalsIgnoreCase(""))) {
            typeArray.add(CustomerStatus.NOTICE.name());
            typeArray.add(CustomerStatus.CHECK_IN.name());
            typeArray.add(CustomerStatus.BOOKED.name());
            typeArray.add(CustomerStatus.SETTLEMENT_GENERATED.name());
        }
        else {
            typeArray.add(type.toUpperCase());
        }
        return customersRepository.getCustomerData(
                hostelId,
                name != null && !name.isBlank() ? name : null,
                typeArray
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
            StringBuilder fullName = new StringBuilder();
           if (item.getFirstName() != null) {
               initials.append(item.getFirstName().toUpperCase().charAt(0));
               fullName.append(item.getFirstName());
           }
           if (item.getLastName() != null && !item.getLastName().equalsIgnoreCase("")) {
               fullName.append(" ");
               fullName.append(item.getLastName());
               initials.append(item.getLastName().toUpperCase().charAt(0));

           }
           else {
               if (item.getFirstName().length() > 1) {
                   initials.append(item.getFirstName().toUpperCase().charAt(1));
               }
           }
            String currentStatus = null;
            if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
                currentStatus = "Booked";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                currentStatus = "Vacated";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                currentStatus = "Notice Period";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
                currentStatus = "Checked In";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
                currentStatus = "Inactive";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ACTIVE.name())) {
                currentStatus = "Active";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
                currentStatus = "Cancelled";
            } else if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                currentStatus = "Settlement Generated";
            }

            if (!filterOption.containsKey(currentStatus)) {
                filterOption.put(currentStatus, currentStatus);
            }

            return new com.smartstay.smartstay.responses.customer.CustomerData(item.getFirstName(),
                    item.getLastName(),
                    fullName.toString(),
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
     * <p>
     * Do not use anywhere else
     *
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
        if (bedsService.isBedAvailableNew(payloads.bedId(), user.getParentId(), payloads.joiningDate())) {
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
        } else {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }


    }

    /**
     * for check in the customers
     * for customers who are not booked
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

        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (!floorsService.checkFloorExistForHostel(payloads.floorId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_FLOOR_FOUND_HOSTEL, HttpStatus.UNAUTHORIZED);
        }

        if (!roomsService.checkRoomExistForFloor(payloads.floorId(), payloads.roomId())) {
            return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.UNAUTHORIZED);
        }

        if (!bedsService.checkBedExistForRoom(payloads.bedId(), payloads.roomId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.UNAUTHORIZED);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_CHECKED_IN, HttpStatus.BAD_REQUEST);
        }


        String date = payloads.joiningDate().replace("/", "-");
        if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) < 0) {
            return new ResponseEntity<>(Utils.CHECK_IN_FUTURE_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (bedsService.isBedAvailable(payloads.bedId(), user.getParentId(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT))) {


            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), joiningDate);
            BillingDates currentBillDate = hostelService.getCurrentBillStartAndEndDates(hostelV1.getHostelId());

            Advance advance = customers.getAdvance();
            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            } else {
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

            bookingsService.addCheckin(customers, payloads);
            customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);
            invoiceService.addInvoice(customerId, payloads.advanceAmount(), InvoiceType.ADVANCE.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates);
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.DAY_OF_MONTH, day);

//            Date startateOfCurrentCycle = cal.getTime();

            //checking joining date is fall under todays date
            if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
                return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
            }
            calculateRentAndCreateRentalInvoice(customers, payloads);
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

        } else {
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

        Date joiningDate = Utils.stringToDate(checkinRequest.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        if (Utils.compareWithTwoDates(joiningDate, booking.getBookingDate()) < 0) {
            return new ResponseEntity<>(Utils.JOINING_DATE_CANNOT_BEFORE_BOOKING, HttpStatus.BAD_REQUEST);
        }

        if (bedsService.checkAvailabilityForCheckIn(booking.getBedId(), joiningDate) != null) {

            customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());

            customers.setJoiningDate(Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT));

            Advance advance = customers.getAdvance();

            List<Deductions> listDeductions = null;
            if (advance == null) {
                advance = new Advance();
                listDeductions = new ArrayList<>();
            } else {
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
                    booking.getFloorId(),
                    booking.getBedId(),
                    booking.getRoomId(),
                    checkinRequest.joiningDate(),
                    checkinRequest.advanceAmount(),
                    checkinRequest.rentalAmount(),
                    checkinRequest.stayType(),
                    checkinRequest.deductions()
            );

            bookingsService.checkInBookedCustomer(customers, request);

            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), joiningDate);
            BillingDates currentBillDate = hostelService.getCurrentBillStartAndEndDates(hostelV1.getHostelId());

//            Calendar calendar = Calendar.getInstance();
//            int dueDate = calendar.get(Calendar.DAY_OF_MONTH) + 5;
//
//            int day = 1;
//            if (hostelV1.getElectricityConfig() != null) {
//                day = hostelV1.getElectricityConfig().getBillDate();
//            }

            invoiceService.addInvoice(customerId, checkinRequest.advanceAmount(), InvoiceType.ADVANCE.name(), booking.getHostelId(), customers.getMobile(), customers.getEmailId(), date, billingDates);

            bedsService.addUserToBed(booking.getBedId(), date);
            customersConfigService.addToConfiguration(customerId, hostelV1.getHostelId(), joiningDate);

            //check joining date is in this current cycle.
            if (Utils.compareWithTwoDates(joiningDate, currentBillDate.currentBillStartDate()) < 0) {
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

            if (customerInfo.emailId() != null && !customerInfo.emailId().isEmpty() && customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(customerInfo.emailId(), hostelId, List.of("VACATED"))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }

            if (customerInfo.mobileNumber() != null && !customerInfo.mobileNumber().isEmpty() && customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(customerInfo.mobileNumber(), hostelId, List.of("VACATED"))) {
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

            CustomerCredentials customerCredentials = ccs.addCustomerCredentials(customerInfo.mobileNumber());
            if (customerCredentials != null) {
                customers.setXuid(customerCredentials.getXuid());
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


        CustomerCredentials customerCredentials = ccs.addCustomerCredentials(customerInfo.mobile());
        if (customerCredentials != null) {
            customers.setXuid(customerCredentials.getXuid());
        }
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
//            if (updateInfo.mobile() != null && !updateInfo.mobile().equalsIgnoreCase("")) {
//                if (customersRepository.findCustomersByMobile(customers.getCustomerId(), updateInfo.mobile()) > 0) {
//                    return new ResponseEntity<>(Utils.MOBILE_NO_EXISTS, HttpStatus.BAD_REQUEST);
//                }
//                customers.setMobile(updateInfo.mobile());
//            }

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

        } else {
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
        if (Utils.compareWithTwoDates(requestDate, joiningDate) < 0) {
            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(checkoutDate, joiningDate) < 0) {
            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (Utils.compareWithTwoDates(checkoutDate, billingDates.currentBillStartDate()) < 0) {
            return new ResponseEntity<>(Utils.REQUEST_DATE_MUST_AFTER_BILLING_START_DATE + Utils.dateToString(billingDates.currentBillStartDate()), HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(checkoutDate, requestDate) < 0) {
            return new ResponseEntity<>(Utils.CHECKOUT_DATE_MUST_AFTER_REQUEST_DATE, HttpStatus.BAD_REQUEST);
        }

        customers.setCurrentStatus(CustomerStatus.NOTICE.name());


        bedsService.updateBedToNotice(bookingsService.getBedIdFromBooking(customers.getCustomerId(), hostelId), checkoutNotice.checkoutDate());
        bookingsService.moveToNotice(customers.getCustomerId(), checkoutNotice.checkoutDate(), checkoutNotice.requestDate(), checkoutNotice.reason());
        customersRepository.save(customers);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }


    public static AdvanceInfo toAdvanceInfoResponse(Advance advance, InvoiceResponse invoicesV1, double bookingAmount) {
        if (advance == null) return null;
        double maintenanceAmount = 0.0;
        double otherDeductionsAmount = 0.0;
        double invoicePaidAmount = 0.0;
        String paymentStatus = null;
        double paidAmount = 0.0;
        String dueDate = null;
        if (invoicesV1 != null) {
            invoicePaidAmount = invoicesV1.paidAmount();
            dueDate = invoicesV1.dueDate();
            paymentStatus = invoicesV1.paymentStatus();
            paidAmount = invoicesV1.paidAmount();
        }

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
                ? advance.getAdvanceAmount() - invoicePaidAmount
                : 0.0;

        return new AdvanceInfo(
                advance.getInvoiceDate() != null ? Utils.dateToString(advance.getInvoiceDate()) : null,
                dueDate,
                dueAmount,
                advance.getAdvanceAmount(),
                bookingAmount,
                paymentStatus,
                maintenanceAmount,
                otherDeductionsAmount,
                paidAmount
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
        } else {
            initials.append(customers.getFirstName().toUpperCase().charAt(1));
        }
        String fullName = customers.getFirstName() + " " + customers.getLastName();

        CustomersBookingDetails bookingDetails = bookingsService.getCustomerBookingDetails(customers.getCustomerId());
        List<InvoiceResponse> invoiceResponseList = invoiceService.getInvoiceResponseList(customers.getCustomerId());
        InvoiceResponse advanceInvoice = invoiceResponseList.stream()
                .filter(inv -> "ADVANCE".equalsIgnoreCase(inv.invoiceType()))
                .limit(1)
                .findFirst()
                .orElse(null);

        HostelInformation hostelInformation = null;
        BookingInfo bookingInfo = null;
        Advance advance = customers.getAdvance();
        List<Deductions> listDeduction = null;
        AdvanceInfo advanceInfo = null;
        List<Deductions> otherDeductionBreakup = null;
        String bookingId = null;
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
            bookingId = bookingDetails.getBookingId();
            advanceInfo = toAdvanceInfoResponse(advance, advanceInvoice, bookingDetails.getBookingAmount());

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

            if (bookingDetails.getIsBooked() != null && bookingDetails.getIsBooked()) {
                BookingsV1 bookingV1 = bookingsService.getBookingsByCustomerId(customerId);
                if (bookingV1 != null) {

                    CustomersBedHistory cbh = bedHistory.getCustomerBookedBed(bookingV1.getCustomerId());

                    BedDetails bedDetails = bedsService.getBedDetails(cbh.getBedId());
                    String bookedBedName = null;
                    String bookedFloorName = null;
                    String bookedRoomName = null;

                    if (bedDetails != null) {
                        bookedBedName = bedDetails.getBedName();
                        bookedFloorName = bedDetails.getFloorName();
                        bookedRoomName = bedDetails.getRoomName();
                    }

                    bookingInfo = new BookingInfo(Utils.dateToString(bookingV1.getBookingDate()),
                            bookingV1.getBookingAmount(),
                            bookedBedName,
                            bookedFloorName,
                            bookedRoomName);
                }
            }

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
        } else {
            kycInfo = new KycInformations(kycDetails.getCurrentStatus(),
                    null,
                    null,
                    null);
        }

        CheckoutInfo checkoutInfo = null;
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getCheckoutDate());
            checkoutInfo = new CheckoutInfo(Utils.dateToString(bookingDetails.getCheckoutDate()),
                    Utils.dateToString(bookingDetails.getRequestedCheckoutDate()),
                    Utils.dateToString(bookingDetails.getNoticeDate()),
                    noticeDays,
                    null);
        }
        else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getRequestedCheckoutDate());
            checkoutInfo = new CheckoutInfo(null,
                    Utils.dateToString(bookingDetails.getRequestedCheckoutDate()),
                    Utils.dateToString(bookingDetails.getNoticeDate()),
                    noticeDays,
                    null);
        }
        else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            assert bookingDetails != null;
            long noticeDays = Utils.findNumberOfDays(bookingDetails.getNoticeDate(), bookingDetails.getRequestedCheckoutDate());
            checkoutInfo = new CheckoutInfo(null,
                    Utils.dateToString(bookingDetails.getRequestedCheckoutDate()),
                    Utils.dateToString(bookingDetails.getNoticeDate()),
                    noticeDays,
                    null);
        }


        List<BedHistory> listBeds = bedHistory.getCustomersBedHistory(customers.getCustomerId());
        List<Amenities> amenities = amenitiesService.getAmenitiesByCustomerId(customerId);
        List<TransactionDto> listTransactions = transactionService.getTranactionInfoByCustomerId(customerId);
        List<AmenityRequestDTO> listRequestedAmenities = amenityRequestService.getRequestedAmenities(customerId, customers.getHostelId());

        List<String> invoicesIds = listTransactions
                .stream()
                .map(TransactionDto::invoiceId)
                .toList();
        Set<String> bankIds = listTransactions
                .stream()
                .map(TransactionDto::bankId)
                .collect(Collectors.toSet());
        List<InvoicesV1> listOfInvoices = invoiceService.findInvoices(invoicesIds);
        List<BankingV1> listOFBankings = bankingService.findAllBanksById(bankIds);
        List<String> userIds = listOFBankings.stream()
                .map(BankingV1::getUserId)
                .toList();
        List<Users> listUsers = userService.findAllUsersFromUserId(userIds);


        List<com.smartstay.smartstay.responses.customer.TransactionDto> listTransactionResponse = listTransactions
                .stream()
                .map(i -> new TransctionsForCustomerDetails(listOfInvoices, listOFBankings, listUsers).apply(i))
                .toList();


        CustomerDetails details = new CustomerDetails(customers.getCustomerId(),
                customers.getHostelId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName,
                customers.getEmailId(),
                customers.getMobile(),
                "91",
                initials.toString(),
                customers.getProfilePic(),
                bookingId,
                customers.getCurrentStatus(),
                address,
                hostelInformation,
                kycInfo,
                advanceInfo,
                checkoutInfo,
                bookingInfo,
                invoiceResponseList,
                listBeds,
                listTransactionResponse,
                amenities,
                listRequestedAmenities);

        return new ResponseEntity<>(details, HttpStatus.OK);
    }


    public void calculateRentAndCreateRentalInvoice(Customers customers, CheckInRequest payloads) {
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 != null) {

//            int lastRulingBillDate = 1;
//            if (!hostelV1.getBillingRulesList().isEmpty()) {
//                lastRulingBillDate  = hostelV1.getBillingRulesList().get(0).getBillingStartDate();
//            }


            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

            BillingDates billingDates = hostelService.getBillingRuleOnDate(customers.getHostelId(), joiningDate);

//            Date lastDate = null;
//            Date startDate = null;
//            if (billingDates != null) {
//                lastDate = billingDates.currentBillEndDate();
//                startDate = billingDates.currentBillStartDate();
//            } else {
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.DAY_OF_MONTH, 1);
//                startDate = calendar.getTime();
//                lastDate = Utils.findLastDate(1, new Date());
//            }


            Calendar c = Calendar.getInstance();
            c.setTime(joiningDate);


            long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
            long noOfDaysLeftInCurrentMonth = Utils.findNumberOfDays(c.getTime(), billingDates.currentBillEndDate());
            double calculateRentPerDay = payloads.rentalAmount() / noOfDaysInCurrentMonth;
            double finalRent = Math.round(calculateRentPerDay * noOfDaysLeftInCurrentMonth);
            if (finalRent > payloads.rentalAmount()) {
                finalRent = payloads.rentalAmount();
            }

            invoiceService.addInvoice(customers.getCustomerId(), finalRent, InvoiceType.RENT.name(), customers.getHostelId(), customers.getMobile(), customers.getEmailId(), payloads.joiningDate(), billingDates);

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
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
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
        } else {
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
        } else {
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
                    .mapToDouble(i -> {
                        if (i.paidAmount() == null) {
                            return 0.0;
                        }
                        return i.paidAmount();
                    })
                    .sum();
            if (currentRentPaid > 0) {
                isCurrentRentPaid = true;
            }
        } else {
            //current month invoice is paid Rent
            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthRentInvoice(customerId);
            if (invoicesV1 != null) {
                currentMonthRent = invoicesV1.getTotalAmount();
                if (invoicesV1.getPaidAmount() == null) {
                    currentRentPaid = 0;
                }
                else {
                    currentRentPaid = invoicesV1.getPaidAmount();
                }
                isCurrentRentPaid = true;
            }
        }

        List<CustomersBedHistory> listBedHistory = bedHistory.getCustomersBedHistory(customerId, billStartDate, billDate.currentBillEndDate());


        double rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
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

        currentMonthPayableRent = noOfDaySatayed * rentPerDay;

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                .toList();

        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }
        } else {
            InvoicesV1 invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
            if (invAdvanceInvoice != null) {
                Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
                if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                    isAdvancePaid = true;
                    advancePaidAmount = paidAmount;
                }
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

        totalAmountToBePaid = invoiceBalance - advancePaidAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthPayableRent - currentRentPaid);
        } else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthPayableRent;
        }

        totalAmountToBePaid =  totalAmountToBePaid + totalDeductions;

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

        List<RentBreakUp> rentBreakUpList = new ArrayList<>();

        RentInfo rentInfo = new RentInfo((double) Math.round(currentMonthPayableRent),
                (double) Math.round(currentRentPaid),
                (int) noOfDaySatayed,
                currentMonthRent,
                Utils.roundOffDecimal(rentPerDay),
                Utils.dateToString(calStartDate.getTime()),
                Utils.dateToString(calEndDate.getTime()),
                rentBreakUpList);

        if (totalAmountToBePaid < 0) {
            isRefundable = true;
        }

        SettlementInfo settlementInfo = new SettlementInfo((double)Math.round(totalAmountToBePaid),
                totalDeductions,
                unpaidInvoiceAmount,
                0.0,
                0.0,
                isRefundable);


        FinalSettlement finalSettlement = new FinalSettlement(customerInformations, stayInfo, unpaidInvoices, rentInfo, settlementInfo);

        return new ResponseEntity<>(finalSettlement, HttpStatus.OK);
    }

    public ResponseEntity<?> getInformationForFinalSettlementNew(String customerId) {
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
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());
        CustomersBedHistory cbh = bedHistory.getLatestCustomerBed(customerId);

        if (Utils.compareWithTwoDates(cbh.getStartDate(), billDate.currentBillStartDate()) > 0) {
           return calculateFinalSettlemtForBedChange(customers, bookingDetails, billDate);
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
        } else {
            if (customers.getFirstName().length() > 1) {
                initials.append(customers.getFirstName().toUpperCase().charAt(1));
            }
        }



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
                .filter(item -> (item.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || item.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) && Utils.compareWithTwoDates(item.getInvoiceStartDate(), billDate.currentBillStartDate()) >= 0)
                .toList();

        Calendar calStartDate = Calendar.getInstance();
        calStartDate.setTime(billDate.currentBillStartDate());

        Date billStartDate = null;
        Calendar calBillStartDate = Calendar.getInstance();

        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) < 0) {
            calBillStartDate.setTime(billDate.currentBillStartDate());
        } else {
            calBillStartDate.setTime(bookingDetails.getJoiningDate());
        }

        billStartDate = calBillStartDate.getTime();


        Calendar calEndDate = Calendar.getInstance();
        calEndDate.setTime(billDate.currentBillEndDate());

        Long findNoOfDaysInCurrentMonth = Utils.findNumberOfDays(calStartDate.getTime(), calEndDate.getTime());

        noOfDaySatayed = Utils.findNumberOfDays(billStartDate, new Date());

        //taken from unpaid invoices. So current month invoice is empty for paid
        if (!currentMonthInvoice.isEmpty()) {
//            List<> currentInvoice = currentMonthInvoice.get(0);
            currentMonthRent = currentMonthInvoice
                    .stream()
                    .mapToDouble(InvoicesV1::getTotalAmount)
                    .sum();

            List<String> currentMonthInfo = currentMonthInvoice
                    .stream()
                    .map(InvoicesV1::getInvoiceId)
                    .toList();

            currentRentPaid = transactionService.getTransactionInfo(currentMonthInfo)
                    .stream()
                    .mapToDouble(i -> {
                        if (i.paidAmount() == null) {
                            return 0.0;
                        }
                        return i.paidAmount();
                    })
                    .sum();
            if (currentRentPaid > 0) {
                isCurrentRentPaid = true;
            }
        } else {
            //current month invoice is paid Rent
            List<InvoicesV1> listCurrentInvoicesPaid = invoiceService.getAllCurrentMonthRentInvoices(customerId);

//            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthRentInvoice(customerId);
            if (listCurrentInvoicesPaid != null) {
                currentMonthRent = listCurrentInvoicesPaid
                        .stream()
                        .mapToDouble(InvoicesV1::getTotalAmount)
                        .sum();

                currentRentPaid = listCurrentInvoicesPaid
                        .stream()
                        .mapToDouble(InvoicesV1::getTotalAmount)
                        .sum();
                isCurrentRentPaid = true;
            }
        }


        double rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
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

        currentMonthPayableRent = noOfDaySatayed * rentPerDay;

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                .toList();

        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }
        } else {
            InvoicesV1 invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
            if (invAdvanceInvoice != null) {
                Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
                if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                    isAdvancePaid = true;
                    advancePaidAmount = paidAmount;
                }
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

        totalAmountToBePaid = invoiceBalance - advancePaidAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthPayableRent - currentRentPaid);
        } else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthPayableRent;
        }

        totalAmountToBePaid =  totalAmountToBePaid + totalDeductions;

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

        String bedName = null;
        String floorName = null;
        String roomName = null;
        if (cbh != null) {
            BedDetails bedDetails = bedsService.getBedDetails(cbh.getBedId());
            if (bedDetails != null) {
                bedName = bedDetails.getBedName();
                floorName = bedDetails.getFloorName();
                roomName = bedDetails.getRoomName();
            }

        }
        List<RentBreakUp> rentBreakUpList = new ArrayList<>();
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) <= 0) {
            RentBreakUp rentBreakUp = new RentBreakUp(Utils.dateToString(billDate.currentBillStartDate()),
                    Utils.dateToString(new Date()),
                    Utils.findNumberOfDays(billDate.currentBillStartDate(), new Date()),
                    (double) Math.round(currentMonthPayableRent),
                    bedName,
                    roomName,
                    floorName);
            rentBreakUpList.add(rentBreakUp);
        }
        else {
            RentBreakUp rentBreakUp = new RentBreakUp(Utils.dateToString(bookingDetails.getJoiningDate()),
                    Utils.dateToString(new Date()),
                    Utils.findNumberOfDays(bookingDetails.getJoiningDate(), new Date()),
                    (double) Math.round(currentMonthPayableRent),
                    bedName,
                    roomName,
                    floorName);
            rentBreakUpList.add(rentBreakUp);
        }

        RentInfo rentInfo = new RentInfo((double) Math.round(currentMonthPayableRent),
                (double) Math.round(currentRentPaid),
                (int) noOfDaySatayed,
                currentMonthRent,
                Utils.roundOffDecimal(rentPerDay),
                Utils.dateToString(calStartDate.getTime()),
                Utils.dateToString(calEndDate.getTime()),
                rentBreakUpList);

        if (totalAmountToBePaid < 0) {
            isRefundable = true;
        }

        SettlementInfo settlementInfo = new SettlementInfo((double)Math.round(totalAmountToBePaid),
                totalDeductions,
                unpaidInvoiceAmount,
                0.0,
                0.0,
                isRefundable);


        FinalSettlement finalSettlement = new FinalSettlement(customerInformations, stayInfo, unpaidInvoices, rentInfo, settlementInfo);

        return new ResponseEntity<>(finalSettlement, HttpStatus.OK);
    }

    private ResponseEntity<?> calculateFinalSettlemtForBedChange(Customers customers, BookingsV1 bookingDetails, BillingDates billDate) {
        double bookingAmount = 0.0;
        if (bookingDetails.getBookingAmount() != null) {
            bookingAmount = bookingDetails.getBookingAmount();
        }

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();

        boolean isAdvancePaid = false;
        double totalDeductions = 0.0;
        double advancePaidAmount = 0.0;
        double currentMonthPaidRent = 0.0;
        boolean isRefundable = false;
        double totalAmountToBePaid = 0.0;
        double totalCurrentMonthRent = 0.0;
        double totalCurrentMonthRentPaid = 0.0;
        double refundableRent = 0.0;

        if (customers.getFirstName() != null) {
            fullName.append(customers.getFirstName());
            initials.append(customers.getFirstName().toUpperCase().charAt(0));
        }
        if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(customers.getLastName());
            initials.append(customers.getLastName().toUpperCase().charAt(0));
        } else {
            if (customers.getFirstName().length() > 1) {
                initials.append(customers.getFirstName().toUpperCase().charAt(1));
            }
        }

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

        List<InvoicesV1> listUnpaidInvoices = invoiceService.listAllOldUnpaidInvoices(customers.getCustomerId(), customers.getHostelId());

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());

        if (advanceInvoice != null) {
            //there is a chance of partial payment
            if (advanceInvoice.getPaidAmount() != null) {
                isAdvancePaid = true;
                advancePaidAmount = advanceInvoice.getPaidAmount();
            }
            else {
                if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                    advancePaidAmount = 0.0;
                }
            }
        }


        double oldTotalInvoiceAmount = listUnpaidInvoices
                .stream()
                .mapToDouble(InvoicesV1::getTotalAmount)
                .sum();
        double oldInvoicesPaidAmounts = listUnpaidInvoices
                .stream()
                .mapToDouble(InvoicesV1::getPaidAmount)
                .sum();
        double oldInvoiceBalanceAmount = oldTotalInvoiceAmount - oldInvoicesPaidAmounts;

        Date currentMonthInvoiceStartDate = bookingDetails.getJoiningDate();
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) <= 0) {
            currentMonthInvoiceStartDate = billDate.currentBillStartDate();
        }

        List<InvoicesV1> currentMonthInvoices = invoiceService.findAllCurrentMonthRentalInvoice(customers.getCustomerId(),customers.getHostelId(), billDate.currentBillStartDate());

        InvoicesV1 findLatestInvoice = invoiceService.findRunningInvoice(customers.getCustomerId(), billDate);


        List<InvoicesV1> currentMonthInvoicesBeforeBedChange = currentMonthInvoices
                .stream()
                .filter(i -> !i.getInvoiceId().equalsIgnoreCase(findLatestInvoice.getInvoiceId()))
                .toList();

        double currentMonthRentBeforeChangingBed = currentMonthInvoicesBeforeBedChange
                .stream()
                .mapToDouble(InvoicesV1::getTotalAmount)
                .sum();
        double currentMonthPaidAmountBeforeChangingBed = currentMonthInvoicesBeforeBedChange
                .stream()
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    return 0.0;
                })
                .sum();

        double currentRunningInvoicePaidAmount = 0.0;
        if (findLatestInvoice != null && findLatestInvoice.getPaidAmount() != null) {
            currentRunningInvoicePaidAmount = findLatestInvoice.getPaidAmount();
        }

        currentMonthPaidRent = currentMonthPaidAmountBeforeChangingBed + currentRunningInvoicePaidAmount;


        List<String> partialPaymentInvoices = listUnpaidInvoices
                .stream()
                .filter(invoicesV1 -> invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                .map(InvoicesV1::getInvoiceId)
                .toList();

        List<PartialPaidInvoiceInfo> lisPartialPayments = transactionService.getTransactionInfo(partialPaymentInvoices);

        List<UnpaidInvoices> unpaidInvoices = listUnpaidInvoices
                .stream()
                .map(item -> new UnpaidInvoicesMapper(lisPartialPayments).apply(item))
                .toList();

        long totalNoOfDaysStayedIncludeOldAndNewBed = Utils.findNumberOfDays(currentMonthInvoiceStartDate, new Date());
        long totalNoOfDaysInCurrentMonth = Utils.findNumberOfDays(currentMonthInvoiceStartDate, billDate.currentBillEndDate());
        long totalNoOfDaysAfterChangingBed = Utils.findNumberOfDays(findLatestInvoice.getInvoiceStartDate(), billDate.currentBillEndDate());
        long findNoOfDaysStayedInNewBedAsOfToday = Utils.findNumberOfDays(findLatestInvoice.getInvoiceStartDate(), new Date());

        double newRentPerDay = findLatestInvoice.getTotalAmount() / totalNoOfDaysAfterChangingBed;
        double payableRentAsOfToday = newRentPerDay * findNoOfDaysStayedInNewBedAsOfToday;

        double totalRentIncludePreviousBed = payableRentAsOfToday + currentMonthRentBeforeChangingBed;
        refundableRent = totalRentIncludePreviousBed - currentMonthPaidRent;
        if (refundableRent > 0) {
            refundableRent = 0.0;
        }
        else {
            refundableRent = -1 * refundableRent;
        }


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

        List<CustomersBedHistory> listCustomerBedHistories = bedHistory.getByCustomerIdAndStartAndEndDate(customers.getCustomerId(), billDate.currentBillStartDate(), billDate.currentBillEndDate());
        List<Integer> bedIds = listCustomerBedHistories
                .stream()
                .map(CustomersBedHistory::getBedId)
                .toList();
        List<BedDetails> listBedDetails = bedsService.getBedDetails(bedIds);

        List<RentBreakUp> breakUpList = new ArrayList<>(currentMonthInvoicesBeforeBedChange
                .stream()
                .map(i -> new FinalSettlementMapper(listCustomerBedHistories, listBedDetails).apply(i))
                .toList());
        CustomersBedHistory runningBed = listCustomerBedHistories
                .stream()
                .filter(i -> Utils.compareWithTwoDates(i.getStartDate(), findLatestInvoice.getInvoiceStartDate()) <= 0 && i.getEndDate() == null)
                .findFirst()
                .orElse(null);
        String bedName = null;
        String floorName = null;
        String roomName = null;
        if (runningBed != null) {
            BedDetails details = listBedDetails
                    .stream()
                    .filter(i -> i.getBedId().equals(runningBed.getBedId()))
                    .findFirst()
                    .orElse(null);
            if (details != null) {
                bedName = details.getBedName();
                roomName = details.getRoomName();
                floorName = details.getFloorName();
            }
        }
        RentBreakUp rentBreakUpForNewInvoice = new RentBreakUp(Utils.dateToString(findLatestInvoice.getInvoiceStartDate()),
                Utils.dateToString(new Date()),
                findNoOfDaysStayedInNewBedAsOfToday,
                Utils.roundOfDouble(Math.round(payableRentAsOfToday)),
                bedName,
                roomName,
                floorName);

        if (breakUpList != null) {
            breakUpList.add(rentBreakUpForNewInvoice);
        }


        RentInfo rentInfo = new RentInfo((double) Math.round(totalRentIncludePreviousBed),
                (double) Math.round(currentMonthPaidRent),
                (int) totalNoOfDaysStayedIncludeOldAndNewBed,
                findLatestInvoice.getTotalAmount(),
                Utils.roundOffDecimal(newRentPerDay),
                Utils.dateToString(currentMonthInvoiceStartDate),
                Utils.dateToString(billDate.currentBillEndDate()),
                breakUpList);

        totalAmountToBePaid = oldInvoiceBalanceAmount - advancePaidAmount;
        totalAmountToBePaid = (totalRentIncludePreviousBed + totalAmountToBePaid) - currentMonthPaidRent;
        if (totalDeductions > 0) {
            totalAmountToBePaid = totalAmountToBePaid + totalDeductions;
        }

        if (totalAmountToBePaid < 0) {
            isRefundable = true;
        }

        SettlementInfo settlementInfo = new SettlementInfo((double)Math.round(totalAmountToBePaid),
                totalDeductions,
                oldInvoiceBalanceAmount,
                refundableRent,
                0.0,
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
//        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
//            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_GENERATED, HttpStatus.BAD_REQUEST);
//        }
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
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.TERMINATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_ERROR, HttpStatus.BAD_REQUEST);
        } else if (bookingDetails.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_CHECKED_NOT_IN_NOTICE, HttpStatus.BAD_REQUEST);
        }

        BillingDates billDate = hostelService.getCurrentBillStartAndEndDates(customers.getHostelId());
        CustomersBedHistory cbh = bedHistory.getLatestCustomerBed(customerId);

        if (Utils.compareWithTwoDates(cbh.getStartDate(), billDate.currentBillStartDate()) > 0) {
            return calculateAndGenerateFinalSettlemtForBedChange(customers, bookingDetails, billDate, cbh, deductions);
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
        double totalAmountWithoutDeductions = 0.0;
        List<Deductions> listDeductions = new ArrayList<>();


        if (bookingDetails.getBookingAmount() != null) {
            bookingAmount = bookingDetails.getBookingAmount();
        }

        if (customers.getAdvance() != null) {
            totalDeductions = customers.getAdvance()
                    .getDeductions()
                    .stream()
                    .mapToDouble(Deductions::getAmount)
                    .sum();

            listDeductions = customers.getAdvance().getDeductions();
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
            dateStartDate = bookingDetails.getJoiningDate();
        } else {
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
            if (currentRentPaid > 0) {
                isCurrentRentPaid = true;
            }
        } else {
            //current month invoice is paid rent
            InvoicesV1 invoicesV1 = invoiceService.getCurrentMonthRentInvoice(customerId);
            if (invoicesV1 != null) {
                currentMonthRent = invoicesV1.getTotalAmount();
                if (invoicesV1.getPaidAmount() == null) {
                    currentRentPaid = 0.0;
                }
                else {
                    currentRentPaid = invoicesV1.getPaidAmount();
                }

                isCurrentRentPaid = true;
            }
        }


        double rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
        if (Utils.compareWithTwoDates(bookingDetails.getJoiningDate(), billDate.currentBillStartDate()) >= 0) {
            rentPerDay = bookingDetails.getRentAmount() / findNoOfDaysInCurrentMonth;
        }
        currentMonthPayableRent = Math.round(noOfDaySatayed * rentPerDay);

        List<InvoicesV1> advanceInvoice = listUnpaidInvoices
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                .toList();
        InvoicesV1 invAdvanceInvoice = null;

        if (!advanceInvoice.isEmpty()) {
            InvoicesV1 advInv = advanceInvoice.get(0);
            invAdvanceInvoice = advanceInvoice.get(0);
            if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                isAdvancePaid = false;
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = false;
                advancePaidAmount = transactionService.getAdvancePaidAmount(advInv.getInvoiceId());
            } else if (advInv.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advInv.getTotalAmount();
            }
        } else {
            invAdvanceInvoice = invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId());
//            advanceInvoice = new ArrayList<>();
//            advanceInvoice.add(invoiceService.getAdvanceInvoiceDetails(customerId, customers.getHostelId()));
            if (invAdvanceInvoice != null) {
                Double paidAmount = transactionService.getAdvancePaidAmount(invAdvanceInvoice.getInvoiceId());
                if (paidAmount > 0 && invAdvanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                    isAdvancePaid = true;
                    advancePaidAmount = paidAmount;
                }
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

        totalAmountToBePaid = invoiceBalance - advancePaidAmount;

        if (isCurrentRentPaid) {
            totalAmountToBePaid = totalAmountToBePaid + (currentMonthPayableRent - currentRentPaid);
        } else {
            totalAmountToBePaid = totalAmountToBePaid + currentMonthPayableRent;
        }

        totalAmountWithoutDeductions = totalAmountToBePaid;
        totalAmountToBePaid = totalAmountToBePaid + totalDeductions;
        double totalAmountForFinalSettlement = totalAmountToBePaid;
        double totalDeductionForFinalSettlement = 0.0;

        if (deductions != null && !deductions.isEmpty()) {
            double finalDeductions = deductions
                    .stream()
                    .mapToDouble(Settlement::amount)
                    .sum();
            List<Deductions> newDeductions = deductions
                    .stream()
                    .map(i -> {
                        Deductions d = new Deductions();
                        d.setType(i.item());
                        d.setAmount(i.amount());
                        return d;
                    })
                    .toList();
            listDeductions.addAll(newDeductions);

            totalAmountToBePaid = totalAmountToBePaid + finalDeductions;
            totalDeductionForFinalSettlement = finalDeductions;
        }


        List<InvoicesV1> unpaidUpdated = listUnpaidInvoices
                .stream()
                .peek(item -> item.setCancelled(true))
                .toList();


        totalAmountToBePaid = Math.round(totalAmountToBePaid);

        invoiceService.cancelActiveInvoice(unpaidUpdated);
        if (invAdvanceInvoice != null) {
            invoiceService.createSettlementInvoice(customers, customers.getHostelId(), totalAmountToBePaid, unpaidUpdated, listDeductions, totalAmountWithoutDeductions);

            customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
            customersRepository.save(customers);
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> calculateAndGenerateFinalSettlemtForBedChange(Customers customers, BookingsV1 bookings, BillingDates billingDates, CustomersBedHistory latestBed, List<Settlement> deductions) {
        Double advanceAmount = 0.0;
        Double bookingAmount = 0.0;
        Double advancePaidAmount = 0.0;
        Double deductionsAmount = 0.0;
        Double unpaidInvoicesAmount = 0.0;
        Double currentMonthTotalAmount = 0.0;
        Double currentMonthPaidAmount = 0.0;
        double totalAdvacePaidAmount = 0.0;
        double currentPayableRent = 0.0;
        double totalAmountToBePaid = 0.0;
        Double totalAmountWithoutDeductions = 0.0;
        List<Deductions> listDeductions = new ArrayList<>();

        boolean isAdvancePaid = false;

        if (customers.getAdvance() != null) {
            Advance advance = customers.getAdvance();
            if (advance != null) {
                advanceAmount = advance.getAdvanceAmount();
                deductionsAmount = advance
                        .getDeductions()
                        .stream()
                        .mapToDouble(i-> {
                            if (i.getAmount() != null) {
                                return i.getAmount();
                            }
                            return 0.0;
                        })
                        .sum();

                listDeductions.addAll(advance.getDeductions());
            }
        }

        if (deductions != null && !deductions.isEmpty()) {
            double ded = deductions.stream()
                    .mapToDouble(i -> {
                        if (i.amount() != null) {
                            return i.amount();
                        }
                        return 0.0;
                    })
                    .sum();
            deductionsAmount = deductionsAmount + ded;

            List<Deductions> newDeductions = deductions
                    .stream()
                    .map(i -> {
                        Deductions d = new Deductions();
                        d.setAmount(i.amount());
                        d.setType(i.item());
                        return d;
                    })
                    .toList();

            listDeductions.addAll(newDeductions);
        }

        if (bookings != null) {
            if (bookings.getBookingAmount() != null) {
                bookingAmount = bookings.getBookingAmount();
            }
        }

        InvoicesV1 advaceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), customers.getHostelId());
        if (advaceInvoice != null) {
            advanceAmount = advaceInvoice.getTotalAmount();
            if (advaceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advaceInvoice.getPaidAmount();
            }
            else if (advaceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                isAdvancePaid = true;
                advancePaidAmount = advaceInvoice.getPaidAmount();
            }
        }

        List<InvoicesV1> listAllUnpaidInvoices = invoiceService.listAllOldUnpaidInvoices(customers.getCustomerId(), customers.getHostelId());
        if (listAllUnpaidInvoices != null && !listAllUnpaidInvoices.isEmpty()) {
            unpaidInvoicesAmount = listAllUnpaidInvoices
                    .stream()
                    .mapToDouble(i -> {
                        double paidAmount = 0.0;
                        double totalAmount = 0.0;
                        if (i.getPaidAmount() != null) {
                            paidAmount = i.getPaidAmount();
                        }
                        if (i.getTotalAmount() != null) {
                            totalAmount = i.getTotalAmount();
                        }

                        return totalAmount - paidAmount;
                    })
                    .sum();
        }

        InvoicesV1 currentRunningInvoice = invoiceService.findRunningInvoice(customers.getCustomerId(), billingDates);
        List<InvoicesV1> currentMonthInvoices = invoiceService.getAllCurrentMonthRentInvoices(customers.getCustomerId())
                .stream()
                .filter(i -> !i.getInvoiceId().equalsIgnoreCase(currentRunningInvoice.getInvoiceId()))
                .toList();

        if (currentRunningInvoice != null && currentRunningInvoice.getTotalAmount() != null) {
            currentMonthTotalAmount = currentMonthTotalAmount + currentRunningInvoice.getTotalAmount();
        }
        if (currentRunningInvoice != null && currentRunningInvoice.getPaidAmount() != null) {
            currentMonthPaidAmount = currentMonthPaidAmount + currentRunningInvoice.getPaidAmount();
        }

        if (currentMonthInvoices != null && !currentMonthInvoices.isEmpty()) {
            double rentForOldBeds = currentMonthInvoices
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getTotalAmount() != null) {
                            return i.getTotalAmount();
                        }
                        return 0;
                    })
                    .sum();
            currentMonthTotalAmount = currentMonthTotalAmount + rentForOldBeds;
            currentPayableRent = rentForOldBeds;

            double rentPaidForOldBeds = currentMonthInvoices
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() != null) {
                            return i.getPaidAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            currentMonthPaidAmount = currentMonthPaidAmount + rentPaidForOldBeds;

        }

        totalAdvacePaidAmount = bookingAmount + advancePaidAmount;

        if (latestBed != null) {
            double rentPerMonth = latestBed.getRentAmount();
            assert currentRunningInvoice != null;
            long stayedDays = Utils.findNumberOfDays(currentRunningInvoice.getInvoiceStartDate(), new Date());
            long noOfDaysInTheMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());

            double invoiceRentAmount = currentRunningInvoice.getInvoiceItems()
                    .stream()
                    .filter(i -> i.getInvoiceItem().equalsIgnoreCase(InvoiceItems.RENT.name()))
                    .mapToDouble(i -> {
                        if (i.getAmount() != null) {
                            return i.getAmount();
                        }
                        return 0.0;
                    })
                    .sum();
            double rentPerDays = rentPerMonth / noOfDaysInTheMonth;
            double rentForStayedDays = rentPerDays * stayedDays;

            currentPayableRent = currentPayableRent + rentForStayedDays;

        }

        totalAmountWithoutDeductions =  currentPayableRent - totalAdvacePaidAmount - currentMonthPaidAmount;
        totalAmountToBePaid = currentPayableRent - totalAdvacePaidAmount - currentMonthPaidAmount;

        totalAmountToBePaid = totalAmountToBePaid + deductionsAmount;

        assert listAllUnpaidInvoices != null;
        List<InvoicesV1> unpaidInvoicesForCancelling = new ArrayList<>(listAllUnpaidInvoices);
        if (advaceInvoice != null) {
            if (!advaceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                unpaidInvoicesForCancelling.add(advaceInvoice);
            }
        }
        if (currentMonthInvoices != null && !currentMonthInvoices.isEmpty()) {
            List<InvoicesV1> currentMonthUnpaid = currentMonthInvoices
                    .stream()
                    .filter(i -> !i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()))
                    .toList();
            unpaidInvoicesForCancelling.addAll(currentMonthUnpaid);

        }
        if (currentRunningInvoice != null) {
            if (!currentRunningInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                unpaidInvoicesForCancelling.add(currentRunningInvoice);
            }
        }

        List<InvoicesV1> cancellInvoices = unpaidInvoicesForCancelling
                .stream()
                .peek(item -> item.setCancelled(true))
                .toList();

        if (cancellInvoices != null && !cancellInvoices.isEmpty()) {
            invoiceService.cancelActiveInvoice(cancellInvoices);
        }


        if (advaceInvoice != null) {
            invoiceService.createSettlementInvoice(customers, customers.getHostelId(), Math.round(totalAmountToBePaid), cancellInvoices, listDeductions, totalAmountWithoutDeductions);

            customers.setCurrentStatus(CustomerStatus.SETTLEMENT_GENERATED.name());
            customersRepository.save(customers);
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public boolean customerExist(String hostelId) {
        return customersRepository.existsByHostelIdAndCurrentStatusIn(hostelId, List.of(CustomerStatus.NOTICE.name(), CustomerStatus.CHECK_IN.name(), CustomerStatus.BOOKED.name()));
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
        if (wallet != null && wallet.getAmount() != null) {
            wallet.setAmount(wallet.getAmount() - balanceAmount);
            wallet.setTransactionDate(joiningDate);
        } else {
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

    public ResponseEntity<?> cancelCheckOut(String hostelId, String customerId, CancelCheckout request) {
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
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingsV1 = bookingsService.findBookingsByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.NO_BOOKING_INFORMATION_FOUND, HttpStatus.BAD_REQUEST);
        }
//        if (bookingsV1.getBedId() == request.bedId()) {
//            return new ResponseEntity<>(Utils.CHANGE_BED_SAME_BED_ERROR, HttpStatus.BAD_REQUEST);
//        }

        Date reCheckInDate = Utils.stringToDate(request.reCheckInDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        if (bookingsService.isBedBookedNextDay(bookingsV1.getBedId(), customerId, reCheckInDate)) {
            return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(reCheckInDate, bookingsV1.getJoiningDate()) < 0) {
            return new ResponseEntity<>(Utils.RECHECK_DATE_SHOULD_BE_GREATER_THAN_JOINING_DATE, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoicesV1 = invoiceService.getFinalSettlementStatus(customers.getCustomerId());
        if (invoicesV1 != null) {
            invoicesV1.setPaymentStatus(PaymentStatus.CANCELLED.name());
            invoicesV1.setUpdatedAt(new Date());
            invoicesV1.setUpdatedBy(user.getUserId());
            invoiceService.saveInvoice(invoicesV1);
        }


        Reasons reasons = new Reasons();
        reasons.setReasonText(request.reason());
        reasons.setCreatedAt(new Date());
        reasons.setCreatedBy(user.getUserId());
        reasonService.SaveReason(reasons);


        bookingsV1.setLeavingDate(null);
        bookingsV1.setCurrentStatus(BookingStatus.CHECKIN.name());
        bookingsV1.setUpdatedAt(new Date());
        bookingsService.saveBooking(bookingsV1);

        customers.setReasons(reasons);
        customers.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.CHECK_IN.name());
        customersRepository.save(customers);


        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public void markCustomerCheckedOut(Customers customers) {
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.VACATED.name());
        customersRepository.save(customers);
    }

    public ResponseEntity<?> getCheckoutCustomers(String hostelId, String name) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CHECKOUT, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(authentication.getName(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        List<CheckoutCustomers> listCustomers =  customersRepository.getCheckedOutCustomerData(hostelId, name)
                .stream()
                .map(item -> {
                    StringBuilder initials = new StringBuilder();
                    String[] nameArray = item.getFirstName().split(" ");
                    initials.append(nameArray[0].toUpperCase().charAt(0));
                    if (nameArray.length > 1) {
                        initials.append(nameArray[nameArray.length - 1].toUpperCase().charAt(0));
                    } else {
                        initials.append(nameArray[0].toUpperCase().charAt(1));
                    }
                    String currentStatus = null;
                    if (item.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                        currentStatus = "Vacated";
                    }

                    return new CheckoutCustomers(item.getFirstName(),
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
                            item.getFloorName(),
                            Utils.dateToString(item.getCheckoutDate()));
                })
                .toList();

        return new ResponseEntity<>(listCustomers, HttpStatus.OK);
    }

    public List<Customers> getCustomerDetails(List<String> customerIds) {
        if (!customerIds.isEmpty()) {
            return customersRepository.findByCustomerIdIn(customerIds);
        }
        return null;
    }

    public Double getAdvanceAmountFromAllCustomers(String hostelId) {
        List<Customers> listCustomers = customersRepository.findCheckedInCustomerByHostelId(hostelId);
        return listCustomers
                .stream()
                .map(Customers::getAdvance)
                .mapToDouble(Advance::getAdvanceAmount)
                .sum();
    }

    public void updateCustomersJoiningDate(Customers customers, Date joinigDate) {
        customers.setJoiningDate(joinigDate);
        customersRepository.save(customers);
    }

    public void updateAdvanceAmount(Customers customers, Advance advance) {
        customers.setAdvance(advance);
        customersRepository.save(customers);
    }

    public boolean existsByHostelIdAndCustomerIdAndStatusesIn(String s, String s1, List<String> currentStatus) {
        return customersRepository.existsByHostelIdAndCustomerIdAndStatusesIn(s, s1, currentStatus);
    }

    public ResponseEntity<?> deleteCustomer(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersRepository.findById(customerId).orElse(null);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_ACTIVE_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }

        customers.setCurrentStatus(CustomerStatus.DELETED.name());
        customersRepository.save(customers);


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public List<Customers> searchCustomerByHostelName(String hostelId, String keyword) {
        return customersRepository.findByHostelIdAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(hostelId, keyword, keyword);
    }

}
