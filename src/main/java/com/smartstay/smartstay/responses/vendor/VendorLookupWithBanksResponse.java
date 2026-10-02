package com.smartstay.smartstay.responses.vendor;

import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.List;

public record VendorLookupWithBanksResponse(
        List<VendorLookupResponse> vendors,
        List<DebitsBank> banks) {
}
