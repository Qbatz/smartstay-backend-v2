package com.smartstay.smartstay.payloads.transactions;

import java.util.List;

public record TenantPayment(
        String tenantId,
        String paymentDate,
        String referenceId,
        String bankId,
        String description,
        List<TenantPaymentInvoice> invoices) {
}
