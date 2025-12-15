package com.smartstay.smartstay.filterOptions.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class InvoiceFilterOptions {
    List<PaymentStatus> paymentStatus = null;
    List<InvoiceMode> invoiceModes = null;
    List<InvoiceType> invoiceTypes = null;
    List<CreatedBy> createdBy = null;

    public InvoiceFilterOptions() {
        invoiceModes = new ArrayList<>();
        InvoiceMode mode1 = new InvoiceMode("Recurring", com.smartstay.smartstay.ennum.InvoiceMode.RECURRING.name());
        InvoiceMode mode2 = new InvoiceMode("Automatic", com.smartstay.smartstay.ennum.InvoiceMode.AUTOMATIC.name());
        InvoiceMode mode3 = new InvoiceMode("Manual", com.smartstay.smartstay.ennum.InvoiceMode.MANUAL.name());

        invoiceModes.add(mode1);
        invoiceModes.add(mode2);
        invoiceModes.add(mode3);

        paymentStatus = new ArrayList<>();
        PaymentStatus status1 = new PaymentStatus("Partial Payment", com.smartstay.smartstay.ennum.PaymentStatus.PARTIAL_PAYMENT.name());
        PaymentStatus status2 = new PaymentStatus("Paid", com.smartstay.smartstay.ennum.PaymentStatus.PAID.name());
        PaymentStatus status3 = new PaymentStatus("Pending", com.smartstay.smartstay.ennum.PaymentStatus.PENDING.name());
        PaymentStatus status4 = new PaymentStatus("Refunded", com.smartstay.smartstay.ennum.PaymentStatus.REFUNDED.name());
        PaymentStatus status5 = new PaymentStatus("Pending Refund", com.smartstay.smartstay.ennum.PaymentStatus.PENDING_REFUND.name());
        PaymentStatus status6 = new PaymentStatus("Partial Refund", com.smartstay.smartstay.ennum.PaymentStatus.PARTIAL_REFUND.name());

        paymentStatus.add(status1);
        paymentStatus.add(status2);
        paymentStatus.add(status3);
        paymentStatus.add(status4);
        paymentStatus.add(status5);
        paymentStatus.add(status6);

        invoiceTypes = new ArrayList<>();
        InvoiceType type1 = new InvoiceType("Rent", com.smartstay.smartstay.ennum.InvoiceType.RENT.name());
        InvoiceType type2 = new InvoiceType("Settlement", com.smartstay.smartstay.ennum.InvoiceType.SETTLEMENT.name());
        InvoiceType type3 = new InvoiceType("Advance", com.smartstay.smartstay.ennum.InvoiceType.ADVANCE.name());
        InvoiceType type4 = new InvoiceType("Reassign Rent", com.smartstay.smartstay.ennum.InvoiceType.REASSIGN_RENT.name());

        invoiceTypes.add(type1);
        invoiceTypes.add(type2);
        invoiceTypes.add(type3);
        invoiceTypes.add(type4);
    }
}
