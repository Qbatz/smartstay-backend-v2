package com.smartstay.smartstay.responses.dashboard;

public record BillingSummary(Integer totalInvoiceGenerated, Double totalPaid, Double totalPending) {
}
