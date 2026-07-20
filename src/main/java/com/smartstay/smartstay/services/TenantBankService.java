package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.TenantBanking;
import com.smartstay.smartstay.repositories.TenantBankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TenantBankService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private TenantBankingRepository tenantBankingRepository;

    public Double addTransactionToTenantBank(Date paymentDate, Double amount, String customerId, String hostelId) {
        TenantBanking tenantBanking = tenantBankingRepository.findByCustomerId(customerId);
        double existingBalance = 0.0;
        if (tenantBanking == null) {
            tenantBanking = new TenantBanking();
        }
        else {
            existingBalance = tenantBanking.getAmount();
        }
        tenantBanking.setCustomerId(customerId);
        tenantBanking.setHostelId(hostelId);
        tenantBanking.setAmount(existingBalance + amount);
        tenantBanking.setLastUpdate(paymentDate);

        tenantBankingRepository.save(tenantBanking);

        return existingBalance + amount;
    }
}
