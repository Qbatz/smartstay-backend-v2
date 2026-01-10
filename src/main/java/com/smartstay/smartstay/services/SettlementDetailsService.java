package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.SettlementDetails;
import com.smartstay.smartstay.repositories.SettlementDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SettlementDetailsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private SettlementDetailsRepository settlementDetailsRepository;

    public void addSettlementForCustomer(String customerId, Date leavingDate) {
        if (authentication.isAuthenticated()) {
            SettlementDetails settlementDetails = settlementDetailsRepository.findByCustomerId(customerId);
            if (settlementDetails == null) {
                settlementDetails = new SettlementDetails();
                settlementDetails.setCustomerId(customerId);
                settlementDetails.setLeavingDate(leavingDate);
                settlementDetails.setCreatedAt(new Date());
                settlementDetails.setCreatedBy(authentication.getName());
                settlementDetailsRepository.save(settlementDetails);
            }
            else {
                settlementDetails.setLeavingDate(leavingDate);
                settlementDetails.setCreatedAt(new Date());
                settlementDetails.setCreatedBy(authentication.getName());
                settlementDetailsRepository.save(settlementDetails);
            }
        }
    }

    public SettlementDetails getSettlmentInfoForCustomer(String customerId) {
        return settlementDetailsRepository.findByCustomerId(customerId);
    }
}
