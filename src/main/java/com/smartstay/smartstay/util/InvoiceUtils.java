package com.smartstay.smartstay.util;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;

public class InvoiceUtils {
    public static String getInvoicePaymentStatusByInvoice(InvoicesV1 invoices) {
        if (invoices != null && invoices.getPaymentStatus() != null) {
            String paymentStatus = null;
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
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
                paymentStatus = "Refunded";
            }
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                paymentStatus = "Pending Refund";
            }
            else if (invoices.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {
                paymentStatus = "Partially Refunded";
            }

            return paymentStatus;
        }
        return null;
    }

    public static String getInvoicePaymentStatusByStatus(String status) {
        if (status != null) {
            String paymentStatus = null;
            if (status.equalsIgnoreCase(PaymentStatus.PAID.name())) {
                paymentStatus = "Paid";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.PENDING.name())) {
                paymentStatus = "Pending";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                paymentStatus = "Partial Payment";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.ADVANCE_IN_HAND.name())) {
                paymentStatus = "Over pay";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
                paymentStatus = "Refunded";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
                paymentStatus = "Pending Refund";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {
                paymentStatus = "Partially Refunded";
            }
            else if (status.equalsIgnoreCase(PaymentStatus.CANCELLED.name())) {
                paymentStatus = "Cancelled";
            }

            return paymentStatus;
        }
        return null;
    }

    public static String getInvoiceType(InvoicesV1 invoicesV1) {
        String invoiceType = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADDITIONAL_ADVANCE.name())) {
            invoiceType = "Advance";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.OTHER.name())) {
            invoiceType = "Other";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
            invoiceType = "Reassign-Rent";
        }

        return invoiceType;
    }
}
