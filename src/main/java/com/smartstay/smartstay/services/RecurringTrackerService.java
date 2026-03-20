package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RecurringTracker;
import com.smartstay.smartstay.ennum.RecurringModeEnum;
import com.smartstay.smartstay.repositories.RecurringTrackerRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class RecurringTrackerService {

    @Autowired
    private RecurringTrackerRepository recurringTrackerRepository;

    public void markAsInvoiceGenerated(String hostelId) {
        Calendar calendar = Calendar.getInstance();

        int billingDay = Utils.findDateFromDate(calendar.getTime());
        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(new Date());
        rt.setMode(RecurringModeEnum.AUTOMATIC.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreationYear(Utils.dateToYear(calendar.getTime()));
        rt.setCreationMonth(Utils.dateToYear(calendar.getTime()));

        recurringTrackerRepository.save(rt);


    }
}
