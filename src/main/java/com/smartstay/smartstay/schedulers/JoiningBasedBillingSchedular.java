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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    @Scheduled(cron = "0 30 3 * * *")
    public void joiningDateInvoiceScheduler() {
        int dayFromDate = Utils.findDateFromDate(new Date());
//        int dayFromDate = 29;
        List<BillingRules> findListOfHostelsHavingJoiningBasedBilling = hostelConfigService.findHostelsHavingJoiningBsedInvoice();
        List<HostelV1> hostels = findListOfHostelsHavingJoiningBasedBilling
                .stream()
                .map(BillingRules::getHostel)
                .toList();
        List<String> hostelIds = hostels
                .stream()
                .map(HostelV1::getHostelId)
                .toList();
        int lastDayOfCurrentMonth = Utils.findLastDate(new Date());

//        int lastDayOfCurrentMonth = 29;
        List<Integer> billingDays = new ArrayList<>();
        if (dayFromDate == 28) {
            if (lastDayOfCurrentMonth == 28) {
                for (int i = 28; i<=31; i++) {
                    billingDays.add(i);
                }
            }
            else {
                billingDays.add(dayFromDate);
            }
        }
        else if (dayFromDate == 29) {
            if (lastDayOfCurrentMonth == 29) {
                for (int i = 29; i<=31; i++) {
                    billingDays.add(i);
                }
            }
            else {
                billingDays.add(dayFromDate);
            }
        }
        else if (dayFromDate == 30) {
            if (lastDayOfCurrentMonth == 30) {
                for (int i = 30; i<=31; i++) {
                    billingDays.add(i);
                }
            }
            else {
                billingDays.add(dayFromDate);
            }
        }
        else {
            billingDays.add(dayFromDate);
        }

        List<CustomerBillingRules> listBilling = customerBillingRulesService.findCustomersHavingBillingToday(hostelIds, billingDays);

        listBilling.forEach(item -> {
            applicationEventPublisher.publishEvent(new JoiningBasedPrepaidEvents(this, item.getHostelId(), item.getCustomerId()));
        });


    }
}

