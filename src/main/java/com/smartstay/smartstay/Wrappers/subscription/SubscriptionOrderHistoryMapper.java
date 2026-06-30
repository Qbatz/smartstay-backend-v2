package com.smartstay.smartstay.Wrappers.subscription;

import com.smartstay.smartstay.dao.OrderHistory;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.responses.plans.BillingHistoryItem;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SubscriptionOrderHistoryMapper implements Function<Subscription, BillingHistoryItem> {
    List<OrderHistory> listOrderHistory = null;
    Map<String, Users> paidByUserMap = null;
    private List<Plans> listPlans = null;
    public SubscriptionOrderHistoryMapper(List<OrderHistory> listOrderHistory, Map<String, Users> usersMap, List<Plans> listPlans) {
        this.listOrderHistory = listOrderHistory;
        this.paidByUserMap = usersMap;
        this.listPlans = listPlans;
    }

    @Override
    public BillingHistoryItem apply(Subscription subscription) {
        OrderHistory oh;
        String paymentMethod = null;
        String subscriptionNo = null;
        String paidById = null;
        String fullName = null;
        String subscriptionStatus = "Active";

        String paidByName = "";
        String planName = null;


        if (listOrderHistory != null) {
            oh = listOrderHistory
                    .stream()
                    .filter(i -> subscription.getOrderId() != null && subscription.getOrderId().equals(i.getHistoryId()))
                    .findFirst()
                    .orElse(null);

        } else {
            oh = null;
        }

        if (oh != null) {
            paymentMethod = Utils.resolvePaymentMethod(oh);
            subscriptionNo = subscription.getSubscriptionNumber();
            paidById = oh.getPaidBy();

            if (paidById != null && paidByUserMap.containsKey(paidById)) {
                Users paidByUser = paidByUserMap.get(paidById);
                String firstName = paidByUser.getFirstName() != null ? paidByUser.getFirstName() : "";
                String lastName = paidByUser.getLastName() != null ? paidByUser.getLastName() : "";
                fullName = (firstName + " " + lastName).trim();
            }

            Plans plans1 = listPlans.stream()
                    .filter(i -> i.getPlanCode().equalsIgnoreCase(oh.getPlanCode()))
                    .findFirst()
                    .orElse(null);
            if (plans1 != null) {
                planName = plans1.getPlanName();
            }

        }



        return new BillingHistoryItem(
                subscription.getSubscriptionId(),
                subscriptionNo,
                planName,
                subscription.getPlanCode(),
                subscription.getPlanAmount(),
                subscription.getDiscountAmount(),
                subscription.getPaidAmount(),
                subscriptionStatus,
                paymentMethod,
                paymentMethod,
                paidById,
                paidByName,
                Utils.dateToString(subscription.getCreatedAt())
        );
    }
}
