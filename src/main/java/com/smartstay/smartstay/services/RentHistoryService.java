package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.RentHistory;
import com.smartstay.smartstay.repositories.RentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RentHistoryService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private RentHistoryRepository rentHistoryRepository;


    //this will be triggered when check in the customer
    public RentHistory addInitialRent(BookingsV1 bookingsV1) {
        if (authentication.isAuthenticated()) {
            RentHistory rentHistory = new RentHistory();
            rentHistory.setCustomerId(bookingsV1.getCustomerId());
            rentHistory.setRent(bookingsV1.getRentAmount());
            rentHistory.setReason("Initial check in");
            rentHistory.setStartsFrom(bookingsV1.getJoiningDate());
            rentHistory.setCreatedAt(bookingsV1.getJoiningDate());
            rentHistory.setCreatedBy(authentication.getName());

            rentHistory.setBooking(bookingsV1);

            return rentHistoryRepository.save(rentHistory);
        }

        return null;
    }

    public void updateJoiningDate(String customerId, Date joinigDate) {
        RentHistory rentHistory = rentHistoryRepository.findByCustomerId(customerId);

        rentHistory.setStartsFrom(joinigDate);
        rentHistoryRepository.save(rentHistory);
    }

    public void updateOldRentEndDate(String customerId, Date startDate, Date currentEndDate) {
        RentHistory rentHistory = rentHistoryRepository.findRentByCustomerIdAndDate(customerId, startDate);
        rentHistory.setEndingAt(currentEndDate);

        rentHistoryRepository.save(rentHistory);
    }

    public void updateRentAmount(String customerId, Double newRent) {
        RentHistory rentHistory = rentHistoryRepository.findByCustomerId(customerId);

        rentHistory.setRent(newRent);
        rentHistoryRepository.save(rentHistory);
    }
}
