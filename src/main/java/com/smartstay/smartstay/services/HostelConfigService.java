package com.smartstay.smartstay.services;


import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HostelConfigService {

    @Autowired
    private BillingRuleRepository billingRuleRepository;

    Optional<BillingRules> getBillingRuleByIdAndHostelId(Integer id, String hostelId) {
        return billingRuleRepository.findBillingRuleByIdAndHostelId(id, hostelId);
    }

    Optional<BillingRules> getBillingRuleByHostelId(String hostelId) {
        return billingRuleRepository.findByHostel_hostelId(hostelId);
    }



    public void saveBillingRule(BillingRules billingRule) {
        billingRuleRepository.save(billingRule);
    }


    public void updateExistingBillRule(BillingRules latestBillingRules) {
        billingRuleRepository.save(latestBillingRules);
    }

    public BillingRules getLatestBillRuleByHostelIdAndStartDate(String hostelId, Date date) {
        return billingRuleRepository.findByHostelIdAndStartDate(hostelId, date);
    }

    public BillingRules getNewBillRuleByHostelIdAndStartDate(String hostelId, Date date) {
        return billingRuleRepository.findNewRuleByHostelIdAndDate(hostelId, date);
    }

    public BillingRules getCurrentMonthTemplate(String hostelId) {
        return billingRuleRepository.findLatestBillingRule(hostelId, new Date());
    }

    public BillingDates getBillingRuleByDateAndHostelId(String hostelId, Date dateJoiningDate) {
        BillingRules billingRules = billingRuleRepository.findBillingRulesOnDateAndHostelId(hostelId, dateJoiningDate);
        BillingDates billDates = null;

        int billStartDate = 1;
        int billingRuleDueDate = 5;
        int billMonth;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateJoiningDate);
        billMonth = calendar.get(Calendar.MONTH);

        if (billingRules != null) {
            if (billingRules.isInitial()) {
                List<BillingRules> listBillingRulesExceptInitial = billingRuleRepository.findAllBillingRulesByHostelIdExceptInitial(hostelId);
                if (!listBillingRulesExceptInitial.isEmpty()) {
                    billingRules = listBillingRulesExceptInitial.get(0);
                }
            }
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
        }


        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        if (Utils.compareWithTwoDates(dateJoiningDate, calendar.getTime()) < 0) {
            billMonth = billMonth - 1;
        }
        calendar.set(Calendar.MONTH, billMonth);

//        Calendar calendarDueDate = Calendar.getInstance();
//        calendarDueDate.set(Calendar.DAY_OF_MONTH, billingRuleDate);

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        if (billingRules != null) {
            billDates = new BillingDates(calendar.getTime(),findEndDate, dueDate, billingRuleDueDate);
        }
        return billDates;
    }

    public List<BillingRules> findAllHostelsHavingBillingToday() {
        Date date = new Date();
        String day = Utils.getDayFromDate(date);

        return billingRuleRepository.findAllHostelsHavingTodaysRecurring(day, date);
    }

    public BillingDates getNextMonthBillingDates(String hostelId) {

        BillingRules billingRules = billingRuleRepository.findNextBillingDates(hostelId, new Date());
        BillingDates billingDates = null;
        if (billingRules != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, billingRules.getBillingStartDate());

            Date findEndDate = Utils.findLastDate(billingRules.getBillingStartDate(), cal.getTime());
            Date dueDate = Utils.addDaysToDate(cal.getTime(), billingRules.getBillDueDays());

            billingDates = new BillingDates(cal.getTime(), findEndDate, dueDate, billingRules.getBillDueDays());
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            Date findEndDate = Utils.findLastDate(1, cal.getTime());
            Date dueDate = Utils.addDaysToDate(cal.getTime(), 10);

            billingDates = new BillingDates(cal.getTime(), findEndDate, dueDate, 10);
        }


        return billingDates;
    }
}
