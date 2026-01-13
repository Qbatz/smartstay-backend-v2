package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.List;

public record InitializeRefund(
        String roomName,
        String floorName,
        String bedName,
        Double refundableAmount,
        Double refundedAmount,
        Double pendingRefund,
        String invoiceDate,
        List<RefundableBanks> listBanks) {
}
