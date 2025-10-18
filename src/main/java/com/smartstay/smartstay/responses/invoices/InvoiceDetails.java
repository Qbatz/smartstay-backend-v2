package com.smartstay.smartstay.responses.invoices;

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
                             ConfigInfo configurations) {
}
