package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.reminders.DueReminders;
import com.smartstay.smartstay.events.ReminderEvents;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.services.CustomerNotificationService;
import com.smartstay.smartstay.services.CustomersService;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class RemindersListener {

    @Autowired
    private BillingRuleRepository billingRuleRepository;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private CustomerNotificationService customerNotificationService;
    @Async
    @EventListener
    public void notifyReminders(ReminderEvents reminderEvents) {
        List<BillingRules> remindersList = billingRuleRepository.findAllHostelsHavingReminders();
        HashMap<String, DueReminders> customerReminders = new HashMap<>();
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
                        long noOfDays = Utils.findNumberOfDays(new Date(), dueDate)-1;

                        long findDueDateInReminderArray = reminderDays
                                .stream()
                                .filter(i -> i==noOfDays)
                                .findFirst()
                                .orElse(0);
                        if (findDueDateInReminderArray == noOfDays) {
                            if (!customerReminders.containsKey(item.getCustomerId())) {
                                Double dueAmount = 0.0;
                                Double totalAmount = 0.0;
                                if (item.getTotalAmount() != null) {
                                    totalAmount = item.getTotalAmount();
                                }
                                if (item.getPaidAmount() != null) {
                                    dueAmount = totalAmount - item.getPaidAmount();
                                }
                                else {
                                    dueAmount = totalAmount;
                                }
                                DueReminders dueReminders = new DueReminders(item.getCustomerId(),
                                        item.getInvoiceId(),
                                        dueAmount,
                                        totalAmount,
                                        item.getInvoiceNumber());
                                customerReminders.put(item.getCustomerId(), dueReminders);
                            }
                        }
                    }
                });

                List<String> customerIds = customerReminders.keySet().stream().toList();
                List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);

                customerNotificationService.notifyReminders(listCustomers, customerReminders);
            }
        }
    }
}
