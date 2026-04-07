package com.smartstay.smartstay.services;

import com.smartstay.smartstay.ennum.RecurringModeEnum;
import com.smartstay.smartstay.repositories.CustomerRecurringTrackerRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerRecurringTracker {
    @Autowired
    private CustomerRecurringTrackerRepository customerRecurringTrackerRepository;
    public void addToTracker(String customerId, String hostelId, Date createdDate) {
        com.smartstay.smartstay.dao.CustomerRecurringTracker crt = new com.smartstay.smartstay.dao.CustomerRecurringTracker();
        crt.setCreatedAt(createdDate);
        crt.setHostelId(hostelId);
        crt.setCustomerId(customerId);
        crt.setCreationDay(Utils.dateToDate(createdDate));
        crt.setCreationMonth(Utils.dateToMonthAlone(createdDate));
        crt.setCreationYear(Utils.dateToYear(createdDate));
        crt.setMode(RecurringModeEnum.AUTOMATIC.name());

        customerRecurringTrackerRepository.save(crt);
    }
}
