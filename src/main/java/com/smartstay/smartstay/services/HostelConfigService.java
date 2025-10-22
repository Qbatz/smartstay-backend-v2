package com.smartstay.smartstay.services;


import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
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
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateJoiningDate);
        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        if (billingRules != null) {
            billDates = new BillingDates(calendar.getTime(),findEndDate);
        }
        return billDates;
    }
}
