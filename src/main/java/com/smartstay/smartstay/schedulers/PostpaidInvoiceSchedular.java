package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.events.PostpaidRecurringEvents;
import com.smartstay.smartstay.services.HostelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostpaidInvoiceSchedular {
    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 58 20 * * *")
    public void scheduleInvoice() {
        List<BillingRules> listBillingRules = hostelConfigService.findHostelsHavingBillingStartDateTomorrow();

        if (!listBillingRules.isEmpty()) {
            List<HostelV1> listHostels = listBillingRules
                    .stream()
                    .map(BillingRules::getHostel)
                    .toList();

            listBillingRules.forEach(item -> {
                applicationEventPublisher.publishEvent(new PostpaidRecurringEvents(this, item.getHostel().getHostelId()));
            });
        }

    }
}
