package com.smartstay.smartstay.Wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceListMapper implements Function<Invoices, InvoicesList> {
    @Override
    public InvoicesList apply(Invoices invoices) {
        StringBuilder fullNameBuilder = new StringBuilder();
        fullNameBuilder.append(invoices.getFirstName());
        fullNameBuilder.append(" ");
        fullNameBuilder.append(invoices.getLastName());
        Double paidAmount = 0.0;
        if (invoices.getPaidAmount() != null) {
            paidAmount = invoices.getPaidAmount();
        }
        String invoiceType = null;
        String paymentStatus = null;
        if (invoices.getPaymentStatus() != null) {
            if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                paymentStatus = "Paid";
            }
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                paymentStatus = "Pending";
            }
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                paymentStatus = "Partial Payment";
            }
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.ADVANCE_IN_HAND.name())) {
                paymentStatus = "Over pay";
            }
        }


        if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
        }
        else if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        }
        else if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        }
        else if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.OTHERS.name())) {
            invoiceType = "Others";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Deductions> listDeductions = null;
        try {
             listDeductions = mapper.readValue(
                    invoices.getDeductions(), new TypeReference<List<Deductions>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        double totalAmount = invoices.getTotalAmount();
        long gstAmount = 0;
        if (invoices.getGst() != null) {
            totalAmount = totalAmount + invoices.getGst();
        }
        if (invoices.getGst() != null) {
            gstAmount = Math.round(invoices.getGst());
        }


        return new InvoicesList(invoices.getFirstName(),
                invoices.getLastName(),
                fullNameBuilder.toString(),
                invoices.getCustomerId(),
                Math.ceil(totalAmount),
                Math.ceil(invoices.getTotalAmount()),
                invoices.getInvoiceId(),
                Math.round(paidAmount),
                Math.round(totalAmount-paidAmount),
                invoices.getCgst(),
                invoices.getSgst(),
                gstAmount,
                Utils.dateToString(invoices.getCreatedAt()),
                invoices.getCreatedBy(),
                invoices.getHostelId(),
                Utils.dateToString(invoices.getInvoiceStartDate()),
                Utils.dateToString(invoices.getInvoiceDueDate()),
                invoiceType,
                paymentStatus,
                Utils.dateToString(invoices.getUpdatedAt()),
                invoices.getInvoiceNumber(),
                listDeductions);
    }
}
