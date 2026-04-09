package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.events.PostpaidRecurringEvents;
import com.smartstay.smartstay.services.HostelConfigService;
import com.smartstay.smartstay.services.RecurringTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

@Component
public class PostpaidInvoiceSchedular {
    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private RecurringTrackerService recurringTrackerService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    //Schedule it for morning 3;
//    @Scheduled(cron = "0 0 3 * * *") for production
//    @Scheduled(cron = "0 15 3 * * *") for dev
    @Scheduled(cron = "0 15 3 * * *")
    public void scheduleInvoice() {
        List<BillingRules> listBillingRules = hostelConfigService.findHostelsHavingBillingStartDateToday();

        if (!listBillingRules.isEmpty()) {
            List<HostelV1> listHostels = listBillingRules
                    .stream()
                    .map(BillingRules::getHostel)
                    .toList();

            listBillingRules.forEach(item -> {
                String hostelId = item.getHostel().getHostelId();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                if (recurringTrackerService.canGenerateInvoice(hostelId, calendar.getTime())) {
                    applicationEventPublisher.publishEvent(new PostpaidRecurringEvents(this, hostelId));
                }

            });
        }

    }
}
