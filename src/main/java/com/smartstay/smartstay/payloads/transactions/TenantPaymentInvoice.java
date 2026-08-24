package com.smartstay.smartstay.payloads.transactions;

public record TenantPaymentInvoice(
        String invoiceId,
        Double amount) {
}
