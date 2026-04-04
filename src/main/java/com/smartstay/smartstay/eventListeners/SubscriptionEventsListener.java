package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.events.SubscriptionEvents;
import com.smartstay.smartstay.services.HostelService;
import com.smartstay.smartstay.services.PlansService;
import com.smartstay.smartstay.services.SubscriptionService;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SubscriptionEventsListener {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private HostelService hostelService;

    @Async
    @EventListener
    public void subscribe(SubscriptionEvents subscriptionEvents) {
        OrderHistory oh = subscriptionEvents.getOrderHistory();

        Plans plan = plansService.findPlanByPlanCode(oh.getPlanCode());
        boolean shouldActivateToday = false;

        Subscription runningSubscription = subscriptionService.findLatestSubscription(oh.getHostelId());
        Date startDate = runningSubscription.getPlanEndsAt();

        if (Utils.compareWithTwoDates(runningSubscription.getPlanEndsAt(), new Date()) < 0) {
            startDate = new Date();
            shouldActivateToday = true;
        }

        Date endDate = Utils.addDaysToDate(startDate,  plan.getDuration().intValue());
        double discountPercentage = (oh.getDiscountAmount()/oh.getPlanAmount()) * 100;
        Subscription subscription = new Subscription();
        subscription.setSubscriptionNumber("ABCD1234");
        subscription.setHostelId(oh.getHostelId());
        subscription.setPlanCode(oh.getPlanCode());
        subscription.setPlanName(plan.getPlanName());
        subscription.setPlanStartsAt(startDate);
        subscription.setPaidAmount(oh.getTotalAmount());
        subscription.setPlanAmount(oh.getPlanAmount());
        subscription.setDiscount(oh.getDiscountAmount());
        subscription.setDiscountAmount(discountPercentage);
        subscription.setCreatedBy(subscriptionEvents.getCreatedBy());
        subscription.setCreatedByUserType(UserType.OWNER.name());
        subscription.setCreatedAt(new Date());
        subscription.setPlanEndsAt(endDate);
        subscription.setNextBillingAt(endDate);
        subscription.setActivatedAt(startDate);

        subscription.setIsActive(true);
        subscriptionService.saveFromEvents(subscription);

        if (shouldActivateToday) {
            HostelV1 hostelV1 = hostelService.getHostelInfo(oh.getHostelId());
            if (hostelV1 != null) {
                HostelPlan hostelPlan = hostelV1.getHostelPlan();
                if (hostelPlan == null) {
                    hostelPlan = new HostelPlan();
                    hostelPlan.setCurrentPlanCode(subscription.getPlanCode());
                    hostelPlan.setCurrentPlanName(subscription.getPlanName());
                    hostelPlan.setHostel(hostelV1);
                }
                hostelPlan.setCurrentPlanStartsAt(subscription.getPlanStartsAt());
                hostelPlan.setCurrentPlanEndsAt(subscription.getPlanEndsAt());
                hostelPlan.setCurrentPlanPrice(oh.getPlanAmount());
                hostelPlan.setPaidAmount(oh.getTotalAmount());
                hostelPlan.setTrial(true);
                hostelPlan.setTrialEndingAt(subscription.getPlanEndsAt());

                hostelService.updateHostel(hostelV1);
            }
        }
    }
}
