package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.KycConfig;
import com.smartstay.smartstay.dto.kyc.KycRequestMessage;
import com.smartstay.smartstay.repositories.KycConfigRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KycConfigService {

    @Autowired
    private KycConfigRepository kycConfigRepository;

    public KycRequestMessage checkKycRequestConfig(String hostelId) {
        KycConfig kycConfig = kycConfigRepository.findByHostelId(hostelId);
        if (kycConfig == null) {
            return new KycRequestMessage(true, -1, null);
        }
        if (kycConfig.getCanRequest() != null && !kycConfig.getCanRequest()) {
            return new KycRequestMessage(false, 0, Utils.KYC_REQUEST_DISABLED);
        }
        if (kycConfig.getLimitPerMonth() == null) {
            return new KycRequestMessage(true, -1, null);
        }
        if (kycConfig.getLimitPerMonth() < 0) {
            return new KycRequestMessage(false, 0, Utils.KYC_REQUEST_DISABLED);
        }
        return new KycRequestMessage(true, kycConfig.getLimitPerMonth(), null);
    }

    public List<KycConfig> findInactiveHostels(List<String> inactiveSubscriptions) {
        List<KycConfig> expiredHostelIds = kycConfigRepository.findByHostelId(inactiveSubscriptions);
        if (expiredHostelIds == null) {
            return new ArrayList<>();
        }
        return expiredHostelIds;
    }

    public void saveAll(List<KycConfig> newKycConfigs) {
        kycConfigRepository.saveAll(newKycConfigs);
    }

    public void addInitialKycConfig(String hostelId) {
        KycConfig kycConfig = new KycConfig();
        kycConfig.setCanRequest(true);
        kycConfig.setHostelId(hostelId);
        kycConfig.setLimitPerMonth(15);

        kycConfigRepository.save(kycConfig);
    }
}
