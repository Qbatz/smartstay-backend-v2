package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record AdvanceItems(String label,
                           Double availableAdvanceBalance,
                           Double appliedAmount,
                           Double paidAmount,
                           String invoiceNo,
                           List<RedeemedInfo> redeemedList) {
}
