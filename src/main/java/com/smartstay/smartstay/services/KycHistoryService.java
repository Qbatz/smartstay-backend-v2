package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.KycHistory;
import com.smartstay.smartstay.repositories.KycHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class KycHistoryService {

    @Autowired
    private KycHistoryRepository kycHistoryRepository;
    public List<KycHistory> updateEndDateInKycHistory(List<String> hostelIds) {
        List<KycHistory> kycHistories = kycHistoryRepository.findByHostelsById(hostelIds, new Date());
        if (kycHistories != null && !kycHistories.isEmpty()) {
            List<KycHistory> updatedKycHistory = kycHistories
                    .stream()
                    .map(i -> {
                        i.setEndDate(new Date());
                        i.setIsCancelledDueToPlan(true);
                        i.setCancellationReason("Cancelled due to plan");
                        return i;
                    })
                    .toList();
            kycHistoryRepository.saveAll(updatedKycHistory);
            return kycHistories;
        }
        return new ArrayList<>();
    }

    public void addInitialHistory(String hostelId) {
        KycHistory kycHistory = new KycHistory();
        kycHistory.setHostelId(hostelId);
        kycHistory.setStartDate(new Date());
        kycHistory.setActivationReason("Welcome Benefit");
        kycHistory.setCreatedAt(new Date());
        kycHistoryRepository.save(kycHistory);
    }
}
