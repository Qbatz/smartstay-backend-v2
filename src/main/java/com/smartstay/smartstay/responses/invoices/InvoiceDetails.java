package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;

import java.util.List;

public record InvoiceDetails(String invoiceNumber,
                             String invoiceId,
                             String invoiceDate,
                             String dueDate,
                             String emailId,
                             String mobile,
                             String countryCode,
                             CustomerInfo customerInfo,
                             StayInfo stayInfo,
                             InvoiceInfo invoiceInfo,
                             AccountDetails accountDetails,
                             List<PaymentHistoryProjection> paymentHistory,
                             ConfigInfo configurations) {
}
