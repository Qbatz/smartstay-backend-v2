package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.responses.InvoiceRedemption.InvoiceInfo;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class InvoicesInvoiceInfoMapper implements Function<InvoicesV1, InvoiceInfo> {
    @Override
    public InvoiceInfo apply(InvoicesV1 invoicesV1) {
        String redemptionStatus = null;

        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            if (invoicesV1.getPaidAmount() != null) {
                double deductionAmount = 0.0;
                if (invoicesV1.getDeductionAmount() != null) {
                    deductionAmount = invoicesV1.getDeductionAmount();
                }
                double totalAmount = invoicesV1.getTotalAmount() - deductionAmount;
                if (invoicesV1.getBalanceAmount() != null) {
                    if (totalAmount == invoicesV1.getBalanceAmount()) {
                        redemptionStatus = "Available";
                    }
                    else if (invoicesV1.getBalanceAmount() < totalAmount) {
                        redemptionStatus = "Partially Available";
                    }
                    else {
                        redemptionStatus = "Redeemed";
                    }
                }
            }
        }

        return new InvoiceInfo(invoicesV1.getInvoiceId(),
                invoicesV1.getInvoiceNumber(),
                invoicesV1.getInvoiceType(),
                invoicesV1.getTotalAmount(),
                invoicesV1.getPaidAmount(),
                invoicesV1.getBalanceAmount() != null ? Math.round(invoicesV1.getBalanceAmount() * 100.0) / 100.0 : null,
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                redemptionStatus);
    }
}
