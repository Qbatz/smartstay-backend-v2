package com.smartstay.smartstay.dto.transaction;

import java.util.Date;

public interface Receipts {
    String getInvoiceId();
    String getInvoiceNumber();
    String getInvoiceMode();
    String getInvoiceType();
    String getPaymentStatus();
    Double getPaidAmount();
    Date getPaidAt();
    String getCustomerId();
    String getTransactionId();
    String getFirstName();
    String getLastName();
    String getReferenceNumber();
}
