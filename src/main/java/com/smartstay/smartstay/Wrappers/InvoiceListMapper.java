package com.smartstay.smartstay.Wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.InvoiceUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceListMapper implements Function<Invoices, InvoicesList> {
    @Override
    public InvoicesList apply(Invoices invoices) {
        boolean isRefundable = false;
        boolean isCancelled = false;
        StringBuilder fullNameBuilder = new StringBuilder();
        fullNameBuilder.append(invoices.getFirstName());
        fullNameBuilder.append(" ");
        fullNameBuilder.append(invoices.getLastName());
        String invoiceMode = null;

        StringBuilder initials = new StringBuilder();
        if (invoices.getFirstName() != null) {
            initials.append(invoices.getFirstName().toUpperCase().charAt(0));
        }
        if (invoices.getLastName() != null && !invoices.getLastName().trim().equalsIgnoreCase("")) {
            initials.append(invoices.getLastName().toUpperCase().charAt(0));
        }
        else {
            if (invoices.getFirstName().length() > 1) {
                initials.append(invoices.getFirstName().toUpperCase().charAt(1));
            }
        }

        Double dueAmount = 0.0;
        Double paidAmount = 0.0;
        if (invoices.getPaidAmount() != null) {
            paidAmount = invoices.getPaidAmount();
        }

        String invoiceType = null;
        String paymentStatus = null;
        if (invoices.getPaymentStatus() != null) {
           paymentStatus = InvoiceUtils.getInvoicePaymentStatusByStatus(invoices.getPaymentStatus());
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
        else if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
            if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                if (invoices.getTotalAmount() - paidAmount < 0) {
                    isRefundable = true;
                }
            }

        }
        else if (invoices.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign-Rent";
        }


        if (invoices.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())) {
            invoiceMode = "Recurring";
        }
        else if (invoices.getInvoiceMode().equalsIgnoreCase(InvoiceMode.MANUAL.name())) {
            invoiceMode = "Manual";
        }
        else if (invoices.getInvoiceMode().equalsIgnoreCase(InvoiceMode.AUTOMATIC.name())) {
            invoiceMode = "Automatic";
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

        if (invoices.getCancelled() != null && invoices.getCancelled()) {
            paymentStatus = "Cancelled";
            isCancelled = true;
        }

        if (invoices.getTotalAmount() < 0) {
            dueAmount = invoices.getTotalAmount() + paidAmount;
        }
        else {
            dueAmount = invoices.getTotalAmount() - paidAmount;
        }


        return new InvoicesList(invoices.getFirstName(),
                invoices.getLastName(),
                fullNameBuilder.toString(),
                invoices.getCustomerId(),
                initials.toString(),
                invoices.getProfilePic(),
                isRefundable,
                Utils.roundOfDouble(totalAmount),
                Utils.roundOfDouble(invoices.getTotalAmount()),
                invoices.getInvoiceId(),
                Utils.roundOfDouble(paidAmount),
                Utils.roundOfDouble(dueAmount),
                invoices.getCgst(),
                invoices.getSgst(),
                gstAmount,
                Utils.dateToString(invoices.getCreatedAt()),
                invoices.getCreatedBy(),
                invoices.getHostelId(),
                Utils.dateToString(invoices.getInvoiceStartDate()),
                Utils.dateToString(invoices.getInvoiceDueDate()),
                invoiceType,
                invoiceMode,
                paymentStatus,
                Utils.dateToString(invoices.getUpdatedAt()),
                invoices.getInvoiceNumber(),
                isCancelled,
                listDeductions);
    }
}
