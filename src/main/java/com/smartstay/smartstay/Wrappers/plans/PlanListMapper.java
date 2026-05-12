package com.smartstay.smartstay.Wrappers.plans;

import com.smartstay.smartstay.dao.PlanFeatures;
import com.smartstay.smartstay.dao.Plans;
import com.smartstay.smartstay.responses.plans.PlansList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class PlanListMapper implements Function<Plans, PlansList> {
    @Override
    public PlansList apply(Plans plans) {
        Double discount = 0.0;
        Double discountedPrice = 0.0;

        List<String> features = plans.getFeaturesList()
                .stream()
                .map(PlanFeatures::getFeatureName)
                .toList();

        return new PlansList(plans.getPlanName(),
                plans.getPlanId(),
                Utils.roundOffWithTwoDigit(plans.getPrice()),
                Utils.roundOffWithTwoDigit(discount),
                Utils.roundOffWithTwoDigit(discountedPrice),
                plans.getPlanCode(),
                plans.isCanCustomize(),
                "Monthly",
                Utils.roundOffWithTwoDigit(plans.getCgst()),
                Utils.roundOffWithTwoDigit(plans.getSgst()),
                Utils.roundOffWithTwoDigit(plans.getCgstAmount()),
                Utils.roundOffWithTwoDigit(plans.getSgstAmount()),
                Utils.roundOffWithTwoDigit(plans.getFinalPrice()),
                features);
    }
}
