package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.repositories.BookingsRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BookingsService {

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private Authentication authentication;

    public void assignBedToCustomer(AssignBed assignBed) {

        if (authentication.isAuthenticated()) {
            BookingsV1 bookings = new BookingsV1();
            bookings.setCustomerId(assignBed.customerId());
            bookings.setUpdatedAt(new Date());
            bookings.setJoiningDate(Utils.stringToDate(assignBed.joiningDate()));
            bookings.setRentAmount(assignBed.rentalAmount());
            bookings.setCreatedAt(new Date());
            bookings.setCreatedBy(authentication.getName());
            bookings.setHostelId(assignBed.hostelId());
            bookings.setFloorId(assignBed.floorId());
            bookings.setRoomId(assignBed.roomId());
            bookings.setBedId(assignBed.bedId());

            bookingsRepository.save(bookings);
        }

    }

}
