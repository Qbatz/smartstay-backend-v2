package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.invoice.ItemResponse;

import java.text.SimpleDateFormat;
import java.util.List;

public class InvoiceMapper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static InvoiceResponse toResponse(InvoicesV1 invoice) {
        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getInvoiceNumber(),
                capitalize(invoice.getInvoiceType()),
                capitalize(invoice.getPaymentStatus()),
                invoice.getTotalAmount(),
                invoice.getInvoiceGeneratedDate() != null
                        ? dateFormat.format(invoice.getInvoiceGeneratedDate())
                        : null,
                invoice.getInvoiceItems() != null
                        ? invoice.getInvoiceItems().stream()
                        .map(item -> new ItemResponse(
                                item.getInvoiceItem() != null && item.getInvoiceItem().equalsIgnoreCase("EB")
                                        ? "EB"
                                        : capitalize(item.getInvoiceItem()),
                                item.getAmount()))
                        .toList()
                        : List.of()
        );
    }


    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}

