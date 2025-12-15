package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BookingsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.BedInformations;
import com.smartstay.smartstay.dto.beds.BedRoomFloor;
import com.smartstay.smartstay.dto.booking.BedBookingStatus;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.booking.BookedCustomerInfoElectricity;
import com.smartstay.smartstay.dto.customer.CancelBookingDto;
import com.smartstay.smartstay.dto.customer.CustomersBookingDetails;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.invoices.InvoiceCustomer;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.beds.ChangeBed;
import com.smartstay.smartstay.payloads.booking.CancelBooking;
import com.smartstay.smartstay.payloads.booking.UpdateAdvance;
import com.smartstay.smartstay.payloads.booking.UpdateBookingDetails;
import com.smartstay.smartstay.payloads.customer.BookingRequest;
import com.smartstay.smartstay.payloads.customer.CheckInRequest;
import com.smartstay.smartstay.repositories.BookingsRepository;
import com.smartstay.smartstay.responses.banking.DebitsBank;
import com.smartstay.smartstay.responses.bookings.InitializeCancel;
import com.smartstay.smartstay.responses.bookings.InitializeCheckIn;
import com.smartstay.smartstay.responses.bookings.InitializeCheckout;
import com.smartstay.smartstay.util.Utils;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class BookingsService {

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService userService;
    @Autowired
    private UserHostelService userHostelService;
    private BedsService bedsService;
    @Autowired
    private InvoiceV1Service invoiceService;
    private CustomersService customersService;
    @Autowired
    private CreditDebitNoteService creditDebitNoteService;
    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private BankingService bankingService;
    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;
    @Autowired
    private RentHistoryService rentHistoryService;
    private CustomersConfigService customersConfigService;
    private HostelService hostelService;

    @Autowired
    public void setBedsService(@Lazy BedsService bedsService) {
        this.bedsService = bedsService;
    }

    @Autowired
    public void setCustomersConfigService(@Lazy CustomersConfigService customersConfigService) {
        this.customersConfigService = customersConfigService;
    }

    @Autowired
    public void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }

    public void assignBedToCustomer(AssignBed assignBed) {

        if (authentication.isAuthenticated()) {
            BookingsV1 bookings = new BookingsV1();
            bookings.setCustomerId(assignBed.customerId());
            bookings.setUpdatedAt(new Date());
            bookings.setJoiningDate(Utils.stringToDate(assignBed.joiningDate(), Utils.USER_INPUT_DATE_FORMAT));
            bookings.setRentAmount(assignBed.rentalAmount());
            bookings.setCreatedAt(new Date());
            bookings.setCreatedBy(authentication.getName());
            bookings.setCurrentStatus(CustomerStatus.CHECK_IN.name());
            bookings.setHostelId(assignBed.hostelId());
            bookings.setFloorId(assignBed.floorId());
            bookings.setRoomId(assignBed.roomId());
            bookings.setBedId(assignBed.bedId());

            bookingsRepository.save(bookings);
        }

    }


    public ResponseEntity<?> getAllCheckInCustomers(String hostelId) {
        if (authentication.isAuthenticated()) {

            String userId = authentication.getName();
            Users user = userService.findUserByUserId(userId);

            if (!rolesService.checkPermission(user.getRoleId(), ModuleId.CUSTOMERS.getId(), Utils.PERMISSION_READ)) {
                return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
            }

            if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
                return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            List<Bookings> allCheckInList = bookingsRepository.findAllByHostelId(hostelId);
            List<com.smartstay.smartstay.responses.bookings.Bookings> responseBookings = allCheckInList.stream().map(item -> new BookingsMapper().apply(item)).toList();
            return new ResponseEntity<>(responseBookings, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    public List<BookingsV1> getAllCheckedInCustomer(String hostelId) {
        List<String> statuses = new ArrayList<>();
        statuses.add(BookingStatus.NOTICE.name());
        statuses.add(BookingStatus.CHECKIN.name());
        return bookingsRepository.findByHostelIdAndCurrentStatusIn(hostelId, statuses);
    }

    public List<BookingsV1> getAllCheckedInCustomersByListOfCustomerIdsAndHostelId(List<String> customerIds, String hostelId) {
        return bookingsRepository.findBookingsByListOfCustomersAndHostelId(customerIds, hostelId);
    }

    public int getAllCheckedInCustomersCount(String hostelId) {
        List<String> statuses = new ArrayList<>();
        statuses.add(BookingStatus.NOTICE.name());
        statuses.add(BookingStatus.CHECKIN.name());
        List<BookingsV1> checkIncount =  bookingsRepository.findByHostelIdAndCurrentStatusIn(hostelId, statuses);
        if (checkIncount != null) {
            return checkIncount.size();
        }
        return 0;
    }

    public BookingsV1 checkLatestStatusForBed(int bedId) {

        return bookingsRepository.findLatestBooking(bedId);
    }

    public BookingsV1 checkOccupiedByBedId(Integer bedId) {
        return bookingsRepository.findOccupiedDetails(bedId);
    }

//    public BookingsV1 saveBooking(BookingsV1 bookingsV1) {
//
//        return bookingsRepository.save(bookingsV1);
//    }

    public BookingsV1 getBookingsByCustomerId(String customerId) {
        return bookingsRepository.findByCustomerId(customerId);
    }

    public int getBedIdFromBooking(String customerId, String hostelId) {
        if (customerId != null && !customerId.equalsIgnoreCase("") && hostelId != null && !hostelId.equalsIgnoreCase("")) {
            BookingsV1 bookingsV1 = bookingsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
            if (bookingsV1 == null) {
                return 0;
            }
            return bookingsV1.getBedId();
        }
        return 0;
    }

    public BookingsV1 findBookingsByCustomerIdAndHostelId(String customerId, String hostelId) {
        return bookingsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
    }

    public int moveToNotice(String customerId, String relievingDate, String requestedDate, String reason) {

        if (!authentication.isAuthenticated()) {
            return -1;
        }

        BookingsV1 booking = getBookingsByCustomerId(customerId);

        if (booking == null) {
            return 0;
        }

        booking.setReasonForLeaving(reason);
        booking.setLeavingDate(Utils.stringToDate(relievingDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        booking.setNoticeDate(Utils.stringToDate(requestedDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        booking.setUpdatedAt(new Date());
        booking.setCurrentStatus(BedStatus.NOTICE.name());
        booking.setUpdatedBy(authentication.getName());

        bookingsRepository.save(booking);

        return 1;
    }

    /**
     *
     * this works only for the customer who are directly checkin
     *
     * not booked then check in
     * @return
     */
    public BookingsV1 checkinCustomer(CheckInRequest request, Customers customers) {
        BookingsV1 bookingv1 = new BookingsV1();
        bookingv1.setCustomerId(customers.getCustomerId());
        bookingv1.setHostelId(customers.getHostelId());
        String date = request.joiningDate().replace("/", "-");

        bookingv1.setCurrentStatus(BookingStatus.CHECKIN.name());

        bookingv1.setBookingAmount(0.0);
        bookingv1.setAdvanceAmount(request.advanceAmount());
        bookingv1.setUpdatedAt(new Date());
        bookingv1.setBookingDate(Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        bookingv1.setUpdatedBy(authentication.getName());
        bookingv1.setBedId(request.bedId());
        bookingv1.setFloorId(request.floorId());
        bookingv1.setHostelId(customers.getHostelId());
        bookingv1.setRentAmount(request.rentalAmount());
        bookingv1.setCreatedAt(new Date());
        bookingv1.setCreatedBy(authentication.getName());
        bookingv1.setRoomId(request.roomId());
        bookingv1.setUpdatedBy(authentication.getName());
        bookingv1.setUpdatedAt(new Date());

        bookingv1.setJoiningDate(Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

        CustomersBedHistory cbh = new CustomersBedHistory();
        cbh.setRoomId(bookingv1.getRoomId());
        cbh.setBedId(bookingv1.getBedId());
        cbh.setFloorId(bookingv1.getFloorId());
        cbh.setHostelId(bookingv1.getHostelId());
        cbh.setStartDate(bookingv1.getJoiningDate());
        cbh.setCustomerId(bookingv1.getCustomerId());
        cbh.setChangedBy(authentication.getName());
        cbh.setType(CustomersBedType.CHECK_IN.name());
        cbh.setActive(true);
        cbh.setCreatedAt(new Date());
        cbh.setRentAmount(request.rentalAmount());

        customersBedHistoryService.saveCheckInHistory(cbh);


//        List<RentHistory> rentHistoryList = new ArrayList<>();
//        RentHistory rentHistory = rentHistoryService.addInitialRent(bookingv1);
//        rentHistoryList.add(rentHistory);
//
//        bookingv1.setRentHistory(rentHistoryList);

        return bookingsRepository.save(bookingv1);

    }

    public BookingsV1 addBooking(String hostelId, BookingRequest payload) {
        if (authentication.isAuthenticated()) {
            BookingsV1 bookingsV1 = new BookingsV1();
            bookingsV1.setHostelId(hostelId);
            bookingsV1.setIsBooked(true);
            bookingsV1.setFloorId(payload.floorId());
            bookingsV1.setRoomId(payload.roomId());
            bookingsV1.setBedId(payload.bedId());
            bookingsV1.setCustomerId(payload.customerId());
            bookingsV1.setCreatedAt(new Date());
            bookingsV1.setCreatedBy(authentication.getName());
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setBookingAmount(payload.bookingAmount());
            bookingsV1.setBookingDate(Utils.stringToDate(payload.bookingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            bookingsV1.setUpdatedBy(authentication.getName());
            bookingsV1.setLeavingDate(null);
            bookingsV1.setExpectedJoiningDate(Utils.stringToDate(payload.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

            bookingsV1.setCurrentStatus(BedStatus.BOOKED.name());

            CustomersBedHistory customersBedHistory = new CustomersBedHistory();
            customersBedHistory.setType(CustomersBedType.BOOKED.name());
            customersBedHistory.setRoomId(payload.roomId());
            customersBedHistory.setBedId(payload.bedId());
            customersBedHistory.setFloorId(payload.floorId());
            customersBedHistory.setHostelId(hostelId);
            customersBedHistory.setCustomerId(payload.customerId());
            customersBedHistory.setChangedBy(authentication.getName());
            customersBedHistory.setReason("Booking");
            customersBedHistory.setActive(true);
            customersBedHistory.setCreatedAt(new Date());

            customersBedHistoryService.saveCheckInHistory(customersBedHistory);

            return bookingsRepository.save(bookingsV1);
        }

        return null;
    }

    public void addCheckin(Customers customers, CheckInRequest payloads) {
        String date = payloads.joiningDate().replace("/", "-");
        BookingsV1 bookingsV1 = findBookingsByCustomerIdAndHostelId(customers.getCustomerId(), customers.getHostelId());
        if (bookingsV1 != null) {
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setIsBooked(false);
            bookingsV1.setLeavingDate(null);
            bookingsV1.setCurrentStatus(BookingStatus.CHECKIN.name());
            bookingsV1.setRoomId(payloads.roomId());
            String rawDateStr = payloads.joiningDate().replace("-", "/");

            Date joiningDate = Utils.convertStringToDate(rawDateStr);
            bookingsV1.setJoiningDate(joiningDate);
            bookingsV1.setAdvanceAmount(payloads.advanceAmount());

            CustomersBedHistory cbh = new CustomersBedHistory();
            cbh.setRoomId(bookingsV1.getRoomId());
            cbh.setBedId(bookingsV1.getBedId());
            cbh.setFloorId(bookingsV1.getFloorId());
            cbh.setHostelId(bookingsV1.getHostelId());
            cbh.setStartDate(bookingsV1.getJoiningDate());
            cbh.setCustomerId(bookingsV1.getCustomerId());
            cbh.setChangedBy(authentication.getName());
            cbh.setType(CustomersBedType.CHECK_IN.name());
            cbh.setRentAmount(payloads.rentalAmount());
            cbh.setReason("Initial check in");
            cbh.setActive(true);
            cbh.setCreatedAt(new Date());

            customersBedHistoryService.saveCheckInHistory(cbh);

            bookingsRepository.save(bookingsV1);

            rentHistoryService.addInitialRent(bookingsV1);
        }else {
            BookingsV1 bookingsV11 = checkinCustomer(payloads, customers);
            rentHistoryService.addInitialRent(bookingsV11);
        }
    }

    public void checkInBookedCustomer(Customers customers, CheckInRequest payloads) {
        String date = payloads.joiningDate().replace("/", "-");
        BookingsV1 bookingsV1 = findBookingsByCustomerIdAndHostelId(customers.getCustomerId(), customers.getHostelId());
        if (bookingsV1 != null) {
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setLeavingDate(null);
            bookingsV1.setCurrentStatus(BookingStatus.CHECKIN.name());
            bookingsV1.setRoomId(payloads.roomId());
            bookingsV1.setRentAmount(payloads.rentalAmount());
            String rawDateStr = payloads.joiningDate().replace("-", "/");

            Date joiningDate = Utils.convertStringToDate(rawDateStr);
            bookingsV1.setJoiningDate(joiningDate);
            bookingsV1.setAdvanceAmount(payloads.advanceAmount());

            CustomersBedHistory cbh = new CustomersBedHistory();
            cbh.setRoomId(bookingsV1.getRoomId());
            cbh.setBedId(bookingsV1.getBedId());
            cbh.setFloorId(bookingsV1.getFloorId());
            cbh.setHostelId(bookingsV1.getHostelId());
            cbh.setStartDate(bookingsV1.getJoiningDate());
            cbh.setCustomerId(bookingsV1.getCustomerId());
            cbh.setRentAmount(payloads.rentalAmount());
            cbh.setChangedBy(authentication.getName());
            cbh.setReason("Initial check in");
            cbh.setType(CustomersBedType.CHECK_IN.name());
            cbh.setActive(true);
            cbh.setCreatedAt(new Date());

            customersBedHistoryService.saveCheckInHistory(cbh);
//            List<RentHistory> listRentHistory = new ArrayList<>();
//            RentHistory rentHistory = rentHistoryService.addInitialRent(bookingsV1);
//            listRentHistory.add(rentHistory);
//
//            bookingsV1.setRentHistory(listRentHistory);

            bookingsRepository.save(bookingsV1);
            rentHistoryService.addInitialRent(bookingsV1);
        }
    }

    /**
     *
     * this function is for deleting a bed
     */
    public boolean checkIsBedOccupied(Integer bedId) {
        BookingsV1 bookingsV1 = bookingsRepository.findOccupiedDetails(bedId);
        if (bookingsV1 != null) {
            return true;
        }
        return false;
    }

    public CustomersBookingDetails getCustomerBookingDetails(String customerId) {
        return bookingsRepository.getCustomerBookingDetails(customerId);
    }

    public BookingsV1 getBookingDetails(String bookingId) {
        return bookingsRepository.findById(bookingId).orElse(null);
    }

    /**
     * this works only for booked customers will not work normally
     *
     * @param hostelId
     * @param customerId
     * @return
     */
    public ResponseEntity<?> initializeCheckIn(String hostelId, String customerId) {
        Double bookingAmount = 0.0;
        boolean canCheckIn = true;
        BookingsV1 bookingsV1 = bookingsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 != null) {
            Beds bed = bedsService.isBedAvailabeForCheckIn(bookingsV1.getBedId(),bookingsV1.getExpectedJoiningDate());
            bookingAmount = invoiceService.getBookingAmount(customerId, hostelId);
            if (bookingAmount == null) {
                bookingAmount = 0.0;
            }

            if (bed != null) {
                if (bed.getCurrentStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
                    canCheckIn = true;
                }
                else if (Utils.compareWithTwoDates(new Date(), bed.getFreeFrom()) > 0) {
                    canCheckIn = false;
                }
                InitializeCheckIn initializeCheckIn = new InitializeCheckIn(
                        bed.getBedId(),
                        bed.getBedName(),
                        bookingAmount,
                        Utils.dateToString(bookingsV1.getBookingDate()),
                        bed.getRentAmount(),
                        canCheckIn,
                        bookingsV1.getBookingId()
                );

                return new ResponseEntity<>(initializeCheckIn, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(Utils.BED_CURRENTLY_UNAVAILABLE, HttpStatus.BAD_REQUEST);
    }

    public BookingsV1 findByBookingId(String bookingId) {
        return bookingsRepository.findById(bookingId).orElse(null);
    }

    public ResponseEntity<?> cancelBooking(String customerId, CancelBooking cancelBooking) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BOOKING.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BookingsV1 bookingsV1 = bookingsRepository.findByCustomerId(customerId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), bookingsV1.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }
        if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_INACTIVE_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (!bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_INACTIVE_ACTIVE_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_INACTIVE_ERROR, HttpStatus.BAD_REQUEST);
        }
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }


        Date cancelDate = null;
        if (cancelBooking.cancelDate() != null) {
            cancelDate = Utils.stringToDate(cancelBooking.cancelDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

            if (Utils.compareWithTwoDates(cancelDate, bookingsV1.getBookingDate()) < 0) {
                return new ResponseEntity<>(Utils.DATE_VALIDATION_ERROR_CANCEL_BOOKING, HttpStatus.BAD_REQUEST);
            }
        }
        else {
            cancelDate = new Date();
        }
        bookingsV1.setCancelDate(cancelDate);
        bookingsV1.setReasonForCancellation(cancelBooking.reason());
        bookingsV1.setCurrentStatus(BookingStatus.CANCELLED.name());

        bookingsRepository.save(bookingsV1);

        customersService.markCustomerInactive(customers);
        InvoicesV1 invoicesV1 = invoiceService.getInvoiceDetails(customerId, bookingsV1.getHostelId());


        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            invoiceService.cancelBookingInvoice(invoicesV1);
            CancelBookingDto cancelBookingDto = new CancelBookingDto(cancelBooking.reason(),
                    customerId,
                    bookingsV1.getBookingAmount(),
                    invoicesV1.getInvoiceId(),
                    cancelBooking.bankId(),
                    cancelBooking.referenceNumber());

            creditDebitNoteService.cancelBooking(cancelBookingDto);


            TransactionDto transactionDto = new TransactionDto(cancelBooking.bankId(),
                    cancelBooking.referenceNumber(),
                    bookingsV1.getBookingAmount(),
                    BankTransactionType.DEBIT.name(),
                    BankSource.INVOICE.name(),
                    bookingsV1.getHostelId(),
                    Utils.dateToString(cancelDate).replace("/", "-"),
                    "");

            bankTransactionService.cancelBooking(transactionDto);
        }

        bedsService.cancelBooking(bookingsV1.getBedId(), user.getParentId());


        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> initiateCancel(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BOOKING.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BookingsV1 bookingsV1 = bookingsRepository.findByCustomerId(customerId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), bookingsV1.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }
        if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_INACTIVE_ERROR, HttpStatus.BAD_REQUEST);
        }

        BedDetails bedInformations = bedsService.getBedDetails(bookingsV1.getBedId());
        String bedName = null;
        String roomName = null;
        String floorName = null;
        if (bedInformations != null) {
            roomName = bedInformations.getRoomName();
            floorName = bedInformations.getFloorName();
            bedName = bedInformations.getBedName();
        }
        List<DebitsBank> listBanks = bankingService.getAllBankForReturn(bookingsV1.getHostelId());

        InitializeCancel initializeCancel = new InitializeCancel(
                bookingsV1.getHostelId(),
                bookingsV1.getBookingId(),
                bookingsV1.getCustomerId(),
                bookingsV1.getBookingAmount(),
                Utils.dateToString(bookingsV1.getExpectedJoiningDate()),
                roomName,
                bedName,
                floorName,
                listBanks);
        return new ResponseEntity<>(initializeCancel, HttpStatus.OK);
    }

    /**
     *
     * this will be called while checking out the customer
     *
     * this should be trigered when cuctomer is in notice
     *
     * @param customerId
     * @return
     */
    public ResponseEntity<?> checkoutCustomer(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = userService.findUserByUserId(userId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BOOKING.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BookingsV1 bookingsV1 = bookingsRepository.findByCustomerId(customerId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), bookingsV1.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.UNAUTHORIZED);
        }

        if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_INACTIVE_ERROR, HttpStatus.BAD_REQUEST);
        }

        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_ALREADY_VACATED, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_NOT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_NOT_GENERATED, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoicesV1 = invoiceService.getFinalSettlementStatus(customers.getCustomerId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_NOT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        if (!invoiceService.isFinalSettlementPaid(invoicesV1)) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_NOT_PAID, HttpStatus.BAD_REQUEST);
        }

        Beds bed = bedsService.makeABedVacant(bookingsV1.getBedId());
        if (bed == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

        customersService.markCustomerCheckedOut(customers);
        customersBedHistoryService.checkoutCustomer(customerId);
        bookingsV1.setCheckoutDate(new Date());
        bookingsV1.setCurrentStatus(BookingStatus.VACATED.name());
        bookingsRepository.save(bookingsV1);
        customersConfigService.disableRecurring(customerId);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public List<BookedCustomer> findBookedCustomers(List<Integer> roomIds, Date startDate, Date endDate) {
        return bookingsRepository.findBookingsByListRooms(roomIds, startDate, endDate);
    }

    public BookingsV1 getBookingInfoByCustomerId(String customerId) {
        return bookingsRepository.findByCustomerId(customerId);
    }

    public List<BookedCustomerInfoElectricity> getAllCheckInCustomers(Integer roomId, Date startDate, Date endDate) {
        return bookingsRepository.getBookingInfoForElectricity(roomId, startDate, endDate);
    }

    /**
     * this is used for manual invoice generation
     *
     * @return
     */
    public BookingsV1 getBookingByCustomerIdAndDate(String customerId, Date startDate, Date endDate) {
        return bookingsRepository.findByCustomerIdAndJoiningDate(customerId, startDate, endDate);
    }

    public void saveBooking(BookingsV1 bookingsV1) {
        bookingsRepository.save(bookingsV1);
    }

    public boolean isBedBookedNextDay(int bedId, String customerId, Date expectedJoiningDate) {
        List<BookingsV1> conflicts = bookingsRepository.findNextDayBookingForSameBed(
                bedId,
                customerId,
                expectedJoiningDate
        );
        return !conflicts.isEmpty();
    }

    public boolean isBedAvailableByDate(Integer bedId, String joiningDate) {
        Date joiningDateDt = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        BookingsV1 bookingsV1 = bookingsRepository.checkBedsAvailabilityForDate(bedId);
        if (bookingsV1 == null) {
            return true;
        }
        if (bookingsV1.getLeavingDate() != null) {
            return Utils.compareWithTwoDates(joiningDateDt, bookingsV1.getLeavingDate()) >= 0;
        }
        else if (bookingsV1.getJoiningDate() != null) {
            return Utils.compareWithTwoDates(joiningDateDt, bookingsV1.getJoiningDate()) < 0;
        }
        else {
            return Utils.compareWithTwoDates(joiningDateDt, bookingsV1.getExpectedJoiningDate()) < 0;
        }
    }

    public void reassignBed(BedRoomFloor bedRoomFloor, BookingsV1 bookingsV1, ChangeBed request) {

        Date endDate = Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Calendar previousEndDate = Calendar.getInstance();
        previousEndDate.setTime(endDate);
        previousEndDate.set(Calendar.DAY_OF_MONTH, previousEndDate.get(Calendar.DAY_OF_MONTH) -1);


        CustomersBedHistory currentBed = customersBedHistoryService.getLatestCustomerBed(bookingsV1.getCustomerId());
        currentBed.setEndDate(previousEndDate.getTime());
        customersBedHistoryService.updateBedEndDate(currentBed);

        CustomersBedHistory cbh = new CustomersBedHistory();
        Double rent = bookingsV1.getRentAmount();

        if (request.rentAmount() != null ) {
            bookingsV1.setRentAmount(request.rentAmount());
            rent = request.rentAmount();
        }
        bookingsV1.setBedId(request.bedId());
        bookingsV1.setFloorId(bedRoomFloor.getFloorId());
        bookingsV1.setRoomId(bedRoomFloor.getRoomId());

        cbh.setRoomId(bookingsV1.getRoomId());
        cbh.setBedId(bookingsV1.getBedId());
        cbh.setFloorId(bookingsV1.getFloorId());
        cbh.setHostelId(bookingsV1.getHostelId());
        cbh.setStartDate(Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        cbh.setCustomerId(bookingsV1.getCustomerId());
        cbh.setChangedBy(authentication.getName());
        cbh.setType(CustomersBedType.REASSIGNED.name());
        cbh.setReason(request.reason());
        cbh.setRentAmount(rent);
        cbh.setActive(true);
        cbh.setCreatedAt(new Date());

        customersBedHistoryService.saveCheckInHistory(cbh);

        bookingsRepository.save(bookingsV1);
    }

    public List<BookingsV1> getBookingInfoByBedId(Integer bedId) {
        return bookingsRepository.findBookedDetails(bedId);
    }

    public List<BedBookingStatus> getBookingDetailsByBedIds(List<Integer> listBedId) {
        return bookingsRepository.findByBedBookingStatus(listBedId);
    }

    public boolean isBedAvailableForCheckIn(int bedId, String joiningDate) {
//        BookingsV1 bookingsV1 = bookingsRepository.checkBookingsByBedIdAndStatus(bedId);
        List<BookingsV1> listBookings = bookingsRepository.checkBookingsByBedIdAndStatus(bedId);
        if (listBookings == null) {
            return true;
        }

        BookingsV1 currenlyCheckIn = listBookings
                .stream()
                .filter(i -> (i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()) || i.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())))
                .findAny()
                .orElse(null);

        if (currenlyCheckIn == null) {
            return false;
        }



        Date dateJoiningDate = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);

        if (Utils.compareWithTwoDates(dateJoiningDate, currenlyCheckIn.getLeavingDate()) >= 0) {
            return true;
        }
        else {
            return false;
        }

    }

    public List<InvoiceCustomer> findDueCustomers(List<String> lisCustomerIds) {
        return invoiceService.findDueCustomers(lisCustomerIds);
    }

    public Integer getBookedBedCount(String hostelId) {
        List<BookingsV1> listBookings = bookingsRepository.findBookingsByHostelId(hostelId);
        if (listBookings == null) {
            return  0;
        }
        return listBookings.size();
    }

    public Double getNextMonthProjections(String hostelId, BillingDates currentMonthBillingDate) {
        List<String> statuses = new ArrayList<>();
        statuses.add(BookingStatus.NOTICE.name());
        statuses.add(BookingStatus.CHECKIN.name());
        List<BookingsV1> checkedInUsers =  bookingsRepository.findByHostelIdAndCurrentStatusIn(hostelId, statuses);

        Calendar nextMonthBillStartDate = Calendar.getInstance();
        nextMonthBillStartDate.setTime(currentMonthBillingDate.currentBillStartDate());
        nextMonthBillStartDate.add(Calendar.MONTH, 1);

        Calendar nextMonthBillEndDate = Calendar.getInstance();
        nextMonthBillEndDate.setTime(currentMonthBillingDate.currentBillEndDate());
        nextMonthBillEndDate.add(Calendar.MONTH, 1);


        List<BookingsV1> newJoiners = bookingsRepository.findBookingsWithDate(hostelId, nextMonthBillStartDate.getTime());

        double checkInRent = checkedInUsers
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()))
                .mapToDouble(BookingsV1::getRentAmount)
                .sum();
        double noticeRentAmount = checkedInUsers
                .stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name()))
                .mapToDouble(item -> {
                    if (Utils.compareWithTwoDates(item.getLeavingDate(), nextMonthBillStartDate.getTime()) > 0) {
                        if (Utils.compareWithTwoDates(item.getLeavingDate(), nextMonthBillEndDate.getTime()) < 0) {
                            long totalNumberOfDays = Utils.findNumberOfDays(nextMonthBillStartDate.getTime(), nextMonthBillEndDate.getTime());
                            long noOfDaysStaying = Utils.findNumberOfDays(nextMonthBillStartDate.getTime(), item.getLeavingDate());


                            double rentPerDay = item.getRentAmount() / totalNumberOfDays;
                            double rentForLeavingDate = noOfDaysStaying * rentPerDay;

                            return Math.round(rentForLeavingDate);
                        }
                        else {
                            return item.getRentAmount();
                        }
                    }
                    else {
                        return 0.0;
                    }

                })
                .sum();
        double upcomingJoiningRents = newJoiners
                .stream()
                .mapToDouble(BookingsV1::getRentAmount)
                .sum();

        return checkInRent + noticeRentAmount + upcomingJoiningRents;

    }

    public ResponseEntity<?> updateBookingInfo(String hostelId, String bookingId, UpdateBookingDetails updateInfo) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BookingsV1 bookingsV1 = bookingsRepository.findById(bookingId).orElse(null);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }

        Customers customers = customersService.getCustomerInformation(bookingsV1.getCustomerId());
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_JOINING_DATE_VACATED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_JOINING_DATE_SETTLEMENT_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_JOINING_DATE_CANCELLED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }

        if (updateInfo == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (updateInfo.joiningDate() != null && !updateInfo.joiningDate().equalsIgnoreCase("")) {
            //trying to modify the joining date.
            Date joinigDate = Utils.stringToDate(updateInfo.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name()) || customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                if (invoiceService.updateJoiningDate(customers, joinigDate, hostelId, customers.getJoiningDate(), bookingsV1.getRentAmount())) {
                    rentHistoryService.updateJoiningDate(customers.getCustomerId(), joinigDate);
                    customersBedHistoryService.updateJoiningDate(customers.getCustomerId(), joinigDate);
                    customersService.updateCustomersJoiningDate(customers, joinigDate);
                    bookingsV1.setJoiningDate(joinigDate);
                    bookingsRepository.save(bookingsV1);

                    return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

                }
                else {
                    return new ResponseEntity<>(Utils.CANNOT_UPDATE_JOINING_DATE_DUE_TO_INVOICES, HttpStatus.BAD_REQUEST);
                }

            }
            else {
                return new ResponseEntity<>(Utils.CANNOT_CHANGE_JOINING_DATE_CUSTOMER_NOT_CHECKEDIN, HttpStatus.BAD_REQUEST);
            }

        }
        else if (updateInfo.newRent() != null) {
            if (updateInfo.newRent() <= 0.5) {
                return new ResponseEntity<>(Utils.RENT_AMOUNT_REQUIRED_TO_UPDATE_RENT, HttpStatus.BAD_REQUEST);
            }
            BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
            Calendar newBillingCalendar = Calendar.getInstance();
            newBillingCalendar.setTime(billingDates.currentBillStartDate());
            newBillingCalendar.add(Calendar.MONTH, 1);

            Date startDate = null;
            Date currentRentEndDate = null;
            if (updateInfo.effectiveDate() != null && !updateInfo.effectiveDate().trim().equalsIgnoreCase("")) {
                Date startsFrom = Utils.stringToDate(updateInfo.effectiveDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                if (Utils.compareWithTwoDates(startsFrom, billingDates.currentBillEndDate()) > 0) {
                    BillingDates futureBillDates = hostelService.getBillingRuleOnDate(hostelId, startsFrom);
                    startDate = futureBillDates.currentBillStartDate();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(futureBillDates.currentBillStartDate());
                    calendar.add(Calendar.MONTH, -1);

                    currentRentEndDate = calendar.getTime();
                }
                else {
                    currentRentEndDate = billingDates.currentBillEndDate();
                    startDate = newBillingCalendar.getTime();
                }

                rentHistoryService.updateOldRentEndDate(customers.getCustomerId(), startDate, currentRentEndDate);


                RentHistory rentHistory  = new RentHistory();
                rentHistory.setRent(updateInfo.newRent());
                rentHistory.setStartsFrom(startDate);
                rentHistory.setCustomerId(customers.getCustomerId());
                rentHistory.setReason(updateInfo.reason());
                rentHistory.setCreatedAt(new Date());
                rentHistory.setCreatedBy(authentication.getName());

                rentHistory.setBooking(bookingsV1);
                List<RentHistory> listRentHistories = bookingsV1.getRentHistory();
                listRentHistories.add(rentHistory);

                bookingsRepository.save(bookingsV1);
            }
            else {
                BillingDates billingDateTimeOfJoining = hostelService.getBillingRuleOnDate(hostelId, bookingsV1.getJoiningDate());
                //this is to handle the first time
                if (invoiceService.canChangeRentalAmount(customers, updateInfo.newRent(), bookingsV1.getJoiningDate(), billingDateTimeOfJoining)) {
                    customersBedHistoryService.updateRentAmount(updateInfo.newRent(), customers.getCustomerId());
                    rentHistoryService.updateRentAmount(customers.getCustomerId(), updateInfo.newRent());
                    bookingsV1.setRentAmount(updateInfo.newRent());
                    bookingsRepository.save(bookingsV1);
                }
                else {
                    return new ResponseEntity<>(Utils.CANNOT_CHANGE_RENT_FOR_OLD_DATES, HttpStatus.BAD_REQUEST);
                }

            }



            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

        }

        return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);

    }

    public ResponseEntity<?> updateAdvanceAmount(String hostelId, String bookingId, UpdateAdvance updateAdvance) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BookingsV1 bookingsV1 = bookingsRepository.findById(bookingId).orElse(null);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_BOOKING_ID, HttpStatus.BAD_REQUEST);
        }

        Customers customers = customersService.getCustomerInformation(bookingsV1.getCustomerId());
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_ADVANCE_VACATED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_ADVANCE_CANCELLED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }

        if (updateAdvance == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (updateAdvance.advanceAmount() == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (updateAdvance.advanceAmount().equals(0.0)) {
            return new ResponseEntity<>(Utils.ADVANCE_AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 advanceInvoice = invoiceService.getAdvanceInvoiceDetails(customers.getCustomerId(), hostelId);
        if (advanceInvoice.isCancelled()) {
            return new ResponseEntity<>(Utils.CANNOT_REFUND_CANCELLED_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CHANGE_ADVANCE_PAID_INVOICE, HttpStatus.BAD_REQUEST);
        }

        invoiceService.updateAdvaceAmount(advanceInvoice, updateAdvance.advanceAmount());

        Advance advance = customers.getAdvance();
        if (advance == null) {
            advance = new Advance();
        }
        advance.setAdvanceAmount(updateAdvance.advanceAmount());
        bookingsV1.setAdvanceAmount(updateAdvance.advanceAmount());

        customersService.updateAdvanceAmount(customers, advance);

        bookingsRepository.save(bookingsV1);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    /**
     *
     * used for update the new rent for all the customers
     */
    public void updateRentalAmount() {

        List<RentHistory> listRentHistory = rentHistoryService.findAnyNewRent();

        List<BookingsV1> bookings = listRentHistory
                .stream()
                .map(i -> {
                    BookingsV1 bookingsV1 = i.getBooking();
                    bookingsV1.setRentAmount(i.getRent());
                    return bookingsV1;
                })
                .toList();


        bookingsRepository.saveAll(bookings);
        customersBedHistoryService.updateNewRentAmount(listRentHistory);

    }

    /**
     * used for getting bed info for booking a beds
     *
     * @param bedsForGettingBookingInfo
     * @return
     */
    public List<BookingsV1> getBookingInfoByListOfBeds(List<Integer> bedsForGettingBookingInfo) {
        return bookingsRepository.findLatestBookingsForBeds(bedsForGettingBookingInfo);
    }

    public ResponseEntity<?> initializeCheckout(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!hostelId.equalsIgnoreCase(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.FINAL_SETTLEMENT_NOT_GENERATED, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingsV1 = bookingsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (bookingsV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 finalSettlementInvoice = invoiceService.getFinalSettlementStatus(customerId);
        boolean finalSettlementStatus = invoiceService.isFinalSettlementPaid(finalSettlementInvoice);



        InitializeCheckout initializeCheckout = new InitializeCheckout(finalSettlementStatus,
                Utils.dateToString(bookingsV1.getLeavingDate()),
                Utils.dateToString(bookingsV1.getJoiningDate()));

        return new ResponseEntity<>(initializeCheckout, HttpStatus.OK);
    }

    public List<BookingsV1> checkAllByHostelId(String hostelId) {
        return bookingsRepository.findAllBookingsByHostelId(hostelId);
    }
}
