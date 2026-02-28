package com.smartstay.smartstay.responses.invoices;

public interface InvoiceSummary {
    String getInvoiceId();

    String getInvoiceNumber();

    Double getTotalAmount();

    String getInvoiceStartDate();

    String getInvoiceType();

}
