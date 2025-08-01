package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BookingsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.beds.AssignBed;
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
            bookings.setJoiningDate(Utils.stringToDate(assignBed.joiningDate()));
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

    public BookingsV1 saveBooking(BookingsV1 bookingsV1) {

        return bookingsRepository.save(bookingsV1);
    }
}
