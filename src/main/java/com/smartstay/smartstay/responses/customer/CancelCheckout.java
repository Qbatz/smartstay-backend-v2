package com.smartstay.smartstay.responses.customer;

public record CancelCheckout(String customerId,
                             String hostelId,
                             boolean canCancel,
                             String joiningDate,
                             String latestBedChange,
                             boolean isBedChanged,
                             String noticeStartedFrom,
                             String requestedCheckoutDate,
                             boolean canRecheckinSameBed,
                             boolean isSettlementPaid) {
}
