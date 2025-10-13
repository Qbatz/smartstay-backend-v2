package com.smartstay.smartstay.services;

import com.smartstay.smartstay.repositories.CustomerBedHistoryRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomersBedHistory {

    @Autowired
    private CustomerBedHistoryRespository customerBedHistoryRepository;
}
