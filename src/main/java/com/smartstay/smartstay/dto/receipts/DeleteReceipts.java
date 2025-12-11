package com.smartstay.smartstay.dto.receipts;

public record DeleteReceipts(String invoiceId, Double invoiceAmount, boolean status) {
}
