package com.smartstay.smartstay.responses.plans;

import java.util.List;

public record PlansList(String planName,
                        Long planId,
                        Double price,
                        Double discounts,
                        Double discountedPrice,
                        String planCode,
                        boolean canCustomize,
                        String frequency,
                        List<String> features) {
}
