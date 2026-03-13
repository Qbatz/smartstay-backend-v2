package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ReminderSchedulers {

    @Autowired
    private BillingRuleRepository billingRuleRepository;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;

    @Scheduled(cron = "0 50 21 * * *")
    public void sendReminderNotifications() {
        List<BillingRules> remindersList = billingRuleRepository.findAllHostelsHavingReminders();
        if (remindersList != null && !remindersList.isEmpty()) {
            List<BillingRules> billingRulesHavingReminders = remindersList
                    .stream()
                    .filter(BillingRules::isShouldNotify)
                    .toList();

            List<InvoicesV1> listInvoiceshavingDue = invoicesV1Repository.findInvoicesHavingDueDate(new Date());
            if (listInvoiceshavingDue != null && !listInvoiceshavingDue.isEmpty()) {
                listInvoiceshavingDue.forEach(item -> {
                    BillingRules billingRuleForIndividualHostel = billingRulesHavingReminders
                            .stream()
                            .filter(i -> i.getHostel().getHostelId().equalsIgnoreCase(item.getHostelId()))
                            .findFirst()
                            .orElse(null);

                    if (billingRuleForIndividualHostel != null) {
                        List<Integer> reminderDays = billingRuleForIndividualHostel.getReminderDays();
                        boolean shouldSendNotify = false;
                        Date dueDate = item.getInvoiceDueDate();
                        long noOfDays = Utils.findNumberOfDays(new Date(), dueDate);

                        long findDueDateInReminderArray = reminderDays
                                .stream()
                                .filter(i -> i==noOfDays)
                                .findFirst()
                                .orElse(0);
                        if (findDueDateInReminderArray == noOfDays) {
                            System.out.println("Notify user");
                        }
                    }
                });
            }
        }
    }
}
