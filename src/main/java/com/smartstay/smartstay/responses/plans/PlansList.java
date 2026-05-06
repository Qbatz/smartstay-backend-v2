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
                        Double CGST,
                        Double SGST,
                        Double cGSTAmount,
                        Double sGSTAmount,
                        Double finalPrice,
                        List<String> features) {
}
