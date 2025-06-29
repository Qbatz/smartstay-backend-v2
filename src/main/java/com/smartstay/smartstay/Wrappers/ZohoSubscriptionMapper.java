package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.responses.ZohoSubscription;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class ZohoSubscriptionMapper implements Function<ZohoSubscription, Subscription>  {

    @Override
    public Subscription apply(ZohoSubscription zohoSubscription) {
        Subscription subscription = new Subscription();
        ZohoSubscription.Subscription sub = zohoSubscription.getSubscription();
        ZohoSubscription.Plan plan = sub.getPlan();
        List<ZohoSubscription.Taxes> tax = sub.getTaxes();

        subscription.setSubscriptionId(sub.getSubscriptionId());
        subscription.setPlanCode(plan.getPlanId());
        subscription.setPlanName(plan.getPlanName());
        subscription.setPlanAmount(sub.getAmount());
        subscription.setSubTotal(sub.getSubTotal());
        subscription.setSubscriptionNumber(sub.getSubscriptionNumber());
        subscription.setStatus(sub.getStatus());
        subscription.setGst(tax.stream().mapToInt(ZohoSubscription.Taxes::getTaxAmount).sum());
        subscription.setDiscountAmount(plan.getDiscountAmount());
        subscription.setDiscount(plan.getDiscount());
        subscription.setStatus(sub.getStatus());
        subscription.setCreatedAt(Utils.stringToDate(sub.getCreatedAt()));
        subscription.setActivatedAt(Utils.stringToDate(sub.getActivatedAt()));
        subscription.setTrialStartsAt(Utils.stringToDate(sub.getTrialStartsAt()));
        subscription.setTrialEndsAt(Utils.stringToDate(sub.getTrialEndsAt()));
        subscription.setTrialRemainingDays(sub.getTrialRemainingDays());
        subscription.setNextBillingAt(Utils.stringToDate(sub.getNextBillingAt()));
        return subscription;
    }
}
