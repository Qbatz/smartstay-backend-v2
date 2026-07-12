package com.smartstay.smartstay.responses.invoices;

public record InvoiceSummaryInfo(Integer totalInvoices,
                                 Double collectedThisMonth,
                                 Double todaysDue,
                                 Double overDueAmount,
                                 Double outstandingAmount,
                                 Double totalAmount,
                                 Double settlementValue) {
}
