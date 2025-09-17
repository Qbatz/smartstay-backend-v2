package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BookingsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.dto.customer.CustomersBookingDetails;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.BookingRequest;
import com.smartstay.smartstay.payloads.customer.CheckInRequest;
import com.smartstay.smartstay.repositories.BookingsRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public void assignBedToCustomer(AssignBed assignBed) {

        if (authentication.isAuthenticated()) {
            BookingsV1 bookings = new BookingsV1();
            bookings.setCustomerId(assignBed.customerId());
            bookings.setUpdatedAt(new Date());
            bookings.setJoiningDate(Utils.stringToDate(assignBed.joiningDate(), Utils.DATE_FORMAT_YY));
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

    public BookingsV1 checkLatestStatusForBed(int bedId) {

        return bookingsRepository.findLatestBooking(bedId);
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
    public BookingsV1 checkinCustomer(CheckInRequest request, String customerId) {
        BookingsV1 bookingv1 = new BookingsV1();
        bookingv1.setCustomerId(customerId);
        bookingv1.setHostelId(request.hostelId());
        String date = request.joiningDate().replace("/", "-");
        if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) <= 0) {
            bookingv1.setCurrentStatus(BookingStatus.CHECKIN.name());
        }
        else {
            bookingv1.setCurrentStatus(BookingStatus.BOOKED.name());
        }

        bookingv1.setUpdatedAt(new Date());
        bookingv1.setUpdatedBy(authentication.getName());
        bookingv1.setBedId(request.bedId());
        bookingv1.setFloorId(request.floorId());
        bookingv1.setHostelId(request.hostelId());
        bookingv1.setRentAmount(request.rentalAmount());
        bookingv1.setCreatedAt(new Date());
        bookingv1.setCreatedBy(authentication.getName());
        bookingv1.setRoomId(request.roomId());
        bookingv1.setRentAmount(bookingv1.getRentAmount());
        bookingv1.setJoiningDate(Utils.stringToDate(request.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

        return bookingsRepository.save(bookingv1);

    }

    public BookingsV1 addBooking(String hostelId, BookingRequest payload) {
        if (authentication.isAuthenticated()) {
            BookingsV1 bookingsV1 = new BookingsV1();
            bookingsV1.setHostelId(hostelId);
            bookingsV1.setFloorId(payload.floorId());
            bookingsV1.setRoomId(payload.roomId());
            bookingsV1.setBedId(payload.bedId());
            bookingsV1.setCustomerId(payload.customerId());
            bookingsV1.setCreatedAt(new Date());
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setUpdatedBy(authentication.getName());
            bookingsV1.setLeavingDate(null);
            bookingsV1.setExpectedJoiningDate(Utils.stringToDate(payload.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

            bookingsV1.setCurrentStatus(BedStatus.BOOKED.name());

            return bookingsRepository.save(bookingsV1);
        }

        return null;
    }

    public void addChecking(String customerId, CheckInRequest payloads) {
        String date = payloads.joiningDate().replace("/", "-");
        BookingsV1 bookingsV1 = findBookingsByCustomerIdAndHostelId(customerId, payloads.hostelId());
        if (bookingsV1 != null) {
            bookingsV1.setUpdatedAt(new Date());
            bookingsV1.setLeavingDate(null);
            if (Utils.compareWithTwoDates(new Date(), Utils.stringToDate(date, Utils.USER_INPUT_DATE_FORMAT)) <= 0) {
                bookingsV1.setCurrentStatus(BookingStatus.CHECKIN.name());
            }
            else {
                bookingsV1.setCurrentStatus(BookingStatus.BOOKED.name());
            }
            bookingsV1.setRoomId(payloads.roomId());
            String rawDateStr = payloads.joiningDate().replace("-", "/");

            Date joiningDate = Utils.convertStringToDate(rawDateStr);
            bookingsV1.setJoiningDate(joiningDate);
            bookingsRepository.save(bookingsV1);
        }else {
            checkinCustomer(payloads, customerId);
        }
    }

    public boolean checkIsBedOccupied(Integer bedId) {
        BookingsV1 bookingsV1 = bookingsRepository.findLatestBooking(bedId);
        if (bookingsV1.getLeavingDate() != null) {
            if (Utils.compareWithTwoDates(new Date(), bookingsV1.getLeavingDate()) < 0) {
                return true;
            }
        }
        return false;
    }

    public CustomersBookingDetails getCustomerBookingDetails(String customerId) {
        return bookingsRepository.getCustomerBookingDetails(customerId);
    }
}
