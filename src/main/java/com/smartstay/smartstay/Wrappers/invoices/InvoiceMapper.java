package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.invoice.ItemResponse;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.util.InvoiceUtils;
import com.smartstay.smartstay.util.Utils;

import java.text.SimpleDateFormat;
import java.util.List;

public class InvoiceMapper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static InvoiceResponse toResponse(InvoicesV1 invoice) {
        Double paidAmount = 0.0;
        if (invoice.getPaidAmount() != null) {
            paidAmount = invoice.getPaidAmount();
        }
        String paymentStatus = null;
        if (invoice.getPaymentStatus() != null) {
            paymentStatus = InvoiceUtils.getInvoicePaymentStatusByStatus(invoice.getPaymentStatus());
        }
        if (invoice.isCancelled()) {
            paymentStatus = "Cancelled";
        }

        double dueAmount = 0.0;
        if (invoice.getPaidAmount() != null) {
            if (invoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                dueAmount = invoice.getTotalAmount() + invoice.getPaidAmount();
            }
            if (invoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
                dueAmount = 0;
            }
            if (invoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                dueAmount = invoice.getTotalAmount() - invoice.getPaidAmount();
            }
            if (invoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                dueAmount = 0;
            }
            if (invoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {
                dueAmount = invoice.getTotalAmount() + invoice.getPaidAmount();
            }
        }

        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getInvoiceNumber(),
                Utils.capitalize(invoice.getInvoiceType()),
                paymentStatus,
                Utils.roundOfDouble(invoice.getTotalAmount()),
                Utils.roundOfDouble(dueAmount),
                Utils.roundOfDouble(paidAmount),
                invoice.getInvoiceDueDate() != null
                        ? dateFormat.format(invoice.getInvoiceDueDate())
                        : null,
                invoice.getInvoiceStartDate()!= null
                        ? dateFormat.format(invoice.getInvoiceStartDate())
                        : null,
                invoice.getInvoiceMode(),
                invoice.isDiscounted(),
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

