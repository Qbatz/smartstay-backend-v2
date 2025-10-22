package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.BedHistoryCustomerListMapper;
import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.repositories.CustomerBedHistoryRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CustomersBedHistoryService {

    @Autowired
    private CustomerBedHistoryRespository customerBedHistoryRepository;

    public CustomersBedHistory getCustomerBedByStartDate(String customerId, Date startDate, Date endDate) {
        return customerBedHistoryRepository.findByCustomerIdAndDate(customerId, startDate, endDate);
    }

    public List<CustomerBedsList> getAllCustomerFromBedsHistory(String hostelId, Date billStartDate, Date billEndDate) {
        return customerBedHistoryRepository.findByHostelIdAndStartAndEndDate(hostelId, billStartDate, billEndDate)
                .stream()
                .map(item -> new BedHistoryCustomerListMapper().apply(item))
                .toList();
    }

    public CustomersBedHistory getLatestCustomerBed(String customerId) {
        return customerBedHistoryRepository.findTopByCustomerIdOrderByCreatedAtDesc(customerId).orElse(null);
    }
}
