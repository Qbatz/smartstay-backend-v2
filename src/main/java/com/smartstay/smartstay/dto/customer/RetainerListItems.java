package com.smartstay.smartstay.dto.customer;

public record RetainerListItems(String invoiceId,
                                String invoiceNo,
                                String status,
                                String date,
                                String invoiceType,
                                Double amount,
                                Double availableBalance,
                                String paymentMode) {
}
