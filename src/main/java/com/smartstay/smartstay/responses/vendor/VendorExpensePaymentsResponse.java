package com.smartstay.smartstay.responses.vendor;

import java.util.List;

public record VendorExpensePaymentsResponse(
        long totalPayments,
        int currentPage,
        int totalPages,
        int itemPerPage,
        List<VendorExpensePaymentResponse> payments) {
}
