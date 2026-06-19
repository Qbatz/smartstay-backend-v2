package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.List;

public record VendorInitialize(
        String hostelId,
        String vendorId,
        List<DebitsBank> banks,
        List<VendorExpenseSummary> expenses) {
}
