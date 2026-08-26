package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.KYCUsage;
import com.smartstay.smartstay.repositories.KYCUsageRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class KYCUsageService {
    @Autowired
    private KYCUsageRepositories kycUsageRepositories;
    @Autowired
    private Authentication authentication;

    public void updateRequestCountAndCustomerId(String customerId, String hostelId) {
        KYCUsage kycUsage = kycUsageRepositories.findByHostelId(hostelId);
        if (kycUsage == null) {
            kycUsage = new KYCUsage();
            kycUsage.setHostelId(hostelId);
            kycUsage.setRequestCount(1);
        }
        else {
            Integer count = kycUsage.getRequestCount();
            if (count == null) {
                count = 1;
            }
            else {
                count = count + 1;
            }
            kycUsage.setRequestCount(count);
        }
        kycUsage.setLatestRequest(new Date());
        kycUsage.setLatestRequestTo(customerId);
        kycUsage.setLatestRequestBy(authentication.getName());

        kycUsageRepositories.save(kycUsage);

    }
}
