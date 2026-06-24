package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.AmountSettled;
import com.smartstay.smartstay.dto.invoices.AppliedInvoices;

import java.util.List;

public record InvoiceInfo(String invoiceId,
        Double subTotal,
                          Double taxAmount,
                          Double taxPercentage,
                          Double totalAmount,
                          Double paidAmount,
                          Double balanceAmount,
                          Double discountAmount,
                          Double discountPercentage,
                          String invoicePeriod,
                          String invoiceMonth,
                          String paymentStatus,
                          boolean isCancelled,
                          String cancelledOn,
                          boolean isDiscounted,
                          String discountReason,
                          double totalDeduction,
                          boolean canRedeem,
                          boolean isNewPattern,
                          Double avilableAmountToRedeem,
                          boolean isAvanceAvailableForRedeem,
                          boolean canApplyToOtherInvoice,
                          Double advanceAvailableAmount,
                          List<InvoiceItems> invoiceItems,
                          List<Deductions> listDeductions,
                          AmountSettled redemptionInfo) {
}
