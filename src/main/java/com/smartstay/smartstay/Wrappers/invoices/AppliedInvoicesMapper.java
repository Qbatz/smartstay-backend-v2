package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.InvoiceRedemption;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.AppliedInvoices;

import java.util.List;
import java.util.function.Function;

public class AppliedInvoicesMapper implements Function<com.smartstay.smartstay.dao.InvoiceRedemption, AppliedInvoices> {

    List<InvoicesV1> listInvoices = null;

    public AppliedInvoicesMapper(List<InvoicesV1> listInvoices) {
        this.listInvoices = listInvoices;
    }

    @Override
    public AppliedInvoices apply(InvoiceRedemption invoiceRedemption) {
        String invoiceNo = null;
        if (listInvoices != null) {
            InvoicesV1 invoicesV1 = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(invoiceRedemption.getSourceInvoiceId()))
                    .findFirst()
                    .orElse(null);

            if (invoicesV1 != null) {
                invoiceNo = invoicesV1.getInvoiceNumber();
            }
        }

        return new AppliedInvoices(invoiceRedemption.getSourceInvoiceId(),
                invoiceNo,
                invoiceRedemption.getRedemptionAmount());
    }
}
