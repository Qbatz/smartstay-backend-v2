package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.CustomerBillingRules;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.events.JoiningBasedPrepaidEvents;
import com.smartstay.smartstay.services.CustomerBillingRulesService;
import com.smartstay.smartstay.services.HostelConfigService;
import com.smartstay.smartstay.services.NotificationService;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JoiningBasedBillingSchedular {

    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private CustomerBillingRulesService customerBillingRulesService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
//    @Scheduled(cron = "* 30 3 * * *") for production
//@Scheduled(cron = "0 30 2 * * *") for dev
    @Scheduled(cron = "0 30 2 * * *")
    public void joiningDateInvoiceScheduler() {
        int dayFromDate = Utils.findDateFromDate(new Date());
        List<BillingRules> findListOfHostelsHavingJoiningBasedBilling = hostelConfigService.findHostelsHavingJoiningBsedInvoice();
        List<HostelV1> hostels = findListOfHostelsHavingJoiningBasedBilling
                .stream()
                .map(BillingRules::getHostel)
                .toList();
        List<String> hostelIds = hostels
                .stream()
                .map(HostelV1::getHostelId)
                .toList();

        List<CustomerBillingRules> listBilling = customerBillingRulesService.findCustomersHavingBillingToday(hostelIds, dayFromDate);

        listBilling.forEach(item -> {
            applicationEventPublisher.publishEvent(new JoiningBasedPrepaidEvents(this, item.getHostelId(), item.getCustomerId()));
        });


    }
}

