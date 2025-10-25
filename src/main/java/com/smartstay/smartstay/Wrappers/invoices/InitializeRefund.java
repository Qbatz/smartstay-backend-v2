package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.List;

public record InitializeRefund(Double refundableAmount,
                               List<RefundableBanks> listBanks) {
}
