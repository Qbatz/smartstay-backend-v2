package com.smartstay.smartstay.dto.invoices;

import java.util.Date;

public interface Invoices {
    String getInvoiceId();
    Double getInvoiceAmount();
    Double getBasePrice();
    Double getTotalAmount();
    Double getGst();
    Double getCgst();
    Double getSgst();
    Date getCreatedAt();
    String getCreatedBy();
    String getCustomerId();
    String getHostelId();
    Date getInvoiceGeneratedAt();
    Date getInvoiceDueDate();
    String getInvoiceType();
    String getPaymentStatus();
    Date getUpdatedAt();
    String getInvoiceNumber();
    String getFirstName();
    String getLastName();
    Double getAdvanceAmount();
    String getDeductions();
    Double getPaidAmount();
    Date getInvoiceStartDate();

}
