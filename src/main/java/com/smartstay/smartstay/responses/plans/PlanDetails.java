package com.smartstay.smartstay.responses.plans;

import java.util.List;

public record PlanDetails(String planId,
                          String planCode,
                          String planName,
                          String subscriptionNumber,
                          long numberOfDaysRemaining,
                          boolean isReactivated,
                          String planStartDate,
                          String planEndDate,
                          double subscriptionAmount,
                          List<String> planFeatures) {
}
