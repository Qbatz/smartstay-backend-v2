package com.smartstay.smartstay.services;


import com.smartstay.smartstay.dao.BillingRules;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return billingRuleRepository.findByHostelIdAndDate(hostelId, date);
    }
}
