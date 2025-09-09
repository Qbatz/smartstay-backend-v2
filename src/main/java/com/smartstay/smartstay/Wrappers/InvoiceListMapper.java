package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class InvoiceListMapper implements Function<Invoices, InvoicesList> {
    @Override
    public InvoicesList apply(Invoices invoices) {
        StringBuilder fullNameBuilder = new StringBuilder();
        fullNameBuilder.append(invoices.getFirstName());
        fullNameBuilder.append(" ");
        fullNameBuilder.append(invoices.getLastName());


        return new InvoicesList(invoices.getFirstName(),
                invoices.getLastName(),
                fullNameBuilder.toString(),
                invoices.getCustomerId(),
                invoices.getInvoiceAmount(),
                invoices.getInvoiceId(),
                invoices.getCgst(),
                invoices.getSgst(),
                invoices.getGst(),
                Utils.dateToString(invoices.getCreatedAt()),
                invoices.getCreatedBy(),
                invoices.getHostelId(),
                Utils.dateToString(invoices.getInvoiceGeneratedAt()),
                Utils.dateToString(invoices.getInvoiceDueDate()),
                invoices.getInvoiceType(),
                invoices.getPaymentStatus(),
                Utils.dateToString(invoices.getUpdatedAt()),
                invoices.getInvoiceNumber(),
                null);
    }
}
