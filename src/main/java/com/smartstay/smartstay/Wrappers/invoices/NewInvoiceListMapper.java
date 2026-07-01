package com.smartstay.smartstay.Wrappers.invoices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.responses.invoices.InvoicesApplied;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.InvoiceUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class NewInvoiceListMapper implements Function<InvoicesV1, InvoicesList> {
    List<Customers> listCustomers = null;
    List<Users> listCreatedBy = null;
    List<InvoiceDiscounts> listInvoiceDiscounts = null;
    List<InvoiceRedemption> listAppliedInvoices = null;
    List<InvoicesV1> listAdvances = null;

    List<InvoicesV1> pendingInvoices = null;

    public NewInvoiceListMapper(List<Customers> customers, List<Users> createdBy, List<InvoiceDiscounts> listInvoiceDiscounts, List<InvoiceRedemption> listAppliedInvoices, List<InvoicesV1> listAdvanceInvoices, List<InvoicesV1> pendingInvoices) {
        this.listCustomers = customers;
        this.listCreatedBy = createdBy;
        this.listInvoiceDiscounts = listInvoiceDiscounts;
        this.listAppliedInvoices = listAppliedInvoices;
        this.listAdvances = listAdvanceInvoices;
        this.pendingInvoices = pendingInvoices;
    }

    @Override
    public InvoicesList apply(InvoicesV1 invoicesV1) {
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String firstName = null;
        String lastName = null;
        String profilePic = null;
        String invoiceType = null;
        String invoiceMode = null;
        boolean isRefundable = false;
        boolean isCancelled = false;
        String cancelledOn = null;
        Double discountPercentage = 0.0;
        Double discountAmount = 0.0;
        boolean canEdit = false;
        boolean canUnpaid = false; // computed below based on payment status
        boolean isInvoicesApplied = false;
        String customerStatus = null;
        //should used for advance invoices.
        boolean canRedeem = false;
        boolean canApplyFromAdvance = false;
        InvoicesApplied invoicesApplied = null;
        InvoicesV1 advanceInvoiceForCurrentCustomer = null;

        Double dueAmount = 0.0;
        Double paidAmount = 0.0;
        Double invoiceAmount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }

        if (invoicesV1.getTotalAmount() != null) {
            invoiceAmount = invoicesV1.getTotalAmount();
        }

        double totalAmount = 0.0;
        if (invoicesV1.getSubTotal() != null) {
            totalAmount = invoicesV1.getSubTotal();
        }
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
            customerStatus = customers.getCurrentStatus();
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

        if (listAdvances != null && !invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            advanceInvoiceForCurrentCustomer = listAdvances
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (advanceInvoiceForCurrentCustomer != null) {
                if (advanceInvoiceForCurrentCustomer.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) ||
                advanceInvoiceForCurrentCustomer.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                    if (advanceInvoiceForCurrentCustomer.getPaidAmount() != null) {
                        if (advanceInvoiceForCurrentCustomer.getBalanceAmount() != null && advanceInvoiceForCurrentCustomer.getBalanceAmount() > 0) {
                            canApplyFromAdvance = true;
                            canRedeem = false;
                        }

                        if (advanceInvoiceForCurrentCustomer.isCancelled()) {
                            canApplyFromAdvance = false;
                        }
                    }
                }
            }

            if (canApplyFromAdvance) {
                if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                    canApplyFromAdvance = false;
                }
                if (invoicesV1.isCancelled()) {
                    canApplyFromAdvance = false;
                }
            }
        }


        String paymentStatus = null;
        if (invoicesV1.getPaymentStatus() != null) {
            paymentStatus = InvoiceUtils.getInvoicePaymentStatusByStatus(invoicesV1.getPaymentStatus());
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
            canEdit = true;
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
            canEdit = false;
            canRedeem = true;
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
            canEdit = false;
            if (invoicesV1.getBalanceAmount() != null && invoicesV1.getBalanceAmount() > 0) {
                canRedeem = true;
            }
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.OTHERS.name())) {
            invoiceType = "Others";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
            canEdit = false;
            if (invoicesV1.getPaymentStatus() != null) {
                if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                    if (invoicesV1.getTotalAmount() - paidAmount < 0) {
                        isRefundable = true;
                    }
                }
            }
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign-Rent";
            canEdit = true;
        }

        if (invoicesV1.isCancelled()) {
            paymentStatus = "Cancelled";
            isCancelled = true;
            canEdit = false;
            if (invoicesV1.getCancelledDate() != null) {
                cancelledOn = Utils.dateToString(invoicesV1.getCancelledDate());
            }
        }

        if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())) {
            invoiceMode = "Recurring";
            if (!canEdit) {
                canEdit = true;
            }
        }
        else if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.MANUAL.name())) {
            invoiceMode = "Manual";
        }
        else if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.AUTOMATIC.name())) {
            invoiceMode = "Automatic";

        }

        if (listInvoiceDiscounts != null) {
            InvoiceDiscounts ids = listInvoiceDiscounts
                    .stream()
                    .filter(i -> i.getInvoiceId().equalsIgnoreCase(invoicesV1.getInvoiceId()))
                    .findFirst()
                    .orElse(null);
            if (ids != null) {
                if (canEdit) {
                    canEdit = false;
                }
                if (ids.getDiscountPercentage() != null) {
                    discountPercentage = Utils.roundOffWithTwoDigit(ids.getDiscountPercentage());
                }
                if (ids.getDiscountAmount() != null) {
                    discountAmount = Utils.roundOffWithTwoDigit(ids.getDiscountAmount());
                }
            }
        }

        if (!listAppliedInvoices.isEmpty()) {

            double appliedInvoiceAmount = listAppliedInvoices
                    .stream()
                    .filter(i -> i.getTargetInvoiceId().equalsIgnoreCase(invoicesV1.getInvoiceId()))
                    .mapToDouble(i -> {
                        if (i.getRedemptionAmount() != null) {
                            return i.getRedemptionAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            long appliedInvoiceCount = listAppliedInvoices
                    .stream()
                    .filter(i -> i.getTargetInvoiceId().equalsIgnoreCase(invoicesV1.getInvoiceId()))
                    .count();

            if (appliedInvoiceCount > 0) {
                isInvoicesApplied = true;
                invoicesApplied = new InvoicesApplied((int) appliedInvoiceCount, appliedInvoiceAmount);
            }
        }

        if (invoicesV1.getPaymentStatus() != null) {
            if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                canEdit = false;
            }
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            if (canEdit) {
                canEdit = false;
            }
        }

        if (invoicesV1.isCancelled()) {
            canRedeem = false;
            if (canEdit) {
                canEdit = false;
            }
        }

        if (invoicesV1.isDiscounted()) {
            if (canEdit) {
                canEdit = false;
            }
        }

       if (canRedeem) {
           if (customerStatus.equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name()) || customerStatus.equalsIgnoreCase(CustomerStatus.VACATED.name())) {
               canRedeem = false;
           }
       }

       if (canRedeem){
           if (pendingInvoices != null) {
               List<InvoicesV1> pendingInvoice = pendingInvoices
                       .stream()
                       .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                       .toList();
               if (pendingInvoice.isEmpty()) {
                   canRedeem = false;
               }
           }
       }

       if (canApplyFromAdvance) {
           if (customerStatus.equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name()) || customerStatus.equalsIgnoreCase(CustomerStatus.VACATED.name())) {
               canApplyFromAdvance = false;
           }
       }
       if (canApplyFromAdvance) {
           if (isRefundable) {
               canApplyFromAdvance = false;
           }
       }

       if (canRedeem) {
           if (isRefundable) {
               canRedeem = false;
           }
       }

       // canUnpaid = true only when amount has actually been paid (PAID or PARTIAL_PAYMENT)
       // AND the customer is NOT settlement-generated (settlement invoices must not be reversed)
       if (invoicesV1.getPaymentStatus() != null) {
           boolean isPaidOrPartial = invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())
                   || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name());
           boolean isSettlementGenerated = customers != null
                   && CustomerStatus.SETTLEMENT_GENERATED.name().equalsIgnoreCase(customers.getCurrentStatus());
           canUnpaid = isPaidOrPartial && !isSettlementGenerated;
       }

        return new InvoicesList(firstName,
                lastName,
                fullName.toString(),
                invoicesV1.getCustomerId(),
                initials.toString(),
                profilePic,
                isRefundable,
                Utils.roundOffWithTwoDigit(invoiceAmount),
                Utils.roundOffWithTwoDigit(totalAmount),
                invoicesV1.getInvoiceId(),
                Utils.roundOffWithTwoDigit(paidAmount),
                Utils.roundOffWithTwoDigit(dueAmount),
                invoicesV1.isDiscounted(),
                discountAmount,
                discountPercentage,
                invoicesV1.getCgst(),
                invoicesV1.getSgst(),
                gstAmount,
                Utils.dateToString(invoicesV1.getCreatedAt()),
                invoicesV1.getCreatedBy(),
                invoicesV1.getHostelId(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                invoiceType,
                invoiceMode,
                paymentStatus,
                Utils.dateToString(invoicesV1.getUpdatedAt()),
                invoicesV1.getInvoiceNumber(),
                isCancelled,
                cancelledOn,
                canEdit,
                canUnpaid,
                isInvoicesApplied,
                canRedeem,
                canApplyFromAdvance,
                invoicesApplied);
    }
}
