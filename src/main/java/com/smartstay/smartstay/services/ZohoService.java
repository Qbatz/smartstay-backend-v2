package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ZohoService {


    @Autowired
    SubscriptionRepository subscriptionRepository;

    public boolean isSubscriptionValid(String subscriptionId, String hostelId) {
        Optional<Subscription> subscription = subscriptionRepository.findBySubscriptionIdAndHostel_HostelId(subscriptionId, hostelId);

        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            Date currentDate = new Date();
            return sub.getActivatedAt() != null &&
                    sub.getTrialEndsAt() != null &&
                    currentDate.before(sub.getTrialEndsAt());
        }

        return false;
    }
}
