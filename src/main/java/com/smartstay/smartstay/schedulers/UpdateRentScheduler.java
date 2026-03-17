package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.services.BookingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateRentScheduler {

    @Autowired
    private BookingsService bookingsService;

    @Scheduled(cron = "0 1 0 * * *")
    public void updateRentalAmount() {
        bookingsService.updateRentalAmount();
    }
}
