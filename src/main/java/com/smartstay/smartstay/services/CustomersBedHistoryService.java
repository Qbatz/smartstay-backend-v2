package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.repositories.CustomerBedHistoryRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomersBedHistoryService {

    @Autowired
    private CustomerBedHistoryRespository customerBedHistoryRepository;

    public CustomersBedHistory getCustomerBedByStartDate(String customerId, Date startDate, Date endDate) {
        return customerBedHistoryRepository.findByCustomerIdAndDate(customerId, startDate, endDate);
    }
}
