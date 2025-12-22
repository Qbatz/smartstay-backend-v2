package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.events.RecurringEvents;
import com.smartstay.smartstay.services.BookingsService;
import com.smartstay.smartstay.services.HostelConfigService;
import com.smartstay.smartstay.services.HostelService;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class InvoiceScheduler {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @Scheduled(cron = "0 29 10 * * *")
    public void generateInvoice() {


        List<com.smartstay.smartstay.dao.BillingRules> listBillingRules = hostelConfigService.findAllHostelsHavingBillingToday();

        List<HostelV1> listHostels = listBillingRules
                .stream()
                .map(BillingRules::getHostel)
                .toList();

        if (listHostels != null && !listHostels.isEmpty()) {
            listHostels.forEach(item -> {
                applicationEventPublisher.publishEvent(new RecurringEvents(this, item.getHostelId()));
            });
        }
    }
}
