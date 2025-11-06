package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.invoice.ItemResponse;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.util.Utils;

import java.text.SimpleDateFormat;
import java.util.List;

public class InvoiceMapper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static InvoiceResponse toResponse(InvoicesV1 invoice, InvoicesV1Repository invoicesV1Repository) {
        Double paidAmount = invoicesV1Repository.findTotalPaidAmountByInvoiceId(invoice.getInvoiceId());
        String paymentStatus = null;
        if (invoice.getPaymentStatus() != null) {
            paymentStatus = Utils.capitalize(PaymentStatus.valueOf(invoice.getPaymentStatus()).getDisplayName());
        }
        if (invoice.isCancelled()) {
            paymentStatus = "Cancelled";
        }

        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getInvoiceNumber(),
                Utils.capitalize(invoice.getInvoiceType()),
                paymentStatus,
                invoice.getTotalAmount(),
                invoice.getTotalAmount() - paidAmount,
                paidAmount,
                invoice.getInvoiceDueDate() != null
                        ? dateFormat.format(invoice.getInvoiceDueDate())
                        : null,
                invoice.getInvoiceGeneratedDate() != null
                        ? dateFormat.format(invoice.getInvoiceGeneratedDate())
                        : null,
                invoice.getInvoiceItems() != null
                        ? invoice.getInvoiceItems().stream()
                        .map(item -> {
                            String displayName;

                            if (item.getOtherItem() != null && !item.getOtherItem().trim().isEmpty()) {
                                displayName = Utils.capitalize(item.getOtherItem().trim());
                            } else {
                                displayName = Utils.capitalize(item.getInvoiceItem());
                            }

                            return new ItemResponse(displayName, item.getAmount());
                        })
                        .toList()
                        : List.of()
        );
    }
}

