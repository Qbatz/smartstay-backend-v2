package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.repositories.HostelPlanRepository;
import com.smartstay.smartstay.services.HostelService;
import com.smartstay.smartstay.services.KycConfigService;
import com.smartstay.smartstay.services.KycHistoryService;
import com.smartstay.smartstay.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class UpdatePlanStatusScheduler {
    @Autowired
    private HostelPlanRepository hostelPlanRepository;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private KycConfigService kycConfigService;
    @Autowired
    private KycHistoryService kycHistoryService;

    @Scheduled(cron = "0 2 0 * * *")
    public void findHostelsHavingPlanEnded() {
        List<HostelPlan> hostelPlans = hostelPlanRepository.findNotActiveHostels(new Date());
        if (hostelPlans != null) {
            List<String> hostelIds = hostelPlans
                    .stream()
                    .map(i -> {
                        HostelV1 hostelV1 = i.getHostel();
                        return hostelV1.getHostelId();
                    })
                    .toList();
            if (hostelIds != null) {
                List<String> inactiveSubscriptions = subscriptionService.findActiveSubscriptionHostels(hostelIds);
                List<KycConfig> listKycConfigs = kycConfigService.findInactiveHostels(inactiveSubscriptions);
                if (!listKycConfigs.isEmpty()) {
                    List<KycHistory> listKycHistories = kycHistoryService.updateEndDateInKycHistory(hostelIds);
                    if (listKycHistories != null && !listKycHistories.isEmpty()) {
                        List<String> hostelIdFromHistory = listKycHistories
                                .stream()
                                .map(KycHistory::getHostelId)
                                .toList();

                        List<KycConfig> newKycConfigs = listKycConfigs
                                .stream()
                                .filter(i -> hostelIdFromHistory.contains(i.getHostelId()))
                                .map(i -> {
                                    i.setCanRequest(false);
                                    return i;
                                })
                                .toList();
                        kycConfigService.saveAll(newKycConfigs);
                    }
                    else {
                        List<KycConfig> newKycConfigs = listKycConfigs
                                .stream()
                                .map(i -> {
                                    i.setCanRequest(false);
                                    return i;
                                })
                                .toList();
                        kycConfigService.saveAll(newKycConfigs);
                    }


                }
            }
        }

    }
}
