package com.smartstay.smartstay.responses.receipt;

import com.smartstay.smartstay.responses.invoices.AccountDetails;
import com.smartstay.smartstay.responses.invoices.CustomerInfo;
import com.smartstay.smartstay.responses.invoices.StayInfo;

public record ReceiptDetails(
        String invoiceNumber,
        String invoiceDate,
        String invoiceId,
        Double invoiceAmount,
        Double paidAmount,
        Double dueAmount,
        String emailId,
        String mobile,
        String countryCode,
        ReceiptInfo receiptInfo,
        CustomerInfo customerInfo,
        StayInfo stayInfo,
        AccountDetails accountDetails,
        ReceiptConfigInfo configurations
) {
}
