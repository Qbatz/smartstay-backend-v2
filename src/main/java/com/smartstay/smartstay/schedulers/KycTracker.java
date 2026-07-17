package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.KycDetails;
import com.smartstay.smartstay.services.KycServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KycTracker {
    @Autowired
    private KycServices kycService;

//    @Scheduled(cron = "0 31 9 * * *")
//    @Scheduled(fixedRate = 150 * 60 * 1000)
    @Scheduled(fixedRate = 150 * 60 * 1000)
    public void getAllRequestWithRequestedState() {
        List<KycDetails> listKyc = kycService.getAllRequestWithRequestedState();
        if (!listKyc.isEmpty()) {
           makeAPICall(listKyc, 1);
        }
    }

    private void makeAPICall(List<KycDetails> listKyc, int count) {
        if (!listKyc.isEmpty()) {
            if (count < listKyc.size()) {
                kycService.verifyKycStatusAndUpdate(listKyc.get(count-1));
                count = count + 1;
                if (count % 3 == 0) {
                    try {
                        Thread.sleep(2000);
                        makeAPICall(listKyc, count);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    makeAPICall(listKyc, count);
                }


            }
        }
    }
}
