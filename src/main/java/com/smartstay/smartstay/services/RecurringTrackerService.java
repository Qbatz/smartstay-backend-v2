package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RecurringTracker;
import com.smartstay.smartstay.ennum.RecurringModeEnum;
import com.smartstay.smartstay.repositories.RecurringTrackerRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        rt.setCreationMonth(Utils.dateToMonthAlone(calendar.getTime()));

        recurringTrackerRepository.save(rt);


    }

    public void markAsPostpaidInvoiceGenerated(String hostelId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        int billingDay = Utils.findDateFromDate(calendar.getTime());
        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(new Date());
        rt.setMode(RecurringModeEnum.AUTOMATIC.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreationYear(Utils.dateToYear(calendar.getTime()));
        rt.setCreationMonth(Utils.dateToMonthAlone(calendar.getTime()));

        recurringTrackerRepository.save(rt);


    }

    public boolean canGenerateInvoice(String hostelId, Date date) {
        List<RecurringTracker> listRecurringForHostel = recurringTrackerRepository.findInvoiceGenerateForAMonth(hostelId, Utils.dateToMonthAlone(date), Utils.dateToYear(date));
        if (listRecurringForHostel == null) {
            return true;
        }
        else if (listRecurringForHostel.isEmpty()) {
            return true;
        }
        return false;
    }
}
