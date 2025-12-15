package com.smartstay.smartstay.Wrappers.invoices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.InvoiceUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class NewInvoiceListMapper implements Function<InvoicesV1, InvoicesList> {
    List<Customers> listCustomers = null;
    List<Users> listCreatedBy = null;

    public NewInvoiceListMapper(List<Customers> customers, List<Users> createdBy) {
        this.listCustomers = customers;
        this.listCreatedBy = createdBy;
    }

    @Override
    public InvoicesList apply(InvoicesV1 invoicesV1) {
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String firstName = null;
        String lastName = null;
        String profilePic = null;
        String invoiceType = null;
        boolean isRefundable = false;
        boolean isCancelled = false;

        Double dueAmount = 0.0;
        Double paidAmount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }

        double totalAmount = invoicesV1.getTotalAmount();
        long gstAmount = 0;
        if (invoicesV1.getGst() != null) {
            totalAmount = totalAmount + invoicesV1.getGst();
        }

        if (invoicesV1.getTotalAmount() < 0) {
            dueAmount = invoicesV1.getTotalAmount() + paidAmount;
        }
        else {
            dueAmount = invoicesV1.getTotalAmount() - paidAmount;
        }

        Customers customers = listCustomers
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                .findFirst()
                .orElse(null);

        if (customers != null) {
            firstName = customers.getFirstName();
            lastName = customers.getLastName();
            profilePic = customers.getProfilePic();
            if (customers.getFirstName() != null) {
                fullName.append(customers.getFirstName());
                initials.append(customers.getFirstName().toUpperCase().charAt(0));
            }
            if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
                fullName.append(" ");
                fullName.append(customers.getLastName());
                initials.append(customers.getLastName().toUpperCase().charAt(0));
            }
            else {
                if (customers.getFirstName() != null && customers.getFirstName().length() > 1) {
                    initials.append(customers.getFirstName().toUpperCase().charAt(1));
                }
            }
        }

        String paymentStatus = null;
        if (invoicesV1.getPaymentStatus() != null) {
            paymentStatus = InvoiceUtils.getInvoicePaymentStatusByStatus(invoicesV1.getPaymentStatus());
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.OTHERS.name())) {
            invoiceType = "Others";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
            if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                if (invoicesV1.getTotalAmount() - paidAmount < 0) {
                    isRefundable = true;
                }
            }

        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign-Rent";
        }

        if (invoicesV1.isCancelled()) {
            paymentStatus = "Cancelled";
            isCancelled = true;
        }

        return new InvoicesList(firstName,
                lastName,
                fullName.toString(),
                invoicesV1.getCustomerId(),
                initials.toString(),
                profilePic,
                isRefundable,
                Utils.roundOfDouble(totalAmount),
                Utils.roundOfDouble(invoicesV1.getTotalAmount()),
                invoicesV1.getInvoiceId(),
                Utils.roundOfDouble(paidAmount),
                Utils.roundOfDouble(dueAmount),
                invoicesV1.getCgst(),
                invoicesV1.getSgst(),
                gstAmount,
                Utils.dateToString(invoicesV1.getCreatedAt()),
                invoicesV1.getCreatedBy(),
                invoicesV1.getHostelId(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                invoiceType,
                paymentStatus,
                Utils.dateToString(invoicesV1.getUpdatedAt()),
                invoicesV1.getInvoiceNumber(),
                isCancelled,
                null);
    }
}
