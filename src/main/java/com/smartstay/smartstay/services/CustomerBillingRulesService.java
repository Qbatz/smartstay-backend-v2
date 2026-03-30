package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.CustomerBillingRules;
import com.smartstay.smartstay.repositories.CustomerBillingRulesRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomerBillingRulesService {
    @Autowired
    private CustomerBillingRulesRepository customerBillingRulesRepository;

    public void addCustomerBillingRule(String customerId, String hostelId, Date joiningDate) {
        CustomerBillingRules cbr = new CustomerBillingRules();
        cbr.setBillingDay(Utils.dateToDate(joiningDate));
        cbr.setCustomerId(customerId);
        cbr.setHostelId(hostelId);
        cbr.setIsActive(true);
        cbr.setCreatedAt(new Date());

        customerBillingRulesRepository.save(cbr);
    }

    public List<CustomerBillingRules> findCustomersHavingBillingToday(List<String> hostelIds, int dayFromDate) {
        List<CustomerBillingRules> listCustomerBillingRules = customerBillingRulesRepository.findCustomersHavingBillingToday(hostelIds, dayFromDate);
        if (listCustomerBillingRules == null) {
            listCustomerBillingRules = new ArrayList<>();
        }

        return listCustomerBillingRules;
    }

    public void updateRecurringInvoiceDate(String hostelId, String customerId, Date joinigDate) {
        CustomerBillingRules customerBillingRules = customerBillingRulesRepository.findByHostelIdAndCustomerId(hostelId, customerId);
        customerBillingRules.setBillingDay(Utils.dateToDate(joinigDate));
        customerBillingRulesRepository.save(customerBillingRules);
    }
}
