package com.smartstay.smartstay.payloads.retainer;

import java.util.List;

public record RedeemAmount(Double appliedAmount,
                           String redeemedOn,
                           String comments,
                           List<RedeemInvoice> retainersBreakup) {
}
