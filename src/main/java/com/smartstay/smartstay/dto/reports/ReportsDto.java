package com.smartstay.smartstay.dto.reports;

public record ReportsDto(Double paidAmount,
                         Double outstandingAmount,
                         Double totalAmount,
                         int invoiceCount) {
}
