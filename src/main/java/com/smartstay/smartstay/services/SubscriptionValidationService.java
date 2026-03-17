package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SubscriptionValidationService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public boolean validateSubscription(String hostelId) {
        SubscriptionDto subscriptionDto = getCurrentSubscriptionDetails(hostelId);
        return subscriptionDto != null && subscriptionDto.isValid();
    }

    public SubscriptionDto getCurrentSubscriptionDetails(String hostelId) {
        Subscription subscriptionToday = subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
        SubscriptionDto subscriptionDto = null;
        if (subscriptionToday != null) {
            boolean isValid = false;
            int planEndsIn = 0;

            Date nextBillingDate = Utils.addDaysToDate(subscriptionToday.getPlanEndsAt(), 1);

            if (Utils.compareWithTwoDates(subscriptionToday.getPlanStartsAt(), new Date()) <= 0) {
                if (Utils.compareWithTwoDates(subscriptionToday.getPlanEndsAt(), new Date()) >= 0) {
                    isValid = true;
                }
            }
            if (isValid) {
                long numberOfDays = Utils.findNumberOfDays(new Date(), subscriptionToday.getPlanEndsAt());
                planEndsIn = (int) numberOfDays;
            }
            subscriptionDto = new SubscriptionDto(subscriptionToday.getPlanStartsAt(),
                    subscriptionToday.getPlanEndsAt(),
                    nextBillingDate,
                    isValid,
                    planEndsIn);
        }

        return subscriptionDto;
    }
}
