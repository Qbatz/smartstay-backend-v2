package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.events.RecurringEvents;
import com.smartstay.smartstay.responses.hostelConfig.BillingRules;
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


    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Kolkata")
    public void generateInvoice() {
        List<HostelV1> listHostels = hostelService.getAllHostelsForRecuringInvoice();
        List<String> listOfHostelsHavingBillDateToday = new ArrayList<>();

        listHostels
                .forEach(item -> {
                    BillingDates billingRules = hostelConfigService.getBillingRuleByDateAndHostelId(item.getHostelId(), new Date());
                    if (billingRules != null) {
                        if (Utils.compareWithTwoDates(billingRules.currentBillStartDate(), new Date()) == 0) {
                            applicationEventPublisher.publishEvent(new RecurringEvents(this, item.getHostelId()));
                        }

                    }
                });
    }
}
